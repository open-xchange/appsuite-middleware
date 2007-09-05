
package com.openexchange.admin.console;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author d7
 *
 */
public class StatisticToolsTest extends AbstractTest {
    
    @Test
    public void testgetServerStats() {
        resetBuffers();
        final StatisticTools statisticTools = new StatisticTools(){
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCode = exitCode;
            }
        };
        statisticTools.start(new String[]{"-x"});
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }

    @Test
    public void testgetAdminStats() {
        resetBuffers();
        final StatisticTools statisticTools = new StatisticTools(){
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCode = exitCode;
            }
        };
        statisticTools.start(new String[]{"-x", "-A"});
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
}