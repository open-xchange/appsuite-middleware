#!/bin/bash
cd $(pwd)

cd documents/intro/
pandoc -f markdown -t html5 -o ./intro.html --columns=1000 -p intro.md
cd ../appendix/
pandoc -f markdown -t html5 -o ./appendix.html --columns=1000 -p 1_Name_Restrictions.md 2_Client_side_filtering.md 3_Metadata_Synchronization.md 4_Sync_Multiple.md
cd ../
sed -e '/OX_MARKDOWN_INTRO/r ./intro/intro.html' ../index.html > ./html/tmp.html

sed -e '/OX_MARKDOWN_APPENDIX/r ./appendix/appendix.html' ./html/tmp.html > ./html/index.html

rm ./html/tmp.html