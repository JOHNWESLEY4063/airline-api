pipeline {
    agent any

    tools {
        jdk 'jdk11'
        maven 'maven3'
    }

    stages {
        stage('Build Java Service') {
            steps {
                echo 'Building the Java project...'
                bat 'mvn clean compile'
            }
        }

        stage('Start Docker DB') {
            steps {
                echo 'Starting the MySQL database container via Docker Compose...'
                // Use 'docker-compose up' to start the database service in the background (-d)
                bat 'docker-compose up -d mysql_db'
                echo 'Waiting 15 seconds for MySQL to fully initialize before running tests...'
                // A brief pause is often necessary for the MySQL container to be fully ready
                sleep 15
            }
        }

        stage('Run Integration Tests') {
            steps {
                echo 'Running TestNG integration tests against the Dockerized DB...'
                bat 'mvn test'
            }
        }
    }

    post {
        always {
            echo 'Archiving test results...'
            // Archive the beautiful Extent Report
            archiveArtifacts artifacts: 'target/ExtentReport.html', allowEmptyArchive: true

            echo 'Stopping and removing Docker containers...'
            // Use 'docker-compose down' to stop the services and clean up the environment
            bat 'docker-compose down'
        }
    }
}