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

package com.openexchange.chronos.itip;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.analyzers.AbstractITipAnalyzer;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;

/**
 * 
 * {@link ITipChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ITipChange {

    public static enum Type {
        CREATE, UPDATE, DELETE, CREATE_DELETE_EXCEPTION;
    }

    private Type type;

    private Event currentEvent;

    private Event newEvent;

    private List<EventConflict> conflicts;

    private Event master;

    private Event deleted;

    private boolean isException = false;

    private ITipEventUpdate diff;

    private List<String> diffDescription;

    private String introduction;

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Event getNewEvent() {
        return newEvent;
    }

    public void setNewEvent(Event newEvent) {
        this.newEvent = newEvent;
    }

    public Event getCurrentEvent() throws OXException {
        if (currentEvent == null) {
            if (isException && master != null && newEvent != null && newEvent.getRecurrenceId() != null) {
                RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);
                RecurrenceIterator<Event> recurrenceIterator = recurrenceService.iterateEventOccurrences(master, CalendarUtils.asDate(newEvent.getStartDate()), CalendarUtils.asDate(newEvent.getEndDate()));
                while (recurrenceIterator.hasNext()) {
                    Event next = recurrenceIterator.next();
                    if (next.getRecurrenceId().equals(newEvent.getRecurrenceId())) {
                        return EventMapper.getInstance().copy(next, new Event(), (EventField[]) null);
                    }
                }
            }
        }
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public List<EventConflict> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<EventConflict> conflicts) {
        this.conflicts = conflicts;
    }

    public Event getMasterEvent() {
        return master;
    }

    public void setMaster(Event master) {
        this.master = master;
    }

    public Event getDeletedEvent() {
        return deleted;
    }

    public void setDeleted(Event deleted) {
        this.deleted = deleted;
    }

    public void setException(boolean b) {
        isException = b;
    }

    public boolean isException() {
        return isException;
    }

    public ITipEventUpdate getDiff() throws OXException {
        autodiff();
        return diff;
    }

    private void autodiff() throws OXException {
        if (currentEvent != null && newEvent != null && type == Type.UPDATE) {
            diff = new ITipEventUpdate(currentEvent, newEvent, true, AbstractITipAnalyzer.SKIP);
        }

        if (isException && master != null && newEvent != null && newEvent.getRecurrenceId() != null && newEvent.getRecurrenceId().getValue() != null && type == Type.CREATE) {
            RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);
            TimeZone timeZone = newEvent.getRecurrenceId().getValue().getTimeZone();
            Calendar recurrenceId = GregorianCalendar.getInstance(null == timeZone ? TimeZone.getTimeZone("UTC") : timeZone);
            recurrenceId.setTimeInMillis(newEvent.getRecurrenceId().getValue().getTimestamp());
            int position = recurrenceService.calculateRecurrencePosition(master, recurrenceId);
            if (position > 0) {
                RecurrenceIterator<Event> recurrenceIterator = recurrenceService.iterateEventOccurrences(master, CalendarUtils.asDate(newEvent.getStartDate()), CalendarUtils.asDate(newEvent.getEndDate()));
                while (recurrenceIterator.hasNext() && position >= recurrenceIterator.getPosition()) {
                    Event event = recurrenceIterator.next();
                    if (position == recurrenceIterator.getPosition()) {
                        diff = new ITipEventUpdate(event, newEvent, true, AbstractITipAnalyzer.SKIP);
                        return;
                    }
                }
            }
        }
    }

    public void setDiffDescription(List<String> diffDescription) {
        this.diffDescription = diffDescription;
    }

    public List<String> getDiffDescription() {
        return diffDescription;
    }

    public void setIntroduction(String message) {
        this.introduction = message;
    }

    public String getIntroduction() {
        return introduction;
    }

}
