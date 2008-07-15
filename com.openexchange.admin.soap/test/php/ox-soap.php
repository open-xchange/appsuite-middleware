<?php


/*
 * Helper class
 * 
 */

class Credentials {
	var $login;
	var $password;
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

function getCredentialsObject($user, $password) {
	$credObj = new Credentials();
	$credObj->login = $user;
	$credObj->password = $password;
	return $credObj;
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
?>
