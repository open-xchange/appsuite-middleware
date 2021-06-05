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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.FreeBusyData;
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
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * {@link ICalUtilitiesImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICalUtilitiesImpl implements ICalUtilities {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ICalUtilitiesImpl.class);

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
        ComponentList alarmComponents = exportList((Alarm data, ICalParameters p, List<OXException> w) -> {
            return mapper.exportAlarm(data, p, w);
        }, alarms, parameters);
        if (null != alarmComponents) {
            exportComponents(outputStream, alarmComponents);
        }
    }

    public void exportAlarms(FoldingWriter writer, List<Alarm> alarms, ICalParameters parameters) throws OXException {
        ComponentList alarmComponents = exportList((Alarm data, ICalParameters p, List<OXException> w) -> {
            return mapper.exportAlarm(data, p, w);
        }, alarms, parameters);
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
        ComponentList timeZoneComponents = exportList((String data, ICalParameters p, List<OXException> w) -> {
            return exportTimeZone(data, p, w);
        }, timeZoneIDs, parameters);
        if (null != timeZoneComponents) {
            exportComponents(outputStream, timeZoneComponents);
        }
    }

    public void exportTimeZones(FoldingWriter writer, List<String> timeZoneIDs, ICalParameters parameters) throws OXException {
        ComponentList timeZoneComponents = exportList((String data, ICalParameters p, List<OXException> w) -> {
            return exportTimeZone(data, p, w);
        }, timeZoneIDs, parameters);
        if (null != timeZoneComponents) {
            exportComponents(writer, timeZoneComponents);
        }
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

    @Override
    public void exportEvent(OutputStream outputStream, List<Event> events, ICalParameters parameters) throws OXException {
        ComponentList eventComponents = exportList((Event data, ICalParameters p, List<OXException> w) -> {
            return mapper.exportEvent(data, p, w);
        }, events, parameters);
        if (null != eventComponents) {
            exportComponents(outputStream, eventComponents);
        }
    }

    public void exportEvent(FoldingWriter writer, List<Event> events, ICalParameters parameters) throws OXException {
        ComponentList eventComponents = exportList((Event data, ICalParameters p, List<OXException> w) -> {
            return mapper.exportEvent(data, p, w);
        }, events, parameters);
        if (null != eventComponents) {
            exportComponents(writer, eventComponents);
        }
    }

    @Override
    public void exportFreeBusy(OutputStream outputStream, List<FreeBusyData> freeBusyData, ICalParameters parameters) throws OXException {
        ComponentList eventComponents = exportList((FreeBusyData data, ICalParameters p, List<OXException> w) -> {
            return mapper.exportFreeBusy(data, p, w);
        }, freeBusyData, parameters);
        if (null != eventComponents) {
            exportComponents(outputStream, eventComponents);
        }
    }

    public void exportFreeBusy(FoldingWriter writer, List<FreeBusyData> freeBusyData, ICalParameters parameters) throws OXException {
        ComponentList eventComponents = exportList((FreeBusyData data, ICalParameters p, List<OXException> w) -> {
            return mapper.exportFreeBusy(data, p, w);
        }, freeBusyData, parameters);
        if (null != eventComponents) {
            exportComponents(writer, eventComponents);
        }
    }

    @Override
    public void exportAvailability(OutputStream outputStream, List<Availability> availabilities, ICalParameters parameters) throws OXException {
        ComponentList eventComponents = exportList((Availability data, ICalParameters p, List<OXException> w) -> {
            return mapper.exportAvailability(data, p, w);
        }, availabilities, parameters);
        if (null != eventComponents) {
            exportComponents(outputStream, eventComponents);
        }
    }

    public void exportAvailability(FoldingWriter writer, List<Availability> availabilities, ICalParameters parameters) throws OXException {
        ComponentList eventComponents = exportList((Availability data, ICalParameters p, List<OXException> w) -> {
            return mapper.exportAvailability(data, p, w);
        }, availabilities, parameters);
        if (null != eventComponents) {
            exportComponents(writer, eventComponents);
        }
    }

    private <T> ComponentList exportList(ComponentExporter<T> exporter, List<T> data, ICalParameters parameters) {
        if (null == data || 0 == data.size()) {
            return null;
        }
        parameters = getParametersOrDefault(parameters);
        ComponentList components = new ComponentList();
        ArrayList<OXException> warnings = new ArrayList<OXException>();
        for (T d : data) {
            Component component = exporter.export(d, parameters, warnings);
            if (null != component) {
                ICalUtils.removeProperties(component, parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class));
                ICalUtils.removeParameters(component, parameters.get(ICalParameters.IGNORED_PROPERTY_PARAMETERS, String[].class));
                components.add(component);
            }
        }
        logWarning(warnings);
        return components;
    }
    
    private void logWarning(List<OXException> warnings) {
        for (OXException e : warnings) {
            LOGGER.trace("", e);
        }
    }

    @FunctionalInterface
    private interface ComponentExporter<T> {

        Component export(T data, ICalParameters parameters, List<OXException> warnings);
    }

}
