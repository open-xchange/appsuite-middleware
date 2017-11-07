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
     * The user configuration's key for the folder's name
     */
    static final String NAME = "name";

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
     * The folder's color
     */
    static final String COLOR = "color";

    /**
     * Flag indicating whether the folder is used for sync
     */
    static final String USED_FOR_SYNC = "usedForSync";

    /**
     * The schedule transparency property
     */
    static final String SCHEDULE_TRANSP = "scheduleTransp";

    /**
     * The folder's description
     */
    static final String DESCRIPTION = "description";

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
     * of the calendar folder, e.g. color or name.
     */
    static final String LAST_MODIFIED = "lastModified";
}
