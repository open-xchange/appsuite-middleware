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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.oauth.OAuthServiceMetaData;
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
        final GoogleCredential googleCreds = GoogleApiClients.getCredentials(subscription.getSession());
        final Calendar googleCalendarService = new Calendar(
            googleCreds.getTransport(),
            googleCreds.getJsonFactory(),
            googleCreds.getRequestInitializer());
        
        final List<CalendarObject> calObjList = new LinkedList<CalendarObject>();

        final String tmp = (String) subscription.getConfiguration().get("calendarId");
        final String calendarId = (tmp == null) ? "primary" : tmp;

        try {
            // Fetch calendar metadata
            // com.google.api.services.calendar.model.Calendar calendar = googleCalendarService.calendars().get(calendarId).setOauthToken(googleCreds.getAccessToken()).execute();
            final CalendarEventParser parser = new CalendarEventParser(subscription.getSession().getContext());

            final Events events = googleCalendarService.events().list(calendarId).setOauthToken(googleCreds.getAccessToken()).setMaxResults(PAGE_SIZE).execute();
            parseAndAdd(events, parser, calObjList);

            if (events.getNextPageToken() == null) {
                return calObjList;
            } else {
                final ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
                final FolderUpdaterRegistry folderUpdaterRegistry = services.getOptionalService(FolderUpdaterRegistry.class);
                final FolderUpdaterService<CalendarObject> folderUpdater = null == folderUpdaterRegistry ? null : folderUpdaterRegistry.<CalendarObject> getFolderUpdater(subscription);

                if (null == threadPool || null == folderUpdater) {
                    // Fetch all in one thread
                    do {
                        final Events e = googleCalendarService.events().list(calendarId).setOauthToken(googleCreds.getAccessToken()).setMaxResults(PAGE_SIZE).setPageToken(
                            events.getNextPageToken()).execute();
                        parseAndAdd(e, parser, calObjList);
                    } while (events.getNextPageToken() != null);
                    return calObjList;
                }

                // Or spawn background thread
                threadPool.submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        do {
                            final Events e = googleCalendarService.events().list(calendarId).setOauthToken(googleCreds.getAccessToken()).setMaxResults(PAGE_SIZE).setPageToken(
                                events.getNextPageToken()).execute();
                            final List<CalendarObject> appointments = new ArrayList<CalendarObject>(e.getItems().size());
                            parseAndAdd(e, parser, appointments);
                            folderUpdater.save(new SearchIteratorDelegator<CalendarObject>(appointments), subscription);
                        } while (events.getNextPageToken() != null);
                        return null;
                    }
                });
            }
        } catch (IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        }
        return calObjList;
    }

    private void parseAndAdd(final Events events, final CalendarEventParser parser, final List<CalendarObject> calendarObjectsList) {
        for (Event event : events.getItems()) {
            final CalendarDataObject calenderObject = new CalendarDataObject();
            parser.parseCalendarEvent(event, calenderObject);
            calendarObjectsList.add(calenderObject);
        }
    }
}
