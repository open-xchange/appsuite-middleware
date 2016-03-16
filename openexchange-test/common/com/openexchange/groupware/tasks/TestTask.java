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

package com.openexchange.groupware.tasks;

import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.JANUARY;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.java.util.TimeZones;

/**
 * {@link TestTask}
 */
public class TestTask extends Task {

    protected TimeZone timezone;

    public static final int DATES = 1;
    public static final int RECURRENCES = 2;

    final Calendar calendar = Calendar.getInstance(TimeZones.UTC);

    private Date date = null;

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public Calendar getCalendar(){
        return calendar;
    }

    public TestTask(){
        super();

        calendar.clear();
        calendar.set(Calendar.DAY_OF_MONTH, 11);
        calendar.set(Calendar.MONTH, 2);
        calendar.set(Calendar.YEAR, 1982);
        this.date = calendar.getTime();
    }

    public TestTask checkConsistencyOf(int... switches){
        for(int currentSwitch : switches) {
            switch (currentSwitch) {
            case DATES:
                //appointments without start dates do not work
                if( ! containsStartDate() ) {
                    setStartDate(this.date);
                }
                //appointments without end dates do not work
                if( containsStartDate() && ! containsEndDate() ) {
                    setEndDate(this.getStartDate());
                }
                //appointments with end dates before start dates do not work
                if( containsStartDate() && containsEndDate() ) {
                    if( getEndDate().compareTo(getStartDate()) < 0) {
                        setEndDate( getStartDate() );
                    }
                }
                break;

            case RECURRENCES:
                if( containsRecurrenceType()
                    || containsDays()
                    || containsDayInMonth()
                    || containsInterval()
                    ){
                    //if there is a recurrence, but no interval is set, set it to 1
                    if( ! containsInterval() )
                    {
                        setInterval(1);
                        //if there is no start date, set it to the start of the recurrence
                    }

                }
            default:
                break;
            }
        }
        return this;
    }

    //set the day
    protected Date dateAtDayOfTheMonth(Date date, int dayOfTheMonth){
        Calendar time = getCalendar();
        time.setTime(date);
        time.set(Calendar.DAY_OF_MONTH, dayOfTheMonth);
        return time.getTime();
    }

    protected Date shiftDateByDays(Date date, int daysToShift){
        Calendar time = getCalendar();
        time.setTime(date);
        time.add(Calendar.DAY_OF_MONTH, daysToShift);

        return time.getTime();
    }

    protected Date setWeekDay(Date date, int weekDay) {
        Calendar time = getCalendar();
        time.setTime(date);
        time.set(Calendar.DAY_OF_WEEK, ox2CalDay(weekDay));
        return time.getTime();
    }

    protected Date setMonthDay(Date date, int monthDay) {
        Calendar time = getCalendar();
        time.setTime(date);
        time.set(Calendar.DAY_OF_MONTH, monthDay);
        return time.getTime();
    }

    protected Date setWeekOfMonth(Date date, int week) {
        Calendar time = getCalendar();
        time.setTime(date);
        int wantedMonth = time.get(Calendar.MONTH);
        time.set(Calendar.WEEK_OF_MONTH, 1);
        if (time.get(Calendar.MONTH) < wantedMonth) {
            time.add(Calendar.WEEK_OF_MONTH, 1);
        }
        time.add(Calendar.WEEK_OF_MONTH, week - 1);
        return time.getTime();
    }

    public TestTask startsThisWeek(int dayOfWeek) {
        if (!containsStartDate()) {
            setStartDate(this.date);
        }
        setStartDate(setWeekDay(getStartDate(), dayOfWeek));
        return this;
    }

    public TestTask startsThisMonth(int dayOfMonth) {
        if (!containsStartDate()) {
            setStartDate(this.date);
        }
        setStartDate(setMonthDay(getStartDate(), dayOfMonth));
        return this;
    }

    public TestTask startsWeekOfMonth(int week) {
        if (!containsStartDate()) {
            setStartDate(this.date);
        }
        setStartDate(setWeekOfMonth(getStartDate(), week));
        return this;
    }

