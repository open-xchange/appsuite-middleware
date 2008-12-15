<?php
require_once 'PHPUnit/Framework.php';
require_once "../ox-soap.php";
require_once "tools.php";

class ContextTests extends PHPUnit_Framework_TestCase {

	public function verifyCreatedContexts($expected, $server_response) {
		$this->assertEquals($expected->maxQuota, $server_response->maxQuota);
		$this->assertEquals($expected->name, $server_response->name);
		$this->assertEquals($expected->id, $server_response->id);
	}

	/**
	 * Create a new Context in the OX System via SOAP
	 * and then check if it was created!
	 * 
	 * It checks the following context fields:
	 * 
	 * - ID
	 * - MAXQUOTA
	 * - NAME
	 */
	public function testCreate() {

		$random_id = generateContextId();

		$user = new User();
		$name = "soap_test_admin_" . $random_id;
		$user->name = $name;
		$user->display_name = "OX Soap Admin User " . $random_id;
		$user->given_name = "Soap Given Name" . $random_id;
		$user->sur_name = "Soap Surname" . $random_id;
		$user->password = "secret";
		$user->email1 = $name . "@context" . $random_id . ".org";
		$user->primaryEmail = $name . "@context" . $random_id . ".org";

		$ctx = new Context();
		$ctx->id = $random_id;
		$ctx->maxQuota = 1;
		$ctx->name = "soap_test_context" . $random_id;

		// TODO: FIX THE HASHSET OPTIONS IN SOAP
		/*
		$ctx->loginMappings = array (
			"loginmappings" => "mapping_1_" . $random_id,
			"loginmappings" => "mapping_2_" . $random_id
		);
		*/

		$create_context_result = getContextClient("localhost")->create($ctx, $user, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

		if (!is_soap_fault($create_context_result)) {
			// If no error occured, load the context via listcontext and compare
			$list_contexts_result = getContextClient("localhost")->list("*", getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

			$found_context = false;
			if (is_array($list_contexts_result)) {
				foreach ($list_contexts_result['return'] as $val_obj) {
					// check if our context is created					
					if ($val_obj->id == $random_id) {
						$this->verifyCreatedContexts($ctx, $val_obj);
						$found_context = true;
					}
				}
				if (!$found_context) {
					// test must fail, because we did not found our created context
					$this->assertFalse(true, "Context was not found after creation!");
				}
			} else {
				// check if our context is created				
				$this->verifyCreatedContexts($ctx, $create_context_result);
			}
		}
	}

	/**
	 * Create a new Context in the OX System via SOAP
	 * and then check if it was deleted!
	 *	 
	 */
	public function testDelete() {

		$random_id = generateContextId();

		$user = new User();
		$name = "soap_test_admin_" . $random_id;
		$user->name = $name;
		$user->display_name = "OX Soap Admin User " . $random_id;
		$user->given_name = "Soap Given Name" . $random_id;
		$user->sur_name = "Soap Surname" . $random_id;
		$user->password = "secret";
		$user->email1 = $name . "@context" . $random_id . ".org";
		$user->primaryEmail = $name . "@context" . $random_id . ".org";

		$ctx = new Context();
		$ctx->id = $random_id;
		$ctx->maxQuota = 1;
		$ctx->name = "soap_test_context" . $random_id;

		$create_context_result = getContextClient("localhost")->create($ctx, $user, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

		if (!is_soap_fault($create_context_result)) {
			// If no error occured, load the context via listcontext and compare
			$list_contexts_result = getContextClient("localhost")->list("*", getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

			$found_context = false;
			if (is_array($list_contexts_result)) {
				foreach ($list_contexts_result['return'] as $val_obj) {
					// check if our context is created					
					if ($val_obj->id == $random_id) {
						$this->verifyCreatedContexts($ctx, $val_obj);
						$found_context = true;
					}
				}
				if (!$found_context) {
					// test must fail, because we did not found our created context
					$this->assertFalse(true, "Context was not found after creation!");
				}
			} else {
				// check if our context is created				
				$this->verifyCreatedContexts($ctx, $create_context_result);
			}
		}

		// now delete the context from system
		$delete_result = getContextClient("localhost")->delete($ctx, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

		// now list all contexts and our deleted context should not be in the list
		$list_contexts_result = getContextClient("localhost")->list("*", getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));
		$found_context = false;
		if (is_array($list_contexts_result)) {
			foreach ($list_contexts_result['return'] as $val_obj) {
				// check if our context is created					
				if ($val_obj->id == $random_id) {
					$found_context = true;
				}
			}
		} else {
			// only 1 contexts was returned from search	
			$this->assertNotEquals($list_contexts_result->id,$random_id);						
		}
		// found context in list?
		$this->assertFalse($found_context,"Context deletion failed");

	}

	
}
?>