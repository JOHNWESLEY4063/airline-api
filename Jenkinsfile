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
                // Use 'bat' for Windows
                bat 'mvn compile'
            }
        }
        stage('Test') {
            steps {
                echo 'Running tests...'
                // Use 'bat' for Windows
                bat 'mvn test'
            }
        }
    }

    post {
        always {
            echo 'Archiving test results...'
            // Archive the beautiful Extent Report
            archiveArtifacts artifacts: 'target/ExtentReport.html', allowEmptyArchive: true
        }
    }
}
