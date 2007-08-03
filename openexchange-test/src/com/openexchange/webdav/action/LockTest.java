package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import org.jdom.Namespace;

import com.openexchange.test.XMLCompare;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavResource;

public class LockTest extends ActionTestCase {
	
	private static final Namespace TEST_NS = Namespace.getNamespace("http://www.open-xchange.com/namespace/webdav-test");
	
	public void testLock() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner></lockinfo>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Timeout", "infinite");
		
		WebdavAction action = new WebdavLockAction();
		action.perform(req, res);
		assertEquals(HttpServletResponse.SC_OK, res.getStatus());
		
		// LockToken Header
		assertEquals(1,factory.resolveResource(INDEX_HTML_URL).getLocks().size());
		WebdavLock lock = factory.resolveResource(INDEX_HTML_URL).getLocks().get(0);
		assertNotNull(lock.getToken());
		String lockToken = lock.getToken();
		
		assertEquals(lockToken, res.getHeader("Lock-Token"));
		
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><prop xmlns=\"DAV:\"><lockdiscovery><activelock><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner><depth>0</depth><locktoken><href>"+lockToken+"</href></locktoken><timeout></timeout></activelock></lockdiscovery></prop>";
		
		XMLCompare compare = new XMLCompare();
		compare.setCheckTextNames("owner","locktoken");
		
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
		
		WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
		r.unlock(lockToken);
		r.save();
		
	}
	
	public void testLockOwnerInXML() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner><shortName xmlns=\""+TEST_NS.getURI()+"\">me</shortName></owner></lockinfo>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Timeout", "infinite");
		
		WebdavAction action = new WebdavLockAction();
		action.perform(req, res);
		
		assertEquals(HttpServletResponse.SC_OK, res.getStatus());
		
		// LockToken Header
		assertEquals(1,factory.resolveResource(INDEX_HTML_URL).getLocks().size());
		WebdavLock lock = factory.resolveResource(INDEX_HTML_URL).getLocks().get(0);
		assertNotNull(lock.getToken());
		String lockToken = lock.getToken();
		
		assertEquals(lockToken, res.getHeader("Lock-Token"));
		
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><prop xmlns=\"DAV:\"><lockdiscovery><activelock><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner><shortName xmlns=\""+TEST_NS.getURI()+"\">me</shortName></owner><depth>0</depth><locktoken><href>"+lockToken+"</href></locktoken><timeout>Infinite</timeout></activelock></lockdiscovery></prop>";
		
		XMLCompare compare = new XMLCompare();
		compare.setCheckTextNames("owner","locktoken","timeout","shortName");
		
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
		
		WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
		r.unlock(lockToken);
		r.save();
	
	}
	
	public void testDepth() throws Exception {
		final String GUI_URL = testCollection+"/development/gui";
		
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner></lockinfo>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(GUI_URL);
		req.setHeader("Timeout", "infinite");
		req.setHeader("Depth","infinity");
		
		WebdavAction action = new WebdavLockAction();
		action.perform(req, res);
		assertEquals(HttpServletResponse.SC_OK, res.getStatus());
		
		WebdavResource resource = factory.resolveResource(GUI_URL);
		
		assertEquals(1,resource.getLocks().size());
		WebdavLock lock = resource.getLocks().get(0);
		assertEquals(WebdavCollection.INFINITY, lock.getDepth());
		
		resource.unlock(lock.getToken());
		
		req = new MockWebdavRequest(factory, "http://localhost/");
		res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(GUI_URL);
		req.setHeader("Timeout", "infinite");
		req.setHeader("Depth","1");
		
		action.perform(req, res);
		
		assertEquals(1,resource.getLocks().size());
		lock = resource.getLocks().get(0);
		assertEquals(1, lock.getDepth());
	}
	
	public void testTimeoutInSeconds() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner></lockinfo>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Timeout", "Second-3600");
		
		WebdavAction action = new WebdavLockAction();
		action.perform(req, res);
		
		assertEquals(HttpServletResponse.SC_OK, res.getStatus());
		
		// LockToken Header
		assertEquals(1,factory.resolveResource(INDEX_HTML_URL).getLocks().size());
		WebdavLock lock = factory.resolveResource(INDEX_HTML_URL).getLocks().get(0);
		
		assertTrue(3600-lock.getTimeout()<100);
	
		WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
		String lockToken = r.getLocks().iterator().next().getToken();
		r.unlock(lockToken);
		r.save();
	
	}
	
	public void testLockNull() throws Exception {
		final String LOCK_HTML_URL = testCollection+"/lock.html";
		
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner></lockinfo>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(LOCK_HTML_URL);
		req.setHeader("Timeout", "infinite");
		
		WebdavAction action = new WebdavLockAction();
		action.perform(req, res);
		
		assertEquals(HttpServletResponse.SC_OK, res.getStatus());
		
		WebdavResource resource = factory.resolveResource(LOCK_HTML_URL);
		assertTrue(resource.isLockNull());
		
		WebdavLock lock = resource.getLocks().get(0);
		String lockToken = lock.getToken();
		
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><prop xmlns=\"DAV:\"><lockdiscovery><activelock><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner><depth>0</depth><locktoken><href>"+lockToken+"</href></locktoken><timeout></timeout></activelock></lockdiscovery></prop>";
		
		XMLCompare compare = new XMLCompare();
		compare.setCheckTextNames("owner","locktoken");
		
		System.out.println(res.getResponseBodyAsString());
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
		
	}
}
