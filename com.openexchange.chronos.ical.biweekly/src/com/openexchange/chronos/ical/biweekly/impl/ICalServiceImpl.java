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

package com.openexchange.chronos.ical.biweekly.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.io.TimezoneInfo;
import biweekly.io.text.ICalReader;
import biweekly.io.text.ICalWriter;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.AlarmData;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.CalendarImport;
import com.openexchange.chronos.ical.EventData;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.biweekly.impl.mapping.ICalMapper;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link ICalServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICalServiceImpl implements ICalService {

    private final ICalMapper mapper;

    public ICalServiceImpl() {
        super();
        this.mapper = new ICalMapper();
    }

    @Override
    public CalendarImport importICal(InputStream iCalFile, ICalParameters parameters) throws OXException {
        List<OXException> warnings = new ArrayList<OXException>();
        ICalParameters iCalParameters = getParametersOrDefault(parameters);
        ICalendar iCalendar = null;
        try {
            List<List<String>> parserWarnings = new ArrayList<List<String>>();

            //        	ChainingTextParser<ChainingTextParser<?>> parser = new ChainingTextParser<>(iCalFile);

            ICalReader reader = new ICalReader(iCalFile);

            iCalendar = reader.readNext();
            if (null != parserWarnings && 0 < parserWarnings.size()) {
                warnings.addAll(getParserWarnings(reader.getWarnings()));
            }
            TimezoneInfo timezoneInfo = reader.getTimezoneInfo();
            iCalParameters.set("TIMEZONE_INFO", timezoneInfo);
            reader.close();

            //        	            iCalendar = Biweekly.parse(iCalFile).warnings(parserWarnings).first();
            if (0 < parserWarnings.size()) {
                warnings.addAll(getParserWarnings(parserWarnings.get(0)));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (null == iCalendar) {
            return null;
        }
        String method = null == iCalendar.getMethod() ? null : iCalendar.getMethod().getValue();
        List<EventData> vEventImports = importEvents(iCalendar.getEvents(), iCalParameters, warnings);
        return new DefaultCalendarImport(method, vEventImports, warnings);
    }

    @Override
    public CalendarExport exportICal(ICalParameters parameters) {
        return new DefaultCalendarExport(mapper, parameters);
    }

    private List<EventData> importEvents(List<VEvent> vEvents, ICalParameters iCalParameters, List<OXException> warnings) throws OXException {
        if (null == vEvents) {
            return null;
        }
        List<EventData> events = new ArrayList<EventData>(vEvents.size());
        for (VEvent vEvent : vEvents) {
            /*
             * import VALARM subcomponents separately
             */
            List<AlarmData> alarms = importAlarms(vEvent.getAlarms(), iCalParameters, warnings);
            /*
             * import VEVENT
             */
            Event event = mapper.importVEvent(vEvent, null, iCalParameters, warnings);
            ThresholdFileHolder iCalHolder = exportComponent(vEvent, null);
            events.add(new DefaultEventData(event, alarms, iCalHolder));
        }
        return events;
    }

    private List<AlarmData> importAlarms(List<VAlarm> vAlarms, ICalParameters iCalParameters, List<OXException> warnings) throws OXException {
        if (null == vAlarms) {
            return null;
        }
        List<AlarmData> alarms = new ArrayList<AlarmData>(vAlarms.size());
        for (VAlarm vAlarm : vAlarms) {
            Alarm alarm = mapper.importVAlarm(vAlarm, null, iCalParameters, warnings);
            ThresholdFileHolder iCalHolder = exportComponent(vAlarm, null);
            alarms.add(new DefaultAlarmData(alarm, iCalHolder));
        }
        return alarms;
    }

    static ThresholdFileHolder exportComponent(ICalComponent component, ICalParameters parameters) throws OXException {
        ICalendar iCalendar = new ICalendar();
        iCalendar.addComponent(component);
        return exportICalendar(iCalendar, parameters);
    }

    static ThresholdFileHolder exportICalendar(ICalendar iCalendar, ICalParameters parameters) throws OXException {
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        ICalWriter iCalWriter = null;
        try {
            iCalWriter = new ICalWriter(fileHolder.asOutputStream(), ICalVersion.V2_0);
            applyParameters(iCalWriter, parameters);
            iCalWriter.write(iCalendar);
            iCalWriter.flush();
        } catch (IOException e) {
            throw new OXException();
        } finally {
            Streams.close(iCalWriter);
        }
        return fileHolder;
    }

    /**
     * Gets the iCal parameters, or the default parameters if passed instance is <code>null</code>.
     *
     * @param parameters The parameters as passed from the client
     * @return The parameters, or the default parameters if passed instance is <code>null</code>
     */
    private ICalParameters getParametersOrDefault(ICalParameters parameters) {
        return null != parameters ? parameters : new ICalParametersImpl();
    }

    private static void applyParameters(ICalWriter writer, ICalParameters parameters) {
        if (null != parameters) {
            TimezoneInfo tzInfo = parameters.get("TIMEZONE_INFO", TimezoneInfo.class);
            if (null != tzInfo) {
                writer.setTimezoneInfo(tzInfo);
            }
        }
    }

    private static List<OXException> getParserWarnings(List<String> parserWarnings) {
        if (null == parserWarnings || 0 == parserWarnings.size()) {
            return Collections.emptyList();
        }
        List<OXException> warnings = new ArrayList<OXException>();
        for (String parserWarning : parserWarnings) {
            warnings.add(OXException.general(parserWarning));
        }
        return warnings;
    }

}
