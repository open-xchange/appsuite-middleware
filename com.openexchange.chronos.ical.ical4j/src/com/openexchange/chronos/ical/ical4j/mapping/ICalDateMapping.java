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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.DateProperty;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link ICalDateMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalDateMapping<T extends CalendarComponent, U> extends AbstractICalMapping<T, U> {

    private final String propertyName;

    /**
     * Initializes a new {@link ICalDateMapping}.
     * 
     * @param propertyName The name of the mapping's property
     */
    protected ICalDateMapping(String propertyName) {
        super();
        this.propertyName = propertyName;
    }

    protected abstract Date getValue(U object);

    protected abstract String getTimezone(U object);

    protected abstract boolean hasTime(U object);

    protected abstract void setValue(U object, Date value, String timezone, boolean hasTime);

    protected abstract DateProperty createProperty();

    @Override
    public void export(U object, T component, ICalParameters parameters, List<OXException> warnings) {
        Date value = getValue(object);
        if (null == value) {
            removeProperties(component, propertyName);
        } else {
            DateProperty property = (DateProperty) component.getProperty(propertyName);
            if (null == property) {
                property = createProperty();
                component.getProperties().add(property);
            }
            if (hasTime(object)) {
                String timezoneID = getTimezone(object);
                if (Strings.isNotEmpty(timezoneID)) {
                    TimeZoneRegistry timeZoneRegistry = parameters.get(ICalParameters.TIMEZONE_REGISTRY, TimeZoneRegistry.class);
                    net.fortuna.ical4j.model.TimeZone timeZone = timeZoneRegistry.getTimeZone(timezoneID);
                    if (null != timeZone) {
                        DateTime dateTime = new DateTime(false);
                        dateTime.setTimeZone(timeZone);
                        dateTime.setTime(value.getTime());
                        property.setDate(dateTime);
                    } else {
                        addConversionWarning(warnings, propertyName, "No timezone '" + timezoneID + "' registered.");
                        DateTime dateTime = new DateTime(true);
                        dateTime.setTime(value.getTime());
                        property.setDate(dateTime);
                    }
                } else {
                    DateTime dateTime = new DateTime(true);
                    dateTime.setTime(value.getTime());
                    property.setDate(dateTime);
                }
            } else {
                property.setDate(new net.fortuna.ical4j.model.Date(value.getTime()));
            }
        }
    }

    @Override
    public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
        DateProperty property = (DateProperty) component.getProperty(propertyName);
        if (null == property || null == property.getDate()) {
            setValue(object, null, null, true);
        } else {
            TimeZone defaultTimeZone = parameters.get(ICalParameters.DEFAULT_TIMEZONE, TimeZone.class);
            if (ParserTools.isDateTime(property)) {
                Date value = ParserTools.parseDateConsideringDateType(component, property, defaultTimeZone);
                TimeZone timeZone = TimeZoneUtils.selectTimeZone(property, defaultTimeZone);
                setValue(object, value, null != timeZone ? timeZone.getID() : null, true);
            } else {
                setValue(object, ParserTools.parseDate(component, property, defaultTimeZone), null, false);
            }
            //			net.fortuna.ical4j.model.Date date = property.getDate();
            //			Date value;
            //			String timezone;
            //			boolean hasTime;
            //			if (DateTime.class.isInstance(date)) {
            //				hasTime = true;
            //				TimeZone timeZone = ((DateTime) date).getTimeZone();
            //				timezone = null != timeZone ? timeZone.getID() : null;
            //				value = new Date(date.getTime());				
            //			} else {
            //				hasTime = false;
            //				timezone = null;
            //				value = new Date(date.getTime());
            //			}
            //			setValue(object, value, timezone, hasTime);

        }
    }

}
