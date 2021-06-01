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

package com.openexchange.messaging.twitter;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.session.Session;

/**
 * {@link TwitterMessagingAccountAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingAccountAccess extends AbstractTwitterMessagingAccess implements MessagingAccountAccess {

    private MessagingFolderAccess folderAccess;

    private MessagingMessageAccess messageAccess;

    /**
     * Initializes a new {@link TwitterMessagingAccountAccess}.
     *
     * @throws OXException If initialization fails
     */
    public TwitterMessagingAccountAccess(final MessagingAccount account, final Session session) throws OXException {
        super(account, session);
    }

    @Override
    public int getAccountId() {
        return account.getId();
    }

    @Override
    public MessagingFolderAccess getFolderAccess() throws OXException {
        if (null == folderAccess) {
            folderAccess = new TwitterMessagingFolderAccess(account, session);
        }
        return folderAccess;
    }

    @Override
    public MessagingMessageAccess getMessageAccess() throws OXException {
        if (null == messageAccess) {
            messageAccess = new TwitterMessagingMessageAccess(twitterAccess, account, session);
        }
        return messageAccess;
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
    public MessagingFolder getRootFolder() throws OXException {
        return getFolderAccess().getRootFolder();
    }

    @Override
    public boolean cacheable() {
        return true;
    }

}
