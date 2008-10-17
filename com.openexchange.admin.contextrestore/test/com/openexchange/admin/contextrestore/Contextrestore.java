package com.openexchange.admin.contextrestore;


import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.openexchange.admin.tools.ShellExecutor;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;

public class Contextrestore {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testRestore() throws IOException, InterruptedException {
        final ShellExecutor shellExecutor = new ShellExecutor();

        // First we dump the database...
        ArrayOutput retval = shellExecutor.executeprocargs(new String[]{"bash" , "-c" ,
                "mysqldump --user=openexchange -p --all-databases --single-transaction > mysqldumpnew.txt"});
        
        // ... then we add some new data to the context ...
        retval = shellExecutor.executeprocargs(new String[]{"createuser", "-A", "oxadmin" , "-P", "secret", "-c", "7777", "-u", "test1", "-d",
                "test1", "-g", "test1", "-s", "test1", "-p", "secret", "-e", "xyz27@bla.de"});
        
        // ... now split the dump ...
        
        // ... afterwards the restore takes places ...
        retval = shellExecutor.executeprocargs(new String[]{"contextrestore", "-A", "oxadminmaster" , "-P", "secret", "-c", "7777", "-f",
                "/home/d7/mysqlconfigdbdump.txt,/home/d7/mysqluserdump.txt"});
        
        // ... finally we create a new dump
        
        // If the dump we made at first is equal to the dump created afterwards everything is fine
        
    }

}
