#!bats/bin/bats

# Tests to prepare for support of old an new java versioning scheme
# JEP 223: New Version-String Scheme 
# http://openjdk.java.net/jeps/223

source ../lib/oxfunctions.sh
# oxfunctions sets +e see bug #40756
set -e

function OPENJDK_170 () {
  echo 'java version "1.7.0_111"'
}

function OPENJDK_180 () {
  echo 'openjdk version "1.8.0_91"'
}

function OPENJDK_9_EA () {
  echo 'java version "9-ea"'
}

function OPENJDK_9_MAJOR () {
  echo 'java version "9"'
}

function OPENJDK_9_SECURITY () {
  echo 'java version "9.0.1"'
}

function OPENJDK_9_MINOR () {
  echo 'java version "9.1.2"'
}


@test "detect openjdk 1.7.0" {
  JAVA_BIN=OPENJDK_170
  minor=$(detect_minor_java_version)
   [ $minor -eq 7 ]
}

@test "detect openjdk 1.8.0" {
  JAVA_BIN=OPENJDK_180
  minor=$(detect_minor_java_version)
   [ $minor -eq 8 ]
}

@test "detect openjdk 9 ea" {
  JAVA_BIN=OPENJDK_9_EA
  minor=$(detect_minor_java_version)
   [ $minor -eq 9 ]
}

@test "detect openjdk 9 major" {
  JAVA_BIN=OPENJDK_9_MAJOR
  minor=$(detect_minor_java_version)
   [ $minor -eq 9 ]
}

@test "detect openjdk 9 security" {
  JAVA_BIN=OPENJDK_9_SECURITY
  minor=$(detect_minor_java_version)
   [ $minor -eq 9 ]
}

@test "detect openjdk 9 minor" {
  JAVA_BIN=OPENJDK_9_MINOR
  minor=$(detect_minor_java_version)
   [ $minor -eq 9 ]
}

