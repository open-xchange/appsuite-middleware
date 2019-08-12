#!/bin/bash

cd documents/intro/
pandoc -f markdown -t html5 -o ./intro.html --columns=1000 -p 1_introduction.md
cd ../appendix/
pandoc -f markdown -t html5 -o ./appendix.html --columns=1000 -p 2_column_identifiers.md 3_flags_bitmasks.md 4_mail_filter.md 5_advanced_search.md 6_calendar_account_configuration.md 7_module_config.md
cd ../
sed -e '/OX_MARKDOWN_INTRO/r ./intro/intro.html' ../index.html > ./html/tmp.html

sed -e '/OX_MARKDOWN_APPENDIX/r ./appendix/appendix.html' ./html/tmp.html > ./html/index.html

rm ./html/tmp.html