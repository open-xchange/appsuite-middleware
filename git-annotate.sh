# !/bin/bash

file=$1

if [ "" = "$file" ]; then
 echo "Usage: ./git-annotate.sh <file>"
 exit 1
fi

git annotate $file
