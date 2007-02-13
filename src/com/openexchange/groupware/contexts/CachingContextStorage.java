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

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.cache.Configuration;
import com.openexchange.cache.dynamic.CacheProxy;
import com.openexchange.cache.dynamic.OXObjectFactory;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.ContextException.Code;

/**
 * This class implements a caching for the context storage. It provides a proxy
 * implementation for the Context interface to the outside world to be able to
 * keep the referenced context data actual.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CachingContextStorage extends ContextStorage {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(
        CachingContextStorage.class);

    /**
     * Cache.
     */
    private static final JCS CACHE;

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
        Integer contextId = (Integer) CACHE.get(loginInfo);
        if (null == contextId) {
            LOG.trace("Cache MISS. Login info: " + loginInfo);
            contextId = persistantImpl.getContextId(loginInfo);
            try {
                CACHE.put(loginInfo, contextId);
            } catch (CacheException e) {
                throw new ContextException(Code.CACHE_PUT, e);
            }
        } else {
            LOG.trace("Cache HIT. Login info: " + loginInfo);
        }
        return contextId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext(final int contextId) throws ContextException {
        return CacheProxy.getCacheProxy(
            new OXObjectFactory<Context>() {
                public Object getKey() {
                    return contextId;
                }
                public Context load() throws AbstractOXException {
                    return persistantImpl.getContext(contextId);
                }
            }, CACHE, Context.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getAllContextIds() throws ContextException {
        return persistantImpl.getAllContextIds();
    }

    static {
        try {
            Configuration.load();
            CACHE = JCS.getInstance("Context");
        } catch (CacheException e) {
            throw new RuntimeException("Can't create context cache.", e);
        } catch (IOException e) {
            throw new RuntimeException("Can't read cache configuration.", e);
        }
    }
}
