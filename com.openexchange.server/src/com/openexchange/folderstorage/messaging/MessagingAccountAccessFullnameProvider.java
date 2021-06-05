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

package com.openexchange.folderstorage.messaging;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingFolderAccess;

/**
 * {@link MessagingAccountAccessFullnameProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingAccountAccessFullnameProvider implements DefaultFolderFullnameProvider {

    /**
     * The connected access instance.
     */
    private final MessagingAccountAccess access;

    /**
     * The folder storage instance.
     */
    private MessagingFolderAccess folderAccess;

    /**
     * Initializes a new {@link MessagingAccountAccessFullnameProvider}.
     *
     * @param access The connected access instance
     */
    public MessagingAccountAccessFullnameProvider(final MessagingAccountAccess access) {
        super();
        this.access = access;
    }

    private MessagingFolderAccess getFolderStorage() throws OXException {
        if (null == folderAccess) {
            folderAccess = access.getFolderAccess();
        }
        return folderAccess;
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return getFolderStorage().getConfirmedHamFolder();
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return getFolderStorage().getConfirmedSpamFolder();
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return getFolderStorage().getDraftsFolder();
    }

    @Override
    public String getINBOXFolder() throws OXException {
        return "INBOX";
    }

    @Override
    public String getSentFolder() throws OXException {
        return getFolderStorage().getSentFolder();
    }

    @Override
    public String getSpamFolder() throws OXException {
        return getFolderStorage().getSpamFolder();
    }

    @Override
    public String getTrashFolder() throws OXException {
        return getFolderStorage().getTrashFolder();
    }

}
