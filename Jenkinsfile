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
  - name: gradle
    image: gradle:5.4-alpine
    command:
    - cat
    tty: true
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
        buildDiscarder(logRotator(daysToKeepStr: '30'))
        checkoutToSubdirectory('backend')
        timestamps()
    }
    triggers {
        cron('H H(20-23) * * 1-5')
    }
    stages {
        stage('Configuration documentation') {
            when {
                allOf {
                    // Can be replaced with "triggeredBy('TimerTrigger')" once Pipeline: Declarative 1.3.4 is installed
                    expression { com.openexchange.jenkins.Trigger.isStartedByTrigger(currentBuild.buildCauses, com.openexchange.jenkins.Trigger.Triggers.TIMER) }
                    expression { null != version4ConfigDocProcessor(env.BRANCH_NAME) }
                }
            }
            steps {
                script {
                    def targetVersion = version4ConfigDocProcessor(env.BRANCH_NAME)
                    def targetDirectory
                    dir('config-doc-processor') {
                        // Need to do some file operation in directory otherwise it is not created.
                        writeFile file: 'properties.json', text: ''
                        targetDirectory = pwd()
                    }
                    container('gradle') {
                        dir('backend/documentation-generic/config') {
                            sh "gradle runConfigDocuProcessor -PtargetDirectory=${targetDirectory} -PtargetVersion=${targetVersion}"
                        }
                    }
                    dir('config-doc-processor') {
                        sshPublisher(publishers: [sshPublisherDesc(configName: 'documentation.open-xchange.com/var/www/documentation', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: "components/middleware/config/${targetVersion}", remoteDirectorySDF: false, removePrefix: '', sourceFiles: 'properties.json')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false)])
                    }
                }
            }
            post {
                success {
                    archiveArtifacts 'config-doc-processor/properties.json'
                }
            }
        }
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
            post {
                always {
                    archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityIntermediateDir}/build-log.txt")
                    archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityBuildDir}/**")
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
            post {
                always {
                    archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityIntermediateDir}/output/analysis-log.txt")
                    archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityIntermediateDir}/output/distributor.log")
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
        failure {
            emailext attachLog: true,
                body: "${env.BUILD_URL} failed.\\n\\nFull log at: ${env.BUILD_URL}console",
                subject: "${env.JOB_NAME} (#${env.BUILD_NUMBER}) - ${currentBuild.result}",
                to: 'backend@open-xchange.com'
        }
    }
}

String version4ConfigDocProcessor(String branchName) {
    if ('develop' == branchName)
        return branchName
    if (branchName.startsWith('master-'))
        return branchName.substring(7)
    if (branchName.startsWith('release-'))
        return branchName.substring(8)
    if ('master' == branchName)
        return '7.10.1'
    error "Processing configuration documentation is not intended for branch ${branchName}"
}
