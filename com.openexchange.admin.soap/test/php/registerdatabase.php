<?php

include ("ox-soap.php");

try {

	$db = new Database();
	$db->name = "name_by_soap";
	$db->password = "password_by_soap";
	$db->login = "login_by_soap";
	$db->url = "jdbc:mysql://soaphost/?useUnicode=true&characterEncoding=UTF-8&" +
                "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" +
                "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000";
    $db->masterId = 0;
    $db->master = true;
    $db->clusterWeight = 100;
    $db->maxUnits = 1000;
    $db->poolHardLimit = 11;
    $db->poolInitial = 2;
    $db->poolMax = 5;
	
	
	$result = getUtilClient($SOAPHOST)->registerDatabase($db, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

	if (!is_soap_fault($result)) {		
		printDatabase($result->id);		
	}

} catch (SoapFault $fault) {
	handleSoapFault($fault);
} catch (Exception $e) {
	handleExcepion($e);
}
?>
