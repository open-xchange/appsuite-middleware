///*******************************************************************************
// * Copyright (c) 2010 BSI Business Systems Integration AG.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// * 
// * Contributors:
// *     BSI Business Systems Integration AG - initial API and implementation
// ******************************************************************************/
//package com.openexchange.test.osgi;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.util.Enumeration;
//
//import junit.framework.Test;
//import junit.framework.TestFailure;
//import junit.framework.TestResult;
//import junit.framework.TestSuite;
//import junit.textui.TestRunner;
//
//
//
///**
// * Runner for JUnit Plug-in tests.
// * <p>
// * <b>Note:</b>This class is similar to Eclipse's test framework. It is however built on top of JUnit4 and runs with
// * Eclipse version 3.5 and above. Additionally it does not require a test suite that lists all tests to be executed but
// * collects them using Scout's {@link BundleBrowser}.
// */
//public class JUnitTestExecutor {
//
//  public static final Integer EXIT_CODE_OK = 0;
//  public static final Integer EXIT_CODE_TESTS_FAILED = 1;
//  public static final Integer EXIT_CODE_ERRORS_OCCURRED = 2;
//
//
//
//  public JUnitTestExecutor() {
//  }
//
//  public int runAllTests(TestSuite[] tcs) {
//    int exitCode = EXIT_CODE_OK;
//    try {
//      for (TestSuite tc : tcs) {
//        int textResultCode = runTest(tc);
//        exitCode = Math.max(exitCode, textResultCode);
//      }
//    }
//    catch (Throwable t) {
//        
//      exitCode = EXIT_CODE_ERRORS_OCCURRED;
//    }
//    return exitCode;
//  }
//
//  public int runTest(TestSuite tc) {
//    int result = EXIT_CODE_OK;
//
//    PrintStream sysOut = null;
//    PrintStream sysErr = null;
//    PrintStream oldSysOut = System.out;
//    PrintStream oldSysErr = System.err;
//
//    try {
//      // redirect sysout and syserr
//      ByteArrayOutputStream outStrm = new ByteArrayOutputStream();
//      sysOut = new PrintStream(outStrm);
//      ByteArrayOutputStream errStrm = new ByteArrayOutputStream();
//      sysErr = new PrintStream(errStrm);
//      
//      ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
//      TestRunner tr = new TestRunner(new PrintStream(outbuf));
//      System.out.println("Running Test Suite: " + tc.getName());
//      
//      long time = System.currentTimeMillis();
//      TestResult result1 = tr.doRun(tc);
//      time = (System.currentTimeMillis() - time) / 1000;
//
//      Enumeration<Test> te = tc.tests();
//      
//      while (te.hasMoreElements()) {
//          Test test = te.nextElement();
//          
////          XmlNode testNode = new XmlNode(out, "testcase").addAttribute("classname", tc.getName()).addAttribute("name", test.toString()).addAttribute("time", "" + time);
//          System.out.println("Classname " + tc.getName() + " name " + test.toString() + " time " + time);
//          for (Enumeration<TestFailure> fenum = result1.failures(); fenum.hasMoreElements();)  {       
//              TestFailure f = fenum.nextElement();
//              System.out.println("failure " + f.trace() + " type " + f.exceptionMessage());
////              testNode.addChild(new XmlNode("failure", f.trace()).addAttribute("type", f.exceptionMessage()));
//          }
//          
//          for (Enumeration<TestFailure> fenum = result1.errors(); fenum.hasMoreElements();)    {       
//              TestFailure f = fenum.nextElement();
//              System.out.println("error " + f.trace() + " type " + f.exceptionMessage());
////              testNode.addChild(new XmlNode("error", f.trace()).addAttribute("type", f.exceptionMessage()));
//          }
//      }
//      
//      System.out.println(outbuf.toString());
//    }
//    finally {
//      if (sysOut != null) {
//        sysOut.close();
//        sysOut = null;
//      }
//      if (sysErr != null) {
//        sysErr.close();
//        sysErr = null;
//      }
//      System.setOut(oldSysOut);
//      System.setErr(oldSysErr);
//    }
//    return result;
//  }
//}

/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

package com.openexchange.test.osgi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;

/**
 * Runner for JUnit Plug-in tests.
 * <p>
 * <b>Note:</b>This class is similar to Eclipse's test framework. It is however built on top of JUnit4 and runs with Eclipse version 3.5 and
 * above. Additionally it does not require a test suite that lists all tests to be executed but collects them using Scout's
 * {@link BundleBrowser}.
 */
public class JUnitTestExecutor {

    private static final String DUMMY_TESTSUITE_NAME = "FRAMEWORK.INIT";

    public static final String JUNIT_REPORTS_DIR_ARG_NAME = "junitReportsDir";

    public static final Integer EXIT_CODE_OK = 0;

    public static final Integer EXIT_CODE_TESTS_FAILED = 1;

    public static final Integer EXIT_CODE_ERRORS_OCCURRED = 2;

    private final String m_reportsDir;

    private String m_launchingProductId;

    public JUnitTestExecutor() {
        this(findReportsDir());
    }

    public JUnitTestExecutor(String reportsDir) {
        if (reportsDir == null) {
            throw new IllegalArgumentException(
                JUNIT_REPORTS_DIR_ARG_NAME + " must not be null; check if argument '" + JUNIT_REPORTS_DIR_ARG_NAME + "' is set");

        }
        m_reportsDir = reportsDir;
        checkAndCreateReportsDir(m_reportsDir);
    }

