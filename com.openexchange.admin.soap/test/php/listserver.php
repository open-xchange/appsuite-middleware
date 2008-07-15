<?php

include("ox-soap.php");

$client = getUtilClient("localhost");

$credObj = new Credentials();
$credObj->login = "oxadminmaster";
$credObj->password = "secret";

try {
	$result = $client->listServer("*", $credObj);
	if (!is_soap_fault($result)) {
		if (is_array($result)) {
			foreach ($result['return'] as $val_obj) {
				printServer($val_obj);
			}
		} else {
			printServer($result);
		}
	}
} catch (SoapFault $fault) {
	printf($fault->faultstring);
} catch (Exception $e) {
	echo $e->getMessage()."\n";
}
?>
