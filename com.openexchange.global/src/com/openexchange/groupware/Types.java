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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

    public static final int PUBLICATION = 127;

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
