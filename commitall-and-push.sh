# !/bin/bash

alias1="Performed minor changes"
alias2="Typo or enhanced JavaDoc/comment"
alias3="Resolved warnings annotated by IDE or cody analysis tool (e.g. FindBugs, lint4j, PMD, etc.)"

echo "Aliases:"
echo "1: $alias1"
echo "2: $alias2"
echo "3: $alias3"

comment=$1

if [ "" = "$comment" ]; then
 echo -n "Empty comment. Please enter comment (default is \"$alias1\"): "
 read comment
fi

if [ "" = "$comment" ]; then
 comment="$alias1"
elif [ "1" = "$comment" ]; then
 comment="$alias1"
elif [ "2" = "$comment" ]; then
 comment="$alias2"
elif [ "3" = "$comment" ]; then
 comment="$alias3"
fi

git add --all && git commit -m "$comment" && git pull && git push origin master
