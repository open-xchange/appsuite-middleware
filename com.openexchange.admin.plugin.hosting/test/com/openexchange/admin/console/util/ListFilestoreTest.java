
package com.openexchange.admin.console.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.openexchange.admin.console.AbstractTest;
import com.openexchange.admin.console.BasicCommandlineOptions;

/**
 * @author cutmasta
 *
 */
public class ListFilestoreTest extends AbstractTest {
    
    @Test
    public void testListFilestore() {
        
        resetBuffers();
        
        new ListFilestores(getMasterCredentialsOptionData()){
            protected void sysexit(int exitCode) {
                ListFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testListFilestoreCSV() {
        
        resetBuffers();
        
        new ListFilestores(getCSVMasterOptionData()){
            protected void sysexit(int exitCode) {
                ListFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testListFilestoreWithInvalidCredentials() {
        
        resetBuffers();
        
        new ListFilestores(getWrongMasterCredentialsOptionData()){
            protected void sysexit(int exitCode) {
                ListFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid credentials as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_CREDENTIALS==this.returnCode);
    }
    
    @Test
    public void testListFilestoreWithUnknownOption() {
        
        resetBuffers();
        
        new ListFilestores(getUnknownOptionData()){
            protected void sysexit(int exitCode) {
                ListFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected unknown option as return code!",BasicCommandlineOptions.SYSEXIT_UNKNOWN_OPTION==this.returnCode);
    }
    
    

}
