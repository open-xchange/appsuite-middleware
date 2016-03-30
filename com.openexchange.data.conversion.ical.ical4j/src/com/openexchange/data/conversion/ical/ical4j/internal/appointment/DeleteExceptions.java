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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ExDate;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class DeleteExceptions extends AbstractVerifyingAttributeConverter<VEvent, Appointment> {

    public DeleteExceptions() {
        super();
    }

    @Override
    public boolean isSet(final Appointment appointment) {
        return appointment.isMaster() && appointment.containsDeleteExceptions();
    }

    @Override
    public void emit(final Mode mode, final int index, final Appointment appointment, final VEvent vEvent, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        final java.util.Date[] dates = appointment.getDeleteException();
        if (null == dates) {
            return;
        }
        // Only when the DateList is created this way the correct dates are written to iCal file.
        for (final java.util.Date deleteException : dates) {
            final DateList deleteExceptions = new DateList();
            deleteExceptions.setUtc(true);
            final net.fortuna.ical4j.model.Date date;
            if (CalendarDataObject.class.isAssignableFrom(appointment.getClass())) {
                final CalendarDataObject cloned = (CalendarDataObject) appointment.clone();
                date = EmitterTools.toDateTime(EmitterTools.calculateExactTime(cloned, deleteException));
            } else {
                date = EmitterTools.toDateTime(deleteException);
            }
            deleteExceptions.add(date);
            vEvent.getProperties().add(new ExDate(deleteExceptions));
        }
    }

    @Override
    public boolean hasProperty(final VEvent vEvent) {
        return null != vEvent.getProperty("EXDATE");
    }

    @Override
    public void parse(final int index, final VEvent vEvent, final Appointment appointment, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        final PropertyList exdates = vEvent.getProperties("EXDATE");
        final int size = exdates.size();
        for (int i = 0; i < size; i++) {
            final ExDate exdate = (ExDate) exdates.get(i);

            final DateList dates = exdate.getDates();
            final int size2 = dates.size();
            for (int j = 0; j < size2; j++) {
                final net.fortuna.ical4j.model.Date icaldate = (net.fortuna.ical4j.model.Date) dates.get(j);
                final java.util.Date date = ParserTools.recalculateAsNeeded(icaldate, exdate, timeZone);
                appointment.addDeleteException(normalize(date));
            }
        }
    }

	private Date normalize(Date date) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
