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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.messaging.mail;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.messaging.DefaultMessagingFolder;
import com.openexchange.messaging.DefaultMessagingPermission;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolder.DefaultFolderType;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.Quota;
import com.openexchange.messaging.Quota.Type;
import com.openexchange.session.Session;

/**
 * {@link MailMessagingFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18
 */
public final class MailMessagingFolderAccess implements MessagingFolderAccess {

    private final IMailFolderStorage folderStorage;

    private final int accountId;

    private final Session session;

    private final MailCapabilities caps;

    /**
     * Initializes a new {@link MailMessagingFolderAccess}.
     *
     * @param mailAccess The mail folder storage
     * @param accountId The account ID
     * @param caps The capabilities
     * @param session The session providing user data
     */
    public MailMessagingFolderAccess(final IMailFolderStorage folderStorage, final int accountId, final MailCapabilities caps, final Session session) {
        super();
        this.folderStorage = folderStorage;
        this.accountId = accountId;
        this.session = session;
        this.caps = caps;
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        try {
            folderStorage.clearFolder(folderId);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        try {
            folderStorage.clearFolder(folderId, hardDelete);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String createFolder(final MessagingFolder toCreate) throws OXException {
        try {
            final MailFolderDescription mfd = new MailFolderDescription();

            mfd.setExists(false);
            mfd.setAccountId(accountId);
            mfd.setName(toCreate.getName());
            mfd.setParentAccountId(accountId);
            mfd.setParentFullname(toCreate.getParentId());
            mfd.setSeparator(toCreate.getSeparator());
            mfd.setSubscribed(toCreate.isSubscribed());

            addMailPermissions(toCreate, mfd);

            return folderStorage.createFolder(mfd);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        try {
            return folderStorage.deleteFolder(folderId);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        try {
            return folderStorage.deleteFolder(folderId, hardDelete);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        try {
            return folderStorage.exists(folderId);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        try {
            return folderStorage.getConfirmedHamFolder();
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        try {
            return folderStorage.getConfirmedSpamFolder();
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String getDraftsFolder() throws OXException {
        try {
            return folderStorage.getDraftsFolder();
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public MessagingFolder getFolder(final String folderId) throws OXException {
        try {
            final MailFolder folder = folderStorage.getFolder(folderId);

            final DefaultMessagingFolder dmf = convert2MessagingFolder(folder, caps);
            return dmf;
        } catch (final OXException e) {
            throw e;
        }
    }

    static DefaultMessagingFolder convert2MessagingFolder(final MailFolder folder, final MailCapabilities caps) {
        final DefaultMessagingFolder dmf = new DefaultMessagingFolder();
        {
            final Set<String> capabilities = new HashSet<String>();
            if (caps.hasPermissions()) {
                capabilities.add(MessagingFolder.CAPABILITY_PERMISSIONS);
            }
            if (caps.hasQuota()) {
                capabilities.add(MessagingFolder.CAPABILITY_QUOTA);
            }
            if (caps.hasSort()) {
                capabilities.add(MessagingFolder.CAPABILITY_SORT);
            }
            if (caps.hasSubscription()) {
                capabilities.add(MessagingFolder.CAPABILITY_SUBSCRIPTION);
            }
            if (caps.hasThreadReferences()) {
                capabilities.add(MailConstants.CAPABILITY_THREAD_REFERENCES);
            }
            if (folder.isSupportsUserFlags()) {
                capabilities.add(MessagingFolder.CAPABILITY_USER_FLAGS);
            }
            dmf.setCapabilities(capabilities);
        }
        dmf.setDefaultFolder(folder.isDefaultFolder());
        dmf.setDefaultFolderType(getDefaultFolderType(folder.getDefaultFolderType()));
        dmf.setDeletedMessageCount(folder.getDeletedMessageCount());
        dmf.setExists(folder.exists());
        dmf.setHoldsFolders(folder.isHoldsFolders());
        dmf.setHoldsMessages(folder.isHoldsMessages());
        dmf.setId(folder.getFullname());
        dmf.setMessageCount(folder.getMessageCount());
        dmf.setName(folder.getName());
        dmf.setNewMessageCount(folder.getNewMessageCount());
        {
            final MailPermission ownPermission = folder.getOwnPermission();
            final MessagingPermission mp = DefaultMessagingPermission.newInstance();
            mp.setAdmin(ownPermission.isFolderAdmin());
            mp.setAllPermissions(
                ownPermission.getFolderPermission(),
                ownPermission.getReadPermission(),
                ownPermission.getWritePermission(),
                ownPermission.getDeletePermission());
            mp.setEntity(ownPermission.getEntity());
            mp.setGroup(ownPermission.isGroupPermission());
            mp.setSystem(ownPermission.getSystem());
            dmf.setOwnPermission(mp);
        }
        dmf.setParentId(folder.getParentFullname());
        {
            for (final MailPermission permission : folder.getPermissions()) {
                final MessagingPermission mp = DefaultMessagingPermission.newInstance();
                mp.setAdmin(permission.isFolderAdmin());
                mp.setAllPermissions(
                    permission.getFolderPermission(),
                    permission.getReadPermission(),
                    permission.getWritePermission(),
                    permission.getDeletePermission());
                mp.setEntity(permission.getEntity());
                mp.setGroup(permission.isGroupPermission());
                mp.setSystem(permission.getSystem());
                dmf.addPermission(mp);
            }
        }
        dmf.setRootFolder(folder.isRootFolder());
        dmf.setSeparator(folder.getSeparator());
        dmf.setSubfolders(folder.hasSubfolders());
        dmf.setSubscribed(folder.isSubscribed());
        dmf.setSubscribedSubfolders(folder.hasSubscribedSubfolders());
        dmf.setUnreadMessageCount(folder.getUnreadMessageCount());
        return dmf;
    }

    private static DefaultFolderType getDefaultFolderType(final com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType defaultFolderType) {
        switch (defaultFolderType) {
        case CONFIRMED_HAM:
            return DefaultFolderType.CONFIRMED_HAM;
        case CONFIRMED_SPAM:
            return DefaultFolderType.CONFIRMED_SPAM;
        case DRAFTS:
            return DefaultFolderType.DRAFTS;
        case INBOX:
            return DefaultFolderType.INBOX;
        case NONE:
            return DefaultFolderType.NONE;
        case SENT:
            return DefaultFolderType.SENT;
        case SPAM:
            return DefaultFolderType.SPAM;
        case TRASH:
            return DefaultFolderType.TRASH;
        default:
            // Cannot occur
            throw new InternalError("Unknown enum constant: " + defaultFolderType);
        }
    }

    @Override
    public Quota getMessageQuota(final String folderId) throws OXException {
        try {
            final com.openexchange.mail.Quota messageQuota = folderStorage.getMessageQuota(folderId);
            return getQuotaFrom(messageQuota);
        } catch (final OXException e) {
            throw e;
        }

    }

    private Quota getQuotaFrom(final com.openexchange.mail.Quota otherQuota) {
        return new Quota(otherQuota.getLimit(), otherQuota.getUsage(), getType(otherQuota.getType()));
    }

    private Type getType(final com.openexchange.mail.Quota.Type type) {
        switch (type) {
        case MESSAGE:
            return Type.MESSAGE;
        case STORAGE:
            return Type.STORAGE;
        default:
            // Cannot occur
            throw new InternalError("Unknown enum constant: " + type);
        }
    }

    @Override
    public MessagingFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        try {
            final MailFolder[] mailFolders = folderStorage.getPath2DefaultFolder(folderId);
            final MessagingFolder[] ret = new MessagingFolder[mailFolders.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = convert2MessagingFolder(mailFolders[i], caps);
            }
            return ret;
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        try {
            final com.openexchange.mail.Quota.Type[] oTypes = new com.openexchange.mail.Quota.Type[types.length];
            for (int i = 0; i < oTypes.length; i++) {
                oTypes[i] = getReverseType(types[i]);
            }
            final com.openexchange.mail.Quota[] quotas = folderStorage.getQuotas(folder, oTypes);
            final Quota[] ret = new Quota[quotas.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = getQuotaFrom(quotas[i]);
            }
            return ret;
        } catch (final OXException e) {
            throw e;
        }
    }

    private com.openexchange.mail.Quota.Type getReverseType(final Type type) {
        switch (type) {
        case MESSAGE:
            return com.openexchange.mail.Quota.Type.MESSAGE;
        case STORAGE:
            return com.openexchange.mail.Quota.Type.STORAGE;
        default:
            // Cannot occur
            throw new InternalError("Unknown enum constant: " + type);
        }
    }

    @Override
    public MessagingFolder getRootFolder() throws OXException {
        try {
            return convert2MessagingFolder(folderStorage.getRootFolder(), caps);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String getSentFolder() throws OXException {
        try {
            return folderStorage.getSentFolder();
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String getSpamFolder() throws OXException {
        try {
            return folderStorage.getSpamFolder();
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        try {
            final com.openexchange.mail.Quota messageQuota = folderStorage.getStorageQuota(folderId);
            return getQuotaFrom(messageQuota);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public MessagingFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        try {
            final MailFolder[] mailFolders = folderStorage.getSubfolders(parentIdentifier, all);
            final MessagingFolder[] ret = new MessagingFolder[mailFolders.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = convert2MessagingFolder(mailFolders[i], caps);
            }
            return ret;
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String getTrashFolder() throws OXException {
        try {
            return folderStorage.getTrashFolder();
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        try {
            return folderStorage.moveFolder(folderId, newParentId);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        try {
            return folderStorage.renameFolder(folderId, newName);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public String updateFolder(final String identifier, final MessagingFolder toUpdate) throws OXException {
        try {
            final MailFolderDescription mfd = new MailFolderDescription();

            mfd.setFullname(toUpdate.getId());

            mfd.setExists(true);
            mfd.setAccountId(accountId);
            mfd.setName(toUpdate.getName());
            mfd.setParentAccountId(accountId);
            mfd.setParentFullname(toUpdate.getParentId());
            mfd.setSeparator(toUpdate.getSeparator());
            mfd.setSubscribed(toUpdate.isSubscribed());

            addMailPermissions(toUpdate, mfd);

            return folderStorage.updateFolder(identifier, mfd);
        } catch (final OXException e) {
            throw e;
        }
    }

    private void addMailPermissions(final MessagingFolder toUpdate, final MailFolderDescription mfd) throws OXException {
        final MailProvider provider = MailProviderRegistry.getMailProviderBySession(session, accountId);
        for (final MessagingPermission permission : toUpdate.getPermissions()) {
            final MailPermission mailPermission = provider.createNewMailPermission(session, accountId);
            mailPermission.setAllPermission(
                permission.getFolderPermission(),
                permission.getReadPermission(),
                permission.getWritePermission(),
                permission.getDeletePermission());
            mailPermission.setEntity(permission.getEntity());
            mailPermission.setFolderAdmin(permission.isAdmin());
            mailPermission.setGroupPermission(permission.isGroup());
            mailPermission.setSystem(permission.getSystem());
            mfd.addPermission(mailPermission);
        }
    }

}
