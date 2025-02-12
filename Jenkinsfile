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
                dir('backend') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build'
                    sh 'docker buildx build --platform linux/amd64 -t chanheess/chcalendar . --push'
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
                sshagent(['ec2']) {
                    sh '''
                    scp frontend/build.tar.gz ${EC2_IP}:/home/ec2-user/
                    scp backend/nginx/default-ec2.conf ${EC2_IP}:/home/ec2-user/nginx/default.conf
                    scp backend/docker-compose-ec2.yml ${EC2_IP}:/home/ec2-user/docker-compose.yml
                    
                    ssh -o StrictHostKeyChecking=no ${EC2_IP} "
                        sudo mkdir -p /var/www/html/frontend/build &&
                        sudo rm -rf /var/www/html/frontend/build/* &&
                        sudo tar -xzf /home/ec2-user/build.tar.gz -C /var/www/html/frontend/build &&
                        
                        docker pull chanheess/chcalendar &&
                        
                        sudo docker-compose up -d
                    "
                    '''
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
