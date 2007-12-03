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
		
		String expect = "<blupp xmlns:D=\"DAV:\"> <D:activelock> <D:locktype><D:write /></D:locktype> <D:lockscope><D:exclusive/></D:lockscope> <D:depth>0</D:depth> <D:owner>me</D:owner> <D:timeout>Infinite</D:timeout> <D:locktoken><D:href>opaquelocktoken:blaaaa</D:href></D:locktoken> </D:activelock></blupp>";
		String got = "<blupp xmlns:D=\"DAV:\">"+writer.lock2xml(lock)+"</blupp>";
		
		assertTrue(xmlCompare.compare(expect,got));
		
		lock.setScope(WebdavLock.Scope.SHARED_LITERAL);
		
		expect = "<blupp xmlns:D=\"DAV:\"><D:activelock> <D:locktype><D:write /></D:locktype> <D:lockscope><D:shared/></D:lockscope> <D:depth>0</D:depth> <D:owner>me</D:owner> <D:timeout>Infinite</D:timeout> <D:locktoken><D:href>opaquelocktoken:blaaaa</D:href></D:locktoken> </D:activelock></blupp>";
		got = "<blupp xmlns:D=\"DAV:\">"+writer.lock2xml(lock)+"</blupp>";
		
		assertTrue(xmlCompare.compare(expect,got));
		

		lock.setDepth(1);
		
		expect = "<blupp xmlns:D=\"DAV:\"><D:activelock> <D:locktype><D:write /></D:locktype> <D:lockscope><D:shared/></D:lockscope> <D:depth>1</D:depth> <D:owner>me</D:owner> <D:timeout>Infinite</D:timeout> <D:locktoken><D:href>opaquelocktoken:blaaaa</D:href></D:locktoken> </D:activelock></blupp>";
		got = "<blupp xmlns:D=\"DAV:\">"+writer.lock2xml(lock)+"</blupp>";

		assertTrue(xmlCompare.compare(expect,got));
		
		
		lock.setDepth(WebdavCollection.INFINITY);
		
		expect = "<blupp xmlns:D=\"DAV:\"><D:activelock> <D:locktype><D:write /></D:locktype> <D:lockscope><D:shared/></D:lockscope> <D:depth>infinity</D:depth> <D:owner>me</D:owner> <D:timeout>Infinite</D:timeout> <D:locktoken><D:href>opaquelocktoken:blaaaa</D:href></D:locktoken> </D:activelock></blupp>";
		got = "<blupp xmlns:D=\"DAV:\">"+writer.lock2xml(lock)+"</blupp>";
		
		assertTrue(xmlCompare.compare(expect,got));
		
	}

}
