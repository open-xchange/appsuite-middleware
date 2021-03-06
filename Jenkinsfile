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
    image: gitlab.open-xchange.com:4567/jenkins/slave-coverity:2020.06-jdk8
    command:
    - cat
    tty: true
  - name: gettext
    image: gitlab.open-xchange.com:4567/docker/gettext:2.0
    command:
    - /toox/server
    - msgcat
    - xgettext
    env:
    - name: TOOX_TOOLS
      value: ant
    tty: true
    volumeMounts:
    - mountPath: /toox
      name: toox
  - name: ant
    image: frekele/ant:1.9.11-jdk8
    command:
    - /toox/server
    - ant
    env:
    - name: TOOX_TOOLS
      value: msgcat,xgettext
    tty: true
    volumeMounts:
    - mountPath: /toox
      name: toox
  - name: toox-proxy
    image: gitlab.open-xchange.com:4567/engineering/tools/toox:0.15.1
    command:
    - /toox/proxy
    env:
    - name: TOOX_TOOLS
      value: ant,msgcat,xgettext
    tty: true
    volumeMounts:
    - mountPath: /toox
      name: toox
  imagePullSecrets:
  - name: gitlab
  initContainers:
  - name: toox-init
    image: gitlab.open-xchange.com:4567/engineering/tools/toox:0.15.1
    command:
    - /toox/init
    volumeMounts:
    - mountPath: /init
      name: toox
    workingDir: /init
  volumes:
  - name: toox
    emptyDir: {}
"""
        }
    }
    options {
        buildDiscarder(logRotator(daysToKeepStr: '30'))
        checkoutToSubdirectory('backend')
    }
    triggers {
        cron('develop' == env.BRANCH_NAME ? 'H H(20-23) * * 1-5' : '')
    }
    stages {
        stage('POT') {
            when {
                triggeredBy 'BranchIndexingCause'
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
                    container('toox-proxy') {
                        sh "ant -file backendPot.xml -DcheckoutDir=${env.WORKSPACE}/backend -DproductToGenerate=backend -DpotDir=${env.WORKSPACE}/backend/l10n create-server-pot"
                        sh 'rm -rv tmp build'
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
                expression { null != version4Documentation(env.BRANCH_NAME) }
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
                expression { null != version4Documentation(env.BRANCH_NAME) }
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
    if (branchName.startsWith('master-'))
        return branchName.substring(7)
    if ('master' == branchName)
        return sh(script: "awk '\$0 ~ /OXVersion/ {print \$2}' backend/com.openexchange.version/META-INF/MANIFEST.MF", returnStdout: true).trim() as String
    if (branchName.startsWith('release-'))
        return branchName.substring(8)
    return null
}

