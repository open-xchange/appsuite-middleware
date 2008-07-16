<?php

include ("ox-soap.php");

try {

	$result = getUtilClient($SOAPHOST)->listDatabase("*", getCredentialsObject("oxadminmaster", "secret"));

	if (!is_soap_fault($result)) {
		if (is_array($result)) {
			foreach ($result['return'] as $val_obj) {
				printDatabase($val_obj);
			}
		} else {
			printDatabase($result);
		}
	}

} catch (SoapFault $fault) {
	handleSoapFault($fault);
} catch (Exception $e) {
	handleExcepion($e);
}
?>
