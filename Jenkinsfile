pipeline {
    agent any

    tools {
        jdk 'jdk11'
        maven 'maven3'
    }

    stages {
        // Stage 1 is implicitly SCM checkout

        stage('2. Build Java Service') {
            steps {
                echo 'Building Java/Spring Boot Service and running integration tests...'
                // mvn clean install runs the tests successfully against the local workspace DB
                bat 'mvn clean install'
            }
        }

        stage('3. Build & Start Helper API (Docker)') {
            steps {
                echo 'Building and starting Node.js Helper API container...'
                dir('database-helper-api') {
                    // This uses the Dockerfile you created earlier
                    bat 'docker build -t helper-api:v1 .'
                    // Run container and expose port 3001 (for Newman)
                    bat 'docker run -d -p 3001:3001 --name airline-helper helper-api:v1'
                }
                // CRITICAL: Health Check Loop for Node.js readiness
                echo 'Waiting for Node.js Helper API Health Check (http://localhost:3001/query)...'
                bat 'powershell -Command "for ($i=1; $i -le 12; $i++) { try { Invoke-WebRequest -Uri http://localhost:3001/query -TimeoutSec 5 } catch { Start-Sleep -Seconds 5 } }"'
            }
        }

        stage('4. Run API Tests (Newman)') {
            steps {
                echo 'Executing Postman Collection with Newman...'
                dir('postman') {
                    // This test hits the Java service (on 8080, if running) AND the Node.js helper API (on 3001)
                    bat 'newman run "Airline Reservation API.json" -e "Airline Local DB.json" -r cli,htmlextra'
                }

                // Clean up the Node.js container
                echo 'Stopping Node.js Helper API container...'
                bat 'docker stop airline-helper'
                bat 'docker rm airline-helper'
            }
            post {
                always {
                    echo 'Archiving Newman Test Reports...'
                    archiveArtifacts artifacts: 'postman/newman-html-report/*.html', allowEmptyArchive: true
                }
            }
        }

        stage('5. Package Java Artifact') {
            steps {
                echo 'Packaging the final executable JAR...'
                // Re-running install (or package) ensures the final JAR is built and available for archiving
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
        // --- FINAL ARCHIVE AND CLEANUP ---
        success {
            echo 'Pipeline Succeeded! Archiving final build artifact (JAR file)...'
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
        }
        always {
            // Ensure Helper API is cleaned up (redundant but safe)
            bat 'docker rm -f airline-helper'
        }
    }
}