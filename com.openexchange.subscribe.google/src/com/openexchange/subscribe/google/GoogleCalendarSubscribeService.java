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
import java.util.Iterator;
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
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.osgi.ShutDownRuntimeException;
import com.openexchange.policy.retry.ExponentialBackOffRetryPolicy;
import com.openexchange.policy.retry.RetryPolicy;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.startup.ThreadControlService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.google.internal.CalendarEventParser;
import com.openexchange.subscribe.google.osgi.Services;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GoogleCalendarSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GoogleCalendarSubscribeService extends AbstractGoogleSubscribeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarSubscribeService.class);

    /** The logger */
    static final Logger LOG = LoggerFactory.getLogger(GoogleCalendarSubscribeService.class);

    final Integer pageSize;
    private final SubscriptionSource source;

    /**
     * Initialises a new {@link GoogleCalendarSubscribeService}.
     *
     * @param googleMetaData The {@link OAuthServiceMetaData} for the Google subscribe service
     * @param services The {@link ServiceLookup} instance
     */
    public GoogleCalendarSubscribeService(final OAuthServiceMetaData googleMetaData, ServiceLookup services) {
        super(googleMetaData, services);
        source = initSS(FolderObject.CALENDAR, "calendar");
        final ConfigurationService configService = services.getService(ConfigurationService.class);
        pageSize = Integer.valueOf(configService.getIntProperty("com.openexchange.subscribe.google.calendar.pageSize", 25));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.subscribe.SubscribeService#getSubscriptionSource()
     */
    @Override
    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.subscribe.SubscribeService#handles(int)
     */
    @Override
    public boolean handles(int folderModule) {
        return FolderObject.CALENDAR == folderModule;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.subscribe.SubscribeService#getContent(com.openexchange.subscribe.Subscription)
     */
    @Override
    public Collection<?> getContent(final Subscription subscription) throws OXException {

        // Initialise thread pool service
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

        OAuthAccount oauthAccount = null;
        {
            Object accountId = subscription.getConfiguration().get("account");
            if (null != accountId) {
                int iAccountId;
                if (accountId instanceof Integer) {
                    iAccountId = ((Integer) accountId).intValue();
                } else {
                    iAccountId = Integer.parseInt(accountId.toString());
                }
                OAuthService service = services.getService(OAuthService.class);
                oauthAccount = service.getAccount(iAccountId, session, session.getUserId(), session.getContextId());
            }
        }

        final GoogleCredential googleCreds = null == oauthAccount ? GoogleApiClients.getCredentials(session) : GoogleApiClients.getCredentials(oauthAccount, session);
        final Calendar googleCalendarService = new Calendar.Builder(googleCreds.getTransport(), googleCreds.getJsonFactory(), googleCreds.getRequestInitializer()).setApplicationName(GoogleApiClients.getGoogleProductName(session)).build();

        // Check if we have permissions
        try {
            googleCalendarService.events().list(calendarId).setOauthToken(googleCreds.getAccessToken()).setMaxResults(Integer.valueOf(1)).execute();
        } catch (IOException e) {
            if (e instanceof GoogleJsonResponseException) {
                GoogleJsonResponseException ex = (GoogleJsonResponseException) e;
                if (ex.getStatusCode() == 403) {
                    GoogleJsonError details = ex.getDetails();
                    String message = details.getMessage();
                    if (message.toLowerCase().equals("insufficient permission")) {
                        throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(KnownApi.GOOGLE.getShortName(), OXScope.calendar_ro.getDisplayName());
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
                Thread currentThread = Thread.currentThread();
                ThreadControlService threadControl = Services.getOptionalService(ThreadControlService.class);

                boolean added = null == threadControl ? false : threadControl.addThread(currentThread);
                try {
                    final CalendarEventParser parser = new CalendarEventParser(session);

                    // Initialise lists
                    final List<CalendarDataObject> changeExceptions = new LinkedList<CalendarDataObject>();
                    final List<CalendarDataObject> deleteExceptions = new LinkedList<CalendarDataObject>();
                    final List<CalendarDataObject> series = new LinkedList<CalendarDataObject>();

                    final AppointmentSqlFactoryService factoryService = services.getOptionalService(AppointmentSqlFactoryService.class);
                    if (null == factoryService) {
                        throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
                    }
                    final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(session);

                    // Workaround for Bug 53927. Wait until the folder was emptied
                    int objectsInFolder = -1;
                    RetryPolicy retryPolicy = new ExponentialBackOffRetryPolicy();
                    while (retryPolicy.isRetryAllowed() && objectsInFolder != 0) {
                        LOGGER.debug("Folder was not emptied yet. Retrying");
                        objectsInFolder = appointmentsql.countObjectsInFolder(subscription.getFolderIdAsInt());
                    }
                    if (objectsInFolder != 0) {
                        LOGGER.warn("The Google calendar subscription folder '{}' for subscription '{}' in context '{}' was not emptied. The subscription might result in inconcistencies", subscription.getFolderIdAsInt(), subscription.getId(), subscription.getContext().getContextId());
                    }

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
                            LOG.debug("Fetched {} events for user {} in context {}", events.size(), subscription.getSession().getUserId(), subscription.getSession().getContextId());
                        } catch (IOException e) {
                            LOG.error(e.getMessage(), e);
                            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
                        }

                        // Parse the events and fill the series, change exceptions and delete exceptions lists
                        parseAndAdd(events, parser, single, series, changeExceptions, deleteExceptions);
                        // Handle the single appointments first
                        handleSingleAppointments(subscription, appointmentsql, single, currentThread);
                    } while (false == currentThread.isInterrupted() && (nextPageToken = events.getNextPageToken()) != null);

                    // Now handle the series, change exceptions and deletion exceptions
                    handleSeriesAndSeriesExceptions(subscription, changeExceptions, deleteExceptions, series, appointmentsql, currentThread);
                } catch (ShutDownRuntimeException e) {
                    LOG.warn("Aborted Google Calendar subscription due to server shut-down");
                } finally {
                    if (added && null != threadControl) {
                        threadControl.removeThread(currentThread);
                    }
                }

                return null;
            }
        });

        return new LinkedList<CalendarDataObject>();
    }

    /**
     * Parses the {@link Event}s into {@link CalendarDataObject}s
     *
     * @param events The Google {@link Event}s
     * @param parser The {@link CalendarEventParser}
     * @param singleAppointments A list with single appointments
     * @param series A list with series appointments
     * @param changeExceptions A list with change exceptions
     * @param deleteExceptions A list with delete exceptions
     * @throws OXException If the operation fails
     * @throws IOException If an I/O error is occurred
     */
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

    /**
     * Handle single appointments
     *
     * @param subscription The {@link Subscription}
     * @param appointmentsql The {@link AppointmentSQLInterface}
     * @param single A list with single appointments
     * @throws OXException if the appointments cannot be inserted or updated
     */
    private void handleSingleAppointments(final Subscription subscription, final AppointmentSQLInterface appointmentsql, List<CalendarDataObject> single, Thread currentThread) throws OXException {
        for (Iterator<CalendarDataObject> iter = single.iterator(); false == currentThread.isInterrupted() && iter.hasNext();) {
            CalendarDataObject cdo = iter.next();
            cdo.setParentFolderID(subscription.getFolderIdAsInt());
            try {
                appointmentsql.insertAppointmentObject(cdo);
            } catch (OXException e) {
                if (OXCalendarExceptionCodes.APPOINTMENT_UID_ALREDY_EXISTS.equals(e)) {
                    // Such an appointment already exists
                    LOG.debug("An appointment with the same UID already exists: {}.", cdo, e);
                } else {
                    // Just log the exception
                    LOG.error("Couldn't import appointment {}.", cdo, e);
                }
            } catch (ShutDownRuntimeException e) {
                // Re-throw...
                throw e;
            } catch (RuntimeException e) {
                LOG.error("Unexpected exception while importing appointment {}.", cdo, e);
            }
        }
    }

    /**
     * Handles the appointment series and appointment series exceptions
     *
     * @param subscription The {@link Subscription}
     * @param changeExceptions A list with change exceptions
     * @param deleteExceptions A list with delete exceptions
     * @param series A list with appointment series
     * @param appointmentsql The {@link AppointmentSQLInterface}
     * @param currentThread The current thread
     * @throws OXException if the operation fails
     */
    void handleSeriesAndSeriesExceptions(final Subscription subscription, final List<CalendarDataObject> changeExceptions, final List<CalendarDataObject> deleteExceptions, final List<CalendarDataObject> series, final AppointmentSQLInterface appointmentsql, Thread currentThread) throws OXException {
        final Map<String, CalendarDataObject> masterMap = new HashMap<String, CalendarDataObject>(series.size());

        handleSeries(subscription, series, appointmentsql, masterMap, currentThread);
        handleExceptions(subscription, changeExceptions, appointmentsql, masterMap, false, currentThread);
        handleExceptions(subscription, deleteExceptions, appointmentsql, masterMap, true, currentThread);
    }

    /**
     * Handles the appointment series
     *
     * @param subscription The {@link Subscription}
     * @param series A list with series {@link CalendarDataObject}
     * @param appointmentsql The {@link AppointmentSQLInterface}
     * @param masterMap The map with all the appointments in the {@link Subscription}
     * @param currentThread The current thread
     * @return The map with all the appointments in the {@link Subscription}
     * @throws OXException if the operation fails
     */
    private Map<String, CalendarDataObject> handleSeries(final Subscription subscription, final List<CalendarDataObject> series, final AppointmentSQLInterface appointmentsql, final Map<String, CalendarDataObject> masterMap, Thread currentThread) throws OXException {
        for (Iterator<CalendarDataObject> iter = series.iterator(); false == currentThread.isInterrupted() && iter.hasNext();) {
            CalendarDataObject cdo = iter.next();
            try {
                cdo.setParentFolderID(subscription.getFolderIdAsInt());
                appointmentsql.insertAppointmentObject(cdo);
                masterMap.put(cdo.getUid(), cdo);
            } catch (OXException e) {
                if (OXCalendarExceptionCodes.APPOINTMENT_UID_ALREDY_EXISTS.equals(e)) {
                    // Such an appointment already exists
                    LOG.debug("A series appointment with the same UID already exists: {}.", cdo, e);
                } else {
                    // Just log the exception
                    LOG.error("Couldn't import series appointment {}.", cdo, e);
                }
            } catch (ShutDownRuntimeException e) {
                // Re-throw...
                throw e;
            } catch (RuntimeException e) {
                LOG.error("Unexpected exception while importing series appointment {}.", cdo, e);
            }
        }
        return masterMap;
    }

    /**
     * Handles the appointment exceptions
     *
     * @param subscription The {@link Subscription}
     * @param exceptions A list with {@link CalendarDataObject} exceptions
     * @param appointmentsql The {@link AppointmentSQLInterface}
     * @param masterMap A map with all the appointments in the {@link Subscription}
     * @param delete flag indicating a delete exception when true and a change exception when false
     * @param currentThread The current thread
     * @throws OXException if the operation fails
     */
    private void handleExceptions(final Subscription subscription, final List<CalendarDataObject> exceptions, final AppointmentSQLInterface appointmentsql, final Map<String, CalendarDataObject> masterMap, final boolean delete, Thread currentThread) throws OXException {
        // Handle exceptions
        final java.util.Calendar utcCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        for (Iterator<CalendarDataObject> iter = exceptions.iterator(); false == currentThread.isInterrupted() && iter.hasNext();) {
            CalendarDataObject cdo = iter.next();
            String uid = cdo.getUid();
            final CalendarDataObject masterObj = masterMap.get(uid);
            if (masterObj != null) {
                cdo.setObjectID(masterObj.getObjectID());
                setBeginOfTheDay(cdo.getStartDate(), utcCalendar);
                cdo.setRecurrenceDatePosition(utcCalendar.getTime());
                boolean error = true;
                if (delete) {
                    try {
                        appointmentsql.deleteAppointmentObject(cdo, subscription.getFolderIdAsInt(), masterObj.getLastModified());
                        error = false;
                    } catch (SQLException e) {
                        throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                    } catch (ShutDownRuntimeException e) {
                        // Re-throw...
                        throw e;
                    } catch (Exception e) {
                        LOG.error("Couldn't create delete exception {} to series appointment {}.", cdo, masterObj, e);
                    }
                } else {
                    try {
                        appointmentsql.updateAppointmentObject(cdo, subscription.getFolderIdAsInt(), masterObj.getLastModified());
                        error = false;
                    } catch (ShutDownRuntimeException e) {
                        // Re-throw...
                        throw e;
                    } catch (Exception e) {
                        LOG.error("Couldn't create change exception {} to series appointment {}.", cdo, masterObj, e);
                    }
                }
                if (!error) {
                    masterObj.setLastModified(cdo.getLastModified());
                }
            }
        }
    }

    /**
     * Sets the begin of the day to the specified {@link java.util.Calendar}
     *
     * @param startDate The start date
     * @param calendar The {@link java.util.Calendar}
     */
    private void setBeginOfTheDay(final Date startDate, final java.util.Calendar calendar) {
        calendar.setTime(startDate);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
    }
}
