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

package com.openexchange.folderstorage.mail;

import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;

/**
 * {@link MailFolderType} - The folder type for mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderType implements FolderType {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailFolderType.class);

    private static final int LEN = MailFolder.MAIL_PREFIX.length();

    private static final MailFolderType instance = new MailFolderType();

    /**
     * Gets the {@link MailFolderType} instance.
     *
     * @return The {@link MailFolderType} instance
     */
    public static MailFolderType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link MailFolderType}.
     */
    private MailFolderType() {
        super();
    }

    @Override
    public boolean servesTreeId(final String treeId) {
        return FolderStorage.REAL_TREE_ID.equals(treeId);
    }

    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    @Override
    public boolean servesFolderId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        return checkFolderId(folderId);
    }

    @Override
    public boolean servesParentId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        if (PRIVATE_FOLDER_ID.equals(folderId)) {
            return true;
        }
        return checkFolderId(folderId);
    }

    private boolean checkFolderId(final String folderId) {
        if (!folderId.startsWith(MailFolder.MAIL_PREFIX)) {
            return false;
        }

        int len = folderId.length();
        char separator = MailProperties.getInstance().getDefaultSeparator();
        int index = LEN;
        while (index < len && folderId.charAt(index) != separator) {
            index++;
        }

        // Parse account ID
        if (index != LEN) {
            try {
                Integer.parseInt(folderId.substring(LEN, index));
            } catch (NumberFormatException e) {
                final IllegalArgumentException err = new IllegalArgumentException("Mail account is not a number: " + folderId);
                err.initCause(e);
                LOG.warn("Ignoring invalid folder identifier", err);
                return false;
            }
        }
        return true;
    }

}
