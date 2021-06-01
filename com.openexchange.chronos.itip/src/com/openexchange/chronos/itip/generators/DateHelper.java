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

package com.openexchange.chronos.itip.generators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.HumanReadableRecurrences;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.regional.RegionalSettingsUtil;

/**
 * {@link DateHelper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DateHelper {

    private DateFormat timeFormat;
    private DateFormat dateFormat;
    private DateFormat weekdayFormat;

    private final Event event;
    private Locale locale;
    private TimeZone timezone;
    private final TimeZone utc = TimeZone.getTimeZone("UTC");

    public DateHelper(Event event, Locale locale, TimeZone tz, RegionalSettings regionalSettings) {
        super();
        this.event = event;
        this.locale = selectLocale(locale);
        this.timezone = selectTimezone(event, tz);

        timeFormat = RegionalSettingsUtil.getTimeFormat(regionalSettings, DateFormat.SHORT, locale);
        dateFormat = RegionalSettingsUtil.getDateFormat(regionalSettings, DateFormat.FULL, locale);
        weekdayFormat = new SimpleDateFormat("E", locale);

        timeFormat.setTimeZone(timezone);
        dateFormat.setTimeZone(timezone);
        weekdayFormat.setTimeZone(timezone);
    }

    public DateHelper(Event event, Locale locale, TimeZone tz, DateFormat df, int contextId, int userId) {
        super();
        this.event = event;
        this.locale = selectLocale(locale);
        this.timezone = selectTimezone(event, tz);

        RegionalSettingsService regionalSettingsService = Services.getService(RegionalSettingsService.class);
        if (null != regionalSettingsService && contextId > 0 && userId > 0) {
            timeFormat = regionalSettingsService.getTimeFormat(contextId, userId, locale, DateFormat.SHORT);
            dateFormat = regionalSettingsService.getDateFormat(contextId, userId, locale, DateFormat.FULL);
        } else {
            timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
            dateFormat = null == df ? DateFormat.getDateInstance(DateFormat.FULL, locale) : df;
        }
        weekdayFormat = new SimpleDateFormat("E", locale);

        timeFormat.setTimeZone(timezone);
        dateFormat.setTimeZone(timezone);
        weekdayFormat.setTimeZone(timezone);
    }

    public String getRecurrenceDatePosition() {
        return formatDate(new Date(event.getRecurrenceId().getValue().getTimestamp()));
    }

    public String getInterval() {
        return formatInterval(event);
    }

    public String getDateSpec() {
        StringBuilder b = new StringBuilder();
        b.append(formatDate(event));
        if (CalendarUtils.isSeriesMaster(event)) {
            b.append(" - ").append(formatRecurrenceRule(event));
        }
        return b.toString();
    }

    public String formatRecurrenceRule(Event event) {
        if (CalendarUtils.isSeriesMaster(event)) {
            HumanReadableRecurrences recurInfo = new HumanReadableRecurrences(event, locale);
            return recurInfo.getString() + ", " + recurInfo.getEnd(dateFormat);
        }
        return "";
    }

    public String formatDate(Event event) {
        Date startDate = new Date(event.getStartDate().getTimestamp());
        Date endDate = new Date(event.getEndDate().getTimestamp());
        if (event.getStartDate().isAllDay()) {
            endDate = new Date(endDate.getTime() - 1000);
        }

        if (differentDays(startDate, endDate)) {
            if (event.getStartDate().isAllDay()) {
                return String.format("%s - %s", formatDate(startDate, utc), formatDate(endDate, utc));
            }
            return String.format("%s - %s", formatDate(startDate), formatDate(endDate));

        }
        return formatDate(startDate);
    }

    public String formatDate(Date date) {
        if (date == null || dateFormat == null) {
            return "";
        }
        return dateFormat.format(date);
    }

    public String formatDate(Date date, TimeZone timezone) {
        if (date == null || dateFormat == null) {
            return "";
        }
        DateFormat format = (DateFormat) dateFormat.clone();
        format.setTimeZone(timezone);
        return format.format(date);
    }

    public String formatTime(Date date) {
        if (date == null || timeFormat == null) {
            return "";
        }
        return timeFormat.format(date);
    }

    public String formatInterval(Event event) {
        if (event.getStartDate().isAllDay()) {
            return new Sentence(Messages.FULL_TIME).getMessage(locale);
        }
        // TODO: Longer than a day
        Date startDate = new Date(event.getStartDate().getTimestamp());
        Date endDate = new Date(event.getEndDate().getTimestamp());

        if (differentDays(startDate, endDate)) {
            if (differentWeeks(startDate, endDate)) {
                return formatTimeAndDay(startDate) + " - " + formatTimeAndDay(endDate);
            }
            return formatTimeAndWeekday(startDate) + " - " + formatTimeAndWeekday(endDate);

        }
        return formatTime(startDate) + " - " + formatTime(endDate);
    }

    private boolean differentDays(Date startDate, Date endDate) {
        GregorianCalendar cal1 = new GregorianCalendar();
        cal1.setTime(startDate);
        cal1.setTimeZone(timezone);

        GregorianCalendar cal2 = new GregorianCalendar();
        cal2.setTime(endDate);
        cal2.setTimeZone(timezone);

        return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) || cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean differentWeeks(Date startDate, Date endDate) {
        GregorianCalendar cal1 = new GregorianCalendar();
        cal1.setTimeZone(timezone);
        cal1.setTime((new Date(startDate.getTime())));

        GregorianCalendar cal2 = new GregorianCalendar();
        cal2.setTimeZone(timezone);
        cal2.setTime(new Date(endDate.getTime()));

        return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) || cal1.get(Calendar.WEEK_OF_YEAR) != cal2.get(Calendar.WEEK_OF_YEAR);
    }

    private String formatTimeAndDay(Date date) {
        return String.format("%s, %s", formatDate(date), formatTime(date));
    }

    private String formatTimeAndWeekday(Date date) {
        return String.format("%s, %s", weekdayFormat.format(date), formatTime(date));
    }

    public String getCreated() {
        Date date = event.getCreated();
        return formatDate(date) + " " + formatTime(date);
    }

    public String getModified() {
        Date date = event.getLastModified();
        return formatDate(date) + " " + formatTime(date);
    }

    private static Locale selectLocale(Locale locale) {
        if (null == locale) {
            return LocaleTools.DEFAULT_LOCALE;
        }
        return locale;
    }

    private static TimeZone selectTimezone(Event event, TimeZone tz) {
        if (null != tz) {
            return tz;
        }
        if (null != event) {
            DateTime startDate = event.getStartDate();
            if (null != startDate && false == startDate.isFloating()) {
                return startDate.getTimeZone();
            }
        }
        return TimeZone.getDefault();
    }

}
