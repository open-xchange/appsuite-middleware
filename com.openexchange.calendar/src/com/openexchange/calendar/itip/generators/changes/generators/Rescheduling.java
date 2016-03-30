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

package com.openexchange.calendar.itip.generators.changes.generators;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.generators.ArgumentType;
import com.openexchange.calendar.itip.generators.Sentence;
import com.openexchange.calendar.itip.generators.changes.ChangeDescriptionGenerator;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link Rescheduling}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Rescheduling implements ChangeDescriptionGenerator {
    //TODO: Series Description

    private static enum Format {
        SAME_DAY, DIFFERENT_DAYS
    }

    private final String[] FIELDS = new String[] { "start_date", "end_date" };

    @Override
    public List<Sentence> getDescriptions(Context ctx, Appointment original, Appointment updated, AppointmentDiff diff, Locale locale, TimeZone timezone) {
        String msg = Messages.HAS_RESCHEDULED;

        return Arrays.asList(
            new Sentence(msg)
            .add(timeString(original, diff, locale, timezone), ArgumentType.ORIGINAL)
            .add(updatedTimeString(updated, diff, locale, timezone), ArgumentType.UPDATED)
        );
    }

    private String timeString(Appointment appointment, AppointmentDiff diff, Locale locale, TimeZone timezone) {
        Format format = chooseFormat(diff, timezone);
        if (differentDays(appointment.getStartDate(), appointment.getEndDate(), timezone)) {
        	format = Format.DIFFERENT_DAYS;
        }
        return time(format, appointment, locale, timezone);
    }

    private String updatedTimeString(Appointment appointment, AppointmentDiff diff, Locale locale, TimeZone timezone) {
        Format format = chooseFormat(diff, timezone);
        if (differentDays(appointment.getStartDate(), appointment.getEndDate(), timezone)) {
        	format = Format.DIFFERENT_DAYS;
        }
        return updatedTime(format, appointment, locale, timezone);
    }

    private String updatedTime(Format format, Appointment updated, Locale locale, TimeZone timezone) {
        Date startDate = updated.getStartDate();
		Date endDate = updated.getEndDate();

		DateFormat longDate = DateFormat.getDateInstance(DateFormat.LONG, locale);
        longDate.setTimeZone(timezone);
        if (updated.getFullTime()) {
        	longDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        	endDate = forceCorrectDay(endDate);
        }

        DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        time.setTimeZone(timezone);
		switch (format) {
        case SAME_DAY:
        	if (updated.getFullTime()) {
        		return String.format("%s (%s)", longDate.format(startDate), Messages.FULL_TIME);
        	} else {
                return String.format("%s - %s", time.format(startDate), time.format(endDate));
        	}
        case DIFFERENT_DAYS:
        	if (updated.getFullTime()) {
        		return String.format("%s - %s (%s)", longDate.format(startDate), longDate.format(endDate), new Sentence(Messages.FULL_TIME).getMessage(locale));
        	} else {
        		return String.format("%s - %s", longDate.format(startDate) + " " +time.format(startDate), longDate.format(endDate) + " " + time.format(endDate));
        	}
        }
        return ""; // Won't happen
    }

    private String time(Format format, Appointment original, Locale locale, TimeZone timezone) {
        Date startDate = original.getStartDate();
		Date endDate = original.getEndDate();

		DateFormat longDate = DateFormat.getDateInstance(DateFormat.LONG, locale);
        if (original.getFullTime()) {
            longDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        	endDate = forceCorrectDay(endDate);
        } else {
            longDate.setTimeZone(timezone);
        }

        DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        time.setTimeZone(timezone);

		switch (format) {
        case SAME_DAY:
        	if (original.getFullTime()) {
        		return String.format("%s (%s)", longDate.format(startDate), Messages.FULL_TIME);
        	} else {
                return String.format("%s - %s", longDate.format(startDate) + " " +time.format(startDate), time.format(endDate));
        	}
        case DIFFERENT_DAYS:
        	if (original.getFullTime()) {
        		return String.format("%s - %s (%s)", longDate.format(startDate), longDate.format(endDate), new Sentence(Messages.FULL_TIME).getMessage(locale));
        	} else {
            	return String.format("%s - %s", longDate.format(startDate) + " " +time.format(startDate), longDate.format(endDate) + " " + time.format(endDate));
        	}
        }
        return ""; // Won't happen
    }

    private Date forceCorrectDay(Date endDate) {
    	return new Date(endDate.getTime()-1000); // Move this before midnight, so the time formatting routines don't lie
	}

	private Format chooseFormat(AppointmentDiff diff, TimeZone timezone) {

        FieldUpdate update = diff.getUpdateFor("start_date");
        if (update != null) {
            if (differentDays(update.getOriginalValue(), update.getNewValue(), timezone)) {
                return Format.DIFFERENT_DAYS;
            }
        }

        update = diff.getUpdateFor("end_date");

        if (update != null) {
            if (differentDays(update.getOriginalValue(), update.getNewValue(), timezone)) {
                return Format.DIFFERENT_DAYS;
            }
        }

        return Format.SAME_DAY;
    }

    private boolean differentDays(Object originalValue, Object newValue, TimeZone timezone) {
        Date o = (Date) originalValue;
        Date n = (Date) newValue;
        GregorianCalendar cal1 = new GregorianCalendar();
        cal1.setTimeZone(timezone);
        cal1.setTime(o);

        GregorianCalendar cal2 = new GregorianCalendar();
        cal2.setTimeZone(timezone);
        cal2.setTime(n);

        return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) || cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR);
    }


    @Override
    public String[] getFields() {
        return FIELDS;
    }

}
