package com.openexchange.groupware.infostore;

import com.openexchange.groupware.infostore.utils.URLHelper;

import junit.framework.TestCase;

public class URLHelperTest extends TestCase {

	public void testMissingSchema(){
		URLHelper helper = new URLHelper();
		assertEquals("http://www.open-xchange.com", helper.process("www.open-xchange.com"));
		assertEquals("mailto:francisco.laguna@open-xchange.com", helper.process("francisco.laguna@open-xchange.com"));
	}
	
	public void testMailtoSchema(){
		URLHelper helper = new URLHelper();
		assertEquals("mailto:francisco.laguna@open-xchange.com", helper.process("mailto:francisco.laguna@open-xchange.com"));	
	}
	
	public void testAnySchema(){
		URLHelper helper = new URLHelper();
		assertEquals("http://www.open-xchange.com", helper.process("http://www.open-xchange.com"));
		assertEquals("https://www.open-xchange.com", helper.process("https://www.open-xchange.com"));
		assertEquals("ftp://www.open-xchange.com", helper.process("ftp://www.open-xchange.com"));
		assertEquals("sftp://www.open-xchange.com", helper.process("sftp://www.open-xchange.com"));
		
	}
	

}
