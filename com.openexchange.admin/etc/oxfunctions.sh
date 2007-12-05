#
#
#   OPEN-XCHANGE legal information
#
#   All intellectual property rights in the Software are protected by
#   international copyright laws.
#
#
#   In some countries OX, OX Open-Xchange, open xchange and OXtender
#   as well as the corresponding Logos OX Open-Xchange and OX are registered
#   trademarks of the Open-Xchange, Inc. group of companies.
#   The use of the Logos is not covered by the GNU General Public License.
#   Instead, you are allowed to use these Logos according to the terms and
#   conditions of the Creative Commons License, Version 2.5, Attribution,
#   Non-commercial, ShareAlike, and the interpretation of the term
#   Non-commercial applicable to the aforementioned license is published
#   on the web site http://www.open-xchange.com/EN/legal/index.html.
#
#   Please make sure that third-party modules and libraries are used
#   according to their respective licenses.
#
#   Any modifications to this package must retain all copyright notices
#   of the original copyright holder(s) for the original code used.
#
#   After any such modifications, the original and derivative code shall remain
#   under the copyright of the copyright holder(s) and/or original author(s)per
#   the Attribution and Assignment Agreement that can be located at
#   http://www.open-xchange.com/EN/developer/. The contributing author shall be
#   given Attribution for the derivative code and a license granting use.
#
#    Copyright (C) 2004-2006 Open-Xchange, Inc.
#    Mail: info@open-xchange.com
#
#
#    This program is free software; you can redistribute it and/or modify it
#    under the terms of the GNU General Public License, Version 2 as published
#    by the Free Software Foundation.
#
#    This program is distributed in the hope that it will be useful, but
#    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
#    or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
#    for more details.
#
#    You should have received a copy of the GNU General Public License along
#    with this program; if not, write to the Free Software Foundation, Inc., 59
#    Temple Place, Suite 330, Boston, MA 02111-1307 USA

JAVA_BIN=

ox_set_JAVA_BIN() {
    JAVA_BIN=$(which java)
    if [ -z "$JAVA_BIN" ]; then
	local jb=$JAVA_HOME/bin/java
	if [ -x $jb ]; then
	    JAVA_BIN=$jb
	fi
    fi
    if [ -z "$JAVA_BIN" ]; then
	local jb=$JRE_HOME/bin/java
	if [ -x $jb ]; then
	    JAVA_BIN=$jb
	fi
    fi
    test -x $JAVA_BIN || die "$0: unable to get path to java vm"
}

# init script stuff

ox_start_daemon() {
    local path="$1"
    local name="$2"
    local user="$3"
    local group="$4"
    test -z "$path" && die "ox_start_daemon: missing path argument (arg 1)"
    test -z "$name" && die "ox_start_daemon: missing name argument (arg 2)"
    local runasuser=
    test -n "$user"   && runasuser="--chuid $user"
    local runasgroup=
    test -n "$group"  && runasgroup="--group $group"
    start-stop-daemon $runasuser $runasgroup \
    	--background --start --oknodo --startas $path \
	--make-pidfile --pidfile /var/run/${name}.pid
}

ox_stop_daemon() {
    local path="$1"
    local name="$2"
    test -z "$path" && die "ox_stop_daemon: missing path argument (arg 1)"
    test -z "$name" && die "ox_stop_daemon: missing name argument (arg 2)"
    test -x $path ||   die "ox_stop_daemon: $path is not executable"
    #FIXME: --retry results into annoying warnings
    #start-stop-daemon --stop --oknodo --pidfile /var/run/${name}.pid --retry 5
    start-stop-daemon --stop --oknodo --pidfile /var/run/${name}.pid
    rm -f /var/run/${name}.pid
}

ox_restart_daemon() {
    local path="$1"
    local name="$2"
    local user="$3"
    local group="$4"
    test -z "$path" && die "ox_restart_daemon: missing path argument (arg 1)"
    test -z "$name" && die "ox_restart_daemon: missing name argument (arg 2)"
    test -x $path ||   die "ox_restart_daemon: $path is not executable"
    test -z "$user" && user=root
    ox_stop_daemon $path $name
    sleep 3
    ox_start_daemon $path $name $user $group
}

ox_daemon_status() {
    local pidfile="$1"
    test -z "$pidfile" && die "ox_daemon_status: missing pidfile argument (arg 1)"
    if [ ! -f $pidfile ]; then
        # not running
        return 1
    fi
    read PID < $pidfile
    running=$(ps $PID | grep $PID)
    if [ -n "$running" ]; then
        # running
	return 0
    else
        # not running
	return 1
    fi
}

# usage:
# ox_set_property property value /path/to/file
# 
ox_set_property() {
    local prop="$1"
    local val="$2"
    local propfile="$3"
    test -z "$prop"     && die "ox_set_property: missing prop argument (arg 1)"
    test -z "$val"      && die "ox_set_property: missing val argument (arg 2)"
    test -z "$propfile" && die "ox_set_property: missing propfile argument (arg 3)"
    test -e "$propfile" || die "ox_set_property: $propfile does not exist"
    local tmp=${propfile}.tmp$$
    rm -f $tmp
    # quote & in URLs to make sed happy
    val="$(echo $val | sed 's/\&/\\\&/g')"
    # some values need quoting, so leave quotes, if already present
    local q=
    if grep -E "^.*$prop[:=].*\".*\".*$" $propfile >/dev/null; then
	q='"'
    fi
    if grep -E "^$prop" $propfile >/dev/null; then
	cat<<EOF | sed -f - $propfile > $tmp
s;\(^$prop[:=]\).*$;\1${q}${val}${q};
EOF
	if [ $? -gt 0 ]; then
	    rm -f $tmp
	    die "ox_set_property: FATAL: error setting property $prop to \"$val\" in $propfile"
	else
	    mv $tmp $propfile
	fi
    else
	echo "${prop}=$val" >> $propfile
    fi
}

