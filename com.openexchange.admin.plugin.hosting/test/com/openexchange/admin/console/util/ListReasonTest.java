
package com.openexchange.admin.console.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.openexchange.admin.console.AbstractTest;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.util.reason.ListReasons;

/**
 * @author cutmasta
 *
 */
public class ListReasonTest extends AbstractTest {

    @Test
    public void testListDatabase() {
        
        resetBuffers();
        
        new ListReasons(getMasterCredentialsOptionData()){
            protected void sysexit(int exitCode) {
                ListReasonTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testListDatabaseCSV() {
        
        resetBuffers();
        
        new ListReasons(getCSVMasterOptionData()){
            protected void sysexit(int exitCode) {
                ListReasonTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testListDatabaseInvalidCredentials() {
        
        resetBuffers();
        
        new ListReasons(getWrongMasterCredentialsOptionData()){
            protected void sysexit(int exitCode) {
                ListReasonTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid credentials as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_CREDENTIALS==this.returnCode);
    }
    
    @Test
    public void testListDatabaseWithUnknownOption() {
        
        resetBuffers();
        
        new ListReasons(getUnknownOptionData()){
            protected void sysexit(int exitCode) {
                ListReasonTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected unknown option as return code!",BasicCommandlineOptions.SYSEXIT_UNKNOWN_OPTION==this.returnCode);
    }
    
}
