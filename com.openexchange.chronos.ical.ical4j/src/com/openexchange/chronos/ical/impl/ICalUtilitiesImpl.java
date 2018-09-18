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

package com.openexchange.chronos.ical.impl;

import static com.openexchange.chronos.ical.impl.ICalUtils.exportComponents;
import static com.openexchange.chronos.ical.impl.ICalUtils.getParametersOrDefault;
import static com.openexchange.chronos.ical.impl.ICalUtils.importCalendar;
import static com.openexchange.chronos.ical.impl.ICalUtils.parseVAlarmComponents;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalUtilities;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.data.FoldingWriter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * {@link ICalUtilitiesImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICalUtilitiesImpl implements ICalUtilities {

    private final ICalMapper mapper;

    /**
     * Initializes a new {@link ICalUtilitiesImpl}.
     *
     * @param mapper The iCal mapper to use
     */
    public ICalUtilitiesImpl(ICalMapper mapper) {
        super();
        this.mapper = mapper;
    }

    @Override
    public String parsePropertyValue(InputStream inputStream, String propertyName, ICalParameters parameters) throws OXException {
        if (null == propertyName || null == inputStream) {
            return null;
        }
        Calendar calendar = importCalendar(inputStream, parameters);
        Property property = calendar.getProperty(propertyName.toUpperCase(Locale.US));
        return null == property ? null : property.getValue();
    }

    @Override
    public List<Alarm> importAlarms(InputStream inputStream, ICalParameters parameters) throws OXException {
        parameters = getParametersOrDefault(parameters);
        return ICalUtils.importAlarms(parseVAlarmComponents(inputStream, parameters), mapper, parameters);
    }

    @Override
    public void exportAlarms(OutputStream outputStream, List<Alarm> alarms, ICalParameters parameters) throws OXException {
        ComponentList alarmComponents = exportAlarms(alarms, parameters, new ArrayList<OXException>());
        if (null != alarmComponents) {
            exportComponents(outputStream, alarmComponents);
        }
    }

    @Override
    public void exportAlarms(FoldingWriter writer, List<Alarm> alarms, ICalParameters parameters) throws OXException {
        ComponentList alarmComponents = exportAlarms(alarms, parameters, new ArrayList<OXException>());
        if (null != alarmComponents) {
            exportComponents(writer, alarmComponents);
        }
    }

    @Override
    public List<TimeZone> importTimeZones(InputStream inputStream, ICalParameters parameters) throws OXException {
        // parameters = getParametersOrDefault(parameters);
        return null;
    }

    @Override
    public void exportTimeZones(OutputStream outputStream, List<String> timeZoneIDs, ICalParameters parameters) throws OXException {
        ComponentList timeZoneComponents = exportTimeZones(timeZoneIDs, parameters, new ArrayList<OXException>());
        if (null != timeZoneComponents) {
            exportComponents(outputStream, timeZoneComponents);
        }
    }
    
    @Override
    public void exportTimeZones(FoldingWriter writer, List<String> timeZoneIDs, ICalParameters parameters) throws OXException {
        ComponentList timeZoneComponents = exportTimeZones(timeZoneIDs, parameters, new ArrayList<OXException>());
        if (null != timeZoneComponents) {
            exportComponents(writer, timeZoneComponents);
        }
    }

    @Override
    public void exportEvent(OutputStream outputStream, List<Event> events, ICalParameters parameters) throws OXException {
        ComponentList eventComponents = exportEvents(events, parameters, new ArrayList<OXException>());
        if (null != eventComponents) {
            exportComponents(outputStream, eventComponents);
        }
    }

    @Override
    public void exportEvent(FoldingWriter writer, List<Event> events, ICalParameters parameters) throws OXException {
        ComponentList eventComponents = exportEvents(events, parameters, new ArrayList<OXException>());
        if (null != eventComponents) {
            exportComponents(writer, eventComponents);
        }
    }

    private ComponentList exportAlarms(List<Alarm> alarms, ICalParameters parameters, List<OXException> warnings) {
        if (null == alarms || 0 == alarms.size()) {
            return null;
        }
        parameters = getParametersOrDefault(parameters);
        ComponentList components = new ComponentList();
        for (Alarm alarm : alarms) {
            components.add(exportAlarm(alarm, parameters, warnings));
        }
        return components;
    }

    private VAlarm exportAlarm(Alarm alarm, ICalParameters parameters, List<OXException> warnings) {
        VAlarm vAlarm = mapper.exportAlarm(alarm, parameters, warnings);
        ICalUtils.removeProperties(vAlarm, parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class));
        return vAlarm;
    }

    private ComponentList exportTimeZones(List<String> timeZoneIDs, ICalParameters parameters, List<OXException> warnings) {
        if (null == timeZoneIDs || 0 == timeZoneIDs.size()) {
            return null;
        }
        parameters = getParametersOrDefault(parameters);
        ComponentList components = new ComponentList();
        for (String timeZoneID : timeZoneIDs) {
            components.add(exportTimeZone(timeZoneID, parameters, warnings));
        }
        return components;
    }

    private VTimeZone exportTimeZone(String timeZoneID, ICalParameters parameters, List<OXException> warnings) {
        TimeZoneRegistry timeZoneRegistry = parameters.get(ICalParametersImpl.TIMEZONE_REGISTRY, TimeZoneRegistry.class);
        net.fortuna.ical4j.model.TimeZone timeZone = timeZoneRegistry.getTimeZone(timeZoneID);
        if (null != timeZone) {
            return timeZone.getVTimeZone();
        }

        warnings.add(ICalExceptionCodes.CONVERSION_FAILED.create(Component.VTIMEZONE, "No timezone '" + timeZoneID + "' registered."));
        return null;
    }

    private ComponentList exportEvents(List<Event> events, ICalParameters parameters, List<OXException> warnings) {
        if (null == events || 0 == events.size()) {
            return null;
        }
        parameters = getParametersOrDefault(parameters);
        ComponentList components = new ComponentList();
        for (Event event : events) {
            components.add(exportEvent(event, parameters, warnings));
        }
        return components;
    }

    private VEvent exportEvent(Event event, ICalParameters parameters, List<OXException> warnings) {
        VEvent vEvent = mapper.exportEvent(event, parameters, warnings);
        ICalUtils.removeProperties(vEvent, parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class));
        return vEvent;
    }

}
