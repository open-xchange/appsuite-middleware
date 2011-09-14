# !/bin/bash

ext=$1

if [ "" == "$ext" ]; then
 echo "Missing file extension; e.g \"txt\". Get rid off annoying Mac file: ./dropall.sh DS_Store"
 exit 1;
fi

check=`find . -type f -name \*.$ext -print`
if [ "" == "$check" ]; then
 echo "No matching file(s) found."
 exit 1;
fi

rm `find . -type f -name \*.$ext -print`
