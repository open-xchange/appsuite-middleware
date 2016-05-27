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

package com.openexchange.chronos.ical.impl.mapping;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import biweekly.component.ICalComponent;
import biweekly.io.TimezoneInfo;
import biweekly.property.DateOrDateTimeProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDate;

import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;

/**
 * {@link ICalDateOrDateTimeMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalDateOrDateTimeMapping<T extends ICalComponent, U> extends AbstractICalMapping<T, U> {
	
	private final Class<? extends biweekly.property.DateOrDateTimeProperty> clazz;
	
    /**
     * Initializes a new {@link ICalDateOrDateTimeMapping}.
     * 
     * @param clazz The class of the mapping's date-time property 
     */
	protected ICalDateOrDateTimeMapping(Class<? extends biweekly.property.DateOrDateTimeProperty> clazz) {
		super();
		this.clazz = clazz;
	}
	
	protected abstract Date getValue(U object);
	
	protected abstract String getTimezone(U object);
	
	protected abstract boolean isAllDay(U object);

	protected abstract void setValue(U object, Date value, String timezone, boolean allDay);

	protected abstract biweekly.property.DateOrDateTimeProperty createProperty();

	@Override
	public void export(U object, T component, ICalParameters parameters, List<OXException> warnings) {
		Date value = getValue(object);
		if (null == value) {
			/*
			 * not set
			 */
			component.removeProperties(clazz);
		} else {			
			String timezone = getTimezone(object);
			boolean allDay = isAllDay(object);
			DateOrDateTimeProperty property = component.getProperty(clazz);
			if (null == property) {
				property = createProperty();
				component.addProperty(property);
			}			
			if (allDay) {
				/*
				 * all-day event
				 */
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				calendar.setTime(value);
				DateTimeComponents rawComponents = new DateTimeComponents(calendar.get(Calendar.YEAR), 1 + calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
				property.setValue(new ICalDate(rawComponents, false));
			} else if ("UTC".equals(timezone)) {
				/*
				 * UTC timestamp
				 */
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				calendar.setTime(value);
				DateTimeComponents rawComponents = new DateTimeComponents(
					calendar.get(Calendar.YEAR), 1 + calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),
					calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), true);
				property.setValue(new ICalDate(rawComponents, true));
			} else if (null == timezone) {
				/*
				 * floating / local time
				 */
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				calendar.setTime(value);
				DateTimeComponents rawComponents = new DateTimeComponents(
					calendar.get(Calendar.YEAR), 1 + calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),
					calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), false);
				property.setValue(new ICalDate(rawComponents, true));
				trackTimezone(parameters, property, null);
			} else {
				/*
				 * date-time with timezone
				 */
				property.setValue(value, true);
				trackTimezone(parameters, property, TimeZone.getTimeZone(timezone));
			}
		}
	}

	@Override
	public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
		biweekly.property.DateOrDateTimeProperty property = component.getProperty(clazz);
		if (null == property) {
			/*
			 * not set
			 */
			setValue(object, null, null, false);
		} else if (false == property.getValue().hasTime()) {
			/*
			 * "VALUE=DATE", assume all day 
			 */
			DateTimeComponents rawComponents = property.getValue().getRawComponents();
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			calendar.clear();
			calendar.set(rawComponents.getYear(), rawComponents.getMonth() - 1, rawComponents.getDate());
			setValue(object, calendar.getTime(), null, true);
		} else {
			/*
			 * "VALUE=DATE-TIME", otherwise
			 */
			String timezone;
			if (property.getValue().getRawComponents().isUtc()) {
				timezone = "UTC";				
			} else {
				timezone = property.getParameter("TZID");
				if (null == timezone && null != parameters) {
					TimezoneInfo timezoneInfo = parameters.get(ICalParameters.TIMEZONE_INFO, TimezoneInfo.class);
					TimeZone timeZone = timezoneInfo.getTimeZone(property);
					timezone = timeZone.getID();
				}
			}
			setValue(object, new Date(property.getValue().getTime()), timezone, false);
		}
	}
	
}
