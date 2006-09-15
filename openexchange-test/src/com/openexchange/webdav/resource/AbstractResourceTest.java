package com.openexchange.webdav.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.openexchange.webdav.resource.Protocol.Property;
import com.openexchange.webdav.resource.util.PropertySwitch;

public abstract class AbstractResourceTest extends TestCase implements PropertySwitch {
	
public static final int SKEW = 1000;
	
	protected WebdavFactory resourceManager;
	protected String testCollection = "/testCollection";
	
	protected List<WebdavResource> clean = new ArrayList<WebdavResource>();


	protected abstract WebdavFactory getWebdavFactory() throws Exception;
	protected abstract List<Property> getPropertiesToTest() throws Exception;
	protected abstract WebdavResource createResource() throws Exception;

	
	public void setUp() throws Exception{
		resourceManager = getWebdavFactory();
		
		WebdavResource resource = resourceManager.resolveCollection(testCollection);
		assertTrue(resource.isCollection());
		resource.create();
		clean.add(resource);
	}
	

	public void tearDown() throws Exception {
		Collections.reverse(clean);
		for(WebdavResource res : clean) {
			res.delete();
		}
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
		clean.remove(resource);
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
			assertTrue(nameSet.remove(res.getDisplayName()));
		}
		assertTrue(nameSet.toString(),nameSet.isEmpty());
	}
	
}
