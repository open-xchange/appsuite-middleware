#!/bin/bash

mysql -h devel-master -u openexchange -psecret -B -N -e 'SHOW DATABASES' | egrep -v 'mysql|information_schema' | xargs mysqldump -h devel-master -u openexchange -psecret --single-transaction --master-data=2 --add-drop-table --databases 

