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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ExecutionError;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.log.LogProperties;
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
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.1
 */
public class GoogleContactsSubscribeService extends AbstractOAuthSubscribeService {

    /** The logger constant */
    static final Logger LOG = LoggerFactory.getLogger(GoogleContactsSubscribeService.class);

    public static final String SOURCE_ID = KnownApi.GOOGLE.getServiceId() + ".contact";
    private static final int SELF_PROTECTION_LIMIT = 1000000;

    private final String APP_NAME = "ox-appsuite";
    private final String RESSOURCE = "people/me";
    private final String PERSON_FIELDS = PersonFields.create(
        PersonFields.ADDRESSES,
        PersonFields.BIRTHDAYS,
        PersonFields.EMAIL_ADDRESSES,
        PersonFields.IM_CLIENTS,
        PersonFields.NAMES,
        PersonFields.NICKNAMES,
        PersonFields.OCCUPATIONS,
        PersonFields.PHONE_NUMBERS,
        PersonFields.PHOTOS);
    private final int CHUNK_SIZE = 25;

    private final Cache<String, PeopleService> peopleServiceCache;

    private final ServiceLookup services;

    /**
     * Initialises a new {@link GoogleContactsSubscribeService}.
     *
     * @param oAuthServiceMetaData The {@link OAuthServiceMetaData}
     * @param services The {@link ServiceLookup}
     * @throws OXException
     */
    public GoogleContactsSubscribeService(OAuthServiceMetaData oauthServiceMetadata, ServiceLookup services) throws OXException {
        super(oauthServiceMetadata, SOURCE_ID, FolderObject.CONTACT, "Google", services);
        this.services = services;
        this.peopleServiceCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();
    }

