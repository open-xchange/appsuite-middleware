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

package com.openexchange.chronos.provider.schedjoules;

/**
 * {@link SchedJoulesFields}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class SchedJoulesFields {

    ////////////////////// EXTERNAL ATTRIBUTES ////////////////////

    /**
     * The itemId that maps to a SchedJoules itemId
     */
    static final String ITEM_ID = "itemId";

    /**
     * The user configuration's key for all available/visible folders
     */
    static final String FOLDERS = "folders";

    ////////////////////// INTERNAL ATTRIBUTES ////////////////////

    /**
     * The user configuration's key for the feed's URL
     */
    static final String URL = "url";

    /**
     * The refreshInterval for a folder.
     */
    static final String REFRESH_INTERVAL = "refreshInterval";

    /**
     * The optional locale for the item
     */
    static final String LOCALE = "locale";

    /**
     * The schedule transparency property
     */
    static final String SCHEDULE_TRANSP = "scheduleTransp";

    /**
     * The unique user key
     */
    static final String USER_KEY = "userKey";

    /**
     * The etag of a calendar
     */
    static final String ETAG = "etag";

    /**
     * The lastModified of a calendar. The timestamp represents
     * the last time the events were modified and not the attributes
     * of the calendar folder, e.g. colour or name.
     */
    static final String LAST_MODIFIED = "lastModified";
}
