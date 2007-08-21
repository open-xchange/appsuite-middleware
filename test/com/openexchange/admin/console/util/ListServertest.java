
package com.openexchange.admin.console.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.openexchange.admin.console.AbstractTest;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.util.server.ListServer;

/**
 * @author cutmasta
 *
 */
public class ListServertest extends AbstractTest {
    
    @Test
    public void testListServer() {
        
        resetBuffers();
        
        new ListServer(getMasterCredentialsOptionData()){
            protected void sysexit(int exitCode) {
                ListServertest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testListServerCSV() {
        
        resetBuffers();
        
        new ListServer(getCSVMasterOptionData()){
            protected void sysexit(int exitCode) {
                ListServertest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testListServerInvalidCredentials() {
        
        resetBuffers();
        
        new ListServer(getWrongMasterCredentialsOptionData()){
            protected void sysexit(int exitCode) {
                ListServertest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid credentials as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_CREDENTIALS==this.returnCode);
    }
    
    @Test
    public void testListServerUnknownOption() {
        
        resetBuffers();
        
        new ListServer(getUnknownOptionData()){
            protected void sysexit(int exitCode) {
                ListServertest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected unknown options as return code!",BasicCommandlineOptions.SYSEXIT_UNKNOWN_OPTION==this.returnCode);
    }
    
    
}
