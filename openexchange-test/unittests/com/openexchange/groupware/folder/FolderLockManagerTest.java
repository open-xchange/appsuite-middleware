package com.openexchange.groupware.folder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.impl.FolderLock;
import com.openexchange.groupware.impl.FolderLockManager;
import com.openexchange.groupware.impl.FolderLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.Lock;
import com.openexchange.groupware.infostore.webdav.LockManager;

public class FolderLockManagerTest extends FolderTestCase{
	private FolderLockManager lockManager;

	private final List<Integer> clean = new ArrayList<Integer>();

	private int entity = 23;
	private int entityDepth1 = 24;
	private int entityDepth2 = 25;

    private static final long MILLIS_WEEK = 604800000L;
    private static final long MILLIS_YEAR = 52 * MILLIS_WEEK;
    private static final long MILLIS_10_YEARS = 10 * MILLIS_YEAR;

	private final List<Integer> unlock = new ArrayList<Integer>();

	private final Random r = new Random();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		Init.startServer();
		lockManager = new FolderLockManagerImpl(new DBPoolProvider());
		lockManager.startTransaction();

		entity = mkdir(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID,"folder"+r.nextInt()).getObjectID();
		clean.add(entity);

		entityDepth1 = mkdir(entity,"subfolder").getObjectID();
		entityDepth2 = mkdir(entityDepth1,"subsubfolder").getObjectID();
	}

	@Override
	public void tearDown() throws Exception {
		for(final int id : unlock) {
			lockManager.unlock(id, ctx, user);
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
		final int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 0, "Me", ctx, user);
		unlock.add(lockId);

		final List<FolderLock> locks =  lockManager.findFolderLocks(entity, ctx, user);
		assertEquals(1, locks.size());
		final Lock lock = locks.get(0);
		assertEquals(lockId, lock.getId());
		assertEquals(user.getId(), lock.getOwner());
		assertTrue(MILLIS_10_YEARS-lock.getTimeout()-System.currentTimeMillis()<1000);
		assertEquals(LockManager.Scope.EXCLUSIVE, lock.getScope());
		assertEquals(LockManager.Type.WRITE, lock.getType());
	}

	public void testFindDepth1Locks() throws Exception {
		final int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 1, "Me", ctx, user);
		unlock.add(lockId);

		final List<FolderLock> locks =  lockManager.findFolderLocks(entityDepth1, ctx, user);
		assertEquals(1, locks.size());
		final Lock lock = locks.get(0);
		assertEquals(lockId, lock.getId());
		assertEquals(user.getId(), lock.getOwner());
		assertTrue(MILLIS_10_YEARS-lock.getTimeout()-System.currentTimeMillis()<1000);
		assertEquals(LockManager.Scope.EXCLUSIVE, lock.getScope());
		assertEquals(LockManager.Type.WRITE, lock.getType());
	}

	public void testFindDepthInfinityLocks() throws Exception {
		final int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, LockManager.INFINITE, "Me",  ctx, user);
		unlock.add(lockId);

		final List<FolderLock> locks =  lockManager.findFolderLocks(entityDepth2, ctx, user);
		assertEquals(1, locks.size());
		final Lock lock = locks.get(0);
		assertEquals(lockId, lock.getId());
		assertEquals(user.getId(), lock.getOwner());
		assertTrue(MILLIS_10_YEARS-lock.getTimeout()-System.currentTimeMillis()<1000);
		assertEquals(LockManager.Scope.EXCLUSIVE, lock.getScope());
		assertEquals(LockManager.Type.WRITE, lock.getType());
	}

	public void testLoadOwnLocks() throws Exception {
		final int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, LockManager.INFINITE, "Me",  ctx, user);
		unlock.add(lockId);

		final int lockId2 = lockManager.lock(entityDepth1, LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 0, "Me", ctx, user);
		unlock.add(lockId2);

		final Map<Integer, List<FolderLock>> lockMap = lockManager.loadOwnLocks(Arrays.asList(entity, entityDepth1, entityDepth2), ctx, user);

		assertTrue(lockMap.get(entityDepth2).isEmpty());
		assertEquals(1, lockMap.get(entityDepth1).size());
		assertEquals(lockId2, lockMap.get(entityDepth1).get(0).getId());

		assertEquals(1, lockMap.get(entity).size());
		assertEquals(lockId, lockMap.get(entity).get(0).getId());

	}

	public void testUnlock() throws Exception {
		final int lockId = lockManager.lock(entity ,LockManager.INFINITE, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 0, "Me", ctx, user);
		unlock.add(lockId);

		lockManager.unlock(lockId, ctx, user);

		final List<FolderLock> locks =  lockManager.findFolderLocks(entity, ctx, user);
		assertTrue(locks.isEmpty());

	}

	public void testTimeout() throws Exception {
		final int lockId = lockManager.lock(entity ,-23, LockManager.Scope.EXCLUSIVE, LockManager.Type.WRITE, 0, "Me", ctx, user);
		unlock.add(lockId);

		final List<FolderLock> locks =  lockManager.findFolderLocks(entity, ctx, user);
		assertEquals(0, locks.size());

	}
}
