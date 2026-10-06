pipeline {
    agent any

    environment {
        APP_URL = 'http://localhost:8085'
        SONARQUBE_SERVER = 'SonarQube'
        SLACK_CHANNEL = '#calidad'
        JAVA_HOME = 'C:\\Program Files\\Java\\jdk-21.0.11'
        MAVEN_HOME = 'C:\\Program Files\\Java\\Apache\\apache-maven-3.9.16'
        PATH = "${MAVEN_HOME}\\bin;${JAVA_HOME}\\bin;${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build y pruebas unitarias') {
            steps {
                bat 'mvn clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: false, testResults: 'target/surefire-reports/*.xml'
                    publishHTML(target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'Cobertura JaCoCo'
                    ])
                }
            }
        }

        stage('Análisis SonarQube') {
            steps {
                withSonarQubeEnv("${SONARQUBE_SERVER}") {
                    bat 'mvn sonar:sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Pruebas de carga JMeter') {
            steps {
                bat 'jmeter -n -t jmeter/api-load-test.jmx -l target/jmeter-results.jtl -e -o target/jmeter-report -Jhost=localhost -Jport=8085'
            }
            post {
                always {
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/jmeter-report',
                        reportFiles: 'index.html',
                        reportName: 'Reporte JMeter'
                    ])
                }
            }
        }
    }

    post {
        success {
            slackSend(channel: "${SLACK_CHANNEL}", color: 'good', message: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Pipeline de calidad completado. ${env.BUILD_URL}")
        }
        failure {
            slackSend(channel: "${SLACK_CHANNEL}", color: 'danger', message: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Revisar la ejecución. ${env.BUILD_URL}")
        }
        always {
            archiveArtifacts artifacts: 'target/jmeter-results.jtl,target/site/jacoco/**', allowEmptyArchive: true
        }
    }
}
