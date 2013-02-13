# !/bin/bash

file=$1

if [ "" = "$file" ]; then
 echo "Usage: ./git-history.sh <file>"
 exit 1
fi

git log -p $file
