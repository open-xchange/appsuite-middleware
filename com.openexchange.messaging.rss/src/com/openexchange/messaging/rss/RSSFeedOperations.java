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

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.session.Session;
import com.sun.syndication.fetcher.FeedFetcher;


/**
 * {@link RSSFeedOperations}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RSSFeedOperations implements MessagingAccountAccess, MessagingAccountTransport {

    private final int accountId;
    private final RSSMessageAccess messageAccess;
    private final RSSFolderAccess folderAccess;
    private boolean connected;

    public RSSFeedOperations(final int accountId, final Session session, final FeedFetcher fetcher, final MessagingAccountManager accounts) {
        super();
        this.accountId = accountId;

        folderAccess = new RSSFolderAccess(accountId, session);
        messageAccess = new RSSMessageAccess(accountId, session, fetcher, accounts);
    }

    @Override
    public int getAccountId() {
        return accountId;
    }

    @Override
    public MessagingFolderAccess getFolderAccess() throws OXException {
        return folderAccess;
    }

    @Override
    public MessagingMessageAccess getMessageAccess() throws OXException {
        return messageAccess;
    }

    @Override
    public MessagingFolder getRootFolder() throws OXException {
        return folderAccess.getRootFolder();
    }

    @Override
    public void close() {
        connected = false;
    }

    @Override
    public void connect() throws OXException {
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean ping() throws OXException {
        return true;
    }

    @Override
    public void transport(final MessagingMessage message, final Collection<MessagingAddressHeader> recipients) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public boolean cacheable() {
        // Nothing to cache
        return false;
    }

}
