<?php


/*
 * Helper class
 *
 */

$SOAPHOST = "localhost";

$OXMASTER_ADMIN = "oxadminmaster";
$OXMASTER_ADMIN_PASS = "admin_master_password";

$CONTEXT_ID = 2;
$CONTEXT_ADMIN = "oxadmin";
$CONTEXT_ADMIN_PASS = "admin_password";

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
    var $loginMappings;
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
    var $fax_business;
    var $fax_home;
    var $fax_other;
    var $given_name;
    var $guiPreferencesForSoap;
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
    var $passwordMech;
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
    var $gui_spam_filter_enabled;
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
        "location" => "http://" . $host . "/webservices/OXContextService?wsdl",
        "style" => SOAP_RPC,
        "uri" => "http://soap.admin.openexchange.com",
        "use" => SOAP_ENCODED
    ));

    return $client;
}

function getUtilClient($host) {

    $client = new SoapClient(NULL, array (
        "location" => "http://" . $host . "/webservices/OXUtilService?wsdl",
        "style" => SOAP_RPC,
        "uri" => "http://soap.admin.openexchange.com",
        "use" => SOAP_ENCODED
    ));

    return $client;
}

function getGroupClient($host) {

    $client = new SoapClient(NULL, array (
        "location" => "http://" . $host . "/webservices/OXGroupService?wsdl",
        "style" => SOAP_RPC,
        "uri" => "http://soap.admin.openexchange.com",
        "use" => SOAP_ENCODED
    ));

    return $client;
}

function getResourceClient($host) {

    $client = new SoapClient(NULL, array (
        "location" => "http://" . $host . "/webservices/OXResourceService?wsdl",
        "style" => SOAP_RPC,
        "uri" => "http://soap.admin.openexchange.com",
        "use" => SOAP_ENCODED
    ));

    return $client;
}

