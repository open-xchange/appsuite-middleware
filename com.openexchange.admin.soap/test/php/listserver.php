<?php

include("ox-soap.php");

try {
	
	$result = getUtilClient($SOAPHOST)->listServer("*", getCredentialsObject("oxadminmaster","secret"));
	
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
	handleSoapFault($fault);
} catch (Exception $e) {
	handleExcepion($e);
}
?>
