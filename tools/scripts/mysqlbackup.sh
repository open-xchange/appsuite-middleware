#!/bin/bash

mysql -h devel-slave -u openexchange -psecret -B -N -e 'SHOW DATABASES' | egrep -v 'mysql|information_schema' | xargs mysqldump -h devel-slave -u openexchange -psecret --single-transaction --databases 

