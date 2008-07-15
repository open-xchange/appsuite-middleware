<?php

include ("ox-soap.php");

try {

	$result = getUtilClient("localhost")->listFilestore("*", getCredentialsObject("oxadminmaster", "secret"));

	if (!is_soap_fault($result)) {
		if (is_array($result)) {
			foreach ($result['return'] as $val_obj) {
				printFilestore($val_obj);
			}
		} else {
			printFilestore($result);
		}
	}

} catch (SoapFault $fault) {
	handleSoapFault($fault);
} catch (Exception $e) {
	handleExcepion($e);
}
?>
