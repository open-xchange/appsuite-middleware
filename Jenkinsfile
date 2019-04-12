def intermediateDir = 'analyze-idir'
def name = 'backend'

node('git&&coverity') {
    try {
        def workspace = pwd()
        stage('Checkout') {
            dir(name) {
                checkout([$class: 'GitSCM', branches: [[name: '*/develop']], browser: [$class: 'GitWeb', repoUrl: 'https://gitweb.open-xchange.com/?p=wd/backend;'], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch'], [$class: 'CloneOption', honorRefspec: true, noTags: true, reference: '', shallow: true], [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: true, recursiveSubmodules: true, reference: '', trackingSubmodules: false], [$class: 'CleanCheckout']], gitTool: 'Linux', submoduleCfg: [], userRemoteConfigs: [[credentialsId: '2cb98438-7e92-4038-af72-dad91b4ff6be', url: 'https://code.open-xchange.com/git/wd/backend']]])
            }
        }
        dir(intermediateDir) { deleteDir() }
        try {
            stage('Build') {
                def ant = tool 'ant'
                dir(name + '/build') {
                    withEnv(['PATH+ANT=' + ant + '/bin', 'ANT_OPTS="-XX:MaxPermSize=256M"']) {
                        sh 'cov-build --dir ' + workspace + '/' + intermediateDir + ' ant -f buildAll.xml -DprojectSets=backend-packages -DdestDir=' + workspace + '/build buildLocally'
                        // sh 'cov-build --dir ' + workspace + '/' + intermediateDir + ' -no-command --fs-capture-search .'
                    }
                }
            }
            stage('Analyze') {
                sh 'cov-analyze --dir ' + workspace + '/' + intermediateDir + ' --strip-path ' + workspace + '/' + name + '/ --webapp-security --webapp-security-preview --all'
            }
            stage('Commit') {
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '3b5aa8e5-031b-4363-99b6-c8465a430d7a', passwordVariable: 'COVERITY_PASSWORD', usernameVariable: 'COVERITY_LOGIN']]) {
                    sh 'cov-commit-defects --dir ' + workspace + '/' + intermediateDir + ' --host coverity.open-xchange.com --https-port 8443 --user $COVERITY_LOGIN --password $COVERITY_PASSWORD --certs /home/jenkins/coverity.pem --stream OX-Middleware-develop'
                }
            }
        } finally {
            stage('Archive') {
                dir(intermediateDir) {
                    archiveArtifacts 'build-log.txt'
                    archiveArtifacts 'output/analysis-log.txt'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'output/distributor.log'
                }
                archiveArtifacts 'build/**'
            }
            deleteDir()
        }
    } catch (err) {
        emailext attachLog: true,
            body: "${env.BUILD_URL} failed.\\n\\nFull log at: ${env.BUILD_URL}console",
            subject: "${env.JOB_NAME} (#${env.BUILD_NUMBER}) - ${currentBuild.result}",
            to: 'backend@open-xchange.com'
        manager.buildFailure()
        throw err
    }
}
