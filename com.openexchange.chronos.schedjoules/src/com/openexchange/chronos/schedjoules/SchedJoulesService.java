
package com.openexchange.chronos.schedjoules;
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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SchedJoulesService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface SchedJoulesService {

    /**
     * Retrieves the starting SchedJoules page.
     * 
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult getRoot() throws OXException;

    /**
     * Retrieves the starting SchedJoules page for the specified location
     * in the specified locale
     * 
     * @param locale The locale to use
     * @param location The location to use
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult getRoot(String locale, String location) throws OXException;

    /**
     * Retrieves the SchedJoules page with the specified identifier
     * 
     * @param pageId The page identifier
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult getPage(int pageId) throws OXException;

    /**
     * Retrieves the SchedJoules page with the specified identifier
     * 
     * @param pageId The page identifier
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult getPage(int pageId, String locale) throws OXException;

    /**
     * Retrieves a list of supported country names with the default locale 'en'.
     * 
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult listCountries() throws OXException;

    /**
     * Retrieves a list of supported and localised country names
     * 
     * @param locale The locale
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult listCountries(String locale) throws OXException;

    /**
     * Retrieves a list of supported languages
     * 
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult listLanguages() throws OXException;

    /**
     * Performs a search with the specified parameters. If the country and/or category identifiers are
     * specified, then a search is only performed within those countries/categories respectively.
     * 
     * @param query The query (free text search query parameter)
     * @param locale The locale. Defaults to 'en' if empty
     * @param countryId The country identifier. Ignored if less than or equal to '0'.
     * @param categoryId The category identifier. Ignored if less than or equal to '0'.
     * @param maxRows The maximum amount of results. Defaults to 20.
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult search(String query, String locale, int countryId, int categoryId, int maxRows) throws OXException;

    /**
     * Subscribes to the SchedJoules calendar with the specified identifier
     * 
     * @param session The groupware {@link Session}
     * @param id The calendar identifier
     * @param accountId The identifier of the user's SchedJoules {@link CalendarAccount}
     * @throws OXException if an error is occurred
     */
    String subscribeCalendar(Session session, int id, int accountId) throws OXException;

    /**
     * Subscribes to the SchedJoules calendar with the specified identifier and locale
     * 
     * @param session The groupware {@link Session}
     * @param id The calendar identifier
     * @param accountId The identifier of the user's SchedJoules {@link CalendarAccount}
     * @param locale The locale
     * @throws OXException if an error is occurred
     */
    String subscribeCalendar(Session session, int id, int accountId, String locale) throws OXException;

    /**
     * Un-subscribes from the specified SchedJoules calendar
     * 
     * @param session The groupware {@link Session}
     * @param calendarId The calendar identifier
     * @param accountId The identifier of the user's SchedJoules {@link CalendarAccount}
     * @throws OXException if an error is occurred
     */
    void unsubscribeCalendar(Session session, String calendarId, int accountId) throws OXException;
}
