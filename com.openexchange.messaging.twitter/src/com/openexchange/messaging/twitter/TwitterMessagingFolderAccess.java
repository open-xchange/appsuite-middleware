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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import com.openexchange.messaging.MessagingAccount;
import com.openexchange.exception.OXException;
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

    public void clearFolder(final String folderId) throws MessagingException {
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

    public void clearFolder(final String folderId, final boolean hardDelete) throws MessagingException {
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

    public String createFolder(final MessagingFolder toCreate) throws MessagingException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    public String deleteFolder(final String folderId) throws MessagingException {
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

    public String deleteFolder(final String folderId, final boolean hardDelete) throws MessagingException {
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

    public boolean exists(final String folderId) throws MessagingException {
        return EMPTY.equals(folderId);
    }

    public MessagingFolder getFolder(final String folderId) throws MessagingException {
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

    public Quota getMessageQuota(final String folder) throws MessagingException {
        return getQuotas(folder, MESSAGE)[0];
    }

    /**
     * The constant to return or represent an empty path.
     */
    private static final MessagingFolder[] EMPTY_PATH = new MessagingFolder[0];

    public MessagingFolder[] getPath2DefaultFolder(final String folderId) throws MessagingException {
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

    public Quota[] getQuotas(final String folderId, final Type[] types) throws MessagingException {
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

    public MessagingFolder getRootFolder() throws MessagingException {
        return TwitterMessagingFolder.getInstance(user);
    }

    private static final Quota.Type[] STORAGE = { Quota.Type.STORAGE };

    public Quota getStorageQuota(final String folder) throws MessagingException {
        return getQuotas(folder, STORAGE)[0];
    }

    public MessagingFolder[] getSubfolders(final String parentFullname, final boolean all) throws MessagingException {
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

    public String moveFolder(final String folderId, final String newFullname) throws MessagingException {
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

    public String renameFolder(final String folderId, final String newName) throws MessagingException {
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

    public String updateFolder(final String folderId, final MessagingFolder toUpdate) throws MessagingException {
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

    public String getConfirmedHamFolder() throws MessagingException {
        return null;
    }

    public String getConfirmedSpamFolder() throws MessagingException {
        return null;
    }

    public String getDraftsFolder() throws MessagingException {
        return null;
    }

    public String getSentFolder() throws MessagingException {
        return null;
    }

    public String getSpamFolder() throws MessagingException {
        return null;
    }

    public String getTrashFolder() throws MessagingException {
        return null;
    }

}
