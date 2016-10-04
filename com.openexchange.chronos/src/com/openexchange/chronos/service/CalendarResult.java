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

package com.openexchange.chronos.service;

import java.util.Date;
import java.util.List;
import com.openexchange.groupware.ldap.User;

/**
 * {@link CalendarResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarResult {

    /**
     * Gets the underlying calendar session.
     *
     * @return The calendar session
     */
    CalendarSession getSession();

    /**
     * Gets the the actual target calendar user based on the folder view the action has been performed in. This is either the current
     * session's user when operating in <i>private</i> or <i>public</i> folders, or the folder owner for <i>shared</i> calendar folders.
     *
     * @return The actual calendar user
     */
    User getCalendarUser();

    /**
     * Gets the updated server timestamp as used as new/updated last-modification date of the modified data in storage, which is usually
     * also returned to clients.
     *
     * @return The server timestamp
     */
    Date getTimestamp();

    /**
     * Gets the identifier of the folder the action has been performed in, representing the view of the calendar user.
     *
     * @return The folder identifier
     */
    int getFolderID();

    /**
     * Gets the delete results.
     *
     * @return The delete results, or an empty list if there are none
     */
    List<DeleteResult> getDeletions();

    /**
     * Gets the update results.
     *
     * @return The update results, or an empty list if there are none
     */
    List<UpdateResult> getUpdates();

    /**
     * Gets the create results.
     *
     * @return The create results, or an empty list if there are none
     */
    List<CreateResult> getCreations();

}
