pipeline {
    agent any
    environment {
        EC2_IP = "${env.EC2_IP}" // Jenkins에 설정된 EC2 IP 환경변수
    }
    stages {
        stage('Checkout Code') {
            steps {
                checkout([$class: 'GitSCM', 
                          branches: [[name: '*/main']],
                          userRemoteConfigs: [[url: 'https://github.com/chanheess/calendar.git', 
                                               credentialsId: 'github']]])
            }
        }
        stage('Build Backend') {
            steps {
                script {
                    def envContent = readFile('.env')
                    def envVars = []
                    envContent.split('\n').each { line ->
                        line = line.trim()
                        if(line && !line.startsWith("#")) {
                            envVars.add(line)
                        }
                    }
                    echo "Loaded env vars: ${envVars}"
        
                    withEnv(envVars) {
                        withCredentials([file(credentialsId: 'service-account-key', variable: 'SERVICE_ACCOUNT_KEY')]) {
                            sh '''
                                rm -f backend/src/main/resources/serviceAccountKey.json
                                cp "$SERVICE_ACCOUNT_KEY" backend/src/main/resources/serviceAccountKey.json
                            '''
                            dir('backend') {
                                sh 'chmod +x gradlew'
                                sh './gradlew clean build'
                                sh 'docker buildx build --platform linux/amd64 -t chanheess/chcalendar . --push'
                            }
                        }
                    }
                }
            }
        }
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                    sh 'tar -czf build.tar.gz build'
                }
            }
        }
        stage('Deploy to EC2') {
            steps {
                withCredentials([file(credentialsId: 'service-account-key', variable: 'SERVICE_ACCOUNT_KEY')]) {
                    sshagent(['ec2']) {
                        sh '''
                        scp frontend/build.tar.gz ${EC2_IP}:/home/ec2-user/
                        scp backend/nginx/default-ec2.conf ${EC2_IP}:/home/ec2-user/nginx/default.conf
                        scp backend/docker-compose-ec2.yml ${EC2_IP}:/home/ec2-user/docker-compose.yml
                        scp -o StrictHostKeyChecking=no $SERVICE_ACCOUNT_KEY ${EC2_IP}:/home/ec2-user/serviceAccountKey.json
                        
                        ssh -o StrictHostKeyChecking=no ${EC2_IP} "
                            sudo mkdir -p /var/www/html/frontend &&
                            sudo rm -rf /var/www/html/frontend/* &&
                            sudo tar -xzf /home/ec2-user/build.tar.gz -C /var/www/html/frontend &&
                            
                            docker pull chanheess/chcalendar &&
                            
                            sudo docker-compose up -d
                        "
                        '''
                    }
                }
            }
        }
    }
    post {
        success {
            echo 'Build and Deployment Process Completed Successfully!'
        }
        failure {
            echo 'Build or Deployment Failed. Check Logs.'
        }
    }
}
