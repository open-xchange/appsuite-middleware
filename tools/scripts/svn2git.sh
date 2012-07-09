#!/bin/sh

set -x
set -e

GITDIR=$1

REPOS=$@
#REPOS="com.openexchange.usm com.openexchange.usm.api com.openexchange.usm.authentication com.openexchange.usm.authentication.impl com.openexchange.usm.cache com.openexchange.usm.cache.impl com.openexchange.usm.cache.ox_adapter com.openexchange.usm.configuration com.openexchange.usm.configuration.impl com.openexchange.usm.configuration.ox com.openexchange.usm.contenttypes.calendar com.openexchange.usm.contenttypes.contacts com.openexchange.usm.contenttypes.folder com.openexchange.usm.contenttypes.groups com.openexchange.usm.contenttypes.mail com.openexchange.usm.contenttypes.manager com.openexchange.usm.contenttypes.resources com.openexchange.usm.contenttypes.tasks com.openexchange.usm.contenttypes.tasks.calendar.commons com.openexchange.usm.contenttypes.util com.openexchange.usm.database com.openexchange.usm.database.hsql com.openexchange.usm.database.mysql com.openexchange.usm.database.ox com.openexchange.usm.journal com.openexchange.usm.journal.impl com.openexchange.usm.json com.openexchange.usm.mapping com.openexchange.usm.ox_event com.openexchange.usm.ox_event.impl com.openexchange.usm.ox_json com.openexchange.usm.ox_json.impl com.openexchange.usm.sample_protocol com.openexchange.usm.session.dataobject com.openexchange.usm.session.impl com.openexchange.usm.syncml com.openexchange.usm.test com.openexchange.usm.util com.openexchange.usm.uuid com.openxchange.usm.ox.html org.apache.commons.codec org.apache.commons.httpclient org.apache.commons.logging org.apache.jcs org.hsqldb org.json USMBuild USMBuildLaunchers USMTest USMTestLibraries USMXXXReferencedOXClasses"

BRANCHES="bf_6_12 bf_6_14 bf_6_16 bf_6_18 bf_6_18_0 bf_6_18_1 bf_6_18_2 bf_6_20 bf_6_20_1 bf_6_20_2 bf_6_20_3 bf_6_20_4 bf_6_20_5 trunk"

TEMPDIR=$(pwd)

mkdir $GITDIR
cd $GITDIR
git init
touch initial
git add initial
git commit -m "Initial creation"
for BRANCH in $BRANCHES
do
    git checkout -b $BRANCH
done
cd $TEMPDIR

for REPO in $REPOS
do
    cd $TEMPDIR
    git svn clone --no-metadata -A svn-authors.txt -t tags -b branches -T trunk https://marcus.klein@svn.open-xchange.com/EAS/${REPO}
    cd $REPO
    git filter-branch --tag-name-filter cat --index-filter "SHA=\$(git write-tree); rm \$GIT_INDEX_FILE && git read-tree --prefix=$REPO \$SHA" -- --all
    REPOBRANCHES=$(git branch -a --no-color | grep remotes | grep -v '@' | cut -d '/' -f 2)
    for BRANCH in $REPOBRANCHES
    do
        git checkout -b $BRANCH $BRANCH
    done
    cd $TEMPDIR/$GITDIR
    for BRANCH in $REPOBRANCHES master
    do
        git checkout $BRANCH
        echo "Pulling branch $BRANCH"
        git pull $TEMPDIR/$REPO $BRANCH
    done
    cd $TEMPDIR
done

git clone --bare $GITDIR
