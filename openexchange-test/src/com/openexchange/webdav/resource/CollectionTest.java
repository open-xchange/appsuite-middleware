package com.openexchange.webdav.resource;

import java.io.ByteArrayInputStream;
import java.util.List;

import com.openexchange.webdav.resource.Protocol.Property;


public class CollectionTest extends ResourceTest {
	
	@Override
	public void testBody() throws Exception {
		WebdavCollection coll = createResource().toCollection();
		
		String content = "Hallo Welt!";
		byte[] bytes = content.getBytes("UTF-8");
		
		try {
			coll.putBody(new ByteArrayInputStream(bytes));
			fail("Collections shouldn't accept bodies");
		} catch (WebdavException x) {
			assertTrue(true);
		}
	}
	
	protected void createStructure(WebdavCollection coll) throws WebdavException {
		WebdavResource res = coll.resolveResource("index.html");
		res.create();
		clean.add(res);
		
		res = coll.resolveResource("sitemap.html");
		res.create();
		clean.add(res);
		
		res = coll.resolveCollection("development");
		res.create();
		clean.add(res);
		
		res = res.toCollection().resolveCollection("gui");
		res.create();
		clean.add(res);
		
		res = resourceManager.resolveCollection(coll.getUrl()+"/pm");
		res.create();
		clean.add(res);
		
		res = coll.resolveCollection("pm").resolveResource("index2.html");
		res.create();
		clean.add(res);
	}
	
	public void testChildren() throws Exception {
		WebdavCollection coll = createResource().toCollection();
		createStructure(coll);
		
		List<WebdavResource> children = coll.getChildren();
		assertResources(children, "index.html", "sitemap.html", "development", "pm");
		
		WebdavCollection dev = coll.resolveCollection("development");
		children = dev.getChildren();
		assertResources(children,"gui");
		
		WebdavCollection pm = coll.resolveCollection("pm");
		children = pm.getChildren();
		assertResources(children,"index2.html");
		
		WebdavResource res = pm.resolveResource("index2.html");
		res.delete();
		clean.remove(res);
		
		children = pm.getChildren();
		assertResources(children);
		
		
	}

	public void testIterate() throws Exception {
		WebdavCollection coll = createResource().toCollection();
		createStructure(coll);
		assertResources(coll,"index.html", "sitemap.html", "development", "pm","gui","index2.html"); // Note: Children ONLY
	}
	
	public void testVisit() throws Exception {
		WebdavCollection coll = createResource().toCollection();
		createStructure(coll);
		//TODO
	}
	
	public void testDelete() throws Exception {
		//TODO
	}
	
	public void testMove() throws Exception {
		//TODO
	}
	
	public void testCopy() throws Exception {
		//TODO
	}

	@Override
	protected WebdavResource createResource() throws WebdavException{
		WebdavResource resource = FACTORY.resolveCollection(testCollection+"/testResource"+Math.random());
		clean.add(resource);
		assertFalse(resource.exists());
		resource.create();
		resource = resourceManager.resolveResource(resource.getUrl());
		assertTrue(resource.exists());
		return resource;

	}

	@Override
	protected List<Property> getPropertiesToTest() {
		return FACTORY.getProtocol().VALUES;
	}

	@Override
	public Object resourceType() throws WebdavException {
		WebdavCollection coll = createResource().toCollection();
		assertEquals(Protocol.COLLECTION, coll.getResourceType());
		assertEquals(coll.getResourceType(), coll.getProperty("DAV", "resourcetype").getValue());
		return null;
	}

	@Override
	public Object contentLanguage() throws WebdavException {
		WebdavResource res = createResource();
		String defaultLanguage = null;
		assertEquals(res.getLanguage(), res.getProperty("DAV", "getcontentlanguage"));
		assertEquals(defaultLanguage, res.getLanguage());
		
		try {
			res.setLanguage("de");
			fail("Could update language");
		} catch (WebdavException x) {
			assertTrue(true);
		}
		
		WebdavProperty prop = Protocol.GETCONTENTLANGUAGE_LITERAL.getWebdavProperty();
		prop.setValue("de");
		
		try {
			res.putProperty(prop);
			fail("Could update language");
		} catch (WebdavException x) {
			assertTrue(true);
		}
		
		return null;
	}

	@Override
	public Object contentLength() throws WebdavException {
		WebdavResource res = createResource();
		assertEquals(res.getLength(), res.getProperty("DAV", "getcontentlength"));
		assertEquals(null, res.getLength());
		
		
		try {
			res.setLength(23l);
			fail("Could update length");
		} catch (WebdavException x) {
			assertTrue(true);
		}
		
		WebdavProperty prop = Protocol.GETCONTENTLENGTH_LITERAL.getWebdavProperty();
		prop.setValue("2");
		
		try {
			res.putProperty(prop);
			fail("Could update length");
		} catch (WebdavException x) {
			assertTrue(true);
		}

		return null;
	}

	@Override
	public Object etag() throws WebdavException{
		WebdavResource res = createResource();
		assertEquals(res.getETag(), res.getProperty("DAV", "getetag"));
		assertEquals(null, res.getETag());
		
		return null;
	}
	
	@Override
	public Object contentType() throws WebdavException {
		WebdavResource res = createResource();
		try {
			res.setContentType("text/plain");
			fail("Could update content type");
		} catch (WebdavException x) {
			assertTrue(true);
		}
		
		WebdavProperty prop = Protocol.GETCONTENTTYPE_LITERAL.getWebdavProperty();
		prop.setValue("text/plain");
		
		try {
			res.putProperty(prop);
			fail("Could update content type");
		} catch (WebdavException x) {
			assertTrue(true);
		}

		return null;
	}
	
	
	
}
