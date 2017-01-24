
package com.openexchange.webdav.protocol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.tools.collections.Collector;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.collections.OXCollections;
import com.openexchange.webdav.protocol.Protocol.Property;

public class CollectionTest extends ResourceTest {

    public static final String INDEX_HTML = "<html><head /><body>Company Site</body></html>";
    public static final String SITEMAP_HTML = "<html><head /><body>You are here</body></html>";
    public static final String INDEX3_HTML = "<html><head /><body>GUI Site</body></html>";
    public static final String INDEX2_HTML = "<html><head /><body>PM Site</body></html>";

    protected List<WebdavPath> clean = new ArrayList<WebdavPath>();

    @After
    public void tearDown() throws Exception {
        for (final WebdavPath path : clean) {
            FACTORY.resolveResource(path).delete();
        }
        super.tearDown();
    }

    @Test
    public void testBody() throws Exception {
        final WebdavCollection coll = createResource().toCollection();

        final String content = "Hallo Welt!";
        final byte[] bytes = content.getBytes(com.openexchange.java.Charsets.UTF_8);

        try {
            coll.putBody(new ByteArrayInputStream(bytes));
            fail("Collections shouldn't accept bodies");
        } catch (final WebdavProtocolException x) {
            Assert.assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, x.getStatus());
        }
    }

    public static void createStructure(final WebdavCollection coll, final WebdavFactory factory) throws OXException, UnsupportedEncodingException {
        final String content = "Hallo Welt!";
        final byte[] bytes = content.getBytes(com.openexchange.java.Charsets.UTF_8);

        WebdavResource res = coll.resolveResource(new WebdavPath("index.html"));
        res.putBody(new ByteArrayInputStream(INDEX_HTML.getBytes(com.openexchange.java.Charsets.UTF_8)));
        res.setContentType("text/html");
        res.putBodyAndGuessLength(new ByteArrayInputStream(bytes));
        res.create();

        res = coll.resolveResource(new WebdavPath("sitemap.html"));
        res.putBody(new ByteArrayInputStream(SITEMAP_HTML.getBytes(com.openexchange.java.Charsets.UTF_8)));
        res.setContentType("text/html");
        res.setLength((long) SITEMAP_HTML.getBytes(com.openexchange.java.Charsets.UTF_8).length);
        res.create();

        res = coll.resolveCollection(new WebdavPath("development"));
        res.create();

        res = res.toCollection().resolveCollection(new WebdavPath("gui"));
        res.create();

        res = res.toCollection().resolveResource(new WebdavPath("index3.html"));
        res.putBody(new ByteArrayInputStream(INDEX3_HTML.getBytes(com.openexchange.java.Charsets.UTF_8)));
        res.setContentType("text/html");
        res.setLength((long) INDEX3_HTML.getBytes(com.openexchange.java.Charsets.UTF_8).length);
        res.create();

        res = factory.resolveCollection(coll.getUrl() + "/pm");
        res.create();

        res = coll.resolveCollection(new WebdavPath("pm")).resolveResource(new WebdavPath("index2.html"));
        res.putBody(new ByteArrayInputStream(INDEX2_HTML.getBytes(com.openexchange.java.Charsets.UTF_8)));
        res.setContentType("text/html");
        res.setLength((long) INDEX2_HTML.getBytes(com.openexchange.java.Charsets.UTF_8).length);
        res.create();

        res = coll.resolveCollection(new WebdavPath("special characters?"));
        res.create();

    }

    @Test
    public void testChildren() throws Exception {
        final WebdavCollection coll = createResource().toCollection();
        createStructure(coll, resourceManager);

        List<WebdavResource> children = coll.getChildren();
        assertResources(children, "index.html", "sitemap.html", "development", "pm", "special characters?");

        final WebdavCollection dev = coll.resolveCollection(new WebdavPath("development"));
        children = dev.getChildren();
        assertResources(children, "gui");

        final WebdavCollection gui = dev.resolveCollection(new WebdavPath("gui"));
        children = gui.getChildren();
        assertResources(children, "index3.html");

        final WebdavCollection pm = coll.resolveCollection(new WebdavPath("pm"));
        children = pm.getChildren();
        assertResources(children, "index2.html");

        final WebdavResource res = pm.resolveResource(new WebdavPath("index2.html"));
        res.delete();

        children = pm.getChildren();
        assertResources(children);

    }

    @Test
    public void testIterate() throws Exception {
        final WebdavCollection coll = createResource().toCollection();
        createStructure(coll, resourceManager);
        assertResources(coll, "index.html", "sitemap.html", "development", "pm", "gui", "index2.html", "index3.html", "special characters?"); // Note: Children ONLY
        assertResources(coll.toIterable(1), "index.html", "sitemap.html", "development", "pm", "special characters?");
        assertResources(coll.toIterable(0));

        try {
            coll.toIterable(23);
            fail();
        } catch (final IllegalArgumentException x) {
            assertTrue(true);
        }
    }

    @Test
    public void testDelete() throws Exception {
        final WebdavCollection coll = createResource().toCollection();
        createStructure(coll, resourceManager);
        final WebdavCollection dev = coll.resolveCollection(new WebdavPath("development"));

        List<WebdavResource> subList = new ArrayList<WebdavResource>();
        subList.add(dev);
        subList = OXCollections.inject(subList, dev, new Collector<WebdavResource>());

        dev.delete();
        assertResources(coll, "index.html", "sitemap.html", "pm", "index2.html", "special characters?");

        assertFalse(dev.exists());
        for (final WebdavResource res : subList) {
            assertFalse(res.exists());
        }
    }

    @Test
    public void testMove() throws Exception {
        final WebdavCollection coll = createResource().toCollection();
        createStructure(coll, resourceManager);
        WebdavCollection dev = coll.resolveCollection(new WebdavPath("development"));

        Date lastModified = dev.getLastModified();
        final Date creationDate = dev.getCreationDate();

        final WebdavProperty prop = new WebdavProperty();
        prop.setName("myvalue");
        prop.setNamespace("ox");
        prop.setValue("gnaaa!");

        dev.putProperty(prop);

        List<String> subList = new ArrayList<String>();
        subList = OXCollections.inject(subList, dev, new DisplayNameCollector());

        Thread.sleep(1000);
        final WebdavPath url = dev.getUrl();

        dev.move(coll.getUrl().dup().append("dev2"));
        dev = FACTORY.resolveCollection(url);
        assertFalse(dev.exists());

        final WebdavCollection dev2 = coll.resolveCollection(new WebdavPath("dev2"));
        assertResources(dev2, subList.toArray(new String[subList.size()]));

        assertFalse(lastModified.equals(dev2.getLastModified()));
        Assert.assertEquals(creationDate, dev2.getCreationDate());
        Assert.assertEquals("gnaaa!", dev2.getProperty("ox", "myvalue").getValue());

        dev.create();
        lastModified = dev.getLastModified();
        Thread.sleep(1000);

        dev2.move(dev.getUrl(), true, true);
        assertFalse(FACTORY.resolveCollection(dev2.getUrl()).exists());

    }

    @Test
    public void testCopy() throws Exception {
        final WebdavCollection coll = createResource().toCollection();
        createStructure(coll, resourceManager);
        final WebdavCollection dev = coll.resolveCollection(new WebdavPath("development"));

        final Date lastModified = dev.getLastModified();
        final Date creationDate = dev.getCreationDate();

        final WebdavProperty prop = new WebdavProperty();
        prop.setName("myvalue");
        prop.setNamespace("ox");
        prop.setValue("gnaaa!");

        dev.putProperty(prop);

        List<String> subList = new ArrayList<String>();
        subList = OXCollections.inject(subList, dev, new DisplayNameCollector());

        Thread.sleep(1000);
        dev.copy(coll.getUrl().dup().append("dev2"));

        assertTrue(dev.exists());

        final WebdavCollection dev2 = coll.resolveCollection(new WebdavPath("dev2"));
        assertResources(dev2, subList.toArray(new String[subList.size()]));

        assertFalse(lastModified.equals(dev2.getLastModified()));
        assertFalse(creationDate.equals(dev2.getCreationDate()));
        Assert.assertEquals("gnaaa!", dev2.getProperty("ox", "myvalue").getValue());

    }

    @Test
    public void testConflict() throws Exception {
        final WebdavResource res = super.createResource();
        try {
            resourceManager.resolveCollection(res.getUrl() + "/collection").create();
            fail();
        } catch (final WebdavProtocolException x) {
            assertTrue("" + x.getStatus(), HttpServletResponse.SC_CONFLICT == x.getStatus() || HttpServletResponse.SC_PRECONDITION_FAILED == x.getStatus());
        }
    }

    @Test
    public void testMethodNotAllowed() throws Exception {
        final WebdavCollection col = createResource().toCollection();
        try {
            col.create();
            fail();
        } catch (final WebdavProtocolException x) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, x.getStatus());
        }
    }

    @Test
    public void testLock() throws Exception {
        super.testLock();
        final WebdavCollection coll = createResource().toCollection();
        createStructure(coll, resourceManager);

        //Test Depth-Infinity lock
        final WebdavLock lock = new WebdavLock();
        lock.setType(WebdavLock.Type.WRITE_LITERAL);
        lock.setScope(WebdavLock.Scope.EXCLUSIVE_LITERAL);
        lock.setDepth(WebdavCollection.INFINITY);
        lock.setOwner("me");
        lock.setTimeout(WebdavLock.NEVER);

        coll.lock(lock);
        coll.save();

        final Set<WebdavPath> urls = new HashSet<WebdavPath>();
        for (final WebdavResource res : coll) {
            urls.add(res.getUrl());
            assertNotNull(res.getLock(lock.getToken()));
        }

        coll.unlock(lock.getToken());
        coll.save();

        for (final WebdavResource res : coll) {
            assertNull(res.getLock(lock.getToken()));
        }

        // Test Depth 1 lock
        lock.setToken(null);
        lock.setDepth(1);
        coll.lock(lock);
        coll.save();

        for (final WebdavResource res : coll.toIterable(1)) {
            urls.remove(res.getUrl());
            assertNotNull(res.getLock(lock.getToken()));
        }

        // All level 2+ resources should be left in urls and should not be locked

        for (final WebdavPath url : urls) {
            final WebdavResource res = resourceManager.resolveResource(url);
            Assert.assertEquals(res.getUrl() + " is locked!", 0, res.getLocks().size());
        }

        coll.unlock(lock.getToken());
        coll.save();
    }

    @Test
    public void testOriginalRemainsWhenCopyDisappears() throws Exception {
        //SKIP
    }

    @Override
    protected WebdavResource createResource() throws WebdavProtocolException {
        WebdavResource resource = FACTORY.resolveCollection(testCollection + "/testResource" + Math.random());
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
    public Object resourceType() throws WebdavProtocolException {
        final WebdavCollection coll = createResource().toCollection();
        Assert.assertEquals(Protocol.COLLECTION, coll.getResourceType());
        Assert.assertEquals(coll.getResourceType(), coll.getProperty("DAV:", "resourcetype").getValue());
        return null;
    }

    @Override
    public Object contentLanguage() throws WebdavProtocolException {
        final WebdavResource res = createResource();
        final String defaultLanguage = null;
        Assert.assertEquals(res.getLanguage(), res.getProperty("DAV:", "getcontentlanguage"));
        Assert.assertEquals(defaultLanguage, res.getLanguage());

        try {
            res.setLanguage("de");
            fail("Could update language");
        } catch (final WebdavProtocolException x) {
            assertTrue(true);
        }

        final WebdavProperty prop = Protocol.GETCONTENTLANGUAGE_LITERAL.getWebdavProperty();
        prop.setValue("de");

        try {
            res.putProperty(prop);
            fail("Could update language");
        } catch (final WebdavProtocolException x) {
            assertTrue(true);
        }

        return null;
    }

    @Override
    public Object contentLength() throws WebdavProtocolException {
        final WebdavResource res = createResource();
        Assert.assertEquals(res.getLength(), res.getProperty("DAV:", "getcontentlength"));
        Assert.assertEquals(null, res.getLength());

        try {
            res.setLength(23L);
            fail("Could update length");
        } catch (final WebdavProtocolException x) {
            assertTrue(true);
        }

        final WebdavProperty prop = Protocol.GETCONTENTLENGTH_LITERAL.getWebdavProperty();
        prop.setValue("2");

        try {
            res.putProperty(prop);
            fail("Could update length");
        } catch (final WebdavProtocolException x) {
            assertTrue(true);
        }

        return null;
    }

    @Override
    public Object etag() throws WebdavProtocolException {
        final WebdavResource res = createResource();
        Assert.assertEquals(res.getETag(), res.getProperty("DAV:", "getetag"));
        Assert.assertEquals(null, res.getETag());

        return null;
    }

    @Override
    public Object contentType() throws WebdavProtocolException {
        final WebdavResource res = createResource();
        try {
            res.setContentType("text/plain");
            fail("Could update content type");
        } catch (final WebdavProtocolException x) {
            assertTrue(true);
        }

        final WebdavProperty prop = Protocol.GETCONTENTTYPE_LITERAL.getWebdavProperty();
        prop.setValue("text/plain");

        try {
            res.putProperty(prop);
            fail("Could update content type");
        } catch (final WebdavProtocolException x) {
            assertTrue(true);
        }

        return null;
    }

    protected static final class DisplayNameCollector implements Injector<List<String>, WebdavResource> {

        @Override
        public List<String> inject(final List<String> list, final WebdavResource element) {
            try {
                list.add(element.getDisplayName());
            } catch (final OXException e) {
                list.add(e.toString());
            }
            return list;
        }

    }
}
