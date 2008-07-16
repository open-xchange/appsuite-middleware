<?php


/*
 * Helper class
 * 
 */

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
?>
