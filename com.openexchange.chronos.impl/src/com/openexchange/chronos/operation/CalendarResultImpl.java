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

package com.openexchange.chronos.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.groupware.ldap.User;

/**
 * {@link CalendarResultImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarResultImpl implements CalendarResult {

    protected final CalendarSession session;
    protected final User calendarUser;
    protected final int folderID;

    protected Date timestamp;
    protected List<CreateResult> creations;
    protected List<UpdateResult> updates;
    protected List<DeleteResult> deletions;
    protected List<EventConflict> conflicts;

    /**
     * Initializes a new {@link CalendarResultImpl}.
     *
     * @param session The calendar session
     * @param calendarUser The actual calendar user
     * @param folderID The identifier of the folder the event has been created in.
     */
    public CalendarResultImpl(CalendarSession session, User calendarUser, int folderID) {
        super();
        this.session = session;
        this.calendarUser = calendarUser;
        this.folderID = folderID;
    }

    /**
     * Applies an updated server timestamp as used as new/updated last-modification date of the modified data in storage, which is usually
     * also returned to clients.
     * <p/>
     * The timestamp is taken over into the result in case no previous timestamp was set, or the passed timestamp is <i>after</i> the
     * previously set one.
     *
     * @param timestamp The timestamp to apply
     * @return A self reference
     */
    public CalendarResultImpl applyTimestamp(Date timestamp) {
        if (null == this.timestamp || timestamp.after(this.timestamp)) {
            this.timestamp = timestamp;
        }
        return this;
    }

    /**
     * Adds a deletion to this calendar result.
     *
     * @param deletion The deletion to add
     * @return A self reference
     */
    public CalendarResultImpl addDeletion(DeleteResult deletion) {
        if (null == deletions) {
            deletions = new ArrayList<DeleteResult>();
        }
        deletions.add(deletion);
        return this;
    }

    /**
     * Adds a creation to this calendar result.
     *
     * @param creation The creation to add
     * @return A self reference
     */
    public CalendarResultImpl addCreation(CreateResult creation) {
        if (null == creations) {
            creations = new ArrayList<CreateResult>();
        }
        creations.add(creation);
        return this;
    }

    /**
     * Adds an update to this calendar result.
     *
     * @param update The update to add
     * @return A self reference
     */
    public CalendarResultImpl addUpdate(UpdateResult update) {
        if (null == updates) {
            updates = new ArrayList<UpdateResult>();
        }
        updates.add(update);
        return this;
    }

    /**
     * Adds a conflict to this calendar result.
     *
     * @param conflict The conflict to add
     * @return A self reference
     */
    public CalendarResultImpl addConflict(EventConflict conflict) {
        if (null == conflicts) {
            conflicts = new ArrayList<EventConflict>();
        }
        conflicts.add(conflict);
        return this;
    }

    @Override
    public CalendarSession getSession() {
        return session;
    }

    @Override
    public User getCalendarUser() {
        return calendarUser;
    }

    @Override
    public int getFolderID() {
        return folderID;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public List<DeleteResult> getDeletions() {
        return null == deletions ? Collections.<DeleteResult> emptyList() : Collections.unmodifiableList(deletions);
    }

    @Override
    public List<UpdateResult> getUpdates() {
        return null == updates ? Collections.<UpdateResult> emptyList() : Collections.unmodifiableList(updates);
    }

    @Override
    public List<CreateResult> getCreations() {
        return null == creations ? Collections.<CreateResult> emptyList() : Collections.unmodifiableList(creations);
    }

    @Override
    public List<EventConflict> getConflicts() {
        return null == conflicts ? Collections.<EventConflict> emptyList() : Collections.unmodifiableList(conflicts);
    }

}
