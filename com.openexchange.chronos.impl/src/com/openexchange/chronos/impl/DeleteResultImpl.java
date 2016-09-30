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

package com.openexchange.chronos.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;

/**
 * {@link DeleteResultImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DeleteResultImpl extends UpdateResultImpl implements DeleteResult {

    private final Date timestamp;
    private final List<DeleteResult> nestedResults;

    /**
     * Initializes a new {@link DeleteResultImpl} for a delete operation that resulted in an event update.
     *
     * @param session The calendar session
     * @param calendarUser The actual calendar user
     * @param originalFolderID The original folder identifier
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     */
    public DeleteResultImpl(CalendarSession session, User calendarUser, int originalFolderID, Event originalEvent, Event updatedEvent) throws OXException {
        this(session, calendarUser, originalFolderID, originalEvent, updatedEvent, new ArrayList<DeleteResult>());
    }

    /**
     * Initializes a new {@link DeleteResultImpl} for a delete operation that resulted in an event update.
     *
     * @param session The calendar session
     * @param calendarUser The actual calendar user
     * @param originalFolderID The original folder identifier
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @param nestedResults A list of nested delete results
     */
    public DeleteResultImpl(CalendarSession session, User calendarUser, int originalFolderID, Event originalEvent, Event updatedEvent, List<DeleteResult> nestedResults) throws OXException {
        super(session, calendarUser, originalFolderID, originalEvent, originalFolderID, updatedEvent);
        this.timestamp = null;
        this.nestedResults = null != nestedResults ? new ArrayList<DeleteResult>(nestedResults) : new ArrayList<DeleteResult>();
    }

    /**
     * Initializes a new {@link DeleteResultImpl}.
     *
     * @param session The calendar session
     * @param calendarUser The actual calendar user
     * @param originalFolderID The original folder identifier
     * @param originalEvent The original event
     * @param timestamp The updated timestamp of the deleted event
     */
    public DeleteResultImpl(CalendarSession session, User calendarUser, int originalFolderID, Event originalEvent, Date timestamp) throws OXException {
        this(session, calendarUser, originalFolderID, originalEvent, timestamp, new ArrayList<DeleteResult>());
    }

    /**
     * Initializes a new {@link DeleteResultImpl}.
     *
     * @param session The calendar session
     * @param calendarUser The actual calendar user
     * @param originalFolderID The original folder identifier
     * @param originalEvent The original event
     * @param timestamp The updated timestamp of the deleted event
     * @param nestedResults A list of nested delete results, or <code>null</code> if there are none
     */
    public DeleteResultImpl(CalendarSession session, User calendarUser, int originalFolderID, Event originalEvent, Date timestamp, List<DeleteResult> nestedResults) throws OXException {
        super(session, calendarUser, originalFolderID, originalEvent, originalFolderID, null);
        this.timestamp = timestamp;
        this.nestedResults = null != nestedResults ? new ArrayList<DeleteResult>(nestedResults) : new ArrayList<DeleteResult>();
    }

    public DeleteResultImpl addNestedResult(DeleteResult result) {
        nestedResults.add(result);
        return this;
    }

    @Override
    public Date getTimestamp() {
        return null != timestamp ? timestamp : super.getTimestamp();
    }

    @Override
    public Event getDeletedEvent() {
        return getOriginal();
    }

    @Override
    public boolean wasUpdate() {
        return null != getUpdate();
    }

    @Override
    public UpdateResult asUpdate() {
        return wasUpdate() ? this : null;
    }

    @Override
    public List<DeleteResult> getNestedResults() {
        return nestedResults;
    }

}
