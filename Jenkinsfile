#!/usr/bin/env groovy
def dockerCredentials = "docker-hub-credentials"
def dockerImage = "dniel/forwardauth"
def appVersion

pipeline {
    agent any

    stages {
        stage('Prepare') {
            steps {
                slackSend "Started ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"

                deleteDir()
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
                    sh "mvn clean deploy -Dsha1=${appVersion} -Dchangelist=${env.BRANCH_NAME}"
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
                        sh "docker push ${dockerImage}"
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
