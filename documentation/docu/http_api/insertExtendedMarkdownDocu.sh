#!/bin/bash
cd documents/before/
pandoc -f markdown -t html5 -o ../html/intro.html --columns=1000 1_introduction.md
cd ../after
pandoc -f markdown -t html5 -o ../html/appendix.html --columns=1000 2_column_identifiers.md 3_flags_bitmasks.md 4_mail_filter.md 5_advanced_search.md
cd ../..
cp ./tmpl/index_tmpl.html ./html/index.html
sed -e '/OX_MARKDOWN_INTRO/r./documents/html/intro.html' > ./html/index.html < ./tmpl/index_tmpl.html
cp ./html/index.html tmp.html
sed -e '/OX_MARKDOWN_APPENDIX/r./documents/html/appendix.html' > ./html/index.html < tmp.html
rm tmp.html