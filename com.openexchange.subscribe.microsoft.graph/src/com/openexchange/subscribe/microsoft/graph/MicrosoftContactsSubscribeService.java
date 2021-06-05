/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.subscribe.microsoft.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.log.LogProperties;
import com.openexchange.microsoft.graph.contacts.MicrosoftGraphContactsService;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.oauth.AbstractOAuthSubscribeService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.iterator.SearchIteratorDelegator;

/**
 * {@link MicrosoftContactsSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MicrosoftContactsSubscribeService extends AbstractOAuthSubscribeService {

    static final Logger LOG = LoggerFactory.getLogger(MicrosoftContactsSubscribeService.class);
    public static final String SOURCE_ID = KnownApi.MICROSOFT_GRAPH.getServiceId() + ".contact";
    private static final int CHUNK_SIZE = 25;

    /**
     * Initialises a new {@link MicrosoftContactsSubscribeService}.
     *
     * @throws OXException
     */
    public MicrosoftContactsSubscribeService(OAuthServiceMetaData metadata, ServiceLookup services) throws OXException {
        super(metadata, SOURCE_ID, FolderObject.CONTACT, "Outlook.com", services);
    }

    @Override
    protected KnownApi getKnownApi() {
        return KnownApi.MICROSOFT_GRAPH;
    }

    @Override
    public Collection<?> getContent(Subscription subscription) throws OXException {
        Session session = subscription.getSession();
        OAuthAccount oauthAccount = getOAuthAccount(session, subscription);

        FolderUpdaterRegistry folderUpdaterRegistry = getServices().getOptionalService(FolderUpdaterRegistry.class);
        ThreadPoolService threadPool = getServices().getOptionalService(ThreadPoolService.class);
        FolderUpdaterService<Contact> folderUpdater = null == folderUpdaterRegistry ? null : folderUpdaterRegistry.<Contact> getFolderUpdater(subscription);

        if (threadPool == null || folderUpdater == null) {
            return fetchInForeground(oauthAccount);
        }
        scheduleInBackground(threadPool, folderUpdater, oauthAccount, subscription);
        return Collections.emptyList();
    }

    /**
     * Fetches all contacts in the foreground (blocking thread)
     *
     * @param account The {@link OAuthAccount}
     * @return A {@link List} with all fetched contacts
     * @throws OXException
     */
    private List<Contact> fetchInForeground(OAuthAccount account) throws OXException {
        MicrosoftGraphContactsService contactsService = getServices().getService(MicrosoftGraphContactsService.class);
        return contactsService.getContacts(account.getToken());
    }

    /**
     * Schedules a task to fetch all contacts and executes it in the background
     *
     * @param threadPool the {@link ThreadPoolService}
     * @param folderUpdater The {@link FolderUpdaterService}
     * @param account The {@link OAuthAccount}
     * @param subscription The {@link Subscription}
     */
    private void scheduleInBackground(ThreadPoolService threadPool, FolderUpdaterService<Contact> folderUpdater, OAuthAccount account, Subscription subscription) {
        threadPool.submit(new BackgroundTask(folderUpdater, account, subscription));
    }

    ////////////////////////////// NESTED /////////////////////////////

    /**
     * {@link BackgroundTask} - Background task for fetching contacts
     */
    private final class BackgroundTask extends AbstractTask<Void> {

        private final FolderUpdaterService<Contact> folderUpdater;
        private final OAuthAccount account;
        private final Subscription subscription;

        /**
         * Initializes a new {@link BackgroundTask}.
         *
         * @param folderUpdater The folder updated
         * @param account The account
         * @param subscription The subscription
         * @param backgroundTaskMarker The marker
         */
        BackgroundTask(FolderUpdaterService<Contact> folderUpdater, OAuthAccount account, Subscription subscription) {
            this.folderUpdater = folderUpdater;
            this.account = account;
            this.subscription = subscription;
        }

        @Override
        public Void call() throws Exception {
            LogProperties.put(LogProperties.Name.SUBSCRIPTION_ADMIN, "true");
            try {
                MicrosoftGraphContactsService contactsService = getServices().getService(MicrosoftGraphContactsService.class);
                boolean hasMore = false;
                int offset = 0;
                do {
                    List<Contact> contacts = contactsService.getContacts(account.getToken(), CHUNK_SIZE, offset);
                    folderUpdater.save(new SearchIteratorDelegator<Contact>(contacts), subscription);
                    offset += contacts.size();
                    hasMore = !contacts.isEmpty();
                } while (hasMore);
                return null;
            } catch (Exception e) {
                LOG.error("", e);
                throw e;
            } finally {
                LogProperties.remove(LogProperties.Name.SUBSCRIPTION_ADMIN);
            }
        }
    }
}
