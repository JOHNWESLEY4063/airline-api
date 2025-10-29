pipeline {
    agent any

    tools {
        jdk 'jdk11'
        maven 'maven3'
    }

    stages {
        stage('2. Build Java Service') {
            steps {
                echo 'Building Java/Spring Boot Service and running tests...'
                bat 'mvn clean install'
            }
        }

        stage('3. Package Java Artifact') {
            steps {
                echo 'Packaging the final executable JAR...'
                bat 'mvn package'
            }
            post {
                always {
                    echo 'Archiving Extent HTML Report...'
                    archiveArtifacts artifacts: 'target/ExtentReport.html', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline Succeeded! Archiving final build artifact (JAR file)...'
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
        }
    }
}