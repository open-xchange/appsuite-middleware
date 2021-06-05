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

package com.openexchange.chronos.provider.caching.basic.handlers;

import static com.openexchange.java.Autoboxing.b;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AbstractExtensionHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractExtensionHandler {

    private final CalendarParameters parameters;
    private final Session session;
    private final CalendarAccount account;
    private final SearchOptions searchOptions;
    private final SelfProtection selfProtection;

    /**
     * Initialises a new {@link AbstractExtensionHandler}.
     *
     * @param session The groupware {@link Session}
     * @param account The {@link CalendarAccount}
     * @param parameters The {@link CalendarParameters}
     * @throws OXException
     */
    public AbstractExtensionHandler(Session session, CalendarAccount account, CalendarParameters parameters) {
        super();
        this.session = session;
        this.account = account;
        this.parameters = parameters;
        this.searchOptions = new SearchOptions(parameters);
        LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
        this.selfProtection = SelfProtectionFactory.createSelfProtection(leanConfigurationService);
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
     * Creates and returns the {@link SearchOptions}
     *
     * @return the {@link SearchOptions}
     */
    SearchOptions getSearchOptions() {
        return searchOptions;
    }

    /**
     * <p>Prepares the event fields to request from the storage.</p>
     *
     * <p>If the requested fields is empty or <code>null</code>, then all {@link CalendarUtils#DEFAULT_FIELDS} are included.
     * The client may also define additional fields.
     * </p>
     *
     * @param requestedFields The fields requested by the client, or <code>null</code> to retrieve all fields
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     */
    EventField[] getEventFields() {
        return CalendarUtils.getFields(parameters);
    }

    /**
     * Post process the specified events
     *
     * @param events A {@link List} with {@link Event}s to process
     * @return The processed {@link Event}s
     * @throws OXException
     */
    List<Event> postProcess(List<Event> events) throws OXException {
        boolean expandOccurrences = b(parameters.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class, Boolean.FALSE));
        List<Event> processedEvents = new ArrayList<>(events.size());
        for (Event event : events) {
            event.setFlags(CalendarUtils.getFlags(event, getSession().getUserId()));
            if (CalendarUtils.isSeriesMaster(event) && expandOccurrences) {
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
        RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);
        Iterator<Event> itrerator = recurrenceService.iterateEventOccurrences(event, searchOptions.getFrom(), searchOptions.getUntil());
        List<Event> list = new ArrayList<Event>();
        while (itrerator.hasNext()) {
            list.add(itrerator.next());
            selfProtection.checkEventCollection(list);
        }
        return list;
    }
}
