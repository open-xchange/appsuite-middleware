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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.chronos.provider.caching.basic.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.CalendarStorageUtilities;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link AbstractExtensionHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractExtensionHandler {

    //@formatter:off
    /** 
     * Fields that are always included when searching for events 
     */
    static final List<EventField> DEFAULT_FIELDS_LIST = Arrays.asList(
        EventField.ID, EventField.SUMMARY, EventField.DESCRIPTION, EventField.SERIES_ID, EventField.FOLDER_ID, 
        EventField.RECURRENCE_ID, EventField.TIMESTAMP, EventField.CREATED_BY, EventField.CALENDAR_USER, 
        EventField.CLASSIFICATION, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE,
        EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.ORGANIZER
    );
    //@formatter:on

    /**
     * Fields that are always included when searching for events
     */
    private static final EventField[] DEFAULT_FIELDS_ARRAY = DEFAULT_FIELDS_LIST.toArray(new EventField[DEFAULT_FIELDS_LIST.size()]);

    private final CalendarParameters parameters;
    private final Session session;
    private final CalendarAccount account;
    private final CalendarSession calendarSession;
    private final SearchOptions searchOptions;
    private final SelfProtection selfProtection;

    /**
     * Initialises a new {@link AbstractExtensionHandler}.
     * 
     * @param session The groupware {@link Session}
     * @param account The {@link CalendarAccount}
     * @param calendarParameters The {@link CalendarParameters}
     * @throws OXException if the property {@link CalendarSession} cannot be initialised
     */
    public AbstractExtensionHandler(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super();
        this.session = session;
        this.account = account;
        this.parameters = parameters;
        this.calendarSession = Services.getService(CalendarService.class).init(session);
        this.searchOptions = new SearchOptions(getCalendarSession());
        LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
        this.selfProtection = SelfProtectionFactory.createSelfProtection(session, leanConfigurationService);
    }

    /**
     * Gets the parameters
     *
     * @return The parameters
     */
    CalendarParameters getParameters() {
        return parameters;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    Session getSession() {
        return session;
    }

    /**
     * Gets the account
     *
     * @return The account
     */
    CalendarAccount getAccount() {
        return account;
    }

    /**
     * Gets the calendarSession
     *
     * @return The calendarSession
     */
    CalendarSession getCalendarSession() {
        return calendarSession;
    }

    /**
     * Creates and returns the {@link SearchOptions}
     * 
     * @return the {@link SearchOptions}
     */
    SearchOptions getSearchOptions() {
        return searchOptions;
    }

    /**
     * Returns the {@link #DEFAULT_FIELDS_LIST}
     * 
     * @return the {@link #DEFAULT_FIELDS_LIST}
     */
    EventField[] getDefaultEventFields() {
        return getEventFields(null);
    }

    /**
     * <p>Prepares the event fields to request from the storage.</p>
     * 
     * <p>If the requested fields is empty or <code>null</code>, then all {@link #DEFAULT_FIELDS_LIST} are included.
     * The client may also define additional fields.
     * </p>
     * 
     * @param requestedFields The fields requested by the client, or <code>null</code> to retrieve all fields
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     */
    EventField[] getEventFields(EventField[] requestedFields, EventField... additionalFields) {
        if (null == requestedFields || requestedFields.length == 0) {
            return getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class, DEFAULT_FIELDS_ARRAY);
        }

        Set<EventField> eventFields = new HashSet<>();
        eventFields.addAll(DEFAULT_FIELDS_LIST);
        eventFields.addAll(Arrays.asList(requestedFields));
        if (null != additionalFields && additionalFields.length > 0) {
            eventFields.addAll(Arrays.asList(additionalFields));
        }

        return eventFields.toArray(new EventField[eventFields.size()]);
    }

    /**
     * Initialises and returns a {@link CalendarStorage} for the current {@link Session}
     * 
     * @return The initialised storage
     * @throws OXException if the storage cannot be initialised
     */
    CalendarStorage getStorage() throws OXException {
        ContextService contextService = Services.getService(ContextService.class);
        Context context = contextService.loadContext(getSession().getContextId());

        CalendarStorageFactory storageFactory = Services.getService(CalendarStorageFactory.class);
        return storageFactory.create(context, getAccount().getAccountId(), getCalendarSession().getEntityResolver());
    }

    /**
     * Helper method for getting the {@link EventStorage}
     * 
     * @return The {@link EventStorage}
     * @throws OXException if the {@link EventStorage} cannot be returned
     */
    EventStorage getEventStorage() throws OXException {
        return getStorage().getEventStorage();
    }

    /**
     * Gets additional storage utilities.
     * 
     * @return The {@link CalendarStorageUtilities}
     * @throws OXException if the {@link CalendarStorageUtilities} cannot be returned
     */
    CalendarStorageUtilities getUtilities() throws OXException {
        return getStorage().getUtilities();
    }

    /**
     * Post process the specified events
     * 
     * @param events A {@link List} with {@link Event}s to process
     * @return The processed {@link Event}s
     * @throws OXException
     */
    List<Event> postProcess(List<Event> events) throws OXException {
        if (false == parameters.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class)) {
            return events;
        }
        List<Event> processedEvents = new ArrayList<>(events.size());
        for (Event event : events) {
            if (CalendarUtils.isSeriesMaster(event)) {
                processedEvents.addAll(resolveOccurrences(event));
            } else {
                processedEvents.add(event);
            }
        }
        selfProtection.checkEventCollection(processedEvents);
        return processedEvents;
    }

    /**
     * Resolves/expands the occurrences of the master event
     * 
     * @param event The master {@link Event}
     * @return The expanded series
     * @throws OXException if the expanded series contains too many {@link Event}s or
     *             if there is an error during the iteration of the series
     */
    private List<Event> resolveOccurrences(Event event) throws OXException {
        Iterator<Event> itrerator = calendarSession.getRecurrenceService().iterateEventOccurrences(event, searchOptions.getFrom(), searchOptions.getUntil());
        List<Event> list = new ArrayList<Event>();
        while (itrerator.hasNext()) {
            list.add(itrerator.next());
            selfProtection.checkEventCollection(list);
        }
        return list;
    }
}
