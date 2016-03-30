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
package com.openexchange.data.conversion.ical;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICALFixtures {

    private final SimpleDateFormat dateTime = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    private final SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");


    public ICALFixtures() {
        dateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public String veventWithLocalDTStartAndDTEnd(final Date start, final Date end) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        localDTStartAndDTEnd(bob, start, end);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    private void localDTStartAndDTEnd(final StringBuilder bob, final Date start, final Date end) {
        bob.append("DTSTART:").append(dateTime.format(start)).append('\n');
        bob.append("DTEND:").append(dateTime.format(end)).append('\n');
    }

    private void localDTStartAndDue(final StringBuilder bob, final Date start, final Date end) {
        bob.append("DTSTART:").append(dateTime.format(start)).append('\n');
        bob.append("DUE:").append(dateTime.format(end)).append('\n');
    }

    public String veventWithUTCDTStartAndDTEnd(final Date start, final Date end) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        utcDTStartAndDTEnd(bob, start, end);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    private void utcDTStartAndDTEnd(final StringBuilder bob, final Date start, final Date end) {
        bob.append("DTSTART:").append(dateTime.format(start)).append("Z\n");
        bob.append("DTEND:").append(dateTime.format(end)).append("Z\n");
    }

    private void utcDTStartAndDue(final StringBuilder bob, final Date start, final Date end) {
        bob.append("DTSTART:").append(dateTime.format(start)).append("Z\n");
        bob.append("DUE:").append(dateTime.format(end)).append("Z\n");
    }


    public String veventWithDTStartAndEndInTimeZone(final Date start, final Date end, final TimeZone timeZone) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        timezoneDTStartAndDTEnd(bob, start, end, timeZone.getID());

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String veventWithDTStartAndEndInTimeZone(final Date start, final Date end, final String timeZone) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        timezoneDTStartAndDTEnd(bob, start, end, timeZone);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }


    private void timezoneDTStartAndDTEnd(final StringBuilder bob, final Date start, final Date end, final String timeZone) {
        bob.append("DTSTART;TZID=").append(timeZone).append(':').append(dateTime.format(start)).append('\n');
        bob.append("DTEND;TZID=").append(timeZone).append(':').append(dateTime.format(end)).append('\n');
    }

    private void timezoneDTStartAndDue(final StringBuilder bob, final Date start, final Date end, final String timeZone) {
        bob.append("DTSTART;TZID=").append(timeZone).append(':').append(dateTime.format(start)).append('\n');
        bob.append("DUE;TZID=").append(timeZone).append(':').append(dateTime.format(end)).append('\n');
    }


    public String veventWithDTStartAndDTEndInCustomTimezone(final Date start, final Date end) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);

        addTimeZone(bob);

        beginEvent(bob);

        customTimezoneDTStartAndDTEnd(bob, start, end);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    private void customTimezoneDTStartAndDTEnd(final StringBuilder bob, final Date start, final Date end) {
        bob.append("DTSTART;TZID=").append("/custom/Japan").append(':').append(dateTime.format(start)).append('\n');
        bob.append("DTEND;TZID=").append("/custom/Japan").append(':').append(dateTime.format(end)).append('\n');
    }

    private void customTimezoneDTStartAndDue(final StringBuilder bob, final Date start, final Date end) {
        bob.append("DTSTART;TZID=").append("/custom/Japan").append(':').append(dateTime.format(start)).append('\n');
        bob.append("DUE;TZID=").append("/custom/Japan").append(':').append(dateTime.format(end)).append('\n');
    }


    public String veventWithLocalDTStartAndDuration(final Date start, final String duration) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        bob.append("DTSTART:").append(dateTime.format(start)).append('\n');
        bob.append("DURATION:").append(duration).append('\n');

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String veventWithSimpleProperties(final Date start, final Date end, final String...properties) {
        final StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        for(int i = 0; i < properties.length; i++) {
            final String name = properties[i++];
            final String value = properties[i];
            bob.append(name).append(':').append(value).append('\n');
        }

        endStandardAppFields(bob);
        return bob.toString();
    }

    public String veventWithAttendees(final Date start, final Date end, final String[] mails) {
        final StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        for(final String mail : mails) {
            bob.append("ATTENDEE:MAILTO:").append(mail).append('\n');
        }

        endStandardAppFields(bob);
        return bob.toString();
    }

    public String veventWithResources(final Date start, final Date end, final String[] resources) {
        final StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);
        bob.append("RESOURCES:");
        for(final String resource : resources) {
           bob.append(resource).append(',');
        }
        bob.setCharAt(bob.length()-1, '\n');

        endStandardAppFields(bob);
        return bob.toString();
    }



    public String veventWithResourcesInAttendees(final Date start, final Date end, final String[] resources) {
        final StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        for(final String cn : resources) {
            bob.append("ATTENDEE;CUTYPE=RESOURCE;CN=").append(cn).append(":MAILTO:ignored@bla.invalid\n");
        }

        endStandardAppFields(bob);
        return bob.toString();
    }

    public String veventWithCategories(final Date start, final Date end, final String[] categories) {
        final StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);
        bob.append("CATEGORIES:");
        for(final String category : categories) {
            bob.append(category).append(',');
        }
        bob.setCharAt(bob.length()-1, '\n');

        endStandardAppFields(bob);
        return bob.toString();
    }



    public String veventWithDeleteExceptionsAsDateTime(final Date start, final Date end, final String rrule, final Date[] exceptions) {
        final StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        bob.append("RRULE:").append(rrule).append('\n');

        bob.append("EXDATE:");

        for(final Date exception : exceptions) {
            bob.append(dateTime.format(exception)).append(',');
        }
        bob.setCharAt(bob.length()-1,'\n');

        endStandardAppFields(bob);
        return bob.toString();
    }

    public String veventWithDeleteExceptionsAsDate(final Date start, final Date end, final String rrule, final Date[] exceptions) {
        final StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        bob.append("RRULE:").append(rrule).append('\n');

        bob.append("EXDATE;VALUE=DATE:");

        for(final Date exception : exceptions) {
            bob.append(date.format(exception)).append(',');
        }
        bob.setCharAt(bob.length()-1,'\n');

        endStandardAppFields(bob);
        return bob.toString();
    }


    public String veventWithDisplayAlarm(final Date start, final Date end, final String trigger, final String description) {
        final StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        bob.append("BEGIN:VALARM\n");
        bob.append(trigger).append('\n');
        bob.append("ACTION:DISPLAY\n");
        bob.append("DESCRIPTION:").append(description).append('\n');
        bob.append("END:VALARM\n");

        endStandardAppFields(bob);
        return bob.toString();
    }


    private void standardAppFields(final StringBuilder bob, final Date start, final Date end) {
        beginCalendar(bob);
        beginEvent(bob);

        bob.append("DTSTART:").append(dateTime.format(start)).append("Z\n");
        bob.append("DTEND:").append(dateTime.format(end)).append("Z\n");

    }

    private void endStandardAppFields(final StringBuilder bob) {
        endEvent(bob);
        endCalendar(bob);

    }

    private void addTimeZone(final StringBuilder bob) {
        bob.append("BEGIN:VTIMEZONE\n");
        bob.append("TZID:/custom/Japan\n");
        bob.append("BEGIN:STANDARD\n");
        bob.append("TZOFFSETFROM:+0900\n");
        bob.append("TZOFFSETTO:+0900\n");
        bob.append("TZNAME:JST\n");
        bob.append("DTSTART:19700101T000000\n");
        bob.append("END:STANDARD\n");
        bob.append("END:VTIMEZONE\n");
    }

    private void beginCalendar(final StringBuilder bob) {
        bob.append("BEGIN:VCALENDAR\n")
           .append("VERSION:2.0\n");
    }


    private void beginEvent(final StringBuilder bob) {
        bob.append("BEGIN:VEVENT\n");
    }

    private void endEvent(final StringBuilder bob) {
        bob.append("END:VEVENT\n");
    }

    private void endCalendar(final StringBuilder bob) {
        bob.append("END:VCALENDAR\n");
    }


    // VTODO


    private void standardTodoFields(final StringBuilder bob) {
        bob.append("BEGIN:VCALENDAR\nBEGIN:VTODO\n");
    }

    private void endStandardTodoFields(final StringBuilder bob) {
        bob.append("END:VTODO\nEND:VCALENDAR");
    }

    private void endTodo(final StringBuilder bob) {
        bob.append("END:VTODO\n");
    }

    private void beginTodo(final StringBuilder bob) {
        bob.append("BEGIN:VTODO\n");
    }

    public String vtodoWithSimpleProperties(final String...properties) {

        final StringBuilder bob = new StringBuilder();

        standardTodoFields(bob);

        for(int i = 0; i < properties.length; i++) {
            final String name = properties[i++];
            final String value = properties[i];
            bob.append(name).append(':').append(value).append('\n');
        }

        endStandardTodoFields(bob);
        return bob.toString();
    }


    public String vtodoWithLocalDTStartAndDue(final Date start, final Date end) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        localDTStartAndDue(bob, start, end);

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithUTCDTStartAndDue(final Date start, final Date end) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDue(bob, start, end);

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDTStartAndDueInTimeZone(final Date start, final Date end, final TimeZone timeZone) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        timezoneDTStartAndDue(bob, start, end, timeZone.getID());

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDTStartAndDueInCustomTimezone(final Date start, final Date end) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);

        addTimeZone(bob);

        beginTodo(bob);

        customTimezoneDTStartAndDue(bob, start, end);

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithLocalDTStartAndDuration(final Date start, final String duration) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        bob.append("DTSTART:").append(dateTime.format(start)).append('\n');
        bob.append("DURATION:").append(duration).append('\n');

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithAttendees(final String[] mails) {
        final StringBuilder bob = new StringBuilder();
        standardTodoFields(bob);

        for(final String mail : mails) {
            bob.append("ATTENDEE:MAILTO:").append(mail).append('\n');
        }

        endStandardTodoFields(bob);
        return bob.toString();
    }


    public String vtodoWithCategories(final String[] categories) {
        final StringBuilder bob = new StringBuilder();
        standardTodoFields(bob);
        bob.append("CATEGORIES:");
        for(final String category : categories) {
            bob.append(category).append(',');
        }
        bob.setCharAt(bob.length()-1, '\n');

        endStandardTodoFields(bob);
        return bob.toString();
    }

    public String vtodoWithSimpleProperties(final Date start, final Date end, final String...properties) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDTEnd(bob, start, end);

        for(int i = 0; i < properties.length; i++) {
            final String name = properties[i++];
            final String value = properties[i];
            bob.append(name).append(':').append(value).append('\n');
        }


        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDeleteExceptionsAsDateTime(final Date start, final Date end, final String rrule, final Date[] exceptions) {
        final StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDTEnd(bob, start, end);

        bob.append("RRULE:").append(rrule).append('\n');

        bob.append("EXDATE:");

        for(final Date exception : exceptions) {
            bob.append(dateTime.format(exception)).append(',');
        }
        bob.setCharAt(bob.length()-1,'\n');

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDeleteExceptionsAsDate(final Date start, final Date end, final String rrule, final Date[] exceptions) {
        final StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDTEnd(bob, start, end);

        bob.append("RRULE:").append(rrule).append('\n');

        bob.append("EXDATE;VALUE=DATE:");

        for(final Date exception : exceptions) {
            bob.append(date.format(exception)).append(',');
        }
        bob.setCharAt(bob.length()-1,'\n');

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDisplayAlarm(final Date start, final Date end, final String trigger, final String description) {
        final StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDue(bob, start, end);

        bob.append("BEGIN:VALARM\n");
        bob.append(trigger).append('\n');
        bob.append("ACTION:DISPLAY\n");
        bob.append("DESCRIPTION:").append(description).append('\n');
        bob.append("END:VALARM\n");

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDueDate(final Date due) {
        final StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        bob.append("DUE:").append(dateTime.format(due)).append("Z\n");

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDueDateWithoutTZ(final Date due) {
        final StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        bob.append("DUE;VALUE=DATE:").append(date.format(due)).append('\n');

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDateCompleted(final Date dateCompleted) {
        final StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        bob.append("COMPLETED:").append(dateTime.format(dateCompleted)).append("Z\n");

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }


    //Error Cases

    public String veventWithEnd(final Date date) {
        return veventWithOneDate("DTEND", date);
    }


    public String veventWithStart(final Date date) {
        return veventWithOneDate("DTSTART", date);
    }

    private String veventWithOneDate(final String property, final Date date) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        bob.append(property).append(':').append(dateTime.format(date)).append('\n');

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String veventWithUnspecifiedVTimeZone(final Date start, final Date end) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);


        beginEvent(bob);

        customTimezoneDTStartAndDTEnd(bob, start, end);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String veventWithTwoRecurrences(final Date start, final Date end) {
       return veventWithSimpleProperties(start, end, "RRULE", "FREQ=DAILY;INTERVAL=1;COUNT=3", "RRULE", "FREQ=DAILY;INTERVAL=2;COUNT=6");
    }

    public String veventWithAudioAlarm(final Date start, final Date end, final String trigger, final String audioFile) {
        final StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        bob.append("BEGIN:VALARM\n");
        bob.append(trigger).append('\n');
        bob.append("ACTION:AUDIO\n");
        bob.append("ATTACH:").append(audioFile).append('\n');
        bob.append("END:VALARM\n");

        endStandardAppFields(bob);
        return bob.toString();
    }

    public String veventWithWholeDayEvent(final Date start) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        bob.append("DTSTART;VALUE=DATE:").append(date.format(start)).append('\n');

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String veventWithDTStartAsDateWithoutValue(final Date start) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        bob.append("DTSTART:").append(date.format(start)).append('\n');

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }
    
    public String severalVevents(int amount) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        for(int i = 0; i < amount; i++) {
	        beginEvent(bob);
	        bob.append("DTSTART:").append(date.format(new Date())).append('\n');
	        bob.append("SUMMARY:").append("Appointment #" + i).append('\n');
	        endEvent(bob);
        }
        endCalendar(bob);
        return bob.toString();
    }

    public String severalVtodos(int amount) {
        final StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        for(int i = 0; i < amount; i++) {
	        beginTodo(bob);
	        bob.append("SUMMARY:").append("Task #" + i).append('\n');
	        endTodo(bob);
        }
        endCalendar(bob);
        return bob.toString();
    }

}
