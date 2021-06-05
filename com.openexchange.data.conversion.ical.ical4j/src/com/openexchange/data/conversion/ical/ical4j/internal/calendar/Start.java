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

package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import static com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools.toDate;
import static com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools.toDateTime;
import static com.openexchange.data.conversion.ical.ical4j.internal.ParserTools.isDateTime;
import static com.openexchange.data.conversion.ical.ical4j.internal.ParserTools.parseDateConsideringDateType;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtStart;

/**
 * Converts the start date.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Start<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {

    public Start() {
        super();
    }

    @Override
    public void emit(final Mode mode, final int index, final U calendar, final T component, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        /*
         * emit as date or date-time depending on fulltime flag
         */
        DtStart start = new DtStart();
        if (calendar.getFullTime()) {
            start.setDate(toDate(calendar.getStartDate()));
        } else {
            start.setDate(toDateTime(mode.getZoneInfo(), calendar.getStartDate(), EmitterTools.extractTimezoneIfPossible(calendar)));
        }
        component.getProperties().add(start);
    }

    @Override
    public boolean hasProperty(final T component) {
        return null != component.getProperty(Property.DTSTART);
    }

    @Override
    public boolean isSet(final U calendar) {
        return calendar.containsStartDate();
    }

    @Override
    public void parse(final int index, final T component, final U calendar, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        if (overrideFullTimeSetting(component, calendar)) {
            return;
        }

        final DtStart dtStart = new DtStart();
        final boolean isDateTime = isDateTime(component, dtStart);
        final TimeZone UTC = TimeZone.getTimeZone("UTC");
        final Date start = parseDateConsideringDateType(component, dtStart, timeZone);
        calendar.setStartDate(start);
        calendar.setFullTime(false == isDateTime);
        if (component.getProperty(Property.DTEND) == null && calendar instanceof Appointment) {
            // If an end is specified end date will be overwritten.
            if (isDateTime) {
                /* RFC 2445 4.6.1:
                 * For cases where a "VEVENT" calendar component specifies a "DTSTART"
                 * property with a DATE-TIME data type but no "DTEND" property, the
                 * event ends on the same calendar date and time of day specified by
                 * the "DTSTART" property.
                 */
                calendar.setEndDate(start);
            } else {
                // Only the date is specified. Then we have to set the end to at
                // least 1 day later. Will be overwritten if DTEND is specified.
                final Calendar calendarUTC = new GregorianCalendar();
                calendarUTC.setTimeZone(UTC);
                calendarUTC.setTime(start);
                calendarUTC.add(Calendar.DATE, 1);
                calendar.setEndDate(calendarUTC.getTime());
                // Special flag for appointments.
            }
        }
    }

    /**
     * Overrides fulltime setting depending on Exchange Property: X-MICROSOFT-CDO-ALLDAYEVENT
     *
     * @param component
     * @param calendar
     */
    private boolean overrideFullTimeSetting(T component, U calendar) {
        if (Boolean.FALSE.equals(calendar.getProperty("com.openexchange.data.conversion.ical.useXMicrosoftCDOAllDayEvent"))) {
            return false;
        }
        DtStart dtStart = new DtStart();
        Property msAllDay = component.getProperty(XMicrosoftCdoAlldayEvent.property);
        if (msAllDay != null && msAllDay.getValue().equalsIgnoreCase("true")) {
            DateProperty dateProperty = (DateProperty)component.getProperty(dtStart.getName());
            if (ParserTools.isDateOrMidnight(dateProperty)) {
                int timeZoneOffset = dateProperty.getTimeZone() != null ? dateProperty.getTimeZone().getOffset(dateProperty.getDate().getTime()) : 0;
                Date s = new Date(dateProperty.getDate().getTime() + timeZoneOffset);
                calendar.setStartDate(s);
                calendar.setEndDate(s);
                calendar.setFullTime(true);
                return true;
            }
        }
        return false;
    }
}
