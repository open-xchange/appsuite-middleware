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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.VCalendar;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.chronos.ical.ical4j.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.version.VersionService;
import net.fortuna.ical4j.extensions.property.WrCalName;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VAvailability;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

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
    private final VCalendar vCalendar;
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
        this.mapper = mapper;
        this.parameters = parameters;
        this.warnings = warnings;
        this.timezoneIDs = new HashSet<String>();
        this.vCalendar = initCalendar();
    }

    @Override
    public CalendarExport add(ExtendedProperty property) {
        vCalendar.getProperties().add(ICalUtils.exportProperty(property));
        return this;
    }

    public void setProductId(String prodId) {
        ProdId property = (ProdId) vCalendar.getProperty(Property.PRODID);
        if (null == property) {
            property = new ProdId();
            vCalendar.getProperties().add(property);
        }
        property.setValue(prodId);
    }

    public void setVersion(String version) {
        Version property = (Version) vCalendar.getProperty(Property.VERSION);
        if (null == property) {
            property = new Version();
            vCalendar.getProperties().add(property);
        }
        property.setValue(version);
    }

    @Override
    public void setName(String name) {
        WrCalName property = (WrCalName) vCalendar.getProperty(WrCalName.PROPERTY_NAME);
        if (null == property) {
            property = new WrCalName(PropertyFactoryImpl.getInstance());
            vCalendar.getProperties().add(property);
        }
        property.setValue(name);
    }

    @Override
    public void setMethod(String method) {
        Method property = (Method) vCalendar.getProperty(Property.METHOD);
        if (null == property) {
            property = new Method();
            vCalendar.getProperties().add(property);
        }
        property.setValue(method);
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    @Override
    public CalendarExport add(Event event) throws OXException {
        vCalendar.add(exportEvent(event));
        return this;
    }

    @Override
    public CalendarExport add(FreeBusyData freeBusyData) throws OXException {
        vCalendar.add(exportFreeBusy(freeBusyData));
        return this;
    }

    @Override
    public CalendarExport add(Availability calendarAvailability) throws OXException {
        vCalendar.add(exportAvailability(calendarAvailability));
        return this;
    }

    @Override
    public CalendarExport add(String timeZoneID) {
        trackTimezones(timeZoneID);
        return this;
    }

    @Override
    public ThresholdFileHolder getVCalendar() throws OXException {
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        writeVCalendar(fileHolder.asOutputStream());
        return fileHolder;
    }

    @Override
    public void writeVCalendar(OutputStream outputStream) throws OXException {
        /*
         * add components for all contained timezones
         */
        for (String timezoneID : timezoneIDs) {
            TimeZoneRegistry timeZoneRegistry = parameters.get(ICalParametersImpl.TIMEZONE_REGISTRY, TimeZoneRegistry.class);
            net.fortuna.ical4j.model.TimeZone timeZone = timeZoneRegistry.getTimeZone(timezoneID);
            if (null != timeZone) {
                vCalendar.add(0, timeZone.getVTimeZone());
            } else {
                warnings.add(ICalExceptionCodes.CONVERSION_FAILED.create(Component.VTIMEZONE, "No timezone '" + timezoneID + "' registered."));
            }
        }
        /*
         * export calendar
         */
        ICalUtils.exportCalendar(vCalendar, outputStream);
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

    private VEvent exportEvent(Event event) {
        /*
         * export event data, track timezones
         */
        VEvent vEvent = mapper.exportEvent(event, parameters, warnings);
        ICalUtils.removeProperties(vEvent, parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class));
        if (false == CalendarUtils.isFloating(event)) {
            trackTimezones(event.getStartDate(), event.getEndDate());
        }
        /*
         * export alarms as sub-components
         */
        List<Alarm> alarms = event.getAlarms();
        if (null != alarms && 0 < alarms.size()) {
            for (Alarm alarm : alarms) {
                vEvent.getAlarms().add(exportAlarm(alarm));
            }
        }
        return vEvent;
    }

    private VAlarm exportAlarm(Alarm alarm) {
        /*
         * export alarm data
         */
        VAlarm vAlarm = mapper.exportAlarm(alarm, parameters, warnings);
        ICalUtils.removeProperties(vAlarm, parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class));
        return vAlarm;
    }

    private VFreeBusy exportFreeBusy(FreeBusyData freeBusyData) {
        /*
         * export free/busy data, track timezones
         */
        VFreeBusy vFreeBusy = mapper.exportFreeBusy(freeBusyData, parameters, warnings);
        ICalUtils.removeProperties(vFreeBusy, parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class));
        trackTimezones(freeBusyData.getStartDate(), freeBusyData.getEndDate());
        return vFreeBusy;
    }

    /**
     * Exports the specified {@link Availability} to a {@link VAvailability} component
     *
     * @param availability The {@link Availability} to export
     * @return The exported {@link VAvailability} component
     * @throws OXException if an error is occurred
     */
    private VAvailability exportAvailability(Availability availability) throws OXException {
        VAvailability vAvailability = mapper.exportAvailability(availability, parameters, warnings);
        ICalUtils.removeProperties(vAvailability, parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class));
        // TODO: Track timezones of availability/available components
        return vAvailability;
    }

    private boolean trackTimezones(String... timeZoneIDs) {
        boolean added = false;
        if (null != timeZoneIDs && 0 < timeZoneIDs.length) {
            for (String timeZoneID : timeZoneIDs) {
                if (null != timeZoneID && false == "UTC".equals(timeZoneID)) {
                    added |= timezoneIDs.add(timeZoneID);
                }
            }
        }
        return added;
    }

    private boolean trackTimezones(org.dmfs.rfc5545.DateTime... dateTimes) {
        boolean added = false;
        if (null != dateTimes && 0 < dateTimes.length) {
            for (int i = 0; i < dateTimes.length; i++) {
                org.dmfs.rfc5545.DateTime dateTime = dateTimes[i];
                if (null != dateTime && false == dateTime.isFloating() && null != dateTime.getTimeZone()) {
                    added |= trackTimezones(dateTime.getTimeZone().getID());
                }
            }
        }
        return added;
    }
    
    public boolean trackTimeZones(Event event) {
        boolean added = false;
        if (false == CalendarUtils.isFloating(event)) {
            added |= trackTimezones(event.getStartDate(), event.getEndDate());
        }
        return added;
    }

    private static VCalendar initCalendar() {
        VCalendar vCalendar = new VCalendar();
        vCalendar.getProperties().add(Version.VERSION_2_0);
        VersionService versionService = Services.getService(VersionService.class);
        String versionString = null; 
        if (null == versionService) {
            versionString = "<unknown version>";
        } else {
            versionString = versionService.getVersionString();
        }
        vCalendar.getProperties().add(new ProdId("-//" + VersionService.NAME + "//" + versionString + "//EN"));
        return vCalendar;
    }

}
