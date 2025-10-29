pipeline {
    agent any

    tools {
        jdk 'jdk11'
        maven 'maven3'
    }

    stages {
        stage('2. Build Java Service') {
            steps {
                echo 'Building Java/Spring Boot Service and running integration tests...'
                bat 'mvn clean install'
            }
        }

        stage('3. Start Helper API') {
            steps {
                echo 'Installing dependencies and starting Node.js Helper API...'
                dir('database-helper-api') {
                    bat 'npm install'
                    bat 'start /B node index.js'
                }

                echo 'Waiting for Node.js Helper API Health Check (http://localhost:3001/query)...'
                bat 'powershell -Command "for ($i=1; $i -le 12; $i++) { try { Invoke-WebRequest -Uri http://localhost:3001/query -TimeoutSec 5 } catch { Start-Sleep -Seconds 5 } }"'
            }
        }

        stage('4. Run API Tests (Newman)') {
            steps {
                echo 'Executing Postman Collection with Newman...'
                dir('postman') {
                    bat 'newman run "Airline_API.postman_collection.json" -e "Local_DB.postman_environment.json" -r cli,htmlextra'
                }
            }
            post {
                always {
                    echo 'Archiving Newman Test Reports...'
                    archiveArtifacts artifacts: 'postman/newman-html-report/*.html', allowEmptyArchive: true

                    echo 'Stopping background Node.js Helper API process...'
                    bat "FOR /F \"tokens=5\" %i IN ('netstat -ano ^| findstr :3001') DO TaskKill /PID %i /F"
                }
            }
        }

        stage('5. Package Java Artifact') {
            steps {
                echo 'Packaging the final executable JAR...'
                bat 'mvn package'
            }
            post {
                always {
                    echo 'Archiving Extent Report...'
                    archiveArtifacts artifacts: 'target/extent-report.html', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline Succeeded! Archiving final build artifact (JAR file)...'
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
        }
        always {
            // Final cleanup
            bat "FOR /F \"tokens=5\" %i IN ('netstat -ano ^| findstr :3001') DO TaskKill /PID %i /F"
        }
    }
}