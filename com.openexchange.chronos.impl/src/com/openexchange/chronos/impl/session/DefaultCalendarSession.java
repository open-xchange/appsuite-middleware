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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.impl.session;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.RequestContext;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link DefaultCalendarSession}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultCalendarSession implements CalendarSession {

    private final CalendarService calendarService;
    private final Map<String, Object> parameters;
    private final ServerSession session;
    private final EntityResolver entityResolver;
    private final HostData hostData;

    private static final Logger SESSION_LOGGER = LoggerFactory.getLogger("calendar-session-logger");

    /**
     * Initializes a new {@link DefaultCalendarSession}.
     *
     * @param session The underlying server session
     * @param calendarService A reference to the calendar service
     */
    public DefaultCalendarSession(Session session, CalendarService calendarService) throws OXException {
        super();
        this.calendarService = calendarService;
        this.parameters = new HashMap<String, Object>();
        this.session = ServerSessionAdapter.valueOf(session);
        this.entityResolver = new DefaultEntityResolver(this.session, Services.getServiceLookup());
        RequestContext requestContext = RequestContextHolder.get();
        this.hostData = null != requestContext ? requestContext.getHostData() : null;
        if (isDebugEnabled()) {
            debug("New DefaultCalendarSession created. User: " + session.getUserId() + ", Context: " + session.getContextId());
        }
    }

    @Override
    public CalendarService getCalendarService() {
        return calendarService;
    }

    @Override
    public ServerSession getSession() {
        return session;
    }

    @Override
    public HostData getHostData() {
        return hostData;
    }

    @Override
    public User getUser() {
        return session.getUser();
    }

    @Override
    public Context getContext() {
        return session.getContext();
    }

    @Override
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    @Override
    public <T> CalendarParameters set(String parameter, T value) {
        parameters.put(parameter, value);
        return this;
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return get(parameter, clazz, null);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        Object value = parameters.get(parameter);
        return null == value ? defaultValue : clazz.cast(value);
    }

    @Override
    public boolean contains(String parameter) {
        return parameters.containsKey(parameter);
    }

    @Override
    public String toString() {
        return "CalendarSession [context=" + session.getContextId() + ", user=" + session.getUserId() + ", sessionId=" + session.getSessionID() + "]";
    }

    @Override
    public void debug(String message) {
        if (SESSION_LOGGER.isDebugEnabled()) {
            SESSION_LOGGER.debug("{}@{}: {}", this.getClass().getSimpleName(), System.identityHashCode(this), message);
        }
    }

    @Override
    public void debug(String message, Exception e) {
        if (SESSION_LOGGER.isDebugEnabled()) {
            SESSION_LOGGER.debug("{}@{}: {}", this.getClass().getSimpleName(), System.identityHashCode(this), message, e);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return SESSION_LOGGER.isDebugEnabled();
    }

}