    public TestTask startsWeekOfMonthOnDay(int week, int dayOfWeek) {
        if (!containsStartDate()) {
            setStartDate(this.date);
        }
        Calendar cal = new GregorianCalendar(TimeZones.UTC);
        cal.setTime(getStartDate());
        int currentMonth = cal.get(Calendar.MONTH);
        Date setWeekOfMonth = setWeekOfMonth(getStartDate(), week);
        Date setWeekDay = setWeekDay(setWeekOfMonth, dayOfWeek);
        setStartDate(setWeekDay);
        cal.setTime(getStartDate());
        int otherMonth = cal.get(Calendar.MONTH);
        if (otherMonth != currentMonth) {
            // applying week of month and day of week shifted appointment to another month.
            if (otherMonth < currentMonth || (JANUARY == currentMonth && DECEMBER == otherMonth)) {
                cal.add(Calendar.WEEK_OF_MONTH, 1);
                setStartDate(cal.getTime());
            }
        }
        return this;
    }

    public TestTask endsThisWeek(int dayOfWeek) {
        if (!containsEndDate()) {
            setEndDate(this.date);
        }
        setEndDate(setWeekDay(getEndDate(), dayOfWeek));
        return this;
    }
    public TestTask startsToday(){
        this.setStartDate(this.date);
        return this;
    }

    /**
     * Moves the date one day into the future.
     * Can be used more than once.
     * @return
     */
    public TestTask startsTheFollowingDay(){
        if (!containsStartDate()) {
            setStartDate(this.date);
        }
        setStartDate(shiftDateByDays(getStartDate(), 1));
        return this;
    }

    public TestTask endsTheFollowingDay(){
        if(! containsEndDate() ) {
            setEndDate(this.date);
        }
        setEndDate( shiftDateByDays(getEndDate(), 1));
        return this;
    }

    public TestTask startsTheDayBefore(){
        if(! containsStartDate() ) {
            setStartDate(this.date);
        }
        setStartDate( shiftDateByDays(getStartDate(), -1));
        return this;
    }

    public TestTask endsTheDayBefore(){
        if(! containsEndDate() ) {
            setEndDate(this.date);
        }
        setEndDate( shiftDateByDays(getEndDate(), -1));
        return this;
    }

    protected Date setDayToToday(Date date){
        Calendar now = getCalendar();

        Calendar newDate = getCalendar();
        newDate.setTime(date);

        newDate.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
        newDate.set(Calendar.YEAR, now.get(Calendar.YEAR));
        newDate.set(Calendar.MONTH, now.get(Calendar.MONTH));

        return newDate.getTime();
    }
    /**
     * sets the task to start tomorrow. The time of day is
     * either that set before or, if none was set, it is the
     * current time of day.
     * @return
     */
    public TestTask startsTomorrow(){
        if (!containsStartDate()) {
            setStartDate(this.date);
        }
        setStartDate(setDayToToday(getStartDate()));
        return startsTheFollowingDay();
    }

    public TestTask endsTomorrow(){
        if(! containsEndDate() ) {
            setEndDate(this.date);
        }
        setEndDate( setDayToToday( getEndDate() ) );
        return endsTheFollowingDay();
    }

    public TestTask startsYesterday(){
        if(! containsStartDate() ) {
            setStartDate(this.date);
        }
        setStartDate( setDayToToday( getStartDate() ) );
        return startsTheDayBefore();
    }

    public TestTask endsYesterday(){
        if(! containsEndDate() ) {
            setEndDate(this.date);
        }
        setEndDate( setDayToToday( getEndDate() ) );
        return endsTheDayBefore();
    }

    //set the hour
    protected Date dateAtHours(Date date, int hourOfTheDay) {
        Calendar time = getCalendar();
        time.setTime(date);
        time.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
        time.set(Calendar.MINUTE, 00);
        return time.getTime();
    }

    public TestTask startsAt(int hourOfTheDay) {
        if (!containsStartDate()) {
            setStartDate(this.date);
        }
        setStartDate(dateAtHours(getStartDate(), hourOfTheDay));
        return this;
    }

    public TestTask endsAt(int hourOfTheDay) {
        if (!containsEndDate()) {
            setEndDate(this.date);
        }
        setEndDate(dateAtHours(getEndDate(), hourOfTheDay));
        return this;
    }

