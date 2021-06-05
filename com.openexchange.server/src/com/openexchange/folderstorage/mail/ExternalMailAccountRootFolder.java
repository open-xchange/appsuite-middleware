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

import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.type.MailType;
import com.openexchange.folderstorage.type.SystemType;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExternalMailAccountRootFolder} - A mail folder especially for root folder of an external account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ExternalMailAccountRootFolder extends AbstractFolder {

    private static final long serialVersionUID = -7259106085690350497L;

    protected final com.openexchange.folderstorage.mail.MailFolderImpl.MailFolderType mailFolderType;

    protected final int userId;

    protected final int contexctId;

    /**
     * Initializes a new {@link ExternalMailAccountRootFolder} from given mail account.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     *
     * @param mailAccount The underlying mail account
     * @param mailConfig The mail configuration
     * @param session The session
     * @throws OXException If creation fails
     */
    public ExternalMailAccountRootFolder(final MailAccount mailAccount, /*final MailConfig mailConfig,*/ final ServerSession session) throws OXException {
        super();
        userId = session.getUserId();
        contexctId = session.getContextId();
        final String fullname = MailFolder.ROOT_FOLDER_ID;
        id = MailFolderUtility.prepareFullname(mailAccount.getId(), fullname);
        accountId = id;
        /*
         * Set proper name
         */
        boolean isUnifiedInbox = UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol());
        if (isUnifiedInbox) {
            name = StringHelper.valueOf(session.getUser().getLocale()).getString(MailStrings.UNIFIED_MAIL);
        } else {
            name = mailAccount.getName();
        }
        parent = FolderStorage.PRIVATE_ID;
        final MailPermissionImpl mp = new MailPermissionImpl();
        mp.setEntity(userId);
        if (isUnifiedInbox) {
            mp.setAllPermissions(Permission.CREATE_OBJECTS_IN_FOLDER, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS);
        } else {
            mp.setAllPermissions(Permission.CREATE_SUB_FOLDERS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS);
        }
        mp.setAdmin(false);
        permissions = new Permission[] { mp };
        type = SystemType.getInstance();
        subscribed = true;
        subscribedSubfolders = true;
        this.capabilities = com.openexchange.mail.api.MailCapabilities.EMPTY_CAPS.getCapabilities(); //mailConfig.getCapabilities().getCapabilities();
        summary = "";
        deefault = false;
        total = 0;
        nu = 0;
        unread = 0;
        deleted = 0;
        mailFolderType = com.openexchange.folderstorage.mail.MailFolderImpl.MailFolderType.ROOT;
        bits = createPermissionBits(
            mp.getFolderPermission(),
            mp.getReadPermission(),
            mp.getWritePermission(),
            mp.getDeletePermission(),
            mp.isAdmin());

    }

    private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };

    static int createPermissionBits(final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) throws OXException {
        final int[] perms = new int[5];
        perms[0] = fp == MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : fp;
        perms[1] = orp == MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : orp;
        perms[2] = owp == MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : owp;
        perms[3] = odp == MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : odp;
        perms[4] = adminFlag ? 1 : 0;
        return createPermissionBits(perms);
    }

    /**
     * The actual max permission that can be transfered in field 'bits' or JSON's permission object
     */
    private static final int MAX_PERMISSION = 64;

    private static int createPermissionBits(final int[] permission) throws OXException {
        int retval = 0;
        boolean first = true;
        for (int i = permission.length - 1; i >= 0; i--) {
            final int shiftVal = (i * 7); // Number of bits to be shifted
            if (first) {
                retval += permission[i] << shiftVal;
                first = false;
            } else {
                if (permission[i] == OCLPermission.ADMIN_PERMISSION) {
                    retval += MAX_PERMISSION << shiftVal;
                } else {
                    try {
                        retval += mapping[permission[i]] << shiftVal;
                    } catch (Exception e) {
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                }
            }
        }
        return retval;
    }

    @Override
    public ExternalMailAccountRootFolder clone() {
        return (ExternalMailAccountRootFolder) super.clone();
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public ContentType getContentType() {
        return mailFolderType.getContentType();
    }

    @Override
    public int getDefaultType() {
        return mailFolderType.getType();
    }

    @Override
    public void setDefaultType(final int defaultType) {
        // Nothing to do
    }

    @Override
    public Type getType() {
        return MailType.getInstance();
    }

    @Override
    public void setContentType(final ContentType contentType) {
        // Nothing to do
    }

    @Override
    public void setType(final Type type) {
        // Nothing to do
    }

    @Override
    public boolean isGlobalID() {
        return false;
    }

}
