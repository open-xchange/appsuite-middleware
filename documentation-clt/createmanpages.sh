#!/bin/bash
echo "Make output directory..."
mkdir -p ${PWD}/output/manpages
echo "Creating manpages with pandoc..."
SAVEIFS=$IFS
IFS=$(echo -en "\n\b")
for DIR in $(ls); do
    if [ -d $DIR ]; then
        for SUBDIR in $(ls $DIR); do
            if [ -d $DIR/$SUBDIR ]; then
                for FILE in $(ls $DIR/$SUBDIR | egrep -i "\.md$"); do
                    FILE=$(echo "${PWD}/${DIR}/${SUBDIR}/${FILE}" | sed 's/ /\\ /g')
                    echo "${FILE}"
                    pandoc ${FILE} -s -t man -o ${FILE%.*}.manp
                    gzip -k ${FILE%.*}.manp
                    mv ${FILE%.*}.manp.gz ${PWD}/output/manpages/$(basename ${FILE%.*}).1.gz
                    rm ${FILE%.*}.manp
                done
            fi
        done
    fi
done
IFS=$SAVEIFS
