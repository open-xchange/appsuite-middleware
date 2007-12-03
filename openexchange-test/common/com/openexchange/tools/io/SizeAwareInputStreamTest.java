package com.openexchange.tools.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

// Bug 6104
public class SizeAwareInputStreamTest extends TestCase {
	
	public void testCountSimpleRead() throws IOException{
		ByteArrayInputStream b = new ByteArrayInputStream(new byte[23]);
		TestSizeAwareInputStream testStream = new TestSizeAwareInputStream(b);
		while(testStream.read() != -1){}
		
		assertEquals(23, testStream.getSize());
	}
	
	public void testCountReadBuffer() throws IOException{
		ByteArrayInputStream b = new ByteArrayInputStream(new byte[23]);
		TestSizeAwareInputStream testStream = new TestSizeAwareInputStream(b);
		while(testStream.read(new byte[5]) != -1){}
		assertEquals(23, testStream.getSize());	
	}
	
	public void testCountReadBufferComplicated() throws IOException {
		ByteArrayInputStream b = new ByteArrayInputStream(new byte[23]);
		TestSizeAwareInputStream testStream = new TestSizeAwareInputStream(b);
		while(testStream.read(new byte[5],2,2) != -1){}
		assertEquals(23, testStream.getSize());	
		
	}
	
	private static class TestSizeAwareInputStream extends SizeAwareInputStream{

		private long size;
		
		public TestSizeAwareInputStream(InputStream delegate) {
			super(delegate);
		}
		
		public void size(long size) {
			this.size = size;
		}
		
		public long getSize(){
			return size;
		}
	}
}
