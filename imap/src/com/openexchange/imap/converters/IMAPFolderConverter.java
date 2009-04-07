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

package com.openexchange.imap.converters;

import java.net.InetSocketAddress;
import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.imap.ACLPermission;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.NamespaceFolder;
import com.openexchange.imap.acl.ACLExtension;
import com.openexchange.imap.acl.ACLExtensionFactory;
import com.openexchange.imap.cache.NamespaceFoldersCache;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.RootSubfolderCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.dataobjects.IMAPMailFolder;
import com.openexchange.imap.entity2acl.Entity2ACLArgs;
import com.openexchange.imap.entity2acl.Entity2ACLException;
import com.openexchange.imap.entity2acl.IMAPServer;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;

/**
 * {@link IMAPFolderConverter} - Converts an instance of {@link IMAPFolder} to an instance of {@link MailFolder}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPFolderConverter {

    private static final class Entity2ACLArgsImpl implements Entity2ACLArgs {

        private final int accountId;

        private final InetSocketAddress imapServerAddress;

        private final int sessionUser;

        private final String fullname;

        private final char separator;

        /**
         * Initializes a new {@link Entity2ACLArgsImpl}.
         * 
         * @param accountId The account ID
         * @param imapServerAddress The IMAP server address
         * @param sessionUser The session user ID
         * @param fullname The IMAP folder's fullname
         * @param separator The separator character
         */
        public Entity2ACLArgsImpl(final int accountId, final InetSocketAddress imapServerAddress, final int sessionUser, final String fullname, final char separator) {
            super();
            this.accountId = accountId;
            this.imapServerAddress = imapServerAddress;
            this.sessionUser = sessionUser;
            this.fullname = fullname;
            this.separator = separator;
        }

        public Object[] getArguments(final IMAPServer imapServer) throws AbstractOXException {
            if (IMAPServer.CYRUS.equals(imapServer)) {
                return new Object[] { Integer.valueOf(accountId), imapServerAddress };
            } else if (IMAPServer.COURIER.equals(imapServer)) {
                return new Object[] {
                    Integer.valueOf(accountId), imapServerAddress, Integer.valueOf(sessionUser), fullname, Character.valueOf(separator) };
            }
            throw new Entity2ACLException(Entity2ACLException.Code.UNKNOWN_IMAP_SERVER, imapServer.getName());

        }
    }

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPFolderConverter.class);

    private static final Rights RIGHTS_EMPTY = new Rights();

    /**
     * New mailbox attribute added by the "LIST-EXTENDED" extension.
     */
    private static final String ATTRIBUTE_NON_EXISTENT = "\\NonExistent";

    private static final String ATTRIBUTE_HAS_CHILDREN = "\\HasChildren";

    private static final String ATTRIBUTE_HAS_NO_CHILDREN = "\\HasNoChildren";

    /**
     * Prevent instantiation
     */
    private IMAPFolderConverter() {
        super();
    }

    /**
     * Creates an appropriate implementation of {@link Entity2ACLArgs}.
     * 
     * @param session The session
     * @param imapFolder The IMAP folder
     * @param imapConfig The IMAP configuration
     * @return An appropriate implementation of {@link Entity2ACLArgs}
     * @throws MessagingException If IMAP folder's attributes cannot be accessed
     */
    public static Entity2ACLArgs getEntity2AclArgs(final Session session, final IMAPFolder imapFolder, final IMAPConfig imapConfig) throws MessagingException {
        return new Entity2ACLArgsImpl(
            imapConfig.getAccountId(),
            new InetSocketAddress(imapConfig.getServer(), imapConfig.getPort()),
            session.getUserId(),
            imapFolder.getFullName(),
            imapFolder.getSeparator());
    }

    /**
     * Creates a folder data object from given IMAP folder.
     * 
     * @param imapFolder The IMAP folder
     * @param session The session
     * @param ctx The context
     * @return an instance of <code>{@link IMAPMailFolder}</code> containing the attributes from given IMAP folder
     * @throws MailException If conversion fails
     */
    public static IMAPMailFolder convertFolder(final IMAPFolder imapFolder, final Session session, final IMAPConfig imapConfig, final Context ctx) throws MailException {
        try {
            final IMAPMailFolder mailFolder = new IMAPMailFolder();
            final boolean isRoot = (imapFolder instanceof DefaultFolder);
            mailFolder.setRootFolder(isRoot);
            final boolean exists = imapFolder.exists();
            mailFolder.setExists(exists);
            mailFolder.setSeparator(imapFolder.getSeparator());
            final String imapFullname = imapFolder.getFullName();
            if (exists) {
                String[] attrs;
                try {
                    attrs = imapFolder.getAttributes();
                } catch (final NullPointerException e) {
                    /*
                     * No attributes available.
                     */
                    attrs = null;
                }
                if (null != attrs) {
                    final boolean hasChildren = imapConfig.getImapCapabilities().hasChildren();
                    for (final String attribute : attrs) {
                        if (ATTRIBUTE_NON_EXISTENT.equalsIgnoreCase(attribute)) {
                            mailFolder.setNonExistent(true);
                        }
                        if (hasChildren) {
                            if (ATTRIBUTE_HAS_CHILDREN.equalsIgnoreCase(attribute)) {
                                mailFolder.setSubfolders(true);
                            } else if (ATTRIBUTE_HAS_NO_CHILDREN.equalsIgnoreCase(attribute)) {
                                mailFolder.setSubfolders(false);
                            }
                        }
                    }
                }
                if (!mailFolder.containsSubfolders()) {
                    /*
                     * No \HasChildren attribute found; check for subfolders through a LIST command
                     */
                    checkSubfoldersByCommands(imapFolder, mailFolder, imapFullname, mailFolder.getSeparator(), false);
                }
                if (!mailFolder.containsNonExistent()) {
                    mailFolder.setNonExistent(false);
                }
                /*
                 * Check reliably for subscribed subfolders through LSUB command since folder attributes need not to to be present as per
                 * RFC 3501
                 */
                checkSubfoldersByCommands(imapFolder, mailFolder, imapFullname, mailFolder.getSeparator(), true);
            }
            if (isRoot) {
                mailFolder.setFullname(MailFolder.DEFAULT_FOLDER_ID);
            } else {
                mailFolder.setFullname(imapFullname);
            }
            mailFolder.setName(isRoot ? MailFolder.DEFAULT_FOLDER_NAME : imapFolder.getName());
            {
                final Folder parent = imapFolder.getParent();
                if (null == parent) {
                    mailFolder.setParentFullname(null);
                } else {
                    mailFolder.setParentFullname(parent instanceof DefaultFolder ? MailFolder.DEFAULT_FOLDER_ID : parent.getFullName());
                }
            }
            /*
             * Set type
             */
            if (exists) {
                final int type = imapFolder.getType();
                mailFolder.setHoldsFolders(((type & javax.mail.Folder.HOLDS_FOLDERS) > 0));
                mailFolder.setHoldsMessages(((type & javax.mail.Folder.HOLDS_MESSAGES) > 0));
                if (!mailFolder.isHoldsFolders()) {
                    mailFolder.setSubfolders(false);
                    mailFolder.setSubscribedSubfolders(false);
                }
            } else {
                mailFolder.setHoldsFolders(false);
                mailFolder.setHoldsMessages(false);
                mailFolder.setSubfolders(false);
                mailFolder.setSubscribedSubfolders(false);
            }
            final boolean selectable = mailFolder.isHoldsMessages();
            final Rights ownRights;
            if (isRoot) {
                /*
                 * Properly handled in com.openexchange.mail.json.writer.FolderWriter
                 */
                final ACLPermission ownPermission = new ACLPermission();
                final int fp = RootSubfolderCache.canCreateSubfolders((DefaultFolder) imapFolder, true, session, imapConfig.getAccountId()).booleanValue() ? OCLPermission.CREATE_SUB_FOLDERS : OCLPermission.NO_PERMISSIONS;
                ownPermission.setAllPermission(fp, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                ownPermission.setFolderAdmin(false);
                mailFolder.setOwnPermission(ownPermission);
                ownRights = (Rights) RIGHTS_EMPTY.clone();
            } else {
                final ACLPermission ownPermission = new ACLPermission();
                ownPermission.setEntity(session.getUserId());
                if (!exists || mailFolder.isNonExistent()) {
                    ownPermission.parseRights((ownRights = (Rights) RIGHTS_EMPTY.clone()), imapConfig);
                } else if (!selectable) {
                    /*
                     * Distinguish between holds folders and none
                     */
                    if (mailFolder.isHoldsFolders()) {
                        /*
                         * This is the tricky case: Allow subfolder creation for a common imap folder but deny it for imap server's
                         * namespace folders
                         */
                        if (checkForNamespaceFolder(imapFullname, (IMAPStore) imapFolder.getStore(), session, imapConfig.getAccountId())) {
                            ownPermission.parseRights((ownRights = (Rights) RIGHTS_EMPTY.clone()), imapConfig);
                        } else {
                            ownPermission.setAllPermission(
                                OCLPermission.CREATE_SUB_FOLDERS,
                                OCLPermission.NO_PERMISSIONS,
                                OCLPermission.NO_PERMISSIONS,
                                OCLPermission.NO_PERMISSIONS);
                            ownPermission.setFolderAdmin(true);
                            ownRights = ACLPermission.permission2Rights(ownPermission, imapConfig);
                        }
                    } else {
                        ownPermission.parseRights((ownRights = (Rights) RIGHTS_EMPTY.clone()), imapConfig);
                    }
                } else {
                    ownPermission.parseRights((ownRights = getOwnRightsInternal(imapFolder, session, imapConfig)), imapConfig);
                }
                /*
                 * Check own permission against folder type
                 */
                if (!mailFolder.isHoldsFolders() && ownPermission.canCreateSubfolders()) {
                    ownPermission.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
                }
                if (!selectable) {
                    ownPermission.setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
                }
                mailFolder.setOwnPermission(ownPermission);
            }
            if (isRoot) {
                mailFolder.setDefaultFolder(false);
            } else {
                /*
                 * Default folder
                 */
                if ("INBOX".equals(imapFullname)) {
                    mailFolder.setDefaultFolder(true);
                } else if (isDefaultFoldersChecked(session, imapConfig.getAccountId())) {
                    final int len = UserSettingMailStorage.getInstance().getUserSettingMail(
                        session.getUserId(),
                        ContextStorage.getStorageContext(session.getContextId())).isSpamEnabled() ? 6 : 4;
                    for (int i = 0; (i < len) && !mailFolder.isDefaultFolder(); i++) {
                        if (mailFolder.getFullname().equals(getDefaultMailFolder(i, session, imapConfig.getAccountId()))) {
                            mailFolder.setDefaultFolder(true);
                        }
                    }
                    if (!mailFolder.containsDefaultFolder()) {
                        mailFolder.setDefaultFolder(false);
                    }
                }
            }
            final ACLExtension aclExtension = ACLExtensionFactory.getInstance().getACLExtension(imapConfig);
            if (selectable && aclExtension.canRead(ownRights)) {
                try {
                    mailFolder.setMessageCount(imapFolder.getMessageCount());
                    mailFolder.setNewMessageCount(imapFolder.getNewMessageCount());
                    mailFolder.setUnreadMessageCount(imapFolder.getUnreadMessageCount());
                } catch (final MessagingException e) {
                    final Exception nested = e.getNextException();
                    if (nested instanceof ParsingException && nested.getMessage().indexOf("STATUS") != -1) {
                        /*
                         * Parsing of STATUS response failed
                         */
                        final int[] status = IMAPCommandsCollection.getStatus(imapFolder);
                        mailFolder.setMessageCount(status[0]);
                        mailFolder.setMessageCount(status[1]);
                        mailFolder.setMessageCount(status[2]);
                    } else {
                        // Re-throw
                        throw e;
                    }
                }
                mailFolder.setDeletedMessageCount(imapFolder.getDeletedMessageCount());
            } else {
                mailFolder.setMessageCount(-1);
                mailFolder.setNewMessageCount(-1);
                mailFolder.setUnreadMessageCount(-1);
                mailFolder.setDeletedMessageCount(-1);
            }
            mailFolder.setSubscribed(MailProperties.getInstance().isSupportSubscription() ? ("INBOX".equals(mailFolder.getFullname()) ? true : imapFolder.isSubscribed()) : true);
            if (imapConfig.isSupportsACLs()) {
                // Check if ACLs can be read; meaning GETACL is allowed
                if (selectable && exists && !isRoot && aclExtension.canGetACL(ownRights)) {
                    try {
                        applyACL2Permissions(imapFolder, session, imapConfig, mailFolder, ownRights, ctx);
                    } catch (final MailException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("ACLs could not be parsed", e);
                        }
                        mailFolder.removePermissions();
                        addOwnACL(session.getUserId(), mailFolder, ownRights, imapConfig);
                    }
                } else {
                    addOwnACL(session.getUserId(), mailFolder, ownRights, imapConfig);
                }
            } else {
                addOwnACL(session.getUserId(), mailFolder, ownRights, imapConfig);
            }
            if (MailProperties.getInstance().isUserFlagsEnabled() && exists && selectable && aclExtension.canRead(ownRights) && UserFlagsCache.supportsUserFlags(
                imapFolder,
                true,
                session,
                imapConfig.getAccountId())) {
                mailFolder.setSupportsUserFlags(true);
            } else {
                mailFolder.setSupportsUserFlags(false);
            }
            return mailFolder;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        } catch (final ContextException e) {
            throw new IMAPException(e);
        }
    }

    private static void checkSubfoldersByCommands(final IMAPFolder imapFolder, final IMAPMailFolder mailFolder, final String fullname, final char separator, final boolean checkSubscribed) throws MessagingException {
        final ListInfo[] li = (ListInfo[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                final String pattern = mailFolder.isRootFolder() ? "%" : new StringBuilder().append(fullname).append(separator).append('%').toString();
                if (checkSubscribed) {
                    return protocol.lsub("", pattern);
                }
                return protocol.list("", pattern);
            }
        });
        if (checkSubscribed) {
            mailFolder.setSubscribedSubfolders((li != null) && (li.length > 0));
        } else {
            mailFolder.setSubfolders((li != null) && (li.length > 0));
        }
    }

    private static boolean isDefaultFoldersChecked(final Session session, final int accountId) {
        final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.getParamDefaultFolderChecked(accountId));
        return (b != null) && b.booleanValue();
    }

    private static String getDefaultMailFolder(final int index, final Session session, final int accountId) {
        final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.getParamDefaultFolderArray(accountId));
        return arr == null ? null : arr[index];
    }

    /**
     * Parses IMAP folder's ACLs to instances of {@link ACLPermission} and applies them to specified mail folder.
     * 
     * @param imapFolder The IMAP folder
     * @param session The session providing needed user data
     * @param imapConfig The user's IMAP configuration
     * @param mailFolder The mail folder
     * @param ownRights The rights granted to IMAP folder for session user
     * @param ctx The context
     * @throws MailException If ACLs cannot be mapped
     */
    private static void applyACL2Permissions(final IMAPFolder imapFolder, final Session session, final IMAPConfig imapConfig, final MailFolder mailFolder, final Rights ownRights, final Context ctx) throws MailException {
        if (IMAPConfig.hasNewACLExt(imapConfig.getServer()) && !ownRights.contains(Rights.Right.ADMINISTER)) {
            // Just add own permission and return
            addOwnACL(session.getUserId(), mailFolder, ownRights, imapConfig);
            return;
        }
        final ACL[] acls;
        try {
            acls = imapFolder.getACL();
        } catch (final MessagingException e) {
            if (!ownRights.contains(Rights.Right.ADMINISTER)) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        new StringBuilder(256).append("ACLs could not be requested for folder ").append(imapFolder.getFullName()).append(
                            ". A newer ACL extension (RFC 4314) seems to be supported by IMAP server ").append(imapConfig.getServer()).append(
                            ", which denies GETACL command if no ADMINISTER right is granted."),
                        e);
                }
                /*
                 * Remember newer IMAP server's ACL extension
                 */
                IMAPConfig.setNewACLExt(imapConfig.getServer(), true);
                addOwnACL(session.getUserId(), mailFolder, ownRights, imapConfig);
                return;
            }
            throw MIMEMailException.handleMessagingException(e);
        }
        try {
            final Entity2ACLArgs args = new Entity2ACLArgsImpl(imapConfig.getAccountId(), new InetSocketAddress(
                imapConfig.getServer(),
                imapConfig.getPort()), session.getUserId(), imapFolder.getFullName(), imapFolder.getSeparator());
            final StringBuilder debugBuilder;
            if (LOG.isDebugEnabled()) {
                debugBuilder = new StringBuilder(128);
            } else {
                debugBuilder = null;
            }
            for (int j = 0; j < acls.length; j++) {
                final ACLPermission aclPerm = new ACLPermission();
                try {
                    aclPerm.parseACL(acls[j], args, imapConfig, ctx);
                    mailFolder.addPermission(aclPerm);
                } catch (final AbstractOXException e) {
                    if (isUnknownEntityError(e)) {
                        if (LOG.isDebugEnabled()) {
                            debugBuilder.setLength(0);
                            LOG.debug(debugBuilder.append("Cannot map ACL entity named \"").append(acls[j].getName()).append(
                                "\" to a system user").toString());
                        }
                    } else {
                        throw new MailException(e);
                    }
                }
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    /**
     * Adds current user's rights granted to IMAP folder as an ACL permission.
     * 
     * @param sessionUser The session user
     * @param mailFolder The mail folder
     * @param ownRights The user's rights
     * @param imapConfig The IMAP configuration
     * @throws IMAPException If an IMAP error occurs
     */
    private static void addOwnACL(final int sessionUser, final MailFolder mailFolder, final Rights ownRights, final IMAPConfig imapConfig) throws IMAPException {
        final ACLPermission aclPerm = new ACLPermission();
        aclPerm.setEntity(sessionUser);
        aclPerm.parseRights(ownRights, imapConfig);
        mailFolder.addPermission(aclPerm);
    }

    /**
     * Adds empty ACL permission to specified mail folder for given user.
     * 
     * @param sessionUser The session user
     * @param mailFolder The mail folder
     */
    private static void addEmptyACL(final int sessionUser, final MailFolder mailFolder) {
        final ACLPermission aclPerm = new ACLPermission();
        aclPerm.setEntity(sessionUser);
        aclPerm.setAllPermission(
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS);
        mailFolder.addPermission(aclPerm);
    }

    private static boolean isUnknownEntityError(final AbstractOXException e) {
        return EnumComponent.ACL_ERROR.equals(e.getComponent()) && Entity2ACLException.Code.RESOLVE_USER_FAILED.getNumber() == e.getDetailNumber();
    }

    private static boolean checkForNamespaceFolder(final String fullname, final IMAPStore imapStore, final Session session, final int accountId) throws MessagingException {
        /*
         * Check for namespace folder
         */
        {
            final String[] personalFolders = NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session, accountId);
            for (int i = 0; i < personalFolders.length; i++) {
                if (personalFolders[i].startsWith(fullname)) {
                    return true;
                }
            }
        }
        {
            final String[] userFolders = NamespaceFoldersCache.getUserNamespaces(imapStore, true, session, accountId);
            for (int i = 0; i < userFolders.length; i++) {
                if (userFolders[i].startsWith(fullname)) {
                    return true;
                }
                final NamespaceFolder nsf = new NamespaceFolder(imapStore, userFolders[i], imapStore.getDefaultFolder().getSeparator());
                final Folder[] subFolders = nsf.list();
                for (int j = 0; j < subFolders.length; j++) {
                    if (subFolders[j].getFullName().startsWith(fullname)) {
                        return true;
                    }
                }
            }
        }
        {
            final String[] sharedFolders = NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session, accountId);
            for (int i = 0; i < sharedFolders.length; i++) {
                if (sharedFolders[i].startsWith(fullname)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Rights getOwnRightsInternal(final IMAPFolder folder, final Session session, final IMAPConfig imapConfig) {
        if (folder instanceof DefaultFolder) {
            return null;
        }
        final Rights retval;
        if (imapConfig.isSupportsACLs()) {
            try {
                retval = RightsCache.getCachedRights(folder, true, session, imapConfig.getAccountId());
            } catch (final MessagingException e) {
                if ((e.getNextException() instanceof com.sun.mail.iap.CommandFailedException) && (e.getNextException().getMessage().indexOf(
                    "NO Mailbox does not exist") != -1)) {
                    /*
                     * This occurs when requesting MYRIGHTS on a shared folder. Just log a warning!
                     */
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(IMAPException.getFormattedMessage(IMAPException.Code.FOLDER_NOT_FOUND, folder.getFullName()), e);
                    }
                } else {
                    LOG.error(e.getMessage(), e);
                }
                /*
                 * Write empty string as rights. Nevertheless user may see folder!
                 */
                return (Rights) RIGHTS_EMPTY.clone();
            } catch (final Throwable t) {
                LOG.error(t.getMessage(), t);
                /*
                 * Write empty string as rights. Nevertheless user may see folder!
                 */
                return (Rights) RIGHTS_EMPTY.clone();
            }
        } else {
            /*
             * No ACLs enabled. User has full access.
             */
            try {
                retval = ACLExtensionFactory.getInstance().getACLExtension(imapConfig).getFullRights();
            } catch (final IMAPException e) {
                LOG.error(e.getMessage(), e);
                return new Rights("acdilprsw");
            }
        }
        return retval;
    }

}
