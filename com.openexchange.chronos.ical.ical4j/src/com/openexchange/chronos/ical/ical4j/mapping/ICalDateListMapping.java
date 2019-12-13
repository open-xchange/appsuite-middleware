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

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateListProperty;

/**
 * {@link ICalDateListMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalDateListMapping<T extends Component, U> extends AbstractICalMapping<T, U> {

    private final String propertyName;

    /**
     * Initializes a new {@link ICalDateListMapping}.
     *
     * @param propertyName The name of the mapping's property
     */
    protected ICalDateListMapping(String propertyName) {
        super();
        this.propertyName = propertyName;
    }

    protected abstract SortedSet<RecurrenceId> getValue(U object);

    protected abstract void setValue(U object, SortedSet<RecurrenceId> value);

    protected abstract DateListProperty createProperty(DateList dateList);

    @Override
    public void export(U object, T component, ICalParameters parameters, List<OXException> warnings) {
        removeProperties(component, propertyName);
        SortedSet<RecurrenceId> value = getValue(object);
        if (null != value && 0 < value.size()) {
            Iterator<RecurrenceId> iterator = CalendarUtils.normalizeRecurrenceIDs(value).iterator();
            Date firstICalDate = toICalDate(iterator.next().getValue(), parameters, propertyName, warnings);
            DateListProperty property;
            if (DateTime.class.isInstance(firstICalDate)) {
                property = createProperty(new DateList(Value.DATE_TIME));
                TimeZone timeZone = ((DateTime) firstICalDate).getTimeZone();
                if (null != timeZone) {
                    if ("UTC".equals(timeZone.getID())) {
                        property.setUtc(true);
                    } else {
                        property.setTimeZone(timeZone);
                    }
                } else if (((DateTime) firstICalDate).isUtc()) {
                    property.setUtc(true);
                }
            } else {
                property = createProperty(new DateList(Value.DATE));
            }
            property.getDates().add(firstICalDate);
            while (iterator.hasNext()) {
                property.getDates().add(toICalDate(iterator.next().getValue(), parameters, propertyName, warnings));
            }
            component.getProperties().add(property);
        }
    }

    @Override
    public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
        PropertyList properties = component.getProperties(propertyName);
        if (null != properties && 0 < properties.size()) {
            SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
            for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
                DateListProperty property = (DateListProperty) iterator.next();
                java.util.TimeZone timeZone = selectTimeZone(property, null);
                for (Iterator<?> i = property.getDates().iterator(); i.hasNext();) {
                    net.fortuna.ical4j.model.Date date = (net.fortuna.ical4j.model.Date) i.next();
                    org.dmfs.rfc5545.DateTime exceptionDate;
                    if (net.fortuna.ical4j.model.DateTime.class.isInstance(date)) {
                        exceptionDate = org.dmfs.rfc5545.DateTime.parse(timeZone, date.toString());
                    } else {
                        exceptionDate = org.dmfs.rfc5545.DateTime.parse(date.toString()).toAllDay();
                    }
                    recurrenceIds.add(new DefaultRecurrenceId(exceptionDate));
                }
            }
            setValue(object, recurrenceIds);
        } else if (false == isIgnoreUnsetProperties(parameters)) {
            setValue(object, null);
        }
    }

}
