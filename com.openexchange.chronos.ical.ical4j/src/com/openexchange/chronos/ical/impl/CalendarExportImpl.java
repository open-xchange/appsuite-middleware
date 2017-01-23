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
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.chronos.ical.CalendarExport;
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
        this.mapper = mapper;
        this.parameters = parameters;
        this.warnings = warnings;
        this.timezoneIDs = new HashSet<String>();
        this.calendar = initCalendar();
    }

    public void setProductId(String prodId) {
        ProdId property = calendar.getProductId();
        if (null == property) {
            property = new ProdId();
            calendar.getProperties().add(property);
        }
        property.setValue(prodId);
    }

    public void setVersion(String version) {
        Version property = calendar.getVersion();
        if (null == property) {
            property = new Version();
            calendar.getProperties().add(property);
        }
        property.setValue(version);
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
    public CalendarExport add(FreeBusyData freeBusyData) throws OXException {
        calendar.getComponents().add(exportFreeBusy(freeBusyData));
        return this;
    }

    @Override
    public CalendarExport add(String timeZoneID) throws OXException {
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
                calendar.getComponents().add(0, timeZone.getVTimeZone());
            } else {
                warnings.add(ICalExceptionCodes.CONVERSION_FAILED.create("No timezone '" + timezoneID + "' registered."));
            }
        }
        /*
         * export calendar
         */
        ICalUtils.exportCalendar(calendar, parameters, outputStream);
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

    private VEvent exportEvent(Event event) throws OXException {
        /*
         * export event data & extended properties, track timezones
         */
        VEvent vEvent = mapper.exportEvent(event, parameters, warnings);
        ICalUtils.exportProperties(event, vEvent);
        trackTimezones(event.getStartTimeZone(), event.getEndTimeZone());
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

    private VAlarm exportAlarm(Alarm alarm) throws OXException {
        /*
         * export alarm data & extended properties
         */
        VAlarm vAlarm = mapper.exportAlarm(alarm, parameters, warnings);
        ICalUtils.exportProperties(alarm, vAlarm);
        return vAlarm;
    }

    private VFreeBusy exportFreeBusy(FreeBusyData freeBusyData) throws OXException {
        /*
         * export free/busy data & extended properties, track timezones
         */
        VFreeBusy vFreeBusy = mapper.exportFreeBusy(freeBusyData, parameters, warnings);
        ICalUtils.exportProperties(freeBusyData, vFreeBusy);
        trackTimezones(freeBusyData.getStartTimeZone(), freeBusyData.getEndTimeZone());
        return vFreeBusy;
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

    private static Calendar initCalendar() {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(Version.VERSION_2_0);
        String versionString = com.openexchange.version.Version.getInstance().optVersionString();
        if (null == versionString) {
            versionString = "<unknown version>";
        }
        calendar.getProperties().add(new ProdId("-//" + com.openexchange.version.Version.NAME + "//" + versionString + "//EN"));
        return calendar;
    }

}
