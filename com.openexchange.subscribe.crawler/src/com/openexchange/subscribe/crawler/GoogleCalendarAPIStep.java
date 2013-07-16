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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.log.LogFactory;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.LoginStep;

/**
 * {@link GoogleCalendarAPIStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GoogleCalendarAPIStep extends AbstractStep<CalendarDataObject[], Object> implements LoginStep {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(GoogleCalendarAPIStep.class));

    private String username, password;

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.AbstractStep#execute(com.gargoylesoftware.htmlunit.WebClient)
     */
    @Override
    public void execute(final WebClient webClient) throws OXException {
        // Create a CalenderService and authenticate
        final CalendarService myService = new CalendarService("com.openexchange");
        try {
            myService.setUserCredentials(username, password);
            final URL feedUrl = new URL("http://www.google.com/calendar/feeds/" + username + "/private/full");
            final CalendarEventFeed myFeed = myService.getFeed(feedUrl, CalendarEventFeed.class);
            final ArrayList events = new ArrayList<CalendarDataObject>();

            for (int i = 0; i < myFeed.getEntries().size(); i++) {
                final CalendarEventEntry googleEvent = myFeed.getEntries().get(i);
                final CalendarDataObject oxEvent = new CalendarDataObject();

                // map the attributes from Google-CalendarEventEntry to OX-CalendarDataObject
                oxEvent.setTimezone(myFeed.getTimeZone().getValue());
                oxEvent.setTitle(googleEvent.getTitle().getPlainText());
                for (final When when : googleEvent.getTimes()) {
                    oxEvent.setStartDate(new Date(when.getStartTime().getValue()));
                    oxEvent.setEndDate(new Date(when.getEndTime().getValue()));
                }
                oxEvent.setNote(googleEvent.getPlainTextContent());
                if (googleEvent.getRecurrence() != null) {
                    handleRecurrence(googleEvent, oxEvent);
                }
                // TODO: reminder
                events.add(oxEvent);
            }

            output = new CalendarDataObject[events.size()];
            for (int i = 0; i < events.size() && i < output.length; i++) {
                output[i] = (CalendarDataObject) events.get(i);
            }
            executedSuccessfully = true;

        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (final AuthenticationException e) {
            LOG.error(e.getMessage(), e);
        } catch (final ServiceException e) {
            LOG.error(e);
            LOG.error("User with id=" + workflow.getSubscription().getUserId() + " and context=" + workflow.getSubscription().getContext() + " failed to subscribe source=" + workflow.getSubscription().getSource().getDisplayName() + " with display_name=" + workflow.getSubscription().getDisplayName());
            throw SubscriptionErrorMessage.TEMPORARILY_UNAVAILABLE.create();
        }

    }

    private void handleRecurrence(final CalendarEventEntry googleEvent, final CalendarDataObject oxEvent) {
        try {
            final String fullString = googleEvent.getRecurrence().getValue();
            final Pattern recurrencePattern = Pattern.compile("(RRULE[^\\n]*)");
            final Matcher recurrenceMatcher = recurrencePattern.matcher(fullString);
            Date startDate = null;
            if (recurrenceMatcher.find()) {
                // Start- and End-Date information needs to be gained differently for series, it is not contained in the event
                // itself as for non-series-events
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                final Pattern startDatePattern = Pattern.compile("DTSTART[^:]*:(.*)");
                final Matcher startDateMatcher = startDatePattern.matcher(fullString);
                if (startDateMatcher.find()) {
                    String startDateString = startDateMatcher.group(1);
                    startDateString = startDateString.replace("T", "");
                    startDate = sdf.parse(startDateString);
                    oxEvent.setStartDate(startDate);
                }
                final Pattern endDatePattern = Pattern.compile("DTEND[^:]*:(.*)");
                final Matcher endDateMatcher = endDatePattern.matcher(fullString);
                if (endDateMatcher.find()) {
                    String endDateString = endDateMatcher.group(1);
                    endDateString = endDateString.replace("T", "");
                    final Date endDate = sdf.parse(endDateString);
                    oxEvent.setEndDate(endDate);
                }
                final String recurrenceLine = recurrenceMatcher.group(1);
                LOG.debug("Event title : " + oxEvent.getTitle());
                LOG.debug("Start Date : " + oxEvent.getStartDate());
                LOG.debug("End   Date : " + oxEvent.getEndDate());
                LOG.debug(recurrenceLine);
                // Frequency information will be set here
                final Pattern frequencyPattern = Pattern.compile("FREQ=([^;]*);");
                final Matcher frequencyMatcher = frequencyPattern.matcher(recurrenceLine);
                if (frequencyMatcher.find()) {
                    final String freq = frequencyMatcher.group(1);
                    if (freq.equals("DAILY")) {
                        oxEvent.setRecurrenceType(CalendarObject.DAILY);
                        LOG.debug("Frequence : " + freq);
                    } else if (freq.equals("WEEKLY")) {
                        oxEvent.setRecurrenceType(CalendarObject.WEEKLY);
                        LOG.debug("Frequence : " + freq);
                    } else if (freq.equals("MONTHLY") && startDate != null) {
                        oxEvent.setRecurrenceType(CalendarObject.MONTHLY);
                        LOG.debug("Frequence : " + freq);
                        // oxEvent.setDayInMonth(startDate.getDate());
                        // LOG.debug("***** Day : " + startDate.getDay());
                    } else if (freq.equals("YEARLY") && startDate != null) {
                        LOG.debug("Frequence : " + freq);
                        oxEvent.setRecurrenceType(CalendarObject.YEARLY);
                        oxEvent.setDayInMonth(startDate.getDate());
                        oxEvent.setMonth(startDate.getMonth());
                        LOG.debug("Month : " + startDate.getMonth());
                        LOG.debug("Day in Month : " + startDate.getDate());
                    }
                }
                // WeekDay information will be set here
                final Pattern weekDayPattern = Pattern.compile("BYDAY=([A-Z]{2})");
                final Matcher weekDayMatcher = weekDayPattern.matcher(recurrenceLine);
                if (weekDayMatcher.find()) {
                    final String weekDay = weekDayMatcher.group(1);
                    LOG.debug("Weekday : " + weekDay);
                    if (weekDay.equals("MO")) {
                        oxEvent.setDays(CalendarObject.MONDAY);
                    } else if (weekDay.equals("TU")) {
                        oxEvent.setDays(CalendarObject.TUESDAY);
                    } else if (weekDay.equals("WE")) {
                        oxEvent.setDays(CalendarObject.WEDNESDAY);
                    } else if (weekDay.equals("TH")) {
                        oxEvent.setDays(CalendarObject.THURSDAY);
                    } else if (weekDay.equals("FR")) {
                        oxEvent.setDays(CalendarObject.FRIDAY);
                    } else if (weekDay.equals("SA")) {
                        oxEvent.setDays(CalendarObject.SATURDAY);
                    } else if (weekDay.equals("SU")) {
                        oxEvent.setDays(CalendarObject.SUNDAY);
                    }
                }
                // MonthDay information will be set here
                final Pattern monthDayPattern = Pattern.compile("BYMONTHDAY=([0-9]{2})");
                final Matcher monthDayMatcher = monthDayPattern.matcher(recurrenceLine);
                if (monthDayMatcher.find()) {
                    oxEvent.setDayInMonth(Integer.parseInt(monthDayMatcher.group(1)));
                    LOG.debug("MonthDay : " + Integer.parseInt(monthDayMatcher.group(1)));
                }
                // interval information will be set here (e.g. "every X days/weeks/months/years")
                final Pattern intervalPattern = Pattern.compile("INTERVAL=([0-9]{1})");
                final Matcher intervalMatcher = intervalPattern.matcher(recurrenceLine);
                if (intervalMatcher.find()) {
                    oxEvent.setInterval(Integer.parseInt(intervalMatcher.group(1)));
                    LOG.debug("Interval : " + Integer.parseInt(intervalMatcher.group(1)));
                } else {
                    // if there is no interval given we need to set it to 1
                    oxEvent.setInterval(1);
                    LOG.debug("Interval : 1");
                }
                // if the series has an end it will be set here
                final Pattern untilPattern = Pattern.compile("UNTIL=([0-9]{4})([0-9]{2})([0-9]{2})");
                final Matcher untilMatcher = untilPattern.matcher(recurrenceLine);
                if (untilMatcher.find()) {
                    final String untilYear = untilMatcher.group(1);
                    final String untilMonth = untilMatcher.group(2);
                    final String untilDay = untilMatcher.group(3);
                    final SimpleDateFormat untilDateFormat = new SimpleDateFormat("yyyyMMdd");
                    final Date untilDate = untilDateFormat.parse(untilYear + untilMonth + untilDay);
                    oxEvent.setUntil(untilDate);
                    LOG.debug("Until-Date : " + untilDate);
                }
            }
        } catch (final ParseException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public String getBaseUrl() {
        return "";
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public void setUsername(final String username) {
        this.username = username;
    }

}
