#!/usr/bin/env groovy
def dockerCredentials = "docker-hub-credentials"
def dockerImage = "dniel/forwardauth"
def appVersion

pipeline {
    agent any

    stages {
        stage('Prepare') {
            steps {
                slackSend color: 'GREEN',
                        message: "Started ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                checkout scm

                script {
                    def gitShortCommit = sh([
                            returnStdout: true,
                            script      : 'git rev-parse --short HEAD'
                    ]).trim()

                    def gitCommitTime = sh([
                            returnStdout: true,
                            script      : 'git log -1 --pretty=format:%ct|date +"%m%d%Y-%H%M"'
                    ]).trim()

                    appVersion = "${gitCommitTime}-${gitShortCommit}"
                }
            }
        }

        stage('Build') {
            steps {
                container('maven') {
                    withCredentials([string(credentialsId: 'SNYK_API_TOKEN', variable: 'SNYK_API_TOKEN')]) {
                        sh "mvn org.jacoco:jacoco-maven-plugin:prepare-agent clean deploy -Dmaven.test.failure.ignore=false -Dsha1=${appVersion} -Dchangelist=${env.BRANCH_NAME}"
                    }
                }
            }
        }

        stage('Scan') {
            steps {
                container('sonar-scanner') {
                    withSonarQubeEnv('SonarCloud') {
                        sh "sonar-scanner -X -Dsonar.jacoco.reportPath=target/jacoco.exec -Dsonar.java.coveragePlugin=jacoco -Dsonar.projectKey=dniel_traefik-forward-auth0 -Dsonar.organization=dniel-github -Dsonar.projectVersion=${appVersion}"
                    }
                }
            }
        }

        stage('Docker') {
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId: dockerCredentials, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh "docker login -u ${USERNAME} -p ${PASSWORD}"
                        sh "docker build -t ${dockerImage} ."
                        sh "docker tag ${dockerImage} ${dockerImage}:${appVersion}"
                        sh "docker tag ${dockerImage} ${dockerImage}:${env.BRANCH_NAME}"

                        sh "docker push ${dockerImage}:${appVersion}"
                        sh "docker push ${dockerImage}:${env.BRANCH_NAME}"

                        script {
                            if (env.BRANCH_NAME == "master") {
                                sh "docker push ${dockerImage}:latest"
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        success {
            slackSend(color: 'GREEN',
                    message: "${env.JOB_NAME} ${env.BUILD_NUMBER} completed successfully :) (<${env.BUILD_URL}|Open>)")
        }
        failure {
            slackSend(color: 'RED',
                    message: "${env.JOB_NAME} ${env.BUILD_NUMBER} completed failed :( (<${env.BUILD_URL}|Open>)")
        }
    }
}
