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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.IElementAttributes;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.ConfigurationInit;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.connection.IMAPConnection;
import com.openexchange.sessiond.SessionObject;

/**
 * IMAPConnectionCacheManager - just an idle-time depending cache
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IMAPConnectionCacheManager {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPConnectionCacheManager.class);

	private static final String IMAP_CON_CACHE_REGION_NAME = "OXIMAPConCache";

	private static final Lock LOCK = new ReentrantLock();

	private static IMAPConnectionCacheManager instance;

	private static boolean initialized;

	private final JCS imapConCache;
	
	private final Lock LOCK_MODIFY;

	private IMAPConnectionCacheManager() throws OXCachingException {
		super();
		try {
			ConfigurationInit.init();
			Configuration.load();
			imapConCache = JCS.getInstance(IMAP_CON_CACHE_REGION_NAME);
			LOCK_MODIFY = new ReentrantLock();
			/*
			 * Add element event handler to default element attributes
			 */
			final IMAPConnectionEventHandler eventHandler = new IMAPConnectionEventHandler();
			final IElementAttributes attributes = imapConCache.getDefaultElementAttributes();
			attributes.addElementEventHandler(eventHandler);
			imapConCache.setDefaultElementAttributes(attributes);
		} catch (CacheException e) {
			LOG.error(e.getMessage(), e);
			throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, IMAP_CON_CACHE_REGION_NAME, e
					.getMessage());
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
			throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, IMAP_CON_CACHE_REGION_NAME, e
					.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, IMAP_CON_CACHE_REGION_NAME, e
					.getMessage());
		} catch (AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, IMAP_CON_CACHE_REGION_NAME, e
					.getMessage());
		}
	}

	public final static boolean isInitialized() {
		return initialized;
	}

	public final static IMAPConnectionCacheManager getInstance() throws OXException {
		if (!initialized) {
			LOCK.lock();
			try {
				if (instance == null) {
					instance = new IMAPConnectionCacheManager();
					initialized = true;
				}
			} finally {
				LOCK.unlock();
			}
		}
		return instance;
	}

	public final IMAPConnection removeIMAPConnection(final SessionObject session) throws OXException {
		final CacheKey key = getUserKey(session.getUserObject().getId(), session.getContext());
		if (imapConCache.get(key) == null) {
			/*
			 * Connection is not available. Return immediately.
			 */
			return null;
		}
		LOCK_MODIFY.lock();
		try {
			final IMAPConnection retval = (IMAPConnection) imapConCache.get(key);
			/*
			 * Still available?
			 */
			if (retval == null) {
				return null;
			}
			imapConCache.remove(key);
			return retval;
		} catch (CacheException e) {
			throw new OXCachingException(OXCachingException.Code.FAILED_REMOVE, e, new Object[0]);
		} finally {
			LOCK_MODIFY.unlock();
		}
	}

	public final boolean putIMAPConnection(final SessionObject session, final IMAPConnection imapCon)
			throws OXException {
		final CacheKey key = getUserKey(session.getUserObject().getId(), session.getContext());
		if (imapConCache.get(key) != null) {
			/*
			 * Key is already in use and therefore an IMAP connection is already
			 * in cache for current user
			 */
			return false;
		}
		LOCK_MODIFY.lock();
		try {
			/*
			 * Still not present?
			 */
			if (imapConCache.get(key) != null) {
				return false;
			}
			imapConCache.put(key, imapCon);
			return true;
		} catch (CacheException e) {
			throw new OXCachingException(OXCachingException.Code.FAILED_PUT, e, new Object[0]);
		} finally {
			LOCK_MODIFY.unlock();
		}
	}

	public final boolean containsIMAPConnection(final SessionObject session) {
		return (imapConCache.get(getUserKey(session.getUserObject().getId(), session.getContext())) != null);
	}

	private static final CacheKey getUserKey(final int user, final Context ctx) {
		return new CacheKey(ctx, user);
	}

}
