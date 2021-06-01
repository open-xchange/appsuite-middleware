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

package com.openexchange.chronos.schedjoules.json.actions.parameter;

/**
 * {@link SchedJoulesSearchParameter}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesSearchParameter extends SchedJoulesCommonParameter {

    /**
     * The full text query
     */
    public static final String QUERY = "query";

    /**
     * The maximum amount of rows to return
     */
    public static final String MAX_ROWS = "maxRows";

    /**
     * The country identifier (retrieved by a 'countries' call)
     * to use in order to limit the search.
     */
    public static final String COUNTRY_ID = "countryId";

    /**
     * The category identifier to use in order to limit the search.
     * 
     * @see <a href="https://github.com/schedjoules/calendar-store-api/blob/master/details/search.md">https://github.com/schedjoules/calendar-store-api/blob/master/details/search.md</a>
     */
    public static final String CATEGORY_ID = "categoryId";
}
