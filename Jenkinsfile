pipeline {
    agent any

    // Tools are defined here for the agent
    tools {
        jdk 'jdk11'
        maven 'maven3'
    }

    stages {
        // --- NEW STAGE 1: START MYSQL CONTAINER (CRITICAL FIX) ---
        stage('1. Start MySQL Container') {
            steps {
                echo 'Starting the MySQL database container via Docker Compose on port 3308...'
                // You must have a docker-compose.yml file in the project root for this to work.
                // The service is assumed to be named 'mysql_db' (as per standard practice).
                bat 'docker-compose up -d mysql_db'

                echo 'Waiting 20 seconds for MySQL initialization (Critical for test stability)...'
                // Pause to give the database time to fully initialize and accept connections
                bat 'powershell -Command "Start-Sleep -Seconds 20"'
            }
        }

        // --- STAGE 2: BUILD JAVA SERVICE (Now runs after DB is started) ---
        stage('2. Build Java Service') {
            steps {
                echo 'Building Java/Spring Boot Service with Maven...'
                // This step now runs the tests successfully because the DB is up and waiting.
                bat 'mvn clean install'
            }
        }

        // --- STAGE 3: BUILD & START HELPER API (Dockerized) ---
        stage('3. Build & Start Helper API') {
            steps {
                echo 'Building and starting Node.js Helper API...'
                dir('database-helper-api') {
                    // Docker commands for the polyglot service
                    bat 'docker build -t helper-api:v1 .'
                    bat 'docker run -d -p 3001:3001 --name airline-helper helper-api:v1'
                }

                // Health Check Loop (Ensures Node.js is ready for Newman)
                echo 'Waiting for Node.js Helper API Health Check (http://localhost:3001/query) to be OK...'
                bat 'powershell -Command "for ($i=1; $i -le 12; $i++) { try { Invoke-WebRequest -Uri http://localhost:3001/query -TimeoutSec 5 } catch { Start-Sleep -Seconds 5 } }"'
            }
        }

        // --- STAGE 4: RUN API TESTS (Newman) ---
        stage('4. Run API Tests (Newman)') {
            steps {
                echo 'Executing Postman Collection with Newman...'
                dir('postman') {
                    bat 'newman run "Airline Reservation API.json" -e "Airline Local DB.json" -r cli,htmlextra'
                }

                // Cleanup Node.js container after tests
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

        // --- STAGE 5: RUN INTEGRATION TESTS (TestNG) ---
        stage('5. Run Integration Tests (TestNG)') {
            steps {
                echo 'Executing deep Integration Tests with TestNG...'
                // This runs the testng.xml suite against the live Java service
                bat 'mvn test'
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
        // --- ARCHIVE ARTIFACTS and FINAL CLEANUP ---
        success {
            echo 'Pipeline Succeeded! Archiving final build artifact (JAR file)...'
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
        }
        always {
            // Clean up MySQL container
            echo 'Cleaning up MySQL database container...'
            bat 'docker-compose down' // Stops and removes the DB container

            // Re-archive reports for clean visibility
            echo 'Archiving final test results...'
            archiveArtifacts artifacts: 'target/extent-report.html', allowEmptyArchive: true

            // Ensure Helper API is removed (redundant but safe)
            bat 'docker rm -f airline-helper'
        }
    }
}