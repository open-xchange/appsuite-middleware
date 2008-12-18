<?php
function generateContextId() {
	srand(microtime() * 1000000);
	$random_id = rand(1, 99999);
	return rand(1, 99999);
}

function getContextAdminPassword() {
	return "secret";
}

function getContextAdminUsername() {
	return "oxadmin";
}



function getFullUserObject($name, $random_id) {
	$user = new User();	
	$user->name = $name;
	$user->display_name = $name." Soap User " . $random_id;
	$user->given_name = $name." Soap Given Name" . $random_id;
	$user->sur_name = $name." Soap Surname" . $random_id;
	$user->password = "secret";
	$user->email1 = $name . "@context" . $random_id . ".org";
	$user->primaryEmail = $name . "@context" . $random_id . ".org";
	//$user->aliases = $name . "@context" . $random_id . ".org";
	$user->anniversary = "1900-01-01T00:00:00.00Z";	
	$user->birthday = "1900-01-01T00:00:00.00Z";
	$user->assistant_name = $name." assistant name";
	$user->branches = $name."_branches";
	$user->business_category = $name."_business_category";
	$user->categories = $name."_categories";
	$user->cellular_telephone1 = $name."_cellular_telephone1";
	$user->cellular_telephone2 = $name."_cellular_telephone2";
	$user->city_business = $name."_city_business";
	$user->city_home = $name."_city_home";
	$user->city_other = $name."_city_other";
	$user->commercial_register = $name."_commercial_register";
	$user->company = $name."_company";
	$user->country_business = $name."_country_business";
	$user->country_home = $name."_country_home";
	$user->country_other = $name."_country_other";
	$user->defaultSenderAddress = $name . "@context" . $random_id . ".org";
	
	$group = new Group();
	$group->name = "users";
	$group->id = 1;
	$user->default_group = $group;
	
	$user->department = $name."_department";
	$user->email2 = $name . "_email2@context" . $random_id . ".org";
	$user->email3 = $name . "_email3@context" . $random_id . ".org";
	$user->employeeType = $name."_employeeType";
	$user->extensions = $name."_extensions";
	$user->fax_business = $name."_fax_business";
	$user->fax_home = $name."_fax_home";
	$user->fax_other = $name."_fax_other";
	$user->gUI_Spam_filter_capabilities_enabled = false;
	$user->imapLogin = $name."_imapLogin";
	$user->imapPort = 993;
	$user->imapSchema = "imaps";
	$user->imapServer = $name."_imapserver";
	$user->info = $name."_info";
	$user->instant_messenger1 = $name."_instant_messenger1";
	$user->instant_messenger2 = $name."_instant_messenger2";
	$user->language = "de_DE";
	$user->mail_folder_confirmed_ham_name = $name."_mail_folder_confirmed_ham_name";
	$user->mail_folder_confirmed_spam_name = $name."_mail_folder_confirmed_spam_name";
	$user->mail_folder_drafts_name = $name."_mail_folder_drafts_name";
	$user->mail_folder_sent_name = $name."_mail_folder_sent_name";
	$user->mail_folder_spam_name = $name."_mail_folder_spam_name";
	$user->mail_folder_trash_name = $name."_mail_folder_trash_name";
	$user->mailenabled = true;
	$user->manager_name = $name."_manager_name";
	$user->marital_status = $name."_marital_status";
	$user->middle_name = $name."_middle_name";
	$user->nickname = $name."_nickname";
	$user->note = $name."_note";
	$user->number_of_children = $name."_number_of_children";
	$user->number_of_employee = 1337;
	$user->position = $name."_position";
	$user->postal_code_business = $name."_postal_code_business";
	$user->postal_code_home = $name."_postal_code_home";
	$user->postal_code_other = $name."_postal_code_other";
	$user->profession = $name."_profession";
	$user->room_number = $name."_room_number";
	$user->sales_volume = $name."_sales_volume";
	$user->smtpPort = 25;
	$user->smtpSchema = "smtp";
	$user->smtpServer = $name."_smtpServer";
	$user->spam_filter_enabled = true;
	$user->spouse_name = $name."_spouse_name";
	$user->state_business = $name."_state_business";
	$user->state_home = $name."_state_home";
	$user->state_other = $name."_state_other";
	$user->street_business = $name."_street_business";
	$user->street_home = $name."_street_home";
	$user->street_other = $name."_street_other";
	$user->suffix = $name."_suffix";
	$user->tax_id = $name."_tax_id";
	$user->telephone_assistant = $name."_telephone_assistant";
	$user->telephone_business1 = $name."_telephone_business1";
	$user->telephone_business2 = $name."_telephone_business2";
	$user->telephone_callback = $name."_telephone_callback";
	$user->telephone_car = $name."_telephone_car";
	$user->telephone_company = $name."_telephone_company";
	$user->telephone_home1 = $name."_telephone_home1";
	$user->telephone_home2 = $name."_telephone_home2";
	$user->telephone_ip = $name."_telephone_ip";
	$user->telephone_isdn = $name."_telephone_isdn";
	$user->telephone_other = $name."_telephone_other";
	$user->telephone_pager = $name."_telephone_pager";
	$user->telephone_primary = $name."_telephone_primary";
	$user->telephone_radio = $name."_telephone_radio";
	$user->telephone_telex = $name."_telephone_telex";
	$user->telephone_ttytdd = $name."_telephone_ttytdd";
	$user->timezone = "Europe/Berlin";
	$user->title = $name."_title";
	$user->url = $name."_url";
	$user->userfield01 = $name."_userfield01";
	$user->userfield02 = $name."_userfield02";
	$user->userfield03 = $name."_userfield03";
	$user->userfield04 = $name."_userfield04";
	$user->userfield05 = $name."_userfield05";
	$user->userfield06 = $name."_userfield06";
	$user->userfield07 = $name."_userfield07";
	$user->userfield08 = $name."_userfield08";
	$user->userfield09 = $name."_userfield09";
	$user->userfield10 = $name."_userfield10";
	$user->userfield11 = $name."_userfield11";
	$user->userfield12 = $name."_userfield12";
	$user->userfield13 = $name."_userfield13";
	$user->userfield14 = $name."_userfield14";
	$user->userfield15 = $name."_userfield15";
	$user->userfield16 = $name."_userfield16";
	$user->userfield17 = $name."_userfield17";
	$user->userfield18 = $name."_userfield18";
	$user->userfield19 = $name."_userfield19";
	$user->userfield20 = $name."_userfield20";
	
	return $user;
}
?>
