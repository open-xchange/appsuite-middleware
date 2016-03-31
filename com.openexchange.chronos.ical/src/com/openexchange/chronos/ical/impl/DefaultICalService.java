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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import biweekly.Biweekly;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.io.TimezoneInfo;
import biweekly.io.text.ICalWriter;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.VAlarmImport;
import com.openexchange.chronos.ical.VCalendarImport;
import com.openexchange.chronos.ical.VEventImport;
import com.openexchange.chronos.ical.impl.mapping.ICalMapper;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link DefaultICalService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultICalService implements ICalService {

    private final ICalMapper mapper;

    public DefaultICalService() {
        super();
        this.mapper = new ICalMapper();
    }

    @Override
    public VCalendarImport importICal(InputStream iCalFile, ICalParameters parameters) throws OXException {
        List<OXException> warnings = new ArrayList<OXException>();
        ICalParameters iCalParameters = getParametersOrDefault(parameters);

        ICalendar iCalendar = null;
        try {
            iCalendar = Biweekly.parse(iCalFile).first();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (null != iCalendar) {
            String method = null == iCalendar.getMethod() ? null : iCalendar.getMethod().getValue();
            List<VEvent> vEvents = iCalendar.getEvents();
            List<VEventImport> vEventImports;
            if (null == vEvents) {
                vEventImports = null;
            } else {
                vEventImports = new ArrayList<VEventImport>(vEvents.size());
                for (VEvent vEvent : vEvents) {
                    /*
                     * import VALARM subcomponents separately
                     */
                    List<VAlarm> vAlarms = vEvent.getAlarms();
                    List<VAlarmImport> vAlarmImports;
                    if (null == vAlarms) {
                        vAlarmImports = null;
                    } else {
                        vAlarmImports = new ArrayList<VAlarmImport>(vAlarms.size());
                        for (VAlarm vAlarm : vAlarms) {
                            Alarm alarm = mapper.importVAlarm(vAlarm, null, iCalParameters, warnings);
                            ThresholdFileHolder iCalHolder = exportComponent(vAlarm, null);
                            vAlarmImports.add(new DefaultVAlarmImport(alarm, iCalHolder));
                        }
                    }
                    /*
                     * import VEVENT
                     */
                    Event event = mapper.importVEvent(vEvent, null, iCalParameters, warnings);
                    ThresholdFileHolder iCalHolder = exportComponent(vEvent, null);
                    vEventImports.add(new DefaultVEventImport(event, iCalHolder, vAlarmImports));
                }
            }
            return new DefaultVCalendarImport(method, vEventImports, warnings);
        }
        return null;
    }

    private ThresholdFileHolder exportComponent(ICalComponent component, ICalParameters parameters) throws OXException {
        ICalendar iCalendar = new ICalendar();
        iCalendar.addComponent(component);
        return exportICalendar(iCalendar, parameters);
    }

    private ThresholdFileHolder exportICalendar(ICalendar iCalendar, ICalParameters parameters) throws OXException {
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
     * Gets the vCard parameters, or the default parameters if passed instance is <code>null</code>.
     *
     * @param parameters The parameters as passed from the client
     * @return The parameters, or the default parameters if passed instance is <code>null</code>
     */
    private ICalParameters getParametersOrDefault(ICalParameters parameters) {
        return null != parameters ? parameters : new ICalParametersImpl();
    }

    private void applyParameters(ICalWriter writer, ICalParameters parameters) {
        if (null != parameters) {
            TimezoneInfo tzInfo = parameters.get(ICalParameters.TIMEZONE_INFO, TimezoneInfo.class);
            if (null != tzInfo) {
                writer.setTimezoneInfo(tzInfo);
            }
        }
    }

}