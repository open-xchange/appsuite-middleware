#!/bin/bash

#set -x
#set -e

SOURCE=$1
DESTINATION=$(git branch --no-color | egrep '^\*' | cut -d ' ' -f 2)

TEMPFILE=$(mktemp)
echo $TEMPFILE

mergeDebianChangelog() {
# release branch merge:
# <<<<<<< HEAD
# =======
# >>>>>>> release-7.0.0
# hotfix branch merge
# <<<<<<< ????
# =======
# >>>>>>> ????
    FILES=$(git status --porcelain | grep UU | grep debian/changelog | cut -d ' ' -f 2)
    for FILE in $FILES
    do
        echo $FILE
        COUNT=$(grep '<<<<<<< HEAD' $FILE | wc -l)
        if [ 1 -ne $COUNT ]
        then
            echo "Multiple conflicts."
            continue
        fi

        sed '/<<<<<<< HEAD/ d' $FILE >$TEMPFILE

        # For merging the release branch the line must be
        sed -i '/=======/ d' $TEMPFILE
        # For merging hotfix branches the line must be
        # sed -i 's/=======//' $TEMPFILE

        sed -i '/>>>>>>> / d' $TEMPFILE

        diff -y $FILE $TEMPFILE | less
        echo "Merged correctly?"
        select YN in "Yes" "No"
        do
            case $YN in
                Yes ) cp $TEMPFILE $FILE; git add $FILE; break;;
                No ) break;;
            esac
        done
    done
}

mergeRPMSpec() {
    FILES=$(git status --porcelain | grep UU | grep .spec | cut -d ' ' -f 2)
    for FILE in $FILES
    do
        echo $FILE
        COUNT=$(grep '<<<<<<< HEAD' $FILE | wc -l)
        if [ 2 -ne $COUNT -a 1 -ne $COUNT ]
        then
            echo "More or less than 2 conflicts."
            continue
        fi

        # Hotfix branch
        # sed -i '0,/<<<<<<< HEAD/{/<<<<<<< HEAD/ d;}' $FILE
        # sed -i '0,/>>>>>>> /{/=======/,/>>>>>>> / d;}' $FILE

        # Release branch
        # remove %define ox_release from destination (develop) branch
        sed '0,/=======/{/<<<<<<< HEAD/,/=======/ d;}' $FILE >$TEMPFILE
        sed -i '/>>>>>>> release-/ d' $TEMPFILE

        diff -y $FILE $TEMPFILE | less
        echo "Merged correctly?"
        select YN in "Yes" "No"
        do
            case $YN in
                Yes ) cp $TEMPFILE $FILE; git add $FILE; break;;
                No ) break;;
            esac
        done
    done
}

mergePSF() {
    FILES=$(git status --porcelain | grep UU | grep .psf | cut -d ' ' -f 2)
    for FILE in $FILES
    do
        echo $FILE
        git show $SOURCE:$FILE >$TEMPFILE
        sed -i "s/,$SOURCE,/,$DESTINATION,/g" $TEMPFILE
        TEMPFILE2=$(mktemp)
        git show $DESTINATION:$FILE >$TEMPFILE2
        vimdiff -d $TEMPFILE2 $TEMPFILE
        rm $TEMPFILE2
        echo "Merged correctly?"
        select YN in "Yes" "No"
        do
            case $YN in
                Yes ) cp $TEMPFILE $FILE; git add $FILE; break;;
                No ) break;;
            esac
        done
    done
}

mergeDebianChangelog
mergeRPMSpec
mergePSF
rm $TEMPFILE
exit 0
