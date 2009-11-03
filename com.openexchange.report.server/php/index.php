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
				$reporter_id = null;
				$reporter_id_result = mysql_query(
					sprintf("SELECT id from report_list where license_keys = '%s'",
						mysql_real_escape_string($license_keys))
				);
				if ($row = mysql_fetch_row($reporter_id_result)) {
					$reporter_id = $row[0];
				}
	
				if ($reporter_id == null) {
					if (mysql_query(sprintf("INSERT into report_list set ".
						"license_keys = '%s', ".
						"last_syncdate = NOW(), ".
						"current_revision = '0'",
						mysql_real_escape_string($license_keys)
					))) {
						$reporter_id_result = mysql_query(
							sprintf("SELECT id from report_list where license_keys = '%s'",
								mysql_real_escape_string($license_keys))
						);
						if ($row = mysql_fetch_row($reporter_id_result)) {
							$reporter_id = $row[0];
						} else {
							echo 'could not find yet created reporter id';
						}
					} else {
						echo 'could not create reporter id';
					}
				}
	
				if ($reporter_id != null) {
					$next_revision_number = null;
					$next_revision_number_result = mysql_query(
						sprintf("SELECT current_revision+1 from report_list where license_keys = '%s'",
							mysql_real_escape_string($license_keys))
					);
					if ($row = mysql_fetch_row($next_revision_number_result)) {
  						$next_revision_number = $row[0];
  					}
  					
  					if ($next_revision_number != null) {
						if (!mysql_query(sprintf("INSERT into report_revisions set ".
							"report_revision = '%s', ".
							"reporter_id = '%s', ".
							"connection_information = '%s', ".
							"syncdate = NOW(), ".
							"report = '%s'",
							$next_revision_number,
							$reporter_id,
							mysql_real_escape_string($connection_information),
							mysql_real_escape_string($client_information)
						))) {
							echo 'could not create report revision';
						} else {
							if (!mysql_query(sprintf("UPDATE report_list set ".
								"last_syncdate = NOW(), ".
								"current_revision = '%s' ".
								" WHERE id = '%s'",
								$next_revision_number,
								$reporter_id
							))) {
								echo 'could not update reporter table';
							} else {
								echo 'report successfully created';
							}
						}
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