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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.ConfigurationInit;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * FolderQueryCacheManager
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderQueryCacheManager {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(FolderQueryCacheManager.class);

	private static final Lock LOCK_INIT = new ReentrantLock();

	private static final String REGION_NAME = "OXFolderQueryCache";

	private static FolderQueryCacheManager instance;

	private static boolean initialized;

	private final JCS folderQueryCache;

	private final Lock LOCK_MODIFY = new ReentrantLock();

	private final Condition WAIT = LOCK_MODIFY.newCondition();

	private boolean busy;

	private FolderQueryCacheManager() throws OXException {
		super();
		try {
			ConfigurationInit.init();
			Configuration.load();
			folderQueryCache = JCS.getInstance(REGION_NAME);
		} catch (final CacheException e) {
			throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, REGION_NAME, e
					.getLocalizedMessage());
		} catch (final FileNotFoundException e) {
			throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, REGION_NAME, e
					.getLocalizedMessage());
		} catch (final IOException e) {
			throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, REGION_NAME, e
					.getLocalizedMessage());
		} catch (final AbstractOXException e) {
			throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, REGION_NAME, e
					.getLocalizedMessage());
		}
	}

	public final static boolean isInitialized() {
		return initialized;
	}

	public final static FolderQueryCacheManager getInstance() throws OXException {
		if (!initialized) {
			LOCK_INIT.lock();
			try {
				if (instance == null) {
					instance = new FolderQueryCacheManager();
					initialized = true;
				}
			} finally {
				LOCK_INIT.unlock();
			}
		}
		return instance;
	}

	/**
	 * Gets a query result from cache if present, otherwise <code>null</code>
	 * is returned
	 * 
	 * @return query result if present, otherwise <code>null</code>
	 */
	public final LinkedList<Integer> getFolderQuery(final int queryNum, final SessionObject session) {
		return getFolderQuery(queryNum, session.getUserObject().getId(), session.getContext().getContextId());
	}

	/**
	 * Gets a query result from cache if present, otherwise <code>null</code>
	 * is returned
	 * 
	 * @return query result if present, otherwise <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public final LinkedList<Integer> getFolderQuery(final int queryNum, final int userId, final int cid) {
		if (busy) {
			/*
			 * Another thread performs a PUT at the moment
			 */
			LOCK_MODIFY.lock();
			try {
				while (busy) {
					WAIT.await();
				}
			} catch (final InterruptedException e) {
				LOG.error(e.getMessage(), e);
			} finally {
				LOCK_MODIFY.unlock();
			}
		}
		final Map<CacheKey, LinkedList<Integer>> map = (Map<CacheKey, LinkedList<Integer>>) folderQueryCache
				.getFromGroup(createUserKey(userId), createContextKey(cid));
		final LinkedList<Integer> q;
		if (map == null || (q = map.get(createQueryKey(queryNum))) == null) {
			return null;
		}
		return (LinkedList<Integer>) q.clone();
	}

	/**
	 * Puts a query result into cache
	 */
	public final void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final SessionObject session)
			throws OXException {
		putFolderQuery(queryNum, q, session.getUserObject().getId(), session.getContext().getContextId());
	}

	/**
	 * Puts a query result into cache
	 */
	public final void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final int userId, final int cid)
			throws OXException {
		putFolderQuery(queryNum, q, userId, cid, false);
	}

	/**
	 * Puts a query result into cache. If <code>append</code> is set and cache
	 * already contains a query result belonging to given <code>queryNum</code>,
	 * given result is going to appended to existing one. Otherwise existing
	 * entries are replaced.
	 */
	public final void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final SessionObject session,
			final boolean append) throws OXException {
		putFolderQuery(queryNum, q, session.getUserObject().getId(), session.getContext().getContextId(), append);
	}

	/**
	 * Puts a query result into cache. If <code>append</code> is set and cache
	 * already contains a query result belonging to given <code>queryNum</code>,
	 * given result is going to appended to existing one. Otherwise existing
	 * entries are replaced.
	 */
	@SuppressWarnings("unchecked")
	public final void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final int userId, final int cid,
			final boolean append) throws OXException {
		if (q == null) {
			return;
		}
		LOCK_MODIFY.lock();
		try {
			busy = true;
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
			busy = false;
			WAIT.signalAll();
			LOCK_MODIFY.unlock();
		}
	}

	/**
	 * Clears all cache entries belonging to given context
	 */
	public final void invalidateContextQueries(final SessionObject session) {
		invalidateContextQueries(session.getContext().getContextId());
	}

	/**
	 * Clears all cache entries belonging to given context
	 */
	public final void invalidateContextQueries(final int cid) {
		LOCK_MODIFY.lock();
		try {
			busy = true;
			folderQueryCache.invalidateGroup(createContextKey(cid));
		} finally {
			busy = false;
			WAIT.signalAll();
			LOCK_MODIFY.unlock();
		}
	}

	private final static QueryCacheKey.Module MODULE = QueryCacheKey.Module.FOLDER;

	private final static CacheKey createQueryKey(final int queryNum) {
		return new CacheKey(MODULE.getNum(), Integer.valueOf(queryNum));
	}

	private final static Integer createUserKey(final int userId) {
		return Integer.valueOf(userId);
	}
	
	private final static String createContextKey(final int cid) {
		return String.valueOf(cid);
	}

}
