<?php
	error_reporting(0);

	if (get_magic_quotes_gpc()) {
	    function stripslashes_deep($value) {
		$value = is_array($value) ?
			    array_map('stripslashes_deep', $value) :
			    stripslashes($value);
	
		return $value;
	    }
	
	    $_POST = array_map('stripslashes_deep', $_POST);
	    $_GET = array_map('stripslashes_deep', $_GET);
	    $_COOKIE = array_map('stripslashes_deep', $_COOKIE);
	    $_REQUEST = array_map('stripslashes_deep', $_REQUEST);
	}
	
	$authenticationstring = "rhadsIsAgTicOpyodNainPacloykAuWyribZydkarbEncherc4";

	$connection_information = getIP();

	$connect = mysql_connect("localhost","oxreport","secret");
	$database = mysql_select_db("oxreport");

	if ($connect && $database) {
		$clientauthenticationstring = null;
		$license_keys = null;
		$client_information = null;
	
		foreach( $_POST as $key => $value ) {
			if ($key == "clientauthenticationstring") {
				$clientauthenticationstring = trim($value);
			} else if ($key == "license_keys") {
				$license_keys = trim($value);
			} else if ($key == "client_information") {
				$client_information = trim($value);
			}
		}

		if ($clientauthenticationstring != null
			&& strlen($clientauthenticationstring) > 0
			&& $license_keys != null
			&& strlen($license_keys) > 0
			&& $client_information != null
			&& strlen($client_information) > 0) {
			
			if ($authenticationstring == $clientauthenticationstring) {
				$search_existing_report_id = mysql_query(
					sprintf("SELECT id from reports where license_keys = '%s'",
						mysql_real_escape_string($license_keys))
				);
	
				if (mysql_num_rows($search_existing_report_id) > 0) {
					if (!mysql_query(sprintf("UPDATE reports set ".
						"connection_information = '%s', ".
						"last_syncdate = NOW(), ".
						"client_information = '%s' ".
						" WHERE license_keys = '%s'",
						mysql_real_escape_string($connection_information),
						mysql_real_escape_string($client_information),
						mysql_real_escape_string($license_keys)
					))) {
						echo 'could not update report';
					} else {
						echo 'report successfully updated';
					}
				} else {
					if (!mysql_query(sprintf("INSERT into reports set ".
						"license_keys = '%s', ".
						"connection_information = '%s', ".
						"last_syncdate = NOW(), ".
						"client_information = '%s'",
						mysql_real_escape_string($license_keys),
						mysql_real_escape_string($connection_information),
						mysql_real_escape_string($client_information)
					))) {
						echo 'could not create report';
					} else {
						echo 'report successfully created';
					}
				}
			} else {
				echo 'wrong client authentication string';
			}
		} else {
			$missing_parameters = '';
			if ($clientauthenticationstring == null || strlen($clientauthenticationstring) <= 0) {
				$missing_parameters = 'clientauthenticationstring';
			}
			if ($license_keys == null || strlen($license_keys) <= 0) {
				if (strlen($missing_parameters) > 0) {
					$missing_parameters .= ', ';
				}
				$missing_parameters .= 'license_keys';
			}
			if ($client_information == null || strlen($client_information) <= 0) {
				if (strlen($missing_parameters) > 0) {
					$missing_parameters .= ', ';
				}
				$missing_parameters .= 'client_information';
			}
			echo 'missing parameters: '.$missing_parameters;
		}
	} else {
		echo 'database error';
	}

	mysql_close($connect);

	function getIP() {
		$ip;
		if (getenv("HTTP_CLIENT_IP"))
			$ip = getenv("HTTP_CLIENT_IP");
		else if(getenv("HTTP_X_FORWARDED_FOR"))
			$ip = getenv("HTTP_X_FORWARDED_FOR");
		else if(getenv("REMOTE_ADDR"))
			$ip = getenv("REMOTE_ADDR");
		else
			$ip = "UNKNOWN";
		return $ip;
	}
	
?>