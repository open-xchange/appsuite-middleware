package com.openexchange.ajax.kata;

import junit.framework.Test;
import junit.framework.TestSuite;


public class KataSuite extends TestSuite {
    public static Test suite() {
        TestSuite testSuiteForKatas = new TestSuite();
        testSuiteForKatas.addTestSuite(AppointmentRunner.class);
        testSuiteForKatas.addTestSuite(ContactRunner.class);
        testSuiteForKatas.addTestSuite(TaskRunner.class);
        
        testSuiteForKatas.addTestSuite(FolderRunner.class);
        
        return testSuiteForKatas;
    }
}
