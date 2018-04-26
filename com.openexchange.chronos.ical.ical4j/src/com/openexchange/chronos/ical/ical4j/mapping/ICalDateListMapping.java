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
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
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
        if (null != value) {
            DateList dateList = new DateList();
            dateList.setUtc(true);
            for (RecurrenceId date : value) {
                if (date.getValue().isAllDay()) {
                    dateList.add(new net.fortuna.ical4j.model.Date(date.getValue().getTimestamp()));
                } else {
                    DateTime dateTime = new net.fortuna.ical4j.model.DateTime(true);
                    dateTime.setTime(date.getValue().getTimestamp());
                    dateList.add(dateTime);
                }
            }
            component.getProperties().add(createProperty(dateList));
        }
    }

    @Override
    public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
        PropertyList properties = component.getProperties(propertyName);
        if (null != properties && 0 < properties.size()) {
            SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
            for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
                DateListProperty property = (DateListProperty) iterator.next();
                for (Iterator<?> i = property.getDates().iterator(); i.hasNext();) {
                    net.fortuna.ical4j.model.Date date = (net.fortuna.ical4j.model.Date) i.next();
                    org.dmfs.rfc5545.DateTime exceptionDate;
                    if (DateTime.class.isInstance(date)) {
                        net.fortuna.ical4j.model.TimeZone parsedTimeZone = ((DateTime) date).getTimeZone();
                        if (null != parsedTimeZone) {
                            exceptionDate = new org.dmfs.rfc5545.DateTime(parsedTimeZone, date.getTime());
                        } else {
                            exceptionDate = new org.dmfs.rfc5545.DateTime(date.getTime());
                        }
                    } else {
                        exceptionDate = new org.dmfs.rfc5545.DateTime(date.getTime()).toAllDay();
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
