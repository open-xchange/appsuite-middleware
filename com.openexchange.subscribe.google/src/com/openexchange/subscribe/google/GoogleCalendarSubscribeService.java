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
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.google.internal.CalendarEventParser;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;

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
            final List<CalendarDataObject> single = new LinkedList<CalendarDataObject>();
            final List<CalendarDataObject> seriesExceptions = new LinkedList<CalendarDataObject>();
            final List<CalendarDataObject> series = new LinkedList<CalendarDataObject>();

            // Initialize folderUpdater and thread pool services
            final ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);

            if (null == threadPool) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("ThreadPoolService");
            }

            final AppointmentSqlFactoryService factoryService = services.getOptionalService(AppointmentSqlFactoryService.class);
            if (null == factoryService) {
                throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
            }
            final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(subscription.getSession());

            // Fetch the events
            final String accessToken = googleCreds.getAccessToken();
            final Integer pageSize = Integer.valueOf(PAGE_SIZE);
            Events events = googleCalendarService.events().list(calendarId).setOauthToken(accessToken).setMaxResults(pageSize).execute();
            parseAndAdd(events, parser, single, series, seriesExceptions);

            if (!series.isEmpty()) {
                // handle series and series exceptions in background thread
                threadPool.submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        single.addAll(series);
                        if (!seriesExceptions.isEmpty()) {
                            single.addAll(seriesExceptions);
                        }
                        return null;
                    }
                });
            }

            String nextToken = events.getNextPageToken();
            if (nextToken == null) {
                return single;
            }
            
            // Or spawn background thread
            final String tmp = nextToken;
            threadPool.submit(new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    String nextPageToken = tmp;
                    Events e;
                    List<CalendarDataObject> appointments = new LinkedList<CalendarDataObject>();
                    do {
                        e = googleCalendarService.events().list(calendarId).setOauthToken(accessToken).setMaxResults(pageSize).setPageToken(nextPageToken).execute();
                        parseAndAdd(e, parser, appointments, series, seriesExceptions);
                        for(CalendarDataObject cdo : appointments) {
                            cdo.setParentFolderID(subscription.getFolderIdAsInt());
                            appointmentsql.insertAppointmentObject(cdo);
                        }
                    } while ((nextPageToken = e.getNextPageToken()) != null);

                    if (!series.isEmpty()) {
                        final Map<String, CalendarDataObject> masterMap = new HashMap<String, CalendarDataObject>(series.size());
                        for(CalendarDataObject cdo : series) {
                            cdo.setParentFolderID(subscription.getFolderIdAsInt());
                            appointmentsql.insertAppointmentObject(cdo);
                            masterMap.put(cdo.getUid(), cdo);
                        }
                        if (!seriesExceptions.isEmpty()) {
                            final java.util.Calendar gmtCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            for(CalendarDataObject cdo : seriesExceptions) {
                                final CalendarDataObject masterObj = masterMap.get(cdo.getUid());
                                cdo.setObjectID(masterObj.getObjectID());
                                setBeginOfTheDay(cdo.getStartDate(), gmtCalendar);
                                cdo.setRecurrenceDatePosition(gmtCalendar.getTime());
                                cdo.setParentFolderID(subscription.getFolderIdAsInt());
                                appointmentsql.updateAppointmentObject(cdo, subscription.getFolderIdAsInt(), masterObj.getLastModified());
                                masterObj.setLastModified(cdo.getLastModified());
                            }
                        }
                    }

                    return null;
                }
            });
            return single;
        } catch (IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        }
    }

    protected void parseAndAdd(final Events events, final CalendarEventParser parser, final List<CalendarDataObject> singleAppointments, final List<CalendarDataObject> series, final List<CalendarDataObject> seriesExceptions) throws OXException {
        for (Event event : events.getItems()) {
            final CalendarDataObject calendarObject = new CalendarDataObject();
            parser.parseCalendarEvent(event, calendarObject);
            if (event.getRecurrence() != null) {
                series.add(calendarObject);
            } else if (event.getRecurringEventId() != null) {
                seriesExceptions.add(calendarObject);
            } else {
                singleAppointments.add(calendarObject);
            }
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
