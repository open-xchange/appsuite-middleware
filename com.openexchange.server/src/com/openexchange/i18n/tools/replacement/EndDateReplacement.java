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
