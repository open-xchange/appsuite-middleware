package com.openexchange.groupware.infostore;

import java.util.ArrayList;
import java.util.List;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.Lock;
import com.openexchange.groupware.infostore.webdav.LockManager;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.test.AjaxInit;

import junit.framework.TestCase;

public class EntityLockManagerTest extends TestCase {
	
	private EntityLockManager lockManager;

	private List<Integer> clean = new ArrayList<Integer>();
	
	private int entity = 23;
	
	
	private Context ctx = new ContextImpl(1);
	private User user = null;
	private UserConfiguration userConfig = null;
	
	public void setUp() throws Exception {
        super.setUp();
		Init.startServer();
		user = UserStorage.getInstance().getUser(UserStorage.getInstance().getUserId(getUsername(), ctx), ctx); //FIXME
		lockManager = new EntityLockManagerImpl(new DBPoolProvider(), "infostore_lock");
		lockManager.startTransaction();
		
	}
	
	private String getUsername() {
		return AjaxInit.getAJAXProperty("login");
	}

	public void tearDown() throws Exception {
		for(int id : clean) {
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
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		
		List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
		assertEquals(1, locks.size());
		Lock lock = locks.get(0);
		assertEquals(lockId, lock.getId());
		assertEquals(user.getId(), lock.getOwner());
		assertTrue(Long.MAX_VALUE-lock.getTimeout()-System.currentTimeMillis()<1000);
		assertEquals(LockManager.Scope.EXCLUSIVE, lock.getScope());
		assertEquals(LockManager.Type.WRITE, lock.getType());
	}
	
	public void testIsLocked() throws Exception {
		assertFalse("Should not be locked", lockManager.isLocked(entity, ctx, user, userConfig));
		
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		
		assertTrue("Should be locked", lockManager.isLocked(entity, ctx, user, userConfig));
	}
	
	public void testUnlock() throws Exception {
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		
		lockManager.unlock(lockId, ctx, user, userConfig);
		
		List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
		assertTrue(locks.isEmpty());
		
	}
	
	public void testTimeout() throws Exception {
		int lockId = lockManager.lock(entity ,-23, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		
		List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
		assertEquals(0, locks.size());
		
	}
	
	public void testRemoveAll() throws Exception {
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, "Me",  ctx, user, userConfig);
		clean.add(lockId);
		
		lockManager.removeAll(entity, ctx, user, userConfig);
		
		List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
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
		
		List<Lock> locks =  lockManager.findLocks(entity, ctx, user, userConfig);
		assertEquals("locks are assigned to dest", 3,locks.size());
		for(Lock lock : locks) {
			assertEquals(user.getId()+1, lock.getOwner());
		}
		
	}
	
	
}