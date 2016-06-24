#!groovy

node {
    stage 'Checkout'
    dir('backend') {
        checkout scm
        sh 'git submodule update --init'
    }

    def ant = tool 'ant'

    stage 'Local build'
    def workspace = pwd()
    withEnv(['PATH+ANT=${ant}/bin']) {
        dir('backend') {
            dir('build') {
                sh 'ant -f buildAll.xml -DprojectSets=backend-packages -DdestDir=' + workspace + '/tmp/build -Dprefix=/opt/open-xchange buildLocally'
            }
        }
    }
}

