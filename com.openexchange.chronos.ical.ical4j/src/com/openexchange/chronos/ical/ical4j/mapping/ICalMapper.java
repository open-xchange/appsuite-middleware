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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.VCalendar;
import com.openexchange.chronos.ical.ical4j.mapping.alarm.AlarmMappings;
import com.openexchange.chronos.ical.ical4j.mapping.availability.AvailabilityMappings;
import com.openexchange.chronos.ical.ical4j.mapping.available.AvailableMappings;
import com.openexchange.chronos.ical.ical4j.mapping.calendar.CalendarMappings;
import com.openexchange.chronos.ical.ical4j.mapping.event.EventMappings;
import com.openexchange.chronos.ical.ical4j.mapping.freebusy.FreeBusyMappings;
import com.openexchange.chronos.ical.impl.ICalUtils;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.Available;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VAvailability;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;

/**
 * {@link ICalMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class ICalMapper {

    /**
     * Initializes a new {@link ICalMapper}.
     */
    public ICalMapper() {
        super();
    }

    /**
     * Exports an event to a vEvent.
     *
     * @param event The event to export
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The exported event as vEvent
     */
    public VEvent exportEvent(Event event, ICalParameters parameters, List<OXException> warnings) {
        VEvent vEvent = new VEvent();
        ICalParameters iCalParameters = ICalUtils.getParametersOrDefault(parameters);
        for (ICalMapping<VEvent, Event> mapping : EventMappings.ALL) {
            mapping.export(event, vEvent, iCalParameters, warnings);
        }
        return vEvent;
    }

    /**
     * Exports an alarm to a vAlarm.
     *
     * @param alarm The alarm to export
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The exported alarm as vAlarm
     */
    public VAlarm exportAlarm(Alarm alarm, ICalParameters parameters, List<OXException> warnings) {
        VAlarm vAlarm = new VAlarm();
        ICalParameters iCalParameters = ICalUtils.getParametersOrDefault(parameters);
        for (ICalMapping<VAlarm, Alarm> mapping : AlarmMappings.ALL) {
            mapping.export(alarm, vAlarm, iCalParameters, warnings);
        }
        return vAlarm;
    }

    /**
     * Exports a free/busy data to a vFreeBusy.
     *
     * @param freeBusyData The free/busy data to export
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The exported free/busy data as vFreeBusy
     */
    public VFreeBusy exportFreeBusy(FreeBusyData freeBusyData, ICalParameters parameters, List<OXException> warnings) {
        VFreeBusy vFreeBusy = new VFreeBusy();
        ICalParameters iCalParameters = ICalUtils.getParametersOrDefault(parameters);
        for (ICalMapping<VFreeBusy, FreeBusyData> mapping : FreeBusyMappings.ALL) {
            mapping.export(freeBusyData, vFreeBusy, iCalParameters, warnings);
        }
        return vFreeBusy;
    }

    /**
     * Exports the specified {@link Availability} block to a {@link VAvailability} component.
     *
     * @param availability The {@link Availability} block to export
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The exported {@link Availability} as a {@link VAvailability} component
     */
    public VAvailability exportAvailability(Availability availability, ICalParameters parameters, List<OXException> warnings) {
        VAvailability vAvailability = new VAvailability();
        ICalParameters icalParams = ICalUtils.getParametersOrDefault(parameters);
        for (ICalMapping<VAvailability, Availability> mapping : AvailabilityMappings.ALL) {
            mapping.export(availability, vAvailability, icalParams, warnings);
        }

        // Parse the free slots/available sub-components
        for (com.openexchange.chronos.Available availableBlock : availability.getAvailable()) {
            Available availableComponent = new Available();
            for (ICalMapping<Available, com.openexchange.chronos.Available> mapping : AvailableMappings.ALL) {
                mapping.export(availableBlock, availableComponent, parameters, warnings);
            }
            vAvailability.getAvailable().add(availableComponent);
        }

        return vAvailability;
    }

    /**
     * Imports a vCalendar.
     *
     * @param vCalendar The vCalendar to import
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The imported calendar
     */
    public com.openexchange.chronos.Calendar importVCalendar(VCalendar vCalendar, ICalParameters parameters, List<OXException> warnings) {
        com.openexchange.chronos.Calendar calendar = new com.openexchange.chronos.Calendar();
        ICalParameters iCalParameters = ICalUtils.getParametersOrDefault(parameters);
        for (ICalMapping<VCalendar, com.openexchange.chronos.Calendar> mapping : CalendarMappings.ALL) {
            mapping.importICal(vCalendar, calendar, iCalParameters, warnings);
        }
        return calendar;
    }

    /**
     * Imports a vEvent.
     *
     * @param vEvent The vEvent to import
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The imported event
     */
    public Event importVEvent(VEvent vEvent, ICalParameters parameters, List<OXException> warnings) {
        Event event = new Event();
        ICalParameters iCalParameters = ICalUtils.getParametersOrDefault(parameters);
        for (ICalMapping<VEvent, Event> mapping : EventMappings.ALL) {
            mapping.importICal(vEvent, event, iCalParameters, warnings);
        }
        return event;
    }

    /**
     * Imports a vAlarm.
     *
     * @param vAlarm The vAlarm to import
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The imported alarm
     */
    public Alarm importVAlarm(VAlarm vAlarm, ICalParameters parameters, List<OXException> warnings) {
        Alarm alarm = new Alarm();
        ICalParameters iCalParameters = ICalUtils.getParametersOrDefault(parameters);
        for (ICalMapping<VAlarm, Alarm> mapping : AlarmMappings.ALL) {
            mapping.importICal(vAlarm, alarm, iCalParameters, warnings);
        }
        return alarm;
    }

    /**
     * Imports a vFreeBusy.
     *
     * @param vFreeBusy The vFreeBusy to import
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The imported free/busy data
     */
    public FreeBusyData importVFreeBusy(VFreeBusy vFreeBusy, ICalParameters parameters, List<OXException> warnings) {
        FreeBusyData freeBusy = new FreeBusyData();
        ICalParameters iCalParameters = ICalUtils.getParametersOrDefault(parameters);
        for (ICalMapping<VFreeBusy, FreeBusyData> mapping : FreeBusyMappings.ALL) {
            mapping.importICal(vFreeBusy, freeBusy, iCalParameters, warnings);
        }
        return freeBusy;
    }

    /**
     * Imports a {@link VAvailability} component
     *
     * @param vAvailability The {@link VAvailability} component to import
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The imported {@link Availability} data
     */
    public Availability importVAvailability(VAvailability vAvailability, ICalParameters parameters, List<OXException> warnings) {
        Availability calendarAvailability = new Availability();
        ICalParameters iCalParameters = ICalUtils.getParametersOrDefault(parameters);
        calendarAvailability.setAvailable(importAvailable(vAvailability.getAvailable(), parameters, warnings));
        for (ICalMapping<VAvailability, Availability> mapping : AvailabilityMappings.ALL) {
            mapping.importICal(vAvailability, calendarAvailability, iCalParameters, warnings);
        }
        return calendarAvailability;
    }

    /**
     * Imports the specified {@link Available} blocks
     *
     * @param availableBlocks The {@link Available} blocks to import
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return A {@link List} with the imported {@link Available} data
     */
    public List<com.openexchange.chronos.Available> importAvailable(ComponentList availableBlocks, ICalParameters parameters, List<OXException> warnings) {
        List<com.openexchange.chronos.Available> freeSlots = new ArrayList<>(availableBlocks.size());
        Iterator<?> iterator = availableBlocks.iterator();
        while (iterator.hasNext()) {
            freeSlots.add(importAvailable((Available) iterator.next(), parameters, warnings));
        }
        return freeSlots;
    }

    /**
     * Imports an {@link Available} sub-component
     *
     * @param available The {@link Available} sub-component to import
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The imported {@link Available} data
     */
    public com.openexchange.chronos.Available importAvailable(Available available, ICalParameters parameters, List<OXException> warnings) {
        com.openexchange.chronos.Available freeSlot = new com.openexchange.chronos.Available();
        ICalParameters iCalParameters = ICalUtils.getParametersOrDefault(parameters);
        for (ICalMapping<Available, com.openexchange.chronos.Available> mapping : AvailableMappings.ALL) {
            mapping.importICal(available, freeSlot, iCalParameters, warnings);
        }
        return freeSlot;
    }
}
