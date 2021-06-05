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

package com.openexchange.chronos.schedjoules.api.client;

/**
 * {@link SchedJoulesRESTBindPoint}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum SchedJoulesRESTBindPoint {
    
    /**
     * @see <a href="https://github.com/schedjoules/calendar-store-api/blob/master/details/pages.md">https://github.com/schedjoules/calendar-store-api/blob/master/details/pages.md</a>
     */
    pages,
    /**
     * @see <a href="https://github.com/schedjoules/calendar-store-api#calendars">https://github.com/schedjoules/calendar-store-api#calendars</a>
     */
    calendar,
    /**
     * @see <a href="https://github.com/schedjoules/calendar-store-api/blob/master/details/countries.md">https://github.com/schedjoules/calendar-store-api/blob/master/details/countries.md</a>
     */
    countries,
    /**
     * @see <a href="https://github.com/schedjoules/calendar-store-api/blob/master/details/languages.md">https://github.com/schedjoules/calendar-store-api/blob/master/details/languages.md</a>
     */
    languages,
    /**
     * @see <a href="https://github.com/schedjoules/calendar-store-api/blob/master/details/search.md">https://github.com/schedjoules/calendar-store-api/blob/master/details/search.md</a>
     */
    search

    ;

    /**
     * Returns the absolute path of the REST bind point
     * 
     * @return the absolute path of the REST bind point
     */
    public String getAbsolutePath() {
        return "/" + name();
    }
}
