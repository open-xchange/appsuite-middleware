#!/usr/bin/perl -w
#%# family=auto
#%# capabilities=autoconf

$mox="mox1";
$showruntimestats="/bin/showRuntimeStats $mox";


if ( $ARGV[0] and $ARGV[0] eq "autoconf")
{
	if (-x $showruntimestats) {
		print "yes\n";
		exit 0;
	}
	else {
		print "no\n";
		# Always return 0 (see http://munin-monitoring.org/wiki/ConcisePlugins)
		exit 0;
	}
}

if ( $ARGV[0] and $ARGV[0] eq "config")
{
	print "graph_title  $mox\n";
	print "graph_args --base 1000 -l 0\n";
	print "graph_category Open Xchange\n";
	print "graph_vlabel \n";
	print "a.label \n";
	print "b.label \n";
	print "c.label \n";
	exit 0
}
									

open(SHOWRUNTIME,"$showruntimestats |") || die "can not read monitoring output";   

while (<SHOWRUNTIME>)
{
	if ( $_ =~ //) {
		s/.*\=//; 
		$val=$_+0;
		print "a.value $val\n";
		next;
	}
}

close (SHOWRUNTIME);
