
package com.openexchange.webdav.protocol;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.util.Utils;

public class ResourceTest extends AbstractResourceTest {

    public static final int SKEW = 1000;

    static protected WebdavFactory FACTORY = null;

    @Override
    @Before
    public void setUp() throws Exception {
        try {
            TestWebdavFactoryBuilder.setUp();
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        FACTORY = TestWebdavFactoryBuilder.buildFactory();
        FACTORY.beginRequest();
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        FACTORY.endRequest(200);
        TestWebdavFactoryBuilder.tearDown();
    }

    @Test
    public void testBody() throws Exception {
        final WebdavResource res = createResource();
        final String content = "Hello, I'm the content!";
        final byte[] bytes = content.getBytes(com.openexchange.java.Charsets.UTF_8);

        res.putBody(new ByteArrayInputStream(bytes));

        InputStream in = null;
        InputStream in2 = null;
        try {
            in = res.getBody();
            in2 = new ByteArrayInputStream(bytes);

            int b = -1;
            while ((b = in.read()) != -1) {
                Assert.assertEquals(b, in2.read());
            }
            Assert.assertEquals(-1, in2.read());
        } finally {
            if (in != null) {
                in.close();
            }
            if (in2 != null) {
                in2.close();
            }
        }

    }

    @Test
    public void testMove() throws Exception {
        WebdavResource res = createResource();

        final Date lastModified = res.getLastModified();
        res.getCreationDate();

        final WebdavProperty prop = new WebdavProperty();
        prop.setName("myvalue");
        prop.setNamespace("ox");
        prop.setValue("gnaaa!");

        res.putProperty(prop);

        final String content = "Hello, I'm the content!";
        final byte[] bytes = content.getBytes(com.openexchange.java.Charsets.UTF_8);

        res.putBody(new ByteArrayInputStream(bytes));

        Thread.sleep(1000);

        final WebdavPath url = res.getUrl();

        res.move(testCollection.dup().append("moved"));
        res = FACTORY.resolveResource(url);
        assertFalse(res.exists());

        res = resourceManager.resolveResource(testCollection.dup().append("moved"));
        assertTrue(res.exists());

        assertFalse(lastModified.equals(res.getLastModified()));
        Assert.assertEquals("gnaaa!", res.getProperty("ox", "myvalue").getValue());
        InputStream in = null;
        InputStream in2 = null;
        try {
            in = res.getBody();
            in2 = new ByteArrayInputStream(bytes);

            int b = -1;
            while ((b = in.read()) != -1) {
                Assert.assertEquals(b, in2.read());
            }
            Assert.assertEquals(-1, in2.read());
        } finally {
            if (in != null) {
                in.close();
            }
            if (in2 != null) {
                in2.close();
            }
        }
    }

    @Test
    public void testCopy() throws Exception {
        WebdavResource res = createResource();

        final Date lastModified = res.getLastModified();
        final Date creationDate = res.getCreationDate();

        final WebdavProperty prop = new WebdavProperty();
        prop.setName("myvalue");
        prop.setNamespace("ox");
        prop.setValue("gnaaa!");

        res.putProperty(prop);

        final String content = "Hello, I'm the content!";
        final byte[] bytes = content.getBytes(com.openexchange.java.Charsets.UTF_8);

        res.putBody(new ByteArrayInputStream(bytes));

        Thread.sleep(1000);

        final WebdavPath url = res.getUrl();
        res.copy(testCollection.dup().append("copy"));

        res = FACTORY.resolveResource(url);
        assertTrue(res.exists());

        res = resourceManager.resolveResource(testCollection.dup().append("copy"));
        assertTrue(res.exists());

        //TODO: It's possible to set created/modified during infostore document creation in the meantime.
        //      Maybe we need to mimic the previous behaviour inside the webdav resource copy now?
        //      Or is it save to take over those properties from the source, too?
        //		assertFalse(lastModified.equals(res.getLastModified()));
        //		assertFalse(creationDate.equals(res.getCreationDate()));
        Assert.assertEquals("gnaaa!", res.getProperty("ox", "myvalue").getValue());
        InputStream in = null;
        InputStream in2 = null;
        try {
            in = res.getBody();
            in2 = new ByteArrayInputStream(bytes);

            int b = -1;
            while ((b = in.read()) != -1) {
                Assert.assertEquals(b, in2.read());
            }
            Assert.assertEquals(-1, in2.read());
        } finally {
            if (in != null) {
                in.close();
            }
            if (in2 != null) {
                in2.close();
            }
        }
    }

    // Original File must not disappear if the copy disappears
    // Bug 10962

    @Test
    public void testOriginalRemainsWhenCopyDisappears() throws Exception {
        WebdavResource res = createResource();

        final String content = "Hello, I'm the content!";
        final byte[] bytes = content.getBytes(com.openexchange.java.Charsets.UTF_8);

        res.putBody(new ByteArrayInputStream(bytes));

        final WebdavPath url = res.getUrl();
        res.copy(testCollection.dup().append("copy"));

        res = FACTORY.resolveResource(url);
        assertTrue(res.exists());

        final WebdavResource copy = resourceManager.resolveResource(testCollection.dup().append("copy"));
        copy.delete();

        assertFalse(copy.exists());
        assertTrue(res.exists());

        InputStream in = null;
        InputStream in2 = null;
        try {
            in = res.getBody();
            in2 = new ByteArrayInputStream(bytes);

            int b = -1;
            boolean readAntyhing = false;
            while ((b = in.read()) != -1) {
                readAntyhing = true;
                Assert.assertEquals(b, in2.read());
            }
            assertTrue(readAntyhing);
            Assert.assertEquals(-1, in2.read());
        } finally {
            if (in != null) {
                in.close();
            }
            if (in2 != null) {
                in2.close();
            }
        }

    }

    @Override
    protected List<Property> getPropertiesToTest() {
        return resourceManager.getProtocol().getKnownProperties();
    }

    @Override
    protected WebdavResource createResource() throws WebdavProtocolException {
        WebdavResource resource = FACTORY.resolveResource(testCollection + "/testResource" + Math.random());
        assertFalse(resource.exists());
        resource.create();
        resource = resourceManager.resolveResource(resource.getUrl());
        assertTrue(resource.exists());
        return resource;
    }

    @Override
    protected WebdavFactory getWebdavFactory() {
        return FACTORY;
    }

    @Test
    public void testLock() throws Exception {
        WebdavLock lock = new WebdavLock();
        lock.setType(WebdavLock.Type.WRITE_LITERAL);
        lock.setScope(WebdavLock.Scope.EXCLUSIVE_LITERAL);
        lock.setDepth(0);
        lock.setOwner("me");
        lock.setTimeout(10000);
        assertNull(lock.getToken());

        final WebdavResource res = createResource();

        res.lock(lock);
        res.save();

        assertNotNull(lock.getToken());
        assertNotNull(res.getProperty("DAV:", "lockdiscovery"));
        assertNotNull(res.getProperty("DAV:", "lockdiscovery").getValue()); // Content checked in LockWriterTest
        assertTrue(res.getProperty("DAV:", "lockdiscovery").isXML());

        List<WebdavLock> locks = res.getLocks();
        Assert.assertEquals(1, locks.size());
        Assert.assertEquals(lock, locks.get(0));
        Assert.assertEquals(lock, res.getLock(lock.getToken()));

        res.unlock(lock.getToken());
        res.save();
        locks = res.getLocks();
        Assert.assertEquals(0, locks.size());

        lock.setTimeout(22);
        res.lock(lock);
        res.save();

        Thread.sleep(23);

        locks = res.getLocks();
        Assert.assertEquals(0, locks.size());

        // Renew
        lock.setTimeout(1000);

        res.lock(lock);
        res.save();

        final WebdavLock lock2 = new WebdavLock();
        lock2.setType(WebdavLock.Type.WRITE_LITERAL);
        lock2.setScope(WebdavLock.Scope.EXCLUSIVE_LITERAL);
        lock2.setDepth(0);
        lock2.setOwner("me");
        lock2.setTimeout(WebdavLock.NEVER);
        lock2.setToken(lock.getToken());

        res.lock(lock2);
        locks = res.getLocks();
        Assert.assertEquals(1, locks.size());
        lock = res.getLock(lock.getToken());
        res.unlock(lock.getToken());
        res.save();
        //Assert.assertEquals(WebdavLock.NEVER,lock.getTimeout());
    }

    @Test
    public void testConflict() throws Exception {
        final WebdavResource res = createResource();
        try {
            resourceManager.resolveResource(res.getUrl() + "/resource").create();
            fail();
        } catch (final WebdavProtocolException x) {
            assertTrue("" + x.getStatus(), HttpServletResponse.SC_CONFLICT == x.getStatus() || HttpServletResponse.SC_PRECONDITION_FAILED == x.getStatus());
        }
    }

    @Test
    public void testOptions() throws Exception {
        final WebdavResource res = createResource();
        assertOptions(Arrays.asList(res.getOptions()), WebdavMethod.GET, WebdavMethod.PUT, WebdavMethod.DELETE, WebdavMethod.HEAD, WebdavMethod.OPTIONS, WebdavMethod.TRACE, WebdavMethod.PROPPATCH, WebdavMethod.PROPFIND, WebdavMethod.MOVE, WebdavMethod.COPY, WebdavMethod.LOCK, WebdavMethod.UNLOCK, WebdavMethod.REPORT, WebdavMethod.ACL, WebdavMethod.MKCALENDAR);

        //TODO Newly created, already locked
    }

    // TESTS FOR PROPERTIES

    @Override
    public Object creationDate() throws WebdavProtocolException {
        final Date now = new Date();
        WebdavResource res = createResource();
        Assert.assertEquals(Utils.convert(res.getCreationDate()), res.getProperty("DAV:", "creationdate").getValue());
        assertEquals(now, res.getCreationDate(), SKEW);

        try {
            Thread.sleep(SKEW + 10);
        } catch (final InterruptedException e) {
        }
        res.save();
        res = res.reload();

        assertEquals(now, res.getCreationDate(), SKEW);

        return null;
    }

    @Override
    public Object displayName() throws WebdavProtocolException {
        /*
         * WebdavResource res = createResource();
         * String defaultDispName = res.getUrl().substring(res.getUrl().lastIndexOf("/")+1);
         * Assert.assertEquals(res.getDisplayName(), res.getProperty("DAV:", "displayname").getValue());
         * Assert.assertEquals(defaultDispName, res.getDisplayName());
         *
         * res.setDisplayName("Other Disp");
         * res.save();
         * res = res.reload();
         * Assert.assertEquals("Other Disp", res.getDisplayName());
         * Assert.assertEquals(res.getDisplayName(), res.getProperty("DAV:", "displayname").getValue());
         *
         * WebdavProperty prop = Protocol.DISPLAYNAME_LITERAL.getWebdavProperty();
         * prop.setValue("My other disp");
         * res.putProperty(prop);
         *
         * Assert.assertEquals("My other disp", res.getDisplayName());
         * Assert.assertEquals(res.getDisplayName(), res.getProperty("DAV:","displayname").getValue());
         *
         *
         * return null;
         */
        return null;
    }

    @Override
    public Object contentLanguage() throws WebdavProtocolException {
        /*
         * WebdavResource res = createResource();
         * String defaultLanguage = "en";
         * Assert.assertEquals(res.getLanguage(), res.getProperty("DAV:", "getcontentlanguage").getValue());
         * Assert.assertEquals(defaultLanguage, res.getLanguage());
         *
         * try {
         * res.setLanguage("de");
         * } catch (WebdavException e) {
         * e.printStackTrace();
         * fail(e.toString());
         * }
         * res.save();
         *
         * res = res.reload();
         *
         * Assert.assertEquals(res.getLanguage(), res.getProperty("DAV:", "getcontentlanguage").getValue());
         * Assert.assertEquals("de", res.getLanguage());
         *
         * WebdavProperty prop = Protocol.GETCONTENTLANGUAGE_LITERAL.getWebdavProperty();
         * prop.setValue("fr");
         * res.putProperty(prop);
         *
         * Assert.assertEquals(res.getLanguage(), res.getProperty("DAV:", "getcontentlanguage").getValue());
         * Assert.assertEquals("fr", res.getLanguage());
         *
         *
         * return null;
         */ //FIXME
        return null;
    }

    @Override
    public Object contentLength() throws WebdavProtocolException {
        WebdavResource res = createResource();
        final Long defaultLength = 0L;
        Assert.assertEquals("" + res.getLength(), res.getProperty("DAV:", "getcontentlength").getValue());
        Assert.assertEquals(defaultLength, res.getLength());

        try {
            res.setLength(1L);
        } catch (final OXException e) {
            e.printStackTrace();
            fail(e.toString());
        }
        res.save();
        res = res.reload();

        Assert.assertEquals("" + res.getLength(), res.getProperty("DAV:", "getcontentlength").getValue());
        Assert.assertEquals((Long) 1L, res.getLength());

        final WebdavProperty prop = Protocol.GETCONTENTLENGTH_LITERAL.getWebdavProperty();
        prop.setValue("2");
        res.putProperty(prop);

        Assert.assertEquals("" + res.getLength(), res.getProperty("DAV:", "getcontentlength").getValue());
        Assert.assertEquals((Long) 2L, res.getLength());

        {
            final String content = "Hello, I'm the content!";
            final byte[] bytes = content.getBytes(com.openexchange.java.Charsets.UTF_8);

            res.putBodyAndGuessLength(new ByteArrayInputStream(bytes));

            Assert.assertEquals(bytes.length, (int) (long) res.getLength());

        }

        return null;
    }

    @Override
    public Object contentType() throws WebdavProtocolException {
        WebdavResource res = createResource();

        res.setContentType("text/plain");
        res.save();
        res = res.reload();

        Assert.assertEquals("text/plain", res.getContentType());
        Assert.assertEquals(res.getContentType(), res.getProperty("DAV:", "getcontenttype").getValue());

        final WebdavProperty prop = Protocol.GETCONTENTTYPE_LITERAL.getWebdavProperty();
        prop.setValue("text/html");
        res.putProperty(prop);

        Assert.assertEquals("text/html", res.getContentType());
        Assert.assertEquals(res.getContentType(), res.getProperty("DAV:", "getcontenttype").getValue());

        return null;
    }

    @Override
    public Object etag() throws WebdavProtocolException {
        WebdavResource res = createResource();
        Assert.assertEquals(res.getETag(), res.getProperty("DAV:", "getetag").getValue());

        res.setDisplayName("one");
        final String eTag = res.getETag();
        res.save();

        res = res.reload();

        Assert.assertEquals(res.getETag(), res.getProperty("DAV:", "getetag").getValue());
        Assert.assertEquals(eTag, res.getETag());

        final String text = "Hallo";
        final byte[] bytes = text.getBytes(com.openexchange.java.Charsets.UTF_8);

        try {
            res.putBody(new ByteArrayInputStream(bytes));
        } catch (final OXException e) {
            e.printStackTrace();
            fail(e.toString());
        }
        res.save();

        assertFalse(eTag.equals(res.getETag()));

        return null;
    }

    @Override
    public Object lastModified() throws WebdavProtocolException {
        Date now = new Date();
        final WebdavResource res = createResource();
        Assert.assertEquals(Utils.convert(res.getLastModified()), res.getProperty("DAV:", "getlastmodified").getValue());
        assertEquals(now, res.getLastModified(), SKEW);
        try {
            Thread.sleep(SKEW + 10);
        } catch (final InterruptedException e) {
        }
        now = new Date();
        res.setDisplayName(res.getDisplayName());
        res.save();
        assertEquals(now, res.getLastModified(), SKEW);

        return null;
    }

    @Override
    public Object resourceType() throws WebdavProtocolException {
        final WebdavResource res = createResource();
        assertNotNull(res.getProperty("DAV:", "resourcetype"));
        assertNull(res.getProperty("DAV:", "resourcetype").getValue()); // Is set, but is empty
        Assert.assertEquals(null, res.getResourceType());

        return null;
    }

    @Override
    public Object lockDiscovery() throws WebdavProtocolException {
        // Tested in Lock Test
        return null;
    }

    @Override
    public Object supportedLock() throws WebdavProtocolException {
        // Tested in Lock Test
        return null;
    }

    @Override
    public Object source() throws WebdavProtocolException {
        /*
         * WebdavResource res = createResource();
         * try {
         * res.setSource("http://localhost/theSecretSource");
         * } catch (WebdavException e) {
         * e.printStackTrace();
         * fail(e.toString());
         * }
         * res.save();
         * res = resourceManager.resolveResource(res.getUrl());
         * Assert.assertEquals(res.getSource(), res.getProperty("DAV:", "source").getValue());
         * Assert.assertEquals("http://localhost/theSecretSource", res.getSource());
         *
         * WebdavProperty prop = Protocol.SOURCE_LITERAL.getWebdavProperty();
         * prop.setValue("http://localhost/theSuperSecretSource");
         * res.putProperty(prop);
         *
         * Assert.assertEquals(res.getSource(), res.getProperty("DAV:", "source").getValue());
         * Assert.assertEquals("http://localhost/theSuperSecretSource", res.getSource());
         *
         */ // FIXME
        return null;
    }

}
