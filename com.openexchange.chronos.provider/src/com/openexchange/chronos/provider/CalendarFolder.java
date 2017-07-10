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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.provider;

import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Transp;

/**
 * {@link CalendarFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarFolder {

    /**
     * Gets the identifier of the calendar folder.
     *
     * @return The folder identifier
     */
    String getId();

    /**
     * Gets the name of the calendar folder.
     *
     * @return The folder name
     */
    String getName();

    /**
     * Gets the permissions
     *
     * @return
     */
    List<CalendarPermission> getPermissions();

    /**
     * Gets the calendar description.
     *
     * @return The calendar description, or <code>null</code> if not defined
     */
    String getDescription();

    /**
     * Gets the calendar color.
     *
     * @return The calendar color, or <code>null</code> if not defined
     */
    String getColor();

    /**
     * Gets the last modification date of the calendar.
     *
     * @return The last modification date, or <code>null</code> if not defined
     */
    Date getLastModified();

    /**
     * Gets a value indicating whether the calendar object resources in the calendar will affect the owner's free/busy time information or not.
     *
     * @return {@link Transp#TRANSPARENT} if contained events do not contribute to the user's busy time, {@link Transp#OPAQUE}, otherwise
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-9.1">RFC 6638, section 9.1</a>
     */
    Transp getTransparency();

}
