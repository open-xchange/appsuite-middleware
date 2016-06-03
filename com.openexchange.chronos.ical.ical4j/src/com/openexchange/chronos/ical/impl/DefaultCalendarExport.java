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
import java.util.List;

import net.fortuna.ical4j.extensions.property.WrCalName;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Method;

import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.ical.AlarmData;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.EventData;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultCalendarExport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultCalendarExport implements CalendarExport {

    private final List<OXException> warnings;
    private final ICalMapper mapper;
    private final ICalParameters parameters;
    private final Calendar calendar;
    
    /**
     * Initializes a new {@link DefaultCalendarExport}.
     */
    public DefaultCalendarExport(ICalMapper mapper, ICalParameters parameters) {
        super();
        this.parameters = ICalUtils.getParametersOrDefault(parameters);
        this.warnings = new ArrayList<OXException>();
        this.calendar = new Calendar();
        this.mapper = mapper;
    }

	public void setName(String name) {
		WrCalName property = (WrCalName) calendar.getProperty(WrCalName.PROPERTY_NAME);
		if (null == property) {
			property = new WrCalName(PropertyFactoryImpl.getInstance());
			calendar.getProperties().add(property);
		}
		property.setValue(name);
	}

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
	public void add(EventData event) throws OXException {
		calendar.getComponents().add(exportEvent(event));
	}

	@Override
	public ThresholdFileHolder getVCalendar() throws OXException {
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

	private VAlarm exportAlarm(AlarmData alarm) throws OXException {
		VAlarm originalVAlarm = null;
		IFileHolder originalComponent = alarm.getComponent();
		if (null != originalComponent) {
			originalVAlarm = ICalUtils.parseVAlarmComponent(originalComponent, parameters, warnings);
		}
		return mapper.exportAlarm(alarm.getAlarm(), originalVAlarm, parameters, warnings);
	}
	
	private VEvent exportEvent(EventData event) throws OXException {
		VEvent originalVEvent = null;
		IFileHolder originalComponent = event.getComponent();
		if (null != originalComponent) {
			originalVEvent = ICalUtils.parseVEventComponent(originalComponent, parameters, warnings);
		}
		VEvent vEvent = mapper.exportEvent(event.getEvent(), originalVEvent, parameters, warnings);
		List<AlarmData> alarms = event.getAlarms();
		if (null != alarms && 0 < alarms.size()) {
			for (AlarmData alarm : alarms) {
				vEvent.getAlarms().add(exportAlarm(alarm));
			}
		}		
		return vEvent;
	}
	
}
