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

package com.openexchange.groupware.contexts;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.cache.Configuration;
import com.openexchange.cache.dynamic.CacheProxy;
import com.openexchange.cache.dynamic.OXObjectFactory;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.ContextException.Code;
import com.openexchange.groupware.update.Updater;

/**
 * This class implements a caching for the context storage. It provides a proxy
 * implementation for the Context interface to the outside world to be able to
 * keep the referenced context data up-to-date.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CachingContextStorage extends ContextStorage {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(
        CachingContextStorage.class);

    /**
     * Lock for the cache.
     */
    private static final Lock CACHE_LOCK = new ReentrantLock();

    /**
     * Cache.
     */
    private static JCS cache;

    /**
     * Implementation of the context storage that does persistant storing.
     */
    private final ContextStorage persistantImpl;

    /**
     * Default constructor.
     * @param persistantImpl implementation of the ContextStorage that does
     * persistant storing.
     */
    public CachingContextStorage(final ContextStorage persistantImpl) {
        super();
        this.persistantImpl = persistantImpl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        persistantImpl.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getContextId(final String loginInfo) throws ContextException {
        Integer contextId = (Integer) cache.get(loginInfo);
        if (null == contextId) {
            if (LOG.isTraceEnabled()) {
            	LOG.trace("Cache MISS. Login info: " + loginInfo);
            }
            contextId = Integer.valueOf(persistantImpl.getContextId(loginInfo));
            if (NOT_FOUND != contextId) {
                try {
                    cache.put(loginInfo, contextId);
                } catch (CacheException e) {
                    throw new ContextException(Code.CACHE_PUT, e);
                }
            }
        } else if (LOG.isTraceEnabled()) {
            LOG.trace("Cache HIT. Login info: " + loginInfo);
        }
        return contextId.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ContextExtended loadContext(final int contextId)
        throws ContextException {
        final OXObjectFactory<ContextExtended> factory =
        new OXObjectFactory<ContextExtended>() {
            public Object getKey() {
                return Integer.valueOf(contextId);
            }
            public ContextExtended load() throws AbstractOXException {
                final ContextExtended retval = persistantImpl.loadContext(
                    contextId);
                final Updater updater = Updater.getInstance();
                if (updater.isLocked(retval)) {
                    retval.setEnabled(false);
                }
                return retval;
            }
            public Lock getCacheLock() {
                return CACHE_LOCK;
            }
        };
        if (null == cache.get(factory.getKey())) {
            persistantImpl.loadContext(contextId);
        }
    	return CacheProxy.getCacheProxy(factory, cache, ContextExtended.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getAllContextIds() throws ContextException {
        return persistantImpl.getAllContextIds();
    }

    /**
     * Initializes the context cache.
     * @throws ContextException if the initialization fails.
     */
    public static void start() throws ContextException {
        if (null != cache) {
            LOG.error("Duplicate initialization of CachingContextStorage.");
            return;
        }
        try {
            cache = JCS.getInstance("Context");
        } catch (CacheException e) {
            throw new ContextException(Code.CACHE_INIT, e);
        }
    }

    /**
     * Shuts this class down.
     */
    public static void stop() {
        if (null == cache) {
            LOG.error("Duplicate shutdown of CachingContextStorage.");
            return;
        }
        try {
            cache.clear();
        } catch (CacheException e) {
            LOG.error("Problem while clearing cache.", e);
        }
        cache = null;
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public void invalidateContext(final int contextId) throws ContextException {
		if (cache == null) {
			// Cache not initialized, yet.
			return;
		}
		CACHE_LOCK.lock();
		try {
			cache.remove(Integer.valueOf(contextId));
		} catch (CacheException e) {
			throw new ContextException(ContextException.Code.CACHE_REMOVE, e,
                String.valueOf(contextId));
		} finally {
			CACHE_LOCK.unlock();
		}
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateLoginInfo(final String loginContextInfo)
        throws ContextException {
        if (null == cache) {
            return;
        }
        CACHE_LOCK.lock();
        try {
            cache.remove(loginContextInfo);
        } catch (CacheException e) {
            throw new ContextException(ContextException.Code.CACHE_REMOVE, e,
                loginContextInfo);
        } finally {
            CACHE_LOCK.unlock();
        }
    }
}
