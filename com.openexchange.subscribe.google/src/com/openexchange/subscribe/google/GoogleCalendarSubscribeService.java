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

package com.openexchange.subscribe.google;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.google.internal.CalendarEventParser;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GoogleCalendarSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GoogleCalendarSubscribeService extends AbstractGoogleSubscribeService {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleCalendarSubscribeService.class);

    final Integer pageSize;

    private final SubscriptionSource source;

    public GoogleCalendarSubscribeService(final OAuthServiceMetaData googleMetaData, ServiceLookup services) {
        super(googleMetaData, services);
        source = initSS(FolderObject.CALENDAR, "calendar");
        final ConfigurationService configService = services.getService(ConfigurationService.class);
        pageSize = Integer.valueOf(configService.getIntProperty("com.openexchange.subscribe.google.calendar.pageSize", 25));
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

        // Initialize thread pool service
        final ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);

        if (null == threadPool) {
            throw ServiceExceptionCode.absentService(ThreadPoolService.class);
        }

        final String calendarId;
        {
            final String tmp = (String) subscription.getConfiguration().get("calendarId");
            calendarId = (tmp == null) ? "primary" : tmp;
        }

        final ServerSession session = subscription.getSession();
        final GoogleCredential googleCreds = GoogleApiClients.getCredentials(session);
        final Calendar googleCalendarService = new Calendar.Builder(googleCreds.getTransport(), googleCreds.getJsonFactory(), googleCreds.getRequestInitializer()).setApplicationName(GoogleApiClients.getGoogleProductName(session)).build();

        // Check if we have permissions
        try {
            googleCalendarService.events().list(calendarId).setOauthToken(googleCreds.getAccessToken()).setMaxResults(1).execute();
        } catch (IOException e) {
            if (e instanceof GoogleJsonResponseException) {
                GoogleJsonResponseException ex = (GoogleJsonResponseException) e;
                if (ex.getStatusCode() == 403) {
                    GoogleJsonError details = ex.getDetails();
                    String message = details.getMessage();
                    if (message.toLowerCase().equals("insufficient permission")) {
                        throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(API.GOOGLE.getShortName(), OXScope.calendar_ro.getDisplayName());
                    }
                    throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(message);
                }
            }
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        }

        // Handle everything in a background thread
        threadPool.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {

                final CalendarEventParser parser = new CalendarEventParser(session);

                // Initialize lists
                final List<CalendarDataObject> changeExceptions = new LinkedList<CalendarDataObject>();
                final List<CalendarDataObject> deleteExceptions = new LinkedList<CalendarDataObject>();
                final List<CalendarDataObject> series = new LinkedList<CalendarDataObject>();

                final AppointmentSqlFactoryService factoryService = services.getOptionalService(AppointmentSqlFactoryService.class);
                if (null == factoryService) {
                    throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
                }
                final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(session);

                // Generate list request...
                Calendar.Events.List list = googleCalendarService.events().list(calendarId).setOauthToken(googleCreds.getAccessToken()).setMaxResults(pageSize);

                // ... and do the pagination
                String nextPageToken = null;
                Events events;
                do {
                    List<CalendarDataObject> single = new LinkedList<CalendarDataObject>();
                    if (null != nextPageToken) {
                        list.setPageToken(nextPageToken);
                    }
                    try {
                        events = list.execute();
                    } catch (IOException e) {
                        LOG.error(e.getMessage(), e);
                        throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
                    }
                    parseAndAdd(events, parser, single, series, changeExceptions, deleteExceptions);
                    for (CalendarDataObject cdo : single) {
                        cdo.setParentFolderID(subscription.getFolderIdAsInt());
                        try {
                            appointmentsql.insertAppointmentObject(cdo);
                        } catch (Exception e) {
                            // Just log the exception
                            LOG.error("Couldn't import appointment {}.", cdo, e);
                        }
                    }

                } while ((nextPageToken = events.getNextPageToken()) != null);

                handleSeriesAndSeriesExceptions(subscription, changeExceptions, deleteExceptions, series, appointmentsql);

                return null;
            }
        });

        return new LinkedList<CalendarDataObject>();
    }

    private void parseAndAdd(final Events events, final CalendarEventParser parser, final List<CalendarDataObject> singleAppointments, final List<CalendarDataObject> series, final List<CalendarDataObject> changeExceptions, final List<CalendarDataObject> deleteExceptions) throws OXException, IOException {
        for (Event event : events.getItems()) {
            // Consider only events with an organizer; the rest are only updates on status, e.g. delete exceptions (handle below)
            final CalendarDataObject calendarObject = new CalendarDataObject();
            LOG.debug("Parsing event: {}", event.toPrettyString());
            if (event.getOrganizer() != null) {
                parser.parseCalendarEvent(event, calendarObject);
                if (event.getRecurrence() != null) {
                    series.add(calendarObject);
                } else if (event.getRecurringEventId() != null) {
                    changeExceptions.add(calendarObject);
                } else {
                    singleAppointments.add(calendarObject);
                }
            } else {
                parser.parseDeleteException(event, calendarObject);
                deleteExceptions.add(calendarObject);
            }
        }
    }

    private void handleSeriesAndSeriesExceptions(final Subscription subscription, final List<CalendarDataObject> changeExceptions, final List<CalendarDataObject> deleteExceptions, final List<CalendarDataObject> series, final AppointmentSQLInterface appointmentsql) throws OXException {
        final Map<String, CalendarDataObject> masterMap = new HashMap<String, CalendarDataObject>(series.size());

        handleSeries(subscription, series, appointmentsql, masterMap);
        handleExceptions(subscription, changeExceptions, appointmentsql, masterMap, false);
        handleExceptions(subscription, deleteExceptions, appointmentsql, masterMap, true);
    }

    private Map<String, CalendarDataObject> handleSeries(final Subscription subscription, final List<CalendarDataObject> series, final AppointmentSQLInterface appointmentsql, final Map<String, CalendarDataObject> masterMap) throws OXException {
        // Handle series
        for (CalendarDataObject cdo : series) {
            try {
                cdo.setParentFolderID(subscription.getFolderIdAsInt());
                appointmentsql.insertAppointmentObject(cdo);
                masterMap.put(cdo.getUid(), cdo);
            } catch (Exception e) {
                LOG.error("Couldn't import series appointment {}.", cdo, e);
            }
        }
        return masterMap;
    }

    private void handleExceptions(final Subscription subscription, final List<CalendarDataObject> exceptions, final AppointmentSQLInterface appointmentsql, final Map<String, CalendarDataObject> masterMap, final boolean delete) throws OXException {
        // Handle exceptions
        final java.util.Calendar utcCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        for (CalendarDataObject cdo : exceptions) {
            String uid = cdo.getUid();
            final CalendarDataObject masterObj = masterMap.get(uid);
            if (masterObj != null) {
                cdo.setObjectID(masterObj.getObjectID());
                setBeginOfTheDay(cdo.getStartDate(), utcCalendar);
                cdo.setRecurrenceDatePosition(utcCalendar.getTime());
                boolean error = false;
                if (delete) {
                    try {
                        appointmentsql.deleteAppointmentObject(cdo, subscription.getFolderIdAsInt(), masterObj.getLastModified());
                    } catch (SQLException e) {
                        error = true;
                        throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                    } catch (Exception e) {
                        error = true;
                        LOG.error("Couldn't create delete exception {} to series appointment {}.", cdo, masterObj, e);
                    }
                } else {
                    try {
                        appointmentsql.updateAppointmentObject(cdo, subscription.getFolderIdAsInt(), masterObj.getLastModified());
                    } catch (Exception e) {
                        error = true;
                        LOG.error("Couldn't create change exception {} to series appointment {}.", cdo, masterObj, e);
                    }
                }
                if (!error) {
                    masterObj.setLastModified(cdo.getLastModified());
                }
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
