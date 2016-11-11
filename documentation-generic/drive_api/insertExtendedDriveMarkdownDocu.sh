#!/bin/bash
cd documents/intro/
pandoc -f markdown -t html5 -o ../html/intro.html --columns=1000 -p intro.md
cd ../appendix
pandoc -f markdown -t html5 -o ../html/appendix.html --columns=1000 -p 1_Name_Restrictions.md 2_Client_side_filtering.md 3_Metadata_Synchronization.md 4_Sync_Multiple.md
cd ../../../../../SwaggerUI/
cp ./tmpl/index_tmpl.html ./html/index.html
sed -e '/OX_MARKDOWN_INTRO/r../backend/documentation-generic/drive_api/documents/html/intro.html' > ./html/index.html < ./tmpl/index_tmpl.html
cp ./html/index.html tmp.html
sed -e '/OX_MARKDOWN_APPENDIX/r../backend/documentation-generic/drive_api/documents/html/appendix.html' > ./html/index.html < tmp.html
cp ./html/index.html tmp.html
sed -e 's/<!--OX_MARKDOWN_TITLE-->/OX Drive API/' > ./html/index.html < tmp.html
cp ./html/index.html tmp.html
sed -e 's/<!--OX_MARKDOWN_SWAGGER_FOLDER-->/drive_api/' > ./html/index.html < tmp.html
rm tmp.html