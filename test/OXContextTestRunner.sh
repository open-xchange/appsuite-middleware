#!/bin/bash

JAVA_BIN=$(which java)
test -z "$JAVA_BIN" && {
	echo "no java binary found, please set JAVA_BIN environment variable!"
	exit
}

SERVERNAME=$HOSTNAME

$JAVA_BIN -Dopenexchange.propfile=etc/system.properties -Dconfig=etc/AdminDaemon.properties -DserverNAME=$SERVERNAME -cp jar/ox_admindaemon.jar:lib/mysql.jar:lib/comfiretools.jar:lib/intranet.jar:lib/nas.jar:lib/jcs.jar:lib/commons-logging.jar:lib/concurrent.jar:lib/junit-4.1.jar com.openexchange.admin.test.adminCall.OXContextTestRunner
