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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.smslmms.api;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.DefaultMessagingFolder;
import com.openexchange.messaging.DefaultMessagingPermission;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolder.DefaultFolderType;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.Quota;
import com.openexchange.messaging.Quota.Type;
import com.openexchange.session.Session;


/**
 * {@link DefaultSMSFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultSMSFolderAccess implements MessagingFolderAccess {

    private final String rootFolderId;

    private final MessagingFolder rootFolder;

    private final int accountId;

    private final Session session;

    private final SMSMessageAccess smsMessageAccess;

    /**
     * Initializes a new {@link DefaultSMSFolderAccess}.
     */
    public DefaultSMSFolderAccess(final SMSMessageAccess smsMessageAccess, final int accountId, final Session session) {
        super();
        this.smsMessageAccess = smsMessageAccess;
        this.accountId = accountId;
        this.session = session;
        rootFolderId = MessagingFolder.ROOT_FULLNAME;
        final DefaultMessagingFolder rootFolder = new DefaultMessagingFolder();
        rootFolder.setCapabilities(Collections.<String> emptySet());
        rootFolder.setDefaultFolder(false);
        rootFolder.setDefaultFolderType(DefaultFolderType.NONE);
        rootFolder.setDeletedMessageCount(0);
        rootFolder.setExists(true);
        rootFolder.setHoldsFolders(false);
        rootFolder.setHoldsMessages(true);
        rootFolder.setId(rootFolderId);
        rootFolder.setMessageCount(-1);
        rootFolder.setName(rootFolderId);
        rootFolder.setNewMessageCount(-1);
        final DefaultMessagingPermission p = DefaultMessagingPermission.newInstance();
        rootFolder.setOwnPermission(p);
        rootFolder.setParentId(null);
        rootFolder.setPermissions(Collections.<MessagingPermission> singletonList(p));
        rootFolder.setRootFolder(true);
        rootFolder.setSeparator('/');
        rootFolder.setSubfolders(false);
        rootFolder.setSubscribed(true);
        rootFolder.setSubscribedSubfolders(false);
        rootFolder.setUnreadMessageCount(-1);
        this.rootFolder = rootFolder;
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        return rootFolderId.equals(folderId);
    }

    @Override
    public MessagingFolder getFolder(final String folderId) throws OXException {
        if (!rootFolderId.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(folderId, Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
        return rootFolder;
    }

    @Override
    public MessagingFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        if (!rootFolderId.equals(parentIdentifier)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(parentIdentifier, Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
        return new MessagingFolder[0];
    }

    @Override
    public MessagingFolder getRootFolder() throws OXException {
        return rootFolder;
    }

    @Override
    public String createFolder(final MessagingFolder toCreate) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String updateFolder(final String identifier, final MessagingFolder toUpdate) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        clearFolder(folderId, false);
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        if (!rootFolderId.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(folderId, Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
        final List<SMSMessage> messages = smsMessageAccess.getAllSMSMessages(folderId, null, null, null, MessagingField.ID);
        final String[] ids = new String[messages.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = messages.get(i).getId();
        }
        smsMessageAccess.deleteMessages(folderId, ids, hardDelete);
    }

    @Override
    public MessagingFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        if (!rootFolderId.equals(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(folderId, Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
        return new MessagingFolder[0];
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        return Quota.getUnlimitedQuota(Quota.Type.STORAGE);
    }

    @Override
    public Quota getMessageQuota(final String folderId) throws OXException {
        return Quota.getUnlimitedQuota(Quota.Type.MESSAGE);
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        return Quota.getUnlimitedQuotas(types);
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String getDraftsFolder() throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String getSpamFolder() throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String getSentFolder() throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public String getTrashFolder() throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

}
