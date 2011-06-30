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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.calendar;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.container.CalendarObject;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class RecurrenceChecker {

    public static void check(CalendarObject cdao) throws OXCalendarException {
        if (!containsRecurrenceInformation(cdao))
            return;

        if (!cdao.containsRecurrenceType())
            throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_TYPE);

        if (cdao.getRecurrenceType() == CalendarObject.NO_RECURRENCE) {
            checkNo(cdao);
            return;
        }

        if (!cdao.containsInterval())
            throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_INTERVAL);

        if (cdao.containsUntil() && cdao.containsOccurrence() && !(cdao.getUntil() == null && cdao.getOccurrence() == 0))
            throw new OXCalendarException(OXCalendarException.Code.REDUNDANT_UNTIL_OCCURRENCES);

        if (cdao.getRecurrenceType() == CalendarObject.DAILY)
            checkDaily(cdao);

        if (cdao.getRecurrenceType() == CalendarObject.WEEKLY)
            checkWeekly(cdao);

        if (cdao.getRecurrenceType() == CalendarObject.MONTHLY)
            checkMonthly(cdao);

        if (cdao.getRecurrenceType() == CalendarObject.YEARLY)
            checkYearly(cdao);
    }

    private static void checkNo(CalendarObject cdao) throws OXCalendarException {
        if (containsRecurrenceInformation(cdao, CalendarObject.RECURRENCE_TYPE))
            throw new OXCalendarException(OXCalendarException.Code.UNNECESSARY_RECURRENCE_INFORMATION_NO);
    }

    private static void checkDaily(CalendarObject cdao) throws OXCalendarException {
        if (cdao.containsDays())
            throw unnecessary("days", "daily");

        if (cdao.containsDayInMonth())
            throw unnecessary("dayInMonth", "daily");

        if (cdao.containsMonth())
            throw unnecessary("month", "daily");
    }

    private static void checkWeekly(CalendarObject cdao) throws OXCalendarException {
        if (cdao.containsDayInMonth())
            throw unnecessary("dayInMonth", "weekly");

        if (cdao.containsMonth())
            throw unnecessary("month", "weekly");

        if (!cdao.containsDays())
            throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_WEEKDAY);
        
        if (cdao.getDays() < 1 || cdao.getDays() > 127)
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_DAYS, cdao.getDays());
    }

    private static void checkMonthly(CalendarObject cdao) throws OXCalendarException {
        if (cdao.containsMonth())
            throw unnecessary("month", "monthly");

        if (!cdao.containsDayInMonth())
            throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_MONTHDAY);

        if (!cdao.containsDays())
            checkMonthly1(cdao);
        else
            checkMonthly2(cdao);
    }

    private static void checkMonthly1(CalendarObject cdao) throws OXCalendarException {
        if (cdao.containsDays())
            throw unnecessary("days", "monthly1");

        if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 31)
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL, cdao.getDayInMonth());
    }

    private static void checkMonthly2(CalendarObject cdao) throws OXCalendarException {
        if (!cdao.containsDays())
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_DAYS);
        
        if (cdao.getDays() < 1 || cdao.getDays() > 127)
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_DAYS, cdao.getDays());

        if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 5)
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_DAY_2, cdao.getDayInMonth());
    }

    private static void checkYearly(CalendarObject cdao) throws OXCalendarException {
        if (!cdao.containsDayInMonth())
            throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_MONTHDAY);

        if (!cdao.containsMonth())
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_MONTH, I(-1));

        if (!cdao.containsDays())
            checkYearly1(cdao);
        else
            checkYearly2(cdao);
    }

    private static void checkYearly1(CalendarObject cdao) throws OXCalendarException {
        if (cdao.containsDays())
            throw unnecessary("days", "yearly1");

        if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 31)
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_INTERVAL, cdao.getDayInMonth());
    }

    private static void checkYearly2(CalendarObject cdao) throws OXCalendarException {
        if (!cdao.containsDays())
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_DAYS);
        
        if (cdao.getDays() < 1 || cdao.getDays() > 127)
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_DAYS, cdao.getDays());

        if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 5)
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_INTERVAL, cdao.getDayInMonth());

    }

    /**
     * Checks, if the given CalendarDataObject contains any recurrence information.
     * 
     * @param cdao
     * @param exceptions Recurrence fields, which will not be checked
     * @return
     */
    public static boolean containsRecurrenceInformation(CalendarObject cdao, int... exceptions) {
        Set<Integer> recurrenceFields = new HashSet<Integer>() {

            {
                add(CalendarObject.INTERVAL);
                add(CalendarObject.DAYS);
                add(CalendarObject.DAY_IN_MONTH);
                add(CalendarObject.MONTH);
                add(CalendarObject.RECURRENCE_COUNT);
                add(CalendarObject.UNTIL);
            }
        };

        boolean skipType = false;
        
        for (int exception : exceptions) {
            recurrenceFields.remove(exception);
            if (exception == CalendarObject.RECURRENCE_TYPE)
                skipType = true;
        }

        if (!skipType && cdao.contains(CalendarObject.RECURRENCE_TYPE) && (Integer)cdao.get(CalendarObject.RECURRENCE_TYPE) != CalendarObject.NO_RECURRENCE)
            return true;
        
        for (int recurrenceField : recurrenceFields) {
            if (cdao.contains(recurrenceField))
                return true;
        }

        return false;
    }

    public static boolean containsOneOfFields(CalendarObject cdao, int... fields) {
        for (int field : fields) {
            if (cdao.contains(field))
                return true;
        }
        return false;
    }

    private static OXCalendarException unnecessary(String field, String type) {
        return new OXCalendarException(OXCalendarException.Code.UNNECESSARY_RECURRENCE_INFORMATION, field, type);
    }

}
