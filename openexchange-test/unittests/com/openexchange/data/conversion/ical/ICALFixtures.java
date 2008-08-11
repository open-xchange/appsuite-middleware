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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICALFixtures {

    private SimpleDateFormat dateTime = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    private SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");


    public ICALFixtures() {
        dateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public String veventWithLocalDTStartAndDTEnd(Date start, Date end) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        localDTStartAndDTEnd(bob, start, end);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    private void localDTStartAndDTEnd(StringBuilder bob, Date start, Date end) {
        bob.append("DTSTART:").append(dateTime.format(start)).append("\n");
        bob.append("DTEND:").append(dateTime.format(end)).append("\n");
    }


    public String veventWithUTCDTStartAndDTEnd(Date start, Date end) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        utcDTStartAndDTEnd(bob, start, end);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    private void utcDTStartAndDTEnd(StringBuilder bob, Date start, Date end) {
        bob.append("DTSTART:").append(dateTime.format(start)).append("Z\n");
        bob.append("DTEND:").append(dateTime.format(end)).append("Z\n");
    }


    public String veventWithDTStartAndEndInTimeZone(Date start, Date end, TimeZone timeZone) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        timezoneDTStartAndDTEnd(bob, start, end, timeZone.getID());

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String veventWithDTStartAndEndInTimeZone(Date start, Date end, String timeZone) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        timezoneDTStartAndDTEnd(bob, start, end, timeZone);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }


    private void timezoneDTStartAndDTEnd(StringBuilder bob, Date start, Date end, String timeZone) {
        bob.append("DTSTART;TZID=").append(timeZone).append(":").append(dateTime.format(start)).append("\n");
        bob.append("DTEND;TZID=").append(timeZone).append(":").append(dateTime.format(end)).append("\n");
    }


    public String veventWithDTStartAndDTEndInCustomTimezone(Date start, Date end) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);

        addTimeZone(bob);

        beginEvent(bob);

        customTimezoneDTStartAndDTEnd(bob, start, end);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    private void customTimezoneDTStartAndDTEnd(StringBuilder bob, Date start, Date end) {
        bob.append("DTSTART;TZID=").append("/custom/Japan").append(":").append(dateTime.format(start)).append("\n");
        bob.append("DTEND;TZID=").append("/custom/Japan").append(":").append(dateTime.format(end)).append("\n");
    }


    public String veventWithLocalDTStartAndDuration(Date start, String duration) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        bob.append("DTSTART:").append(dateTime.format(start)).append("\n");
        bob.append("DURATION:").append(duration).append("\n");

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String veventWithSimpleProperties(Date start, Date end, String...properties) {
        StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        for(int i = 0; i < properties.length; i++) {
            String name = properties[i++];
            String value = properties[i];
            bob.append(name).append(":").append(value).append("\n");
        }

        endStandardAppFields(bob);
        return bob.toString();
    }

    public String veventWithAttendees(Date start, Date end, String[] mails) {
        StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        for(String mail : mails) {
            bob.append("ATTENDEE:MAILTO:").append(mail).append("\n");
        }

        endStandardAppFields(bob);
        return bob.toString();        
    }

    public String veventWithResources(Date start, Date end, String[] resources) {
        StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);
        bob.append("RESOURCES:");
        for(String resource : resources) {
           bob.append(resource).append(",");
        }
        bob.setCharAt(bob.length()-1, '\n');

        endStandardAppFields(bob);
        return bob.toString();
    }



    public String veventWithResourcesInAttendees(Date start, Date end, String[] resources) {
        StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        for(String cn : resources) {
            bob.append("ATTENDEE;CUTYPE=RESOURCE;CN=").append(cn).append(":MAILTO:ignored@bla.invalid\n");
        }

        endStandardAppFields(bob);
        return bob.toString();
    }

    public String veventWithCategories(Date start, Date end, String[] categories) {
        StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);
        bob.append("CATEGORIES:");
        for(String category : categories) {
            bob.append(category).append(",");
        }
        bob.setCharAt(bob.length()-1, '\n');

        endStandardAppFields(bob);
        return bob.toString();
    }



    public String veventWithDeleteExceptionsAsDateTime(Date start, Date end, String rrule, Date[] exceptions) {
        StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        bob.append("RRULE:").append(rrule).append("\n");

        bob.append("EXDATE:");

        for(Date exception : exceptions) {
            bob.append(dateTime.format(exception)).append(",");
        }
        bob.setCharAt(bob.length()-1,'\n');

        endStandardAppFields(bob);
        return bob.toString();
    }

    public String veventWithDeleteExceptionsAsDate(Date start, Date end, String rrule, Date[] exceptions) {
        StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        bob.append("RRULE:").append(rrule).append("\n");

        bob.append("EXDATE;VALUE=DATE:");

        for(Date exception : exceptions) {
            bob.append(date.format(exception)).append(",");
        }
        bob.setCharAt(bob.length()-1,'\n');

        endStandardAppFields(bob);
        return bob.toString();
    }


    public String veventWithDisplayAlarm(Date start, Date end, String trigger, String description) {
        StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        bob.append("BEGIN:VALARM\n");
        bob.append(trigger).append("\n");
        bob.append("ACTION:DISPLAY\n");
        bob.append("DESCRIPTION:").append(description).append("\n");
        bob.append("END:VALARM\n");

        endStandardAppFields(bob);
        return bob.toString();
    }


    private void standardAppFields(StringBuilder bob, Date start, Date end) {
        beginCalendar(bob);
        beginEvent(bob);

        bob.append("DTSTART:").append(dateTime.format(start)).append("Z\n");
        bob.append("DTEND:").append(dateTime.format(end)).append("Z\n");

    }

    private void endStandardAppFields(StringBuilder bob) {
        endEvent(bob);
        endCalendar(bob);

    }

    private void addTimeZone(StringBuilder bob) {
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

    private void beginCalendar(StringBuilder bob) {
        bob.append("BEGIN:VCALENDAR\n")
           .append("VERSION:2.0\n");
    }


    private void beginEvent(StringBuilder bob) {
        bob.append("BEGIN:VEVENT\n");
    }

    private void endEvent(StringBuilder bob) {
        bob.append("END:VEVENT\n");
    }

    private void endCalendar(StringBuilder bob) {
        bob.append("END:VCALENDAR\n");
    }


    // VTODO


    private void standardTodoFields(StringBuilder bob) {
        bob.append("BEGIN:VCALENDAR\nBEGIN:VTODO\n");
    }

    private void endStandardTodoFields(StringBuilder bob) {
        bob.append("END:VTODO\nEND:VCALENDAR");
    }

    private void endTodo(StringBuilder bob) {
        bob.append("END:VTODO\n");
    }

    private void beginTodo(StringBuilder bob) {
        bob.append("BEGIN:VTODO\n");
    }

    public String vtodoWithSimpleProperties(String...properties) {

        StringBuilder bob = new StringBuilder();

        standardTodoFields(bob);

        for(int i = 0; i < properties.length; i++) {
            String name = properties[i++];
            String value = properties[i];
            bob.append(name).append(":").append(value).append("\n");
        }

        endStandardTodoFields(bob);
        return bob.toString();
    }


    public String vtodoWithLocalDTStartAndDTEnd(Date start, Date end) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        localDTStartAndDTEnd(bob, start, end);

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithUTCDTStartAndDTEnd(Date start, Date end) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDTEnd(bob, start, end);

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDTStartAndEndInTimeZone(Date start, Date end, TimeZone timeZone) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        timezoneDTStartAndDTEnd(bob, start, end, timeZone.getID());

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDTStartAndDTEndInCustomTimezone(Date start, Date end) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);

        addTimeZone(bob);

        beginTodo(bob);

        customTimezoneDTStartAndDTEnd(bob, start, end);

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithLocalDTStartAndDuration(Date start, String duration) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        bob.append("DTSTART:").append(dateTime.format(start)).append("\n");
        bob.append("DURATION:").append(duration).append("\n");

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithAttendees(String[] mails) {
        StringBuilder bob = new StringBuilder();
        standardTodoFields(bob);

        for(String mail : mails) {
            bob.append("ATTENDEE:MAILTO:").append(mail).append("\n");
        }

        endStandardTodoFields(bob);
        return bob.toString();
    }


    public String vtodoWithCategories(String[] categories) {
        StringBuilder bob = new StringBuilder();
        standardTodoFields(bob);
        bob.append("CATEGORIES:");
        for(String category : categories) {
            bob.append(category).append(",");
        }
        bob.setCharAt(bob.length()-1, '\n');

        endStandardTodoFields(bob);
        return bob.toString();
    }

    public String vtodoWithSimpleProperties(Date start, Date end, String...properties) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDTEnd(bob, start, end);

        for(int i = 0; i < properties.length; i++) {
            String name = properties[i++];
            String value = properties[i];
            bob.append(name).append(":").append(value).append("\n");
        }

        
        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDeleteExceptionsAsDateTime(Date start, Date end, String rrule, Date[] exceptions) {
        StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDTEnd(bob, start, end);

        bob.append("RRULE:").append(rrule).append("\n");

        bob.append("EXDATE:");

        for(Date exception : exceptions) {
            bob.append(dateTime.format(exception)).append(",");
        }
        bob.setCharAt(bob.length()-1,'\n');

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDeleteExceptionsAsDate(Date start, Date end, String rrule, Date[] exceptions) {
        StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDTEnd(bob, start, end);

        bob.append("RRULE:").append(rrule).append("\n");

        bob.append("EXDATE;VALUE=DATE:");

        for(Date exception : exceptions) {
            bob.append(date.format(exception)).append(",");
        }
        bob.setCharAt(bob.length()-1,'\n');

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDisplayAlarm(Date start, Date end, String trigger, String description) {
        StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        utcDTStartAndDTEnd(bob, start, end);

        bob.append("BEGIN:VALARM\n");
        bob.append(trigger).append("\n");
        bob.append("ACTION:DISPLAY\n");
        bob.append("DESCRIPTION:").append(description).append("\n");
        bob.append("END:VALARM\n");
        
        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDueDate(Date due) {
        StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        bob.append("DUE:").append(dateTime.format(due)).append("Z\n");

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String vtodoWithDateCompleted(Date dateCompleted) {
        StringBuilder bob = new StringBuilder();
        beginCalendar(bob);
        beginTodo(bob);

        bob.append("COMPLETED:").append(dateTime.format(dateCompleted)).append("Z\n");

        endTodo(bob);
        endCalendar(bob);

        return bob.toString();
    }


    //Error Cases
    
    public String veventWithEnd(Date date) {
        return veventWithOneDate("DTEND", date);
    }


    public String veventWithStart(Date date) {
        return veventWithOneDate("DTSTART", date);
    }

    private String veventWithOneDate(String property, Date date) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        bob.append(property).append(":").append(dateTime.format(date)).append("\n");

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String veventWithUnspecifiedVTimeZone(Date start, Date end) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);

        
        beginEvent(bob);

        customTimezoneDTStartAndDTEnd(bob, start, end);

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

    public String veventWithTwoRecurrences(Date start, Date end) {
       return veventWithSimpleProperties(start, end, "RRULE", "FREQ=DAILY;INTERVAL=1;COUNT=3", "RRULE", "FREQ=DAILY;INTERVAL=2;COUNT=6");
    }

    public String veventWithAudioAlarm(Date start, Date end, String trigger, String audioFile) {
        StringBuilder bob = new StringBuilder();
        standardAppFields(bob, start,end);

        bob.append("BEGIN:VALARM\n");
        bob.append(trigger).append("\n");
        bob.append("ACTION:AUDIO\n");
        bob.append("ATTACH:").append(audioFile).append("\n");
        bob.append("END:VALARM\n");

        endStandardAppFields(bob);
        return bob.toString();
    }

    public String veventWithWholeDayEvent(Date start) {
        StringBuilder bob = new StringBuilder();

        beginCalendar(bob);
        beginEvent(bob);

        bob.append("DTSTART;VALUE=DATE:").append(date.format(start)).append("\n");

        endEvent(bob);
        endCalendar(bob);

        return bob.toString();
    }

}
