@Library('pipeline-library') _

import com.openexchange.jenkins.Trigger

String workspace // pwd()

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
            when {
                // Can be replaced with "triggeredBy('TimerTrigger')" once Pipeline: Declarative 1.3.4 is installed
                expression { Trigger.isStartedByTrigger(currentBuild.buildCauses, Trigger.Triggers.BRANCH_INDEXING) }
            }
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
                    expression { null != version4Documentation(env.BRANCH_NAME) }
                }
            }
            steps {
                script {
                    def targetVersion = version4Documentation(env.BRANCH_NAME)
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
                    build job: 'middleware/propertyDocumentationUI/master', parameters: [string(name: 'targetVersion', value: targetVersion), string(name: 'targetDirectory', value: 'middleware/config')]
                }
            }
            post {
                success {
                    archiveArtifacts 'config-doc-processor/properties.json'
                }
            }
        }
        stage('Coverity') {
            when {
                allOf {
                    branch 'develop'
                    triggeredBy('Timertrigger')
                }
            }
            environment {
                coverityIntermediateDir = 'analyze-idir'
                coverityBuildDir = 'build'
            }
            tools {
                ant 'ant'
            }
            steps {
                container('coverity') {
                    dir('backend/build') {
                        withEnv(['ANT_OPTS="-XX:MaxPermSize=256M"']) {
                            scanCoverity('backend', 'develop', "ant -f buildAll.xml -DprojectSets=backend-packages -DdestDir=${env.WORKSPACE}/${coverityBuildDir} buildLocally")
                        }
                    }
                }
            }
            post {
                always {
                    archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityIntermediateDir}/build-log.txt")
                    archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityBuildDir}/**")
                    archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityIntermediateDir}/output/analysis-log.txt")
                    archiveArtifacts(allowEmptyArchive: true, artifacts: "${coverityIntermediateDir}/output/distributor.log")
                }
            }
        }
        stage('Build Test Clients') {
            environment {
                artifactory = credentials('044a36e5-ec65-4319-8238-6cdf8e2db7f5')
            }
            steps {
                dir('backend/http-api') {
                    writeFile file: 'gradle.properties', text: """org.gradle.warning.mode=all
                        artifactory_user=${artifactory_USR}
                        artifactory_password=${artifactory_PSW}""".stripIndent()
                    container('gradle') {
                        sh 'gradle clean resolve validate http_api_client:build rest_api_client:build http_api_client:publish rest_api_client:publish'
                    }
                }
            }
            post {
                always {
                    sh 'rm -rf gradle.properties'
                }
            }
        }
        stage('HTTP API documentation') {
            when {
                allOf {
                    expression { null != version4Documentation(env.BRANCH_NAME) }
                    anyOf {
                        triggeredBy 'TimerTrigger'
                        triggeredBy cause: 'UserIdCause'
                    }
                }
            }
            steps {
                script {
                    def targetVersion = version4Documentation(env.BRANCH_NAME)
                    container('gradle') {
                        dir('backend/http-api') {
                            sh 'gradle resolve insertMarkdown'
                        }
                    }
                    dir('backend/documentation-generic/') {
                        sshPublisher(publishers: [sshPublisherDesc(configName: 'documentation.open-xchange.com/var/www/documentation',
                                transfers: [
                                        sshTransfer(cleanRemote: true, excludes: '', execCommand: '', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: "components/middleware/http/${targetVersion}", remoteDirectorySDF: false, removePrefix: 'http_api/documents/html', sourceFiles: 'http_api/documents/html/**'),
                                        sshTransfer(cleanRemote: true, excludes: '', execCommand: '', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: "components/middleware/drive/${targetVersion}", remoteDirectorySDF: false, removePrefix: 'drive_api/documents/html', sourceFiles: 'drive_api/documents/html/**'),
                                        sshTransfer(cleanRemote: true, excludes: '', execCommand: '', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: "components/middleware/rest/${targetVersion}", remoteDirectorySDF: false, removePrefix: 'rest_api/documents/html', sourceFiles: 'rest_api/documents/html/**')],
                                usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)
                        ])
                    }
                }
            }
        }
    }
    post {
        failure {
            emailext attachLog: true,
                body: "${env.BUILD_URL} failed.\n\nFull log at: ${env.BUILD_URL}console\n\n",
                subject: "${env.JOB_NAME} (#${env.BUILD_NUMBER}) - ${currentBuild.result}",
                to: 'backend@open-xchange.com'
        }
    }
}

String version4Documentation(String branchName) {
    if ('develop' == branchName)
        return branchName
    if ('master' == branchName)
        return "7.10.3"
    if (branchName.startsWith('master-'))
        return branchName.substring(7)
    if (branchName.startsWith('release-'))
        return branchName.substring(8)
    return null
}

