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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.mail;

import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SystemContentType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.type.MailType;
import com.openexchange.folderstorage.type.SystemType;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.utils.MailFolderUtility;

/**
 * {@link MailFolderImpl} - A mail folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderImpl extends AbstractFolder {

    private static final long serialVersionUID = 6445442372690458946L;

    /**
     * Initializes an empty {@link MailFolderImpl}.
     */
    public MailFolderImpl() {
        super();
    }

    /**
     * Initializes a new {@link MailFolderImpl} from given mail folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     * 
     * @param mailFolder The underlying mail folder
     * @param accountId The account identifier
     * @param capabilities The capabilities
     */
    public MailFolderImpl(final MailFolder mailFolder, final int accountId, final int capabilities) {
        super();
        this.id = MailFolderUtility.prepareFullname(accountId, mailFolder.getFullname());
        this.name = mailFolder.getName();
        // FolderObject.SYSTEM_PRIVATE_FOLDER_ID
        this.parent = mailFolder.isRootFolder() ? String.valueOf(1) : MailFolderUtility.prepareFullname(
            accountId,
            mailFolder.getParentFullname());
        final MailPermission[] mailPermissions = mailFolder.getPermissions();
        this.permissions = new Permission[mailPermissions.length];
        for (int i = 0; i < mailPermissions.length; i++) {
            this.permissions[i] = new MailPermissionImpl(mailPermissions[i]);
        }
        type = SystemType.getInstance();
        this.subscribed = mailFolder.isSubscribed();
        this.capabilities = capabilities;
        {
            final String value = mailFolder.isRootFolder() ? "" : new StringBuilder(16).append('(').append(mailFolder.getMessageCount()).append(
                '/').append(mailFolder.getUnreadMessageCount()).append(')').toString();
            this.summary = value;
        }
        this.deefault = /* mailFolder.isDefaultFolder(); */0 == accountId && "INBOX".equals(mailFolder.getFullname());
        this.total = mailFolder.getMessageCount();
        this.nu = mailFolder.getNewMessageCount();
        this.unread = mailFolder.getUnreadMessageCount();
        this.deleted = mailFolder.getDeletedMessageCount();
    }

    @Override
    public ContentType getContentType() {
        return SystemContentType.getInstance();
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

    public boolean isGlobalID() {
        return false;
    }

}
