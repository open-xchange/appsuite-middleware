/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.impl.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarConfig;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.RequestContext;
import com.openexchange.framework.request.RequestContextHolder;
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
    private final CalendarParameters parameters;
    private final ServerSession session;
    private final EntityResolver entityResolver;
    private final HostData hostData;
    private final CalendarConfig config;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link DefaultCalendarSession}.
     *
     * @param session The underlying server session
     * @param calendarService A reference to the calendar service
     * @throws OXException In case the user has no calendar access
     */
    public DefaultCalendarSession(Session session, CalendarService calendarService) throws OXException {
        this(session, calendarService, null);
    }

    /**
     * Initializes a new {@link DefaultCalendarSession}.
     *
     * @param session The underlying server session
     * @param calendarService A reference to the calendar service
     * @param parameters calendar parameters to use
     * @throws OXException In case the user has no calendar access
     */
    public DefaultCalendarSession(Session session, CalendarService calendarService, CalendarParameters parameters) throws OXException {
        super();
        this.calendarService = calendarService;
        this.parameters = null != parameters ? parameters : new DefaultCalendarParameters();
        this.session = Check.hasCalendar(ServerSessionAdapter.valueOf(session));
        this.entityResolver = new DefaultEntityResolver(this.session, Services.getServiceLookup());
        RequestContext requestContext = RequestContextHolder.get();
        this.hostData = null != requestContext ? requestContext.getHostData() : null;
        this.warnings = new ArrayList<OXException>();
        this.config = new CalendarConfigImpl(this, Services.getServiceLookup());
    }

    @Override
    public CalendarService getCalendarService() {
        return calendarService;
    }

    @Override
    public FreeBusyService getFreeBusyService() {
        return Services.getService(FreeBusyService.class);
    }

    @Override
    public RecurrenceService getRecurrenceService() {
        return Services.getService(RecurrenceService.class);
    }

    @Override
    public CalendarUtilities getUtilities() {
        return new DefaultCalendarUtilities(Services.getServiceLookup());
    }

    @Override
    public CalendarConfig getConfig() {
        return config;
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
    public int getUserId() {
        return session.getUserId();
    }

    @Override
    public int getContextId() {
        return session.getContextId();
    }

    @Override
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    @Override
    public void addWarning(OXException warning) {
        warnings.add(warning);
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    @Override
    public <T> CalendarParameters set(String parameter, T value) {
        return parameters.set(parameter, value);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return parameters.get(parameter, clazz);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        return parameters.get(parameter, clazz, defaultValue);
    }

    @Override
    public boolean contains(String parameter) {
        return parameters.contains(parameter);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return parameters.entrySet();
    }

    @Override
    public String toString() {
        return "CalendarSession [context=" + session.getContextId() + ", user=" + session.getUserId() + ", sessionId=" + session.getSessionID() + "]";
    }

}
