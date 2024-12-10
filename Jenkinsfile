pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/chanheess/calendar.git'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t chanheess/chcalendar .'
            }
        }
        stage('Push to Docker Hub') {
            steps {
                withDockerRegistry([credentialsId: 'dockerhub', url: 'https://index.docker.io/v1/']) {
                    sh 'docker push chanheess/chcalendar'
                }
            }
        }
        stage('Deploy with Docker Compose') {
            steps {
                sh '''
                docker pull chanheess/chcalendar
                docker-compose down
                docker-compose up -d
                '''
            }
        }
    }
    post {
        always {
            echo 'Build finished!'
        }
    }
}