# common functions

die() {
    test -n "$1" && echo 1>&2 "$1" || echo 1>&2 "ERROR"
    exit 1
}

# checks if admindaemon is running
ox_isrunning() {
    # FIXME: this check must always fail in OX EE
    return 1

    local pidfile=/var/run/open-xchange-admin.pid
    if [ ! -f $pidfile ]; then
        # not running
        return 1
    fi
    read PID < $pidfile
    running=$(ps $PID | grep $PID)
    if [ -n "$running" ]; then
        # running
        return 0
    else
        # not running
        return 1
    fi
}

ox_update_config_init() {
    local cini=$1
    local cinitemplate=$2
    local bdir=$3

    test -z "$cini" && die \
	"ox_update_config_init: missing config.ini argument (arg 1)"
    test -z "$cinitemplate" && die \
	"ox_update_config_init: missing config.ini template argument (arg 2)"
    test -z "$bdir" && die \
	"ox_update_config_init: missing bundle.d argument (arg 3)"

    test -d $bdir || die "$bdir is not a directory"
    test -f $cinitemplate || die "$cinitemplate does not exist"
    test "$(echo $bdir/*.ini)" == "$bdir/*.ini" && die "$bdir is empty"

    # read all installed bundles into an array
    local dirbundles=()
    local bpath=
    for bundle in $bdir/*.ini; do
	read bpath < $bundle
	dirbundles+=( "reference\:file\:${bpath}@start" )
    done

    # read all bundles listed in config.ini into an array
    local configbundles=( $(sed -e '/^osgi.bundles.*/Is;^osgi.bundles=\(.*\);\1;' \
	-n -e 's;,; ;gp' < $cini ) )

    # check if amount of bundles installed in bundles directory does not
    # match and if that's the case, generate new config.ini
    #
    if [ ${#dirbundles[@]} -ne ${#configbundles[@]} ]; then
	echo "updating $cini"
	cp $cinitemplate $cini
	echo "osgi.bundles="$(echo ${dirbundles[@]} | sed 's; ;,;') >> $cini
    fi
}

ox_register_plugin() {
    local plugin=$1
    local osgicf=$2
    local plugpath=$3
    local level=$4

    test -z "$plugin" && die \
	"ox_register_plugin: missing plugin argument (arg 1)"
    test -z "$osgicf" && die \
	"ox_register_plugin: missing osgicf argument (arg 2)"
    test -z "$plugpath" && die \
	"ox_register_plugin: missing plugpath argument (arg 3)"
#    test -z "$level" && die \
#	"ox_register_plugin: missing level argument (arg 4)"

    if grep $plugin $osgicf >/dev/null; then
	echo "plugin $plugin already registered"
    else
	echo "registering plugin $plugin"

	local osgicftmp=${osgicf}.$$
	sed -e "s;\(^osgi.bundles=.*\)$;\1,reference\\:file\\:${plugpath}/${plugin}@start;" \
	    < $osgicf > $osgicftmp
	mv $osgicftmp $osgicf
    fi
}

ox_unregister_plugin() {
    local plugin=$1
    local osgicf=$2

    test -z "$plugin" && die \
	"ox_unregister_plugin: missing plugin argument (arg 1)"
    test -z "$osgicf" && die \
	"ox_unregister_plugin: missing osgicf argument (arg 2)"

    if grep $plugin $osgicf >/dev/null; then
	echo "unregistering plugin $plugin"

	local osgicftmp=${osgicf}.$$
	local ALLPLUGS=$(sed -e 's;,; ;g' -n -e 's;^osgi.bundles=\(.*\);\1;p' $osgicf)
	local NEWPLUGS=$(
	    for plug in $ALLPLUGS; do
		if ! echo $plug | grep $plugin >/dev/null; then
		    echo -n "$plug,"
		fi
	    done)
	NEWPLUGS=${NEWPLUGS:0:( ${#NEWPLUGS} -1 )}

	sed -e "s;^osgi.bundles=.*;osgi.bundles=$NEWPLUGS;" \
	    < $osgicf > $osgicftmp
	mv $osgicftmp $osgicf
    else
	echo "plugin $plugin not registered"
    fi
}

ox_add_hosts_hostip() {
    local fqhn=$1
    local addr=$2
    
    test -z "$fqhn" && die \
	"ox_add_hosts_hostip: missing fqhn argument (arg 1)"
    test -z "$addr" && die \
	"ox_add_hosts_hostip: missing addr argument (arg 2)"

    local hostarr=( $(echo $fqhn | sed -e 's/\./ /g') )
    local hn=${hostarr[0]}

    # workaround for Bug ID#7803 FQDN is replaced by a DHCP value after installation
    # something's adding non fqhn to hosts, so add own entry on top
    local htmp=/etc/hosts.$$
    echo -e "$addr\t\t$fqhn $hn" > $htmp
    cat /etc/hosts >> $htmp
    mv $htmp /etc/hosts
}

ox_remove_hosts_hostip() {
    local addr=$1
    
    test -z "$addr" && die \
	"ox_remove_hosts_hostip: missing addr argument (arg 1)"

    local hosttmp=/etc/hosts.$$
    grep -v "$addr" /etc/hosts > $hosttmp
    mv $hosttmp /etc/hosts
}

ox_remove_hosts_hostname() {
    local name=$1
    
    test -z "$name" && die \
	"ox_remove_hosts_hostname: missing name argument (arg 1)"

    ox_remove_hosts_hostip $name
}