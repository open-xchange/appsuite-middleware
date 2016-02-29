#! /usr/bin/perl -w

use strict;
use SOAP::Lite;

# basics
my $baseURL     = "http://localhost/webservices/";
my $nameSpace   = "http://soap.admin.openexchange.com";

my $adminmaster = "oxadminmaster";
my $masterpw    = "secret";

my $ctxadmin    = "oxadmin";
my $ctxadmpw    = "secret";

# object holding the credentials of the OX Admin Master account
my $masterCreds = SOAP::Data->type("Credentials")->value(
		                    \SOAP::Data->value(
				      SOAP::Data->name("login" => $adminmaster),
				      SOAP::Data->name("password" => $masterpw)
				      )
				    );

# object holding the credentials of the OX Context Admin account
my $ctxCreds    = SOAP::Data->type("Credentials")->value(
		                    \SOAP::Data->value(
				      SOAP::Data->name("login" => $ctxadmin),
				      SOAP::Data->name("password" => $ctxadmpw)
				      )
				    );

##
## creating a SOAP client for the OXUtil service
##
my $soclt = SOAP::Lite->ns( $nameSpace )->proxy( $baseURL."OXUtilService" );

##
## Registering an OX Server
##

my $serverName = "myoxserver";

my $result = $soclt->registerServer(
			    SOAP::Data->value("Server")->value(
                             \SOAP::Data->value(
			      SOAP::Data->name("name" => $serverName)
			      )
                             ),
			    $masterCreds
			    );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();

##
## Registering a Database
##
my $dbname   = "mydb";
my $ismaster = "true";
my $maxctx   = 42042;
my $weight   = 100;
my $dbuser   = "openexchange";
my $dbpasswd = "secret";

$result = $soclt->registerDatabase(
    	      SOAP::Data->value("Database")->value(\SOAP::Data->value(
    	           SOAP::Data->name("name" => $dbname),
    	           SOAP::Data->name("master" => $ismaster),
    	           SOAP::Data->name("maxUnits" => $maxctx),
    	           SOAP::Data->name("clusterWeight" => $weight),
    	           SOAP::Data->name("login" => $dbuser),
    	           SOAP::Data->name("password" => $dbpasswd)
    	           )),
    	      $masterCreds
    	      );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();

##
## Registering a filestore
##
my $storepath = "/tmp/filestore";
my $storeurl  = "file://$storepath";
my $size      = 123456;
$maxctx    = 420;

# create filestore if it does not exist
if( ! -d $storepath ) {
  mkdir($storepath) || die "unable to mkdir $storepath";
}

$result = $soclt->registerFilestore(
    	      SOAP::Data->value("Filestore")->value(\SOAP::Data->value(
    	           SOAP::Data->name("url" => $storeurl),
    	           SOAP::Data->name("size" => $size),
    	           SOAP::Data->name("maxContexts" => $maxctx)
    	           )),
    	      $masterCreds
    	      );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();

##
## creating a context
##


##
## creating a SOAP client for the OXContext service
##
$soclt = SOAP::Lite->ns( $nameSpace )->proxy( $baseURL."OXContextService" );

# mandatory user data for the context admin
my $displayname = "Context Admin";
my $surname     = "Admin";
my $gname       = "Context";
my $email       = "oxadmin\@example.com";
my $lang        = "de_DE";
my $timezone    = "Europe/Berlin";
# context quota
my $ctxid       = 424242;
my $ctxname     = "mycontext";
my $ctxquota    = 500;

# create a context SOAP object
my $context = SOAP::Data->type("Context")->value(
			     \SOAP::Data->value(
                              SOAP::Data->name("id" => $ctxid),
			      SOAP::Data->name("name" => $ctxname),
			      SOAP::Data->name("maxQuota" => $ctxquota)));

$result = $soclt->create($context,
		  SOAP::Data->value("User")->value(\SOAP::Data->value(
			SOAP::Data->name("name" => $ctxadmin),
			SOAP::Data->name("password" => $ctxadmpw),
			SOAP::Data->name("display_name" => $displayname),
			SOAP::Data->name("sur_name" => $surname),
			SOAP::Data->name("given_name" => $gname),
			SOAP::Data->name("primaryEmail" => $email),
			SOAP::Data->name("language" => $lang),
			SOAP::Data->name("timezone" => $timezone)
			)),
		  $masterCreds
		 );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();

##
## creating a SOAP clients for OXUser,OXGroup and OXResource services
##
my $souserclt     = SOAP::Lite->ns( $nameSpace )->proxy( $baseURL."OXUserService" );
my $sogroupclt    = SOAP::Lite->ns( $nameSpace )->proxy( $baseURL."OXGroupService" );
my $soresourceclt = SOAP::Lite->ns( $nameSpace )->proxy( $baseURL."OXResourceService" );

##
## creating a user
##

# mandatory user data
my $uname    = "john";
my $password = "secret";
$displayname = "John Doe";
$surname     = "Doe";
$gname       = "John";
$email       = "john\@example.com";
$lang        = "de_DE";
$timezone    = "Europe/Berlin";
my $birthday = "1942-12-24T00:00:00.00Z";

$result =
    $souserclt->create($context,
		  SOAP::Data->value("User")->value(\SOAP::Data->value(
			SOAP::Data->name("name" => $uname),
			SOAP::Data->name("password" => $password),
			SOAP::Data->name("display_name" => $displayname),
			SOAP::Data->name("sur_name" => $surname),
			SOAP::Data->name("given_name" => $gname),
			SOAP::Data->name("primaryEmail" => $email),
			SOAP::Data->name("email1" => $email),
			SOAP::Data->name("language" => $lang),
			SOAP::Data->name("timezone" => $timezone),
			SOAP::Data->name("birthday" => $birthday)
   	          )),
		  $ctxCreds
		 );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();

##
## creating a group
##

$gname       = "testgroup";
$displayname = "a testgroup";
$email       = "testgroup\@example.com";

# already add some members (can also be done later with a change call)
# we already know/assume, that context admin has id 2 and John Doe has
# id 3.
my @members;
push(@members, SOAP::Data->name( members => 2 ));
push(@members, SOAP::Data->name( members => 3 ));

$result =
      $sogroupclt->create($context,
    	      SOAP::Data->value("Group")->value(\SOAP::Data->value(
    	           SOAP::Data->name("name" => $gname),
    	           SOAP::Data->name("displayname" => $displayname),
    	           SOAP::Data->name("email" => $email),
    	           SOAP::Data->name("members" => @members )
    	           )),
    	      $ctxCreds
    	     );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();

##
## creating a resource
##
my $rname    = "testresource";
$displayname = "a testresource";
$email       = "testresource\@example.com";

$result =
      $soresourceclt->create($context,
    	      SOAP::Data->value("Resource")->value(\SOAP::Data->value(
    	           SOAP::Data->name("name" => $rname),
    	           SOAP::Data->name("displayname" => $displayname),
    	           SOAP::Data->name("email" => $email)
   	           )),
    	      $ctxCreds
    	     );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();

##
## changing userdata
##

# new display name and password
$uname       = "john";
$displayname = "Dr. John Doe";
$password    = "verysecret";

$result =
    $souserclt->change($context,
		  SOAP::Data->value("User")->value(\SOAP::Data->value(
			SOAP::Data->name("name" => $uname),
			SOAP::Data->name("password" => $password),
			SOAP::Data->name("display_name" => $displayname)
   	          )),
		  $ctxCreds
		 );

die "Error: ".$result->faultstring()."\n $@" if $result->fault();
