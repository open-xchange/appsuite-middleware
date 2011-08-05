#!/bin/bash

ORIG_COMMITER="$1"
shift
FULLNAME="$1"
shift
EMAIL="$1"

git filter-branch --commit-filter '
    if [ "$GIT_COMMITTER_NAME" = "$ORIG_COMMITER" ];
    then
        GIT_COMMITTER_NAME="$FULLNAME";
        GIT_AUTHOR_NAME="$FULLNAME";
        GIT_COMMITTER_EMAIL="$EMAIL";
        GIT_AUTHOR_EMAIL="$EMAIL";
        git commit-tree "$@";
    else
        git commit-tree "$@";
    fi' HEAD
