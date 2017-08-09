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
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;

/**
 * {@link InternalCalendarResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class InternalCalendarResult {

    private final CalendarSession session;
    private final int calendarUserId;
    private final String folderId;

    private List<CreateResult> creations;
    private List<CreateResult> userizedCreations;
    private List<UpdateResult> updates;
    private List<UpdateResult> userizedUpdates;
    private List<DeleteResult> deletions;
    private List<DeleteResult> userizedDeletions;

    /**
     * Initializes a new {@link InternalCalendarResult}.
     *
     * @param session The calendar session
     * @param calendarUserId The actual calendar user
     * @param folderId The identifier of the targeted calendar folder
     */
    public InternalCalendarResult(CalendarSession session, int calendarUserId, String folderId) {
        super();
        this.session = session;
        this.calendarUserId = calendarUserId;
        this.folderId = folderId;
    }

    /**
     * Adds a plain/vanilla deletion to this calendar result.
     *
     * @param deletion The deletion to add
     * @return A self reference
     */
    public InternalCalendarResult addPlainDeletion(DeleteResult deletion) {
        if (null == deletions) {
            deletions = new ArrayList<DeleteResult>();
        }
        deletions.add(deletion);
        return this;
    }

    /**
     * Adds a plain/vanilla deletion to this calendar result.
     *
     * @param timestamp The timestamp
     * @param eventID The identifier of the deleted event
     * @return A self reference
     */
    public InternalCalendarResult addPlainDeletion(long timestamp, EventID eventID) {
        return addPlainDeletion(new DeleteResultImpl(timestamp, eventID));
    }

    /**
     * Adds a <i>userized</i> deletion to this calendar result.
     *
     * @param deletion The deletion to add
     * @return A self reference
     */
    public InternalCalendarResult addUserizedDeletion(DeleteResult deletion) {
        if (null == userizedDeletions) {
            userizedDeletions = new ArrayList<DeleteResult>();
        }
        userizedDeletions.add(deletion);
        return this;
    }

    /**
     * Adds a <i>userized</i> deletion to this calendar result.
     *
     * @param timestamp The timestamp
     * @param eventID The identifier of the deleted event
     * @return A self reference
     */
    public InternalCalendarResult addUserizedDeletion(long timestamp, EventID eventID) {
        return addUserizedDeletion(new DeleteResultImpl(timestamp, eventID));
    }

    /**
     * Adds a plain/vanilla creation to this calendar result.
     *
     * @param creation The creation to add
     * @return A self reference
     */
    public InternalCalendarResult addPlainCreation(CreateResult creation) {
        if (null == creations) {
            creations = new ArrayList<CreateResult>();
        }
        creations.add(creation);
        return this;
    }

    /**
     * Adds a plain/vanilla creation to this calendar result.
     *
     * @param createdEvent The created event
     * @return A self reference
     */
    public InternalCalendarResult addPlainCreation(Event createdEvent) {
        return addPlainCreation(new CreateResultImpl(createdEvent));
    }

    /**
     * Adds a <i>userized</i> creation to this calendar result.
     *
     * @param creation The creation to add
     * @return A self reference
     */
    public InternalCalendarResult addUserizedCreation(CreateResult creation) {
        if (null == userizedCreations) {
            userizedCreations = new ArrayList<CreateResult>();
        }
        userizedCreations.add(creation);
        return this;
    }

    /**
     * Adds a <i>userized</i> creation to this calendar result.
     *
     * @param createdEvent The created event
     * @return A self reference
     */
    public InternalCalendarResult addUserizedCreation(Event createdEvent) {
        return addUserizedCreation(new CreateResultImpl(createdEvent));
    }

    /**
     * Adds a plain/vanilla update to this calendar result.
     *
     * @param update The update to add
     * @return A self reference
     */
    public InternalCalendarResult addPlainUpdate(UpdateResult update) {
        if (null == updates) {
            updates = new ArrayList<UpdateResult>();
        }
        updates.add(update);
        return this;
    }

    /**
     * Adds a plain/vanilla update to this calendar result.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @return A self reference
     */
    public InternalCalendarResult addPlainUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        return addPlainUpdate(new UpdateResultImpl(originalEvent, updatedEvent));
    }

    /**
     * Adds a <i>userized</i> update to this calendar result.
     *
     * @param update The update to add
     * @return A self reference
     */
    public InternalCalendarResult addUserizedUpdate(UpdateResult update) {
        if (null == userizedUpdates) {
            userizedUpdates = new ArrayList<UpdateResult>();
        }
        userizedUpdates.add(update);
        return this;
    }

    /**
     * Adds a <i>userized</i> update to this calendar result.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @return A self reference
     */
    public InternalCalendarResult addUserizedUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        return addUserizedUpdate(new UpdateResultImpl(originalEvent, updatedEvent));
    }

    /**
     * Gets the plain/vanilla calendar result.
     *
     * @return The calendar result
     */
    public CalendarResult getPlainResult() {
        return new DefaultCalendarResult(session, calendarUserId, folderId, creations, updates, deletions);
    }

    /**
     * Gets the <i>userized</i> calendar result representing the acting client's point of view on the performed changes.
     *
     * @return The calendar result
     */
    public CalendarResult getUserizedResult() {
        return new DefaultCalendarResult(session, calendarUserId, folderId, userizedCreations, userizedUpdates, userizedDeletions);
    }

}
