package com.openexchange.groupware.infostore;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.Lock;
import com.openexchange.groupware.infostore.webdav.LockExpiryListener;
import com.openexchange.groupware.infostore.webdav.LockManager;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.test.AjaxInit;

public class EntityLockManagerTest extends TestCase {

	private EntityLockManager lockManager;

	private final List<Integer> clean = new ArrayList<Integer>();

	private final int entity = 23;

    private static final long MILLIS_WEEK = 604800000L;
    private static final long MILLIS_YEAR = 52 * MILLIS_WEEK;
    private static final long MILLIS_10_YEARS = 10 * MILLIS_YEAR;
    
	private Context ctx = new ContextImpl(1);
	private User user = null;
	private final UserConfiguration userConfig = null;

	@Override
	public void setUp() throws Exception {
        super.setUp();
		Init.startServer();

		final TestConfig config = new TestConfig();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);

		user = UserStorage.getInstance().getUser(UserStorage.getInstance().getUserId(getUsername(), ctx), ctx); //FIXME
		lockManager = new EntityLockManagerImpl(new DBPoolProvider(), "infostore_lock");
		lockManager.startTransaction();

	}

	private String getUsername() {
		final String userName = AjaxInit.getAJAXProperty("login");
		final int pos = userName.indexOf('@');
        return pos == -1 ? userName : userName.substring(0, pos);
	}

	@Override
	public void tearDown() throws Exception {
		for(final int id : clean) {
			lockManager.unlock(id, ctx, user, userConfig);
		}
		lockManager.commit();
		lockManager.finish();
        Init.stopServer();
        super.tearDown();
	}

	public void testExclusiveLock() throws Exception {
		// TODO
	}

	public void testSharedLock() throws Exception {
		// TODO: Find out semantics of shared locks
	}

	public void testFindLocks() throws Exception {
		final int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);

		final List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
		assertEquals(1, locks.size());
		final Lock lock = locks.get(0);
		assertEquals(lockId, lock.getId());
		assertEquals(user.getId(), lock.getOwner());
		assertTrue(MILLIS_10_YEARS-lock.getTimeout()-System.currentTimeMillis()<1000);
		assertEquals(LockManager.Scope.EXCLUSIVE, lock.getScope());
		assertEquals(LockManager.Type.WRITE, lock.getType());
	}

	public void testIsLocked() throws Exception {
		assertFalse("Should not be locked", lockManager.isLocked(entity, ctx, user, userConfig));

		final int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);

		assertTrue("Should be locked", lockManager.isLocked(entity, ctx, user, userConfig));
	}

	public void testUnlock() throws Exception {
		final int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);

		lockManager.unlock(lockId, ctx, user, userConfig);

		final List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
		assertTrue(locks.isEmpty());

	}

	public void testTimeout() throws Exception {
		final int lockId = lockManager.lock(entity ,-23, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);

		final List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
		assertEquals(0, locks.size());

	}

	public void testTimeoutTriggersListener() throws Exception {
	    final int lockId = lockManager.lock(entity ,-23, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
        final LockExpirySpy spy = new LockExpirySpy();
        lockManager.addExpiryListener(spy);
	    clean.add(lockId);

        final List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
        assertEquals("A lock remained, though it should have timed out", 0, locks.size());
        assertEquals("Expected notification about expired lock", 1, spy.getExpired().size());
	}

	public void testRemoveAll() throws Exception {
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);

		lockManager.removeAll(entity, ctx, user, userConfig);

		final List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
		assertTrue(locks.isEmpty());

	}

	public void testTransferLocks() throws Exception {
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		lockManager.transferLocks(ctx, user.getId(), user.getId()+1);

		final List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
		assertEquals("locks are assigned to dest", 3,locks.size());
		for(final Lock lock : locks) {
			assertEquals(user.getId()+1, lock.getOwner());
		}

	}


	private static final class LockExpirySpy implements LockExpiryListener {

	    private final List<Lock> expired = new ArrayList<Lock>();

        @Override
        public void lockExpired(final Lock lock) {
            expired.add( lock );
        }

        public List<Lock> getExpired() {
            return expired;
        }

	}

}
