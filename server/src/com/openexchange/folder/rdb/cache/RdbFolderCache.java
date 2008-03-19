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

package com.openexchange.folder.rdb.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.folder.FolderException;
import com.openexchange.folder.rdb.RdbFolder;
import com.openexchange.folder.rdb.RdbFolderException;
import com.openexchange.folder.rdb.RdbFolderID;
import com.openexchange.folder.rdb.RdbFolderProperties;
import com.openexchange.folder.rdb.sql.RdbFolderSQL;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link RdbFolderCache} - The folder cache for relation database folder
 * storage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbFolderCache {

	/*
	 * Static stuff
	 */

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RdbFolderCache.class);

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static final String FOLDER_CACHE_REGION_NAME = "OXFolderCache";

	private static final Map<Integer, ReadWriteLock> contextLocks = new HashMap<Integer, ReadWriteLock>();

	private static final Lock LOCK_MOD = new ReentrantLock();

	private static RdbFolderCache instance;

	/**
	 * Gets the singleton instance for folder cache
	 * 
	 * @return The singleton instance for folder cache
	 * @throws FolderException
	 *             If either initialization fails or cache is not enabled
	 *             through properties
	 */
	public static RdbFolderCache getInstance() throws FolderException {
		if (!RdbFolderProperties.getInstance().isEnableFolderCache()) {
			throw new RdbFolderException(RdbFolderException.Code.CACHE_NOT_ENABLED);
		}
		if (!initialized.get()) {
			synchronized (initialized) {
				if (instance == null) {
					instance = new RdbFolderCache();
					initialized.set(true);
				}
			}
		}
		return instance;
	}

	/**
	 * Releases the folder cache and disposes its entries
	 */
	public static void releaseInstance() {
		if (!RdbFolderProperties.getInstance().isEnableFolderCache()) {
			return;
		}
		if (initialized.get()) {
			synchronized (initialized) {
				if (instance != null) {
					instance = null;
					try {
						ServerServiceRegistry.getInstance().getService(CacheService.class).freeCache(
								FOLDER_CACHE_REGION_NAME);
					} catch (final CacheException e) {
						LOG.error(e.getLocalizedMessage(), e);
					}
					initialized.set(false);
				}
			}
		}
	}

	private static ReadWriteLock getContextLock(final Context ctx) {
		final int cid = ctx.getContextId();
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

	private static CacheKey getCacheKey(final int cid, final int objectId) {
		return ServerServiceRegistry.getInstance().getService(CacheService.class).newCacheKey(cid, objectId);
	}

	/*
	 * Member stuff
	 */
	private ElementAttributes initialAttribs;

	private final Cache folderCache;

	/**
	 * Initializes a new {@link RdbFolderCache}
	 * 
	 * @throws FolderException
	 *             If initialization fails
	 */
	private RdbFolderCache() throws FolderException {
		super();
		final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
		try {
			folderCache = cacheService.getCache(FOLDER_CACHE_REGION_NAME);
		} catch (final CacheException e) {
			throw new FolderException(e);
		}
	}

	/**
	 * Fetches the folder object which matches given object id. If none found or
	 * <code>fromCache</code> is not set the folder will be loaded from
	 * underlying database storage and automatically put into cache.
	 * <p>
	 * <b>NOTE:</b> This method returns a clone of cached folder object. Thus
	 * any modifications made to the referenced object will not affect cached
	 * version
	 * </p>
	 * 
	 * @return The corresponding folder object
	 * @throws FolderException
	 *             If folder object cannot be loaded from database storage on
	 *             cache miss
	 */
	public RdbFolder getFolderObject(final int objectId, final boolean fromCache, final Context ctx,
			final Connection readCon) throws FolderException {
		final Lock ctxReadLock = getContextLock(ctx).readLock();
		ctxReadLock.lock();
		try {
			RdbFolder folder = null;
			if (fromCache) {
				folder = (RdbFolder) folderCache.get(getCacheKey(ctx.getContextId(), objectId));
			}
			/*
			 * Either fromCache was false or folder object was not found.
			 */
			if (folder == null) {
				/*
				 * Upgrade lock: unlock first to acquire write lock
				 */
				ctxReadLock.unlock();
				final Lock ctxWriteLock = getContextLock(ctx).writeLock();
				ctxWriteLock.lock();
				try {
					folder = loadFolderObjectInternal(objectId, ctx, readCon);
				} finally {
					/*
					 * Downgrade lock: reacquire read without giving up write
					 * lock and...
					 */
					ctxReadLock.lock();
					/*
					 * ... unlock write.
					 */
					ctxWriteLock.unlock();
				}
			}
			/*
			 * Return a copy, NOT a reference
			 */
			return folder == null ? null : (RdbFolder) folder.clone();
		} finally {
			ctxReadLock.unlock();
		}
	}

	/**
	 * Fetches folder object which matches given object id.
	 * <p>
	 * <b>NOTE:</b> This method returns a clone of cached folder object. Thus
	 * any modifications made to the referenced object will not affect cached
	 * version
	 * </p>
	 * 
	 * @return The corresponding folder object or <code>null</code> on cache
	 *         miss
	 */
	public RdbFolder getFolderObject(final int objectId, final Context ctx) {
		final Lock ctxReadLock = getContextLock(ctx).readLock();
		ctxReadLock.lock();
		try {
			final RdbFolder retval = (RdbFolder) folderCache.get(getCacheKey(ctx.getContextId(), objectId));
			return retval == null ? null : (RdbFolder) retval.clone();
		} finally {
			ctxReadLock.unlock();
		}
	}

	/**
	 * Loads the folder which matches given object id from underlying database
	 * store and puts it into cache.
	 * <p>
	 * <b>NOTE:</b> This method returns a clone of cached folder object. Thus
	 * any modifications made to the referenced object will not affect cached
	 * version
	 * </p>
	 * 
	 * @return matching folder object fetched from storage else
	 *         <code>null</code>
	 * @throws FolderException
	 *             If folder cannot be loaded
	 */
	public RdbFolder loadFolderObject(final int folderId, final Context ctx, final Connection readCon)
			throws FolderException {
		final Lock ctxWriteLock = getContextLock(ctx).writeLock();
		ctxWriteLock.lock();
		try {
			/*
			 * Return a copy, NOT a reference
			 */
			final RdbFolder folder = loadFolderObjectInternal(folderId, ctx, readCon);
			return folder == null ? null : (RdbFolder) folder.clone();
		} finally {
			ctxWriteLock.unlock();
		}
	}

	/**
	 * Simply puts given folder object into cache if object's id is different to
	 * zero.
	 * <p>
	 * <b>NOTE:</b> This method puts a clone of given folder object into cache.
	 * Thus any modifications made to the referenced object will not affect
	 * cached version
	 * </p>
	 * 
	 * @param folderObj
	 *            the folder object
	 * @param ctx
	 *            the contexts
	 * @throws FolderException
	 *             If put into cache fails
	 */
	public void putFolderObject(final RdbFolder folderObj, final Context ctx) throws FolderException {
		putFolderObject(folderObj, ctx, true, null);
	}

	/**
	 * Simply puts given folder object into cache if object's id is different to
	 * zero. If flag <code>overwrite</code> is set to <code>false</code>
	 * then this method returns immediately if cache already holds a matching
	 * entry.
	 * <p>
	 * <b>NOTE:</b> This method puts a clone of given folder object into cache.
	 * Thus any modifications made to the referenced object will not affect
	 * cached version
	 * </p>
	 * 
	 * @param folderObj
	 *            the folder object
	 * @param ctx
	 *            the context
	 * @param overwrite
	 *            <code>true</code> to overwrite; otherwise <code>false</code>
	 * @param elemAttribs
	 *            the element's attributes. Set to <code>null</code> to use
	 *            the default attributes
	 * @throws FolderException
	 *             If put into cache fails
	 */
	public void putFolderObject(final RdbFolder folderObj, final Context ctx, final boolean overwrite,
			final ElementAttributes elemAttribs) throws FolderException {
		try {
			final CacheKey ck = getCacheKey(ctx.getContextId(), folderObj.getFolderID().fuid);
			if (!overwrite) {
				if (folderCache.get(ck) != null) {
					return;
				}
				/*
				 * Wait for other threads that currently own PUT lock
				 */
				final Lock ctxWriteLock = getContextLock(ctx).writeLock();
				ctxWriteLock.lock();
				try {
					if (folderCache.get(ck) != null) {
						/*
						 * Another thread made a PUT in the meantime. Return
						 * cause we may not overwrite.
						 */
						return;
					}
					/*
					 * Since this must be the initial PUT, disable this element
					 * for lateral cache distribution
					 */
					final ElementAttributes attribs;
					if (elemAttribs == null) {
						attribs = getInitialAttributes();
					} else {
						attribs = elemAttribs;
						attribs.setIsLateral(false);
					}
					folderCache.put(ck, (RdbFolder) folderObj.clone(), attribs);
				} finally {
					ctxWriteLock.unlock();
				}
			} else {
				/*
				 * Put clone of new object into cache. If there is currently an
				 * object associated with this key in the region it is replaced.
				 */
				final Lock ctxWriteLock = getContextLock(ctx).writeLock();
				ctxWriteLock.lock();
				try {
					final ElementAttributes attribs = getAppliedAttributes(ck, elemAttribs);
					if (attribs == null) {
						/*
						 * Put with default attributes
						 */
						folderCache.put(ck, (RdbFolder) folderObj.clone());
					} else {
						folderCache.put(ck, (RdbFolder) folderObj.clone(), attribs);
					}
				} finally {
					ctxWriteLock.unlock();
				}
			}
		} catch (final CacheException e) {
			throw new FolderException(e);
		}
	}

	/**
	 * Removes matching folder object from cache
	 * 
	 * @param key
	 *            the key
	 * @param ctx
	 *            the context
	 * @throws FolderException
	 *             If removal from cache fails
	 */
	public void removeFolderObject(final int key, final Context ctx) throws FolderException {
		/*
		 * Remove object in cache if exist
		 */
		if (key > 0) {
			final Lock ctxWriteLock = getContextLock(ctx).writeLock();
			ctxWriteLock.lock();
			try {
				folderCache.remove(getCacheKey(ctx.getContextId(), key));
			} catch (final CacheException e) {
				throw new FolderException(e);
			} finally {
				ctxWriteLock.unlock();
			}
		}
	}

	/**
	 * Loads the folder object from underlying database storage whose id matches
	 * given parameter <code>folderId</code>.
	 * <p>
	 * The returned object references the actually cached entry
	 * 
	 * @param folderId
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @param readCon
	 *            A readable connection or <code>null</code> to fetch a new
	 *            one from connection pool
	 * @return The object referencing the actually cached entry
	 * @throws FolderException
	 *             If folder cannot be loaded
	 */
	private RdbFolder loadFolderObjectInternal(final int folderId, final Context ctx, final Connection readCon)
			throws FolderException {
		if (folderId <= 0) {
			throw new FolderException(FolderException.Code.FOLDER_NOT_FOUND, new RdbFolderID(folderId, ctx));
		}
		final RdbFolder folderObj;
		try {
			folderObj = readCon == null ? RdbFolderSQL.loadFolder(folderId, ctx) : RdbFolderSQL.loadFolder(folderId,
					ctx, readCon);
			final CacheKey key = getCacheKey(ctx.getContextId(), folderId);
			if (null == folderObj) {
				folderCache.remove(key);
			} else {
				/*
				 * Do not propagate an initial PUT
				 */
				final ElementAttributes attribs = getAppliedAttributes(key, null);
				if (attribs == null) {
					/*
					 * Put folder into cache
					 */
					folderCache.put(key, folderObj);
				} else {
					/*
					 * Disable lateral distribution for this element
					 */
					folderCache.put(key, folderObj, attribs);
				}
			}
		} catch (final CacheException e) {
			throw new FolderException(e);
		} catch (final DBPoolingException e) {
			throw new FolderException(e);
		} catch (final SQLException e) {
			throw new RdbFolderException(RdbFolderException.Code.SQL_ERROR, e, e.getLocalizedMessage());
		}
		/*
		 * Return a reference to cached element
		 */
		return folderObj;
	}

	private ElementAttributes getAppliedAttributes(final CacheKey key, final ElementAttributes givenAttribs)
			throws CacheException {
		if (folderCache.get(key) != null) {
			/*
			 * No initial PUT; just return given attributes
			 */
			return givenAttribs;
		}
		if (givenAttribs == null) {
			return getInitialAttributes();
		}
		givenAttribs.setIsLateral(false);
		return givenAttribs;
	}

	private ElementAttributes getInitialAttributes() throws CacheException {
		if (initialAttribs != null) {
			return initialAttribs;
		}
		initialAttribs = folderCache.getDefaultElementAttributes();
		initialAttribs.setIsLateral(false);
		return initialAttribs;
	}
}
