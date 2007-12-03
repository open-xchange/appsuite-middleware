package com.openexchange.groupware.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SizedInputStreamTest {
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(SizedInputStreamTest.class);
	}
	
	
	
	public byte[] testString = null;
	public SizedInputStream testStream = null;
	public static final String ENCODING = "UTF-8";
	public static final String TEXT = "Blabla";
	
	

	@Before public void setup() throws UnsupportedEncodingException{
		testString = TEXT.getBytes(ENCODING);
		testStream = new SizedInputStream(
				new ByteArrayInputStream(testString), 
				testString.length,
				Format.CSV);
	}
	
	@After public void tearDown() throws IOException{
		testStream.close();
	}
	
	@Test public void sizing() throws UnsupportedEncodingException{
		assertTrue("First" , testStream.getSize() == testString.length);
		assertTrue("Second", testStream.getSize() == testString.length);
		assertTrue("Third" , testStream.getSize() == testString.length);
	}
	
	@Test public void normalReading() throws IOException{
		int l;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[0x1];
		while( (l = testStream.read(buffer)) != -1){
			baos.write(buffer, 0, l);
		}
		assertEquals("Content", baos.toString(ENCODING) , TEXT);
		baos.close();
		assertTrue("", testStream.getSize() == testString.length);
		//assertEquals("", testStream.getSize() , testString.length);
	}
	
	@Test public void readingViaGetSize() throws IOException{
		byte[] buffer = new byte[(int) testStream.getSize()];
		testStream.read(buffer);
		String test2 = new String(buffer, ENCODING);
		assertEquals("Content comparison", test2, TEXT);
	}
}
