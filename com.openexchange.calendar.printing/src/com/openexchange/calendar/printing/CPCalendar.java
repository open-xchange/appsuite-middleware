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

package com.openexchange.calendar.printing;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link CPCalendar}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPCalendar extends GregorianCalendar {

    private static final long serialVersionUID = -8935444361292573072L;

    private final Locale locale;

    private int workWeekStartingDay;

    private int workWeekDurationInDays;

    private int workDayStartingHours;

    private int workDayDurationInMinutes;

    private List<Integer> workWeekDays;

    private final SimpleDateFormat format;

    public CPCalendar() {
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    public CPCalendar(TimeZone zone, Locale locale) {
        super(zone, locale);
        this.locale = locale;
        format = new SimpleDateFormat("", locale);
        format.setTimeZone(zone);
    }

    public static CPCalendar getCalendar(TimeZone zone, Locale locale) {
        CPCalendar cal = new CPCalendar(zone, locale);
        setWorkWeek(cal);
        return cal;
    }

    public static CPCalendar getCalendar() {
        CPCalendar cal = new CPCalendar();
        setWorkWeek(cal);
        return cal;
    }

    public Locale getLocale() {
        return locale;
    }

    public String format(String pattern, Date date) {
        format.applyPattern(pattern);
        return format.format(date);
    }

    private static void setWorkWeek(CPCalendar cal) {
        cal.setWorkWeekStartingDay(Calendar.MONDAY);
        cal.setWorkWeekDurationInDays(5);
        cal.setWorkDayStartingHours(9);
        cal.setWorkDayDurationInMinutes(8 * 60);
    }

    public int getWorkWeekStartingDay() {
        return workWeekStartingDay;
    }


    public void setWorkWeekStartingDay(int workWeekStartingDay) {
        this.workWeekStartingDay = workWeekStartingDay;
    }


    public int getWorkWeekDurationInDays() {
        return workWeekDurationInDays;
    }


    public void setWorkWeekDurationInDays(int workWeekDurationInDays) {
        this.workWeekDurationInDays = workWeekDurationInDays;
    }


    public int getWorkDayStartingHours() {
        return workDayStartingHours;
    }


    public void setWorkDayStartingHours(int workDayStartingHours) {
        this.workDayStartingHours = workDayStartingHours;
    }


    public int getWorkDayDurationInMinutes() {
        return workDayDurationInMinutes;
    }


    public void setWorkDayDurationInMinutes(int workDayDurationInMinutes) {
        this.workDayDurationInMinutes = workDayDurationInMinutes;
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getLastDayOfWeek(){
        return (getFirstDayOfWeek() == 1) ? 7 : getFirstDayOfWeek() - 1;
    }


    public List<Integer> getWorkWeekDays() {
        if (workWeekDays == null) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, getWorkWeekStartingDay());
            workWeekDays = new LinkedList<Integer>();
            for (int i = 0; i < getWorkWeekDurationInDays(); i++) {
                workWeekDays.add(Integer.valueOf(cal.get(Calendar.DAY_OF_WEEK)));
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
        }
        return workWeekDays;
    }

    public int getLastDayOfWorkWeek() {
        return getWorkWeekDays().get(getWorkWeekDays().size() - 1).intValue();
    }

    public int getLastDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getTime());
        cal.add(MONTH, 1);
        cal.set(DAY_OF_MONTH, 1);
        cal.add(DAY_OF_MONTH, -1);
        return cal.get(DAY_OF_MONTH);
    }

    public int getFirstDayOfWorkWeek() {
        return getWorkWeekDays().get(0).intValue();
    }

    public boolean isOnFirstDayOfWeek(Date day){
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return cal.get(Calendar.DAY_OF_WEEK) == getFirstDayOfWeek();
    }

    public boolean isOnLastDayOfWeek(Date day){
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return cal.get(Calendar.DAY_OF_WEEK) == getLastDayOfWeek();
    }

    public boolean isOnFirstDayOfWorkWeek(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return cal.get(Calendar.DAY_OF_WEEK) == getFirstDayOfWorkWeek();
    }

    public boolean isOnLastDayOfWorkWeek(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return cal.get(Calendar.DAY_OF_WEEK) == getLastDayOfWorkWeek();
    }
}
