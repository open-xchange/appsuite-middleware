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

package com.openexchange.chronos.provider.caching.basic.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.session.Session;

/**
 * {@link SearchHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SearchHandler {

    /**
     * The wildcard character '*'
     */
    private static final String WILDCARD = "*";

    private CalendarParameters parameters;
    private Session session;
    private CalendarAccount account;

    //@formatter:off
    /** 
     * Fields that are always included when searching for events 
     */
    private static final List<EventField> DEFAULT_FIELDS = Arrays.asList(
        EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID, 
        EventField.TIMESTAMP, EventField.CREATED_BY, EventField.CALENDAR_USER, EventField.CLASSIFICATION, 
        EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE,
        EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.ORGANIZER
    );
    //@formatter:on

    /**
     * Initialises a new {@link SearchHandler}.
     * 
     * @param session The groupware {@link Session}
     * @param account The {@link CalendarAccount}
     * @param calendarParameters The {@link CalendarParameters}
     */
    public SearchHandler(Session session, CalendarAccount account, CalendarParameters parameters) {
        super();
        this.session = session;
        this.account = account;
        this.parameters = parameters;
    }

    /**
     * Searches for events
     * 
     * @param filters A {@link List} with the {@link SearchFilter}s
     * @param queries A {@link List} with the queries
     * @param eventFields The optional {@link EventField}s. If <code>null</code> all fields will be retrieved
     * @return A {@link List} with all matching {@link Event}s
     * @throws OXException if an error is occurred
     */
    public List<Event> searchEvents(List<SearchFilter> filters, List<String> queries, EventField... eventFields) throws OXException {
        ContextService contextService = Services.getService(ContextService.class);
        Context context = contextService.loadContext(session.getContextId());

        SearchTerm<?> searchTerm = compileSearchTerm(queries);
        CalendarStorageFactory storageFactory = Services.getService(CalendarStorageFactory.class);
        SearchOptions sortOptions = new SearchOptions(parameters);
        CalendarStorage storage = storageFactory.create(context, account.getAccountId(), null);
        return storage.getEventStorage().searchEvents(searchTerm, filters, sortOptions, getEventFields(eventFields));
    }

    ///////////////////////////////////////////////// HELPERS ////////////////////////////////////////////////////////

    /**
     * <p>Prepares the event fields to request from the storage.</p>
     * 
     * <p>If the requested fields is empty or <code>null</code>, then all {@link #DEFAULT_FIELDS} are included.
     * The client may also define additional fields.
     * </p>
     * 
     * @param requestedFields The fields requested by the client, or <code>null</code> to retrieve all fields
     * @param requiredFields Additionally required fields to add, or <code>null</code> if not defined
     * @return The fields to use when querying events from the storage
     */
    private EventField[] getEventFields(EventField[] requestedFields, EventField... additionalFields) {
        if (null == requestedFields || requestedFields.length == 0) {
            return EventField.values();
        }

        Set<EventField> eventFields = new HashSet<>();
        eventFields.addAll(DEFAULT_FIELDS);
        eventFields.addAll(Arrays.asList(requestedFields));
        if (null != additionalFields && additionalFields.length > 0) {
            eventFields.addAll(Arrays.asList(additionalFields));
        }

        return eventFields.toArray(new EventField[eventFields.size()]);
    }

    /**
     * Compiles the {@link SearchTerm} from the specified {@link List} of queries
     * 
     * @param queries The {@link List} of queries
     * @return The {@link SearchTerm}
     */
    private SearchTerm<?> compileSearchTerm(List<String> queries) {
        return compileQueriesSearchTerm(queries);
    }

    /**
     * Compiles a {@link SearchTerm} out of the specified {@link List} of queries
     * 
     * @param queries the {@link List} of queries from which to compile the {@link SearchTerm}
     * @return The compiled {@link SearchTerm}
     */
    private SearchTerm<?> compileQueriesSearchTerm(List<String> queries) {
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND);
        if (null == queries || queries.isEmpty()) {
            return searchTerm.getOperands()[0];
        }
        for (String query : queries) {
            if (isWildcardOnly(query)) {
                continue;
            }
            String pattern = surroundWithWildcards(query); //FIXME: maybe check for the minimum search pattern length?

            CompositeSearchTerm compositeSearchTerm = new CompositeSearchTerm(CompositeOperation.OR);
            compositeSearchTerm.addSearchTerm(getSearchTerm(EventField.SUMMARY, SingleOperation.EQUALS, pattern));
            compositeSearchTerm.addSearchTerm(getSearchTerm(EventField.DESCRIPTION, SingleOperation.EQUALS, pattern));
            compositeSearchTerm.addSearchTerm(getSearchTerm(EventField.CATEGORIES, SingleOperation.EQUALS, pattern));

            searchTerm.addSearchTerm(compositeSearchTerm);
        }
        return searchTerm;
    }

    /**
     * Gets a single search term using the field itself as single column operand.
     *
     * @param <E> The field type
     * @param operation The operation to use
     * @param operand The value to use as constant operand
     * @return A single search term
     */
    private <V, E extends Enum<?>> SingleSearchTerm getSearchTerm(E field, SingleOperation operation, V operand) {
        return new SingleSearchTerm(operation).addOperand(new ColumnFieldOperand<E>(field)).addOperand(new ConstantOperand<V>(operand));
    }

    /**
     * Checks whether the specified query is a wildcard query.
     * 
     * @param query The query to check
     * @return <code>true</code> if the query is empty or consists out of the wildcard character '*'
     */
    private boolean isWildcardOnly(String query) {
        return Strings.isEmpty(query) || WILDCARD.equals(query);
    }

    /**
     * Surrounds the specified pattern with wildcards
     * 
     * @param pattern The pattern to surround with wildcards
     * @return The updated pattern
     */
    private String surroundWithWildcards(String pattern) {
        if (Strings.isEmpty(pattern)) {
            return WILDCARD;
        }
        if (false == pattern.startsWith(WILDCARD)) {
            pattern = WILDCARD + pattern;
        }
        if (false == pattern.endsWith(WILDCARD)) {
            pattern += WILDCARD;
        }
        return pattern;
    }
}
