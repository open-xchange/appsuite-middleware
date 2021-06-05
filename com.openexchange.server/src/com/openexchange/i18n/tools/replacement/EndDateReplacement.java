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

package com.openexchange.i18n.tools.replacement;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.i18n.tools.TemplateToken;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link EndDateReplacement} - End date replacement
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EndDateReplacement extends AbstractFormatDateReplacement {

    /**
     * Initializes a new {@link EndDateReplacement}
     *
     * @param endDate The end date
     * @param fulltime <code>true</code> if given end date denotes a full-time end date; otherwise <code>false</code>
     * @param isTask <code>true</code> if this end date denotes a task's end date; otherwise <code>false</code>
     */
    public EndDateReplacement(final Date endDate, final boolean fulltime, final boolean isTask) {
        this(endDate, fulltime, isTask, null, null);
    }

    /**
     * Initializes a new {@link EndDateReplacement}
     *
     * @param endDate The end date The end date
     * @param fulltime <code>true</code> if given end date denotes a full-time end date; otherwise <code>false</code>
     * @param isTask <code>true</code> if this end date denotes a task's end date; otherwise <code>false</code>
     * @param locale The locale
     * @param timeZone The time zone
     */
    public EndDateReplacement(final Date endDate, final boolean fulltime, final boolean isTask, final Locale locale, final TimeZone timeZone) {
        super(
            correctDayOfMonth(endDate, fulltime, isTask),
            !fulltime,
            isTask ? Notifications.FORMAT_DUE_DATE : Notifications.FORMAT_END_DATE,
            locale,
            timeZone);
        fallback = isTask ? Notifications.NO_DUE_DATE : Notifications.NO_END_DATE;
    }

    /*
     * This is worth some discussion. Appointments lasting the entire day end at midnight (00:00 o'Clock) of *the following day*.
     * For example a fulltime appointment from the 3rd of August up until the 4th of August ends midnight on the *5th* of August.
     * This is all nice and useful for calendar calculation, but not so much for date printing. That is why, for the end date, we have
     * to move (when printing) the appointment into the previous day, if only by some milliseconds, so that the appointment ends
     * shortly before midnight the next day, in our example on the 4th of August.
     */
    private static Date correctDayOfMonth(final Date endDate, final boolean fulltime, final boolean isTask) {
        /*-
         * Previous implementation:
         *
         * return fulltime && null != endDate ? new Date(endDate.getTime()-1) : endDate;
         */
        if (!fulltime || null == endDate || isTask) {
            return endDate;
        }
        final Calendar calendar = Calendar.getInstance(TimeZoneUtils.getTimeZone("UTC"));
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    @Override
    public TemplateToken getToken() {
        return TemplateToken.END;
    }
}
