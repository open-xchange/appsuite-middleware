package com.openexchange.groupware.folder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.openexchange.groupware.FolderLock;
import com.openexchange.groupware.FolderLockManager;
import com.openexchange.groupware.FolderLockManagerImpl;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.webdav.Lock;
import com.openexchange.groupware.infostore.webdav.LockManager;
import com.openexchange.groupware.tx.DBPoolProvider;

public class FolderLockManagerTest extends FolderTestCase{
	private FolderLockManager lockManager;

	private List<Integer> clean = new ArrayList<Integer>();
	
	private int entity = 23;
	private int entityDepth1 = 24;
	private int entityDepth2 = 25;
	
	private List<Integer> unlock = new ArrayList<Integer>();
	
	private Random r = new Random();
	
	public void setUp() throws Exception {
		super.setUp();
		Init.startServer();
		lockManager = new FolderLockManagerImpl(new DBPoolProvider());
		lockManager.startTransaction();
		
		entity = mkdir(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID,"folder"+r.nextInt()).getObjectID();
		clean.add(entity);
		
		entityDepth1 = mkdir(entity,"subfolder").getObjectID();
		entityDepth2 = mkdir(entityDepth1,"subsubfolder").getObjectID();
	}

	public void tearDown() throws Exception {
		for(int id : unlock) {
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
	
	public void testFindDepth0Locks() throws Exception {
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 0, "Me", ctx, user, userConfig);
		unlock.add(lockId);
		
		List<FolderLock> locks =  lockManager.findFolderLocks(entity, ctx, user, userConfig);
		assertEquals(1, locks.size());
		Lock lock = locks.get(0);
		assertEquals(lockId, lock.getId());
		assertEquals(user.getId(), lock.getOwner());
		assertTrue(Long.MAX_VALUE-lock.getTimeout()-System.currentTimeMillis()<1000);
		assertEquals(LockManager.Scope.EXCLUSIVE, lock.getScope());
		assertEquals(LockManager.Type.WRITE, lock.getType());
	}
	
	public void testFindDepth1Locks() throws Exception {
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 1, "Me", ctx, user, userConfig);
		unlock.add(lockId);
		
		List<FolderLock> locks =  lockManager.findFolderLocks(entityDepth1, ctx, user, userConfig);
		assertEquals(1, locks.size());
		Lock lock = locks.get(0);
		assertEquals(lockId, lock.getId());
		assertEquals(user.getId(), lock.getOwner());
		assertTrue(Long.MAX_VALUE-lock.getTimeout()-System.currentTimeMillis()<1000);
		assertEquals(LockManager.Scope.EXCLUSIVE, lock.getScope());
		assertEquals(LockManager.Type.WRITE, lock.getType());
	}
	
	public void testFindDepthInfinityLocks() throws Exception {
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, LockManager.INFINITE, "Me",  ctx, user, userConfig);
		unlock.add(lockId);
		
		List<FolderLock> locks =  lockManager.findFolderLocks(entityDepth2, ctx, user, userConfig);
		assertEquals(1, locks.size());
		Lock lock = locks.get(0);
		assertEquals(lockId, lock.getId());
		assertEquals(user.getId(), lock.getOwner());
		assertTrue(Long.MAX_VALUE-lock.getTimeout()-System.currentTimeMillis()<1000);
		assertEquals(LockManager.Scope.EXCLUSIVE, lock.getScope());
		assertEquals(LockManager.Type.WRITE, lock.getType());
	}
	
	public void testLoadOwnLocks() throws Exception {
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, LockManager.INFINITE, "Me",  ctx, user, userConfig);
		unlock.add(lockId);
		
		int lockId2 = lockManager.lock(entityDepth1, LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 0, "Me", ctx, user, userConfig);
		unlock.add(lockId2);
		
		Map<Integer, List<FolderLock>> lockMap = lockManager.loadOwnLocks(Arrays.asList(entity, entityDepth1, entityDepth2), ctx, user, userConfig);
		
		assertTrue(lockMap.get(entityDepth2).isEmpty());
		assertEquals(1, lockMap.get(entityDepth1).size());
		assertEquals(lockId2, lockMap.get(entityDepth1).get(0).getId());
		
		assertEquals(1, lockMap.get(entity).size());
		assertEquals(lockId, lockMap.get(entity).get(0).getId());
		
	}
	
	public void testUnlock() throws Exception {
		int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 0, "Me", ctx, user, userConfig);
		unlock.add(lockId);
		
		lockManager.unlock(lockId, ctx, user, userConfig);
		
		List<FolderLock> locks =  lockManager.findFolderLocks(entity, ctx, user, userConfig);
		assertTrue(locks.isEmpty());
		
	}
	
	public void testTimeout() throws Exception {
		int lockId = lockManager.lock(entity ,-23, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 0, "Me", ctx, user, userConfig);
		unlock.add(lockId);
		
		List<FolderLock> locks =  lockManager.findFolderLocks(entity, ctx, user, userConfig);
		assertEquals(0, locks.size());
		
	}
}
