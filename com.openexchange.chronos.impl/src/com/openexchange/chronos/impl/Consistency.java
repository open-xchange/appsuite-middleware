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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Consistency {

    /**
     * Sets the event's start- and end-timezones if not yet specified, falling back to the supplied user's default timezone.
     *
     * @param event The event to set the timezones in
     * @param user The user to get the fallback timezone from
     */
    public static void setTimeZone(Event event, User user) {
        String startTimezone = event.getStartTimeZone();
        if (null == startTimezone) {
            event.setStartTimeZone(user.getTimeZone());
        } else {
            //TODO: validate timezone?
        }
        String endTimezone = event.getEndTimeZone();
        if (null == endTimezone) {
            event.setEndTimeZone(event.getStartTimeZone());
        } else {
            //TODO: validate timezone?
        }
    }

    /**
     * Adjusts the start- and end-date of the supplied event in case it is marked as "all-day". This includes the truncation of the
     * time-part in <code>UTC</code>-timezone for the start- and end-date, as well as ensuring that the end-date is at least 1 day after
     * the start-date.
     *
     * @param event The event to adjust
     */
    public static void adjustAllDayDates(Event event) {
        //TODO: non-floating all-day events (with timezone specified?)
        if (event.isAllDay()) {
            if (event.containsStartDate() && null != event.getStartDate()) {
                Date truncatedDate = CalendarUtils.truncateTime(event.getStartDate(), TimeZone.getTimeZone("UTC"));
                if (false == truncatedDate.equals(event.getStartDate())) {
                    event.setStartDate(truncatedDate);
                }
            }
            if (event.containsEndDate() && null != event.getEndDate()) {
                Date truncatedDate = CalendarUtils.truncateTime(event.getEndDate(), TimeZone.getTimeZone("UTC"));
                if (truncatedDate.equals(event.getStartDate())) {
                    Calendar calendar = CalendarUtils.initCalendar(TimeZone.getTimeZone("UTC"), truncatedDate);
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    truncatedDate = calendar.getTime();
                }
                if (false == truncatedDate.equals(event.getEndDate())) {
                    event.setEndDate(truncatedDate);
                }
            }
        }
    }

    public static void setModified(Date lastModified, Event event, int modifiedBy) {
        event.setLastModified(lastModified);
        event.setModifiedBy(modifiedBy);
    }

    public static void setCreated(Date created, Event event, int createdBy) {
        event.setCreated(created);
        event.setCreatedBy(createdBy);
    }

    public static void setModifiedNow(Event event, int modifiedBy) {
        setModified(new Date(), event, modifiedBy);
    }

    public static void setCreatedNow(Event event, int createdBy) {
        setCreated(new Date(), event, createdBy);
    }

    public static Event prepareEventUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        Event eventUpdate = EventMapper.getInstance().getDifferences(originalEvent, updatedEvent);
        for (EventField field : EventMapper.getInstance().getAssignedFields(eventUpdate)) {
            switch (field) {
                case ALL_DAY:
                    /*
                     * adjust start- and enddate, too, if required
                     */
                    EventMapper.getInstance().copyIfNotSet(originalEvent, eventUpdate, EventField.START_DATE, EventField.END_DATE);
                    Consistency.adjustAllDayDates(eventUpdate);
                    break;
                case RECURRENCE_RULE:
                    /*
                     * deny update for change exceptions
                     */
                    if (CalendarUtils.isSeriesException(originalEvent)) {
                        throw OXException.general("not allowed change");
                    }
                    /*
                     * ensure all necessary recurrence related data is present in passed event update
                     */
                    EventMapper.getInstance().copyIfNotSet(originalEvent, eventUpdate, EventField.START_DATE, EventField.END_DATE, EventField.START_TIMEZONE, EventField.END_TIMEZONE, EventField.ALL_DAY);
                    /*
                     * assign recurrence id when transforming a single event to a series
                     */
                    if (0 >= originalEvent.getSeriesId()) {
                        eventUpdate.setSeriesId(originalEvent.getId());
                    }
                    break;
                case START_DATE:
                    /*
                     * verify start date
                     */
                    if (null == eventUpdate.getStartDate()) {
                        throw OXException.general("start date is required");
                    }
                    if (eventUpdate.containsEndDate() && null != eventUpdate.getEndDate()) {
                        if (eventUpdate.getEndDate().before(eventUpdate.getStartDate())) {
                            throw OXException.general("start date after end date");
                        }
                    } else if (null != originalEvent.getEndDate() && originalEvent.getEndDate().before(eventUpdate.getStartDate())) {
                        throw OXException.general("start date after end date");
                    }
                    break;
                case END_DATE:
                    /*
                     * verify end date
                     */
                    if (null == eventUpdate.getEndDate()) {
                        if (eventUpdate.containsStartDate() && null != eventUpdate.getStartDate()) {
                            eventUpdate.setEndDate(eventUpdate.getStartDate());
                        } else {
                            eventUpdate.setEndDate(originalEvent.getStartDate());
                        }
                    } else {
                        if (eventUpdate.containsStartDate() && null != eventUpdate.getStartDate()) {
                            if (eventUpdate.getStartDate().after(eventUpdate.getEndDate())) {
                                throw OXException.general("end date before start date");
                            }
                        } else if (null != originalEvent.getStartDate() && originalEvent.getStartDate().after(eventUpdate.getEndDate())) {
                            throw OXException.general("end date before start date");
                        }
                    }
                    break;
                case UID:
                case CREATED:
                case CREATED_BY:
                case SEQUENCE:
                case SERIES_ID:
                case PUBLIC_FOLDER_ID:
                    throw OXException.general("not allowed change");
                default:
                    break;
            }
        }
        EventMapper.getInstance().copy(originalEvent, eventUpdate, EventField.ID);
        return eventUpdate;
    }

    private Consistency() {
        super();
    }

}
