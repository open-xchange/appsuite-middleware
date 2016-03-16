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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.groupware.contexts.impl;

import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.groupware.update.internal.SchemaExceptionCodes;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * This class implements a caching for the context storage. It provides a proxy implementation for the Context interface to the outside
 * world to be able to keep the referenced context data up-to-date.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CachingContextStorage extends ContextStorage {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingContextStorage.class);

    private static final String REGION_NAME = "Context";

    public static volatile CachingContextStorage parent;

    private final ContextStorage persistantImpl;

    private boolean started;

    public CachingContextStorage(final ContextStorage persistantImpl) {
        super();
        this.persistantImpl = persistantImpl;
    }

    @Override
    public int getContextId(final String loginInfo) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return persistantImpl.getContextId(loginInfo);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        Integer contextId = (Integer) cache.get(loginInfo);
        if (null == contextId) {
            LOG.trace("Cache MISS. Login info: {}", loginInfo);
            contextId = I(persistantImpl.getContextId(loginInfo));
            if (NOT_FOUND != contextId.intValue()) {
                try {
                    cache.put(loginInfo, contextId, false);
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
        } else {
            LOG.trace("Cache HIT. Login info: {}", loginInfo);
        }
        return contextId.intValue();
    }

    @Override
    public ContextExtended loadContext(final int contextId) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return load(contextId);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        final Integer key = I(contextId);
        final Object object = cache.get(key);
        if (object instanceof ContextExtended) {
            return (ContextExtended) object;
        }
        // Load it
        final ContextExtended contextExtended = load(contextId);
        cache.put(key, contextExtended, false);
        return contextExtended;
    }

    @Override
    public List<Integer> getAllContextIds() throws OXException {
        return persistantImpl.getAllContextIds();
    }

    @Override
    public List<Integer> getAllContextIdsForFilestore(int filestoreId) throws OXException {
        return persistantImpl.getAllContextIdsForFilestore(filestoreId);
    }

    @Override
    protected void startUp() throws OXException {
        if (started) {
            LOG.error("Duplicate initialization of CachingContextStorage.");
            return;
        }
        persistantImpl.startUp();
        started = true;
    }

    @Override
    public void setAttribute(String name, String value, int contextId) throws OXException {
        persistantImpl.setAttribute(name, value, contextId);
        invalidateContexts(new int[] { contextId });
    }

    @Override
    protected void shutDown() throws OXException {
        if (!started) {
            LOG.error("Duplicate shutdown of CachingContextStorage.");
            return;
        }
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService != null) {
            try {
                cacheService.freeCache(REGION_NAME);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
        persistantImpl.shutDown();
        started = false;
    }

    @Override
    public void invalidateContext(final int contextId) throws OXException {
        invalidateContexts(new int[] { contextId });
    }

    @Override
    public void invalidateContexts(int[] contextIDs) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            // Cache not initialized.
            return;
        }
        /*
         * gather cache keys to invalidate
         */
        Cache cache = cacheService.getCache(REGION_NAME);
        List<Serializable> keys = new LinkedList<Serializable>();
        for (int contextID : contextIDs) {
            Integer key = Integer.valueOf(contextID);
            keys.add(key);
            Object cached = cache.get(key);
            if (null != cached && Context.class.isInstance(cached)) {
                String[] loginInfos = ((Context) cached).getLoginInfo();
                if (null != loginInfos && 0 < loginInfos.length) {
                    for (String loginInfo : loginInfos) {
                        keys.add(loginInfo);
                    }
                }
            }
        }
        /*
         * invalidate cache
         */
        cache.remove(keys);
    }

    @Override
    public void invalidateLoginInfo(final String loginContextInfo) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            // Cache not initialized.
            return;
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        cache.remove(loginContextInfo);
    }

    ContextStorage getPersistantImpl() {
        return persistantImpl;
    }

    private ContextExtended load(final int contextId) throws OXException {
        final ContextExtended retval = CachingContextStorage.parent.getPersistantImpl().loadContext(contextId);
        // TODO We should introduce a logic layer above this context storage
        // layer. That layer should then trigger the update tasks.
        // Nearly all accesses to the ContextStorage need then to be replaced
        // with an access to the ContextService.
        final Updater updater = Updater.getInstance();
        try {
            final UpdateStatus status = updater.getStatus(retval);
            retval.setUpdating(status.blockingUpdatesRunning() || status.needsBlockingUpdates());
            if ((status.needsBlockingUpdates() || status.needsBackgroundUpdates()) && !status.blockingUpdatesRunning() && !status.backgroundUpdatesRunning()) {
                updater.startUpdate(retval);
            }
        } catch (final OXException e) {
            if (SchemaExceptionCodes.DATABASE_DOWN.equals(e)) {
                LOG.warn("Switching to read only mode for context {} because master database is down.", contextId, e);
                retval.setReadOnly(true);
            }
        }
        return retval;
    }
}
