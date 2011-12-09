<?php

require_once "../ox-soap.php";

/*
 * This file deletes all contexts which have not the context id 1!
 * Usefull to clean up test system
 */
$list_contexts_result = getContextClient($SOAPHOST)->list("*", getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

if (is_array($list_contexts_result)) {
	foreach ($list_contexts_result['return'] as $val_obj) {
		// check if our context is created					
		if ($val_obj->id != 1){
			echo "Deleting Context ".$val_obj->id."\n";
			$ctx = new Context();
			$ctx->id = $val_obj->id;
			$delete_result = getContextClient($SOAPHOST)->delete($ctx, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));
		}
	}	
}
?>
