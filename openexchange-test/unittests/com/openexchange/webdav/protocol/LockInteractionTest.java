package com.openexchange.webdav.protocol;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import com.openexchange.webdav.protocol.Protocol.WEBDAV_METHOD;
import com.openexchange.webdav.protocol.WebdavLock.Scope;
import com.openexchange.webdav.protocol.WebdavLock.Type;

public class LockInteractionTest extends TestCase {
	
	// TODO make switching classes transparent
	
	static protected WebdavFactory FACTORY = null;
	static protected final Random RANDOM = new Random();
	private WebdavPath testCollection;
	
	public void setUp() throws Exception {
		TestWebdavFactoryBuilder.setUp();
		FACTORY = TestWebdavFactoryBuilder.buildFactory();
		FACTORY.beginRequest();
		testCollection = new WebdavPath("/testCollection"+ RANDOM.nextInt());
		
		FACTORY.resolveCollection(testCollection).create();
	}
	
	public void tearDown() throws Exception {
		FACTORY.resolveCollection(testCollection).delete();
		FACTORY.endRequest(200);
		TestWebdavFactoryBuilder.tearDown();
	}
	
	private WebdavLock getLock(int depth) {
		WebdavLock lock = new WebdavLock();
		lock.setDepth(depth);
		lock.setOwner("me");
		lock.setScope(Scope.EXCLUSIVE_LITERAL);
		lock.setTimeout(WebdavLock.NEVER);
		lock.setType(Type.WRITE_LITERAL);

		return lock;
	}
	
	public void lockInheritanceTest(int depth, int lockNumber) throws Exception {
		WebdavLock lock = getLock(depth);
		
		WebdavCollection collection = FACTORY.resolveCollection(testCollection);
		
		collection.lock(lock);
		
		WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
		res.create();
		assertEquals(lockNumber, res.getLocks().size());

	}
	
	public void testCreateInDepth0LockedCollection() throws Exception {
		lockInheritanceTest(0,0);
	}
	
	public void testCreateInDepth1LockedCollection() throws Exception {
		lockInheritanceTest(1,1);
	}
	
	public void testCreateInDepthInfinityLockedCollection() throws Exception {
		lockInheritanceTest(WebdavCollection.INFINITY,1);
	}
	
	public void testDeleteLocked() throws Exception {
		WebdavLock lock = getLock(0);
		
		WebdavCollection collection = FACTORY.resolveCollection(testCollection);
		
		WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
		res.create();
		res.lock(lock);
		res.delete();
		
		res = collection.resolveResource(new WebdavPath("test.txt"));
		assertEquals(0, res.getLocks().size());
		
	}

	public void testMoveLocked() throws Exception {
		WebdavLock lock = getLock(0);
		
		WebdavCollection collection = FACTORY.resolveCollection(testCollection);
		
		WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
		res.create();
		res.lock(lock);
		res.move(testCollection.dup().append("test2.txt"));
		
		res = collection.resolveResource(new WebdavPath("test2.txt"));
		assertEquals(0, res.getLocks().size());
	}
	
	public void testCreateLockNullResource() throws Exception {
		WebdavLock lock = getLock(0);
		
		WebdavCollection collection = FACTORY.resolveCollection(testCollection);
		WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
		res.lock(lock);
		res = collection.resolveResource(new WebdavPath("test.txt"));
		
		assertTrue(res.isLockNull());
		assertTrue(res.exists());
		assertEquals(0,collection.getChildren().size());
	}
	
	public void testRemoveLockNullResource() throws Exception {
		WebdavLock lock = getLock(0);
		
		WebdavCollection collection = FACTORY.resolveCollection(testCollection);
		WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
		res.lock(lock);
		res = collection.resolveResource(new WebdavPath("test.txt"));
		assertTrue(res.isLockNull());
		res.unlock(lock.getToken());
		
		res = collection.resolveResource(new WebdavPath("test.txt"));
		
		assertFalse(res.exists());
	}
	
	public void testLockNullProperties() throws Exception {
		testCreateLockNullResource();
		WebdavResource res = FACTORY.resolveResource(testCollection.dup().append("test.txt"));
		
		assertNull(res.getProperty("DAV:", "creationdate"));
		assertNull(res.getProperty("DAV:", "getcontentlanguage"));
		assertNull(res.getProperty("DAV:", "getcontentlength"));
		assertNull(res.getProperty("DAV:", "getetag"));
		assertNull(res.getProperty("DAV:", "getcontenttype"));
		assertNull(res.getProperty("DAV:", "getlastmodified"));
		assertNull(res.getProperty("DAV:", "resourcetype"));
		assertNull(res.getProperty("DAV:", "source"));
		
		assertNotNull(res.getProperty("DAV:","displayname"));
		assertNotNull(res.getProperty("DAV:","lockdiscovery"));
		assertNotNull(res.getProperty("DAV:","supportedlock"));
		
	}
	
	public void testLockNullOptions() throws Exception {
		WebdavLock lock = getLock(0);
		
		WebdavCollection collection = FACTORY.resolveCollection(testCollection);
		WebdavResource res = collection.resolveResource(new WebdavPath("test.txt"));
		res.lock(lock);
		res = collection.resolveResource(new WebdavPath("test.txt"));
		
		WEBDAV_METHOD[] methods = res.getOptions();
		List<WEBDAV_METHOD> expect = Arrays.asList(WEBDAV_METHOD.PUT, WEBDAV_METHOD.MKCOL, WEBDAV_METHOD.OPTIONS, WEBDAV_METHOD.PROPFIND, WEBDAV_METHOD.LOCK, WEBDAV_METHOD.UNLOCK, WEBDAV_METHOD.TRACE);
		
		AbstractResourceTest.assertOptions(expect, methods);
	}
	
	public void testTransformLockNullResource() throws Exception {
		WebdavLock lock = getLock(0);
		WebdavCollection collection = FACTORY.resolveCollection(testCollection);
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
	
	public void testTransformLockNullCollection() throws Exception {
		WebdavLock lock = getLock(0);
		
		WebdavCollection collection = FACTORY.resolveCollection(testCollection);
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
