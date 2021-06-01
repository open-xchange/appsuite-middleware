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
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.Quota;
import com.openexchange.messaging.Quota.Type;
import com.openexchange.session.Session;

/**
 * {@link TwitterMessagingFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingFolderAccess implements MessagingFolderAccess {

    private static String EMPTY = MessagingFolder.ROOT_FULLNAME;

    private final int id;

    private final int user;

    private final int cid;

    /**
     * Initializes a new {@link TwitterMessagingFolderAccess}.
     */
    public TwitterMessagingFolderAccess(final MessagingAccount account, final Session session) {
        super();
        id = account.getId();
        user = session.getUserId();
        cid = session.getContextId();
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public String createFolder(final MessagingFolder toCreate) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        return EMPTY.equals(folderId);
    }

    @Override
    public MessagingFolder getFolder(final String folderId) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return TwitterMessagingFolder.getInstance(user);
    }

    private static final Quota.Type[] MESSAGE = { Quota.Type.MESSAGE };

    @Override
    public Quota getMessageQuota(final String folder) throws OXException {
        return getQuotas(folder, MESSAGE)[0];
    }

    /**
     * The constant to return or represent an empty path.
     */
    private static final MessagingFolder[] EMPTY_PATH = new MessagingFolder[0];

    @Override
    public MessagingFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return EMPTY_PATH;
    }

    @Override
    public Quota[] getQuotas(final String folderId, final Type[] types) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return Quota.getUnlimitedQuotas(types);
    }

    @Override
    public MessagingFolder getRootFolder() throws OXException {
        return TwitterMessagingFolder.getInstance(user);
    }

    private static final Quota.Type[] STORAGE = { Quota.Type.STORAGE };

    @Override
    public Quota getStorageQuota(final String folder) throws OXException {
        return getQuotas(folder, STORAGE)[0];
    }

    @Override
    public MessagingFolder[] getSubfolders(final String parentFullname, final boolean all) throws OXException {
        if (!EMPTY.equals(parentFullname)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                parentFullname,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return EMPTY_PATH;
    }

    @Override
    public String moveFolder(final String folderId, final String newFullname) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public String updateFolder(final String folderId, final MessagingFolder toUpdate) throws OXException {
        if (!EMPTY.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return null;
    }

    @Override
    public String getSentFolder() throws OXException {
        return null;
    }

    @Override
    public String getSpamFolder() throws OXException {
        return null;
    }

    @Override
    public String getTrashFolder() throws OXException {
        return null;
    }

}