    public TestTask startsAtNoon(){
        return startsAt(12);
    }

    public TestTask startsInTheMorning(){
        return startsAt(6);
    }

    public TestTask startsInTheEvening(){
        return startsAt(18);
    }

    public TestTask endsInTheMorning(){
        return endsAt(6);
    }

    public TestTask endsAtNoon(){
        return endsAt(12);
    }

    public TestTask endsInTheEvening() {
        return endsAt(18);
    }

    public TestTask forOneHour(){
        return this;
    }

    //set recurrence
    public TestTask everyDay() {
        if(! containsStartDate()){
            setStartDate(this.date);
        }
        setInterval(1);
        return this;
    }

    public TestTask everyWeek(){
        if (!containsStartDate()) {
            setStartDate(this.date);
        }
        Calendar cal = getCalendar();
        int dayOfTheWeek = cal2OXDay(cal.get(Calendar.DAY_OF_WEEK));
        return everyWeekOn(dayOfTheWeek);
    }

    private int cal2OXDay(int calDayOfWeek) {
        return 1 << (calDayOfWeek - 1);
    }

    private int ox2CalDay(int oxDayOfWeek) {
        switch (oxDayOfWeek) {
        case SUNDAY:
            return Calendar.SUNDAY;
        case MONDAY:
            return Calendar.MONDAY;
        case TUESDAY:
            return Calendar.TUESDAY;
        case WEDNESDAY:
            return Calendar.WEDNESDAY;
        case THURSDAY:
            return Calendar.THURSDAY;
        case FRIDAY:
            return Calendar.FRIDAY;
        case SATURDAY:
            return Calendar.SATURDAY;
        }
        throw new IllegalArgumentException();
    }

    public TestTask everyWeekOn(int... days) {
        //TODO should really start on next day in dayOfTheWeek
        if (!containsStartDate()) {
            setStartDate(this.date);
        }
        int daysOfTheWeek = 0;
        for (int day : days) {
            daysOfTheWeek |= day;
        }
        setRecurrenceType(WEEKLY);
        setInterval(1);
        setDays(daysOfTheWeek);
        return this;
    }

    public TestTask onDay(int day){
        setDayInMonth(day);
        return this;
    }

    public TestTask onWeekDays(int days){
        setDays(days);
        return this;
    }

    public TestTask inMonth(int month){
        setMonth(month);
        return this;
    }

    public TestTask everyMonth(){
        setRecurrenceType(MONTHLY);
        return this;
    }

    public TestTask everyMonthOnDay(int dayOfMonth){
        //this is ugly but compatible to the HTTP_API description as of 2008-11-20
        return everyMonthOnNthWeekday(dayOfMonth, MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY | SATURDAY | SUNDAY);
    }

    public TestTask everyMonthOnNthWeekday(int n, int weekDay){
        everyMonth();
        onDay(n);
        onWeekDays(weekDay);
        if(! containsInterval()){
            setInterval(1);
        }
        return this;
    }

    public TestTask everyOtherMonth(){
        setRecurrenceType(MONTHLY);
        setInterval(2);
        return this;
    }
    public TestTask everyYear(){
        return this;
    }

    public TestTask occurs(final int occurrences) {
        setOccurrence(occurrences);
        return this;
    }

    public TestTask relatedTo(TestTask originalTask) {
        return makeRelatedTo(originalTask);
    }

    public TestTask makeRelatedTo(TestTask originalTask) {
        this.setLastModified(originalTask.getLastModified());
        this.setObjectID(originalTask.getObjectID());
        this.setParentFolderID(originalTask.getParentFolderID());
        return this;
    }
    @Override
    public TestTask clone(){
        TestTask newTask = new TestTask();
        //copy all fields of Task
        for(int field : Task.ALL_COLUMNS){
            Mapper mapper = Mapping.getMapping(field);
            if(mapper != null &&  mapper.isSet(this) ) {
                mapper.set(newTask, mapper.get(this) );
            }
        }
        //copy additional TestTask fields
        newTask.setTimezone(getTimezone());
        //TODO: deep-copy participant lists

        return newTask;
    }
}
