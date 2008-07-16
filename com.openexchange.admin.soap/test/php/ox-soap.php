<?php


/*
 * Helper class
 * 
 */

#$SOAPHOST = "10.10.10.154";
$SOAPHOST = "localhost";

$OXMASTER_ADMIN = "oxadminmaster";
$OXMASTER_ADMIN_PASS = "secret";

$CONTEXT_ID = 1;
$CONTEXT_ADMIN = "oxadmin";
$CONTEXT_ADMIN_PASS = "secret";

class Credentials {
	var $login;
	var $password;
}

class Context {
	var $id;
	var $name;
	var $idset;
}

class User {
	var $id;
	var $name;
	var $idset;
}

class Server {
	var $id;
	var $name;
}

class Database {
	var $clusterWeight;
	var $currentUnits;
	var $driver;
	var $id;
	var $login;
	var $master;
	var $masterId;
	var $maxUnits;
	var $name;
	var $password;
	var $poolHardLimit;
	var $poolInitial;
	var $poolMax;
	var $read_id;
	var $scheme;
	var $url;
}

class Filestore {
	var $currentContexts;
	var $id;
	var $maxContexts;
	var $reserved;
	var $url;
	var $used;
	var $size;		
}

function getContextClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXContextService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getUtilClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXUtilService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getGroupClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXGroupService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getResourceClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXResourceService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getUserClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXUserService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getCredentialsObject($user, $password) {
	$credObj = new Credentials();
	$credObj->login = $user;
	$credObj->password = $password;
	return $credObj;
}

function getContextObject($context_id){
	$ctx = new Context();
	$ctx->id = $context_id;
	$ctx->idset = true;
	return $ctx;
}

function getUserObject($user_id){
	$usr = new User();
	$usr->id = $user_id;
	$usr->idset = true;
	return $usr;
}


// some error handling functions
function handleSoapFault($SoapFault) {
	printf($SoapFault->faultstring . "\n");
}

function handleExcepion($SoapException) {
	echo $SoapException->getMessage() . "\n";
}

// some printing methods
function printServer($serverObject) {
	print_r($serverObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}

function printDatabase($dbObject) {
	print_r($dbObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}

function printFilestore($filestoreObject) {
	print_r($filestoreObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
function printContext($contextObject) {
	print_r($contextObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
function printGroup($groupObject) {
	print_r($groupObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
function printResource($resourceObject) {
	print_r($resourceObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
function printUser($userObject) {
	print_r($userObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
?>
