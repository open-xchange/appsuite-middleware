
package com.openexchange.admin.console.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.openexchange.admin.console.AbstractTest;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.util.database.ListDatabase;

/**
 * @author cutmasta
 *
 */
public class ListDatabasesTest extends AbstractTest {
    
    
    @Test
    public void testListDatabase() {
        
        resetBuffers();
        
        new ListDatabase(getMasterCredentialsOptionData()){
            protected void sysexit(int exitCode) {
                ListDatabasesTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testListDatabaseCSV() {
        
        resetBuffers();
        
        new ListDatabase(getCSVMasterOptionData()){
            protected void sysexit(int exitCode) {
                ListDatabasesTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testListDatabaseInvalidCredentials() {
        
        resetBuffers();
        
        new ListDatabase(getWrongMasterCredentialsOptionData()){
            protected void sysexit(int exitCode) {
                ListDatabasesTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid credentials as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_CREDENTIALS==this.returnCode);
    }
    
    @Test
    public void testListDatabaseUnknownOption() {
        
        resetBuffers();
        
        new ListDatabase(getUnknownOptionData()){
            protected void sysexit(int exitCode) {
                ListDatabasesTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected unknown option as return code!",BasicCommandlineOptions.SYSEXIT_UNKNOWN_OPTION==this.returnCode);
    }
    
    
    
}
