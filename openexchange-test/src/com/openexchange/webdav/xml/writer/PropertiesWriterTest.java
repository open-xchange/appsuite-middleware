package com.openexchange.webdav.xml.writer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.jdom.Element;
import org.jdom.Namespace;

import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.impl.DummyResourceManager;
import com.openexchange.webdav.protocol.util.Utils;
import com.openexchange.webdav.xml.resources.PropfindAllPropsMarshaller;
import com.openexchange.webdav.xml.resources.PropfindPropNamesMarshaller;
import com.openexchange.webdav.xml.resources.PropfindResponseMarshaller;

public class PropertiesWriterTest extends TestCase {
	
	private static final Namespace DAV_NS = Namespace.getNamespace("DAV:");

	private String testCollection = null;
	
	public void setUp() throws Exception {
		Thread.sleep(1);
		testCollection = "testCollection"+System.currentTimeMillis()+"/";
		DummyResourceManager.getInstance().resolveCollection(testCollection).create();
	}
	
	public void tearDown() throws Exception {
		DummyResourceManager.getInstance().resolveCollection(testCollection).delete();	
	}
	
	public void testBasic() throws Exception {
		WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.create();
		
		PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("http://test.invalid");
		marshaller
			.addProperty("DAV:","getlastmodified");
		
		Element response = marshaller.marshal(resource).get(0);
		
		assertHref(response, "http://test.invalid/"+testCollection+"test.txt");
		
		int count = 0;
		for(Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat"))
				continue;
			assertStatus(element, "HTTP/1.1 200 OK");
			assertProp(element, DAV_NS, "getlastmodified", Utils.convert(resource.getLastModified()));
			count++;
		}
		
		assertEquals(1, count);
	}
	
	public void testManyProperties() throws Exception {
		WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.setDisplayName("myDisplayName");
		resource.create();
		
		PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("http://test.invalid");
		marshaller
			.addProperty("DAV:","getlastmodified")
			.addProperty("DAV:", "displayname");
		Element response = marshaller.marshal(resource).get(0);
		
		assertHref(response, "http://test.invalid/"+testCollection+"test.txt");
		
		int count = 0;
		for(Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat"))
				continue;
			assertStatus(element, "HTTP/1.1 200 OK");
			assertProp(element, DAV_NS, "getlastmodified", Utils.convert(resource.getLastModified()));
			assertProp(element, DAV_NS, "displayname", "myDisplayName");	
			count++;
		}
		
		assertEquals(1, count);
		
	}

	public void testPropertyNames() throws Exception {
		WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.create();
		
		
		PropfindPropNamesMarshaller marshaller = new PropfindPropNamesMarshaller("http://test.invalid");
		Element response = marshaller.marshal(resource).get(0);
		
		assertHref(response, "http://test.invalid/"+testCollection+"test.txt");
		
		Set<String> allProps = new HashSet<String>();
		
		for(Property p : new Protocol().getKnownProperties()) { allProps.add(p.getName()); }
		
		for(Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat"))
				continue;
			assertStatus(element, "HTTP/1.1 200 OK");
			Element prop = element.getChild("prop", DAV_NS);
			for(Element child : (List<Element>) prop.getChildren()) {
				assertTrue("Didn't expect "+child.getName(), allProps.remove(child.getName()));
			}
		}
		assertTrue(allProps.toString(), allProps.isEmpty());
	}

	public void testAllProperties() throws Exception {
		WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.create();
		
		
		PropfindAllPropsMarshaller marshaller = new PropfindAllPropsMarshaller("http://test.invalid");
		Element response = marshaller.marshal(resource).get(0);
		
		assertHref(response, "http://test.invalid/"+testCollection+"test.txt");
		
		Set<String> allProps = new HashSet<String>();
		
		for(Property p : new Protocol().getKnownProperties()) { allProps.add(p.getName()); }
		
		for(Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat"))
				continue;
			assertStatus(element, "HTTP/1.1 200 OK");
			Element prop = element.getChild("prop", DAV_NS);
			for(Element child : (List<Element>) prop.getChildren()) {
				assertTrue(allProps.remove(child.getName()));
			}
		}
		assertTrue(allProps.toString(), allProps.isEmpty());
	}
	
	public void testEmptyProperty() throws Exception {
		WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.create();
		
		PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("http://test.invalid");
		marshaller
			.addProperty("DAV:","resourcetype");
		
		Element response = marshaller.marshal(resource).get(0);
		
		assertHref(response, "http://test.invalid/"+testCollection+"test.txt");
		
		int count = 0;
		for(Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat"))
				continue;
			assertStatus(element, "HTTP/1.1 200 OK");
			assertProp(element, DAV_NS, "resourcetype", "");
			count++;
		}
	}
	
	public void testDangerousChars() throws Exception {
		WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.setDisplayName("<&>");
		resource.create();
		
		PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("http://test.invalid");
		marshaller
			.addProperty("DAV:","displayname");
		
		Element response = marshaller.marshal(resource).get(0);
		
		assertHref(response, "http://test.invalid/"+testCollection+"test.txt");
		
		int count = 0;
		for(Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat"))
				continue;
			assertStatus(element, "HTTP/1.1 200 OK");
			assertProp(element, DAV_NS, "displayname", "<&>");
			count++;
		}
	}

	public void testXMLProperty() throws Exception {
		//TODO
	}
		
	public void testDepth() throws Exception {
		//TODO
	}
	
	private static final void assertHref(Element element, String uri) {
		assertEquals(uri, element.getChild("href", DAV_NS).getText());
	}
	
	private static final void assertStatus(Element element, String status) {
		assertEquals(status, element.getChild("status", DAV_NS).getText());
	}
	
	private static final void assertProp(Element element, Namespace namespace, String name, String content) {
		Element prop = element.getChild("prop", DAV_NS);
		Element child = prop.getChild(name, namespace);
		if(null != child) {
			assertEquals(content, child.getText());
			return;
		}
		fail("Couldn't find prop: "+name+" in namespace "+namespace.getURI());
	}
}
