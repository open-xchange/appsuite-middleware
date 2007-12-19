package com.openexchange.webdav.protocol;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.util.PropertySwitch;

public abstract class AbstractResourceTest extends TestCase implements PropertySwitch {
	
public static final int SKEW = 1000;
	
	protected WebdavFactory resourceManager;
	
	protected WebdavPath testCollection = new WebdavPath("testCollection"+Math.random());
	
	protected abstract WebdavFactory getWebdavFactory() throws Exception;
	protected abstract List<Property> getPropertiesToTest() throws Exception;
	protected abstract WebdavResource createResource() throws Exception;

	
	public void setUp() throws Exception{
		resourceManager = getWebdavFactory();
		
		WebdavResource resource = resourceManager.resolveCollection(testCollection);
		assertTrue(resource.isCollection());
		resource.create();
	}
	

	public void tearDown() throws Exception {
		resourceManager.resolveCollection(testCollection).delete();
	}
	
	public void testProperties() throws Exception{
		WebdavResource res = createResource();
		WebdavProperty prop = new WebdavProperty();
		prop.setNamespace("OXTest");
		prop.setName("myvalue");
		prop.setLanguage("en");
		prop.setValue("testValue");
		
		res.putProperty(prop);
		res.save();
	
		res = resourceManager.resolveResource(res.getUrl());
		
		assertEquals(prop, res.getProperty("OXTest","myvalue"));
		
		prop = new WebdavProperty();
		prop.setNamespace("OXTest");
		prop.setName("myvalue");
		prop.setLanguage("en");
		prop.setValue("testValue2");
		
		res.putProperty(prop);
		res.save();
		
		res = resourceManager.resolveResource(res.getUrl());
		
		assertEquals(prop, res.getProperty("OXTest","myvalue"));
		
		res.removeProperty("OXTest","myvalue");
		res.save();
		
		assertNull(res.getProperty("OXTest","myvalue"));
	}
	
	public void testMandatoryProperties() throws Exception {
		List<Property> mandatory = getPropertiesToTest();
		for(Property prop : mandatory) {
			prop.doSwitch(this);
		}
	}	
	
	public void testCreateLoadAndDelete() throws Exception {
		WebdavResource resource = createResource();
		assertTrue(resource.exists());
		resource.delete();
		resource = resourceManager.resolveResource(resource.getUrl());
		assertFalse(resource.exists());
	}
	
	
	
	public static void assertEquals(Date d1, Date d2, int skew){
		long l1 = d1.getTime();
		long l2 = d2.getTime();
		long diff = (l1 < l2) ? l2 - l1 : l1 - l2;
		if(diff > skew)
			assertEquals(l1,l2);
		assertTrue(true);
	}
	
	public static void assertResources(Iterable<WebdavResource> resources, String...displayNames) throws WebdavException{
		//assertEquals(displayNames.length, resources.size());
		
		Set<String> nameSet = new HashSet<String>(Arrays.asList(displayNames));
		
		for(WebdavResource res : resources) {
			assertTrue(res.getDisplayName()+" not expected",nameSet.remove(res.getDisplayName()));
		}
		assertTrue(nameSet.toString(),nameSet.isEmpty());
	}
	
	public static void assertOptions(Iterable<Protocol.WEBDAV_METHOD> expect, Protocol.WEBDAV_METHOD...methods) throws WebdavException{
		//assertEquals(displayNames.length, resources.size());
		
		Set<Protocol.WEBDAV_METHOD> methodSet = new HashSet<Protocol.WEBDAV_METHOD>(Arrays.asList(methods));
		
		for(Protocol.WEBDAV_METHOD method : expect) {
			assertTrue(method+" not expected",methodSet.remove(method));
		}
		assertTrue(methodSet.toString(),methodSet.isEmpty());
	}
	
	public void throwEx(Exception x) throws WebdavException {
		throw new WebdavException(x.getMessage(), x, new WebdavPath() ,500 );
	}
}
