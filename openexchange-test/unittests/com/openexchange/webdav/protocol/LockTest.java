package com.openexchange.webdav.protocol;

import junit.framework.TestCase;

public class LockTest extends TestCase{
	public void testTimeout() throws Exception {
		WebdavLock lock = new WebdavLock();
		lock.setTimeout(1000);
		Thread.sleep(500);
		assertTrue(lock.getTimeout()<=500);
		lock.setTimeout(WebdavLock.NEVER);
		assertEquals(WebdavLock.NEVER,lock.getTimeout());
	}

}
