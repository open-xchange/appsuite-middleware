/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
            Event occurrence = CalendarUtils.getOccurrence(recurrenceService, master, newEvent.getRecurrenceId());
            if (null != occurrence) {
                diff = new ITipEventUpdate(occurrence, newEvent, true, AbstractITipAnalyzer.SKIP);
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
