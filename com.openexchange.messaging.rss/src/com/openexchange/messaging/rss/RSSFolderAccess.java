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

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.Quota;
import com.openexchange.messaging.Quota.Type;
import com.openexchange.session.Session;

/**
 * {@link RSSFolderAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RSSFolderAccess extends RSSCommon implements MessagingFolderAccess {

    public RSSFolderAccess(final int accountId, final Session session) {
        super(accountId, session);
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        checkFolder(folderId);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        checkFolder(folderId);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String createFolder(final MessagingFolder toCreate) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        checkFolder(folderId);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        checkFolder(folderId);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public boolean exists(final String folderId) {
        return EMPTY.equals(folderId);
    }

    @Override
    public String getConfirmedHamFolder() {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() {
        return null;
    }

    @Override
    public String getDraftsFolder() {
        return null;
    }

    @Override
    public MessagingFolder getFolder(final String folderId) throws OXException {
        checkFolder(folderId);
        return new RSSFolder(session.getUserId());
    }

    private static final Quota.Type[] MESSAGE = { Quota.Type.MESSAGE };

    @Override
    public Quota getMessageQuota(final String folderId) throws OXException {
        checkFolder(folderId);
        return getQuotas(folderId, MESSAGE)[0];
    }

    private static final MessagingFolder[] EMPTY_PATH = new MessagingFolder[0];

    @Override
    public MessagingFolder[] getPath2DefaultFolder(final String folderId) {
        return EMPTY_PATH;
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        checkFolder(folder);
        return Quota.getUnlimitedQuotas(types);
    }

    @Override
    public MessagingFolder getRootFolder() {
        return new RSSFolder(session.getUserId());
    }

    @Override
    public String getSentFolder() {
        return null;
    }

    @Override
    public String getSpamFolder() {
        return null;
    }

    private static final Quota.Type[] STORAGE = { Quota.Type.STORAGE };

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        checkFolder(folderId);
        return getQuotas(folderId, STORAGE)[0];
    }

    @Override
    public MessagingFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        checkFolder(parentIdentifier);
        return new MessagingFolder[0];
    }

    @Override
    public String getTrashFolder() throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String updateFolder(final String identifier, final MessagingFolder toUpdate) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

}
