#!/bin/bash 

parse_yaml() {
    local s
    local w
    local fs
    s='[[:space:]]*'
    w='[a-zA-Z0-9_]*'
    fs=" "
     sed -ne "/^.*-[[:space:]]data:/d" \
          -e "/array:/d" \
          -e "s|^\($s\)\($w\)$s:$s\"\(.*\)\"$s\$|\1$fs\2$fs\3|" \
          -e "s|^\($s\)\($w\)$s:$s\(.*\)$s\$|\1$fs\2$fs\3|" \
          -e "p" "$1" |
    awk -F "$fs" '{
        if (length($2) > 0 || multiline=="true"){ 
        if($1 == "Default"){
            multiline="";
            printf(" |\n");
        }
        if ($1 == "Key"){
            printf("| Key | ")
        } else {
            if(multiline!="true"){
                stringWithSpaces=$1;
                gsub(/_/," ",stringWithSpaces)
                printf("| __%s__ | ", stringWithSpaces)
            }
        }
        
        if (length($2) > 0 || multiline=="true") 
        {
            if($2 == ">"){
                multiline="true";
            } else {
                if(multiline=="true"){
                    printf("%s<br>", $0);
                } else {
                     if ($1 == "Key"){
                        if ($2 == \`*) {
                          printf("<span style=\"font-weight:normal\">%s</span>", $2);
                        } else {
                          $2 = "`" + $2 + "`"
                          printf("<span style=\"font-weight:normal\">%s</span>", $2);
                        }
                     } else if ($1 == "Related") {
                        if ($2 == \`*) {
                          printf("%s", $2);
                        } else {
                          $2 = "`" + $2 + "`"
                          printf("%s", $2);
                        }
                     } else {
                        for(i=2;i<=NF;i++){
                          printf("%s ", $i);
                        }
                    }
                }
            }
        } 
        if(multiline!="true"){
           printf(" |\n")
           if($1 == "Key"){
            printf("|:----------------|:--------|\n");
           }
        }
    }
        if ($1 == "File"){
           printf("\n---\n")
        }
    }' 
}

# Parse the given yml (arg1) to a markdown table and merge it with the content from the given file (arg2)
# 
insert_yaml(){
    local tmp
    tmp="$(cat $1 | parse_yaml $1)"
    cat $2 | awk -F "mail" -v tmp="$tmp" '{
            printf("%s\r", $0);
            if ($0 ~ /## '$3'/){
               print ""
               print tmp;
               exists="true" 
            }
         }
         # Append chapter and table if not exist yet
         END{if(exists!="true"){ 
            print "\n"
            print "## '$3' \n"
            print tmp;
         }}' 
}

# Collect all yml files in the given folder (arg1) and try to parse them.
# Then merge the tables with the content from the given template file (arg2)
# and finally create a file with this content (arg3)
handle_ymls(){
    cp $2 tmp.md
    for file in $1/*.yml 
        do
            filename=$(basename "$file")
            filename="${filename%.*}"
            if [ "$filename" != "template" ] 
            then
              insert_yaml $file tmp.md $filename > mytest.md;
              rm tmp.md;
              cp mytest.md tmp.md;
            fi
        done
    rm tmp.md
    if [ -f mytest.md ]; then
      mv mytest.md $3
    else
      echo "Parsing of yml files failed. Maybe there are no yml besides \"template.yml\"."
    fi
    
}

handle_ymls $1 $2 $3
