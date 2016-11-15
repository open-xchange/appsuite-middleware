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

import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.impl.AbstractCollectionUpdate;
import com.openexchange.chronos.impl.AlarmMapper;
import com.openexchange.chronos.impl.AttendeeMapper;
import com.openexchange.chronos.impl.DefaultItemUpdate;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;

/**
 * {@link UpdateResultImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdateResultImpl implements UpdateResult {

    private final int updatedFolderID;
    private final ItemUpdate<Event, EventField> itemUpdate;

    private CollectionUpdate<Alarm, AlarmField> alarmUpdates;
    private CollectionUpdate<Attendee, AttendeeField> attendeeUpdates;

    /**
     * Initializes a new {@link UpdateResultImpl}.
     *
     * @param originalEvent The original event
     * @param updatedFolderID The updated folder identifier, or the original folder identifier if no move took place
     * @param updatedEvent The updated event
     */
    public UpdateResultImpl(Event originalEvent, int updatedFolderID, Event updatedEvent) throws OXException {
        super();
        this.itemUpdate = new DefaultItemUpdate<Event, EventField>(EventMapper.getInstance(), originalEvent, updatedEvent);
        this.updatedFolderID = updatedFolderID;
        setAttendeeUpdates(null != originalEvent ? originalEvent.getAttendees() : null, null != updatedEvent ? updatedEvent.getAttendees() : null);
    }

    public UpdateResultImpl setAlarmUpdates(List<Alarm> originalAlarms, List<Alarm> updatedAlarms) throws OXException {
        return setAlarmUpdates(AlarmMapper.getInstance().getAlarmUpdate(originalAlarms, updatedAlarms));
    }

    public UpdateResultImpl setAlarmUpdates(CollectionUpdate<Alarm, AlarmField> alarmUpdates) {
        this.alarmUpdates = alarmUpdates;
        return this;
    }

    public UpdateResultImpl setAttendeeUpdates(List<Attendee> originalAttendees, List<Attendee> updatedAttendees) throws OXException {
        this.attendeeUpdates = AttendeeMapper.getInstance().getAttendeeUpdate(originalAttendees, updatedAttendees);
        return this;
    }

    @Override
    public int getUpdatedFolderID() {
        return updatedFolderID;
    }

    @Override
    public CollectionUpdate<Attendee, AttendeeField> getAttendeeUpdates() {
        return null != attendeeUpdates ? attendeeUpdates : AbstractCollectionUpdate.<Attendee, AttendeeField> emptyUpdate();
    }

    @Override
    public CollectionUpdate<Alarm, AlarmField> getAlarmUpdates() {
        return null != alarmUpdates ? alarmUpdates : AbstractCollectionUpdate.<Alarm, AlarmField> emptyUpdate();
    }

    @Override
    public Event getOriginal() {
        return itemUpdate.getOriginal();
    }

    @Override
    public Event getUpdate() {
        return itemUpdate.getUpdate();
    }

    @Override
    public Set<EventField> getUpdatedFields() {
        return itemUpdate.getUpdatedFields();
    }

    @Override
    public boolean containsAnyChangeOf(EventField[] fields) {
        return itemUpdate.containsAnyChangeOf(fields);
    }

}
