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

package com.openexchange.messaging.rss;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.MessagingService;
import com.openexchange.rss.utils.RssProperties;
import com.openexchange.session.Session;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;

/**
 * {@link RSSMessagingService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RSSMessagingService implements MessagingService {

    private static final String DISPLAY_NAME = "RSS Feed";
    private static final DynamicFormDescription FORM_DESCRIPTION;
    public static final String ID = "com.openexchange.messaging.rss";

    static {
        final DynamicFormDescription fd = new DynamicFormDescription();
        fd.add(FormElement.input("url", FormStrings.FORM_LABEL_URL));
        FORM_DESCRIPTION = new ReadOnlyDynamicFormDescription(fd);
    }

    private final MessagingAccountManager accountManager;
    private final FeedFetcher fetcher;

    /**
     * Initializes a new {@link RSSMessagingService}.
     *
     * @param rssProperties The RSS properties service
     */
    public RSSMessagingService(RssProperties rssProperties) {
        super();
        accountManager = new ConfigurationCheckingAccountManager(this, rssProperties);
        fetcher = new HttpURLFeedFetcher(HashMapFeedInfoCache.getInstance());
    }

    @Override
    public MessagingAccountAccess getAccountAccess(final int accountId, final Session session) {
        return new RSSFeedOperations(accountId, session, fetcher, accountManager);
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public MessagingAccountTransport getAccountTransport(final int accountId, final Session session) {
        return new RSSFeedOperations(accountId, session, fetcher, accountManager);
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return FORM_DESCRIPTION;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<MessagingAction> getMessageActions() {
        return Collections.emptyList();
    }

    public static int[] getStaticRootPerms() {
        return new int[] {MessagingPermission.READ_FOLDER,
            MessagingPermission.READ_ALL_OBJECTS,
            MessagingPermission.NO_PERMISSIONS,
            MessagingPermission.DELETE_OWN_OBJECTS};
    }

    @Override
    public int[] getStaticRootPermissions() {
        return getStaticRootPerms();
    }

    public static String buildFolderId(final int accountId, final String folder) {
        final StringBuilder stringBuilder = new StringBuilder(ID);
        stringBuilder.append("://").append(accountId).append('/').append(folder);
        return stringBuilder.toString();
    }
}
