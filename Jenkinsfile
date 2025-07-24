pipeline {
    agent any

    tools {
        maven '3.9.10'   // name from Jenkins tool config
        jdk 'JDK 21'           // name from Jenkins tool config
    }

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean install'
            }
        }

    }

    post {
        always {
            cleanWs()
        }
    }
}