    @Override
    public Collection<?> getContent(Subscription subscription) throws OXException {
        Session session = subscription.getSession();
        try {
            PeopleService googlePeopleService = getPeopleService(session, getOAuthAccount(session, subscription));
            ConfigurationService configService = services.getServiceSafe(ConfigurationService.class);
            int maxImageSize = configService.getIntProperty("max_image_size", 4194304);
            Locale locale = subscription.getSession().getUser().getLocale();
            I18nService i18nService = services.getServiceSafe(I18nServiceRegistry.class).getI18nService(locale);
            ContactParser parser = new ContactParser(googlePeopleService, i18nService,  maxImageSize);
            // @formatter:off
            ListConnectionsResponse response = googlePeopleService.people()
                                                                  .connections()
                                                                  .list(RESSOURCE)
                                                                  .setPageSize(I(CHUNK_SIZE))
                                                                  .setPersonFields(PERSON_FIELDS)
                                                                  .execute();
            // @formatter:on

            Integer totalPeople = response.getTotalPeople();
            String nextPageToken = response.getNextPageToken();
            List<Contact> firstBatch = parser.parseListConnectionsResponse(response);
            if (nextPageToken == null) {
                return firstBatch;
            }
            LOG.debug("Parsed first batch with size {} of {} contacts for Google contact subscription for user {} in context {}", I(firstBatch.size()), totalPeople, I(subscription.getSession().getUserId()), I(subscription.getSession().getContextId()));

            FolderUpdaterRegistry folderUpdaterRegistry = services.getOptionalService(FolderUpdaterRegistry.class);
            ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
            FolderUpdaterService<Contact> folderUpdater = null == folderUpdaterRegistry ? null : folderUpdaterRegistry.<Contact> getFolderUpdater(subscription);
            if (null == threadPool || null == folderUpdater) {
                return fetchInForeground(googlePeopleService, subscription, firstBatch, totalPeople, nextPageToken, parser);
            }
            scheduleInBackground(googlePeopleService, subscription, nextPageToken, threadPool, folderUpdater, parser);
            return firstBatch;
        } catch (IOException e) {
            LOG.error("", e);
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Pings the people service
     *
     * @param session The {@link Session}
     * @param oauthAccount The {@link OAuthAccount}
     * @throws OXException
     */
    public void ping(Session session, OAuthAccount account) throws OXException {
        try {
            getPeopleService(session, account)
                .people()
                .connections()
                .list(RESSOURCE)
                .setPageSize(I(1))
                .setPersonFields(PersonFields.NAMES.getName())
                .execute();
        } catch (IOException e) {
            LOG.error("", e);
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        }

    }

    /**
     * Gets the {@link PeopleService} for the specified oauth account
     *
     * @param session The {@link Session}
     * @param oauthAccount The {@link OAuthAccount}
     * @return The {@link ContactsService}
     * @throws OXException if the {@link ContactsService} cannot be initialized
     */
    private PeopleService getPeopleService(Session session, OAuthAccount oauthAccount) throws OXException {
        try {
            return peopleServiceCache.get(getKey(session, oauthAccount), () -> createPeopleService(session, oauthAccount));
        } catch (ExecutionError | ExecutionException e) {
            if (e.getCause() != null) {
                throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e.getCause(), e.getCause().getMessage());
            }
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the cache key
     *
     * @return
     */
    private String getKey(Session session, OAuthAccount account) {
        return session.getContextId() + "_" + session.getUserId() + "_" + account.getId();
    }

    /**
     * Creates a new {@link PeopleService} for the specified oauth account
     *
     * @param session The {@link Session}
     * @param oauthAccount The {@link OAuthAccount}
     * @return The {@link ContactsService}
     * @throws OXException if the {@link ContactsService} cannot be initialized
     * @throws GeneralSecurityException In case an error occurred while creating the transport
     * @throws IOException In case an error occurred while creating the transport
     */
    private PeopleService createPeopleService(Session session, OAuthAccount oauthAccount) throws OXException, GeneralSecurityException, IOException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        // @formatter:off
        return new PeopleService.Builder(HTTP_TRANSPORT,
                                         JSON_FACTORY,
                                         GoogleApiClients.getCredentials(oauthAccount, session))
                                .setApplicationName(APP_NAME)
                                .build();
        // @formatter:on
    }

    /**
     * Fetches all contacts in the foreground (blocking thread)
     *
     * @param googlePeopleService The {@link PeopleService}
     * @param firstBatch The first batch of contacts
     * @param totalPeople The amount of all contacts
     * @param nextPageToken The token to fetch the next batch
     * @param parser The {@link PeopleParser} to parse the response
     * @return A {@link List} with all fetched contacts
     * @throws IOException if an I/O error is occurred
     */
    private List<Contact> fetchInForeground(PeopleService googlePeopleService, Subscription subscription, List<Contact> firstBatch, Integer totalPeople, String nextPageToken, ContactParser parser) throws IOException {
        List<Contact> contacts = new ArrayList<>(i(totalPeople));
        contacts.addAll(firstBatch);
        fetchContacts(googlePeopleService, nextPageToken, subscription, response -> {
            contacts.addAll(parser.parseListConnectionsResponse(response));
        });
        return contacts;
    }

    /**
     * Schedules a task to fetch all contacts and executes it in the background
     *
     * @param googlePeopleService The {@link PeopleService}
     * @param subscription The {@link Subscription}
     * @param nextPageToken The token to fetch the next batch
     * @param threadPool The {@link ThreadPoolService}
     * @param folderUpdater The {@link FolderUpdaterService}
     * @param parser The {@link PeopleParser} to parse the response
     */
    private void scheduleInBackground(PeopleService googlePeopleService, Subscription subscription, String nextPageToken, ThreadPoolService threadPool, FolderUpdaterService<Contact> folderUpdater, ContactParser parser) {
        threadPool.submit(new AbstractTask<Void>() {
            @Override
            public Void call() throws Exception {
                Integer iUserId = I(subscription.getSession().getUserId());
                Integer iContextId = I(subscription.getSession().getContextId());
                LogProperties.put(LogProperties.Name.SUBSCRIPTION_ADMIN, "true");
                fetchContacts(googlePeopleService, nextPageToken, subscription, response -> {
                    List<Contact> nextBatch = parser.parseListConnectionsResponse(response);
                    try {
                        folderUpdater.save(new SearchIteratorDelegator<>(nextBatch), subscription);
                        LOG.debug("Stored next batch with size {} for Google contact subscription for user {} in context {}", I(nextBatch.size()), iUserId, iContextId);
                    } catch (Exception e) {
                        LOG.error("Failed storing {} contacts for Google contact subscription for user {} in context {}", I(nextBatch.size()), iUserId, iContextId, e);
                    } finally {
                        LogProperties.remove(LogProperties.Name.SUBSCRIPTION_ADMIN);
                    }
                });
                return null;
            }
        });
    }

    /**
     * Fetches all contacts
     *
     * @param googlePeopleService The {@link PeopleService}
     * @param nextPageToken The token to fetch the next batch
     * @param responseHandler A {@link Consumer} for handling the response
     * @throws IOException if an I/O error is occurred
     */
    protected void fetchContacts(PeopleService googlePeopleService, String nextPageToken, Subscription subscription, Consumer<ListConnectionsResponse> responseHandler) throws IOException {
        String pageToken = nextPageToken;
        int contactCount = 0;
        while (pageToken != null) {
            if (contactCount > SELF_PROTECTION_LIMIT) {
                LOG.warn("Stopped fetching google contacts for user {} in context {} because there are too many contacts.", I(subscription.getUserId()), I(subscription.getContext().getContextId()));
                return;
            }
            ListConnectionsResponse response = googlePeopleService.people().connections()
                .list(RESSOURCE)
                .setPageSize(I(CHUNK_SIZE))
                .setPageToken(pageToken)
                .setPersonFields(PERSON_FIELDS)
                .execute();
            pageToken = response.getNextPageToken();
            contactCount += response.size();
            responseHandler.accept(response);
        }
    }

    @Override
    protected KnownApi getKnownApi() {
        return KnownApi.GOOGLE;
    }
}
