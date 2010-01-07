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

package com.openexchange.subscribe.crawler;

import com.gargoylesoftware.htmlunit.WebClient;
import com.google.gdata.client.*;
import com.google.gdata.client.calendar.*;
import com.google.gdata.data.*;
import com.google.gdata.data.acl.*;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.LoginStep;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link GoogleCalendarAPIStep}
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GoogleCalendarAPIStep extends AbstractStep<CalendarDataObject[], Object> implements LoginStep {

    private static final Log LOG = LogFactory.getLog(GoogleCalendarAPIStep.class);
    
    private String username, password;

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.AbstractStep#execute(com.gargoylesoftware.htmlunit.WebClient)
     */
    @Override
    public void execute(WebClient webClient) throws SubscriptionException {
        // Create a CalenderService and authenticate
        CalendarService myService = new CalendarService("com.openexchange");
        try {
            myService.setUserCredentials(username, password);
            URL feedUrl = new URL("http://www.google.com/calendar/feeds/" + username + "/private/full");
            CalendarEventFeed myFeed = myService.getFeed(feedUrl, CalendarEventFeed.class);
            ArrayList events = new ArrayList<CalendarDataObject>();

            for (int i = 0; i < myFeed.getEntries().size(); i++) {
                CalendarEventEntry googleEvent = myFeed.getEntries().get(i);
                CalendarDataObject oxEvent = new CalendarDataObject();

                // map the attributes from Google-CalendarEventEntry to OX-CalendarDataObject
                oxEvent.setTimezone(myFeed.getTimeZone().getValue());
                oxEvent.setTitle(googleEvent.getTitle().getPlainText());
                for (When when : googleEvent.getTimes()) {
                    oxEvent.setStartDate(new Date(when.getStartTime().getValue()));
                    oxEvent.setEndDate(new Date(when.getEndTime().getValue()));
                }
                oxEvent.setNote(googleEvent.getPlainTextContent());
                handleRecurrence(googleEvent, oxEvent);
                // TODO: reminder
                events.add(oxEvent);
            }

            output = new CalendarDataObject[events.size()];
            for (int i = 0; i < events.size() && i < output.length; i++) {
                output[i] = (CalendarDataObject) events.get(i);
            }
            executedSuccessfully = true;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

    }

    private void handleRecurrence(CalendarEventEntry googleEvent, CalendarDataObject oxEvent) {
        try {
            String fullString = googleEvent.getRecurrence().getValue();
            Pattern recurrencePattern = Pattern.compile("(RRULE[^\\n]*)");
            Matcher recurrenceMatcher = recurrencePattern.matcher(fullString);
            Date startDate = null;
            if (recurrenceMatcher.find()) {
                // Start- and End-Date information needs to be gained differently for series, it is not contained in the event
                // itself as for non-series-events
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                Pattern startDatePattern = Pattern.compile("DTSTART[^:]*:(.*)");
                Matcher startDateMatcher = startDatePattern.matcher(fullString);
                if (startDateMatcher.find()) {
                    String startDateString = startDateMatcher.group(1);
                    startDateString = startDateString.replace("T", "");
                    startDate = sdf.parse(startDateString);
                    oxEvent.setStartDate(startDate);
                }
                Pattern endDatePattern = Pattern.compile("DTEND[^:]*:(.*)");
                Matcher endDateMatcher = endDatePattern.matcher(fullString);
                if (endDateMatcher.find()) {
                    String endDateString = endDateMatcher.group(1);
                    endDateString = endDateString.replace("T", "");
                    Date endDate = sdf.parse(endDateString);
                    oxEvent.setEndDate(endDate);
                }
                String recurrenceLine = recurrenceMatcher.group(1);
                // Frequency information will be set here
                Pattern frequencyPattern = Pattern.compile("FREQ=([^;]*);");
                Matcher frequencyMatcher = frequencyPattern.matcher(recurrenceLine);
                if (frequencyMatcher.find()) {
                    String freq = frequencyMatcher.group(1);
                    if (freq.equals("DAILY")) {
                        oxEvent.setRecurrenceType(Appointment.DAILY);  
                    } else if (freq.equals("WEEKLY")) {
                        oxEvent.setRecurrenceType(Appointment.WEEKLY);
                    } else if (freq.equals("MONTHLY") && startDate != null) {
                        oxEvent.setRecurrenceType(Appointment.MONTHLY);
                        oxEvent.setDayInMonth(startDate.getDay());
                    } else if (freq.equals("YEARLY") && startDate != null) {
                        oxEvent.setRecurrenceType(Appointment.YEARLY);
                        oxEvent.setDayInMonth(startDate.getDay());
                        oxEvent.setMonth(startDate.getMonth());
                    }
                }
                // if the series has an end it will be set here
                Pattern untilPattern = Pattern.compile("UNTIL=([0-9]{4})([0-9]{2})([0-9]{2})");
                Matcher untilMatcher = untilPattern.matcher(recurrenceLine);
                if (untilMatcher.find()) {
                    String untilYear = untilMatcher.group(1);
                    String untilMonth = untilMatcher.group(2);
                    String untilDay = untilMatcher.group(3);
                    SimpleDateFormat untilDateFormat = new SimpleDateFormat("yyyyMMdd");
                    oxEvent.setUntil(untilDateFormat.parse(untilYear + untilMonth + untilDay));
                }
                // interval information will be set here (e.g. "every X days/weeks/months/years")
                Pattern intervalPattern = Pattern.compile("INTERVAL=([0-9]{1})");
                Matcher intervalMatcher = intervalPattern.matcher(recurrenceLine);
                if (intervalMatcher.find()){
                    oxEvent.setInterval(Integer.parseInt(intervalMatcher.group(1)));
                }
            }
        } catch (NullPointerException e) {
            // Whyever this occurs here beats me...
        } catch (ParseException e) {
            LOG.error(e.getMessage());
        }
    }

    public String getBaseUrl() {
        return "";
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
