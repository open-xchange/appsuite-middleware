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

import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;

/**
 * {@link UpdateResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface UpdateResult {

    /**
     * Gets the underlying calendar session.
     *
     * @return The calendar session
     */
    CalendarSession getSession();

    /**
     * Gets the original event.
     *
     * @return The original event
     */
    Event getOriginalEvent();

    /**
     * Gets the updated event.
     *
     * @return The updated event
     */
    Event getUpdatedEvent();

    /**
     * Gets the identifier of the folder the event has been updated in.
     *
     * @return The original folder identifier
     */
    int getOriginalFolderID();

    /**
     * Gets the identifier of the folder the event has been moved into.
     *
     * @return The updated folder identifier, or the original folder identifier if no move took place
     */
    int getUpdatedFolderID();

    /**
     * Gets a set of fields that were modified through the update operation.
     *
     * @return The updated fields
     */
    Set<EventField> getUpdatedFields();

    /**
     * Gets the attendee-related modifications performed through the update operation.
     *
     * @return The attendee updates, or an empty attendee diff if there were no attendee-related changes
     */
    AttendeeDiff getAttendeeUpdates();

    /**
     * Gets a value indicating whether at least one of the specified fields has been modified through the update operation.
     *
     * @param fields The event fields to check
     * @return <code>true</code> if at least one field was updated, <code>false</code>, otherwise
     */
    boolean containsAnyChangeOf(EventField... fields);

}
