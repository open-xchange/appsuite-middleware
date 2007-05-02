#! /bin/bash
#
# $Id$
#
# Copyright (C) 2004-2007 Open-Xchange, Inc.
#
# apply updates to database
#
DBUSER=openexchange
DBUSER_LONG=dbuser
DBPASS=
DBPASS_LONG=dbpass
CONFIGDB=localhost
CONFIGDB_LONG=configdb
OXDB=localhost
OXDB_LONG=oxdb
CHSQL=changes.sql
CHSQL_LONG=changesfile

MUSTOPTS="DBPASS"
LONGOPTS='$DBUSER_LONG:,$DBPASS_LONG:,$CONFIGDB_LONG:,$OXDB_LONG:,$CHSQL_LONG:'

die() {
    test -n "$1" && echo 1>&2 "$1" || echo 1>&2 "ERROR"
    exit 1
}

usage() {
    echo
    echo "$0 currently knows the following parameters:"
    echo
    local lopts=$(echo $LONGOPTS | sed -e 's/[:,]/ /g')
    printf '%-30s | %s\n' "Parameter" "Default value"
    echo "------------------------------------------------------------"
    for opt in $lopts; do
	local rvar=${opt%_LONG}
	local default=$(eval echo $rvar)
	local lopt=$(eval echo $opt)
	#echo $opt $rvar $default $lopt
	printf '%-30s | %s\n' "--$lopt" $default
    done
    echo
    echo
    echo
cat<<EOF
Example:

  $0 --dbpass=secret
EOF
    echo
}

TEMP=$(POSIXLY_CORRECT=true getopt -o h --long "$(eval echo $LONGOPTS),help" -- "$@")
eval set -- "$TEMP"

while true; do
    case "$1" in
	--$DBUSER_LONG)
	    DBUSER=$2
	    shift 2
	    ;;
	--$DBPASS_LONG)
	    DBPASS=$2
	    shift 2
	    ;;
	--$CONFIGDB_LONG)
	    CONFIGDB=$2
	    shift 2
	    ;;
	--$OXDB_LONG)
	    OXDB=$2
	    shift 2
	    ;;
	--$CHSQL_LONG)
	    CHSQL=$2
	    shift 2
	    ;;
	-h|--help)
	    usage
	    shift
	    ;;
	--)
	    shift
	    break
	    ;;
	*)
	    die "Internal error!"
	    exit 1
	    ;;
    esac
done

# generic parameter checking
for opt in $MUSTOPTS; do
    opt_var=$(eval echo \$$opt)
    opt_var_long=$(eval echo \$${opt}_LONG)
    opt_var_values=$(eval echo \$${opt}_VALUES)
    if [ -z "$opt_var" ]; then
	die "missing required option --$opt_var_long"
    fi
    if [ -n "$opt_var_values" ]; then
	found=
	for val in $opt_var_values; do
	    if [ "$val" == "$opt_var" ]; then
		found=$val
	    fi
	done
	if [ -z "$found" ]; then
	    die "\"$opt_var\" is not a valid option to --$opt_var_long"
	fi
    fi
done

test -f $CHSQL || {
	echo "missing file $(pwd)/${CHSQL}, exiting"
	exit 1
}

BFILE="brokenlinks$$"
rm -f $BFILE

for db in $(echo "select name from db_pool,db_cluster where db_cluster.write_db_pool_id=db_pool.db_pool_id;" | \
            mysql -h $CONFIGDB -u $DBUSER -p"${DBPASS}" configdb | grep -v name); do

	echo "Found database \"$db\" in configdb"
	oxdbs=( $(echo "show databases;" | \
		mysql -h $OXDB -u $DBUSER -p"${DBPASS}" | grep ${db}) )
	echo "Found total amount of ${#oxdbs[*]} databases schemas"
	for dbs in ${oxdbs[@]}; do
		echo "Applying changes to $dbs"
		mysql -h $OXDB -u $DBUSER -p"${DBPASS}" $dbs < $CHSQL
		echo 'SELECT * FROM prg_links WHERE firstfolder=-1 OR secondfolder=-1;' | \
			mysql -h $OXDB -u $DBUSER -p"${DBPASS}" $dbs > $BFILE
	done
done

if [ -s $BFILE ]; then
	echo "there are broken links, please send the output below to the OX Developers"
	echo "-----------------------------------[snip]--------------------------------"
	cat $BFILE
	echo "-----------------------------------[snap]--------------------------------"
fi
rm -f $BFILE
