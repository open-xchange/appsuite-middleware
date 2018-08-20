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
import com.openexchange.microsoft.graph.MicrosoftGraphContactsService;
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
 * @since v7.10.1
 */
public class MicrosoftContactsSubscribeService extends AbstractOAuthSubscribeService {

    private static final Logger LOG = LoggerFactory.getLogger(MicrosoftContactsSubscribeService.class);
    private static final String SOURCE_ID = KnownApi.MICROSOFT_GRAPH.getServiceId() + ".contact";
    private static final int CHUNK_SIZE = 25;

    /**
     * Initialises a new {@link MicrosoftContactsSubscribeService}.
     */
    public MicrosoftContactsSubscribeService(OAuthServiceMetaData metadata, ServiceLookup services) {
        super(metadata, SOURCE_ID, FolderObject.CONTACT, "Microsoft Graph", services);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.oauth.AbstractOAuthSubscribeService#getKnownApi()
     */
    @Override
    protected KnownApi getKnownApi() {
        return KnownApi.MICROSOFT_GRAPH;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.SubscribeService#getContent(com.openexchange.subscribe.Subscription)
     */
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
        threadPool.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
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
                } catch (Exception e) {
                    LOG.error("", e);
                    throw e;
                }
                return null;
            }
        });
    }
}
