#! /usr/bin/perl -w
#
# generate mpasswd file
#

use strict;

my $MASTER = $ARGV[1] || "oxadminmaster";
my $MPW    = $ARGV[2] || "/opt/open-xchange/etc/admindaemon/mpasswd";

print "Using \"$MASTER\" as master user\n";
print "Using \"$MPW\" as mpasswd file\n";

system("stty -echo");
print "Enter $MASTER password: ";
while( <STDIN> ) {
    chomp;
    last if $_ ne "";
}
print "\n";
my $PW = $_;
system("stty echo");

my $ENPW=crypt($PW, pack("C2",(int(rand 26)+65),(int(rand 26)+65)));

open(OUT,">$MPW") || die "unable to open $MPW: $!";
print OUT "$MASTER:$ENPW\n";
close(OUT);
