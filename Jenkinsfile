pipeline {
    agent any
    stages {
        stage('Checkout Code') {
            steps {
                checkout([$class: 'GitSCM', 
                          branches: [[name: '*/main']],
                          userRemoteConfigs: [[url: 'https://github.com/chanheess/calendar.git', 
                                               credentialsId: 'github']]])
            }
        }
        stage('Setup Environment') {
            steps {
                sh 'export $(cat .env | xargs)'
            }
        }
        stage('Build Application') {
            steps {
                sh './gradlew clean build'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker buildx build --platform linux/amd64 -t chanheess/chcalendar . --push'
            }
        }
        stage('Deploy to EC2') {
            steps {
                sshagent(['ec2']) {
                    sh '''
                    ssh -o StrictHostKeyChecking=no ec2-user@<EC2_IP> "
                    docker pull chanheess/chcalendar &&
                    docker-compose down &&
                    docker-compose up -d
                    "
                    '''
                }
            }
        }
    }
    post {
        always {
            echo 'Build and Deployment Process Complete!'
        }
        failure {
            echo 'Build or Deployment Failed. Check Logs.'
        }
    }
}
