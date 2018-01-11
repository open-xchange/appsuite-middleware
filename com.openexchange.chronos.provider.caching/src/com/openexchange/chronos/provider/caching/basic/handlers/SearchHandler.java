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

import static com.openexchange.java.Autoboxing.I;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.session.Session;

/**
 * {@link SearchHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SearchHandler extends AbstractExtensionHandler {

    /**
     * The wildcard character '*'
     */
    private static final String WILDCARD = "*";

    private final int minimumSearchPatternLength;

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
     * @throws OXException if the property <code>com.openexchange.MinimumSearchCharacters</code> is missing or it type
     *             is not an integer
     */
    public SearchHandler(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super(session, account, parameters);
        this.minimumSearchPatternLength = getCalendarSession().getConfig().getMinimumSearchPatternLength();
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
        SearchTerm<?> searchTerm = compileSearchTerm(queries);
        SearchOptions sortOptions = new SearchOptions(getCalendarSession());
        return getEventStorage().searchEvents(searchTerm, filters, sortOptions, getEventFields(eventFields));
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
            return getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class, DEFAULT_FIELDS.toArray(new EventField[DEFAULT_FIELDS.size()]));
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
     * @throws OXException if one of the queries is too short
     */
    private SearchTerm<?> compileSearchTerm(List<String> queries) throws OXException {
        return compileQueriesSearchTerm(queries);
    }

    /**
     * Compiles a {@link SearchTerm} out of the specified {@link List} of queries
     * 
     * @param queries the {@link List} of queries from which to compile the {@link SearchTerm}
     * @return The compiled {@link SearchTerm}
     * @throws OXException if one of the queries is too short
     */
    private SearchTerm<?> compileQueriesSearchTerm(List<String> queries) throws OXException {
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND);
        if (null == queries || queries.isEmpty()) {
            return searchTerm.getOperands()[0];
        }
        for (String query : queries) {
            if (isWildcardOnly(query)) {
                continue;
            }
            String pattern = surroundWithWildcards(checkMinimumSearchPatternLength(query, minimumSearchPatternLength));

            CompositeSearchTerm compositeSearchTerm = new CompositeSearchTerm(CompositeOperation.OR);
            compositeSearchTerm.addSearchTerm(SearchTermFactory.createSearchTerm(EventField.SUMMARY, SingleOperation.EQUALS, pattern));
            compositeSearchTerm.addSearchTerm(SearchTermFactory.createSearchTerm(EventField.DESCRIPTION, SingleOperation.EQUALS, pattern));
            compositeSearchTerm.addSearchTerm(SearchTermFactory.createSearchTerm(EventField.CATEGORIES, SingleOperation.EQUALS, pattern));

            searchTerm.addSearchTerm(compositeSearchTerm);
        }
        return searchTerm;
    }

    /**
     * Checks that the supplied search pattern length is equal to or greater than a configured minimum.
     *
     * @param minimumPatternLength, The minimum search pattern length, or <code>0</code> for no limitation
     * @param pattern The pattern to check
     * @return The passed pattern, after the length was checked
     * @throws OXException {@link CalendarExceptionCodes#QUERY_TOO_SHORT}
     */
    private String checkMinimumSearchPatternLength(String pattern, int minimumPatternLength) throws OXException {
        if (null != pattern && 0 < minimumPatternLength && pattern.length() < minimumPatternLength) {
            throw CalendarExceptionCodes.QUERY_TOO_SHORT.create(I(minimumPatternLength), pattern);
        }
        return pattern;
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
