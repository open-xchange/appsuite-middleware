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
 *     Copyright (C) 2018-2020 OX Software GmbH
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.util.ServiceException;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.google.parser.ContactParser;
import com.openexchange.subscribe.oauth.AbstractOAuthSubscribeService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.iterator.SearchIteratorDelegator;

/**
 * {@link GoogleContactsSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GoogleContactsSubscribeService extends AbstractOAuthSubscribeService {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleContactsSubscribeService.class);

    public static final String SOURCE_ID = KnownApi.GOOGLE.getServiceId() + ".contact";

    /**
     * The Google Contacts' feed URL
     */
    private static final String FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full";
    private static final String APP_NAME = "ox-appsuite";
    private static final int CHUNK_SIZE = 25;

    private final ServiceLookup services;

    /**
     * Initialises a new {@link GoogleContactsSubscribeService}.
     * 
     * @param oAuthServiceMetaData The {@link OAuthServiceMetaData}
     * @param services The {@link ServiceLookup}
     */
    public GoogleContactsSubscribeService(OAuthServiceMetaData oauthServiceMetadata, ServiceLookup services) {
        super(oauthServiceMetadata, SOURCE_ID, FolderObject.CONTACT, "Google", services);
        this.services = services;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.SubscribeService#getContent(com.openexchange.subscribe.Subscription)
     */
    @Override
    public Collection<?> getContent(Subscription subscription) throws OXException {
        Session session = subscription.getSession();
        OAuthAccount oauthAccount = GoogleApiClients.reacquireIfExpired(session, true, getOAuthAccount(session, subscription));
        ContactsService googleContactsService = new ContactsService(APP_NAME);
        ContactParser parser = new ContactParser(googleContactsService);
        googleContactsService.setOAuth2Credentials(GoogleApiClients.getCredentials(oauthAccount, session));

        try {
            Query cQuery = new Query(new URL(FEED_URL));
            cQuery.setMaxResults(CHUNK_SIZE);
            ContactFeed feed = googleContactsService.query(cQuery, ContactFeed.class);
            if (CHUNK_SIZE > feed.getTotalResults()) {
                return parser.parseFeed(feed);
            }

            List<Contact> firstBatch = parser.parseFeed(feed);
            int total = feed.getTotalResults();
            int startOffset = firstBatch.size();

            FolderUpdaterRegistry folderUpdaterRegistry = services.getOptionalService(FolderUpdaterRegistry.class);
            ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
            FolderUpdaterService<Contact> folderUpdater = null == folderUpdaterRegistry ? null : folderUpdaterRegistry.<Contact> getFolderUpdater(subscription);
            if (threadPool == null || folderUpdater == null) {
                return fetchInForeground(cQuery, feed, firstBatch, googleContactsService, parser);
            }
            scheduleInBackground(subscription, cQuery, total, startOffset, threadPool, folderUpdater, googleContactsService, parser);
            return firstBatch;
        } catch (IOException e) {
            LOG.error("", e);
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (ServiceException e) {
            LOG.error("", e);
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e, e.getMessage());
        }
    }

    /**
     * Fetches all contacts in the foreground (blocking thread)
     * 
     * @param cQuery The {@link Query}
     * @param feed The {@link ContactFeed} with the first batch
     * @param firstBatch The first batch of contacts
     * @return A {@link List} with all fetched contacts
     * @throws IOException if an I/O error is occurred
     * @throws ServiceException if a remote service error is occurred
     */
    private List<Contact> fetchInForeground(Query cQuery, ContactFeed feed, List<Contact> firstBatch, ContactsService googleContactsService, ContactParser parser) throws IOException, ServiceException {
        int total = feed.getTotalResults();
        int offset = firstBatch.size();

        List<Contact> contacts = new ArrayList<Contact>(total);
        contacts.addAll(firstBatch);

        while (total > offset) {
            cQuery.setStartIndex(offset);
            feed = googleContactsService.query(cQuery, ContactFeed.class);
            List<Contact> batch = parser.parseFeed(feed);
            contacts.addAll(batch);
            offset += batch.size();
        }

        return contacts;
    }

    /**
     * Schedules a task to fetch all contacts and executes it in the background
     * 
     * @param subscription The {@link Subscription}
     * @param cQuery The {@link Query}
     * @param total The amount of all contacts
     * @param startOffset The stating 1-based index offset
     * @param threadPool the {@link ThreadPoolService}
     * @param folderUpdater The {@link FolderUpdaterService}
     */
    private void scheduleInBackground(Subscription subscription, Query cQuery, int total, int startOffset, ThreadPoolService threadPool, FolderUpdaterService<Contact> folderUpdater, ContactsService googleContactsService, ContactParser parser) {
        // Schedule task for remainder...
        threadPool.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                int offset = startOffset;
                while (total > offset) {
                    cQuery.setStartIndex(offset);
                    ContactFeed feed = googleContactsService.query(cQuery, ContactFeed.class);
                    List<Contact> batch = parser.parseFeed(feed);
                    folderUpdater.save(new SearchIteratorDelegator<Contact>(batch), subscription);
                    offset += batch.size();
                }
                return null;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.oauth.AbstractOAuthSubscribeService#getKnownApi()
     */
    @Override
    protected KnownApi getKnownApi() {
        return KnownApi.GOOGLE;
    }
}
