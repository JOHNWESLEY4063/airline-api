pipeline {
    agent any

    tools {
        jdk 'jdk11'
        maven 'maven3'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building the project...'
                // Use 'sh' for Linux/macOS
                sh 'mvn compile'
            }
        }
        stage('Test') {
            steps {
                echo 'Running tests...'
                // Use 'sh' for Linux/macOS
                sh 'mvn test'
            }
        }
    }

    post {
        always {
            echo 'Archiving test results...'
            archiveArtifacts artifacts: 'target/surefire-reports/emailable-report.html', allowEmptyArchive: true
        }
    }
}