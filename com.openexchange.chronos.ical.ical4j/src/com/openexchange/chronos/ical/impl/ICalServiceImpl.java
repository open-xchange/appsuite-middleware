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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;

import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.AlarmData;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.CalendarImport;
import com.openexchange.chronos.ical.EventData;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.exception.OXException;

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
    	ICalParameters iCalParameters = getParametersOrDefault(parameters);
        List<OXException> warnings = new ArrayList<OXException>();
        Calendar calendar = ICalUtils.importCalendar(iCalFile, iCalParameters);
        return importCalendar(calendar, iCalParameters, warnings);
    }
    
    private DefaultCalendarImport importCalendar(Calendar calendar, ICalParameters parameters, List<OXException> warnings) throws OXException {
    	String method = null != calendar.getMethod() ? calendar.getMethod().getValue() : null;
    	List<EventData> events = importEvents(calendar.getComponents(Component.VEVENT), parameters, warnings);
    	return new DefaultCalendarImport(method, events, warnings);
    }
    
    @Override
    public CalendarExport exportICal(ICalParameters parameters) { 
    	return new DefaultCalendarExport(mapper, parameters);
    }
    
    private List<EventData> importEvents(ComponentList eventComponents, ICalParameters parameters, List<OXException> warnings) throws OXException {
    	if (null == eventComponents) {
    		return null;
    	}
    	List<EventData> events = new ArrayList<EventData>(eventComponents.size());
        for (Iterator<?> iterator = eventComponents.iterator(); iterator.hasNext();) {
            VEvent vEvent = (VEvent) iterator.next();
            List<AlarmData> alarms = importAlarms(vEvent.getAlarms(), parameters, warnings);
            Event event = mapper.importVEvent(vEvent, null, parameters, warnings);
            if (Boolean.TRUE.equals(parameters.get(ICalParameters.KEEP_COMPONENTS, Boolean.class))) {
            	events.add(new DefaultEventData(event, alarms, ICalUtils.exportComponent(vEvent, parameters)));
            } else {
            	events.add(new DefaultEventData(event, alarms, null));
            }
        }
        return events;
    }
    
    private List<AlarmData> importAlarms(ComponentList alarmComponents, ICalParameters parameters, List<OXException> warnings) throws OXException {
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

    /**
     * Gets the iCal parameters, or the default parameters if passed instance is <code>null</code>.
     *
     * @param parameters The parameters as passed from the client
     * @return The parameters, or the default parameters if passed instance is <code>null</code>
     */
    private ICalParameters getParametersOrDefault(ICalParameters parameters) {
        return null != parameters ? parameters : new ICalParametersImpl();
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