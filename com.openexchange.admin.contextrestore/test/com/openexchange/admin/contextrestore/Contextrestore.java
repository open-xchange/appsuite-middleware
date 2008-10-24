package com.openexchange.admin.contextrestore;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.openexchange.admin.tools.ShellExecutor;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;

public class Contextrestore {

    // TODO: These values should been taken from a configfile
    /**
     * Change the username of the admin of the created temporary context here
     */
    private static final String CONTEXT_ADMIN_USERNAME = "oxadmin";

    /**
     * Change the password of the admin of the created temporary context here
     */
    private static final String CONTEXT_ADMIN_PW = "secret";

    /**
     * Change the username of your mysql installation here
     */
    private final static String username = "openexchange";
    
    /**
     * Change the password of your mysql installation here
     */
    private final static String password = "secret";
    
    /**
     * Change the password for your oxadminmaster account here
     */
    private final static String oxmasterpassword = "secret";
    
    /**
     * Change the number for the newly created temporary context here
     */
    private final static String newcontextid = "8888";
    
    private final static String userdumpfile = "/tmp/mysqldumpuser.txt";

    private final static String userdumpfile2 = "/tmp/mysqldumpuser2.txt";

    private final static String configdbdumpfile = "/tmp/mysqldumpconfigdb.txt";

    private final static String configdbdumpfile2 = "/tmp/mysqldumpconfigdb2.txt";
    
