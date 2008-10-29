package com.openexchange.ajax.mail.addresscollector;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AddressCollectorTestSuite extends TestSuite {
    
    public static Test suite() {
        final TestSuite tests = new TestSuite();
        
        tests.addTestSuite(ConfigurationTest.class);
        //tests.addTestSuite(MailTest.class);
        
        return tests;
    }
}
