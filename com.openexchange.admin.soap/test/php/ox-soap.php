#! /usr/bin/php
<?php


/*
 * Helper class
 * 
 */

#$SOAPHOST = "10.10.10.154";
$SOAPHOST = "localhost";

$OXMASTER_ADMIN = "oxadminmaster";
$OXMASTER_ADMIN_PASS = "secret";

$CONTEXT_ID = 1;
$CONTEXT_ADMIN = "oxadmin";
$CONTEXT_ADMIN_PASS = "secret";

class Credentials {
	var $login;
	var $password;
}

class Context {
	var $average_size;
	var $enabled;
	var $filestoreId;
	var $filestore_name;
	var $id;
	var $idAsString;
	var $loginMappings;
	var $maintenanceReason;
	var $maxQuota;
	var $name;
	var $readDatabase;
	var $usedQuota;
	var $writeDatabase;
}

class Server {
	var $id;
	var $name;
}

class Database {
	var $clusterWeight;
	var $currentUnits;
	var $driver;
	var $id;
	var $login;
	var $master;
	var $masterId;
	var $maxUnits;
	var $name;
	var $password;
	var $poolHardLimit;
	var $poolInitial;
	var $poolMax;
	var $read_id;
	var $scheme;
	var $url;
}

class Filestore {
	var $currentContexts;
	var $id;
	var $maxContexts;
	var $reserved;
	var $url;
	var $used;
	var $size;		
}

class User {
	var $aliases;
	var $anniversary;
	var $assistant_name;
	var $birthday;
	var $branches;
	var $business_category;
	var $categories;
	var $cellular_telephone1;
	var $cellular_telephone2;
	var $city_business;
	var $city_home;
	var $city_other;
	var $commercial_register;
	var $company;
	var $contextadmin = false;
	var $country_business;
	var $country_home;
	var $country_other;
	var $defaultSenderAddress;
	var $default_group;
	var $department;
	var $display_name;
	var $email1;
	var $email2;
	var $email3;
	var $employeeType;
	var $extensions;
	var $fax_business;
	var $fax_home;
	var $fax_other;
	var $gUI_Spam_filter_capabilities_enabled;
	var $given_name;
	var $guiPreference;
	var $id;
	var $imapLogin;
	var $imapPort;
	var $imapSchema;
	var $imapServer;
	var $info;
	var $instant_messenger1;
	var $instant_messenger2;
	var $language;
	var $mail_folder_confirmed_ham_name;
	var $mail_folder_confirmed_spam_name;
	var $mail_folder_drafts_name;
	var $mail_folder_sent_name;
	var $mail_folder_spam_name;
	var $mail_folder_trash_name;
	var $mailenabled;
	var $manager_name;
	var $marital_status;
	var $middle_name;
	var $name;
	var $nickname;
	var $note;
	var $number_of_children;
	var $number_of_employee;
	var $password;
	var $passwordMech2String;
	var $password_expired;
	var $position;
	var $postal_code_business;
	var $postal_code_home;
	var $postal_code_other;
	var $primaryEmail;
	var $profession;
	var $room_number;
	var $sales_volume;
	var $smtpPort;
	var $smtpSchema;
	var $smtpServer;
	var $spam_filter_enabled;
	var $spouse_name;
	var $state_business;
	var $state_home;
	var $state_other;
	var $street_business;
	var $street_home;
	var $street_other;
	var $suffix;
	var $sur_name;
	var $tax_id;
	var $telephone_assistant;
	var $telephone_business1;
	var $telephone_business2;
	var $telephone_callback;
	var $telephone_car;
	var $telephone_company;
	var $telephone_home1;
	var $telephone_home2;
	var $telephone_ip;
	var $telephone_isdn;
	var $telephone_other;
	var $telephone_pager;
	var $telephone_primary;
	var $telephone_radio;
	var $telephone_telex;
	var $telephone_ttytdd;
	var $timezone;
	var $title;
	var $url;
	var $userfield01;
	var $userfield02;
	var $userfield03;
	var $userfield04;
	var $userfield05;
	var $userfield06;
	var $userfield07;
	var $userfield08;
	var $userfield09;
	var $userfield10;
	var $userfield11;
	var $userfield12;
	var $userfield13;
	var $userfield14;
	var $userfield15;
	var $userfield16;
	var $userfield17;
	var $userfield18;
	var $userfield19;
	var $userfield20;
	

}

class UserModuleAccess{
	var $calendar;
	var $contacts;
	var $delegateTask;
	var $editGroup;
	var $editPassword;
	var $editPublicFolders;
	var $editResource;
	var $forum;
	var $ical;
	var $infostore;
	var $pinboardWrite;
	var $projects;
	var $readCreateSharedFolders;
	var $rssBookmarks;
	var $rssPortal;
	var $syncml;
	var $tasks;
	var $vcard;
	var $webdav;
	var $webdavXml;
	var $webmail;
	
}

class Group {
	var $name;
	var $displayname;
	var $id;
	var $members;
}

class Resource {
	var $available;
	var $description;
	var $displayname;
	var $email;
	var $id;
	var $name;
}

function getContextClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXContextService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getUtilClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXUtilService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getGroupClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXGroupService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getResourceClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXResourceService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getUserClient($host) {

	$client = new SoapClient(NULL, array (
		"location" => "http://" . $host . "/servlet/axis2/services/OXUserService?wsdl",
		"style" => SOAP_RPC,
		"uri" => "http://soap.admin.openexchange.com",
		"use" => SOAP_ENCODED
	));

	return $client;
}

function getCredentialsObject($user, $password) {
	$credObj = new Credentials();
	$credObj->login = $user;
	$credObj->password = $password;
	return $credObj;
}

function getContextObject($context_id){
	$ctx = new Context();
	$ctx->id = $context_id;
	$ctx->idset = true;
	return $ctx;
}

function getUserObject($user_id){
	$usr = new User();
	$usr->id = $user_id;
	$usr->idset = true;
	return $usr;
}


// some error handling functions
function handleSoapFault($SoapFault) {
	printf($SoapFault->faultstring . "\n");
}

function handleExcepion($SoapException) {
	echo $SoapException->getMessage() . "\n";
}

// some printing methods
function printServer($serverObject) {
	print_r($serverObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}

function printDatabase($dbObject) {
	print_r($dbObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}

function printFilestore($filestoreObject) {
	print_r($filestoreObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
function printContext($contextObject) {
	print_r($contextObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
function printGroup($groupObject) {
	print_r($groupObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
function printResource($resourceObject) {
	print_r($resourceObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
function printUser($userObject) {
	print_r($userObject);
	//echo "ID:" . $serverObject->id . " NAME:" . $serverObject->name . "\n";
}
?>
