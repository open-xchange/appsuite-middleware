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
                            $this->verifyCreatedServer($server,$val_obj);
                            $found_server = true;
                            $loaded_server = $val_obj;
                            break;
                        }
                    }
                } else {
                    if($result->id == $result_register->id){
                        $this->verifyCreatedServer($server,$result);
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
            } else {

                // we just got 1 server as reply from  admindaemon
                if($result->id == $server->id){
                    // ohoh, server is still registered, admindaemon seems to be bugge in unregisterprocess
                    fail();
                }
            }
        }else{
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

        if (is_soap_fault($list_result)) {
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
                            $this->verifyCreatedFilestore($filestore, $val_obj);
                            $foundstore = true;
                            break;
                        }
                    }
                }else{
                    // just got one store as reply
                    $this->verifyCreatedFilestore($filestore, $list_result);
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

        if (is_soap_fault($list_result)) {
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

    

    public function testChangeFilestore() {
        // TODO
    }

    public function testRegisterDatabase() {
        // TODO
    }

    public function testUnregisterDatabase() {
        // TODO
    }

    public function testChangeDatabase() {
        // TODO
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
    public function atestRegisterServer() {

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
    public function atestUnregisterServer() {

        srand(microtime() * 1000000);
        $random_id = rand(1, 99999);

        $server = new Server();
        $server->name = "server_soap_test_".$random_id;

        // register and verfiy server
        $server = $this->createAndVerifyServer();

        // unregisterserver and verify
        $this->unregisterServerAndVerify($server);

    }

    public function verifyCreatedServer($expected, $server_response) {
        $this->assertEquals($expected->name, $server_response->name);
    }

    public function verifyCreatedFilestore($expected, $server_response) {
        $this->assertEquals($expected->id, $server_response->id);
        $this->assertEquals($expected->url, $server_response->url);
        $this->assertEquals($expected->size, $server_response->size);
        $this->assertEquals($expected->maxContexts, $server_response->maxContexts);
    }



}
?>