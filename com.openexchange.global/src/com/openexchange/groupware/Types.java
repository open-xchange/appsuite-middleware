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

package com.openexchange.groupware;

/**
 * Provides constants for Open-Xchange modules:
 * <p>
 * <table border="0" cellpadding="1" cellspacing="0">
 * <tr align="left">
 * <th bgcolor="#CCCCFF" align="left" id="construct">Name</th>
 * <th bgcolor="#CCCCFF" align="left" id="matches">Constant</th>
 * </tr>
 * <tr>
 * <td valign="top"><tt>APPOINTMENT</tt></td>
 * <td headers="matches">1</td>
 * </tr>
 * <tr>
 * <td valign="top"><tt>TASK</tt></td>
 * <td headers="matches">4</td>
 * </tr>
 * <tr>
 * <td valign="top"><tt>CONTACT</tt></td>
 * <td headers="matches">7</td>
 * </tr>
 * <tr>
 * <td valign="top"><tt>EMAIL</tt></td>
 * <td headers="matches">19</td>
 * </tr>
 * <tr>
 * <td valign="top"><tt>FOLDER</tt></td>
 * <td headers="matches">20</td>
 * </tr>
 * <tr>
 * <td valign="top"><tt>USER_SETTING</tt></td>
 * <td headers="matches">31</td>
 * </tr>
 * <tr>
 * <td valign="top"><tt>REMINDER</tt></td>
 * <td headers="matches">55</td>
 * </tr>
 * <tr>
 * <td valign="top" colspan="2">...</td>
 * </tr>
 * </table>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Types {

    public static final int APPOINTMENT = 1;

    public static final int TASK = 4;

    public static final int CONTACT = 7;

    public static final int EMAIL = 19;

    public static final int FOLDER = 20;

    public static final int USER_SETTING = 31;

    public static final int REMINDER = 55;

    public static final int ICAL = 75;

    public static final int VCARD = 95;

    public static final int PARTICIPANT = 105;

    public static final int GROUPUSER = 115;

    public static final int USER = 120;

    public static final int GROUP = 125;

    public static final int SUBSCRIPTION = 126;

    /* Removed with v7.10.2, see MW-1089 */
    // public static final int PUBLICATION = 127;

    /**
     * Identifier for principals. This can be groups and users. This type is used to generate not intersecting identifiers for groups and
     * users.
     */
    public static final int PRINCIPAL = 130;

    /**
     * TODO: EXTRACT to admin Used for gid numbers on group create
     */
    public static final int GID_NUMBER = 1130;

    /**
     * TODO: EXTRACT to admin Used for uid numbers on user create
     */
    public static final int UID_NUMBER = 1131;

    public static final int RESOURCE = 135;

    public static final int INFOSTORE = 137;

    public static final int ATTACHMENT = 138;

    public static final int WEBDAV = 139;

    /**
     * TODO: EXTRACT to admin Used for generating ids for mail service
     */
    public static final int MAIL_SERVICE = 1132;

    public static final int GENERIC_CONFIGURATION = 1200;

    public static final int EAV_NODE = 666;
    
}
