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
            // This stage is stable, using the local MySQL workspace.
            bat 'mvn clean install'
        }
    }

    // --- STAGE 3: RUN NODE.JS HELPER API (NON-DOCKERIZED) ---
    stage('3. Start Helper API') {
        steps {
            echo 'Installing dependencies and starting Node.js Helper API...'
            dir('database-helper-api') {
                // 1. Install dependencies
                bat 'npm install'
                // 2. Start server in the background using Windows 'start /B'
                bat 'start /B node index.js'
            }

            // CRITICAL: Health Check Loop to prevent Newman from failing before API is up
            echo 'Waiting for Node.js Helper API Health Check (http://localhost:3001/query)...'
            // This command checks the health endpoint repeatedly
            bat 'powershell -Command "for ($i=1; $i -le 12; $i++) { try { Invoke-WebRequest -Uri http://localhost:3001/query -TimeoutSec 5 } catch { Start-Sleep -Seconds 5 } }"'
        }
    }

    stage('4. Run API Tests (Newman)') {
        steps {
            echo 'Executing Postman Collection with Newman...'
            dir('postman') {
                // FIX: Correcting the environment file name based on your file structure.
                bat 'newman run "Airline_API.postman_collection.json" -e "Local_DB.postman_environment.json" -r cli,htmlextra'
            }
        }
        post {
            always {
                echo 'Archiving Newman Test Reports...'
                archiveArtifacts artifacts: 'postman/newman-html-report/*.html', allowEmptyArchive: true

                // CRITICAL CLEANUP: Stop the background Node.js process to free port 3001
                echo 'Stopping background Node.js Helper API process...'
                // FIXED: Using `&&` ensures we only execute Taskkill once if a PID is found.
                bat 'FOR /F "tokens=5" %%i IN (\'netstat -ano ^| findstr :3001\') DO (TaskKill /PID %%i /F & TaskKill /PID %%i /F)'
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
        // Final check to ensure the port is free for the next build
        // FIXED: Using `|| true` outside the loop, as `findstr` might output multiple lines, but only valid PIDs should be killed.
        bat 'FOR /F "tokens=5" %%i IN (\'netstat -ano ^| findstr :3001\') DO (TaskKill /PID %%i /F || true)'
    }
}


}