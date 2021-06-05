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
import static com.openexchange.data.conversion.ical.ical4j.internal.ParserTools.parseDateConsideringDateType;
import java.util.Date;
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
import net.fortuna.ical4j.model.property.DtEnd;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class End<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {

    public End() {
        super();
    }

    @Override
    public void emit(final Mode mode, final int index, final U calendar, final T component, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        final DtEnd end = new DtEnd();
        String tz = EmitterTools.extractTimezoneIfPossible(calendar);
        final net.fortuna.ical4j.model.Date date = (needsDate(calendar)) ? toDate(calendar.getEndDate()) : toDateTime(mode.getZoneInfo(), calendar.getEndDate(),tz);
        end.setDate(date);
        component.getProperties().add(end);
    }

    private boolean needsDate(final U calendar) {
        return Appointment.class.isAssignableFrom(calendar.getClass()) && ((Appointment)calendar).getFullTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(final T component) {
        return null != component.getProperty(Property.DTEND);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSet(final U calendar) {
        return calendar.containsEndDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(final int index, final T component, final U calendar, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        if (!overrideFullTimeSetting(component, calendar)) {
            calendar.setEndDate(parseDateConsideringDateType(component, new DtEnd(), timeZone));
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
        DtEnd dtEnd = new DtEnd();
        Property msAllDay = component.getProperty(XMicrosoftCdoAlldayEvent.property);
        if (msAllDay != null && msAllDay.getValue().equalsIgnoreCase("true")) {
            DateProperty dateProperty = (DateProperty)component.getProperty(dtEnd.getName());
            if (ParserTools.isDateOrMidnight(dateProperty)) {
                int timeZoneOffset = dateProperty.getTimeZone() != null ? dateProperty.getTimeZone().getOffset(dateProperty.getDate().getTime()) : 0;
                Date e = new Date(dateProperty.getDate().getTime() + timeZoneOffset);
                calendar.setEndDate(e);
                if (calendar instanceof Appointment) {
                    Appointment appointment = (Appointment) calendar;
                    appointment.setFullTime(true);
                }
                return true;
            }
        }
        return false;
    }

}
