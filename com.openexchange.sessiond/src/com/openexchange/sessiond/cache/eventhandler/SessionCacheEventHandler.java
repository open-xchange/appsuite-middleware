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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.sessiond.cache.eventhandler;

import com.openexchange.caching.CacheElement;
import com.openexchange.caching.ElementEvent;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.caching.objects.CachedSession;
import com.openexchange.sessiond.impl.SessionHandler;

/**
 * {@link SessionCacheEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionCacheEventHandler implements ElementEventHandler {

    private static final long serialVersionUID = -2665608481772455883L;

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(SessionCacheEventHandler.class));

    /**
     * Default constructor
     */
    public SessionCacheEventHandler() {
        super();
    }

    @Override
    public void onExceededIdletimeBackground(final ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        final CachedSession cachedSession = (CachedSession) cacheElem.getVal();
        if (cachedSession.isMarkedAsRemoved()) {
            removedUserSessions(cachedSession);
        } else {
            LOG.error(
                "onExceededIdletimeBackground: key=" + cacheElem.getKey() + " object=" + cacheElem.getVal().toString(),
                new Throwable());
        }
    }

    @Override
    public void onExceededMaxlifeBackground(final ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        final CachedSession cachedSession = (CachedSession) cacheElem.getVal();
        if (cachedSession.isMarkedAsRemoved()) {
            removedUserSessions(cachedSession);
        } else {
            LOG.error(
                "onExceededMaxlifeBackground: key=" + cacheElem.getKey() + " object=" + cacheElem.getVal().toString(),
                new Throwable());
        }
    }

    @Override
    public void onSpooledDiskNotAvailable(final ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        LOG.error("onSpooledDiskNotAvailable: key=" + cacheElem.getKey() + " object=" + cacheElem.getVal().toString(), new Throwable());
    }

    @Override
    public void onSpooledNotAllowed(final ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        LOG.error("onSpooledNotAllowed: key=" + cacheElem.getKey() + " object=" + cacheElem.getVal().toString(), new Throwable());
    }

    @Override
    public void handleElementEvent(final ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        LOG.error("cache-event: key=" + cacheElem.getKey() + " object=" + cacheElem.getVal().toString(), new Throwable());
    }

    @Override
    public void onExceededIdletimeOnRequest(final ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        final CachedSession cachedSession = (CachedSession) cacheElem.getVal();
        if (cachedSession.isMarkedAsRemoved()) {
            removedUserSessions(cachedSession);
        } else {
            LOG.error(
                "onExceededIdletimeOnRequest: key=" + cacheElem.getKey() + " object=" + cacheElem.getVal().toString(),
                new Throwable());
        }
    }

    @Override
    public void onExceededMaxlifeOnRequest(final ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        final CachedSession cachedSession = (CachedSession) cacheElem.getVal();
        if (cachedSession.isMarkedAsRemoved()) {
            removedUserSessions(cachedSession);
        } else {
            LOG.error("onExceededMaxlifeOnRequest: key=" + cacheElem.getKey() + " object=" + cacheElem.getVal().toString(), new Throwable());
        }
    }

    @Override
    public void onSpooledDiskAvailable(final ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        LOG.error("onSpooledDiskAvailable: key=" + cacheElem.getKey() + " object=" + cacheElem.getVal().toString(), new Throwable());
    }

    private void removedUserSessions(final CachedSession cachedSession) {
        SessionHandler.removeUserSessions(cachedSession.getUserId(), cachedSession.getContextId(), false);
    }
}
