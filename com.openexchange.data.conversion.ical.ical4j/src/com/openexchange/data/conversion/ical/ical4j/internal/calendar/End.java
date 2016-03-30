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

package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import static com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools.toDate;
import static com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools.toDateTime;
import static com.openexchange.data.conversion.ical.ical4j.internal.ParserTools.parseDateConsideringDateType;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;

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
        boolean retval = false;
        
        DtEnd dtEnd = new DtEnd();
        Property msAllDay = component.getProperty(XMicrosoftCdoAlldayEvent.property);
        if (msAllDay != null && msAllDay.getValue().equalsIgnoreCase("true")) {
            DateProperty dateProperty = (DateProperty)component.getProperty(dtEnd.getName());
            int timeZoneOffset = dateProperty.getTimeZone() != null ? dateProperty.getTimeZone().getOffset(dateProperty.getDate().getTime()) : 0;
            Date e = new Date(dateProperty.getDate().getTime() + timeZoneOffset);
            calendar.setEndDate(e);
            
            if (calendar instanceof Appointment) {
                Appointment appointment = (Appointment) calendar;
                appointment.setFullTime(true);
            }
            
            retval = true;
        }
        
        return retval;
    }

}
