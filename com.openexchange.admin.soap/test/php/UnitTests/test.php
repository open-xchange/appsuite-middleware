<?php
/*
 * 
 * 
 * FILE IS ONLY FOR TESTING PHP FUCKEL STUFF!!!!!!
 * 
 * 
 * 
 * 
 * 
 */

/** SOAP_Client */
require 'SOAP/Client.php';
require_once "../ox-soap.php";

$username = "admin";
$password = "secret";
$host = "localhost";
$trace = false;

// create context

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

/**
 * Create Contexts
 */
$response = createContext($ctx,$user);


/**
 * List Contexts
 */
$context_list_response = listContext("*");

if (is_array($context_list_response)) {
	foreach ($context_list_response as $context) {
		printf($context->id . "\n");
	}
}


function listContext($search_pattern) {
	global $host;
	global $trace;
	$soapclient = new SOAP_Client('http://' . $host . ':1337/servlet/axis2/services/OXContextService?wsdl');
	$options = array ();
	$options['namespace'] = 'http://soap.admin.openexchange.com';
	$options['trace'] = $trace;
	$retval = $soapclient->call('list', array (
		'search_pattern' => $search_pattern,
		'auth' => getCredentials()
	), $options);
	return checkResponse($retval);
}

function createContext($context,$user) {
	global $host;
	global $trace;
	$soapclient = new SOAP_Client('http://' . $host . ':1337/servlet/axis2/services/OXContextService?wsdl');
	$options = array ();
	$options['namespace'] = 'http://soap.admin.openexchange.com';
	$options['trace'] = $trace;
	$retval = $soapclient->call('create', array (
		'ctx' => $context,
		'admin_user' => $user,
		'auth' => getCredentials()
	), $options);
	return checkResponse($retval);
}

function checkResponse($soap_resonse) {
	if (PEAR :: isError($soap_resonse)) {
		echo 'Error: ' . $soap_resonse->getMessage() . "\n";
	} else {
		return $soap_resonse;
	}
}

function getCredentials() {
	global $username;
	global $password;
	$cred = new Credentials();
	$cred->login = $username;
	$cred->password = $password;
	return $cred;
}

function generateContextId() {
	srand(microtime() * 1000000);
	$random_id = rand(1, 99999);
	return rand(1, 99999);
}
?>
