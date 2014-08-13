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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.subscribe.google;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.google.internal.CalendarEventParser;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.iterator.SearchIteratorDelegator;

/**
 * {@link GoogleCalendarSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GoogleCalendarSubscribeService extends AbstractGoogleSubscribeService {

    private static final int PAGE_SIZE = 25;

    private final SubscriptionSource source;

    public GoogleCalendarSubscribeService(final OAuthServiceMetaData googleMetaData, ServiceLookup services) {
        super(googleMetaData, services);
        source = initSS(FolderObject.CALENDAR, "calendar");
    }

    @Override
    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    @Override
    public boolean handles(int folderModule) {
        return FolderObject.CALENDAR == folderModule;
    }

    @Override
    public Collection<?> getContent(final Subscription subscription) throws OXException {
        try {
            GoogleCredential googleCreds = GoogleApiClients.getCredentials(subscription.getSession());
            final Calendar googleCalendarService = new Calendar(
                googleCreds.getTransport(),
                googleCreds.getJsonFactory(),
                googleCreds.getRequestInitializer());

            final String calendarId;
            {
                final String tmp = (String) subscription.getConfiguration().get("calendarId");
                calendarId = (tmp == null) ? "primary" : tmp;
            }

            final CalendarEventParser parser = new CalendarEventParser(subscription.getSession());

            // Initialize lists
            final List<CalendarObject> singleAppointments = new LinkedList<CalendarObject>();
            final List<CalendarObject> seriesExceptions = new LinkedList<CalendarObject>();
            final List<CalendarObject> series = new LinkedList<CalendarObject>();

            // Initialize folderUpdater and thread pool services
            final ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
            final FolderUpdaterRegistry folderUpdaterRegistry = services.getOptionalService(FolderUpdaterRegistry.class);
            final FolderUpdaterService<CalendarObject> folderUpdater = null == folderUpdaterRegistry ? null : folderUpdaterRegistry.<CalendarObject> getFolderUpdater(subscription);

            if (null == threadPool) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("ThreadPoolService");
            }

            if (null == folderUpdater) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("FolderUpdaterService");
            }

            // Fetch the events
            final String accessToken = googleCreds.getAccessToken();
            final Integer pageSize = Integer.valueOf(PAGE_SIZE);
            Events events = googleCalendarService.events().list(calendarId).setOauthToken(accessToken).setMaxResults(pageSize).execute();
            parseAndAdd(events, parser, singleAppointments, series, seriesExceptions);

            if (!series.isEmpty()) {
                // handle series and series exceptions in background thread
                threadPool.submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        singleAppointments.addAll(series);
                        if (!seriesExceptions.isEmpty()) {
                            singleAppointments.addAll(seriesExceptions);
                        }
                        folderUpdater.save(new SearchIteratorDelegator<CalendarObject>(singleAppointments), subscription);
                        return null;
                    }
                });
            }

            String nextToken = events.getNextPageToken();
            if (nextToken == null) {
                return singleAppointments;
            }

            // Or spawn background thread
            final String tmp = nextToken;
            threadPool.submit(new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    String nextPageToken = tmp;
                    Events e;
                    List<CalendarObject> appointments = new LinkedList<CalendarObject>();
                    do {
                        e = googleCalendarService.events().list(calendarId).setOauthToken(accessToken).setMaxResults(pageSize).setPageToken(
                            nextPageToken).execute();
                        parseAndAdd(e, parser, appointments, series, seriesExceptions);
                        folderUpdater.save(new SearchIteratorDelegator<CalendarObject>(appointments), subscription);
                    } while ((nextPageToken = e.getNextPageToken()) != null);

                    if (!series.isEmpty()) {
                        folderUpdater.save(new SearchIteratorDelegator<CalendarObject>(series), subscription);
                        final Map<String, CalendarObject> exceptionMap = new HashMap<String, CalendarObject>();
                        for (CalendarObject co : series) {
                            exceptionMap.put(co.getExtendedProperties().get("iCalUID").toString(), co);
                        }
                        if (!seriesExceptions.isEmpty()) {
                            for (CalendarObject co : seriesExceptions) {
                                CalendarObject cdo = exceptionMap.get(co.getExtendedProperties().get("iCalUID"));
                                co.setChangeExceptions(Collections.singletonList(co.getStartDate()));
                                co.setRecurrenceID(cdo.getRecurrenceID());
                            }
                            folderUpdater.save(new SearchIteratorDelegator<CalendarObject>(seriesExceptions), subscription);
                        }
                    }

                    return null;
                }
            });
            return singleAppointments;
        } catch (IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        }
    }

    protected void parseAndAdd(final Events events, final CalendarEventParser parser, final List<CalendarObject> singleAppointments, final List<CalendarObject> series, final List<CalendarObject> seriesExceptions) throws OXException {
        final Map<String, CalendarDataObject> exceptionMap = new HashMap<String, CalendarDataObject>();

        for (Event event : events.getItems()) {
            final CalendarDataObject calendarObject = new CalendarDataObject();
            parser.parseCalendarEvent(event, calendarObject);
            calendarObject.addExtendedProperty("iCalUID", event.getICalUID());
            if (event.getRecurrence() != null) {
                series.add(calendarObject);
                exceptionMap.put(event.getICalUID(), calendarObject);
            } else if (event.getRecurringEventId() != null) {
                seriesExceptions.add(calendarObject);
            } else {
                singleAppointments.add(calendarObject);
            }
        }
        
        final java.util.Calendar gmtCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        for (CalendarObject exception : seriesExceptions) {
            CalendarDataObject master = exceptionMap.get(exception.getExtendedProperties().get("iCalUID"));
            int seq = master.getSequence();
            master.setSequence(++seq);
            setBeginOfTheDay(exception.getStartDate(), gmtCalendar);
            exception.setRecurrenceDatePosition(gmtCalendar.getTime());
            master.addChangeException(gmtCalendar.getTime());
        }
    }
    
    private void setBeginOfTheDay(final Date startDate, final java.util.Calendar calendar) {
        calendar.setTime(startDate);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
    }
}
