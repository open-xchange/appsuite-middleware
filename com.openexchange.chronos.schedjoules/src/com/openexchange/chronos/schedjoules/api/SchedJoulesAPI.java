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

package com.openexchange.chronos.schedjoules.api;

import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTClient;

/**
 * {@link SchedJoulesAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class SchedJoulesAPI {

    private final SchedJoulesPagesAPI pages;
    private final SchedJoulesCalendarAPI calendar;
    private final SchedJoulesCountriesAPI countries;
    private final SchedJoulesLanguagesAPI languages;
    private final SchedJoulesRESTClient client;

    /**
     * Initialises a new {@link SchedJoulesAPI}.
     * 
     * @param scheme
     * @param host
     * @param apiKey
     */
    public SchedJoulesAPI(String scheme, String host, String apiKey) {
        super();
        client = new SchedJoulesRESTClient(scheme, host, apiKey);
        pages = new SchedJoulesPagesAPI(client);
        calendar = new SchedJoulesCalendarAPI(client);
        countries = new SchedJoulesCountriesAPI(client);
        languages = new SchedJoulesLanguagesAPI(client);
    }

    /**
     * Gets the pages API
     *
     * @return The pages
     */
    public SchedJoulesPagesAPI pages() {
        return pages;
    }

    /**
     * Gets the calendar API
     *
     * @return The calendar API
     */
    public SchedJoulesCalendarAPI calendar() {
        return calendar;
    }

    /**
     * Gets the countries
     *
     * @return The countries
     */
    public SchedJoulesCountriesAPI countries() {
        return countries;
    }

    /**
     * Gets the languages
     *
     * @return The languages
     */
    public SchedJoulesLanguagesAPI languages() {
        return languages;
    }
}
