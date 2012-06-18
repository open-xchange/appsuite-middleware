package com.openexchange.webdav.xml.writer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.impl.DummyResourceManager;
import com.openexchange.webdav.protocol.util.Utils;
import com.openexchange.webdav.xml.resources.PropfindAllPropsMarshaller;
import com.openexchange.webdav.xml.resources.PropfindPropNamesMarshaller;
import com.openexchange.webdav.xml.resources.PropfindResponseMarshaller;

public class PropertiesWriterTest extends TestCase {

	private static final Namespace DAV_NS = Namespace.getNamespace("DAV:");
	private static final Namespace TEST_NS = Namespace.getNamespace("http://www.open-xchange.com/namespace/webdav-test");

	private String testCollection = null;

	@Override
	public void setUp() throws Exception {
		Thread.sleep(1);
		testCollection = "testCollection"+System.currentTimeMillis()+"/";
		DummyResourceManager.getInstance().resolveCollection(testCollection).create();
	}

	@Override
	public void tearDown() throws Exception {
		DummyResourceManager.getInstance().resolveCollection(testCollection).delete();
	}

	public void testBasic() throws Exception {
		final WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.create();

		final PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("","UTF-8");
		marshaller
			.addProperty("DAV:","getlastmodified");

		final Element response = marshaller.marshal(resource).get(0);

		assertHref(response, "/"+testCollection+"test.txt");

		int count = 0;
		for(final Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat")) {
				continue;
			}
			assertStatus(element, "HTTP/1.1 200 OK");
			assertProp(element, DAV_NS, "getlastmodified", Utils.convert(resource.getLastModified()));
			count++;
		}

