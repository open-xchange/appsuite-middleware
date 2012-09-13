//package com.openexchange.test.osgi;
//
//import java.io.BufferedOutputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.text.SimpleDateFormat;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Enumeration;
//import java.util.Map;
//
//import junit.framework.Test;
//import junit.framework.TestFailure;
//import junit.framework.TestResult;
//import junit.framework.TestSuite;
//import junit.textui.TestRunner;
//
//import org.osgi.framework.Bundle;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.InvalidSyntaxException;
//import org.osgi.framework.ServiceReference;
//
//import com.buglabs.util.xml.XmlNode;
//
//public class BundleTestRunnerThread extends Thread {
//	private static final long SETTLE_MILLIS = 5000;
//
//	private final BundleContext context;
//
//	private final File outputDir;
//	private boolean errorOccurred = false;
//
//	private final int shutdownTimeout;
//
//	public BundleTestRunnerThread(BundleContext context, final File outputDir, int shutdownTimeout) {
//		this.context = context;
//		this.outputDir = outputDir;
//		this.shutdownTimeout = shutdownTimeout;
//	}
//
//	@Override
//	public void run() {
//		try {
//			System.out.println("Waiting " + SETTLE_MILLIS + " millis for OSGi instance to settle...");
//			Thread.sleep(SETTLE_MILLIS);
//			
//			System.out.println("System properties:");
//			for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) 
//				System.out.println("Property: " + entry.getKey().toString() + "  Value: " + entry.getValue());
//			
//			ServiceReference[] srefs = context.getServiceReferences(TestSuite.class.getName(), null);
//
//			if (srefs != null && srefs.length > 0) {				
//
//				for (ServiceReference sr : srefs) {
//					TestSuite ts = (TestSuite) context.getService(sr);
//
//					if (ts != null)
//						try {
//							runTest(ts);
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//				}
//			} else {
//				System.out.println("No " + TestSuite.class.getName() + " tests were found in the service registry.");
//			}
//		} catch (InvalidSyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//		}
//		
//		try {
//			Thread.sleep(shutdownTimeout);
//		} catch (InterruptedException e) {			
//		}
//		
//		//Shutdown all the bundles
//		for (Bundle bundle : context.getBundles()) {
//			if (bundle.getBundleId() != 0) {
//				try {
//					bundle.stop();
//				} catch (Exception e) {
//					//Ignore errors
//				}
//			}
//		}
//		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {			
//		}
//
//		// Test execution complete, now forcibly shutdown the JVM.
//		if (errorOccurred)
//			System.exit(1);
//		else 
//			System.exit(0);
//	}
//
//	protected void runTest(TestSuite tc) throws IOException {
//		XmlNode out = new XmlNode("testsuite");
//		writeProperties(out);
//		ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
//		TestRunner tr = new TestRunner(new PrintStream(outbuf));
//		System.out.println("Running Test Suite: " + tc.getName());
//		
//		long time = System.currentTimeMillis();
//		TestResult result = tr.doRun(tc);
//		time = (System.currentTimeMillis() - time) / 1000;
//
//		Enumeration<Test> te = tc.tests();
//		while (te.hasMoreElements()) {
//			Test test = te.nextElement();
//			
//			XmlNode testNode = new XmlNode(out, "testcase").addAttribute("classname", tc.getName()).addAttribute("name", test.toString()).addAttribute("time", "" + time);
//			
//			for (Enumeration<TestFailure> fenum = result.failures(); fenum.hasMoreElements();)	{		
//				TestFailure f = fenum.nextElement();
//				testNode.addChild(new XmlNode("failure", f.trace()).addAttribute("type", f.exceptionMessage()));
//			}
//			
//			for (Enumeration<TestFailure> fenum = result.errors(); fenum.hasMoreElements();)	{		
//				TestFailure f = fenum.nextElement();
//				testNode.addChild(new XmlNode("error", f.trace()).addAttribute("type", f.exceptionMessage()));
//			}
//		}
//
//		out.addAttribute("errors", "" + result.errorCount());
//		out.addAttribute("failures", "" + result.failureCount());
//
//		out.addAttribute("hostname", getHostName());
//		out.addAttribute("name", tc.getName());
//		out.addAttribute("tests", "" + tc.testCount());
//		out.addAttribute("time", "" + time);
//		out.addAttribute("timestamp", getDateStamp());
//
//		out.addChild(new XmlNode("system-out", outbuf.toString()));
//		
//		File outFile = new File(outputDir, "TEST-" + tc.getName() + ".xml");
//		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
//		bos.write(out.toString().getBytes());
//		bos.flush();
//		bos.close();
//
//		System.out.print("Test Suite Complete: " + tc.getName());
//		System.out.println("  Results ~ Errors: " + result.errorCount() + " Failures: " + result.failureCount());
//		
//		if (result.errorCount() > 0 || result.failureCount() > 0)
//			errorOccurred = true;
//	}
//
//	private String getDateStamp() {
//		Calendar cal = Calendar.getInstance();
//		//2011-05-11T06:20:03
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//		return sdf.format(cal.getTime());
//	}
//
//	private String getHostName() {
//
//		return "t410s";
//	}
//
//	private String wrapCdata(String in) {
//		return "<![CDATA[" + in + "]]>";
//	}
//
//	private void writeProperties(XmlNode out) {
//		for (Object key : System.getProperties().keySet())
//			out.addChild(new XmlNode("property").addAttribute("name", key.toString()).addAttribute("value", System.getProperty(key.toString())));
//	}
//}