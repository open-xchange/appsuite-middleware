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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.AttendeeDiff;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;

/**
 * {@link UpdateResultImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdateResultImpl implements UpdateResult {

    private final CalendarSession session;
    private final int originalFolderID;
    private final Event originalEvent;
    private final Event updatedEvent;
    private final int updatedFolderID;
    private final Set<EventField> updatedFields;
    private final AttendeeDiff attendeeDiff;

    /**
     * Initializes a new {@link UpdateResultImpl}.
     *
     * @param session The calendar session
     * @param originalFolderID The original folder identifier
     * @param originalEvent The original event
     * @param updatedFolderID The updated folder identifier, or the original folder identifier if no move took place
     * @param updatedEvent The updated event
     */
    public UpdateResultImpl(CalendarSession session, int originalFolderID, Event originalEvent, int updatedFolderID, Event updatedEvent) throws OXException {
        super();
        this.session = session;
        this.originalFolderID = originalFolderID;
        this.originalEvent = originalEvent;
        this.updatedFolderID = updatedFolderID;
        this.updatedEvent = updatedEvent;
        if (null != updatedEvent) {
            this.updatedFields = Collections.unmodifiableSet(getUpdatedFields(originalEvent, updatedEvent));
            this.attendeeDiff = new AttendeeDiffImpl(originalEvent.getAttendees(), updatedEvent.getAttendees());
        } else {
            this.updatedFields = Collections.emptySet();
            this.attendeeDiff = new AttendeeDiffImpl(null, null);
        }
    }

    @Override
    public CalendarSession getSession() {
        return session;
    }

    @Override
    public Event getOriginalEvent() {
        return originalEvent;
    }

    @Override
    public int getOriginalFolderID() {
        return originalFolderID;
    }

    @Override
    public Event getUpdatedEvent() {
        return updatedEvent;
    }

    @Override
    public int getUpdatedFolderID() {
        return updatedFolderID;
    }

    @Override
    public Set<EventField> getUpdatedFields() {
        return updatedFields;
    }

    @Override
    public AttendeeDiff getAttendeeUpdates() {
        return attendeeDiff;
    }

    @Override
    public boolean containsAnyChangeOf(EventField... fields) {
        if (null != fields) {
            for (EventField field : fields) {
                if (updatedFields.contains(field)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Set<EventField> getUpdatedFields(Event originalEvent, Event updatedEvent) throws OXException {
        Event differences = EventMapper.getInstance().getDifferences(originalEvent, updatedEvent);
        EventField[] fields = EventMapper.getInstance().getAssignedFields(differences);
        return new HashSet<EventField>(Arrays.asList(fields));
    }

}
