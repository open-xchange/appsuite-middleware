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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.ParserTools;
import com.openexchange.chronos.ical.impl.ICalParametersImpl;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.property.DateProperty;

/**
 * {@link ICalDateTimeMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalDateTimeMapping<T extends Component, U> extends AbstractICalMapping<T, U> {

    private final String propertyName;

    /**
     * Initializes a new {@link ICalDateTimeMapping}.
     *
     * @param propertyName The name of the mapping's property
     */
    protected ICalDateTimeMapping(String propertyName) {
        super();
        this.propertyName = propertyName;
    }

    protected abstract org.dmfs.rfc5545.DateTime getValue(U object);

    protected abstract void setValue(U object, org.dmfs.rfc5545.DateTime value);

    protected abstract DateProperty createProperty();

    protected abstract DateProperty getProperty(T component);

    @Override
    public void export(U object, T component, ICalParameters parameters, List<OXException> warnings) {
        org.dmfs.rfc5545.DateTime value = getValue(object);
        if (null == value) {
            removeProperties(component, propertyName);
        } else {
            DateProperty property = (DateProperty) component.getProperty(propertyName);
            if (null == property) {
                property = createProperty();
                component.getProperties().add(property);
            }
            if (value.isAllDay()) {
                property.setDate(new net.fortuna.ical4j.model.Date(value.getTimestamp()));
                property.setTimeZone(null);
            } else {
                DateTime dateTime;
                String timezoneID = null != value.getTimeZone() ? value.getTimeZone().getID() : null;
                if (Strings.isNotEmpty(timezoneID) && false == "UTC".equals(timezoneID)) {
                    TimeZoneRegistry timeZoneRegistry = parameters.get(ICalParametersImpl.TIMEZONE_REGISTRY, TimeZoneRegistry.class);
                    net.fortuna.ical4j.model.TimeZone timeZone = timeZoneRegistry.getTimeZone(timezoneID);
                    if (null != timeZone) {
                        dateTime = new DateTime(false);
                        dateTime.setTimeZone(timeZone);
                        dateTime.setTime(value.getTimestamp());
                    } else {
                        addConversionWarning(warnings, propertyName, "No timezone '" + timezoneID + "' registered.");
                        dateTime = new DateTime(true);
                        dateTime.setTime(value.getTimestamp());
                    }
                } else {
                    dateTime = new DateTime(true);
                    dateTime.setTime(value.getTimestamp());
                }
                property.setDate(dateTime);
            }
        }
    }

    @Override
    public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
        DateProperty property = getProperty(component);
        if (null != property && null != property.getDate()) {
            if (ParserTools.isDateTime(property)) {
                TimeZone timeZone = selectTimeZone(property, (TimeZone) null);
                setValue(object, org.dmfs.rfc5545.DateTime.parse(timeZone, property.getValue()));
            } else {
                setValue(object, org.dmfs.rfc5545.DateTime.parse(property.getValue()).toAllDay());
            }
        } else if (false == isIgnoreUnsetProperties(parameters)) {
            setValue(object, null);
        }
    }

    private static TimeZone selectTimeZone(DateProperty property, TimeZone defaultTimeZone) {
        if (property.isUtc()) {
            return TimeZones.UTC;
        }
        net.fortuna.ical4j.model.TimeZone parsedTimeZone = property.getTimeZone();
        if (null != parsedTimeZone) {
            return parsedTimeZone;
        }
        Parameter tzidParameter = property.getParameter(Parameter.TZID);
        if (null != tzidParameter && Strings.isNotEmpty(tzidParameter.getValue())) {
            return CalendarUtils.optTimeZone(tzidParameter.getValue(), defaultTimeZone);
        }
        return defaultTimeZone;
    }

}
