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
import biweekly.property.DateOrDateTimeProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDate;

import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;

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
				Calendar calendar = Calendar.getInstance(TimeZones.UTC);
				calendar.setTime(value);
				DateTimeComponents rawComponents = new DateTimeComponents(calendar.get(Calendar.YEAR), 1 + calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
				property.setValue(new ICalDate(rawComponents, false));
			} else if ("UTC".equals(timezone)) {
				/*
				 * UTC timestamp
				 */
				property.setValue(new ICalDate(getRawComponents(value, true), true));
			} else if (null == timezone) {
				/*
				 * floating / local time
				 */
				property.setValue(new ICalDate(getRawComponents(value, false), false));
				trackTimeZone(parameters, property, null);
			} else {
				/*
				 * date-time with timezone
				 */
				property.setValue(value, true);
				trackTimeZone(parameters, property, TimeZone.getTimeZone(timezone));
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
			setValue(object, getUTCDate(property.getValue().getRawComponents()), null, true);
		} else {
			/*
			 * "VALUE=DATE-TIME", otherwise
			 */
			TimeZone timeZone = selectTimeZone(property, parameters, warnings);
			String timezone = null != timeZone ? timeZone.getID() : null;
			setValue(object, new Date(property.getValue().getTime()), timezone, false);
		}
	}
	
	/**
	 * Gets the iCal raw components for a date. 
	 * 
	 * @param value The date to get the raw components for
	 * @param utc <code>true</code> to assume UTC, <code>false</code>, otherwise
	 * @return The date time components
	 */
	private static DateTimeComponents getRawComponents(Date value, boolean utc) {
		Calendar calendar = Calendar.getInstance(TimeZones.UTC);
		calendar.setTime(value);
		return new DateTimeComponents(
			calendar.get(Calendar.YEAR), 1 + calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),
			calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), utc);
	}
	
	/**
	 * Creates a UTC date instance with all time components set to 0.
	 * 
	 * @param rawComponents The raw iCal date components
	 * @return The UTC date
	 */
	private static Date getUTCDate(DateTimeComponents rawComponents) {
		return getUTCDate(rawComponents.getYear(), rawComponents.getMonth() - 1, rawComponents.getDate());
	}
	
	/**
	 * Creates a UTC date instance with all time components set to 0.
	 * 
	 * @param year The value used to set the YEAR calendar field
	 * @param month The value used to set the MONTH calendar field (0-based)
	 * @param date The value used to set the DAY_OF_MONTH calendar field
	 * @return The UTC date
	 */
	private static Date getUTCDate(int year, int month, int date) {
		Calendar calendar = Calendar.getInstance(TimeZones.UTC);
		calendar.clear();
		calendar.set(year, month, date);
		return calendar.getTime();
	}
	
    private static TimeZone selectTimeZone(biweekly.property.DateOrDateTimeProperty property, ICalParameters parameters, List<OXException> warnings) {
		if (property.getValue().getRawComponents().isUtc()) {
			return TimeZones.UTC;			
		}
		TimeZone parsedTimeZone = getTimeZone(parameters, property);
		if (null == parsedTimeZone) {
			String tzidParameter = property.getParameter("TZID");
			return Strings.isNotEmpty(tzidParameter) ? TimeZoneUtils.selectTimeZone(tzidParameter, null) : null;
		}
		return TimeZoneUtils.selectTimeZone(parsedTimeZone, null);
    }

}
