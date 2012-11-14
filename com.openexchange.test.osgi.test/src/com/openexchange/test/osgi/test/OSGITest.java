package com.openexchange.test.osgi.test;


import java.util.Iterator;
import org.osgi.framework.BundleContext;
import com.openexchange.config.ConfigurationService;
import junit.framework.TestCase;
//import org.apache.commons.logging.Log;

//@JUnitTestName(value = "MyTest")
public class OSGITest extends TestCase {

    /**
     * Test to see if can get a reference to the context
     */
    public void testGetContextService() {
        BundleContext context = Activator.getDefault().getContext();

        assertNotNull(context);
        
//        Log ls = com.openexchange.log.Log.loggerFor(OSGITest.class);
        
//        assertNotNull(ls);
    }
    
    public void test() {
        Activator act = Activator.getDefault();
        
        assertNotNull(act);
        
        final ConfigurationService configurationService = act.getService(ConfigurationService.class);
        
        assertNotNull(configurationService);
        
        Iterator it = configurationService.propertyNames();
        
        assertNotNull(it);
    }
}