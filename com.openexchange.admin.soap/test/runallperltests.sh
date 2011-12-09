#! /bin/bash

cleanup() {
   ./deletecontext
   ./unregisterdatabase
   ./unregisterfilestore $FNR
   ./unregisterserver
}

runcmd() {
   cmd="$1"
   arg="$2"
   echo "running \"$cmd $arg\" now" 1>&2
   ./$cmd $arg || { echo "running $cmd failed" | tee FAIL; cleanup; exit; }
}


cmdpath="$1/perl"
test -z "$1" && cmdpath=./perl

cd $cmdpath


runcmd registerdatabase
runcmd registerfilestore | cut -d" " -f 2 > /tmp/fnr
read FNR < /tmp/fnr && rm -f /tmp/fnr
echo "<<< $FNR >>>"
runcmd registerserver
runcmd createcontext
runcmd createuser
runcmd creategroup
runcmd createresource
runcmd changeresource
runcmd changegroup
runcmd changeuser
runcmd listcontext
runcmd listcontextbydatabase
runcmd listcontextbyfilestore $FNR
runcmd listdatabase
runcmd listfilestore
runcmd listgroup
runcmd listresource
runcmd listserver
runcmd listuser
runcmd deletegroup
runcmd deleteresource
runcmd deleteuser
runcmd deletecontext
runcmd unregisterdatabase
runcmd unregisterfilestore $FNR
runcmd unregisterserver

cd -
