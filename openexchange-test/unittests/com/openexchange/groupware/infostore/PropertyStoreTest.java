package com.openexchange.groupware.infostore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.infostore.webdav.PropertyStore;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.webdav.protocol.WebdavProperty;

public class PropertyStoreTest extends TestCase{
	private PropertyStore propertyStore;
	
	protected List<Integer> clean = new ArrayList<Integer>();

	protected int entity = 23;
	protected int entity2 = 42;
	
	private Context ctx = new ContextImpl(1);
	private User user = null;
	private UserConfiguration userConfig = null;
	
	
	public void setUp() throws Exception {
        super.setUp();
		Init.startServer();
		this.propertyStore = new PropertyStoreImpl(new DBPoolProvider(), "infostore_property");
		
		propertyStore.startTransaction();
		
		WebdavProperty property = new WebdavProperty();
		property.setName("testprop");
		property.setNamespace("OX:");
		property.setValue("I'm the value");
		
		WebdavProperty property2 = new WebdavProperty();
		property2.setName("testprop2");
		property2.setNamespace("OX:");
		property2.setValue("<bla>I'm the value2</bla>");
		property2.setXML(true);
		
		
		propertyStore.saveProperties(entity, Arrays.asList(property, property2), ctx);
		clean.add(entity);
		
		propertyStore.saveProperties(entity2, Arrays.asList(property, property2), ctx);
		clean.add(entity2);
	}

	public void tearDown() throws Exception {
		propertyStore.removeAll(clean, ctx);
		clean.clear();
		propertyStore.commit();
		propertyStore.finish();
        Init.stopServer();
        super.tearDown();
	}
	
	public void testLoadSpecificForOneEntity() throws Exception {
		List<WebdavProperty> props = propertyStore.loadProperties(entity, p("OX:","testprop"), ctx);
		assertEquals(1, props.size());
		assertEquals("I'm the value", props.get(0).getValue());
		assertFalse(props.get(0).isXML());
	}

	public void testLoadSpecificForManyEntities() throws Exception {
		Map<Integer, List<WebdavProperty>> props = propertyStore.loadProperties(Arrays.asList(entity, entity2), p("OX:","testprop"), ctx);
		
		assertEquals(2,props.keySet().size());
		
		List<WebdavProperty> p = props.get(entity);
		assertNotNull(p);
		assertEquals(1, p.size());
		assertEquals("I'm the value", p.get(0).getValue());
		assertFalse(p.get(0).isXML());
		
		p = props.get(entity2);
		assertNotNull(p);
		assertEquals(1, p.size());
		assertEquals("I'm the value", p.get(0).getValue());
		assertFalse(p.get(0).isXML());
		
	}
	
	public void testLoadAllForOneEntity() throws Exception {
		List<WebdavProperty> props = propertyStore.loadAllProperties(entity, ctx);
		checkProps(props);
	}

	public void testLoadAllForManyEntities() throws Exception {
		Map<Integer, List<WebdavProperty>> props = propertyStore.loadAllProperties(Arrays.asList(entity, entity2), ctx);
		
		assertEquals(2,props.keySet().size());
		List<WebdavProperty> p = props.get(entity);
		checkProps(p);
		
		p = props.get(entity2);
		checkProps(p);
	}
	
	public void testDeleteSpecificForOneEntity() throws Exception {
		propertyStore.removeProperties(entity, p("OX:", "testprop"), ctx);
		List<WebdavProperty> props = propertyStore.loadAllProperties(entity, ctx);
		assertEquals(1,props.size());
		assertEquals("testprop2", props.get(0).getName());
	}
	
	public void testDeleteAllForOneEntity() throws Exception {
		propertyStore.removeAll(entity, ctx);
		List<WebdavProperty> props = propertyStore.loadAllProperties(entity, ctx);
		assertTrue(props.isEmpty());
	}
	
	private void checkProps(List<WebdavProperty> props) {
		assertNotNull(props);
		assertEquals(2,props.size());
		int mask = 0;
		for(WebdavProperty prop : props) {
			if(prop.getName().equals("testprop")) {
				mask += 1;
				assertEquals("I'm the value", prop.getValue());
				assertFalse(prop.isXML());
			} else if (prop.getName().equals("testprop2")) {
				mask += 2;
				assertEquals("<bla>I'm the value2</bla>", prop.getValue());
				assertTrue(prop.isXML());
			}
		}
		
		assertEquals(3,mask);
	}
	
	private List<WebdavProperty> p(String...props) {
		List<WebdavProperty> propList = new ArrayList<WebdavProperty>();
		for(int i = 0; i < props.length; i++) {
			String namespace = props[i++];
			String name = props[i];
			propList.add(new WebdavProperty(namespace,name));
		}
		return propList;
	}
}
