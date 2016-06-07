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
#   trademarks of the OX Software GmbH group of companies.
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
#    Copyright (C) 2016-2020 OX Software GmbH
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

# debian postinst is going to fail when not set'ting +e
set +e

# CentOS moves utils like pidof to /sbin so we have to append it to $PATH if
# not already contained
[[ "$PATH" =~ (^|:)/sbin:* ]] || PATH=${PATH}:/sbin

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
    minor_version=$(detect_minor_java_version)
    if [ $minor_version -lt 7 ]; then
      JAVA_BIN=/opt/open-xchange/sbin/insufficientjava
    fi
}

# Detect the minor version of the selected JVM
#
# JVMs output e.g: java version "1.7.0_80" as part of their version
# specification. From this line we simply extract the minor version
# which would be 7 in this case.
#
# Returns the detected minor version or -1 if it can't be detected
function detect_minor_java_version () {
    version_line_array=( $($JAVA_BIN -version 2>&1 | grep version) )
    unquoted_version=${version_line_array[2]//\"/}
    version_components=( ${unquoted_version//./ } )
    major=${version_components[1]}
    echo ${major:--1}
}

DEBIAN=1
REDHAT=2
SUSE=4
LSB=8
UCS=16
ox_system_type() {
    local ret=0
    local isucs=$(uname -r|grep ucs)
    if [ -f /etc/debian_version ] && [ -z "$isucs" ]; then
        ret=$(( $ret | $DEBIAN ))
    elif [ -n "$isucs" ]; then
        ret=$(( $ret | $UCS))
    elif [ -f /etc/SuSE-release ]; then
        ret=$(( $ret | $SUSE ))
        ret=$(( $ret | $LSB ))
    elif [ -f /etc/redhat-release ]; then
        ret=$(( $ret | $REDHAT ))
        ret=$(( $ret | $LSB ))
    fi
    return $ret
}

# init script stuff

ox_start_daemon() {
    local path="$1"
    local name="$2"
    local user="$3"
    local group="$4"
    test -z "$path" && die "ox_start_daemon: missing path argument (arg 1)"
    test -x $path   || die "ox_start_daemon: $path is not executable"
    test -z "$name" && die "ox_start_daemon: missing name argument (arg 2)"
    local runasuser=
    test -n "$user"   && runasuser="--chuid $user"
    local runasgroup=
    test -n "$group"  && runasgroup="--group $group"
    ox_system_type
    local type=$?
    if [ $type -eq $DEBIAN -o $type -eq $UCS ]; then
        start-stop-daemon $runasuser $runasgroup --background --start --oknodo --startas $path --make-pidfile --pidfile /var/run/${name}.pid
    elif [ $(( $type & $LSB )) -eq $LSB ]; then
        if [ -n "$user" ] && [ "$user" != "root" ]; then
            su -s /bin/bash $user -c $path > /dev/null 2>&1 & echo $! > /var/run/${name}.pid
        else
            $path > /dev/null 2>&1 & echo $! > /var/run/${name}.pid
        fi
    else
        die "Unable to handle unknown system type"
    fi
}

ox_is_running() {
    local name="$1"
    local pattern="$2"
    local pid="$3"
    test -z "$name" && die "ox_is_running: missing name argument (arg 1)"
    test -z "$pattern" && die "ox_is_running: missing pattern argument (arg 2)"

    if [ -z "$pid" ]; then
       if [ -e /var/run/${name}.pid ]; then
          read pid < /var/run/${name}.pid
       fi
    fi
    if [ -n "$pid" ]; then
        # take care nothing influences line length if ps output
        COLUMNS=1000
        if ps $pid | grep "$pattern" > /dev/null; then
           return 0
        else
           return 1
        fi
    else
        return 1
    fi
}

ox_stop_daemon() {
    local name="$1"
    local nonox="$2"
    test -z "$name" && die "ox_stop_daemon: missing name argument (arg 1)"
    ox_system_type
    local type=$?

    if [ ! -f /var/run/${name}.pid ]; then
        return 0
    fi
    read PID < /var/run/${name}.pid
    test -z "$PID" && { echo "No process in pidfile '/var/run/${name}.pid' found running; none killed."; return 1; }
    if [ -z "$nonox" ]; then
        ps $PID > /dev/null && /opt/open-xchange/sbin/shutdown -w > /dev/null 2>&1
    fi
    ps $PID > /dev/null && kill -QUIT $PID
    ps $PID > /dev/null && kill -TERM $PID
    rm -f /var/run/${name}.pid
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
    test -z "$propfile" && die "ox_set_property: missing propfile argument (arg 3)"
    test -e "$propfile" || die "ox_set_property: $propfile does not exist"
    local tmp=${propfile}.tmp$$
    cp -a --remove-destination $propfile $tmp

    ox_system_type
    local type=$?
    if [ $type -eq $DEBIAN -o $type -eq $UCS ]; then
        local origfile="${propfile}.dpkg-new"
        if [ ! -e $origfile ]; then
            local origfile="${propfile}.dpkg-dist"
        fi
    else
        local origfile="${propfile}.rpmnew"
    fi
    if [ -n "$origfile" ] && [ -e "$origfile" ]; then
        export origfile
        export propfile
        export prop
        export val

        perl -e '
use strict;

open(IN,"$ENV{origfile}") || die "unable to open $ENV{origfile}: $!";
open(OUT,"$ENV{propfile}") || die "unable to open $ENV{propfile}: $!";

my @LINES = <IN>;
my @OUTLINES = <OUT>;

my $opt = $ENV{prop};
my $val = $ENV{val};
my $count = 0;
my $back  = 1;
my $out = "";
foreach my $line (@LINES) {
    if ( $line =~ /^$opt\s*[:=]/ ) {
      $out = $line;
      $out =~ s/^(.*?[:=]).*$/$1$val/;
      while ( $LINES[$count-$back] =~ /^#/ ) {
          $out = $LINES[$count-$back++].$out;
      }
    }
    $count++;
}

$back  = 0;
$count = 0;
my $start = 0;
my $end = 0;
my $found = 0;
foreach my $line (@OUTLINES) {
    if ( $line =~ /^$opt\s*[:=]/ ) {
        $found=1;
        $end=$count;
        while ( $OUTLINES[$count-++$back] =~ /^#/ ) {
        }
        ;
        if ( $count > 0 && $back > 1 ) {
            $start=$count-$back+1;
        } else {
            $start=$end;
        }
    }
    $count++;
}

if ( length($out) == 0 ) {
    $out=$opt."=".$val."\n";
}

if ( $found ) {
    for (my $i=0; $i<=$#OUTLINES; $i++) {
        if ( $i < $start || $i > $end ) {
            print $OUTLINES[$i];
            print "\n" if( substr($OUTLINES[$i],-1) ne "\n" );
        }
        if ( $i == $start ) {
            print $out;
            print "\n" if( substr($OUTLINES[$i],-1) ne "\n" );
        }
    }
} else {
    print @OUTLINES;
    print "\n" if( substr($OUTLINES[-1],-1) ne "\n" );
    print $out;
    print "\n";
}
' > $tmp
        if [ $? -gt 0 ]; then
            rm -f $tmp
            die "ox_set_property: FATAL: error setting property $prop to \"$val\" in $propfile"
        else
            mv $tmp $propfile
        fi
        unset origfile
        unset propfile
        unset prop
        unset val
    else
        # quote & in URLs to make sed happy
        test -n "$val" && val="$(echo $val | sed 's/\&/\\\&/g')"
        if grep -E "^$prop *[:=]" $propfile >/dev/null; then
            cat<<EOF | sed -f - $propfile > $tmp
s;\(^$prop[[:space:]]*[:=]\).*$;\1${val};
EOF
        else
        # add a newline to the last line if it doesn't exist
        sed -i -e '$a\' $tmp
            echo "${prop}=$val" >> $tmp
        fi
        if [ $? -gt 0 ]; then
            rm -f $tmp
            die "ox_set_property: FATAL: error setting property $prop to \"$val\" in $propfile"
        else
            mv $tmp $propfile
        fi
    fi
}

# usage:
# ox_exists_property property /path/to/file
#
ox_exists_property() {
    local prop="$1"
    local propfile="$2"
    test -z "$prop"     && die "ox_exists_property: missing prop argument (arg 1)"
    test -z "$propfile" && die "ox_exists_property: missing propfile argument (arg 2)"
    test -e "$propfile" || die "ox_exists_property: $propfile does not exist"

    local escaped=$(sed 's/[]\.|$(){}?+*^[]/\\&/g' <<< "$prop")
    grep -E "^$escaped *[:=]" $propfile >/dev/null || return 1
}

# savely find key/val in keys and values containing all kind of ugly chars
# delimiter must be either = or :
save_read_prop() {
    export prop="$1"
    export propfile="$2"
    perl -e '
use strict;

my $file=$ENV{"propfile"};
my $search=$ENV{"prop"};
open(FILE,$file) || die "unable to open $file: $!";
my $val=undef;
while(<FILE>) {
    chomp;
    my $len=length($search);
    if( substr($_,0,$len) eq $search ) {
        if( substr($_,$len,$len+1) !~ /^[\s=:]/ ) {
           next;
        }
        foreach my $dl ( "=", ":" ) {
           my $idx=index($_,$dl);
           if( $idx >= $len ) {
              $val=substr($_,$idx+1);
           }
           last if defined($val);
        }
        last;
    }
}
print "$val\n";

close(FILE);
'
}

# usage:
# ox_read_property property /path/to/file
#
ox_read_property() {
    local prop="$1"
    local propfile="$2"
    test -z "$prop"     && die "ox_read_property: missing prop argument (arg 1)"
    test -z "$propfile" && die "ox_read_property: missing propfile argument (arg 2)"
    test -e "$propfile" || die "ox_read_property: $propfile does not exist"

    # sed -n -e "/^$prop/Is;^$prop *[:=]\(.*\).*$;\1;p" < $propfile
    # UGLY: we have keys containing /
    save_read_prop "$prop" "$propfile"
}

# usage:
# ox_remove_property property /path/to/file
#
ox_remove_property() {
    local prop="$1"
    local propfile="$2"
    test -z "$prop"     && die "ox_remove_property: missing prop argument (arg 1)"
    test -z "$propfile" && die "ox_remove_property: missing propfile argument (arg 2)"
    test -e "$propfile" || die "ox_remove_property: $propfile does not exist"

    local tmp=${propfile}.tmp$$
    cp -a --remove-destination $propfile $tmp

    export propfile
    export prop
    perl -e '
use strict;

open(IN,"$ENV{propfile}") || die "unable to open $ENV{propfile}: $!";

my @LINES = <IN>;

my $opt = $ENV{prop};
my $count = 0;
my $back  = 1;
my $start = 0;
my $end = 0;
foreach my $line (@LINES) {
    if ( $line =~ /^$opt\s*[:=]/ ) {
        $end=$count;
        while ( $LINES[$count-$back++] =~ /^#/ ) {
        }
        $start=$count-$back;
    }
    $count++;
}
if ( $LINES[$end+1] =~ /^\s*$/ ) {
    $end++;
}
for (my $i=0; $i<=$#LINES; $i++) {
    if ( $i <= $start+1 || $i > $end ) {
        print $LINES[$i];
    }
}
' > $tmp
    if [ $? -gt 0 ]; then
        rm -f $tmp
        die "ox_remove_property: FATAL: error removing property $prop from $propfile"
    else
        mv $tmp $propfile
    fi
    unset propfile
    unset prop
}

# adding or removing comment (ONLY # supported)
#
# usage:
# ox_comment property action /path/to/file
# where action can be add/remove
#
ox_comment(){
    local prop="$1"
    local action="$2"
    local propfile="$3"
    test -z "$prop"     && die "ox_comment: missing prop argument (arg 1)"
    test -z "$action"      && die "ox_comment: missing action argument (arg 2)"
    test -z "$propfile" && die "ox_comment: missing propfile argument (arg 3)"
    test -e "$propfile" || die "ox_comment: $propfile does not exist"
    local tmp=${propfile}.tmp$$
    cp -a --remove-destination $propfile $tmp
    if [ "$action" == "add" ]; then
        sed "s/^$prop/# $prop/" < $propfile > $tmp;
        if [ $? -gt 0 ]; then
            rm -f $tmp
            die "ox_comment: FATAL: could not add comment in file $propfile to $prop"
        else
            mv $tmp $propfile
        fi
    elif [ "$action" == "remove" ];then
      sed "s/^#[ ]*\($prop[ ]*=\)/\1/" < $propfile > $tmp;
        if [ $? -gt 0 ]; then
            rm -f $tmp
            die "ox_comment: FATAL: could not remove comment in file $propfile for $prop"
        else
            mv $tmp $propfile
        fi
    else
        die "ox_handle_hash: action must be add or remove while it is $action"
    fi
}

ox_update_permissions(){
    local pfile="$1"
    local owner="$2"
    local mode="$3"
    test -z "$pfile" && die "ox_update_permissions: missing pfile argument"
    test -z "$owner" && die "ox_update_permissions: missing owner argument"
    test -z "$mode" && die "ox_update_permissions: missing mode argument"
    test -e "$pfile" || die "ox_update_permissions: $pfile does not exist"

    chmod $mode "$pfile"
    chown $owner "$pfile"
}

die() {
    test -n "$1" && echo 1>&2 "$1" || echo 1>&2 "ERROR"
    exit 1
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
        dirbundles=( ${dirbundles[*]} "reference\:file\:${bpath}" )
    done

    if [ -f $cini ]; then
        # read all bundles listed in config.ini into an array
        local configbundles=( $(sed -e \
            '/^osgi.bundles.*/Is;^osgi.bundles=\(.*\);\1;' \
            -n -e 's;,; ;gp' < $cini ) )
    fi

    cp $cinitemplate $cini
    echo "osgi.bundles=$(echo ${dirbundles[@]} | sed 's; ;,;g')" >> $cini
}

ox_save_backup() {
    local name=$1
    test -z "$name" && die "ox_save_backup: missing name argument (arg1)"

    local backup_name="${name}.old"
    if [ -e $name ]; then
        mv $name $backup_name
    fi
}

# move configuration file from one location/package to another
# RPM ONLY!
ox_move_config_file() {
    local srcdir="$1"
    local dstdir="$2"
    local srcname="$3"
    local dstname="$4"
    test -z "$srcdir" && die "ox_move_config_file: missing srcdir argument (arg1)"
    test -z "$dstdir" && die "ox_move_config_file: missing dstdir argument (arg2)"
    test -z "$srcname" && die "ox_move_config_file: missing srcname argument (arg3)"
    test -z "$dstname" && dstname=$srcname

    if [ -e "${srcdir}/${srcname}" ]; then
        if [ -e "${dstdir}/${dstname}" ] && \
           ! cmp -s "${dstdir}/${dstname}" "${srcdir}/${srcname}" > /dev/null; then
           mv "${dstdir}/${dstname}" "${dstdir}/${dstname}.rpmnew"
        fi
        mv "${srcdir}/${srcname}" "${dstdir}/${dstname}"
    fi
}

# ox_add_property property value /path/to/file
# verifies first that the property does not already exist in file and adds it then
ox_add_property() {
    local property="$1"
    local value="$2"
    local propfile="$3"
    test -z "$property" && die "ox_add_property: missing property argument (arg 1)"
    test -z "$propfile" && die "ox_add_property: missing propfile argument (arg 3)"
    test -e "$propfile" || die "ox_add_property: $propfile does not exist"

    if ! ox_exists_property "$property" "$propfile"
    then
        ox_set_property "$property" "$value" "$propfile"
    fi
}
