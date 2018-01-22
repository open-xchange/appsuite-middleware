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

import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.SearchUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.exception.OXException;
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

    private static final String PROTOCOL = "cal://";
    private static final String DELIMITER = "/";

    private final int minimumSearchPatternLength;

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
        List<Event> events = getEventStorage().searchEvents(compileSearchTerm(queries), filters, getSearchOptions(), getEventFields(eventFields));
        return postProcess(getUtilities().loadAdditionalEventData(getSession().getUserId(), events, eventFields));
    }

    ///////////////////////////////////////////////// HELPERS ////////////////////////////////////////////////////////

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
        if (null == queries || queries.isEmpty()) {
            return SearchUtils.getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, getFolderId());
        }
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND);
        for (String query : queries) {
            if (SearchUtils.isWildcardOnly(query)) {
                continue;
            }
            String pattern = SearchUtils.surroundWithWildcards(SearchUtils.checkMinimumSearchPatternLength(query, minimumSearchPatternLength));

            CompositeSearchTerm compositeSearchTerm = new CompositeSearchTerm(CompositeOperation.OR);
            compositeSearchTerm.addSearchTerm(SearchUtils.getSearchTerm(EventField.SUMMARY, SingleOperation.EQUALS, pattern));
            compositeSearchTerm.addSearchTerm(SearchUtils.getSearchTerm(EventField.DESCRIPTION, SingleOperation.EQUALS, pattern));
            compositeSearchTerm.addSearchTerm(SearchUtils.getSearchTerm(EventField.CATEGORIES, SingleOperation.EQUALS, pattern));

            searchTerm.addSearchTerm(compositeSearchTerm);
        }
        return searchTerm;
    }

    /**
     * Constructs the folder identifier
     * 
     * @return The folder identifier
     */
    private String getFolderId() {
        StringBuilder builder = new StringBuilder(PROTOCOL);
        builder.append(getAccount().getAccountId()).append(DELIMITER).append(BasicCalendarAccess.FOLDER_ID);
        return builder.toString();
    }
}