    /**
     * Returns the directory where JUnit reports are written to. This default implementation uses the following sources:
     * <ol>
     * <li>Plaltform's command line argument <code>-junitReportsDir=&lt;dir&gt;</code> (e.g. <code>-reportsDir=C:\temp\junitreports</code>)</li>
     * <li>System property with name <code>junitReportsDir</code> (e.g. <code>-DjunitReportsDir=C:\temp\junitreports</code>)</li>
     * <li>Java temp dir</li>
     * </ol>
     * 
     * @param context
     * @return
     */
    private static String findReportsDir() {
        System.out.println("JUnitTestExecutor started");
        String reportsDir = null;
        reportsDir = System.getProperty(JUNIT_REPORTS_DIR_ARG_NAME);
        return reportsDir;
    }

    private static void checkAndCreateReportsDir(String reportsDir) {
        File repDir = new File(reportsDir);
        if (repDir.exists() && repDir.isFile()) {
            throw new IllegalArgumentException("the given reports directory already exists and it is a file");
        }
        repDir.mkdirs();
    }

    public String getReportsDir() {
        return m_reportsDir;
    }

    public int runAllTests(TestSuite[] tcs) {
        int exitCode = EXIT_CODE_OK;
        try {
            // JUnitTestClassBrowser browser = new JUnitTestClassBrowser();
            for (TestSuite test : tcs) {
                int textResultCode = runTest(test);
                exitCode = Math.max(exitCode, textResultCode);
            }
        } catch (Throwable t) {
            try {
                // create a dummy test suite so that the Exception is reported in the test results
                JUnitResultFormatter formatter = createJUnitResultFormatter(DUMMY_TESTSUITE_NAME);
                JUnitTest dummyTest = new JUnitTest(DUMMY_TESTSUITE_NAME);
                formatter.startTestSuite(dummyTest);
                formatter.addError(null, t);
                formatter.endTestSuite(dummyTest);
            } catch (FileNotFoundException e) {
                System.err.println(e);
            }
            exitCode = EXIT_CODE_ERRORS_OCCURRED;
        }
        return exitCode;
    }

    public int runTest(TestSuite testClass) throws FileNotFoundException {
        int result = EXIT_CODE_OK;

        PrintStream sysOut = null;
        PrintStream sysErr = null;
        PrintStream oldSysOut = System.out;
        PrintStream oldSysErr = System.err;

        try {
            // redirect sysout and syserr
            // ByteArrayOutputStream outStrm = new ByteArrayOutputStream();
            // sysOut = new PrintStream(outStrm);
            // ByteArrayOutputStream errStrm = new ByteArrayOutputStream();
            // sysErr = new PrintStream(errStrm);

            // create Ant JUnitTest that executes the test case
            // JUnitTest junitTest = createJUnitTest(testClass.getName());
            JUnitResultFormatter formatter = createJUnitResultFormatter(testClass.getName());

            // run the test
            long start = System.currentTimeMillis();
            formatter.startTest(testClass);

            
            
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
            TestRunner tr = new TestRunner(new PrintStream(outbuf));
            
            TestResult testResult = new TestResult();
//            testResult.addListener(formatter);
            
            System.out.println("Running Test Suite: " + testClass.getName());
            
            try {
                testResult = tr.doRun(testClass);
                testResult.addListener(formatter);
            } catch (Throwable t) {
                formatter.addError(null, t);
                result = EXIT_CODE_ERRORS_OCCURRED;
            } finally {

                
                // formatter.setSystemOutput(new String(outStrm.toByteArray()));
                // formatter.setSystemError(new String(errStrm.toByteArray()));
                formatter.endTest(testClass);
                if (result == EXIT_CODE_OK) {
                    if (testResult.errorCount() > 0) {
                        result = EXIT_CODE_ERRORS_OCCURRED;
                    } else if (testResult.failureCount() > 0) {
                        result = EXIT_CODE_TESTS_FAILED;
                    }
                }
            }
        } finally {
            if (sysOut != null) {
                sysOut.close();
                sysOut = null;
            }
            if (sysErr != null) {
                sysErr.close();
                sysErr = null;
            }
            System.setOut(oldSysOut);
            System.setErr(oldSysErr);
        }
        return result;
    }

    /**
     * Creates a new Ant {@link JUnitTest} used to execute the test and for reporting its outcome.
     * 
     * @param testName
     * @return
     */
    private JUnitTest createJUnitTest(String testName) {
        JUnitTest junitTest = new JUnitTest(testName);
        Properties props = new Properties();
        props.putAll(System.getProperties());
        junitTest.setProperties(props);
        return junitTest;
    }

    /**
     * Creates a {@link XMLJUnitResultFormatter} that writes its output to a file in the reports directory.
     * 
     * @param testName
     * @return
     * @throws FileNotFoundException
     */
    private XMLJUnitResultFormatter createJUnitResultFormatter(String testName) throws FileNotFoundException {
        XMLJUnitResultFormatter formatter = new XMLJUnitResultFormatter();
        formatter.setOutput(new FileOutputStream(getReportsDir() + File.separator + getFileNameFor(testName)));
        return formatter;
    }

    private String getFileNameFor(String testName) throws FileNotFoundException {
        if (m_launchingProductId != null) {
            return "TEST-" + m_launchingProductId + "-" + testName + ".xml";
        } else {
            return "TEST-" + testName + ".xml";
        }
    }

    private void dumpResult(File f, int exitCode) {
        if (f.isFile()) {
            if (exitCode == EXIT_CODE_OK) {
                // nop
            } else {
                System.out.println("FAILED " + f.getName());
                try {
                    FileInputStream in = new FileInputStream(f);
                    byte[] buf = new byte[(int) f.length()];
                    in.read(buf);
                    in.close();
                    System.out.println(new String(buf, 0, buf.length));
                } catch (Throwable t) {
                    System.out.println("ERROR: " + t);
                }
                System.exit(0);
            }
        }
    }
}
