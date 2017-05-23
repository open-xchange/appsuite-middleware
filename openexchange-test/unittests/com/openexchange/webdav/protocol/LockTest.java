
package com.openexchange.webdav.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class LockTest {

    @Test
    public void testTimeout() throws Exception {
        final WebdavLock lock = new WebdavLock();
        lock.setTimeout(1000);
        Thread.sleep(500);
        assertTrue(lock.getTimeout() <= 500);
        lock.setTimeout(WebdavLock.NEVER);
        assertEquals(WebdavLock.NEVER, lock.getTimeout());
    }

}
