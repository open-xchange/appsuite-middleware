@Library('pipeline-library') _

String workspace // pwd()
String coverityIntermediateDir = 'analyze-idir'
String coverityBuildDir = 'build'

pipeline {
    agent {
        kubernetes {
            label "middleware-core-${UUID.randomUUID().toString()}"
            defaultContainer 'jnlp'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: coverity
    image: gitlab.open-xchange.com:4567/jenkins/slave-coverity:latest
    command:
    - cat
    tty: true
  imagePullSecrets:
  - name: gitlab
"""
        }
    }
    options {
        buildDiscarder(logRotator(daysToKeepStr: '10', numToKeepStr: '30'))
        checkoutToSubdirectory('backend')
        timestamps()
    }
    triggers {
        cron('H H(20-23) * * 1-5')
    }
    stages {
        stage('Coverity Build') {
            when {
                allOf {
                    branch 'develop'
                    // Can be replaced with "triggeredBy('TimerTrigger')" once Pipeline: Declarative 1.3.4 is installed
                    expression { com.openexchange.jenkins.Trigger.isStartedByTrigger(currentBuild.buildCauses, com.openexchange.jenkins.Trigger.Triggers.TIMER) }
                }
            }
            tools {
                ant 'ant'
            }
            steps {
                script {
                    workspace = pwd()
                    container('coverity') {
                        dir('backend/build') {
                            withEnv(['ANT_OPTS="-XX:MaxPermSize=256M"']) {
                                sh "cov-build --dir ${workspace}/${coverityIntermediateDir} ant -f buildAll.xml -DprojectSets=backend-packages -DdestDir=${workspace}/${coverityBuildDir} buildLocally"
                            }
                        }
                        dir('backend') {
                            // sh "cov-build --dir ${workspace}/${coverityIntermediateDir} -no-command --fs-capture-search ."
                        }
                    }
                }
            }
        }
        stage('Coverity Analyze') {
            when {
                allOf {
                    branch 'develop'
                    // Can be replaced with "triggeredBy('TimerTrigger')" once Pipeline: Declarative 1.3.4 is installed
                    expression { com.openexchange.jenkins.Trigger.isStartedByTrigger(currentBuild.buildCauses, com.openexchange.jenkins.Trigger.Triggers.TIMER) }
                }
            }
            steps {
                container('coverity') {
                    sh "cov-analyze --dir ${workspace}/${coverityIntermediateDir} --strip-path ${workspace}/backend/ --webapp-security --webapp-security-preview --all"
                }
            }
        }
        stage('Coverity Commit') {
            when {
                allOf {
                    branch 'develop'
                    // Can be replaced with "triggeredBy('TimerTrigger')" once Pipeline: Declarative 1.3.4 is installed
                    expression { com.openexchange.jenkins.Trigger.isStartedByTrigger(currentBuild.buildCauses, com.openexchange.jenkins.Trigger.Triggers.TIMER) }
                }
            }
            steps {
                container('coverity') {
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '3b5aa8e5-031b-4363-99b6-c8465a430d7a', passwordVariable: 'COVERITY_PASSWORD', usernameVariable: 'COVERITY_LOGIN']]) {
                        sh "cov-commit-defects --dir ${workspace}/${coverityIntermediateDir} --host coverity.open-xchange.com --https-port 8443 --user $COVERITY_LOGIN --password $COVERITY_PASSWORD --certs /opt/coverity.pem --stream OX-Middleware-develop"
                    }
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityIntermediateDir}/build-log.txt")
            archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityIntermediateDir}/output/analysis-log.txt")
            archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityIntermediateDir}/output/distributor.log")
            archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityBuildDir}/**")
        }
        failure {
            emailext attachLog: true,
                body: "${env.BUILD_URL} failed.\\n\\nFull log at: ${env.BUILD_URL}console",
                subject: "${env.JOB_NAME} (#${env.BUILD_NUMBER}) - ${currentBuild.result}",
                to: 'backend@open-xchange.com'
        }
    }
}
