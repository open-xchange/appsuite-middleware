<?php
$client = new SoapClient(NULL, array (
	"location" => "http://localhost/servlet/axis2/services/OXUtilService?wsdl",
	"style" => SOAP_RPC,
	"uri" => "http://soap.admin.openexchange.com",
	"use" => SOAP_ENCODED
));

class Credentials {
	var $login;
	var $password;
}

function printServer2Console($serverObject) {
	echo "ID:" . $serverObject->id . " NAME " . $serverObject->name . "\n";
}

$credObj = new Credentials();
$credObj->login = "oxadminmaster";
$credObj->password = "secret";

try {
	$result = $client->listServer("*", $credObj);
	if (!is_soap_fault($result)) {
		if (is_array($result)) {
			foreach ($result['return'] as $val_obj) {
				printServer2Console($val_obj);
			}
		} else {
			printServer2Console($result);
		}
	}
} catch (SoapFault $fault) {
	printf($fault->faultstring);
} catch (Exception $e) {
	echo $e->getMessage()."\n";
}
?>
