
package com.openexchange.admin.console.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.openexchange.admin.console.AbstractTest;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.util.reason.CreateReason;

/**
 * @author cutmasta
 *
 */
public class CreateReasonTest extends AbstractTest {

    @Test
    public void testAddReason() {
        
        resetBuffers();
        new CreateReason(getAllOptionData()){
            protected void sysexit(int exitCode) {
                CreateReasonTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testAddReasonWithInvalidCredentials() {
        
        resetBuffers();
        new CreateReason(getAllOptionDataWithInvalidCredentials()){
            protected void sysexit(int exitCode) {
                CreateReasonTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid credentials as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_CREDENTIALS==this.returnCode);
    }
    
    @Test
    public void testAddReasonWithMissingOption() {
        
        resetBuffers();
        new CreateReason(getMissingOptionData()){
            protected void sysexit(int exitCode) {
                CreateReasonTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected missing option as return code!",BasicCommandlineOptions.SYSEXIT_MISSING_OPTION==this.returnCode);
    }
    
    @Test
    public void testAddReasonWithUnknownOption() {
        
        resetBuffers();
        new CreateReason(getUnknownOptionData()){
            protected void sysexit(int exitCode) {
                CreateReasonTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected unknown option as return code!",BasicCommandlineOptions.SYSEXIT_UNKNOWN_OPTION==this.returnCode);
    }
    
    public static String[] getAllOptionData(){
        String[] tmp = {OPTION_SUPER_ADMIN_USER, 
                OPTION_SUPER_ADMIN_PWD,
                "--reasontext=testcase-reason-"+System.currentTimeMillis()               
                };
        return tmp;
    }
    
    public static String[] getAllOptionDataWithInvalidCredentials(){
        String[] tmp = {OPTION_SUPER_ADMIN_USER+"_xyzfioiobar", 
                OPTION_SUPER_ADMIN_PWD+"_xyzfoobar",
                "--reasontext=testcase-reason-"+System.currentTimeMillis()               
                };
        return tmp;
    }
    
}
