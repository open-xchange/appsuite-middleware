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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fortuna.ical4j.extensions.property.WrCalName;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.AlarmData;
import com.openexchange.chronos.ical.CalendarImport;
import com.openexchange.chronos.ical.DefaultAlarmData;
import com.openexchange.chronos.ical.DefaultEventData;
import com.openexchange.chronos.ical.EventData;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link CalendarImportImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarImportImpl implements CalendarImport {

    private final ICalParameters parameters;
    private final List<OXException> warnings;
    private final ICalMapper mapper;

    private List<EventData> events;
    private String method;
    private String name;

    public CalendarImportImpl(Calendar calendar, ICalMapper mapper, ICalParameters parameters, List<OXException> warnings) throws OXException {
        super();
        this.parameters = parameters;
        this.warnings = warnings;
        this.mapper = mapper;
        this.method = ICalUtils.optPropertyValue(calendar.getMethod());
        this.name = ICalUtils.optPropertyValue(calendar.getProperty(WrCalName.PROPERTY_NAME));
        this.events = importEvents(calendar.getComponents(Component.VEVENT));
    }

    @Override
    public void close() throws IOException {
        Streams.close(events);
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<EventData> getEvents() {
        return events;
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    private List<EventData> importEvents(ComponentList eventComponents) throws OXException {
        if (null == eventComponents) {
            return null;
        }
        List<EventData> events = new ArrayList<EventData>(eventComponents.size());
        for (Iterator<?> iterator = eventComponents.iterator(); iterator.hasNext();) {
            VEvent vEvent = (VEvent) iterator.next();
            List<AlarmData> alarms = importAlarms(vEvent.getAlarms());
            Event event = mapper.importVEvent(vEvent, null, parameters, warnings);
            if (Boolean.TRUE.equals(parameters.get(ICalParameters.KEEP_COMPONENTS, Boolean.class))) {
                events.add(new DefaultEventData(event, alarms, ICalUtils.exportComponent(vEvent, parameters)));
            } else {
                events.add(new DefaultEventData(event, alarms, null));
            }
        }
        return events;
    }

    private List<AlarmData> importAlarms(ComponentList alarmComponents) throws OXException {
        if (null == alarmComponents) {
            return null;
        }
        List<AlarmData> alarms = new ArrayList<AlarmData>(alarmComponents.size());
        for (Iterator<?> iterator = alarmComponents.iterator(); iterator.hasNext();) {
            VAlarm vAlarm = (VAlarm) iterator.next();
            Alarm alarm = mapper.importVAlarm(vAlarm, null, parameters, warnings);
            if (Boolean.TRUE.equals(parameters.get(ICalParameters.KEEP_COMPONENTS, Boolean.class))) {
                alarms.add(new DefaultAlarmData(alarm, ICalUtils.exportComponent(vAlarm, parameters)));
            } else {
                alarms.add(new DefaultAlarmData(alarm, null));
            }
        }
        return alarms;
    }

}
