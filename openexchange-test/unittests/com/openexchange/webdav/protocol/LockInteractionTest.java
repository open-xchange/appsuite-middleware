
package com.openexchange.webdav.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.webdav.protocol.WebdavLock.Scope;
import com.openexchange.webdav.protocol.WebdavLock.Type;

public class LockInteractionTest {
    // TODO make switching classes transparent

    static protected WebdavFactory FACTORY = null;
    static protected final Random RANDOM = new Random();
    private WebdavPath testCollection;

    @Before
    public void setUp() throws Exception {
        TestWebdavFactoryBuilder.setUp();
        FACTORY = TestWebdavFactoryBuilder.buildFactory();
        FACTORY.beginRequest();
        testCollection = new WebdavPath("public_infostore", "testCollection" + RANDOM.nextInt());

        FACTORY.resolveCollection(testCollection).create();
    }

    @After
    public void tearDown() throws Exception {
        FACTORY.resolveCollection(testCollection).delete();
        FACTORY.endRequest(200);
        TestWebdavFactoryBuilder.tearDown();
    }

    private WebdavLock getLock(final int depth) {
        final WebdavLock lock = new WebdavLock();
        lock.setDepth(depth);
        lock.setOwner("me");
        lock.setScope(Scope.EXCLUSIVE_LITERAL);
        lock.setTimeout(WebdavLock.NEVER);
        lock.setType(Type.WRITE_LITERAL);

        return lock;
    }

    public void lockInheritanceTest(final int depth, final int lockNumber) throws Exception {
        final WebdavLock lock = getLock(depth);

        final WebdavCollection collection = FACTORY.resolveCollection(testCollection);

        collection.lock(lock);

        final WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
        res.create();
        assertEquals(lockNumber, res.getLocks().size());

    }

    @Test
    public void testCreateInDepth0LockedCollection() throws Exception {
        lockInheritanceTest(0, 0);
    }

    @Test
    public void testCreateInDepth1LockedCollection() throws Exception {
        lockInheritanceTest(1, 1);
    }

    @Test
    public void testCreateInDepthInfinityLockedCollection() throws Exception {
        lockInheritanceTest(WebdavCollection.INFINITY, 1);
    }

    @Test
    public void testDeleteLocked() throws Exception {
        final WebdavLock lock = getLock(0);

        final WebdavCollection collection = FACTORY.resolveCollection(testCollection);

        WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
        res.create();
        res.lock(lock);
        res.delete();

        res = collection.resolveResource(new WebdavPath("test.txt"));
        assertEquals(0, res.getLocks().size());

    }

    @Test
    public void testMoveLocked() throws Exception {
        final WebdavLock lock = getLock(0);

        final WebdavCollection collection = FACTORY.resolveCollection(testCollection);

        WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
        res.create();
        res.lock(lock);
        res.move(testCollection.dup().append("test2.txt"));

        res = collection.resolveResource(new WebdavPath("test2.txt"));
        assertEquals(0, res.getLocks().size());
    }

    @Test
    public void testCreateLockNullResource() throws Exception {
        final WebdavLock lock = getLock(0);

        final WebdavCollection collection = FACTORY.resolveCollection(testCollection);
        WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
        res.lock(lock);
        res = collection.resolveResource(new WebdavPath("test.txt"));

        assertTrue(res.isLockNull());
        assertTrue(res.exists());
        assertEquals(0, collection.getChildren().size());
    }

    @Test
    public void testRemoveLockNullResource() throws Exception {
        final WebdavLock lock = getLock(0);

        final WebdavCollection collection = FACTORY.resolveCollection(testCollection);
        WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
        res.lock(lock);
        res = collection.resolveResource(new WebdavPath("test.txt"));
        assertTrue(res.isLockNull());
        res.unlock(lock.getToken());

        res = collection.resolveResource(new WebdavPath("test.txt"));

        assertFalse(res.exists());
    }

    @Test
    public void testLockNullProperties() throws Exception {
        testCreateLockNullResource();
        final WebdavResource res = FACTORY.resolveResource(testCollection.dup().append("test.txt"));

        assertNull(res.getProperty("DAV:", "creationdate"));
        assertNull(res.getProperty("DAV:", "getcontentlanguage"));
        assertNull(res.getProperty("DAV:", "getcontentlength"));
        assertNull(res.getProperty("DAV:", "getetag"));
        assertNull(res.getProperty("DAV:", "getcontenttype"));
        assertNull(res.getProperty("DAV:", "getlastmodified"));
        assertNull(res.getProperty("DAV:", "resourcetype"));
        assertNull(res.getProperty("DAV:", "source"));

        assertNotNull(res.getProperty("DAV:", "displayname"));
        assertNotNull(res.getProperty("DAV:", "lockdiscovery"));
        assertNotNull(res.getProperty("DAV:", "supportedlock"));

    }

    @Test
    public void testLockNullOptions() throws Exception {
        final WebdavLock lock = getLock(0);

        final WebdavCollection collection = FACTORY.resolveCollection(testCollection);
        WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
        res.lock(lock);
        res = collection.resolveResource(new WebdavPath("test.txt"));

        final WebdavMethod[] methods = res.getOptions();
        final List<WebdavMethod> expect = Arrays.asList(WebdavMethod.PUT, WebdavMethod.MKCOL, WebdavMethod.OPTIONS, WebdavMethod.PROPFIND, WebdavMethod.LOCK, WebdavMethod.UNLOCK, WebdavMethod.TRACE);

        AbstractResourceTest.assertOptions(expect, methods);
    }

    @Test
    public void testTransformLockNullResource() throws Exception {
        final WebdavLock lock = getLock(0);
        final WebdavCollection collection = FACTORY.resolveCollection(testCollection);
        WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
        res.lock(lock);
        res = collection.resolveResource(new WebdavPath("test.txt"));
        res.putBodyAndGuessLength(new ByteArrayInputStream(new byte[2]));
        res.create();

        res = collection.resolveResource(new WebdavPath("test.txt"));
        assertTrue(res.exists());
        assertFalse(res.isCollection());
        assertFalse(res.isLockNull());

        assertEquals(1, res.getLocks().size());
        assertNotNull(res.getLock(lock.getToken()));

        res.unlock(lock.getToken());
        res.save();

    }

    @Test
    public void testTransformLockNullCollection() throws Exception {
        final WebdavLock lock = getLock(0);

        final WebdavCollection collection = FACTORY.resolveCollection(testCollection);
        WebdavResource res = collection.resolveResource(new WebdavPath("test"));
        res.lock(lock);
        res = collection.resolveCollection(new WebdavPath("test"));
        res.create();

        res = collection.resolveResource(new WebdavPath("test"));
        assertTrue(res.exists());
        assertTrue(res.isCollection());
        assertFalse(res.isLockNull());

        assertEquals(1, res.getLocks().size());
        assertNotNull(res.getLock(lock.getToken()));

    }
}
