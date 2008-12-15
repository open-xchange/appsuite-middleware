<?php

function generateContextId(){
	srand(microtime() * 1000000);
	$random_id = rand(1, 99999);
	return rand(1, 99999);
}

function getContextAdminPassword(){
	return "secret";
}

function getContextAdminUsername(){
	return "oxadmin";
}


?>