    @Before
    public void setUp() throws Exception {
        final ShellExecutor shellExecutor = new ShellExecutor();
        
        // First create a new context
        ArrayOutput retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/createcontext", "-A", "oxadminmaster", "-P", oxmasterpassword, "-c", newcontextid, 
                "-u", CONTEXT_ADMIN_USERNAME, "-d", CONTEXT_ADMIN_USERNAME, "-g", CONTEXT_ADMIN_USERNAME, "-s", CONTEXT_ADMIN_USERNAME, "-p", CONTEXT_ADMIN_PW, "-e", "xyz67@bla.de", "-q", "100"});
        assertTrue("Context creation failed due to the following reason: " + retval.errOutput, 0 == retval.exitstatus);
        System.out.println("Temporary Context created");
    }

    @After
    public void tearDown() throws Exception {
        // Delete the files...
        File file = new File(userdumpfile);
        file.delete();
        System.out.println("1. temp file deleted");
        file = new File(userdumpfile2);
        file.delete();
        System.out.println("2. temp file deleted");
        file = new File(configdbdumpfile);
        file.delete();
        System.out.println("3. temp file deleted");
        file = new File(configdbdumpfile2);
        file.delete();
        System.out.println("4. temp file deleted");
        
        final ShellExecutor shellExecutor = new ShellExecutor();

        // Finally delete the created context...
        ArrayOutput retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/deletecontext", "-A", "oxadminmaster", "-P", oxmasterpassword, "-c",
                newcontextid});
        assertTrue("Temp Context deletion failed due to the following reason: " + retval.errOutput, 0 == retval.exitstatus);
        System.out.println("Temporary context deleted");
    }
    
    /**
     * This test does a restore on an already existing context. To verify that the context is restored right, data is created in between
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testRestore() throws IOException, InterruptedException {
        final ShellExecutor shellExecutor = new ShellExecutor();
        
        // First we dump the database...
        ArrayOutput retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema choeger_7 mysql --single-transaction > " + userdumpfile});
        assertTrue("Dump creation for user databases failed due to the following reason: " + retval.errOutput, 0 == retval.exitstatus);
        System.out.println("User dump created");

        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema configdb mysql --single-transaction > " + configdbdumpfile});
        assertTrue("Dump creation for configdb failed", 0 == retval.exitstatus);
        System.out.println("Config dump created");
        
        // ... then we add some new data to the context ...
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/createuser", "-A", CONTEXT_ADMIN_USERNAME , "-P", CONTEXT_ADMIN_PW, "-c", 
                newcontextid, "-u", "test1", "-d", "test1", "-g", "test1", "-s", "test1", "-p", CONTEXT_ADMIN_PW, "-e", "xyz27@bla.de"});
        assertTrue("User creation failed", 0 == retval.exitstatus);
        System.out.println("New user created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/creategroup", "-A", CONTEXT_ADMIN_USERNAME, "-P", CONTEXT_ADMIN_PW, "-n", "test", 
                "-d", "test", "-c", newcontextid});
        assertTrue("Group creation failed", 0 == retval.exitstatus);
        System.out.println("New group created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/createresource", "-A", CONTEXT_ADMIN_USERNAME, "-P", CONTEXT_ADMIN_PW, "-c",
                newcontextid, "-n", "test", "-d", "test", "-e", "ressource@test.de"});
        assertTrue("Resource creation failed", 0 == retval.exitstatus);
        System.out.println("New resource created");
        
        // ... afterwards the restore takes places ...
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/restorecontext", "-A", "oxadminmaster" , "-P", oxmasterpassword, "-c",
                newcontextid, "-f", userdumpfile + ',' + configdbdumpfile});
        assertTrue("Restore failed with reason " + retval.errOutput, 0 == retval.exitstatus);
        System.out.println("Restore done");
        
        // ... finally we create new dumps
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema choeger_7 mysql --single-transaction > " + userdumpfile2});
        assertTrue("Dump creation failed", 0 == retval.exitstatus);
        System.out.println("2nd user dump created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema configdb mysql --single-transaction > " + configdbdumpfile2});
        assertTrue("Dump creation for configdb failed", 0 == retval.exitstatus);
        System.out.println("2nd config dump created");

        // If the dump we made at first is equal to the dump created afterwards everything is fine
        retval = shellExecutor.executeprocargs(new String[]{"/usr/bin/diff", "-I", "^.*Dump completed on.*$", userdumpfile, userdumpfile2});
        assertTrue("The user dumps are different: " + retval.stdOutput, 0 == retval.exitstatus);
        System.out.println("First diff ok");
        
        retval = shellExecutor.executeprocargs(new String[]{"/usr/bin/diff", "-I", "^.*Dump completed on.*$", configdbdumpfile, configdbdumpfile2});
        assertTrue("The config dumps are different: " + retval.stdOutput, 0 == retval.exitstatus);
        System.out.println("Second diff ok");
        
    }

    /**
     * This test does a restore on an already existing context. The difference to the test before is that here the context has data before the dump
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testRestoreWithBeforeData() throws IOException, InterruptedException {
        final ShellExecutor shellExecutor = new ShellExecutor();
        ArrayOutput retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/createuser", "-A", CONTEXT_ADMIN_USERNAME , "-P", CONTEXT_ADMIN_PW, "-c", 
                newcontextid, "-u", "test1", "-d", "test1", "-g", "test1", "-s", "test1", "-p", CONTEXT_ADMIN_PW, "-e", "xyz27@bla.de"});
        assertTrue("User creation failed", 0 == retval.exitstatus);
        System.out.println("New user created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/creategroup", "-A", CONTEXT_ADMIN_USERNAME, "-P", CONTEXT_ADMIN_PW, "-n", "test", 
                "-d", "test", "-c", newcontextid});
        assertTrue("Group creation failed", 0 == retval.exitstatus);
        System.out.println("New group created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/createresource", "-A", CONTEXT_ADMIN_USERNAME, "-P", CONTEXT_ADMIN_PW, "-c",
                newcontextid, "-n", "test", "-d", "test", "-e", "ressource@test.de"});
        assertTrue("Resource creation failed", 0 == retval.exitstatus);
        System.out.println("New resource created");
        
        // First we dump the database...
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema choeger_7 mysql --single-transaction > " + userdumpfile});
        assertTrue("Dump creation for user databases failed due to the following reason: " + retval.errOutput, 0 == retval.exitstatus);
        System.out.println("User dump created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema configdb mysql --single-transaction > " + configdbdumpfile});
        assertTrue("Dump creation for configdb failed", 0 == retval.exitstatus);
        System.out.println("Config dump created");
        
        // ... then we add some new data to the context ...
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/createuser", "-A", CONTEXT_ADMIN_USERNAME , "-P", CONTEXT_ADMIN_PW, "-c", 
                newcontextid, "-u", "test2", "-d", "test2", "-g", "test2", "-s", "test2", "-p", CONTEXT_ADMIN_PW, "-e", "xyz127@bla.de"});
        assertTrue("User creation failed", 0 == retval.exitstatus);
        System.out.println("New user created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/creategroup", "-A", CONTEXT_ADMIN_USERNAME, "-P", CONTEXT_ADMIN_PW, "-n", "test2", 
                "-d", "test2", "-c", newcontextid});
        assertTrue("Group creation failed", 0 == retval.exitstatus);
        System.out.println("New group created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/createresource", "-A", CONTEXT_ADMIN_USERNAME, "-P", CONTEXT_ADMIN_PW, "-c",
                newcontextid, "-n", "test2", "-d", "test2", "-e", "ressource2@test.de"});
        assertTrue("Resource creation failed", 0 == retval.exitstatus);
        System.out.println("New resource created");
        
        // ... afterwards the restore takes places ...
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/restorecontext", "-A", "oxadminmaster" , "-P", oxmasterpassword, "-c",
                newcontextid, "-f", userdumpfile + ',' + configdbdumpfile});
        assertTrue("Restore failed with reason " + retval.errOutput, 0 == retval.exitstatus);
        System.out.println("Restore done");
        
        // ... finally we create new dumps
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema choeger_7 mysql --single-transaction > " + userdumpfile2});
        assertTrue("Dump creation failed", 0 == retval.exitstatus);
        System.out.println("2nd user dump created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema configdb mysql --single-transaction > " + configdbdumpfile2});
        assertTrue("Dump creation for configdb failed", 0 == retval.exitstatus);
        System.out.println("2nd config dump created");
        
        // If the dump we made at first is equal to the dump created afterwards everything is fine
        retval = shellExecutor.executeprocargs(new String[]{"/usr/bin/diff", "-I", "^.*Dump completed on.*$", userdumpfile, userdumpfile2});
        assertTrue("The user dumps are different: " + retval.stdOutput, 0 == retval.exitstatus);
        System.out.println("First diff ok");
        
        retval = shellExecutor.executeprocargs(new String[]{"/usr/bin/diff", "-I", "^.*Dump completed on.*$", configdbdumpfile, configdbdumpfile2});
        assertTrue("The config dumps are different: " + retval.stdOutput, 0 == retval.exitstatus);
        System.out.println("Second diff ok");
        
    }

    /**
     * This test does a restore on a context which was deleted beforehand
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testRestoreWithDelete() throws IOException, InterruptedException {
        final ShellExecutor shellExecutor = new ShellExecutor();
        
        // First we dump the database...
        ArrayOutput retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema choeger_7 mysql --single-transaction > " + userdumpfile});
        assertTrue("Dump creation for user databases failed due to the following reason: " + retval.errOutput, 0 == retval.exitstatus);
        System.out.println("User dump created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema configdb mysql --single-transaction > " + configdbdumpfile});
        assertTrue("Dump creation for configdb failed", 0 == retval.exitstatus);
        System.out.println("Config dump created");
        
        // ... then we delete the context ...
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/deletecontext", "-A", "oxadminmaster", "-P", oxmasterpassword, "-c",
                newcontextid});
        assertTrue("Temp Context deletion failed due to the following reason: " + retval.errOutput, 0 == retval.exitstatus);
        System.out.println("Temporary context deleted");
        
        // ... afterwards the restore takes places ...
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/restorecontext", "-A", "oxadminmaster" , "-P", oxmasterpassword, "-c", newcontextid, "-f",
                userdumpfile + ',' + configdbdumpfile});
        assertTrue("Restore failed with reason " + retval.errOutput, 0 == retval.exitstatus);
        System.out.println("Restore done");
        
        // ... finally we create new dumps
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema choeger_7 mysql --single-transaction > " + userdumpfile2});
        assertTrue("Dump creation failed", 0 == retval.exitstatus);
        System.out.println("2nd user dump created");
        
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema configdb mysql --single-transaction > " + configdbdumpfile2});
        assertTrue("Dump creation for configdb failed", 0 == retval.exitstatus);
        System.out.println("2nd config dump created");
        
        // If the dump we made at first is equal to the dump created afterwards everything is fine
        retval = shellExecutor.executeprocargs(new String[]{"/usr/bin/diff", "-I", "^.*Dump completed on.*$", userdumpfile, userdumpfile2});
        assertTrue("The user dumps are different: " + retval.stdOutput, 0 == retval.exitstatus);
        System.out.println("First diff ok");
        
        retval = shellExecutor.executeprocargs(new String[]{"/usr/bin/diff", "-I", "^.*Dump completed on.*$", configdbdumpfile, configdbdumpfile2});
        assertTrue("The config dumps are different: " + retval.stdOutput, 0 == retval.exitstatus);
        System.out.println("Second diff ok");
        
    }

}
