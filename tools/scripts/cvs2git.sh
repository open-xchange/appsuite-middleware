#!/bin/sh

GITDIR="$1" # $HOME/git/backend
shift
SUBREPO="$1" # com.openexchange.admin
shift
# git cvsimport -o master -d :pserver:marcus@cvs.netline.de:/var/lib/cvs -C $CVSEXPORT -k -A /home/marcus/git/authors.txt open-xchange-admin-plugin-hosting
CVSEXPORT="$1" # $HOME/open-xchange-admin which is created by "git cvsimport"
shift
TEMPDIR="$1" # Some temp directory where this script is able to create a directory named $SUBREPO
shift
BRANCHES="$@" # Space separated list of branches to migrate "bf_6_2 bf_6_4 bf_6_6 ... master"

for BRANCH in $BRANCHES
do
    echo $BRANCH
    # Switch to plain branch
    cd $GITDIR
    git checkout -f master
    git branch -D temp
    git checkout -b temp
    git branch -D $BRANCH
    git fetch origin ${BRANCH}:${BRANCH}
    git checkout -f ${BRANCH}
    git branch -D temp
    // Prepare branch import
    cd $TEMPDIR
    rm -rf $SUBREPO
    cp -rv $CVSEXPORT $SUBREPO
    cd $SUBREPO
    git checkout $BRANCH
    git filter-branch --index-filter "git ls-files -s | sed \"s-\t-&${SUBREPO}/-\" | GIT_INDEX_FILE=\$GIT_INDEX_FILE.new git update-index --index-info && mv \$GIT_INDEX_FILE.new \$GIT_INDEX_FILE" HEAD
    cd $GITDIR
    git pull $TEMPDIR/$SUBREPO
    rm -rf $TEMPDIR/$SUBREPO
done

# git push # Commented because that is a dangerous big operation.

