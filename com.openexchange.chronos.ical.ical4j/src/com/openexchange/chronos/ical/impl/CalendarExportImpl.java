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

package com.openexchange.chronos.ical.impl;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ComponentData;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.extensions.property.WrCalName;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Method;

/**
 * {@link CalendarExportImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarExportImpl implements CalendarExport {

    private final List<OXException> warnings;
    private final ICalMapper mapper;
    private final ICalParameters parameters;
    private final Calendar calendar;
    private final Set<String> timezoneIDs;

    /**
     * Initializes a new {@link CalendarExportImpl}.
     *
     * @param mapper The iCal mapper to use
     * @param parameters The iCal parameters
     * @param warnings The warnings
     */
    public CalendarExportImpl(ICalMapper mapper, ICalParameters parameters, List<OXException> warnings) {
        super();
        this.parameters = parameters;
        this.warnings = warnings;
        this.calendar = new Calendar();
        this.mapper = mapper;
        this.timezoneIDs = new HashSet<String>();
    }

    @Override
    public void setName(String name) {
        WrCalName property = (WrCalName) calendar.getProperty(WrCalName.PROPERTY_NAME);
        if (null == property) {
            property = new WrCalName(PropertyFactoryImpl.getInstance());
            calendar.getProperties().add(property);
        }
        property.setValue(name);
    }

    @Override
    public void setMethod(String method) {
        Method property = (Method) calendar.getProperty(Property.METHOD);
        if (null == property) {
            property = new Method();
            calendar.getProperties().add(property);
        }
        property.setValue(method);
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    @Override
    public CalendarExport add(Event event) throws OXException {
        calendar.getComponents().add(exportEvent(event));
        return this;
    }

    @Override
    public ThresholdFileHolder getVCalendar() throws OXException {
        /*
         * add components for all contained timezones
         */
        for (String timezoneID : timezoneIDs) {
            TimeZoneRegistry timeZoneRegistry = parameters.get(ICalParameters.TIMEZONE_REGISTRY, TimeZoneRegistry.class);
            net.fortuna.ical4j.model.TimeZone timeZone = timeZoneRegistry.getTimeZone(timezoneID);
            if (null != timeZone) {
                calendar.getComponents().add(0, timeZone.getVTimeZone());
            } else {
                warnings.add(ICalExceptionCodes.CONVERSION_FAILED.create("No timezone '" + timezoneID + "' registered."));
            }
        }
        /*
         * export calendar
         */
        return ICalUtils.exportCalendar(calendar, parameters);
    }

    @Override
    public InputStream getClosingStream() throws OXException {
        return getVCalendar().getClosingStream();
    }

    @Override
    public byte[] toByteArray() throws OXException {
        try (ThresholdFileHolder fileHolder = getVCalendar()) {
            return fileHolder.toByteArray();
        }
    }

    private VAlarm exportAlarm(Alarm alarm) throws OXException {
        VAlarm originalVAlarm = null;
        if (ComponentData.class.isInstance(alarm)) {
            IFileHolder originalComponent = ((ComponentData) alarm).getComponent();
            if (null != originalComponent) {
                originalVAlarm = ICalUtils.parseVAlarmComponent(originalComponent, parameters, warnings);
            }
        }
        return mapper.exportAlarm(alarm, originalVAlarm, parameters, warnings);
    }

    private VEvent exportEvent(Event event) throws OXException {
        VEvent originalVEvent = null;
        if (ComponentData.class.isInstance(event)) {
            IFileHolder originalComponent = ((ComponentData) event).getComponent();
            if (null != originalComponent) {
                originalVEvent = ICalUtils.parseVEventComponent(originalComponent, parameters, warnings);
            }
        }
        VEvent vEvent = mapper.exportEvent(event, originalVEvent, parameters, warnings);
        List<Alarm> alarms = event.getAlarms();
        if (null != alarms && 0 < alarms.size()) {
            for (Alarm alarm : alarms) {
                vEvent.getAlarms().add(exportAlarm(alarm));
            }
        }
        trackTimezones(event);
        return vEvent;
    }

    private void trackTimezones(Event event) {
        if (null != event.getStartTimeZone()) {
            timezoneIDs.add(event.getStartTimeZone());
        }
        if (null != event.getEndTimeZone()) {
            timezoneIDs.add(event.getEndTimeZone());
        }
    }

}