		assertEquals(1, count);
	}

	public void testManyProperties() throws Exception {
		final WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.setDisplayName("myDisplayName");
		resource.create();

		final PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("","UTF-8");
		marshaller
			.addProperty("DAV:","getlastmodified")
			.addProperty("DAV:", "displayname");
		final Element response = marshaller.marshal(resource).get(0);

		assertHref(response, "/"+testCollection+"test.txt");

		int count = 0;
		for(final Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat")) {
				continue;
			}
			assertStatus(element, "HTTP/1.1 200 OK");
			assertProp(element, DAV_NS, "getlastmodified", Utils.convert(resource.getLastModified()));
			assertProp(element, DAV_NS, "displayname", "myDisplayName");
			count++;
		}

		assertEquals(1, count);

	}

	public void testPropertyNames() throws Exception {
		final WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.create();


		final PropfindPropNamesMarshaller marshaller = new PropfindPropNamesMarshaller("","UTF-8");
		final Element response = marshaller.marshal(resource).get(0);

		assertHref(response, "/"+testCollection+"test.txt");

		final Set<String> allProps = new HashSet<String>();

		for(final Property p : new Protocol().getKnownProperties()) { allProps.add(p.getName()); }

		for(final Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat")) {
				continue;
			}
			assertStatus(element, "HTTP/1.1 200 OK");
			final Element prop = element.getChild("prop", DAV_NS);
			for(final Element child : (List<Element>) prop.getChildren()) {
				assertTrue("Didn't expect "+child.getName(), allProps.remove(child.getName()));
			}
		}
		assertTrue(allProps.toString(), allProps.isEmpty());
	}

	public void testAllProperties() throws Exception {
		final WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.create();


		final PropfindAllPropsMarshaller marshaller = new PropfindAllPropsMarshaller("","UTF-8");
		final Element response = marshaller.marshal(resource).get(0);

		assertHref(response, "/"+testCollection+"test.txt");

		final Set<String> allProps = new HashSet<String>();

		for(final Property p : new Protocol().getKnownProperties()) { allProps.add(p.getName()); }

		for(final Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat")) {
				continue;
			}
			assertStatus(element, "HTTP/1.1 200 OK");
			final Element prop = element.getChild("prop", DAV_NS);
			for(final Element child : (List<Element>) prop.getChildren()) {
				assertTrue(allProps.remove(child.getName()));
			}
		}
		assertTrue(allProps.toString(), allProps.isEmpty());
	}

	public void testEmptyProperty() throws Exception {
		final WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.create();

		final PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("","UTF-8");
		marshaller
			.addProperty("DAV:","resourcetype");

		final Element response = marshaller.marshal(resource).get(0);

		assertHref(response, "/"+testCollection+"test.txt");

		for(final Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat")) {
				continue;
			}
			assertStatus(element, "HTTP/1.1 200 OK");
			assertProp(element, DAV_NS, "resourcetype", "");
		}
	}

	public void testDangerousChars() throws Exception {
		final WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.setDisplayName("<&>");
		resource.create();

		final PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("","UTF-8");
		marshaller
			.addProperty("DAV:","displayname");

		final Element response = marshaller.marshal(resource).get(0);

		assertHref(response, "/"+testCollection+"test.txt");

		for(final Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat")) {
				continue;
			}
			assertStatus(element, "HTTP/1.1 200 OK");
			assertProp(element, DAV_NS, "displayname", "<&>");
		}
	}

	public void testXMLProperty() throws Exception {
		final WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		final WebdavProperty property = new WebdavProperty();
		property.setNamespace(TEST_NS.getURI());
		property.setName("test");
		property.setValue("<quark xmlns=\"http://www.open-xchange.com/namespace/webdav-test\"> In the left corner: The incredible Tessssssst Vallllhhhhuuuuuueeeee!</quark>");
		property.setXML(true);
		resource.putProperty(property);
		resource.create();

		final PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("","UTF-8");
		marshaller
			.addProperty(TEST_NS.getURI(),"test");

		Element response = marshaller.marshal(resource).get(0);

		assertHref(response, "/"+testCollection+"test.txt");

		for(final Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat")) {
				continue;
			}
			assertStatus(element, "HTTP/1.1 200 OK");
			final Element prop = element.getChild("prop", DAV_NS);
			final Element child = prop.getChild("test", TEST_NS);
			final Element quark = child.getChild("quark",TEST_NS);
			assertEquals(" In the left corner: The incredible Tessssssst Vallllhhhhuuuuuueeeee!", quark.getText());
		}

		property.setValue("<quark xmlns=\"http://www.open-xchange.com/namespace/webdav-test\"> In the left corner: The incredible Tessssssst Vallllhhhhuuuuuueeeee!</quark><gnurk xmlns=\"http://www.open-xchange.com/namespace/webdav-test\"> In the right corner: The incredible other Tessssssst Vallllhhhhuuuuuueeeee!</gnurk>");
		resource.putProperty(property);
		resource.save();

		response = marshaller.marshal(resource).get(0);

		assertHref(response, "/"+testCollection+"test.txt");

		for(final Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat")) {
				continue;
			}
			assertStatus(element, "HTTP/1.1 200 OK");
			final Element prop = element.getChild("prop", DAV_NS);
			final Element child = prop.getChild("test", TEST_NS);
			final Element quark = child.getChild("quark",TEST_NS);
			assertEquals(" In the left corner: The incredible Tessssssst Vallllhhhhuuuuuueeeee!", quark.getText());
			final Element gnurk = child.getChild("gnurk",TEST_NS);
			assertEquals(" In the right corner: The incredible other Tessssssst Vallllhhhhuuuuuueeeee!", gnurk.getText());
		}
	}

	public void testNotExists() throws Exception {
		final WebdavResource resource = DummyResourceManager.getInstance().resolveResource(testCollection +"test.txt");
		resource.setDisplayName("myDisplayName");
		resource.create();

		final PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller("","UTF-8");
		marshaller
			.addProperty("DAV:","getlastmodified")
			.addProperty("DAV:", "displayname")
			.addProperty("OX:", "notExist");
		final Element response = marshaller.marshal(resource).get(0);

		assertHref(response, "/"+testCollection+"test.txt");

		int count = 0;
		int status = 0;
		for(final Element element : (List<Element>)response.getChildren()) {
			if(!element.getName().equals("propstat")) {
				continue;
			}
			if(element.getChild("status", DAV_NS).getText().equals("HTTP/1.1 200 OK")) {
				assertStatus(element, "HTTP/1.1 200 OK");
				assertProp(element, DAV_NS, "getlastmodified", Utils.convert(resource.getLastModified()));
				count++;
				status += 2;
			} else if (element.getChild("status", DAV_NS).getText().equals("HTTP/1.1 404 NOT FOUND")) {
				count++;
				status += 1;
			}
		}
		assertEquals(2, count);
		assertTrue(status == 3);
	}

	private static final void assertHref(final Element element, final String uri) {
		assertEquals(uri, element.getChild("href", DAV_NS).getText());
	}

	private static final void assertStatus(final Element element, final String status) {
		assertEquals(status, element.getChild("status", DAV_NS).getText());
	}

	private static final void assertProp(final Element element, final Namespace namespace, final String name, final String content) {
		final Element prop = element.getChild("prop", DAV_NS);
		final Element child = prop.getChild(name, namespace);
		if(null != child) {
			assertEquals(content, child.getText());
			return;
		}
		fail("Couldn't find prop: "+name+" in namespace "+namespace.getURI());
	}
}
