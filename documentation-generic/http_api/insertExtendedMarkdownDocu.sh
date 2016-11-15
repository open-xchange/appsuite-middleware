#!/bin/bash
cd documents/intro/
pandoc -f markdown -t html5 -o ../html/intro.html --columns=1000 -p 1_introduction.md
cd ../appendix
pandoc -f markdown -t html5 -o ../html/appendix.html --columns=1000 -p 2_column_identifiers.md 3_flags_bitmasks.md 4_mail_filter.md 5_advanced_search.md
cd ../../../../../SwaggerUI/
cp ./tmpl/index_tmpl.html ./html/index.html
sed -e '/OX_MARKDOWN_INTRO/r../backend/documentation-generic/http_api/documents/html/intro.html' > ./html/index.html < ./tmpl/index_tmpl.html
cp ./html/index.html tmp.html
sed -e '/OX_MARKDOWN_APPENDIX/r../backend/documentation-generic/http_api/documents/html/appendix.html' > ./html/index.html < tmp.html
cp ./html/index.html tmp.html
sed -e 's/<!--OX_MARKDOWN_TITLE-->/OX HTTP API/' > ./html/index.html < tmp.html
cp ./html/index.html tmp.html
sed -e 's/<!--OX_MARKDOWN_SWAGGER_FOLDER-->/http_api/' > ./html/index.html < tmp.html
rm tmp.html