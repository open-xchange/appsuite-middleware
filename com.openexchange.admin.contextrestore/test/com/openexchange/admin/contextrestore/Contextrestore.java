package com.openexchange.admin.contextrestore;


import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.openexchange.admin.tools.ShellExecutor;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;

import static org.junit.Assert.assertTrue;

public class Contextrestore {

    // TODO: These values should been taken from a configfile
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
    
    private final static String userdumpfile = "/home/d7/mysqldumpuser.txt";

    private final static String userdumpfile2 = "/home/d7/mysqldumpuser2.txt";

    private final static String configdbdumpfile = "/home/d7/mysqldumpconfigdb.txt";

    private final static String configdbdumpfile2 = "/home/d7/mysqldumpconfigdb2.txt";
    
    @Before
    public void setUp() throws Exception {
        final ShellExecutor shellExecutor = new ShellExecutor();
        
        // First create a new context
        ArrayOutput retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/createcontext", "-A", "oxadminmaster", "-P", oxmasterpassword, "-c", newcontextid, 
                "-u", "oxadmin", "-d", "oxadmin", "-g", "oxadmin", "-s", "oxadmin", "-p", "secret", "-e", "xyz67@bla.de", "-q", "100"});
        assertTrue("Context creation failed due to the following reason: " + retval.errOutput, 0 == retval.exitstatus);
    }

    @After
    public void tearDown() throws Exception {
        final ShellExecutor shellExecutor = new ShellExecutor();

        // Finally delete the created context...
        ArrayOutput retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/deletecontext", "-A", "oxadminmaster", "-P", oxmasterpassword, "-c",
                newcontextid});
        assertTrue("Temp Context deletion failed due to the following reason: " + retval.errOutput, 0 == retval.exitstatus);
    }
    
    @Test
    public void testRestore() throws IOException, InterruptedException {
        final ShellExecutor shellExecutor = new ShellExecutor();
        
        // First we dump the database...
        ArrayOutput retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema choeger_7 mysql --single-transaction > " + userdumpfile});
        assertTrue("Dump creation for user databases failed due to the following reason: " + retval.errOutput, 0 == retval.exitstatus);

        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema configdb mysql --single-transaction > " + configdbdumpfile});
        assertTrue("Dump creation for configdb failed", 0 == retval.exitstatus);
        
        // ... then we add some new data to the context ...
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/createuser", "-A", "oxadmin" , "-P", "secret", "-c", newcontextid, "-u", "test1", "-d",
                "test1", "-g", "test1", "-s", "test1", "-p", "secret", "-e", "xyz27@bla.de"});
        assertTrue("User creation failed", 0 == retval.exitstatus);
        
        // ... afterwards the restore takes places ...
        retval = shellExecutor.executeprocargs(new String[]{"/opt/open-xchange/sbin/contextrestore", "-A", "oxadminmaster" , "-P", oxmasterpassword, "-c", newcontextid, "-f",
                userdumpfile + ',' + configdbdumpfile});
        assertTrue("Restore failed", 0 == retval.exitstatus);
        
        // ... finally we create new dumps
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema choeger_7 mysql --single-transaction > " + userdumpfile2});
        assertTrue("Dump creation failed", 0 == retval.exitstatus);
        
        retval = shellExecutor.executeprocargs(new String[]{"/bin/bash" , "-c" ,
                "mysqldump --user=" + username + " -p" + password  + " --databases information_schema configdb mysql --single-transaction > " + configdbdumpfile2});
        assertTrue("Dump creation for configdb failed", 0 == retval.exitstatus);

        // If the dump we made at first is equal to the dump created afterwards everything is fine
        retval = shellExecutor.executeprocargs(new String[]{"/usr/bin/diff", "-I", "^.*Dump completed on.*$", userdumpfile, userdumpfile2});
        assertTrue("The user dumps are different: " + retval.stdOutput, 0 == retval.exitstatus);
        
        retval = shellExecutor.executeprocargs(new String[]{"/usr/bin/diff", "-I", "^.*Dump completed on.*$", configdbdumpfile, configdbdumpfile2});
        assertTrue("The config dumps are different: " + retval.stdOutput, 0 == retval.exitstatus);
        
    }

}
