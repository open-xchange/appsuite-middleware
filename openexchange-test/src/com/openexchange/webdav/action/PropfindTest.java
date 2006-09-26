package com.openexchange.webdav.action;

import java.util.Date;

import com.openexchange.test.XMLCompare;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.util.Utils;

public class PropfindTest extends ActionTestCase {
	
	//TODO: noroot
	
	public void testOneProperty() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		Date lastModified = factory.resolveResource(INDEX_HTML_URL).getLastModified();
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:getlastmodified/></D:prop></D:propfind>";
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/"+INDEX_HTML_URL+"</D:href><D:propstat><D:prop><D:getlastmodified>"+Utils.convert(lastModified)+"</D:getlastmodified></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory);
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavPropfindAction();
		action.perform(req, res);
		
		XMLCompare compare = new XMLCompare();
		compare.setCheckTextNames("getlastmodified");
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
		
	}	
	
	public void testManyProperties() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		Date lastModified = factory.resolveResource(INDEX_HTML_URL).getLastModified();
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:getlastmodified/><D:displayname/><D:resourcetype/></D:prop></D:propfind>";
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/"+INDEX_HTML_URL+"</D:href><D:propstat><D:prop><D:getlastmodified>"+Utils.convert(lastModified)+"</D:getlastmodified><D:displayname>index.html</D:displayname><D:resourcetype /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory);
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavPropfindAction();
		action.perform(req, res);
		
		XMLCompare compare = new XMLCompare();
		compare.setCheckTextNames("getlastmodified", "displayname","resourcetype" );
		
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
	}
	
	public void testPropNames() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:propname /></D:propfind>";
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><multistatus xmlns=\"DAV:\"><response><href>http://localhost/"+INDEX_HTML_URL+"</href><propstat><prop><getlastmodified /><creationdate /><resourcetype /><displayname /><getcontentlanguage /><getcontentlength /><getcontenttype /><getetag /><lockdiscovery /><supportedlock /></prop><status>HTTP/1.1 200 OK</status></propstat></response></multistatus>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory);
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavPropfindAction();
		action.perform(req, res);
		
		XMLCompare compare = new XMLCompare();
		compare.setCheckTextNames("creationdate", "resourcetype", "displayname", "getcontenttype", "getcontentlanguage", "getcontentlength", "getlastmodified", "getetag"); // FIXME: lockdiscovery und supportedlock
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
	}
	
	public void testAllProperties() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:allprop /></D:propfind>"; 
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><multistatus xmlns=\"DAV:\"><response><href>http://localhost/"+INDEX_HTML_URL+"</href><propstat><prop><getlastmodified>"+Utils.convert(resource.getLastModified())+"</getlastmodified> <creationdate>"+Utils.convert(resource.getCreationDate())+"</creationdate><resourcetype /><displayname>"+resource.getDisplayName()+"</displayname><getcontentlanguage>"+resource.getLanguage()+"</getcontentlanguage><getcontentlength>"+resource.getLength()+"</getcontentlength><getcontenttype>"+resource.getContentType()+"</getcontenttype><getetag>"+resource.getETag()+"</getetag><lockdiscovery /><supportedlock /></prop><status>HTTP/1.1 200 OK</status></propstat></response></multistatus>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory);
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavPropfindAction();
		action.perform(req, res);
		
		XMLCompare compare = new XMLCompare();
		compare.setCheckTextNames("creationdate", "resourcetype", "displayname", "getcontenttype", "getcontentlanguage", "getcontentlength", "getlastmodified", "getetag"); // FIXME: lockdiscovery und supportedlock
		
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
	}
	
	public void testDepth() throws Exception {
		final String DEVELOPMENT_URL = testCollection+"/development";
		final String PM_URL = testCollection+"/pm";
		final String INDEX_HTML_URL = testCollection+"/index.html";
		final String SITEMAP_HTML_URL = testCollection+"/sitemap.html";
		
		String testCollDispName = factory.resolveResource(testCollection).getDisplayName();
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:displayname/></D:prop></D:propfind>";
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/"+DEVELOPMENT_URL+"</D:href><D:propstat><D:prop><D:displayname>development</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory);
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(DEVELOPMENT_URL);
		req.setHeader("depth", "0");
		
		WebdavAction action = new WebdavPropfindAction();
		action.perform(req, res);
		
		XMLCompare compare = new XMLCompare();
		compare.setCheckTextNames("displayname");
		
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
		
		expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost"+testCollection+"</D:href><D:propstat><D:prop><D:displayname>"+testCollDispName+"</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/"+DEVELOPMENT_URL+"</D:href><D:propstat><D:prop><D:displayname>development</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/"+PM_URL+"</D:href><D:propstat><D:prop><D:displayname>pm</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/"+INDEX_HTML_URL+"</D:href><D:propstat><D:prop><D:displayname>index.html</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/"+SITEMAP_HTML_URL+"</D:href><D:propstat><D:prop><D:displayname>sitemap.html</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
		res = new MockWebdavResponse();
		
		req = new MockWebdavRequest(factory);
		
		req.setBodyAsString(body);
		req.setHeader("depth","1");
		req.setUrl(testCollection);
		
		action.perform(req, res);
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
		
		
	}
	
	
}
