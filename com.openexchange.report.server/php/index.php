<?php
        error_reporting(0);

        $connection_information = getIP();

        $connect = mysql_connect("localhost","oxreport","secret");
        $database = mysql_select_db("oxreport");

        if ($connect && $database) {
                $license_keys = null;
                $client_information = null;
        
                foreach( $_POST as $key => $value ) {
                        if ($key == "license_keys") {
                                $license_keys = trim($value);
                        } else if ($key == "client_information") {
                                $client_information = trim($value);
                        }
                }

                if ($license_keys != null
                        && strlen($license_keys) > 0
                        && $client_information != null
                        && strlen($client_information) > 0) {
                        
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
                                        echo 'activation.open-xchange.com said: could not update report';
                                } else {
                                        echo 'activation.open-xchange.com said: report successfully updated';
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
                                        echo 'activation.open-xchange.com said: could not create report';
                                } else {
                                        echo 'activation.open-xchange.com said: report successfully created';
                                }
                        }
                } else {
                        $missing_parameters = '';
                        if ($license_keys == null || strlen($license_keys) <= 0) {
                                $missing_parameters = 'license_keys';
                        }
                        if ($client_information == null || strlen($client_information) <= 0) {
                                if (strlen($missing_parameters) > 0) {
                                        $missing_parameters .= ', ';
                                }
                                $missing_parameters .= 'client_information';
                        }
                        echo 'activation.open-xchange.com said: missing parameters: '.$missing_parameters;
                }
        } else {
                echo 'activation.open-xchange.com said: database error';
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
        