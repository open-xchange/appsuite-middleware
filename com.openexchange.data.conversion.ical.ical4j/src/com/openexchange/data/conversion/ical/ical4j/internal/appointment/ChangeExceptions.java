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

package com.openexchange.data.conversion.ical.ical4j.internal.appointment;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RecurrenceId;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;

/**
 * Writes the recurrence identifier into change exception appointments to get the series not displayed at the change exceptions original
 * time stamp.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ChangeExceptions extends AbstractVerifyingAttributeConverter<VEvent, Appointment> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ChangeExceptions.class);
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static volatile CalendarCollectionService calendarCollection;

    public ChangeExceptions() {
        super();
    }

    @Override
    public boolean isSet(final Appointment appointment) {
        return appointment.isException();
    }

    @Override
    public void emit(final Mode mode, final int index, final Appointment appointment, final VEvent vEvent, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        final java.util.Date changeException = appointment.getRecurrenceDatePosition();
        if (null == changeException) {
            return;
        }
        net.fortuna.ical4j.model.Date date;
        if (CalendarDataObject.class.isAssignableFrom(appointment.getClass())) {
            final CalendarDataObject cloned = (CalendarDataObject) appointment.clone();
            try {
                calendarCollection.fillDAO(cloned);
                String recurrence = cloned.getRecurrence();
                if (recurrence == null) {
                	date = EmitterTools.toDate(changeException);
                } else {
    				cloned.setStartDate(parseSeriesStart(recurrence));
                    date = EmitterTools.toDateTime(EmitterTools.calculateExactTime(cloned, changeException));
                }
            } catch (final OXException e) {
                LOG.warn("", e);
                date = EmitterTools.toDate(changeException);
            }
        } else {
            date = EmitterTools.toDate(changeException);
        }
        final RecurrenceId recurrenceId = new RecurrenceId(date);
        vEvent.getProperties().add(recurrenceId);
    }

    private static final Pattern startDatePattern = Pattern.compile("s\\|(\\d*)\\|");

    private java.util.Date parseSeriesStart(final String recurrenceString) throws OXException {
    	if (recurrenceString == null)  {
    		return null;
    	}
        final Matcher matcher = startDatePattern.matcher(recurrenceString);
        final long start;
        if (matcher.find()) {
            try {
                start = Long.parseLong(matcher.group(1));
            } catch (final NumberFormatException e) {
                throw OXCalendarExceptionCodes.RECURRING_MISSING_START_DATE.create(e);
            }
        } else {
            throw OXCalendarExceptionCodes.RECURRING_MISSING_START_DATE.create();
        }
        return new java.util.Date(start);
    }

    @Override
    public boolean hasProperty(final VEvent vEvent) {
        return vEvent.getRecurrenceId() != null;
    }

    @Override
    public void parse(final int index, final VEvent vEvent, final Appointment appointment, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        RecurrenceId property = vEvent.getRecurrenceId();
        java.util.Date date = ParserTools.recalculateAsNeeded(property.getDate(), property, timeZone);
        java.util.Date recurrencePosition = normalize(date);
        appointment.setRecurrenceDatePosition(recurrencePosition);
    }

    private java.util.Date normalize(java.util.Date date) {
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * TODO Refactor this to add {@link ChangeExceptions} dynamically depending on service in AppointmentConverters.
     */
    public static void setCalendarCollection(final CalendarCollectionService calendarCollection) {
        ChangeExceptions.calendarCollection = calendarCollection;
    }
}
