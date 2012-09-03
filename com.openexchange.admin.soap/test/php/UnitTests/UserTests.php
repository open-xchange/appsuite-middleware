<?php
require_once 'PHPUnit/Framework.php';
require_once "../ox-soap.php";

class UserTests extends PHPUnit_Framework_TestCase {
	
	/*
	 * Creates a new context identified by $ctx and admin user $admin_user
	 * and then creates a new user within this context which will be returned by
	 * this function for further use in other testcases. 
	 */
	function createAndVerifyUser($ctx,$admin_user){
		global $SOAPHOST;
		global $OXMASTER_ADMIN;
		global $OXMASTER_ADMIN_PASS;
		
		// create a new context 
		$create_context_result = getContextClient($SOAPHOST)->create($ctx, $admin_user, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

		// verify that the context is created
		if (!is_soap_fault($create_context_result)) {
			// If no error occured, load the context via listcontext and compare
			$list_contexts_result = getContextClient($SOAPHOST)->list("*", getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

			$found_context = false;
			if (is_array($list_contexts_result)) {
				foreach ($list_contexts_result['return'] as $val_obj) {
					// check if our context is created
					if ($val_obj->id == $ctx->id) {
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

		// now create a user within this context
		$new_user = getFullUserObject("soap-test-createuser", $ctx->id);
		$user_create_response = getUserClient($SOAPHOST)->create($ctx, $new_user, getCredentialsObject($admin_user->name, $admin_user->password));

		// now list all users and find the create one, if found, compare if all values were set correctly
		$user_list_response = getUserClient($SOAPHOST)->list($ctx, "*", getCredentialsObject($admin_user->name, $admin_user->password));

		// loop through users and for each user id response, query server for user details
		foreach ($user_list_response['return'] as $ret_user){
			$query_user = new User();
			$query_user->id = $ret_user->id;
			$user_get_response = getUserClient($SOAPHOST)->getData($ctx, $query_user, getCredentialsObject($admin_user->name, $admin_user->password));
			if($user_get_response->name == $new_user->name){
				// verfiy user data				
				$this->verifyUser($new_user,$user_get_response);
			}
			 
		}
		return $user_get_response;
	} 
	
	
	
	
	function changeAndVerifyUser($ctx,$admin_user,$user){
		
		global $SOAPHOST;
		global $OXMASTER_ADMIN;
		global $OXMASTER_ADMIN_PASS;		
		
		// now change user within this context		
		$user_create_response = getUserClient($SOAPHOST)->change($ctx, $user, getCredentialsObject($admin_user->name, $admin_user->password));

		// now list all users and find the changed one, if found, compare if all values were set correctly
		$user_list_response = getUserClient($SOAPHOST)->list($ctx, "*", getCredentialsObject($admin_user->name, $admin_user->password));

		// loop through users and for each user id response, query server for user details
		foreach ($user_list_response['return'] as $ret_user){
			$query_user = new User();
			$query_user->id = $ret_user->id;
			$user_get_response = getUserClient($SOAPHOST)->getData($ctx, $query_user, getCredentialsObject($admin_user->name, $admin_user->password));
			if($user_get_response->name == $user->name){
				// verfiy user data				
				$this->verifyUser($user,$user_get_response);
			}
			 
		}		
	} 
	
	/*
	 * This function deletes a user and checks if user was deleted via simple list request
	 */
	function deleteAndVerify($ctx,$admin_user,$user){
		
		global $SOAPHOST;
		global $OXMASTER_ADMIN;
		global $OXMASTER_ADMIN_PASS;		
		
		// now change user within this context		
		$user_create_response = getUserClient($SOAPHOST)->delete($ctx, $user, getCredentialsObject($admin_user->name, $admin_user->password));

		// now list all users and find the changed one, if found, compare if all values were set correctly
		$user_list_response = getUserClient($SOAPHOST)->list($ctx, "*", getCredentialsObject($admin_user->name, $admin_user->password));

		// loop through users and for each user id response, query server for user details
		$found_deleted_user = false;
		if (!is_soap_fault($user_list_response)) {
		    if (is_array($user_list_response)) {
        		foreach ($user_list_response['return'] as $ret_user){
        			$query_user = new User();
        			$query_user->id = $ret_user->id;
        			$user_get_response = getUserClient($SOAPHOST)->getData($ctx, $query_user, getCredentialsObject($admin_user->name, $admin_user->password));
        			if($user_get_response->name == $user->name){
        				$found_deleted_user = true;
        			}
        		}
        	} else {
        	    $query_user = new User();
                $query_user->id = $user_list_response->id;
                $user_get_response = getUserClient($SOAPHOST)->getData($ctx, $query_user, getCredentialsObject($admin_user->name, $admin_user->password));
                if($user_get_response->name == $user->name){
                    $found_deleted_user = true;
                }
        	}
    	}
		$this->assertFalse($found_deleted_user);	
	} 
	
	
	/**
	 * Create a new User in the OX System via SOAP
	 * and then check if it was created correctly!
	 * 
	 * It checks the following data:
	 * 
	 * -- All USER Fields EXCLUSIVE "aliases" or other "Hashset" based Values, cause of "PHP Soap" Bugs
	 * 
	 */
	public function testCreateUser() {
		

		$random_id = generateContextId();
		$name = "soap-test-admin-" . $random_id;
		$admin_user = getFullUserObject($name, $random_id);

		$ctx = new Context();
		$ctx->id = $random_id;
		$ctx->maxQuota = 1;
		$ctx->name = "soap-test-context" . $random_id;

		$this->createAndVerifyUser($ctx,$admin_user);		
	}
	
	/**
	 * Create a new User in the OX System via SOAP
	 * and then changes this user and after that it 
	 * checks if it was changed correctly!
	 * 
	 * It checks the following data:
	 * 
	 * -- All USER Fields EXCLUSIVE "aliases" or other "Hashset" based Values, cause of "PHP Soap" Bugs
	 * 
	 */
	public function testChangeUser(){
		$random_id = generateContextId();
		$name = "soap-test-admin-" . $random_id;
		$admin_user = getFullUserObject($name, $random_id);

		$ctx = new Context();
		$ctx->id = $random_id;
		$ctx->maxQuota = 1;
		$ctx->name = "soap-test-context" . $random_id;

		// create new context and new user
		$new_user = $this->createAndVerifyUser($ctx,$admin_user);
		
		// now change all values in the user object
		$new_user = modifyUserData($new_user,null);
		
		// change on server via UserService SOAP Method "change"
		$this->changeAndVerifyUser($ctx,$admin_user,$new_user);
	}
	/**
	 * Create a new User in the OX System via SOAP
	 * and then deletes the user and after that it 
	 * checks if it was deleted correctly!
	 * The check is done via "list" Method in
	 * the OXUserService.
	 *	 
	 */
	public function testDeleteUser(){
		$random_id = generateContextId();
		$name = "soap-test-admin-" . $random_id;
		$admin_user = getFullUserObject($name, $random_id);

		$ctx = new Context();
		$ctx->id = $random_id;
		$ctx->maxQuota = 1;
		$ctx->name = "soap-test-context" . $random_id;

		// create new context and new user
		$new_user = $this->createAndVerifyUser($ctx,$admin_user);
		
		// now delete user and check if it still exists
		$this->deleteAndVerify($ctx,$admin_user,$new_user);	
		
	}
	
	public function verifyCreatedContexts($expected, $server_response) {
		$this->assertEquals($expected->maxQuota, $server_response->maxQuota);
		$this->assertEquals($expected->name, $server_response->name);
		$this->assertEquals($expected->id, $server_response->id);
	}
	
	
	
	
	
	public function verifyUser($expected, $server_response) {
		$this->assertEquals($expected->name, $server_response->name);
		$this->assertEquals($expected->display_name, $server_response->display_name);
		$this->assertEquals($expected->given_name, $server_response->given_name);
		$this->assertEquals($expected->sur_name, $server_response->sur_name);
		$this->assertEquals($expected->email1, $server_response->email1);
		$this->assertEquals($expected->primaryEmail, $server_response->primaryEmail);
		
		// parse anniversary and check day and month year
		$ani_expected = (object) date_parse($expected->anniversary);
		$ani_server   = (object) date_parse($server_response->anniversary);
		$this->assertEquals($ani_expected->year, $ani_server->year, "anniversary year");
		$this->assertEquals($ani_expected->month, $ani_server->month, "anniversary month");
		$this->assertEquals($ani_expected->day, $ani_server->day, "anniversary day");
		
		// parse birthday and check day month year
		$birth_expected = (object) date_parse($expected->birthday);
		$birth_server   = (object) date_parse($server_response->birthday);
		$this->assertEquals($birth_expected->year, $birth_server->year, "birthday year");
		$this->assertEquals($birth_expected->month, $birth_server->month, "birthday month");
		$this->assertEquals($birth_expected->day, $birth_server->day, "birthday day");
		
		
		
		$this->assertEquals($expected->assistant_name, $server_response->assistant_name);
		$this->assertEquals($expected->branches, $server_response->branches);
		$this->assertEquals($expected->business_category, $server_response->business_category);
		$this->assertEquals($expected->categories, $server_response->categories);
		$this->assertEquals($expected->cellular_telephone1, $server_response->cellular_telephone1);
		$this->assertEquals($expected->cellular_telephone2, $server_response->cellular_telephone2);
		$this->assertEquals($expected->city_business, $server_response->city_business);
		$this->assertEquals($expected->city_home, $server_response->city_home);
		$this->assertEquals($expected->city_other, $server_response->city_other);
		$this->assertEquals($expected->commercial_register, $server_response->commercial_register);
		$this->assertEquals($expected->company, $server_response->company);
		$this->assertEquals($expected->country_business, $server_response->country_business);
		$this->assertEquals($expected->country_home, $server_response->country_home);
		$this->assertEquals($expected->country_other, $server_response->country_other);
		$this->assertEquals($expected->defaultSenderAddress, $server_response->defaultSenderAddress);
		$this->assertEquals($expected->department, $server_response->department);
		$this->assertEquals($expected->email2, $server_response->email2);
		$this->assertEquals($expected->email3, $server_response->email3);
		$this->assertEquals($expected->employeeType, $server_response->employeeType);		
		$this->assertEquals($expected->fax_business, $server_response->fax_business);
		$this->assertEquals($expected->fax_home, $server_response->fax_home);
		$this->assertEquals($expected->fax_other, $server_response->fax_other);
		$this->assertEquals($expected->gui_spam_filter_enabled, $server_response->gui_spam_filter_enabled);
		$this->assertEquals($expected->imapLogin, $server_response->imapLogin);
		
		// special case of asserting because ox sends all imap infos in the "imapserver" attribute
		// First parse, then assert
		$imap_uri = parse_url($expected->imapServer);
		$this->assertEquals($imap_uri["host"], $server_response->imapServer);
		$this->assertEquals($imap_uri["port"], $server_response->imapPort);
		$this->assertEquals($imap_uri["scheme"]."://", $server_response->imapSchema);
		
		$this->assertEquals($expected->info, $server_response->info);
		$this->assertEquals($expected->instant_messenger1, $server_response->instant_messenger1);
		$this->assertEquals($expected->instant_messenger2, $server_response->instant_messenger2);
		$this->assertEquals($expected->language, $server_response->language);
		$this->assertEquals($expected->mail_folder_confirmed_ham_name, $server_response->mail_folder_confirmed_ham_name);
		$this->assertEquals($expected->mail_folder_confirmed_spam_name, $server_response->mail_folder_confirmed_spam_name);
		$this->assertEquals($expected->mail_folder_drafts_name, $server_response->mail_folder_drafts_name);
		$this->assertEquals($expected->mail_folder_sent_name, $server_response->mail_folder_sent_name);
		$this->assertEquals($expected->mail_folder_spam_name, $server_response->mail_folder_spam_name);
		$this->assertEquals($expected->mail_folder_trash_name, $server_response->mail_folder_trash_name);
		$this->assertEquals($expected->mailenabled, $server_response->mailenabled);
		$this->assertEquals($expected->manager_name, $server_response->manager_name);
		$this->assertEquals($expected->marital_status, $server_response->marital_status);
		$this->assertEquals($expected->middle_name, $server_response->middle_name);
		$this->assertEquals($expected->nickname, $server_response->nickname);
		$this->assertEquals($expected->note, $server_response->note);
		$this->assertEquals($expected->number_of_children, $server_response->number_of_children);
		$this->assertEquals($expected->number_of_employee, $server_response->number_of_employee);
		$this->assertEquals($expected->position, $server_response->position);
		$this->assertEquals($expected->postal_code_business, $server_response->postal_code_business);
		$this->assertEquals($expected->postal_code_home, $server_response->postal_code_home);
		$this->assertEquals($expected->postal_code_other, $server_response->postal_code_other);
		$this->assertEquals($expected->profession, $server_response->profession);
		$this->assertEquals($expected->room_number, $server_response->room_number);
		$this->assertEquals($expected->sales_volume, $server_response->sales_volume);
		
		// special case of asserting because ox sends all smtp infos in the "smtpserver" attribute
		// First parse, then assert
		$smtp_uri = parse_url($expected->smtpServer);
		$this->assertEquals($smtp_uri["host"], $server_response->smtpServer);
		$this->assertEquals($smtp_uri["port"], $server_response->smtpPort);
		$this->assertEquals($smtp_uri["scheme"]."://", $server_response->smtpSchema);	

		$this->assertEquals($expected->spouse_name, $server_response->spouse_name);
		$this->assertEquals($expected->state_business, $server_response->state_business);
		$this->assertEquals($expected->state_home, $server_response->state_home);
		$this->assertEquals($expected->state_other, $server_response->state_other);
		$this->assertEquals($expected->street_business, $server_response->street_business);
		$this->assertEquals($expected->street_home, $server_response->street_home);
		$this->assertEquals($expected->street_other, $server_response->street_other);
		$this->assertEquals($expected->suffix, $server_response->suffix);
		$this->assertEquals($expected->tax_id, $server_response->tax_id);
		$this->assertEquals($expected->telephone_assistant, $server_response->telephone_assistant);
		$this->assertEquals($expected->telephone_business1, $server_response->telephone_business1);
		$this->assertEquals($expected->telephone_business2, $server_response->telephone_business2);
		$this->assertEquals($expected->telephone_callback, $server_response->telephone_callback);
		$this->assertEquals($expected->telephone_car, $server_response->telephone_car);
		$this->assertEquals($expected->telephone_company, $server_response->telephone_company);
		$this->assertEquals($expected->telephone_home1, $server_response->telephone_home1);
		$this->assertEquals($expected->telephone_home2, $server_response->telephone_home2);
		$this->assertEquals($expected->telephone_ip, $server_response->telephone_ip);
		$this->assertEquals($expected->telephone_isdn, $server_response->telephone_isdn);
		$this->assertEquals($expected->telephone_other, $server_response->telephone_other);
		$this->assertEquals($expected->telephone_pager, $server_response->telephone_pager);
		$this->assertEquals($expected->telephone_primary, $server_response->telephone_primary);
		$this->assertEquals($expected->telephone_radio, $server_response->telephone_radio);
		$this->assertEquals($expected->telephone_telex, $server_response->telephone_telex);
		$this->assertEquals($expected->telephone_ttytdd, $server_response->telephone_ttytdd);
		$this->assertEquals($expected->timezone, $server_response->timezone);
		$this->assertEquals($expected->title, $server_response->title);
		$this->assertEquals($expected->url, $server_response->url);
		$this->assertEquals($expected->userfield01, $server_response->userfield01);
		$this->assertEquals($expected->userfield02, $server_response->userfield02);
		$this->assertEquals($expected->userfield03, $server_response->userfield03);
		$this->assertEquals($expected->userfield04, $server_response->userfield04);
		$this->assertEquals($expected->userfield05, $server_response->userfield05);
		$this->assertEquals($expected->userfield06, $server_response->userfield06);
		$this->assertEquals($expected->userfield07, $server_response->userfield07);
		$this->assertEquals($expected->userfield08, $server_response->userfield08);
		$this->assertEquals($expected->userfield09, $server_response->userfield09);
		$this->assertEquals($expected->userfield10, $server_response->userfield10);
		$this->assertEquals($expected->userfield11, $server_response->userfield11);
		$this->assertEquals($expected->userfield12, $server_response->userfield12);
		$this->assertEquals($expected->userfield13, $server_response->userfield13);
		$this->assertEquals($expected->userfield14, $server_response->userfield14);
		$this->assertEquals($expected->userfield15, $server_response->userfield15);
		$this->assertEquals($expected->userfield16, $server_response->userfield16);
		$this->assertEquals($expected->userfield17, $server_response->userfield17);
		$this->assertEquals($expected->userfield18, $server_response->userfield18);
		$this->assertEquals($expected->userfield19, $server_response->userfield19);
		$this->assertEquals($expected->userfield20, $server_response->userfield20);
	}
	
	
}
?>
