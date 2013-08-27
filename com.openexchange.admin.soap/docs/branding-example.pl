#! /usr/bin/perl -w
#
# Example on how to set the userAttributes in contexts to implement
# different brandings
# see http://oxpedia.org/wiki/index.php?title=CCBranding
#
use strict;
use SOAP::Lite;
use Data::Dumper;

# basics
my $baseURL     = "http://localhost/webservices/";
my $nameSpace   = "http://soap.admin.openexchange.com";

my $ctxadmin    = "oxadminmaster";
my $ctxadmpw    = "secret";

# object holding the credentials of the OX master admin account
my $creds    = SOAP::Data->type("Credentials")->value(
		                    \SOAP::Data->value(
				      SOAP::Data->name("login" => $ctxadmin),
				      SOAP::Data->name("password" => $ctxadmpw)
				      )
				);

##
## creating a SOAP client for the OXContext service
##
my $soclt = SOAP::Lite->ns( $nameSpace )->proxy( $baseURL."OXContextService" );

# mandatory user data for the context admin
my $displayname = "Context Admin";
my $surname     = "Admin";
my $gname       = "Context";
my $email       = "oxadmin\@example.com";
my $lang        = "de_DE";
my $timezone    = "Europe/Berlin";

########################################################################################
## Brand Groupware4You
##
my $ctxid_gw4u    = 424242;
my $ctxname_gw4u  = "Groupware4You";
my $ctxquota_gw4u = 5000;
my $brand_gw4u    = SOAP::Data->name("entries" => \SOAP::Data->value(
			SOAP::Data->name("key"   => "types"),
			SOAP::Data->name("value" => "gw4u")));

# context SOAP object
my $ctx_gw4u = SOAP::Data->type("Context")->value(
		 \SOAP::Data->value(
                  SOAP::Data->name("id" => $ctxid_gw4u),
		  SOAP::Data->name("name" => $ctxname_gw4u),
		  SOAP::Data->name("maxQuota" => $ctxquota_gw4u),
		  SOAP::Data->name("userAttributes" => \SOAP::Data->value(
		    SOAP::Data->name("entries" =>
		    \SOAP::Data->value(
		    	SOAP::Data->name("key" => "taxonomy"),
		    	SOAP::Data->name("value" => \SOAP::Data->value($brand_gw4u))))))
						));


########################################################################################
## Brand CollaborationPro
##
my $ctxid_cpro    = 424243;
my $ctxname_cpro  = "CollaborationPro";
my $ctxquota_cpro = 10000;
my $brand_cpro    = SOAP::Data->name("entries" => \SOAP::Data->value(
			SOAP::Data->name("key"   => "types"),
			SOAP::Data->name("value" => "cpro")));

# context SOAP object
my $ctx_cpro = SOAP::Data->type("Context")->value(
		 \SOAP::Data->value(
                  SOAP::Data->name("id" => $ctxid_cpro),
		  SOAP::Data->name("name" => $ctxname_cpro),
		  SOAP::Data->name("maxQuota" => $ctxquota_cpro),
		  SOAP::Data->name("userAttributes" => \SOAP::Data->value(
		    SOAP::Data->name("entries" =>
		    \SOAP::Data->value(
		    	SOAP::Data->name("key" => "taxonomy"),
		    	SOAP::Data->name("value" => \SOAP::Data->value($brand_cpro))))))
						));


########################################################################################
## create contexts
##

my $result = $soclt->create($ctx_gw4u,
		  SOAP::Data->value("User")->value(\SOAP::Data->value(
			SOAP::Data->name("name" => $ctxadmin),
			SOAP::Data->name("password" => $ctxadmpw),
			SOAP::Data->name("display_name" => $displayname),
			SOAP::Data->name("sur_name" => $surname),
			SOAP::Data->name("given_name" => $gname),
			SOAP::Data->name("primaryEmail" => $email),
			SOAP::Data->name("email1" => $email),
			SOAP::Data->name("language" => $lang),
			SOAP::Data->name("timezone" => $timezone)
			)),
		  $creds
		 );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();

# read data and dump to console
$result = $soclt->getData($ctx_gw4u,$creds);
my @data = $result->paramsall;
print Dumper(@data);


$result = $soclt->create($ctx_cpro,
		  SOAP::Data->value("User")->value(\SOAP::Data->value(
			SOAP::Data->name("name" => $ctxadmin),
			SOAP::Data->name("password" => $ctxadmpw),
			SOAP::Data->name("display_name" => $displayname),
			SOAP::Data->name("sur_name" => $surname),
			SOAP::Data->name("given_name" => $gname),
			SOAP::Data->name("primaryEmail" => $email),
			SOAP::Data->name("email1" => $email),
			SOAP::Data->name("language" => $lang),
			SOAP::Data->name("timezone" => $timezone)
			)),
		  $creds
		 );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();

# read data and dump to console
$result = $soclt->getData($ctx_cpro,$creds);
@data = $result->paramsall;
print Dumper(@data);

# cleanup
$result = $soclt->delete($ctx_gw4u,$creds);
$result = $soclt->delete($ctx_cpro,$creds);
