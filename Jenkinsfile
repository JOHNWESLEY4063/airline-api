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
            // Archive the improved TestNG HTML report
            archiveArtifacts artifacts: 'target/surefire-reports/index.html', allowEmptyArchive: true
        }
    }
}