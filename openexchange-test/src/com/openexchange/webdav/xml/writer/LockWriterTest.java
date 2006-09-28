package com.openexchange.webdav.xml.writer;

import junit.framework.TestCase;

import com.openexchange.test.XMLCompare;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.xml.WebdavLockWriter;

public class LockWriterTest extends TestCase {
	
	private XMLCompare xmlCompare = new XMLCompare();
	
	
	public void testWriteLock() throws Exception {
		xmlCompare.setCheckTextNames("depth", "owner", "timeout", "href");
		
		WebdavLockWriter writer = new WebdavLockWriter();

		WebdavLock lock = new WebdavLock();
		
		lock.setType(WebdavLock.Type.WRITE_LITERAL);
		lock.setScope(WebdavLock.Scope.EXCLUSIVE_LITERAL);
		lock.setDepth(0);
		lock.setOwner("me");
		lock.setTimeout(WebdavLock.NEVER);
		lock.setToken("opaquelocktoken:blaaaa");
		
		String expect = "<activelock> <locktype><write /></locktype> <lockscope><exclusive/></lockscope> <depth>0</depth> <owner>me</owner> <timeout>Infinite</timeout> <locktoken><href>opaquelocktoken:blaaaa</href></locktoken> </activelock>";
		String got = writer.lock2xml(lock);
		
		assertTrue(xmlCompare.compare(expect,got));
		
		lock.setScope(WebdavLock.Scope.SHARED_LITERAL);
		
		expect = "<activelock> <locktype><write /></locktype> <lockscope><shared/></lockscope> <depth>0</depth> <owner>me</owner> <timeout>Infinite</timeout> <locktoken><href>opaquelocktoken:blaaaa</href></locktoken> </activelock>";
		got = writer.lock2xml(lock);
		
		assertTrue(xmlCompare.compare(expect,got));
		

		lock.setDepth(1);
		
		expect = "<activelock> <locktype><write /></locktype> <lockscope><shared/></lockscope> <depth>1</depth> <owner>me</owner> <timeout>Infinite</timeout> <locktoken><href>opaquelocktoken:blaaaa</href></locktoken> </activelock>";
		got = writer.lock2xml(lock);

		assertTrue(xmlCompare.compare(expect,got));
		
		
		lock.setDepth(WebdavCollection.INFINITY);
		
		expect = "<activelock> <locktype><write /></locktype> <lockscope><shared/></lockscope> <depth>infinity</depth> <owner>me</owner> <timeout>Infinite</timeout> <locktoken><href>opaquelocktoken:blaaaa</href></locktoken> </activelock>";
		got = writer.lock2xml(lock);
		
		assertTrue(xmlCompare.compare(expect,got));
		
	}

}
