
package com.openexchange.admin.console.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.openexchange.admin.console.AbstractTest;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.util.filestore.ChangeFilestore;
import com.openexchange.admin.console.util.filestore.RegisterFilestore;

/**
 * @author cutmasta
 *
 */
public class ChangeFilestoreTest extends AbstractTest {
    
    @Test
    public void testEditFilestore() {
        
        resetBuffers();
        String store = "file:////tmp/"+System.currentTimeMillis();
        new RegisterFilestore(RegisterFilestoreTest.getAllOptionData(store)){
            protected void sysexit(int exitCode) {
                ChangeFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
        
        
        //edit fstore via store id        
        int store_id = Integer.parseInt(outBytes.toString().trim());
        new ChangeFilestore(getAllChangeOptionData(store,store_id)){
            protected void sysexit(int exitCode) {
                ChangeFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
        
    }
    
    @Test
    public void testEditFilestoreWithInvalidCredentials() {
        
        resetBuffers();
        String store = "file:////tmp/"+System.currentTimeMillis();
        new RegisterFilestore(RegisterFilestoreTest.getAllOptionData(store)){
            protected void sysexit(int exitCode) {
                ChangeFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
        
        
        //edit fstore via store id        
        int store_id = Integer.parseInt(outBytes.toString().trim());
        new ChangeFilestore(getAllChangeOptionDataWithInvalidCredentials(store,store_id)){
            protected void sysexit(int exitCode) {
                ChangeFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid credentials as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_CREDENTIALS==this.returnCode);
        
    }
    
    @Test
    public void testEditFilestoreWithunknownOption() {
        
        resetBuffers();
      
        
        new ChangeFilestore(getUnknownOptionData()){
            protected void sysexit(int exitCode) {
                ChangeFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected unknown option as return code!",BasicCommandlineOptions.SYSEXIT_UNKNOWN_OPTION==this.returnCode);
        
    }
    
    @Test
    public void testEditFilestoreWithMissingOption() {
        
        resetBuffers();
      
        
        new ChangeFilestore(getMissingOptionData()){
            protected void sysexit(int exitCode) {
                ChangeFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected missing option as return code!",BasicCommandlineOptions.SYSEXIT_MISSING_OPTION==this.returnCode);
        
    }
    
    
    public static String[] getAllChangeOptionData(String store,int id){
        String[] tmp = {OPTION_SUPER_ADMIN_USER, 
                OPTION_SUPER_ADMIN_PWD,
                "--storepath="+store+"_"+CHANGE_SUFFIX,
                "--storesize=13337",
                "--maxcontexts=13336",
                "--id="+id
                };
        return tmp;
    }
    
    public static String[] getAllChangeOptionDataWithInvalidCredentials(String store,int id){
        String[] tmp = {OPTION_SUPER_ADMIN_USER+"_xyzfoobar", 
                OPTION_SUPER_ADMIN_PWD+"xyzfoobar",
                "--storepath="+store+"_"+CHANGE_SUFFIX,
                "--storesize=13337",
                "--maxcontexts=13336",
                "--id="+id
                };
        return tmp;
    }
    
}
