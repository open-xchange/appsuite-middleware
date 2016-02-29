<?php
require_once 'PHPUnit/Framework.php';
require_once "../ox-soap.php";

class UtilTests extends PHPUnit_Framework_TestCase {

    /*
     * Creates a new server and then tries to load this created server and verifies it.
     */
    function createAndVerifyServer(){
        global $SOAPHOST;
        global $OXMASTER_ADMIN;
        global $OXMASTER_ADMIN_PASS;

        srand(microtime() * 1000000);
        $random_id = rand(1, 99999);

        $server = new Server();
        $server->name = "server_soap_test_".$random_id;

        $result_register = getUtilClient($SOAPHOST)->registerServer($server, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));
        $loaded_server ;
        if (is_soap_fault($result_register)) {
            $this->fail("Server could not be registered");
        }else{

            // now try to verify server via getting the server by listing all servers
            $result = getUtilClient($SOAPHOST)->listServer("*", getCredentialsObject($OXMASTER_ADMIN,$OXMASTER_ADMIN_PASS));
            $found_server = false;
            if (!is_soap_fault($result)) {
                if (is_array($result)) {
                    foreach ($result['return'] as $val_obj) {
                        if($val_obj->id == $result_register->id){
                            $this->verifyServer($server,$val_obj);
                            $found_server = true;
                            $loaded_server = $val_obj;
                            break;
                        }
                    }
                } else {
                    if($result->id == $result_register->id){
                        $this->verifyServer($server,$result);
                        $loaded_server = $result;
                        break;
                    }else{
                        fail("could not verify server");
                    }
                }
            }else{
                fail("Error while listing all registered servers");
            }
        }

        $this->assertTrue($found_server);


