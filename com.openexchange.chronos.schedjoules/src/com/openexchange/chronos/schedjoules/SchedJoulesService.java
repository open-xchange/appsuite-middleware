
package com.openexchange.chronos.schedjoules;
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

import java.net.URL;
import java.util.Set;
import com.openexchange.chronos.schedjoules.api.SchedJoulesPageField;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCalendar;
import com.openexchange.exception.OXException;

/**
 * {@link SchedJoulesService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface SchedJoulesService {

    /**
     * Retrieves the starting SchedJoules page.
     *
     * @param contextId The context identifier
     *
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult getRoot(int contextId, Set<SchedJoulesPageField> filteredFields) throws OXException;

    /**
     * Retrieves the starting SchedJoules page for the specified location
     * in the specified locale
     *
     * @param contextId The context identifier
     * @param locale The locale to use
     * @param location The location to use
     *
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult getRoot(int contextId, String locale, String location, Set<SchedJoulesPageField> filteredFields) throws OXException;

    /**
     * Retrieves the SchedJoules page with the specified identifier
     *
     * @param contextId The context identifier
     * @param pageId The page identifier
     *
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult getPage(int contextId, int pageId, Set<SchedJoulesPageField> filteredFields) throws OXException;

    /**
     * Retrieves the SchedJoules page with the specified identifier
     *
     * @param contextId The context identifier
     * @param pageId The page identifier
     *
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult getPage(int contextId, int pageId, String locale, Set<SchedJoulesPageField> filteredFields) throws OXException;

    /**
     * Retrieves a list of supported and localised country names
     *
     * @param contextId The context identifier
     * @param locale The locale
     *
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult listCountries(int contextId, String locale) throws OXException;

    /**
     * Retrieves a list of supported languages
     *
     * @param contextId The context identifier
     *
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult listLanguages(int contextId) throws OXException;

    /**
     * Performs a search with the specified parameters. If the country and/or category identifiers are
     * specified, then a search is only performed within those countries/categories respectively.
     *
     * @param contextId The context identifier
     * @param query The query (free text search query parameter)
     * @param locale The locale. Defaults to 'en' if empty
     * @param countryId The country identifier. Ignored if less than or equal to '0'.
     * @param categoryId The category identifier. Ignored if less than or equal to '0'.
     * @param maxRows The maximum amount of results. Defaults to 20.
     *
     * @return The {@link SchedJoulesResult}
     * @throws OXException if an error is occurred
     */
    SchedJoulesResult search(int contextId, String query, String locale, int countryId, int categoryId, int maxRows, Set<SchedJoulesPageField> filteredFields) throws OXException;

    /**
     * Retrieves the calendar data from the specified {@link URL}
     *
     * @param contextId The context identifier
     * @param url The {@link URL} from which to retrieve the ical data
     * @param etag The etag (if available)
     * @param lastModified The last modified timestamp (if available)
     * @return The {@link SchedJoulesCalendar}
     * @throws OXException if the calendar data cannot be retrieved
     */
    SchedJoulesCalendar getCalendar(int contextId, URL url, String etag, long lastModified) throws OXException;

    /**
     * Gets a value indicating whether SchdeJoules-services are properly configured in a specific context.
     *
     * @param contextId The context identifier to check
     * @return <code>true</code> if SchdeJoules services are available, <code>false</code>, otherwise
     */
    boolean isAvailable(int contextId);

}
