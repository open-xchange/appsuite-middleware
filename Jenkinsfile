#!/usr/bin/env groovy

node {
    stage 'Checkout'
    dir('backend') {
        checkout scm
        sh 'git submodule update --init'
    }

    def ant = tool 'ant'

    stage 'Build'
    def workspace = pwd()
    sh 'rm -rvf ' + workspace + '/deb'
    withEnv(['PATH+ANT=${ant}/bin']) {
        dir('backend/build') {
            sh 'ant -f obs.xml -Dbranch=' + env.BRANCH_NAME + ' -DprojectSets=backend-packages -DfullProductName=backend -DshortProductName=backend determineProject createProject deleteObsoletePackages'
            sh 'ant -f buildAll.xml -Dbranch=' + env.BRANCH_NAME + ' -DprojectSets=backend-packages -DfullProductName=backend -DshortProductName=backend determineProject upload'
            sh 'ant -f obs.xml -Dbranch=' + env.BRANCH_NAME + ' -DprojectSets=backend-packages -DfullProductName=backend -DshortProductName=backend determineProject wait4Project'
            sh 'ant -f buildAll.xml -Dbranch=' + env.BRANCH_NAME + ' -DdebDir=' + workspace + '/deb -DprojectSets=backend-packages -DfullProductName=backend -DshortProductName=backend determineProject fetch'
        }
    }
}

// vim: ft=groovy

