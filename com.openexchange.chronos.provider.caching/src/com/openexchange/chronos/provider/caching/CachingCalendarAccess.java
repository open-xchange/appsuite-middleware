/*
 *
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

package com.openexchange.chronos.provider.caching;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.CachingExecutor;
import com.openexchange.chronos.provider.caching.internal.handler.CachingHandler;
import com.openexchange.chronos.provider.caching.internal.handler.CachingHandlerFactory;
import com.openexchange.chronos.provider.caching.internal.handler.ProcessingType;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarAccountStorageFactory;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * A caching {@link CalendarAccess} implementation that provides generic caching (based on the database) for external events.<br>
 * <br>
 * Some abstract methods have to be implemented from the underlying implementation to retrieve data and some configurations (for instance the refresh interval to define after which period the external provider should be contacted)
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class CachingCalendarAccess implements CalendarAccess {

    public static final String LAST_UPDATE = "lastUpdate";

    public static final String PREVIOUS_LAST_UPDATE = "previousLastUpdate";

    private final ServerSession session;
    private final CalendarAccount account;
    private final CalendarParameters parameters;

    public CachingCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        this.parameters = parameters;
        this.session = ServerSessionAdapter.valueOf(session);
        this.account = account;
    }

    /**
     * Defines the refresh interval in minutes
     * 
     * @return The interval to update imported {@link Event}s in {@link TimeUnit#MINUTES}
     */
    public abstract int getRefreshInterval();

    /**
     * Returns the requested list of {@link Event}s by querying the underlying calendar.
     * 
     * @param folderId The identifier of the folder to get the events from
     * @return The events
     */
    public abstract List<Event> getEvents(String folderId) throws OXException;

    @Override
    public final Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        ProcessingType updateType = getType();

        CachingHandler cachingHandler = CachingHandlerFactory.getInstance().get(updateType, this);
        try {
            List<Event> doIt = new CachingExecutor(this, cachingHandler).doIt(folderId, eventId, recurrenceId);
            if (doIt != null && !doIt.isEmpty()) {
                return doIt.get(0);
            }
            return new Event(); //FIXME how to handle empty search results?
        } catch (OXException e) {
            cachingHandler.handleExceptions(e);
            throw e;
        }
    }

    @Override
    public final List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        ProcessingType updateType = getType();

        CachingHandler cachingHandler = CachingHandlerFactory.getInstance().get(updateType, this);
        try {
            return new CachingExecutor(this, cachingHandler).doIt(eventIDs);
        } catch (OXException e) {
            cachingHandler.handleExceptions(e);
            throw e;
        }
    }

    @Override
    public final List<Event> getEventsInFolder(String folderId) throws OXException {
        ProcessingType updateType = getType();

        CachingHandler cachingHandler = CachingHandlerFactory.getInstance().get(updateType, this);
        try {
            return new CachingExecutor(this, cachingHandler).doIt(folderId);
        } catch (OXException e) {
            cachingHandler.handleExceptions(e);
            throw e;
        }
    }

    public ServerSession getSession() {
        return session;
    }

    public CalendarAccount getAccount() {
        return account;
    }

    public CalendarParameters getParameters() {
        return parameters;
    }

    /**
     * Returns the type of processing that should be executed for the current request
     * 
     * @return {@link ProcessingType} that indicates the following steps of processing
     * @throws OXException
     */
    protected final ProcessingType getType() throws OXException {
        Number lastUpdate = (Number) getConfiguration(LAST_UPDATE);
        if (lastUpdate == null || lastUpdate.longValue() <= 0) {
            return ProcessingType.INITIAL_INSERT;
        }

        long currentTimeMillis = System.currentTimeMillis();
        int refreshInterval = getRefreshInterval();
        if (refreshInterval * 1000 * 60 < currentTimeMillis - lastUpdate.longValue()) {
            return ProcessingType.UPDATE;
        }
        return ProcessingType.READ_DB;
    }

    /**
     * Returns the persisted account configuration for the given key.
     * 
     * @param key The configuration key
     * @return {@link Object} With the set configuration
     * @throws OXException
     */
    protected Object getConfiguration(String key) throws OXException {
        return getAccount().getConfiguration().get(key);
    }

    /**
     * Saves the current configuration for the account.<br>
     * <br>
     * This might be overridden to enhance the configuration by parameters of the underlying implementation.
     * 
     * @param configuration - The configuration to persist
     * @throws OXException
     */
    public void saveConfig(Map<String, Object> configuration) throws OXException {
        CalendarAccountStorage accountStorage = Services.getService(CalendarAccountStorageFactory.class).create(this.getSession().getContext());
        accountStorage.updateAccount(this.account.getAccountId(), configuration, this.getAccount().getLastModified().getTime());
    }
}
