
package com.openexchange.admin.console.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.openexchange.admin.console.AbstractTest;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.util.filestore.RegisterFilestore;

/**
 * @author cutmasta
 *
 */
public class RegisterFilestoreTest extends AbstractTest {
    
    
    @Test
    public void testRegisterFilestore() {
        
        resetBuffers();
        String store = "file:////tmp/"+System.currentTimeMillis();
        new RegisterFilestore(getAllOptionData(store)){
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testRegisterFilestoreWithInvalidData() {
        
        resetBuffers();
        String store = "tmp/"+System.currentTimeMillis();
        new RegisterFilestore(getAllOptionData(store)){
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid data as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_DATA==this.returnCode);
    }
    
    @Test
    public void testRegisterFilestoreWithMissingOption() {
        
        resetBuffers();
        
        new RegisterFilestore(getMissingOptionData()){
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected missing option as return code!",BasicCommandlineOptions.SYSEXIT_MISSING_OPTION==this.returnCode);
    }
    
    @Test
    public void testRegisterFilestoreWithUnknownOption() {
        
        resetBuffers();
        
        new RegisterFilestore(getUnknownOptionData()){
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected unknown option as return code!",BasicCommandlineOptions.SYSEXIT_UNKNOWN_OPTION==this.returnCode);
    }
    
    @Test
    public void testRegisterFilestoreWithInvalidCredentials() {
        
        resetBuffers();
        String store = "file:////tmp/"+System.currentTimeMillis();
        new RegisterFilestore(getAllOptionDataWithInvalidCredentials(store)){
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid credentials as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_CREDENTIALS==this.returnCode);
    }
    
    public static String[] getAllOptionData(String store){
        String[] tmp = {OPTION_SUPER_ADMIN_USER, 
                OPTION_SUPER_ADMIN_PWD,
                "--storepath="+store,
                "--storesize=1000",
                "--maxcontexts=1000"
                };
        return tmp;
    }
    
    public static String[] getAllOptionDataWithInvalidCredentials(String store){
        String[] tmp = {OPTION_SUPER_ADMIN_USER+"_xyzfoobar", 
                OPTION_SUPER_ADMIN_PWD+"_barfoo",
                "--storepath="+store,
                "--storesize=1000",
                "--maxcontexts=1000"
                };
        return tmp;
    }
    
    
    
}
