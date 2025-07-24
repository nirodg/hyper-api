pipeline {
    agent any

    tools {
        maven 'Mvn 3.9.10'   // name from Jenkins tool config
#        jdk 'jdk21'           // name from Jenkins tool config
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

        stage('Show JDK Version') {
            steps {
                sh 'java -version'
                sh 'echo JAVA_HOME is $JAVA_HOME'
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
