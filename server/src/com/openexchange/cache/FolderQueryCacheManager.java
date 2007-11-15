/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.api2.OXException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * {@link FolderQueryCacheManager}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderQueryCacheManager {

	private static final Map<Integer, ReadWriteLock> contextLocks = new HashMap<Integer, ReadWriteLock>();

	private static final Lock LOCK_MOD = new ReentrantLock();

	private static final Lock LOCK_INIT = new ReentrantLock();

	private static final String REGION_NAME = "OXFolderQueryCache";

	private static FolderQueryCacheManager instance;

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private final JCS folderQueryCache;

	private static ReadWriteLock getContextLock(final int cid) {
		final Integer key = Integer.valueOf(cid);
		ReadWriteLock l = contextLocks.get(key);
		if (l == null) {
			LOCK_MOD.lock();
			try {
				if ((l = contextLocks.get(key)) == null) {
					l = new ReentrantReadWriteLock();
					contextLocks.put(key, l);
				}
			} finally {
				LOCK_MOD.unlock();
			}
		}
		return l;
	}

	private FolderQueryCacheManager() throws OXException {
		super();
		try {
			folderQueryCache = JCS.getInstance(REGION_NAME);
		} catch (final CacheException e) {
			throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, REGION_NAME, e
					.getLocalizedMessage());
		}
	}

	public static boolean isInitialized() {
		return initialized.get();
	}

	/**
	 * @return The singleton instance of {@link FolderQueryCacheManager}
	 * @throws OXException
	 *             if instance of {@link FolderQueryCacheManager} cannot be
	 *             initialized
	 */
	public static FolderQueryCacheManager getInstance() throws OXException {
		if (!initialized.get()) {
			LOCK_INIT.lock();
			try {
				if (instance == null) {
					instance = new FolderQueryCacheManager();
					initialized.set(true);
				}
			} finally {
				LOCK_INIT.unlock();
			}
		}
		return instance;
	}

	/**
	 * Releases the singleton instance of {@link FolderQueryCacheManager} and
	 * frees its cache resources through {@link Configuration#freeCache(String)}
	 */
	public static void releaseInstance() {
		if (initialized.get()) {
			LOCK_INIT.lock();
			try {
				if (instance != null) {
					instance = null;
					Configuration.getInstance().freeCache(REGION_NAME);
					initialized.set(false);
				}
			} finally {
				LOCK_INIT.unlock();
			}
		}
	}

	/**
	 * Gets a query result from cache if present, otherwise <code>null</code>
	 * is returned
	 * 
	 * @return query result if present, otherwise <code>null</code>
	 */
	public LinkedList<Integer> getFolderQuery(final int queryNum, final SessionObject session) {
		return getFolderQuery(queryNum, session.getUserId(), session.getContext().getContextId());
	}

	/**
	 * Gets a query result from cache if present, otherwise <code>null</code>
	 * is returned
	 * 
	 * @return query result if present, otherwise <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public LinkedList<Integer> getFolderQuery(final int queryNum, final int userId, final int cid) {
		final Lock ctxReadLock = getContextLock(cid).readLock();
		ctxReadLock.lock();
		try {
			final Map<CacheKey, LinkedList<Integer>> map = (Map<CacheKey, LinkedList<Integer>>) folderQueryCache
					.getFromGroup(createUserKey(userId), createContextKey(cid));
			final LinkedList<Integer> q;
			if (map == null || (q = map.get(createQueryKey(queryNum))) == null) {
				return null;
			}
			return (LinkedList<Integer>) q.clone();
		} finally {
			ctxReadLock.unlock();
		}
	}

	/**
	 * Puts a query result into cache
	 */
	public void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final SessionObject session)
			throws OXException {
		putFolderQuery(queryNum, q, session.getUserId(), session.getContext().getContextId());
	}

	/**
	 * Puts a query result into cache
	 */
	public void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final int userId, final int cid)
			throws OXException {
		putFolderQuery(queryNum, q, userId, cid, false);
	}

	/**
	 * Puts a query result into cache. If <code>append</code> is set and cache
	 * already contains a query result belonging to given <code>queryNum</code>,
	 * given result is going to appended to existing one. Otherwise existing
	 * entries are replaced.
	 */
	public void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final SessionObject session,
			final boolean append) throws OXException {
		putFolderQuery(queryNum, q, session.getUserId(), session.getContext().getContextId(), append);
	}

	/**
	 * Puts a query result into cache. If <code>append</code> is set and cache
	 * already contains a query result belonging to given <code>queryNum</code>,
	 * given result is going to appended to existing one. Otherwise existing
	 * entries are replaced.
	 */
	@SuppressWarnings("unchecked")
	public void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final int userId, final int cid,
			final boolean append) throws OXException {
		if (q == null) {
			return;
		}
		final Lock ctxWriteLock = getContextLock(cid).writeLock();
		ctxWriteLock.lock();
		try {
			boolean insertMap = false;
			Map<CacheKey, LinkedList<Integer>> map = (Map<CacheKey, LinkedList<Integer>>) folderQueryCache
					.getFromGroup(createUserKey(userId), createContextKey(cid));
			if (map == null) {
				map = new HashMap<CacheKey, LinkedList<Integer>>();
				insertMap = true;
			}
			final CacheKey queryKey = createQueryKey(queryNum);
			final LinkedList<Integer> tmp = map.get(queryKey);
			if (tmp == null || !append) {
				map.put(queryKey, (LinkedList<Integer>) q.clone());
			} else {
				tmp.addAll((LinkedList<Integer>) q.clone());
			}
			if (insertMap) {
				folderQueryCache.putInGroup(createUserKey(userId), createContextKey(cid), map);
			}
		} catch (final CacheException e) {
			throw new OXCachingException(OXCachingException.Code.FAILED_PUT, e, new Object[0]);
		} finally {
			ctxWriteLock.unlock();
		}
	}

	/**
	 * Clears all cache entries belonging to given session's context
	 */
	public void invalidateContextQueries(final SessionObject session) {
		invalidateContextQueries(session.getContext().getContextId());
	}

	/**
	 * Clears all cache entries belonging to given context
	 */
	public void invalidateContextQueries(final int cid) {
		final Lock ctxWriteLock = getContextLock(cid).writeLock();
		ctxWriteLock.lock();
		try {
			folderQueryCache.invalidateGroup(createContextKey(cid));
		} finally {
			ctxWriteLock.unlock();
		}
	}

	private final static QueryCacheKey.Module MODULE = QueryCacheKey.Module.FOLDER;

	private static CacheKey createQueryKey(final int queryNum) {
		return new CacheKey(MODULE.getNum(), Integer.valueOf(queryNum));
	}

	private static Integer createUserKey(final int userId) {
		return Integer.valueOf(userId);
	}

	private static String createContextKey(final int cid) {
		return String.valueOf(cid);
	}

}
