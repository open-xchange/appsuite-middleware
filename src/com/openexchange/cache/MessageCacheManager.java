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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.ConfigurationInit;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.MailFolderObject;
import com.openexchange.groupware.container.mail.MessageCacheObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * MessageCacheManager - a tiny cache for instances of
 * <code>MessageCacheObject</code> which does only operate in local memory and
 * does not use any auxiliary.
 * <p>
 * Furthermore it is heavily volatile because it's intended to fasten list
 * requests on IMAP folders. All user-constrained entries are going to be
 * deleted (if any present) and refilled on every list request.
 * <p>
 * Moreover the number of allowed cache entries per user is limited to constant
 * <code>{@link IMAPProperties#getMessageFetchLimit()}</code> defined in file
 * "imap.properties".
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @see com.openexchange.imap.IMAPProperties#getMessageFetchLimit()
 */
public class MessageCacheManager {

	private static final String STR_UNCHECKED = "unchecked";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageCacheManager.class);

	private static final String MSG_CACHE_REGION_NAME = "OXMessageCache";

	private static final Lock INIT_LOCK = new ReentrantLock();

	private static final Map<Integer, ReadWriteLock> contextLocks = new HashMap<Integer, ReadWriteLock>();

	private static final Lock LOCK_MOD = new ReentrantLock();

	private static MessageCacheManager instance;

	private static boolean initialized;

	private final JCS msgCache;

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

	private MessageCacheManager() throws OXCachingException {
		super();
		try {
			ConfigurationInit.init();
			Configuration.load();
			msgCache = JCS.getInstance(MSG_CACHE_REGION_NAME);
		} catch (final CacheException e) {
			LOG.error(e.getMessage(), e);
			throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, MSG_CACHE_REGION_NAME, e.getMessage());
		} catch (final FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
			throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, MSG_CACHE_REGION_NAME, e.getMessage());
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
			throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, MSG_CACHE_REGION_NAME, e.getMessage());
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, MSG_CACHE_REGION_NAME, e.getMessage());
		}
	}

	/**
	 * @return <code>true</code> if cache is initialized, <code>false</code>
	 *         otherwise
	 */
	public final static boolean isInitialized() {
		return initialized;
	}

	/**
	 * @return the singleton instance of <code>MessageCacheManager</code>
	 */
	public final static MessageCacheManager getInstance() throws OXException {
		if (!initialized) {
			INIT_LOCK.lock();
			try {
				if (instance == null) {
					instance = new MessageCacheManager();
					initialized = true;
				}
			} finally {
				INIT_LOCK.unlock();
			}
		}
		return instance;
	}

	/**
	 * @return <code>MessageCacheObject</code> instance matching given folder
	 *         and UID
	 */
	public final MessageCacheObject getMessage(final SessionObject session, final long msgUID, final String folder) {
		return getMessage(session.getUserObject().getId(), msgUID, folder, session.getContext());
	}

	/**
	 * @return <code>MessageCacheObject</code> instance matching given folder
	 *         and UID
	 */
	@SuppressWarnings(STR_UNCHECKED)
	public final MessageCacheObject getMessage(final int user, final long msgUID, final String folder, final Context ctx) {
		final Lock ctxReadLock = getContextLock(ctx.getContextId()).readLock();
		ctxReadLock.lock();
		try {
			final Map<String, MessageCacheObject> msgMap = (HashMap<String, MessageCacheObject>) msgCache
					.get(getMapKey(user, ctx));
			if (msgMap == null) {
				return null;
			}
			return msgMap.get(getMsgKey(msgUID, folder));
		} finally {
			ctxReadLock.unlock();
		}
	}

	/**
	 * @return <code>MessageCacheObject</code> instances matching given folder
	 *         and UIDs
	 */
	public final MessageCacheObject[] getMessages(final SessionObject session, final long[] msgUIDs, final String folder) {
		return getMessages(session.getUserObject().getId(), msgUIDs, folder, session.getContext());
	}

	/**
	 * @return <code>MessageCacheObject</code> instances matching given folder
	 *         and UIDs
	 */
	@SuppressWarnings(STR_UNCHECKED)
	public final MessageCacheObject[] getMessages(final int user, final long[] msgUIDs, final String folder,
			final Context ctx) {
		final Lock ctxReadLock = getContextLock(ctx.getContextId()).readLock();
		ctxReadLock.lock();
		try {
			final Map<String, MessageCacheObject> msgMap = (ConcurrentHashMap<String, MessageCacheObject>) msgCache
					.get(getMapKey(user, ctx));
			if (msgMap == null) {
				return null;
			}
			final MessageCacheObject[] retval = new MessageCacheObject[msgUIDs.length];
			for (int i = 0; i < retval.length; i++) {
				retval[i] = msgMap.get(getMsgKey(msgUIDs[i], folder));
			}
			return retval;
		} finally {
			ctxReadLock.unlock();
		}
	}

	/**
	 * Puts all instances of <code>MessageCacheObject</code> contained in
	 * given <code>SearchIterator</code> reference into cache
	 * 
	 * @param session -
	 *            the session containing user data
	 * @param iter -
	 *            the iterator
	 * @throws OXException -
	 *             if a cache error occurs
	 * @throws SearchIteratorException -
	 *             if iterator traversal fails
	 */
	public final void putIteratorMessages(final SessionObject session, final SearchIterator iter) throws OXException,
			SearchIteratorException {
		putIteratorMessages(session.getUserObject().getId(), iter, session.getContext());
	}

	/**
	 * Puts all instances of <code>MessageCacheObject</code> contained in
	 * given <code>SearchIterator</code> reference into cache
	 * 
	 * @param user -
	 *            the user id
	 * @param iter -
	 *            the ierator
	 * @param ctx -
	 *            the context
	 * @throws OXException -
	 *             if a cache error occurs
	 * @throws SearchIteratorException -
	 *             if iterator traversal fails
	 */
	@SuppressWarnings(STR_UNCHECKED)
	public final void putIteratorMessages(final int user, final SearchIterator iter, final Context ctx)
			throws OXException, SearchIteratorException {
		if (iter == null) {
			return;
		}
		final Lock ctxWriteLock = getContextLock(ctx.getContextId()).writeLock();
		ctxWriteLock.lock();
		try {
			Map<String, MessageCacheObject> msgMap = (ConcurrentHashMap<String, MessageCacheObject>) msgCache
					.get(getMapKey(user, ctx));
			boolean insert = false;
			if (msgMap == null) {
				/*
				 * Does not exist in cache, yet
				 */
				msgMap = new ConcurrentHashMap<String, MessageCacheObject>();
				insert = true;
			}
			if (iter.hasSize()) {
				final int size = iter.size();
				for (int i = 0; i < size; i++) {
					final MessageCacheObject msg = (MessageCacheObject) iter.next();
					msgMap.put(getMsgKey(msg.getUid(), MailFolderObject.prepareFullname(msg.getFolderFullname(), msg
							.getSeparator())), msg);
				}
			} else {
				while (iter.hasNext()) {
					final MessageCacheObject msg = (MessageCacheObject) iter.next();
					msgMap.put(getMsgKey(msg.getUid(), MailFolderObject.prepareFullname(msg.getFolderFullname(), msg
							.getSeparator())), msg);
				}
			}
			if (insert) {
				try {
					msgCache.put(getMapKey(user, ctx), msgMap);
				} catch (final CacheException e) {
					throw new OXCachingException(OXCachingException.Code.FAILED_PUT, e, new Object[0]);
				}
			}
		} finally {
			ctxWriteLock.unlock();
		}
	}

	/**
	 * Returns user's message map which is kept in cache. If none is in cache, a
	 * new map is going to be created, put into cache and returned.
	 * 
	 * @param session -
	 *            the session
	 * @return user's message map
	 * @throws OXCachingException -
	 *             if a cache error occurs
	 */
	public final Map<String, MessageCacheObject> getUserMessageMap(final SessionObject session)
			throws OXCachingException {
		return getUserMessageMap(session.getUserObject().getId(), session.getContext());
	}

	/**
	 * Returns user's message map which is kept in cache. If none is in cache, a
	 * new map is going to be created, put into cache and returned.
	 * 
	 * @param user -
	 *            the user ID
	 * @param ctx -
	 *            the context
	 * @return user's message map
	 * @throws OXCachingException -
	 *             if a cache error occurs
	 */
	@SuppressWarnings(STR_UNCHECKED)
	public final Map<String, MessageCacheObject> getUserMessageMap(final int user, final Context ctx)
			throws OXCachingException {
		final Lock ctxReadLock = getContextLock(ctx.getContextId()).readLock();
		ctxReadLock.lock();
		try {
			final CacheKey mapKey = getMapKey(user, ctx);
			Map<String, MessageCacheObject> msgMap = (ConcurrentHashMap<String, MessageCacheObject>) msgCache
					.get(mapKey);
			if (msgMap == null) {
				/*
				 * Does not exist in cache, yet
				 */
				msgMap = new ConcurrentHashMap<String, MessageCacheObject>();
				try {
					msgCache.put(mapKey, msgMap);
				} catch (final CacheException e) {
					throw new OXCachingException(OXCachingException.Code.FAILED_PUT, e, new Object[0]);
				}
			}
			/*
			 * Return reference
			 */
			return msgMap;
		} finally {
			ctxReadLock.unlock();
		}
	}

	/**
	 * Puts given <code>MessageCacheObject</code> instance into cache
	 */
	public final void putMessage(final SessionObject session, final long msgUID, final MessageCacheObject msg)
			throws OXException {
		putMessage(session.getUserObject().getId(), msgUID, msg, session.getContext());
	}

	/**
	 * Puts given <code>MessageCacheObject</code> instance into cache
	 */
	@SuppressWarnings(STR_UNCHECKED)
	public final void putMessage(final int user, final long msgUID, final MessageCacheObject msg, final Context ctx)
			throws OXException {
		if (msg == null) {
			return;
		}
		final Lock ctxWriteLock = getContextLock(ctx.getContextId()).writeLock();
		ctxWriteLock.lock();
		try {
			final CacheKey mapKey = getMapKey(user, ctx);
			Map<String, MessageCacheObject> msgMap = (ConcurrentHashMap<String, MessageCacheObject>) msgCache
					.get(mapKey);
			boolean insert = false;
			if (msgMap == null) {
				/*
				 * Does not exist in cache, yet
				 */
				msgMap = new ConcurrentHashMap<String, MessageCacheObject>();
				insert = true;
			}
			msgMap.put(
					getMsgKey(msgUID, MailFolderObject.prepareFullname(msg.getFolderFullname(), msg.getSeparator())),
					msg);
			if (insert) {
				try {
					msgCache.put(mapKey, msgMap);
				} catch (final CacheException e) {
					throw new OXCachingException(OXCachingException.Code.FAILED_PUT, e, new Object[0]);
				}
			}
		} finally {
			ctxWriteLock.unlock();
		}
	}

	/**
	 * Removes <code>MessageCacheObject</code> instance from cache which
	 * matches given folder and UID
	 */
	public final void removeMessage(final SessionObject session, final long msgUID, final String folder) {
		removeMessage(session.getUserObject().getId(), msgUID, folder, session.getContext());
	}

	/**
	 * Removes <code>MessageCacheObject</code> instance from cache which
	 * matches given folder and UID
	 */
	@SuppressWarnings(STR_UNCHECKED)
	public final void removeMessage(final int user, final long msgUID, final String folder, final Context ctx) {
		final Lock ctxWriteLock = getContextLock(ctx.getContextId()).writeLock();
		ctxWriteLock.lock();
		try {
			final Map<String, MessageCacheObject> msgMap = (ConcurrentHashMap<String, MessageCacheObject>) msgCache
					.get(getMapKey(user, ctx));
			if (msgMap == null) {
				return;
			}
			msgMap.remove(getMsgKey(msgUID, folder));
		} finally {
			ctxWriteLock.unlock();
		}
	}

	/**
	 * @return <code>true</code> if cache currently contains messages
	 *         belonging to given user, <code>false</code> otherwise
	 */
	public final boolean containsUserMessages(final SessionObject session) {
		return containsUserMessages(session.getUserObject().getId(), session.getContext());
	}

	/**
	 * @return <code>true</code> if cache currently contains messages
	 *         belonging to given user, <code>false</code> otherwise
	 */
	public final boolean containsUserMessages(final int user, final Context ctx) {
		try {
			return (msgCache.get(getMapKey(user, ctx)) != null);
		} catch (final Exception e) {
			return false;
		}
	}

	/**
	 * Clears cache from messages belonging to given user
	 */
	public void clearUserMessages(final SessionObject session) throws OXException {
		clearUserMessages(session.getUserObject().getId(), session.getContext());
	}

	/**
	 * Clears cache from messages belonging to given user
	 */
	public void clearUserMessages(final int user, final Context ctx) throws OXException {
		final Lock ctxWriteLock = getContextLock(ctx.getContextId()).writeLock();
		ctxWriteLock.lock();
		try {
			msgCache.remove(getMapKey(user, ctx));
		} catch (final CacheException e) {
			throw new OXCachingException(OXCachingException.Code.FAILED_REMOVE, e, new Object[0]);
		} finally {
			ctxWriteLock.unlock();
		}
	}

	private static final CacheKey getMapKey(final int user, final Context ctx) {
		return new CacheKey(ctx, user);
	}

	public static final String getMsgKey(final long uid, final String folder) {
		return new StringBuilder(32).append(uid).append('@').append(folder).toString();
	}

}
