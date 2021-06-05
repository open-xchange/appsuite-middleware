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

package com.openexchange.mail.json.actions;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageEnhanced;
import com.openexchange.mail.api.IMailFolderStorageEnhanced2;
import com.openexchange.mail.dataobjects.MailFolder;

/**
 * {@link FolderInfo} - Simple class to hold folder information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
class FolderInfo {

    /**
     * Yields the folder info for given arguments.
     *
     * @param fullName The full name; e.g. "INBOX/My Folder"
     * @param folderStorage The associated folder storage
     * @return The folder info
     * @throws OXException If folder info cannot be returned
     */
    static FolderInfo getFolderInfo(String fullName, IMailFolderStorage folderStorage) throws OXException {
        IMailFolderStorageEnhanced2 storageEnhanced2 = folderStorage.supports(IMailFolderStorageEnhanced2.class);
        if (null != storageEnhanced2) {
            int[] totalAndUnread = storageEnhanced2.getTotalAndUnreadCounter(fullName);
            return new FolderInfo(totalAndUnread[0], totalAndUnread[1]);
        }

        IMailFolderStorageEnhanced storageEnhanced = folderStorage.supports(IMailFolderStorageEnhanced.class);
        if (null != storageEnhanced) {
            int total = storageEnhanced.getTotalCounter(fullName);
            int unread = storageEnhanced.getUnreadCounter(fullName);
            return new FolderInfo(total, unread);
        }

        MailFolder folder = folderStorage.getFolder(fullName);
        return new FolderInfo(folder.getMessageCount(), folder.getUnreadMessageCount());
    }

    // --------------------------------------------------------------------------------------------------------------

    final int total;
    final int unread;

    FolderInfo(int total, int unread) {
        super();
        this.total = total;
        this.unread = unread;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("{total=").append(total).append(", unread=").append(unread).append('}').toString();
    }

}
