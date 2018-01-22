#!/bin/bash
cd documents/
mapfile -t introFiles < <(find -L ./intro -name \*.md | sort)
pandoc -f markdown -t html5 -o ./html/intro.html --columns=1000 -p "${introFiles[@]}"
mapfile -t appendixFiles < <(find -L ./appendix -name \*.md | sort)
pandoc -f markdown -t html5 -o ./html/appendix.html --columns=1000 -p "${appendixFiles[@]}"
cd ../../../../SwaggerUI/
cp ./tmpl/index_tmpl.html ./html/index.html
sed -e '/OX_MARKDOWN_INTRO/r../backend/documentation-generic/http_api/documents/html/intro.html' > ./html/index.html < ./tmpl/index_tmpl.html
cp ./html/index.html tmp.html
sed -e '/OX_MARKDOWN_APPENDIX/r../backend/documentation-generic/http_api/documents/html/appendix.html' > ./html/index.html < tmp.html
cp ./html/index.html tmp.html
sed -e 's/<!--OX_MARKDOWN_TITLE-->/OX HTTP API/' > ./html/index.html < tmp.html
cp ./html/index.html tmp.html
sed -e 's/<!--OX_MARKDOWN_SWAGGER_FOLDER-->/http/' > ./html/index.html < tmp.html
rm tmp.html