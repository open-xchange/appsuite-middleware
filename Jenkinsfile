@Library('pipeline-library') _

import com.openexchange.jenkins.Trigger

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
    image: gradle:5.5-jdk8
    command:
    - cat
    tty: true
  - name: coverity
    image: gitlab.open-xchange.com:4567/jenkins/slave-coverity:latest
    command:
    - cat
    tty: true
  - name: java-gettext
    image: gitlab.open-xchange.com:4567/jenkins/slave-gettext:latest
    command:
    - cat
    tty: true
    securityContext:
      runAsUser: 1000
      allowPrivilegeEscalation: false
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
        cron('develop' == env.BRANCH_NAME ? 'H H(20-23) * * 1-5' : '')
    }
    stages {
        stage('POT') {
            tools {
                ant 'ant'
            }
            steps {
                dir('automation') {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/master']],
                        browser: [$class: 'GitWeb', repoUrl: 'https://gitweb.open-xchange.com/?p=automation'],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [],
                        gitTool: 'Linux',
                        submoduleCfg: [],
                        userRemoteConfigs: [[credentialsId: '2cb98438-7e92-4038-af72-dad91b4ff6be', url: "https://code.open-xchange.com/git/automation"]]
                    ])
                }
                dir('automation/backendI18N') {
                    container('java-gettext') {
                        sh "ant -file backendPot.xml -DcheckoutDir=${env.WORKSPACE}/backend -DproductToGenerate=backend -DpotDir=${env.WORKSPACE}/backend/l10n create-server-pot"
                    }
                }
                dir('backend') {
                    script {
                        def gitStatus = sh script: 'git status --porcelain', returnStdout: true
                        if (gitStatus.contains('l10n/backend.pot')) {
                            sh 'git add l10n/backend.pot'
                            sh 'git commit -m "Automatic POT generation"'
                            sh 'git show HEAD'
                            sshagent(['9a40d6b1-813a-4c46-9b0d-18320a0a4ef4']) {
                                sh "git push origin HEAD:${env.BRANCH_NAME}"
                            }
                        }
                    }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'backend/l10n/backend.pot', onlyIfSuccessful: true
                }
                always {
                    dir('automation') {
                        deleteDir()
                    }
                }
            }
        }
        stage('Configuration documentation') {
            when {
                allOf {
                    // Can be replaced with "triggeredBy('TimerTrigger')" once Pipeline: Declarative 1.3.4 is installed
                    expression { Trigger.isStartedByTrigger(currentBuild.buildCauses, Trigger.Triggers.TIMER, Trigger.Triggers.USER) }
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
                        sshPublisher failOnError: true, publishers: [sshPublisherDesc(configName: 'documentation.open-xchange.com/var/www/documentation', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: "components/middleware/config/${targetVersion}", remoteDirectorySDF: false, removePrefix: '', sourceFiles: 'properties.json')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)]
                    }
                    build job: 'middleware/propertyDocumentationUI/master', parameters: [string(name: 'targetVersion', value: targetVersion)]
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
    return null
}

