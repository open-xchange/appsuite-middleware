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
import java.io.PrintStream;
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;



/**
 * Runner for JUnit Plug-in tests.
 * <p>
 * <b>Note:</b>This class is similar to Eclipse's test framework. It is however built on top of JUnit4 and runs with
 * Eclipse version 3.5 and above. Additionally it does not require a test suite that lists all tests to be executed but
 * collects them using Scout's {@link BundleBrowser}.
 */
public class JUnitTestExecutor {

  public static final Integer EXIT_CODE_OK = 0;
  public static final Integer EXIT_CODE_TESTS_FAILED = 1;
  public static final Integer EXIT_CODE_ERRORS_OCCURRED = 2;



  public JUnitTestExecutor() {
  }

  public int runAllTests(TestSuite[] tcs) {
    int exitCode = EXIT_CODE_OK;
    try {
      for (TestSuite tc : tcs) {
        int textResultCode = runTest(tc);
        exitCode = Math.max(exitCode, textResultCode);
      }
    }
    catch (Throwable t) {
        
      exitCode = EXIT_CODE_ERRORS_OCCURRED;
    }
    return exitCode;
  }

  public int runTest(TestSuite tc) {
    int result = EXIT_CODE_OK;

    PrintStream sysOut = null;
    PrintStream sysErr = null;
    PrintStream oldSysOut = System.out;
    PrintStream oldSysErr = System.err;

    try {
      // redirect sysout and syserr
      ByteArrayOutputStream outStrm = new ByteArrayOutputStream();
      sysOut = new PrintStream(outStrm);
      ByteArrayOutputStream errStrm = new ByteArrayOutputStream();
      sysErr = new PrintStream(errStrm);
      
      ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
      TestRunner tr = new TestRunner(new PrintStream(outbuf));
      System.out.println("Running Test Suite: " + tc.getName());
      
      long time = System.currentTimeMillis();
      TestResult result1 = tr.doRun(tc);
      time = (System.currentTimeMillis() - time) / 1000;

      Enumeration<Test> te = tc.tests();
      
      while (te.hasMoreElements()) {
          Test test = te.nextElement();
          
//          XmlNode testNode = new XmlNode(out, "testcase").addAttribute("classname", tc.getName()).addAttribute("name", test.toString()).addAttribute("time", "" + time);
          System.out.println("Classname " + tc.getName() + " name " + test.toString() + " time " + time);
          for (Enumeration<TestFailure> fenum = result1.failures(); fenum.hasMoreElements();)  {       
              TestFailure f = fenum.nextElement();
              System.out.println("failure " + f.trace() + " type " + f.exceptionMessage());
//              testNode.addChild(new XmlNode("failure", f.trace()).addAttribute("type", f.exceptionMessage()));
          }
          
          for (Enumeration<TestFailure> fenum = result1.errors(); fenum.hasMoreElements();)    {       
              TestFailure f = fenum.nextElement();
              System.out.println("error " + f.trace() + " type " + f.exceptionMessage());
//              testNode.addChild(new XmlNode("error", f.trace()).addAttribute("type", f.exceptionMessage()));
          }
      }
      
      System.out.println(outbuf.toString());
    }
    finally {
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
}
