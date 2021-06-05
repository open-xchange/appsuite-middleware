/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
