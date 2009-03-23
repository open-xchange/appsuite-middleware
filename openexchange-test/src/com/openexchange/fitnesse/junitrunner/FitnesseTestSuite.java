package com.openexchange.fitnesse.junitrunner;

import junit.framework.Test;
import junit.framework.TestSuite;


public class FitnesseTestSuite extends TestSuite {
    public static Test suite() {
        final TestSuite tests = new TestSuite();
        tests.addTestSuite(ContactFitnesseTests.class);
        tests.addTestSuite(AppointmentFitnesseTests.class);
        tests.addTestSuite(TaskFitnesseTests.class);
        return tests;
    }
}
