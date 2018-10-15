#!/bin/bash
echo "Make output directory..."
mkdir -p ${PWD}/output
echo "Create all manpages with pandoc..."
SAVEIFS=$IFS
IFS=$(echo -en "\n\b")
for FILE in $(ls | egrep -i "\.md$"); do  
   FILE=$(echo "${PWD}/${FILE}" | sed 's/ /\\ /g')
   echo "${FILE}" 
   pandoc ${FILE} -s -t man -o ${FILE%.*}.manp
   gzip -k ${FILE%.*}.manp
   mv ${FILE%.*}.manp.gz ${PWD}/output/$(basename ${FILE%.*}).1.gz
   rm ${FILE%.*}.manp

done
IFS=$SAVEIFS