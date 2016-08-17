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

package com.openexchange.imap.converters;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.imap.ACLPermission;
import com.openexchange.imap.IMAPAccess;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.NamespaceFoldersCache;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.RootSubfoldersEnabledCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.dataobjects.IMAPMailFolder;
import com.openexchange.imap.entity2acl.Entity2ACLArgs;
import com.openexchange.imap.entity2acl.Entity2ACLExceptionCode;
import com.openexchange.imap.entity2acl.IMAPServer;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
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
        private final String serverUrl;
        private final int sessionUser;
        private final String fullname;
        private final char separator;
        private final String otherUserNamespace;
        private final String publicNamespace;

        /**
         * Initializes a new {@link Entity2ACLArgsImpl}.
         *
         * @param accountId The account ID
         * @param imapServerAddress The IMAP server address
         * @param sessionUser The session user ID
         * @param fullname The IMAP folder's full name
         * @param separator The separator character
         * @param otherUserNamespaces The namespace for other users
         */
        Entity2ACLArgsImpl(int accountId, String serverUrl, int sessionUser, String fullname, char separator, String[] otherUserNamespaces, String[] publicNamespaces) {
            super();
            this.accountId = accountId;
            this.serverUrl = serverUrl;
            this.sessionUser = sessionUser;
            this.fullname = fullname;
            this.separator = separator;
            this.otherUserNamespace = null == otherUserNamespaces || otherUserNamespaces.length == 0 ? null : otherUserNamespaces[0];
            this.publicNamespace = null == publicNamespaces || publicNamespaces.length == 0 ? null : publicNamespaces[0];
        }

        @Override
        public Object[] getArguments(final IMAPServer imapServer) throws OXException {
            return imapServer.getArguments(accountId, serverUrl, sessionUser, fullname, separator, otherUserNamespace, publicNamespace);
        }
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPFolderConverter.class);

    /**
     * New mailbox attribute added by the "LIST-EXTENDED" extension.
     */
    private static final String ATTRIBUTE_NON_EXISTENT = "\\nonexistent";

    private static final String ATTRIBUTE_HAS_CHILDREN = "\\haschildren";

    private static final String ATTRIBUTE_NO_INFERIORS = "\\noinferiors";

    private static final String ATTRIBUTE_DRAFTS = "\\drafts";
    private static final String ATTRIBUTE_JUNK = "\\junk";
    private static final String ATTRIBUTE_SENT = "\\sent";
    private static final String ATTRIBUTE_TRASH = "\\trash";

    // private static final String ATTRIBUTE_HAS_NO_CHILDREN = "\\HasNoChildren";

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
     * @throws OXException If IMAP folder's attributes cannot be accessed
     */
    public static Entity2ACLArgs getEntity2AclArgs(Session session, IMAPFolder imapFolder, IMAPConfig imapConfig) throws OXException {
        return getEntity2AclArgs(session.getUserId(), session, imapFolder, imapConfig);
    }

    /**
     * Creates an appropriate implementation of {@link Entity2ACLArgs}.
     *
     * @param userId The user identifier
     * @param session The session
     * @param imapFolder The IMAP folder
     * @param imapConfig The IMAP configuration
     * @return An appropriate implementation of {@link Entity2ACLArgs}
     * @throws OXException If IMAP folder's attributes cannot be accessed
     */
    public static Entity2ACLArgs getEntity2AclArgs(int userId, Session session, IMAPFolder imapFolder, IMAPConfig imapConfig) throws OXException {
        try {
            return new Entity2ACLArgsImpl(
                imapConfig.getAccountId(),
                new StringBuilder(36).append(IDNA.toASCII(imapConfig.getServer())).append(':').append(imapConfig.getPort()).toString(),
                userId,
                imapFolder.getFullName(),
                ListLsubCache.getSeparator(imapConfig.getAccountId(), imapFolder, session, imapConfig.getIMAPProperties().isIgnoreSubscription()),
                NamespaceFoldersCache.getUserNamespaces((IMAPStore) imapFolder.getStore(), true, session, imapConfig.getAccountId()),
                NamespaceFoldersCache.getSharedNamespaces((IMAPStore) imapFolder.getStore(), true, session, imapConfig.getAccountId()));
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    private static final DefaultFolderType[] TYPES = {
        DefaultFolderType.DRAFTS, DefaultFolderType.SENT, DefaultFolderType.SPAM, DefaultFolderType.TRASH,
        DefaultFolderType.CONFIRMED_SPAM, DefaultFolderType.CONFIRMED_HAM, DefaultFolderType.INBOX };

    private static final class Key {

        private final StackTraceElement[] stackTrace;

        public Key(final StackTraceElement[] stackTrace) {
            super();
            this.stackTrace = stackTrace;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(stackTrace);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (!Arrays.equals(stackTrace, other.stackTrace)) {
                return false;
            }
            return true;
        }

    }

    // private static final ConcurrentMap<Key, Object> M = new NonBlockingHashMap<Key, Object>();

    private static final boolean DO_STATUS = false;

    /**
     * Creates a folder data object from given IMAP folder.
     *
     * @param imapFolder The IMAP folder
     * @param session The session
     * @param ctx The context
     * @return An instance of <code>{@link IMAPMailFolder}</code> containing the attributes from given IMAP folder
     * @throws OXException If conversion fails
     */
    public static IMAPMailFolder convertFolder(final IMAPFolder imapFolder, final Session session, final IMAPAccess imapAccess, final Context ctx) throws OXException {
        try {
            synchronized (imapFolder) {
                final String imapFullName = imapFolder.getFullName();
                if (imapFolder instanceof DefaultFolder) {
                    return convertRootFolder((DefaultFolder) imapFolder, session, imapAccess.getIMAPConfig());
                }
                final IMAPConfig imapConfig = imapAccess.getIMAPConfig();
                final long st = System.currentTimeMillis();
                // Convert non-root folder
                final IMAPMailFolder mailFolder = new IMAPMailFolder();
                mailFolder.setRootFolder(false);
                // Get appropriate entries
                final int accountId = imapConfig.getAccountId();
                final ListLsubEntry listEntry = ListLsubCache.getCachedLISTEntry(imapFullName, accountId, imapFolder, session, imapConfig.getIMAPProperties().isIgnoreSubscription());
                /*
                 * Check existence
                 */
                final boolean exists = "INBOX".equals(imapFullName) || listEntry.exists();
                mailFolder.setExists(exists);
                mailFolder.setSeparator(listEntry.getSeparator());
                // Shared?
                {
                    final IMAPStore imapStore = (IMAPStore) imapFolder.getStore();
                    final String[] users = NamespaceFoldersCache.getUserNamespaces(imapStore, true, session, accountId);
                    final char sep = mailFolder.getSeparator();
                    final StringBuilder tmp = new StringBuilder(32);
                    boolean shared = false;
                    String owner = null;
                    for (int i = 0; !shared && i < users.length; i++) {
                        final String userNamespace = users[i];
                        if (!com.openexchange.java.Strings.isEmpty(userNamespace)) {
                            if (imapFullName.equals(userNamespace)) {
                                shared = true;
                            } else {
                                tmp.setLength(0);
                                final String prefix = tmp.append(userNamespace).append(sep).toString();
                                if (imapFullName.startsWith(prefix)) {
                                    shared = true;
                                    /*-
                                     * "Other Users/user1"
                                     *  vs.
                                     * "Other Users/user1/My shared folder"
                                     */
                                    final int pLen = prefix.length();
                                    final int pos = imapFullName.indexOf(sep, pLen);
                                    owner = pos < 0 ? imapFullName.substring(pLen) : imapFullName.substring(pLen, pos);
                                }
                            }
                        }
                    }
                    mailFolder.setShared(shared);
                    if (null != owner) {
                        mailFolder.setOwner(owner);
                    }
                    boolean isPublic = false;
                    if (!shared) {
                        final String[] shares = NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session, accountId);
                        final String[] personals = NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session, accountId);
                        for (int i = 0; !shared && i < shares.length; i++) {
                            final String sharedNamespace = shares[i];
                            if (!com.openexchange.java.Strings.isEmpty(sharedNamespace)) {
                                if (imapFullName.equals(sharedNamespace)) {
                                    isPublic = true;
                                } else {
                                    tmp.setLength(0);
                                    final String prefix = tmp.append(sharedNamespace).append(sep).toString();
                                    if (imapFullName.startsWith(prefix)) {
                                        isPublic = true;
                                    }
                                }
                            } else if (!startsWithOneOf(imapFullName, sep, personals, users, tmp)) {
                                isPublic = true;
                            }
                        }
                    }
                    mailFolder.setPublic(isPublic);
                }
                /*-
                 * -------------------------------------------------------------------
                 * -------------------------##################------------------------
                 * -------------------------------------------------------------------
                 */
                // final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                // final Key key = new Key(stackTrace);
                // if (null == M.putIfAbsent(key, TYPES)) {
                // final Throwable t = new Throwable();
                // t.setStackTrace(stackTrace);
                // t.printStackTrace(System.out);
                // }
                /*-
                 * -------------------------------------------------------------------
                 * -------------------------##################------------------------
                 * -------------------------------------------------------------------
                 */
                if (exists) {
                    final Set<String> attrs = listEntry.getAttributes();
                    if (null != attrs && !attrs.isEmpty()) {
                        if (attrs.contains(ATTRIBUTE_NON_EXISTENT)) {
                            mailFolder.setNonExistent(true);
                        }
                        if (imapConfig.getImapCapabilities().hasChildren() && attrs.contains(ATTRIBUTE_HAS_CHILDREN)) {
                            mailFolder.setSubfolders(true);
                        }
                        if (attrs.contains(ATTRIBUTE_NO_INFERIORS)) {
                            mailFolder.setSubfolders(true);
                            mailFolder.setSubscribedSubfolders(false);
                        }
                        if (imapConfig.asMap().containsKey("SPECIAL-USE")) {
                            if (attrs.contains(ATTRIBUTE_DRAFTS)) {
                                mailFolder.setDefaultFolder(true);
                                mailFolder.setDefaultFolderType(DefaultFolderType.DRAFTS);
                            } else if (attrs.contains(ATTRIBUTE_JUNK)) {
                                mailFolder.setDefaultFolder(true);
                                mailFolder.setDefaultFolderType(DefaultFolderType.SPAM);
                            } else if (attrs.contains(ATTRIBUTE_SENT)) {
                                mailFolder.setDefaultFolder(true);
                                mailFolder.setDefaultFolderType(DefaultFolderType.SENT);
                            } else if (attrs.contains(ATTRIBUTE_TRASH)) {
                                mailFolder.setDefaultFolder(true);
                                mailFolder.setDefaultFolderType(DefaultFolderType.TRASH);
                            }
                        }
                    }
                    if (!mailFolder.containsSubfolders()) {
                        /*
                         * No \HasChildren attribute found; check for subfolders through a LIST command
                         */
                        final List<ListLsubEntry> children = listEntry.getChildren();
                        mailFolder.setSubfolders(null != children && !children.isEmpty());
                    }
                    if (!mailFolder.containsNonExistent()) {
                        mailFolder.setNonExistent(false);
                    }
                    /*
                     * Check reliably for subscribed subfolders through LSUB command since folder attributes need not to to be present as
                     * per RFC 3501
                     */
                    mailFolder.setSubscribedSubfolders(ListLsubCache.hasAnySubscribedSubfolder(imapFullName, accountId, imapFolder, session, imapConfig.getIMAPProperties().isIgnoreSubscription()));
                }
                /*
                 * Set full name, name, and parent full name
                 */
                mailFolder.setFullname(imapFullName);
                mailFolder.setName(listEntry.getName());
                {
                    final ListLsubEntry parentListEntry = listEntry.getParent();
                    if (null == parentListEntry) {
                        mailFolder.setParentFullname(null);
                    } else {
                        final String pfn = parentListEntry.getFullName();
                        mailFolder.setParentFullname(pfn.length() == 0 ? MailFolder.DEFAULT_FOLDER_ID : pfn);
                    }
                }
                if (!mailFolder.containsDefaultFolder()) {
                    /*
                     * Default folder
                     */
                    if (mailFolder.isShared() || mailFolder.isPublic()) {
                        mailFolder.setDefaultFolder(false);
                        mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
                    } else if ("INBOX".equals(imapFullName)) {
                        mailFolder.setDefaultFolder(true);
                        mailFolder.setDefaultFolderType(DefaultFolderType.INBOX);
                    } else if (isDefaultFoldersChecked(session, accountId)) {
                        final String[] defaultMailFolders = getDefaultMailFolders(session, accountId);
                        for (int i = 0; i < defaultMailFolders.length && !mailFolder.isDefaultFolder(); i++) {
                            if (imapFullName.equals(defaultMailFolders[i])) {
                                mailFolder.setDefaultFolder(true);
                                mailFolder.setDefaultFolderType(TYPES[i]);
                            }
                        }
                        if (!mailFolder.containsDefaultFolder()) {
                            mailFolder.setDefaultFolder(false);
                            mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
                        }
                    } else {
                        mailFolder.setDefaultFolder(false);
                        mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
                    }
                }
                /*
                 * Set type
                 */
                if (exists) {
                    final int type = listEntry.getType();
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
                Rights ownRights;
                /*
                 * Add own rights
                 */
                {
                    final ACLPermission ownPermission = new ACLPermission();
                    ownPermission.setEntity(session.getUserId());
                    if (!exists || mailFolder.isNonExistent()) {
                        ownPermission.parseRights((ownRights = new Rights()), imapConfig);
                    } else if (!selectable) {
                        ownRights = ownRightsFromProblematic(session, imapAccess, imapFullName, imapConfig, mailFolder, accountId, ownPermission);
                    } else {
                        ownRights = getOwnRights(imapFolder, session, imapConfig);
                        if (null == ownRights) {
                            ownRights = ownRightsFromProblematic(session, imapAccess, imapFullName, imapConfig, mailFolder, accountId, ownPermission);
                        } else {
                            ownPermission.parseRights(ownRights, imapConfig);
                        }
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
                /*
                 * Set message counts for total, new, unread, and deleted
                 */
                if (selectable && imapConfig.getACLExtension().canRead(ownRights)) {
                    if (DO_STATUS) {
                        final int messageCount = listEntry.getMessageCount();
                        if (messageCount < 0) {
                            final int[] status = IMAPCommandsCollection.getStatus(imapFolder);
                            mailFolder.setMessageCount(status[0]);
                            mailFolder.setNewMessageCount(status[1]);
                            mailFolder.setUnreadMessageCount(status[2]);
                            listEntry.rememberCounts(status[0], status[1], status[2]);
                        } else {
                            mailFolder.setMessageCount(messageCount);
                            mailFolder.setNewMessageCount(listEntry.getNewMessageCount());
                            mailFolder.setUnreadMessageCount(listEntry.getUnreadMessageCount());
                        }
                    } else {
                        mailFolder.setMessageCount(-1);
                        mailFolder.setNewMessageCount(-1);
                        mailFolder.setUnreadMessageCount(-1);
                    }
                    mailFolder.setDeletedMessageCount(-1/* imapFolder.getDeletedMessageCount() */);
                } else {
                    mailFolder.setMessageCount(-1);
                    mailFolder.setNewMessageCount(-1);
                    mailFolder.setUnreadMessageCount(-1);
                    mailFolder.setDeletedMessageCount(-1);
                }
                mailFolder.setSubscribed(MailProperties.getInstance().isSupportSubscription() ? ("INBOX".equals(mailFolder.getFullname()) ? true : listEntry.isSubscribed()) : true);
                /*
                 * Parse ACLs to user/group permissions for primary account only.
                 */
                if (MailAccount.DEFAULT_ID == accountId && imapConfig.isSupportsACLs()) {
                    // Check if ACLs can be read; meaning GETACL is allowed
                    if (selectable && exists && imapConfig.getACLExtension().canGetACL(ownRights)) {
                        try {
                            applyACL2Permissions(imapFolder, listEntry, session, imapConfig, mailFolder, ownRights, ctx);
                        } catch (final OXException e) {
                            LOG.warn("ACLs could not be parsed", e);
                            mailFolder.removePermissions();
                            addOwnACL(mailFolder);
                        }
                    } else {
                        addOwnACL(mailFolder);
                    }
                } else {
                    addOwnACL(mailFolder);
                }
                if (MailProperties.getInstance().isUserFlagsEnabled() && exists && selectable && imapConfig.getACLExtension().canRead(
                    ownRights) && UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId)) {
                    mailFolder.setSupportsUserFlags(true);
                } else {
                    mailFolder.setSupportsUserFlags(false);
                }
                LOG.debug("IMAP folder \"{}\" converted in {}msec.", imapFullName, System.currentTimeMillis() - st);
                return mailFolder;
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static boolean startsWithOneOf(final String imapFullName, final char sep, final String[] personalNamespaces, final String[] userNamespaces, final StringBuilder tmp) {
        for (final String string : userNamespaces) {
            if (imapFullName.equals(string)) {
                return true;
            }
            tmp.setLength(0);
            if (imapFullName.startsWith(tmp.append(string).append(sep).toString())) {
                return true;
            }
        }
        for (final String string : personalNamespaces) {
            if (imapFullName.equals(string)) {
                return true;
            }
            tmp.setLength(0);
            if (imapFullName.startsWith(tmp.append(string).append(sep).toString())) {
                return true;
            }
        }
        return false;
    }

    private static Rights ownRightsFromProblematic(Session session, IMAPAccess imapAccess, String imapFullName, IMAPConfig imapConfig, IMAPMailFolder mailFolder, int accountId, ACLPermission ownPermission) throws MessagingException, OXException, IMAPException {
        Rights ownRights;
        /*
         * Distinguish between holds folders and none
         */
        if (mailFolder.isHoldsFolders()) {
            /*
             * This is the tricky case: Allow subfolder creation for a common IMAP folder but deny it for namespace folders
             */
            if (checkForNamespaceFolder(imapFullName, imapAccess.getIMAPStore(), session, accountId, imapConfig.getIMAPProperties().isIgnoreSubscription())) {
                ownRights = new Rights();
                ownPermission.parseRights(ownRights, imapConfig);
            } else {
                ownPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                ownPermission.setFolderAdmin(false);
                ownRights = ACLPermission.permission2Rights(ownPermission, imapConfig);
            }
        } else {
            ownRights = new Rights();
            ownPermission.parseRights(ownRights, imapConfig);
        }
        return ownRights;
    }

    private static void checkSubfoldersByCommands(final IMAPFolder imapFolder, final IMAPMailFolder mailFolder, final String fullname, final char separator, final boolean checkSubscribed) throws MessagingException {
        final ListInfo[] li = (ListInfo[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
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

    private static IMAPMailFolder convertRootFolder(final DefaultFolder rootFolder, final Session session, final IMAPConfig imapConfig) throws OXException {
        try {
            final IMAPMailFolder mailFolder = new IMAPMailFolder();
            mailFolder.setRootFolder(true);
            mailFolder.setExists(true);
            mailFolder.setShared(false);
            mailFolder.setPublic(true);
            boolean ignoreSubscription = imapConfig.getIMAPProperties().isIgnoreSubscription();
            mailFolder.setSeparator(ListLsubCache.getSeparator(imapConfig.getAccountId(), rootFolder, session, ignoreSubscription));
            final String imapFullname = "";
            final ListLsubEntry listEntry = ListLsubCache.getCachedLISTEntry(imapFullname, imapConfig.getAccountId(), rootFolder, session, ignoreSubscription);
            final Set<String> attrs = listEntry.getAttributes();
            if (null != attrs && !attrs.isEmpty()) {
                if (attrs.contains(ATTRIBUTE_NON_EXISTENT)) {
                    mailFolder.setNonExistent(true);
                }
                if (imapConfig.getImapCapabilities().hasChildren() && attrs.contains(ATTRIBUTE_HAS_CHILDREN)) {
                    mailFolder.setSubfolders(true);
                }
            }
            mailFolder.setSubfolders(true);
            mailFolder.setSubscribedSubfolders(true); // At least INBOX
            if (!mailFolder.containsNonExistent()) {
                mailFolder.setNonExistent(false);
            }
            /*
             * Set full name, name, and parent full name
             */
            mailFolder.setFullname(MailFolder.DEFAULT_FOLDER_ID);
            mailFolder.setName(MailFolder.DEFAULT_FOLDER_NAME);
            mailFolder.setParentFullname(null);
            mailFolder.setDefaultFolder(false);
            mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
            /*
             * Root folder only holds folders but no messages
             */
            mailFolder.setHoldsFolders(true);
            mailFolder.setHoldsMessages(false);
            /*
             * Check if subfolder creation is allowed
             */
            final ACLPermission ownPermission = new ACLPermission();
            final int fp = RootSubfoldersEnabledCache.isRootSubfoldersEnabled(imapConfig, rootFolder) ? OCLPermission.CREATE_SUB_FOLDERS : OCLPermission.READ_FOLDER;
            ownPermission.setEntity(session.getUserId());
            ownPermission.setAllPermission(fp, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
            ownPermission.setFolderAdmin(false);
            mailFolder.setOwnPermission(ownPermission);
            mailFolder.addPermission(ownPermission);
            /*
             * Set message counts
             */
            mailFolder.setMessageCount(-1);
            mailFolder.setNewMessageCount(-1);
            mailFolder.setUnreadMessageCount(-1);
            mailFolder.setDeletedMessageCount(-1);
            /*
             * Root folder is always subscribed
             */
            mailFolder.setSubscribed(true);
            /*
             * No user flag support
             */
            mailFolder.setSupportsUserFlags(false);
            return mailFolder;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static boolean isDefaultFoldersChecked(final Session session, final int accountId) {
        final Boolean b = MailSessionCache.getInstance(session).getParameter(
            accountId,
            MailSessionParameterNames.getParamDefaultFolderChecked());
        return (b != null) && b.booleanValue();
    }

    private static String[] getDefaultMailFolders(final Session session, final int accountId) {
        return MailSessionCache.getInstance(session).getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
    }

    /**
     * Parses IMAP folder's ACLs to instances of {@link ACLPermission} and applies them to specified mail folder.
     *
     * @param imapFolder The IMAP folder
     * @param listEntry The LIST entry
     * @param session The session providing needed user data
     * @param imapConfig The user's IMAP configuration
     * @param mailFolder The mail folder
     * @param ownRights The rights granted to IMAP folder for session user
     * @param ctx The context
     * @throws OXException If ACLs cannot be mapped
     * @throws MessagingException If a messaging error occurs
     */
    private static void applyACL2Permissions(final IMAPFolder imapFolder, final ListLsubEntry listEntry, final Session session, final IMAPConfig imapConfig, final MailFolder mailFolder, final Rights ownRights, final Context ctx) throws OXException, MessagingException {
        final ACL[] acls;
        try {
            final List<ACL> list = listEntry.getACLs();
            if (null == list) {
                acls = imapFolder.getACL();
                listEntry.rememberACLs(Arrays.asList(acls));
            } else {
                acls = list.toArray(new ACL[list.size()]);
            }
        } catch (final MessagingException e) {
            if (!ownRights.contains(Rights.Right.ADMINISTER)) {
                LOG.warn("ACLs could not be requested for folder {}. A newer ACL extension (RFC 4314) seems to be supported by IMAP server {}, which denies GETACL command if no ADMINISTER right is granted.", imapFolder.getFullName(), imapConfig.getServer(), e);
                addOwnACL(mailFolder);
                return;
            }
            throw MimeMailException.handleMessagingException(e);
        }
        Entity2ACLArgs args = new Entity2ACLArgsImpl(imapConfig.getAccountId(), new StringBuilder(36).append(IDNA.toASCII(imapConfig.getServer())).append(':').append(imapConfig.getPort()).toString(), session.getUserId(), imapFolder.getFullName(), listEntry.getSeparator(), NamespaceFoldersCache.getUserNamespaces((IMAPStore) imapFolder.getStore(), true, session, imapConfig.getAccountId()), NamespaceFoldersCache.getSharedNamespaces((IMAPStore) imapFolder.getStore(), true, session, imapConfig.getAccountId()));
        boolean userPermAdded = false;
        for (int j = 0; j < acls.length; j++) {
            final ACLPermission aclPerm = new ACLPermission();
            final ACL acl = acls[j];
            try {
                aclPerm.parseACL(acl, args, (IMAPStore) imapFolder.getStore(), imapConfig, ctx);
                if (session.getUserId() == aclPerm.getEntity()) {
                    userPermAdded = true;
                    final Rights aclRights = acl.getRights();
                    if (!ownRights.equals(aclRights)) {
                        LOG.debug("Detected different rights for MYRIGHTS ({}) and GETACL ({}) for user {} in context {}. Preferring GETACL rights as user''s own-rights.", ownRights, aclRights, session.getUserId(), session.getContextId());
                        final MailPermission ownPermission = mailFolder.getOwnPermission();
                        if (ownPermission instanceof ACLPermission) {
                            ((ACLPermission) ownPermission).parseRights(aclRights, imapConfig);
                        } else {
                            ownPermission.setAllPermission(
                                aclPerm.getFolderPermission(),
                                aclPerm.getReadPermission(),
                                aclPerm.getWritePermission(),
                                aclPerm.getDeletePermission());
                            ownPermission.setFolderAdmin(aclPerm.isFolderAdmin());
                        }
                    }
                }
                mailFolder.addPermission(aclPerm);
            } catch (final OXException e) {
                if (!isUnknownEntityError(e)) {
                    throw e;
                }
                LOG.debug("Cannot map ACL entity named \"{}\" to a system user", acl.getName());
            }
        }
        /*
         * Check if permission for user was added
         */
        if (!userPermAdded) {
            addOwnACL(mailFolder);
        }
    }

    /**
     * Adds current user's rights granted to IMAP folder as an ACL permission.
     *
     * @param mailFolder The mail folder containing own permission
     */
    private static void addOwnACL(final MailFolder mailFolder) {
        mailFolder.addPermission(mailFolder.getOwnPermission());
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

    private static boolean isUnknownEntityError(final OXException e) {
        final int code = e.getCode();
        return (e.isPrefix("ACL") && (Entity2ACLExceptionCode.RESOLVE_USER_FAILED.getNumber() == code)) || (e.isPrefix("USR") && (LdapExceptionCode.USER_NOT_FOUND.getNumber() == code));
    }

    private static boolean checkForNamespaceFolder(final String fullName, final IMAPStore imapStore, final Session session, final int accountId, boolean ignoreLsub) throws MessagingException, OXException {
        /*
         * Check for namespace folder
         */
        {
            final String[] personalFolders = NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session, accountId);
            for (int i = 0; i < personalFolders.length; i++) {
                if (personalFolders[i].startsWith(fullName)) {
                    return true;
                }
            }
        }
        {
            final String[] userFolders = NamespaceFoldersCache.getUserNamespaces(imapStore, true, session, accountId);
            if (userFolders.length > 0) {
                // final char sep = ListLsubCache.getSeparator(accountId, imapStore, session);
                for (int i = 0; i < userFolders.length; i++) {
                    if (userFolders[i].startsWith(fullName)) {
                        return true;
                    }
                    final List<ListLsubEntry> children = ListLsubCache.getCachedLISTEntry(userFolders[i], accountId, imapStore, session, ignoreLsub).getChildren();
                    for (final ListLsubEntry entry : children) {
                        if (entry.getFullName().startsWith(fullName)) {
                            return true;
                        }
                    }
                }
            }
        }
        {
            final String[] sharedFolders = NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session, accountId);
            for (int i = 0; i < sharedFolders.length; i++) {
                if (sharedFolders[i].startsWith(fullName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the unread count from given IMAP folder.
     *
     * @param imapFolder The IMAP folder
     * @return The unread count
     * @throws OXException If returning unread count fails
     */
    public static int getUnreadCount(final IMAPFolder imapFolder) throws OXException {
        try {
            final int[] status = IMAPCommandsCollection.getStatus(imapFolder);
            return status[2];
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the session user's own rights.
     * <p>
     * <b>Note</b>: This method assumes all preconditions were met (exists, selectable, etc.) to perform MYRIGHTS command on specified IMAP
     * folder.
     *
     * @param folder The IMAP folder
     * @param session The session
     * @param imapConfig The IMAP configuration
     * @return The own rights
     * @throws MessagingException In case an unrecoverable exception occurs
     */
    public static Rights getOwnRights(final IMAPFolder folder, final Session session, final IMAPConfig imapConfig) throws MessagingException {
        if (folder instanceof DefaultFolder) {
            return null;
        }
        final Rights retval;
        if (imapConfig.isSupportsACLs()) {
            try {
                retval = RightsCache.getCachedRights(folder, true, session, imapConfig.getAccountId());
            } catch (FolderClosedException e) {
                // Unable to recover...
                throw e;
            } catch (StoreClosedException e) {
                // Unable to recover...
                throw e;
            } catch (MessagingException e) {
                Exception nextException = e.getNextException();
                if ((nextException instanceof com.sun.mail.iap.CommandFailedException)) {
                    /*
                     * Handle command failed exception
                     */
                    handleCommandFailedException(((com.sun.mail.iap.CommandFailedException) nextException), folder.getFullName());
                    return null;
                }
                LOG.error("", e);
                /*
                 * Write empty string as rights. Nevertheless user may see folder!
                 */
                return new Rights();
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                LOG.error("", t);
                /*
                 * Write empty string as rights. Nevertheless user may see folder!
                 */
                return new Rights();
            }
        } else {
            /*
             * No ACLs enabled. User has full access.
             */
            retval = imapConfig.getACLExtension().getFullRights();
        }
        return retval;
    }

    private static void handleCommandFailedException(final com.sun.mail.iap.CommandFailedException e, final String fullName) {
        final String msg = e.getMessage().toLowerCase(Locale.ENGLISH);
        if (msg.indexOf("Mailbox doesn't exist") >= 0 || msg.indexOf("Mailbox does not exist") >= 0) {
            LOG.warn(IMAPException.getFormattedMessage(IMAPException.Code.FOLDER_NOT_FOUND, fullName), e);
        } else {
            LOG.debug("Failed MYRIGHTS for: {}", fullName, e);
        }
    }
}