        return $loaded_server;

    }


    /*
     * Unregisters given server and then tries to load this server.
     * If server list does not contain deleted server, then all is OK.
     */
    function unregisterServerAndVerify($server){

        global $SOAPHOST;
        global $OXMASTER_ADMIN;
        global $OXMASTER_ADMIN_PASS;


        // unregister via soap
        $result = getUtilClient($SOAPHOST)->unregisterServer($server, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

        // now load all servers and try to find it, we shouldnt find it :()
        $list_result = getUtilClient($SOAPHOST)->listServer("*", getCredentialsObject($OXMASTER_ADMIN,$OXMASTER_ADMIN_PASS));
        if (!is_soap_fault($list_result)) {

            // found multiple servers
            if (is_array($result)) {
                foreach ($result['return'] as $val_obj) {
                    if($val_obj->id == $server->id){
                        // Ohoh, server was found, thats bad, unregister process seems to be buggy
                        fail();
                    }
                }
            } else if ($result) {

                // we just got 1 server as reply from  admindaemon
                if($result->id == $server->id){
                    // ohoh, server is still registered, admindaemon seems to be bugge in unregisterprocess
                    fail();
                }
            } // if no server at all can be found, all is fine
        } else {
            $this->fail("Error while listing all registered servers to verify unregistered server");
        }


    }



    /*
     * registers given filestore and then tries to load it via list function.
     * if we find the registered store in the list, then all is ok
     */
    function registerFilestoreAndVerify($filestore){

        global $SOAPHOST;
        global $OXMASTER_ADMIN;
        global $OXMASTER_ADMIN_PASS;


        // register via soap
        $result = getUtilClient($SOAPHOST)->registerFilestore($filestore, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

        if (is_soap_fault($result)) {
            fail("Error while registering filestore");
        }else{

            // give store the returned id to verify later also the id
            $filestore->id = $result->id;

            $foundstore = false;
            // now list all filestores and try to find it
            $list_result = getUtilClient($SOAPHOST)->listFilestore("*", getCredentialsObject($OXMASTER_ADMIN,$OXMASTER_ADMIN_PASS));
            if (!is_soap_fault($list_result)){
                if (is_array($list_result)) {
                    // loop through all stores and try to verify
                    foreach ($list_result['return'] as $val_obj) {
                        if($val_obj->id == $filestore->id){
                            $this->verifyFilestore($filestore, $val_obj);
                            $foundstore = true;
                            break;
                        }
                    }
                }else{
                    // just got one store as reply
                    $this->verifyFilestore($filestore, $list_result);
                    $foundstore = true;
                }
            }else{
                fail("Error listing filestores");
            }

            $this->assertTrue($foundstore);

            return $filestore;
        }

    }

     /*
     * unregisters given filestore and then tries to load it via list function.
     * if we find the unregistered store in the list, then test must fail
     */
    function unregisterFilestoreAndVerify($filestore){

        global $SOAPHOST;
        global $OXMASTER_ADMIN;
        global $OXMASTER_ADMIN_PASS;

        $result = getUtilClient($SOAPHOST)->unregisterFilestore($filestore, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));

        if (is_soap_fault($result)) {
            fail("Error while unregistering filestore");
        }else{
            // filestore was unregistered, now list all filestores
            $list_result = getUtilClient($SOAPHOST)->listFilestore("*", getCredentialsObject($OXMASTER_ADMIN,$OXMASTER_ADMIN_PASS));
            if (!is_soap_fault($list_result)){
                if (is_array($list_result)) {
                    // loop through all stores and do not find it, if found, test fails
                    foreach ($list_result['return'] as $val_obj) {
                        if($val_obj->id == $filestore->id){
                            // found store thats bad
                            fail();
                        }
                    }
                }else{
                    if($list_result->id == $filestore->id){
                        fail();
                    }
                }
            }else{
                fail("Error listing filestores");
            }
        }
    }

    function changeFilestoreAndVerify($filestore){

        global $SOAPHOST;
        global $OXMASTER_ADMIN;
        global $OXMASTER_ADMIN_PASS;

        $result = getUtilClient($SOAPHOST)->changeFilestore($filestore, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));
        $found_store = false;
        if (is_soap_fault($result)) {
            fail("Error while change filestore");
        }else{
            // now load filestore again and return
            // filestore was change, now list all filestores
            $list_result = getUtilClient($SOAPHOST)->listFilestore("*", getCredentialsObject($OXMASTER_ADMIN,$OXMASTER_ADMIN_PASS));
            if (!is_soap_fault($list_result)){
                if (is_array($list_result)) {
                    // loop through all stores and do not find it, if found, test fails
                    foreach ($list_result['return'] as $val_obj) {
                        if($val_obj->id == $filestore->id){
                            $this->verifyFilestore($filestore, $val_obj);
                            $found_store = true;
                            break;
                        }
                    }
                }else{
                    if($list_result->id == $filestore->id){
                        $this->verifyFilestore($filestore, $list_result);
                    }
                }
            }else{
                fail("Error listing filestores");
            }
        }

        $this->assertTrue($found_store);

    }

    function registerDatabaseAndVerify($database){

        global $SOAPHOST;
        global $OXMASTER_ADMIN;
        global $OXMASTER_ADMIN_PASS;

        $result = getUtilClient($SOAPHOST)->registerDatabase($database, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));
        $found_db = false;
        if (is_soap_fault($result)) {
            fail("Error while registering database");
        }else{
            // set database id
            $database->id = $result->id;
            // now load database again and verify
            $list_result = getUtilClient($SOAPHOST)->listDatabase("*", getCredentialsObject($OXMASTER_ADMIN,$OXMASTER_ADMIN_PASS));
            if (!is_soap_fault($list_result)){
                if (is_array($list_result)) {
                    // loop through all dbs 
                    foreach ($list_result['return'] as $val_obj) {
                        if($val_obj->name == $database->name){
                            // found database
                            $this->verifyDatabase($database, $val_obj);
                            $found_db = true;
                            break;
                        }
                    }
                }else{
                    if($list_result->name == $database->name){
                        $this->verifyDatabase($filestore, $list_result);
                    }
                }
            }else{
                fail("Error listing databases");
            }
        }

        return $database;



    }


    function unregisterDatabaseAndVerify($database){

        global $SOAPHOST;
        global $OXMASTER_ADMIN;
        global $OXMASTER_ADMIN_PASS;

        $result = getUtilClient($SOAPHOST)->unregisterDatabase($database, getCredentialsObject($OXMASTER_ADMIN, $OXMASTER_ADMIN_PASS));
        
        if (is_soap_fault($result)) {
            fail("Error while unregistering database");
        }else{
            
            // now load database again and verify
            $list_result = getUtilClient($SOAPHOST)->listDatabase("*", getCredentialsObject($OXMASTER_ADMIN,$OXMASTER_ADMIN_PASS));
            if (!is_soap_fault($list_result)){
                if (is_array($list_result)) {
                    // loop through all dbs
                    foreach ($list_result['return'] as $val_obj) {
                        if($val_obj->id == $database->id){                            
                            fail("database was not unregistered");
                        }
                    }
                }else{
                    if($list_result->id == $database->id){
                       fail("database was not unregistered!");
                    }
                }
            }else{
                fail("Error listing databases");
            }
        }

    }





    public function testRegisterDatabase() {
        srand(microtime()*1000000);
        $random_id = rand(1, 99999);

        $db = new Database();
        $db->name = "name_by_soap" . $random_id;
        $db->password = "password_by_soap." . $random_id;
        $db->login = "login_by_soap" . $random_id;
        $db->url = "jdbc:mysql://soaphost" . $random_id . "/?useUnicode=true&characterEncoding=UTF-8&autoReconnect=false&useUnicode=true&useServerPrepStmts=false&useTimezone=true&serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000";
        $db->masterId = 0;
        $db->master = true;
        $db->clusterWeight = 100;
        $db->maxUnits = 1000;
        $db->poolHardLimit = 11;
        $db->poolInitial = 2;
        $db->poolMax = 5;
        $db->driver = "com.mysql.jdbc.Driver";

        $db = $this->registerDatabaseAndVerify($db);      
        
    }

    public function testUnregisterDatabase() {
        srand(microtime()*1000000);
        $random_id = rand(1, 99999);

        $db = new Database();
        $db->name = "name_by_soap" . $random_id;
        $db->password = "password_by_soap." . $random_id;
        $db->login = "login_by_soap" . $random_id;
        $db->url = "jdbc:mysql://soaphost" . $random_id . "/?useUnicode=true&characterEncoding=UTF-8&autoReconnect=false&useUnicode=true&useServerPrepStmts=false&useTimezone=true&serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000";
        $db->masterId = 0;
        $db->master = true;
        $db->clusterWeight = 100;
        $db->maxUnits = 1000;
        $db->poolHardLimit = 11;
        $db->poolInitial = 2;
        $db->poolMax = 5;
        $db->driver = "com.mysql.jdbc.Driver";

        // register database and verify creation
        $db = $this->registerDatabaseAndVerify($db);

        // unregister and verify
        $this->unregisterDatabaseAndVerify($db);
    }  

    /**
     * Registerfilestore, change it and load the changed store again and
     * verify the loaded store against the changed
     */
    public function testChangeFilestore() {
        srand(microtime()*1000000);
        $random_id = rand(1, 99999);

        $store_dir = "/tmp/ox_tests_mount_".$random_id;
        // create the dir temporary and delete a the end
        mkdir($store_dir, 0700);

        $store = new Filestore();
        $store->url = "file://".$store_dir;
        $store->size = 10000;
        $store->maxContexts = 1337;

        // create filestore and verify
        $store = $this->registerFilestoreAndVerify($store);

        // now change filestore data and verify
        $store->url = $store->url."_changed";
        $store->size = 1000;
        $store->maxContexts = 7331;

        $changed_store = $this->changeFilestoreAndVerify($store);


    }


    /**
     * Create a new filestore in the OX System via SOAP
     * and then check if it was created correctly!
     * This also checks the listsfilestore function.
     *
     * It checks the following data:
     *
     * - name
     * - url
     * - size
     * - maxcontexts
     *
     *
     */
    public function testRegisterFilestore() {

        srand(microtime()*1000000);
        $random_id = rand(1, 99999);

        $store_dir = "/tmp/ox_tests_mount_".$random_id;
        // create the dir temporary and delete a the end
        mkdir($store_dir, 0700);

        $store = new Filestore();
        $store->url = "file://".$store_dir;
        $store->size = 10000;
        $store->maxContexts = 1337;

        $this->registerFilestoreAndVerify($store);
    }



    /**
     * Unregisters a new filestore in the OX System via SOAP
     * and then unregister it!
     * Then it lists all filestores and try to find the unregistered one.
     * this must fail. else the unregister function is buggy.
     *
     * This also checks the listsfilestore function.
     *
     *
     */
    public function testUnregisterFilestore() {

        srand(microtime()*1000000);
        $random_id = rand(1, 99999);

        $store_dir = "/tmp/ox_tests_mount_".$random_id;
        // create the dir temporary and delete a the end
        mkdir($store_dir, 0700);

        $store = new Filestore();
        $store->url = "file://".$store_dir;
        $store->size = 10000;
        $store->maxContexts = 1337;

        // create filestore and verify
        $store = $this->registerFilestoreAndVerify($store);

        // now unregister failstore
        $this->unregisterFilestoreAndVerify($store);


    }




     /**
     * Create a new Server in the OX System via SOAP
     * and then check if it was created correctly!
     * This also checks the listserver function.
     *
     * It checks the following data:
     *
     * - name
     *
     */
    public function testRegisterServer() {

        srand(microtime() * 1000000);
        $random_id = rand(1, 99999);

        $server = new Server();
        $server->name = "server_soap_test_".$random_id;

        $this->createAndVerifyServer();
    }

    /**
     *
     *
     * Create a new Server in the OX System via SOAP
     * and then check if it was created correctly!
     * Then this server will be unregistered.
     * After that we will list all servers to verfiy
     * that the server was deleted.
     *
     *
     */
    public function testUnregisterServer() {

        srand(microtime() * 1000000);
        $random_id = rand(1, 99999);

        $server = new Server();
        $server->name = "server_soap_test_".$random_id;

        // register and verfiy server
        $server = $this->createAndVerifyServer();

        // unregisterserver and verify
        $this->unregisterServerAndVerify($server);

    }

    public function verifyServer($expected, $server_response) {
        $this->assertEquals($expected->name, $server_response->name);
    }

    public function verifyFilestore($expected, $server_response) {
        $this->assertEquals($expected->id, $server_response->id);
        $this->assertEquals($expected->url, $server_response->url);
        $this->assertEquals($expected->size, $server_response->size);
        $this->assertEquals($expected->maxContexts, $server_response->maxContexts);
    }
    
    public function verifyDatabase($expected, $server_response) {
        $this->assertEquals($expected->id, $server_response->id);
        $this->assertEquals($expected->password, $server_response->password);
        $this->assertEquals($expected->login, $server_response->login);
        $this->assertEquals($expected->name, $server_response->name);
        $this->assertEquals($expected->url, $server_response->url);
        $this->assertEquals($expected->masterId, $server_response->masterId);
        $this->assertEquals($expected->master, $server_response->master);
        $this->assertEquals($expected->clusterWeight, $server_response->clusterWeight);
        $this->assertEquals($expected->maxUnits, $server_response->maxUnits);
        $this->assertEquals($expected->poolHardLimit, $server_response->poolHardLimit);
        $this->assertEquals($expected->poolInitial, $server_response->poolInitial);
        $this->assertEquals($expected->poolMax, $server_response->poolMax);
        $this->assertEquals($expected->driver, $server_response->driver);
    }


}
?>
