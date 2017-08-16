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

package com.openexchange.chronos.alarm;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.EventField;;

/**
 * {@link AlarmChange}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmChange {

    private EventSeriesWrapper oldEvent;
    private EventSeriesWrapper newEvent;
    private Set<EventField> changedFields;
    private Map<Integer, List<Alarm>> alarmsPerAttendee;
    private Type type;

    /**
     *
     * {@link Type}s of alarm changes
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.0
     */
    public enum Type {
        CREATE,
        DELETE,
        UPDATE
    }

    /**
     * Creates an {@link AlarmChange} for a newly created event
     *
     * @param event The created event
     * @param alarmsPerAttendee A map of alarms per attendee
     * @return The {@link AlarmChange}
     */
    public static AlarmChange newCreate(EventSeriesWrapper event, Map<Integer, List<Alarm>> alarmsPerAttendee) {
        return new AlarmChange(Type.CREATE, null, event, null, alarmsPerAttendee);
    }

    /**
     * Creates an {@link AlarmChange} for a updated event
     *
     * @param oldEventSeriesWrapper The original event
     * @param newEventSeriesWrapper The updated event
     * @param changedFields A set of changed fields
     * @param alarmsPerAttendee A map of alarms per attendee
     * @return The {@link AlarmChange}
     */
    public static AlarmChange newUpdate(EventSeriesWrapper oldEventSeriesWrapper, EventSeriesWrapper newEventSeriesWrapper, Set<EventField> changedFields, Map<Integer, List<Alarm>> alarmsPerAttendee) {
        return new AlarmChange(Type.UPDATE, oldEventSeriesWrapper, newEventSeriesWrapper, changedFields, alarmsPerAttendee);
    }

    /**
     * Creates an {@link AlarmChange} for a deleted event.
     *
     * @param event The deleted event
     * @return The {@link AlarmChange}
     */
    public static AlarmChange newDelete(EventSeriesWrapper event){
        return new AlarmChange(Type.DELETE, event, null, null, null);
    }

    private AlarmChange(Type type, EventSeriesWrapper oldEventSeriesWrapper, EventSeriesWrapper newEventSeriesWrapper, Set<EventField> changedFields, Map<Integer, List<Alarm>> alarmsPerAttendee) {
        super();
        this.oldEvent = oldEventSeriesWrapper;
        this.newEvent = newEventSeriesWrapper;
        this.alarmsPerAttendee = alarmsPerAttendee;
        this.changedFields = changedFields;
        this.type=type;
    }

    /**
     * Gets the oldEvent
     *
     * @return The oldEvent
     */
    public EventSeriesWrapper getOldEvent() {
        return oldEvent;
    }


    /**
     * Sets the oldEvent
     *
     * @param oldEvent The oldEvent to set
     */
    public void setOldEvent(EventSeriesWrapper oldEvent) {
        this.oldEvent = oldEvent;
    }


    /**
     * Gets the newEvent
     *
     * @return The newEvent
     */
    public EventSeriesWrapper getNewEvent() {
        return newEvent;
    }


    /**
     * Sets the newEvent
     *
     * @param newEvent The newEvent to set
     */
    public void setNewEvent(EventSeriesWrapper newEvent) {
        this.newEvent = newEvent;
    }


    /**
     * Gets the changedFields
     *
     * @return The changedFields
     */
    public Set<EventField> getChangedFields() {
        return changedFields;
    }


    /**
     * Sets the changedFields
     *
     * @param changedFields The changedFields to set
     */
    public void setChangedFields(Set<EventField> changedFields) {
        this.changedFields = changedFields;
    }


    /**
     * Gets the alarmsPerAttendee
     *
     * @return The alarmsPerAttendee
     */
    public Map<Integer, List<Alarm>> getAlarmsPerAttendee() {
        return alarmsPerAttendee;
    }


    /**
     * Sets the alarmsPerAttendee
     *
     * @param alarmsPerAttendee The alarmsPerAttendee to set
     */
    public void setAlarmsPerAttendee(Map<Integer, List<Alarm>> alarmsPerAttendee) {
        this.alarmsPerAttendee = alarmsPerAttendee;
    }


    /**
     * Gets the type
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }


    /**
     * Sets the type
     *
     * @param type The type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

}
