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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
