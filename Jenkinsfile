pipeline {
    agent any

    // Tools are defined here for the agent
    tools {
        jdk 'jdk11'
        maven 'maven3'
    }

    stages {
        // --- STAGE 1: CHECKOUT (Implicitly handled by Jenkins on SCM trigger) ---

        stage('2. Build Java Service') {
            steps {
                echo 'Building Java/Spring Boot Service with Maven...'
                // Command: mvn clean install (Builds JAR, runs Unit Tests)
                // Use 'bat' for Windows/Cygwin shell compatibility
                bat 'mvn clean install'
            }
        }

        stage('3. Build & Start Helper API') {
            steps {
                echo 'Building and starting Node.js Helper API...'
                // Change to the Node.js directory to run commands
                dir('database-helper-api') {
                    // Command: npm install
                    bat 'npm install'
                    // Command: node index.js (Starts the server in the background)
                    // Note: 'start /B' is used for a non-blocking background process on Windows.
                    bat 'start /B node index.js'
                }

                // CRITICAL: Health Check Loop (as discussed in the report and interview prep)
                // Pings the Helper API health endpoint until it is ready (or fails after 60s)
                echo 'Waiting for Node.js Helper API Health Check (http://localhost:3001/query) to be OK...'
                bat 'powershell -Command "for ($i=1; $i -le 12; $i++) { try { Invoke-WebRequest -Uri http://localhost:3001/query -TimeoutSec 5 } catch { Start-Sleep -Seconds 5 } }"'
            }
        }

        stage('4. Run API Tests (Newman)') {
            steps {
                echo 'Executing Postman Collection with Newman...'
                dir('postman') {
                    // Command: newman run ...
                    // Assumes Newman is globally available on the Jenkins agent.
                    bat 'newman run "Airline Reservation API.json" -e "Airline Local DB.json" -r cli,htmlextra'
                }

                // Stop the Node.js server started in Stage 3
                echo 'Stopping Node.js Helper API process...'
                // Find the process running on port 3001 and kill it (Advanced Cleanup)
                bat 'FOR /F "tokens=5" %%i IN (\'netstat -ano ^| findstr :3001\') DO (TaskKill /PID %%i /F)'
            }
            post {
                // Archive Newman report even if API tests fail (Failure box in workflow)
                always {
                    echo 'Archiving Newman Test Reports...'
                    archiveArtifacts artifacts: 'postman/newman-html-report/*.html', allowEmptyArchive: true
                }
            }
        }

        stage('5. Run Integration Tests (TestNG)') {
            steps {
                echo 'Executing deep Integration Tests with TestNG...'
                // Command: mvn test (Runs the testng.xml suite)
                bat 'mvn test'
            }
            post {
                // Archive Extent Report even if Integration tests fail
                always {
                    echo 'Archiving Extent Report...'
                    // Assumes Extent Report is configured to save here
                    archiveArtifacts artifacts: 'target/extent-report.html', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        // --- STAGE 6: ARCHIVE ARTIFACTS ---
        success {
            echo 'Pipeline Succeeded! Archiving final build artifact (JAR file)...'
            // Archive the final executable JAR file
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
        }
        failure {
            echo 'Pipeline failed. Check build logs and test reports.'
        }
    }
}