function getUserClient($host) {

    $client = new SoapClient(NULL, array (
        "location" => "http://" . $host . "/webservices/OXUserService?wsdl",
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
    return $ctx;
}

function getUserObject($user_id){
    $usr = new User();
    $usr->id = $user_id;
    return $usr;
}


// some error handling functions
function handleSoapFault($SoapFault) {
    printf("faultcode: "       . $SoapFault->faultcode . "\n");
    printf("faultstring: " . $SoapFault->faultstring . "\n");
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

function getFullGroupObject($name, $random_id) {
    $group = new Group();
    $group->name = $name."_".$random_id."_soap_test_group";
    $group->displayname = strtoupper($group->name);
    return $group;
}

function getFullResourceObject($name, $random_id) {
    $res = new Resource();
    $res->name = $name."_".$random_id."_soap_test_resource";
    $res->displayname = strtoupper($res->name);
    $res->email = $res->name. "@context" . $random_id . ".org";
    return $res;
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
    $user->anniversary = "2007-02-02T00:00:00.00Z";
    $user->birthday = "2007-02-02T00:00:00.00Z";
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
    $user->fax_business = $name."_fax_business";
    $user->fax_home = $name."_fax_home";
    $user->fax_other = $name."_fax_other";

    $user->imapLogin = $name."_imapLogin";
    $user->imapServer = "imaps://".$name."Imapserver:993";

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

    $user->smtpServer = "smtps://".$name."SmtpServer:583";

    $user->gui_spam_filter_enabled = true;
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

/*
 * This function modfifies all user attributes and appends/prepends the given $changed_suffix
 *
 * If changed_suffix is null then "changed" is used!
 */
function modifyUserData($user,$changed_suffix){
    if($changed_suffix==null){
        $changed_suffix = "changed";
    }
    $user->name = $user->name.$changed_suffix;

    $user->display_name = $user->display_name.$changed_suffix;
    $user->given_name = $user->given_name.$changed_suffix;
    $user->sur_name = $user->sur_name.$changed_suffix;
    $user->password = $user->password.$changed_suffix;
    if (!is_array($user->aliases)) {
        $user->aliases = array();
    }
    array_push($user->aliases, $user->primaryEmail);
    $key = 'aliases';
    for ($i = 0; $i < count($user->aliases); $i++) {
        $user->$key = $changed_suffix.$user->aliases[$i];
        $key .= ' ';
    }
    $user->email1 = $changed_suffix.$user->email1;
    $user->primaryEmail = $changed_suffix.$user->primaryEmail;
    $user->anniversary = "1981-02-02T00:00:00.00Z";
    $user->birthday = "1981-02-02T00:00:00.00Z";
    $user->assistant_name = $changed_suffix.$user->assistant_name;
    $user->branches = $changed_suffix.$user->branches;
    $user->business_category = $changed_suffix.$user->business_category;
    $user->categories = $changed_suffix.$user->categories;
    $user->cellular_telephone1 = $changed_suffix.$user->cellular_telephone1;
    $user->cellular_telephone2 = $changed_suffix.$user->cellular_telephone2;
    $user->city_business = $changed_suffix.$user->city_business;
    $user->city_home = $changed_suffix.$user->city_home;
    $user->city_other = $changed_suffix.$user->city_other;
    $user->commercial_register = $changed_suffix.$user->commercial_register;
    $user->company = $user->company.$changed_suffix;
    $user->country_business = $user->country_business.$changed_suffix;
    $user->country_home = $user->country_home.$changed_suffix;
    $user->country_other = $user->country_other.$changed_suffix;
    $user->defaultSenderAddress = $changed_suffix.$user->defaultSenderAddress;

    $user->department = $user->department.$changed_suffix;
    $user->email2 = $changed_suffix.$user->email2;
    $user->email3 = $changed_suffix.$user->email3;
    $user->employeeType = $user->employeeType.$changed_suffix;
    $user->fax_business = $user->fax_business.$changed_suffix;
    $user->fax_home = $user->fax_home.$changed_suffix;
    $user->fax_other = $user->fax_other.$changed_suffix;
    $user->gui_spam_filter_enabled = "true";

    $user->imapLogin = $user->imapLogin.$changed_suffix;
    $user->imapServer = "imaps://".$user->imapServer.$changed_suffix.":993";

    $user->info = $user->info.$changed_suffix;
    $user->instant_messenger1 = $user->instant_messenger1.$changed_suffix;
    $user->instant_messenger2 = $user->instant_messenger2.$changed_suffix;
    $user->language = "en_US";
    $user->mail_folder_confirmed_ham_name = $user->mail_folder_confirmed_ham_name.$changed_suffix;
    $user->mail_folder_confirmed_spam_name = $user->mail_folder_confirmed_spam_name.$changed_suffix;
    $user->mail_folder_drafts_name = $user->mail_folder_drafts_name.$changed_suffix;
    $user->mail_folder_sent_name = $user->mail_folder_sent_name.$changed_suffix;
    $user->mail_folder_spam_name = $user->mail_folder_spam_name.$changed_suffix;
    $user->mail_folder_trash_name = $user->mail_folder_trash_name.$changed_suffix;
    $user->mailenabled = false;
    $user->manager_name = $user->manager_name.$changed_suffix;
    $user->marital_status = $user->marital_status.$changed_suffix;
    $user->middle_name = $user->middle_name.$changed_suffix;
    $user->nickname = $user->nickname.$changed_suffix;
    $user->note = $user->note.$changed_suffix;
    $user->number_of_children = $user->number_of_children.$changed_suffix;
    $user->number_of_employee = 7331;
    $user->position = $user->position.$changed_suffix;
    $user->postal_code_business = $user->postal_code_business.$changed_suffix;
    $user->postal_code_home = $user->postal_code_home.$changed_suffix;
    $user->postal_code_other = $user->postal_code_other.$changed_suffix;
    $user->profession = $user->profession.$changed_suffix;
    $user->room_number = $user->room_number.$changed_suffix;
    $user->sales_volume = $user->sales_volume.$changed_suffix;

    $user->smtpServer = "smtps://".$user->smtpServer.$changed_suffix.":583";

    $user->spouse_name = $user->spouse_name.$changed_suffix;
    $user->state_business = $user->state_business.$changed_suffix;
    $user->state_home = $user->state_home.$changed_suffix;
    $user->state_other = $user->state_other.$changed_suffix;
    $user->street_business = $user->street_business.$changed_suffix;
    $user->street_home = $user->street_home.$changed_suffix;
    $user->street_other = $user->street_other.$changed_suffix;
    $user->suffix = $user->suffix.$changed_suffix;
    $user->tax_id = $user->tax_id.$changed_suffix;
    $user->telephone_assistant = $user->telephone_assistant.$changed_suffix;
    $user->telephone_business1 = $user->telephone_business1.$changed_suffix;
    $user->telephone_business2 = $user->telephone_business2.$changed_suffix;
    $user->telephone_callback = $user->telephone_callback.$changed_suffix;
    $user->telephone_car = $user->telephone_car.$changed_suffix;
    $user->telephone_company = $user->telephone_company.$changed_suffix;
    $user->telephone_home1 = $user->telephone_home1.$changed_suffix;
    $user->telephone_home2 = $user->telephone_home2.$changed_suffix;
    $user->telephone_ip = $user->telephone_ip.$changed_suffix;
    $user->telephone_isdn = $user->telephone_isdn.$changed_suffix;
    $user->telephone_other = $user->telephone_other.$changed_suffix;
    $user->telephone_pager = $user->telephone_pager.$changed_suffix;
    $user->telephone_primary = $user->telephone_primary.$changed_suffix;
    $user->telephone_radio = $user->telephone_radio.$changed_suffix;
    $user->telephone_telex = $user->telephone_telex.$changed_suffix;
    $user->telephone_ttytdd = $user->telephone_ttytdd.$changed_suffix;
    $user->timezone = "Europe/Amsterdam";
    $user->title = $user->title.$changed_suffix;
    $user->url = $user->url.$changed_suffix;
    $user->userfield01 = $user->userfield01.$changed_suffix;
    $user->userfield02 = $user->userfield02.$changed_suffix;
    $user->userfield03 = $user->userfield03.$changed_suffix;
    $user->userfield04 = $user->userfield04.$changed_suffix;
    $user->userfield05 = $user->userfield05.$changed_suffix;
    $user->userfield06 = $user->userfield06.$changed_suffix;
    $user->userfield07 = $user->userfield07.$changed_suffix;
    $user->userfield08 = $user->userfield08.$changed_suffix;
    $user->userfield09 = $user->userfield09.$changed_suffix;
    $user->userfield10 = $user->userfield10.$changed_suffix;
    $user->userfield11 = $user->userfield11.$changed_suffix;
    $user->userfield12 = $user->userfield12.$changed_suffix;
    $user->userfield13 = $user->userfield13.$changed_suffix;
    $user->userfield14 = $user->userfield14.$changed_suffix;
    $user->userfield15 = $user->userfield15.$changed_suffix;
    $user->userfield16 = $user->userfield16.$changed_suffix;
    $user->userfield17 = $user->userfield17.$changed_suffix;
    $user->userfield18 = $user->userfield18.$changed_suffix;
    $user->userfield19 = $user->userfield19.$changed_suffix;
    $user->userfield20 = $user->userfield20.$changed_suffix;

    return $user;
}

?>
