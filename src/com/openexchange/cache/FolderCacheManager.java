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
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.ElementEvent;

import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api2.OXException;
import com.openexchange.cache.OXCachingException.Code;
import com.openexchange.configuration.ConfigurationInit;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * The <code>FolderCacheManager</code> holds a JCS cache for
 * <code>FolderObject</code> instances. <b>NOTE:</b> Only cloned versions of
 * <code>FolderObject</code> instances are put into or received from cache.
 * That prevents the danger of further working on and therefore changing cached
 * instances.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class FolderCacheManager {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(FolderCacheManager.class);

	private static final boolean enabled = FolderCacheProperties.isEnableFolderCache();

	private static final Lock LOCK_INIT = new ReentrantLock();

	private static final Map<Integer, ReadWriteLock> contextLocks = new HashMap<Integer, ReadWriteLock>();

	private static final Lock LOCK_MOD = new ReentrantLock();

	private static FolderCacheManager instance;

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private final JCS folderCache;

	private IElementAttributes initialAttribs;

	private static final String FOLDER_CACHE_REGION_NAME = "OXFolderCache";

	private FolderCacheManager() throws OXException {
		super();
		try {
			ConfigurationInit.init();
			Configuration.load();
			folderCache = JCS.getInstance(FOLDER_CACHE_REGION_NAME);
		} catch (final CacheException e) {
			throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, FOLDER_CACHE_REGION_NAME, e
					.getLocalizedMessage());
		} catch (final FileNotFoundException e) {
			throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, FOLDER_CACHE_REGION_NAME, e
					.getLocalizedMessage());
		} catch (final IOException e) {
			throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, FOLDER_CACHE_REGION_NAME, e
					.getLocalizedMessage());
		} catch (final AbstractOXException e) {
			throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, FOLDER_CACHE_REGION_NAME, e
					.getLocalizedMessage());
		}
	}

	public static boolean isInitialized() {
		return initialized.get();
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static FolderCacheManager getInstance() throws FolderCacheNotEnabledException, OXException {
		if (!enabled) {
			throw new FolderCacheNotEnabledException();
		}
		if (!initialized.get()) {
			LOCK_INIT.lock();
			try {
				if (instance == null) {
					instance = new FolderCacheManager();
					initialized.set(true);
				}
			} finally {
				LOCK_INIT.unlock();
			}
		}
		return instance;
	}

	private static final ReadWriteLock getContextLock(final Context ctx) {
		return getContextLock(ctx.getContextId());
	}

	private static final ReadWriteLock getContextLock(final int cid) {
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

	/**
	 * <p>
	 * Fetches <code>FolderObject</code> which matches given object id. If
	 * none found or <code>fromCache</code> is not set the folder will be
	 * loaded from underlying database store and automatically put into cache.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> This method returns a clone of cached
	 * <code>FolderObject</code> instance. Thus any modifications made to the
	 * referenced object will not affect cached version
	 * </p>
	 */
	public FolderObject getFolderObject(final int objectId, final boolean fromCache, final Context ctx,
			final Connection readConArg) throws OXException {
		final Lock ctxReadLock = getContextLock(ctx).readLock();
		ctxReadLock.lock();
		try {
			FolderObject folderObj = null;
			if (fromCache) {
				folderObj = (FolderObject) folderCache.get(new CacheKey(ctx, objectId));
			}
			/*
			 * Either fromCache was false or folder object was not found.
			 */
			if (folderObj == null) {
				/*
				 * Upgrade lock: unlock first to acquire write lock
				 */
				ctxReadLock.unlock();
				final Lock ctxWriteLock = getContextLock(ctx).writeLock();
				ctxWriteLock.lock();
				try {
					folderObj = loadFolderObjectInternal(objectId, ctx, readConArg);
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
			return (FolderObject) folderObj.clone();
		} finally {
			ctxReadLock.unlock();
		}
	}

	/**
	 * <p>
	 * Fetches <code>FolderObject</code> which matches given object id.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> This method returns a clone of cached
	 * <code>FolderObject</code> instance. Thus any modifications made to the
	 * referenced object will not affect cached version
	 * </p>
	 * 
	 * @return matching <code>FolderObject</code> instance else
	 *         <code>null</code>
	 */
	public FolderObject getFolderObject(final int objectId, final Context ctx) {
		final Lock ctxReadLock = getContextLock(ctx).readLock();
		ctxReadLock.lock();
		try {
			final FolderObject retval = (FolderObject) folderCache.get(new CacheKey(ctx, objectId));
			return retval == null ? null : (FolderObject) retval.clone();
		} finally {
			ctxReadLock.unlock();
		}
	}

	/**
	 * <p>
	 * Loads the folder which matches given object id from underlying database
	 * store and puts it into cache.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> This method returns a clone of cached
	 * <code>FolderObject</code> instance. Thus any modifications made to the
	 * referenced object will not affect cached version
	 * </p>
	 * 
	 * @return matching <code>FolderObject</code> instance fetched from
	 *         storage else <code>null</code>
	 */
	public FolderObject loadFolderObject(final int folderId, final Context ctx, final Connection readCon)
			throws OXException {
		final Lock ctxWriteLock = getContextLock(ctx).writeLock();
		ctxWriteLock.lock();
		try {
			/*
			 * Return a copy, NOT a reference
			 */
			return (FolderObject) loadFolderObjectInternal(folderId, ctx, readCon).clone();
		} finally {
			ctxWriteLock.unlock();
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
	 * @throws OXException
	 *             If folder object could not be loaded
	 */
	private FolderObject loadFolderObjectInternal(final int folderId, final Context ctx, final Connection readCon)
			throws OXException {
		if (folderId <= 0) {
			throw new OXFolderNotFoundException(folderId, ctx.getContextId());
		}
		final FolderObject folderObj;
		try {
			folderObj = FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
			final CacheKey key = new CacheKey(ctx, folderId);
			/*
			 * Do not propagate an initial PUT
			 */
			final IElementAttributes attribs = getAppliedAttributes(key, null);
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
		} catch (final CacheException e) {
			throw new OXCachingException(Code.FAILED_PUT, e, new Object[0]);
		}
		/*
		 * Return a reference to cached element
		 */
		return folderObj;
	}

	/**
	 * <p>
	 * Simply puts given <code>FolderObject</code> into cache if object's id
	 * is different to zero.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> This method puts a clone of given <code>FolderObject</code>
	 * instance into cache. Thus any modifications made to the referenced object
	 * will not affect cached version
	 * </p>
	 * 
	 * @param folderObj
	 *            the folder object
	 * @param ctx
	 *            the context
	 * @throws OXException
	 */
	public void putFolderObject(final FolderObject folderObj, final Context ctx) throws OXException {
		putFolderObject(folderObj, ctx, true, null);
	}

	/**
	 * <p>
	 * Simply puts given <code>FolderObject</code> into cache if object's id
	 * is different to zero. If flag <code>overwrite</code> is set to
	 * <code>false</code> then this method returns immediately if cache
	 * already holds a matching entry.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> This method puts a clone of given <code>FolderObject</code>
	 * instance into cache. Thus any modifications made to the referenced object
	 * will not affect cached version
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
	 * @throws OXException
	 */
	public void putFolderObject(final FolderObject folderObj, final Context ctx, final boolean overwrite,
			final IElementAttributes elemAttribs) throws OXException {
		try {
			if (!folderObj.containsObjectID()) {
				throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, FolderFields.ID, Integer.valueOf(-1),
						Integer.valueOf(ctx.getContextId()));
			}
			final CacheKey ck = new CacheKey(ctx, folderObj.getObjectID());
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
					final IElementAttributes attribs;
					if (elemAttribs == null) {
						attribs = getInitialAttributes();
					} else {
						attribs = elemAttribs;
						attribs.setIsLateral(false);
					}
					folderCache.put(ck, folderObj.clone(), attribs);
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
					final IElementAttributes attribs = getAppliedAttributes(ck, elemAttribs);
					if (attribs == null) {
						/*
						 * Put with default attributes
						 */
						folderCache.put(ck, folderObj.clone());
					} else {
						folderCache.put(ck, folderObj.clone(), attribs);
					}
				} finally {
					ctxWriteLock.unlock();
				}
			}
		} catch (final CacheException e) {
			throw new OXCachingException(Code.FAILED_PUT, e, new Object[0]);
		}
	}

	/**
	 * Removes matching <code>FolderObject</code> instance from cache
	 * 
	 * @param key
	 *            the key
	 * @param ctx
	 *            the context
	 * @throws OXException
	 */
	public void removeFolderObject(final int key, final Context ctx) throws OXException {
		/*
		 * Remove object in cache if exist
		 */
		if (key > 0) {
			final Lock ctxWriteLock = getContextLock(ctx).writeLock();
			ctxWriteLock.lock();
			try {
				folderCache.remove(new CacheKey(ctx, key));
			} catch (final CacheException e) {
				throw new OXCachingException(Code.FAILED_REMOVE, e, new Object[0]);
			} finally {
				ctxWriteLock.unlock();
			}
		}
	}

	private final IElementAttributes getAppliedAttributes(final CacheKey key, final IElementAttributes givenAttribs)
			throws CacheException {
		if (folderCache.get(key) != null) {
			/*
			 * No intial PUT; just return given attributes
			 */
			return givenAttribs;
		}
		if (givenAttribs == null) {
			return getInitialAttributes();
		}
		givenAttribs.setIsLateral(false);
		return givenAttribs;
	}

	private final IElementAttributes getInitialAttributes() throws CacheException {
		if (initialAttribs != null) {
			return initialAttribs;
		}
		initialAttribs = folderCache.getDefaultElementAttributes();
		initialAttribs.setIsLateral(false);
		return initialAttribs;
	}

	/**
	 * Returns default element attributes for this cache
	 * 
	 * @return default element attributes for this cache
	 * @throws OXException
	 */
	public IElementAttributes getDefaultFolderObjectAttributes() throws OXException {
		try {
			/*
			 * Returns a copy NOT a reference
			 */
			return folderCache.getDefaultElementAttributes();
		} catch (final CacheException e) {
			throw new OXCachingException(Code.FAILED_ATTRIBUTE_RETRIEVAL, e, new Object[0]);
		}
	}

	public static class FolderCacheEventHandler extends ElementEventHandlerWrapper {

		@Override
		protected void onExceededIdletimeBackground(final ElementEvent event) {
			super.onExceededIdletimeBackground(event);
			if (LOG.isTraceEnabled()) {
				LOG.trace("onExceededIdletimeBackground()");
			}
		}

	}

}
