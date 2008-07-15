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


// some printing methods
function printServer($serverObject) {
	echo "ID:" . $serverObject->id . " NAME " . $serverObject->name . "\n";
}


?>
