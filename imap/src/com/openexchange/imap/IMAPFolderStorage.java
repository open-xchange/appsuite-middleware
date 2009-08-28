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

package com.openexchange.imap;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import static com.openexchange.mail.utils.MailFolderUtility.isEmpty;
import static java.util.regex.Matcher.quoteReplacement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.ReadOnlyFolderException;
import javax.mail.StoreClosedException;
import javax.mail.Quota.Resource;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.imap.acl.ACLExtension;
import com.openexchange.imap.acl.ACLExtensionFactory;
import com.openexchange.imap.cache.NamespaceFoldersCache;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.converters.IMAPFolderConverter;
import com.openexchange.imap.entity2acl.Entity2ACL;
import com.openexchange.imap.entity2acl.Entity2ACLArgs;
import com.openexchange.imap.entity2acl.Entity2ACLException;
import com.openexchange.imap.util.IMAPSessionStorageAccess;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;

/**
 * {@link IMAPFolderStorage} - The IMAP folder storage implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPFolderStorage extends MailFolderStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPFolderStorage.class);

    private static final String STR_INBOX = "INBOX";

    private static final String STR_MSEC = "msec";

    private final IMAPStore imapStore;

    private final IMAPAccess imapAccess;

    private final int accountId;

    private final Session session;

    private final Context ctx;

    private final IMAPConfig imapConfig;

    private ACLExtension aclExtension;

    private Character separator;

    private IMAPDefaultFolderChecker checker;

    /**
     * Initializes a new {@link IMAPFolderStorage}
     * 
     * @param imapStore The IMAP store
     * @param imapAccess The IMAP access
     * @param session The session providing needed user data
     * @throws IMAPException If context loading fails
     */
    public IMAPFolderStorage(final IMAPStore imapStore, final IMAPAccess imapAccess, final Session session) throws IMAPException {
        super();
        this.imapStore = imapStore;
        this.imapAccess = imapAccess;
        this.accountId = imapAccess.getAccountId();
        this.session = session;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new IMAPException(e);
        }
        imapConfig = imapAccess.getIMAPConfig();
    }

    private IMAPDefaultFolderChecker getChecker() {
        if (null == checker) {
            checker = new IMAPDefaultFolderChecker(accountId, session, ctx, imapStore, imapConfig);
        }
        return checker;
    }

    private ACLExtension getACLExtension() throws IMAPException {
        if (null == aclExtension) {
            aclExtension = ACLExtensionFactory.getInstance().getACLExtension(imapConfig);
        }
        return aclExtension;
    }

    private char getSeparator() throws MessagingException {
        if (null == separator) {
            separator = Character.valueOf(imapStore.getDefaultFolder().getSeparator());
        }
        return separator.charValue();
    }

    @Override
    public boolean exists(final String fullname) throws MailException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                return true;
            }
            if (imapStore.getFolder(fullname).exists()) {
                return true;
            }
            return (checkForNamespaceFolder(fullname) != null);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    @Override
    public MailFolder getFolder(final String fullname) throws MailException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                return IMAPFolderConverter.convertFolder((IMAPFolder) imapStore.getDefaultFolder(), session, imapConfig, ctx);
            }
            IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullname);
            if (f.exists()) {
                return IMAPFolderConverter.convertFolder(f, session, imapConfig, ctx);
            }
            f = checkForNamespaceFolder(fullname);
            if (null == f) {
                throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
            }
            return IMAPFolderConverter.convertFolder(f, session, imapConfig, ctx);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    private static final String PATTERN_ALL = "%";

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException {
        try {
            IMAPFolder parent;
            if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
                parent = (IMAPFolder) imapStore.getDefaultFolder();
                final boolean subscribed = (!MailProperties.getInstance().isIgnoreSubscription() && !all);
                /*
                 * Request subfolders the usual way
                 */
                final List<Folder> subfolders = new ArrayList<Folder>();
                {
                    final IMAPFolder[] childFolders;
                    final long start = System.currentTimeMillis();
                    if (subscribed) {
                        childFolders = (IMAPFolder[]) parent.listSubscribed(PATTERN_ALL);
                        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    } else {
                        childFolders = (IMAPFolder[]) parent.list(PATTERN_ALL);
                        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    }
                    subfolders.addAll(Arrays.asList(childFolders));
                    boolean containsInbox = false;
                    for (int i = 0; i < childFolders.length && !containsInbox; i++) {
                        containsInbox = STR_INBOX.equals(childFolders[i].getFullName());
                    }
                    if (!containsInbox) {
                        /*
                         * Add folder INBOX manually
                         */
                        subfolders.add(0, imapStore.getFolder(STR_INBOX));
                    }
                }
                if (imapConfig.getImapCapabilities().hasNamespace()) {
                    /*
                     * Merge with namespace folders
                     */
                    {
                        mergeWithNamespaceFolders(subfolders, NamespaceFoldersCache.getPersonalNamespaces(
                            imapStore,
                            true,
                            session,
                            accountId), subscribed, parent);
                    }
                    {
                        mergeWithNamespaceFolders(
                            subfolders,
                            NamespaceFoldersCache.getUserNamespaces(imapStore, true, session, accountId),
                            subscribed,
                            parent);
                    }
                    {
                        mergeWithNamespaceFolders(
                            subfolders,
                            NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session, accountId),
                            subscribed,
                            parent);
                    }
                }
                /*
                 * Output subfolders
                 */
                final List<MailFolder> list = new ArrayList<MailFolder>(subfolders.size());
                for (final Folder subfolder : subfolders) {
                    list.add(IMAPFolderConverter.convertFolder((IMAPFolder) subfolder, session, imapConfig, ctx));
                }
                return list.toArray(new MailFolder[list.size()]);
            }
            parent = (IMAPFolder) imapStore.getFolder(parentFullname);
            if (parent.exists()) {
                /*
                 * Holds LOOK-UP right?
                 */
                if (imapConfig.isSupportsACLs() && isSelectable(parent)) {
                    try {
                        if (!getACLExtension().canLookUp(RightsCache.getCachedRights(parent, true, session, accountId))) {
                            throw IMAPException.create(IMAPException.Code.NO_LOOKUP_ACCESS, imapConfig, session, parentFullname);
                        }
                    } catch (final MessagingException e) {
                        throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, parentFullname);
                    }
                }
                return getSubfolderArray(all, parent);
            }
            /*
             * Check for namespace folder
             */
            parent = checkForNamespaceFolder(parentFullname);
            if (null != parent) {
                return getSubfolderArray(all, parent);
            }
            return EMPTY_PATH;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    private MailFolder[] getSubfolderArray(final boolean all, final IMAPFolder parent) throws MessagingException, MailException {
        final Folder[] subfolders;
        if (MailProperties.getInstance().isIgnoreSubscription() || all) {
            subfolders = parent.list(PATTERN_ALL);
        } else {
            subfolders = parent.listSubscribed(PATTERN_ALL);
        }
        final List<MailFolder> list = new ArrayList<MailFolder>(subfolders.length);
        for (int i = 0; i < subfolders.length; i++) {
            final MailFolder mo = IMAPFolderConverter.convertFolder((IMAPFolder) subfolders[i], session, imapConfig, ctx);
            if (mo.exists()) {
                list.add(mo);
            }
        }
        return list.toArray(new MailFolder[list.size()]);
    }

    private void mergeWithNamespaceFolders(final List<Folder> subfolders, final String[] namespaces, final boolean subscribed, final IMAPFolder defaultFolder) throws MessagingException {
        if (namespaces.length == 0) {
            return;
        }
        final String[] namespaceFolders = new String[namespaces.length];
        System.arraycopy(namespaces, 0, namespaceFolders, 0, namespaces.length);
        NextNSFolder: for (int i = 0; i < namespaceFolders.length; i++) {
            final String nsFullname = namespaceFolders[i];
            if ((nsFullname == null) || (nsFullname.length() == 0)) {
                namespaceFolders[i] = null;
                continue NextNSFolder;
            }
            for (final Folder subfolder : subfolders) {
                if (nsFullname.equals(subfolder.getFullName())) {
                    /*
                     * Namespace folder already contained in subfolder list
                     */
                    namespaceFolders[i] = null;
                    continue NextNSFolder;
                }
            }
        }
        final char sep = defaultFolder.getSeparator();
        if (subscribed) {
            /*
             * Remove not-subscribed namespace folders
             */
            for (int i = 0; i < namespaceFolders.length; i++) {
                final String nsFullname = namespaceFolders[i];
                if (nsFullname != null && !IMAPCommandsCollection.isSubscribed(nsFullname, sep, true, defaultFolder)) {
                    namespaceFolders[i] = null;
                }
            }
        }
        /*
         * Add remaining namespace folders to subfolder list
         */
        for (final String fullname : namespaceFolders) {
            if (fullname != null) {
                subfolders.add(new NamespaceFolder(imapStore, fullname, sep));
            }
        }
    }

    /**
     * Checks if given fullname matches a namespace folder
     * 
     * @param fullname The folder's fullname
     * @return The corresponding namespace folder or <code>null</code>
     * @throws MessagingException
     */
    private IMAPFolder checkForNamespaceFolder(final String fullname) throws MessagingException {
        if (NamespaceFoldersCache.containedInPersonalNamespaces(fullname, imapStore, true, session, accountId)) {
            return new NamespaceFolder(imapStore, fullname, getSeparator());
        }
        if (NamespaceFoldersCache.containedInUserNamespaces(fullname, imapStore, true, session, accountId)) {
            return new NamespaceFolder(imapStore, fullname, getSeparator());
        }
        if (NamespaceFoldersCache.containedInSharedNamespaces(fullname, imapStore, true, session, accountId)) {
            return new NamespaceFolder(imapStore, fullname, getSeparator());
        }
        return null;
    }

    @Override
    public MailFolder getRootFolder() throws MailException {
        try {
            return IMAPFolderConverter.convertFolder((IMAPFolder) imapStore.getDefaultFolder(), session, imapConfig, ctx);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    @Override
    public void checkDefaultFolders() throws MailException {
        getChecker().checkDefaultFolders();
    }

    private static final int FOLDER_TYPE = (Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws MailException {
        final String name = toCreate.getName();
        if (isEmpty(name)) {
            throw new MailException(MailException.Code.INVALID_FOLDER_NAME_EMPTY);
        }
        boolean created = false;
        IMAPFolder createMe = null;
        try {
            /*
             * Insert
             */
            String parentFullname = toCreate.getParentFullname();
            final boolean isParentDefault;
            IMAPFolder parent;
            if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
                parent = (IMAPFolder) imapStore.getDefaultFolder();
                parentFullname = parent.getFullName();
                isParentDefault = true;
            } else {
                if (toCreate.containsSeparator() && !checkFolderPathValidity(parentFullname, toCreate.getSeparator())) {
                    throw IMAPException.create(
                        IMAPException.Code.INVALID_FOLDER_NAME,
                        imapConfig,
                        session,
                        Character.valueOf(toCreate.getSeparator()));
                }
                parent = (IMAPFolder) imapStore.getFolder(parentFullname);
                isParentDefault = false;
            }
            if (!parent.exists()) {
                parent = checkForNamespaceFolder(parentFullname);
                if (null == parent) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, parentFullname);
                }
            }
            /*
             * Check if parent holds folders
             */
            if (!inferiors(parent)) {
                throw IMAPException.create(
                    IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS,
                    imapConfig,
                    session,
                    parent instanceof DefaultFolder ? DEFAULT_FOLDER_ID : parentFullname);
            }
            /*
             * Check ACLs if enabled
             */
            if (imapConfig.isSupportsACLs()) {
                try {
                    if (!getACLExtension().canCreate(RightsCache.getCachedRights(parent, true, session, accountId))) {
                        throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, parentFullname);
                    }
                } catch (final MessagingException e) {
                    /*
                     * MYRIGHTS command failed for given mailbox
                     */
                    if (!imapConfig.getImapCapabilities().hasNamespace() || !NamespaceFoldersCache.containedInPersonalNamespaces(
                        parent.getFullName(),
                        imapStore,
                        true,
                        session,
                        accountId)) {
                        /*
                         * No namespace support or given parent is NOT covered by user's personal namespaces.
                         */
                        throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, parentFullname);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("MYRIGHTS command failed on namespace folder", e);
                    }
                }
            }
            /*
             * Check if IMAP server is in MBox format; meaning folder either hold messages or subfolders but not both
             */
            final char separator = parent.getSeparator();
            final boolean mboxEnabled;
            {
                final String param = MailSessionParameterNames.getParamMBox(accountId);
                Boolean mbox = (Boolean) session.getParameter(param);
                if (null == mbox) {
                    mbox = Boolean.valueOf(!IMAPCommandsCollection.supportsFolderType(parent, FOLDER_TYPE, new StringBuilder(
                        parent.getFullName()).append(separator).toString()));
                    session.setParameter(param, mbox);
                }
                mboxEnabled = mbox.booleanValue();
            }
            if (!checkFolderNameValidity(name, separator, mboxEnabled)) {
                throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, Character.valueOf(separator));
            }
            if (isParentDefault) {
                /*
                 * Below default folder
                 */
                createMe = (IMAPFolder) imapStore.getFolder(name);
            } else {
                createMe = (IMAPFolder) imapStore.getFolder(new StringBuilder(parent.getFullName()).append(separator).append(name).toString());
            }
            if (createMe.exists()) {
                throw IMAPException.create(IMAPException.Code.DUPLICATE_FOLDER, imapConfig, session, createMe.getFullName());
            }
            final int ftype;
            if (mboxEnabled) {
                /*
                 * Determine folder creation type dependent on folder name
                 */
                ftype = createMe.getName().endsWith(String.valueOf(separator)) ? Folder.HOLDS_FOLDERS : Folder.HOLDS_MESSAGES;
            } else {
                ftype = FOLDER_TYPE;
            }
            try {
                if (!(created = createMe.create(ftype))) {
                    throw IMAPException.create(
                        IMAPException.Code.FOLDER_CREATION_FAILED,
                        imapConfig,
                        session,
                        createMe.getFullName(),
                        parent instanceof DefaultFolder ? DEFAULT_FOLDER_ID : parent.getFullName());
                }
            } catch (final MessagingException e) {
                if ("Unsupported type".equals(e.getMessage())) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            "IMAP folder creation failed due to unsupported type." + " Going to retry with fallback type HOLDS-MESSAGES.",
                            e);
                    }
                    if (!(created = createMe.create(Folder.HOLDS_MESSAGES))) {
                        throw IMAPException.create(
                            IMAPException.Code.FOLDER_CREATION_FAILED,
                            imapConfig,
                            session,
                            e,
                            createMe.getFullName(),
                            parent instanceof DefaultFolder ? DEFAULT_FOLDER_ID : parent.getFullName());
                    }
                    if (LOG.isInfoEnabled()) {
                        LOG.info("IMAP folder created with fallback type HOLDS_MESSAGES");
                    }
                } else {
                    throw MIMEMailException.handleMessagingException(e, imapConfig, session);
                }
            }
            /*
             * Subscribe
             */
            if (!MailProperties.getInstance().isSupportSubscription()) {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, createMe.getFullName(), true);
            } else if (toCreate.containsSubscribed()) {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, createMe.getFullName(), toCreate.isSubscribed());
            } else {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, createMe.getFullName(), true);
            }
            if (imapConfig.isSupportsACLs() && toCreate.containsPermissions()) {
                final ACL[] initialACLs = getACLSafe(createMe);
                if (initialACLs != null) {
                    final ACL[] newACLs = permissions2ACL(toCreate.getPermissions(), createMe);
                    final Entity2ACL entity2ACL = Entity2ACL.getInstance(imapConfig);
                    final Entity2ACLArgs args = IMAPFolderConverter.getEntity2AclArgs(session, createMe, imapConfig);
                    final Map<String, ACL> m = acl2map(newACLs);
                    if (!equals(initialACLs, m, entity2ACL, args)) {
                        final ACLExtension aclExtension = getACLExtension();
                        if (!aclExtension.canSetACL(createMe.myRights())) {
                            throw IMAPException.create(
                                IMAPException.Code.NO_ADMINISTER_ACCESS_ON_INITIAL,
                                imapConfig,
                                session,
                                createMe.getFullName());
                        }
                        boolean adminFound = false;
                        for (int i = 0; (i < newACLs.length) && !adminFound; i++) {
                            if (aclExtension.canSetACL(newACLs[i].getRights())) {
                                adminFound = true;
                            }
                        }
                        if (!adminFound) {
                            throw IMAPException.create(IMAPException.Code.NO_ADMIN_ACL, imapConfig, session, createMe.getFullName());
                        }
                        /*
                         * Apply new ACLs
                         */
                        final Map<String, ACL> om = acl2map(initialACLs);
                        for (int i = 0; i < newACLs.length; i++) {
                            createMe.addACL(validate(newACLs[i], om));
                        }
                        /*
                         * Remove other ACLs
                         */
                        final ACL[] removedACLs = getRemovedACLs(m, initialACLs);
                        if (removedACLs.length > 0) {
                            for (int i = 0; i < removedACLs.length; i++) {
                                if (isKnownEntity(removedACLs[i].getName(), entity2ACL, ctx, args)) {
                                    createMe.removeACL(removedACLs[i].getName());
                                }
                            }
                        }
                    }
                }
            }
            return createMe.getFullName();
        } catch (final MessagingException e) {
            if (createMe != null && created) {
                try {
                    if (createMe.exists()) {
                        createMe.delete(true);
                    }
                } catch (final Throwable e2) {
                    LOG.error(new StringBuilder().append("Temporary created IMAP folder \"").append(createMe.getFullName()).append(
                        "could not be deleted"), e2);
                }
            }
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        } catch (final MailException e) {
            if (createMe != null && created) {
                try {
                    if (createMe.exists()) {
                        createMe.delete(true);
                    }
                } catch (final Throwable e2) {
                    LOG.error(new StringBuilder().append("Temporary created IMAP folder \"").append(createMe.getFullName()).append(
                        "could not be deleted"), e2);
                }
            }
            throw e;
        } catch (final AbstractOXException e) {
            if (createMe != null && created) {
                try {
                    if (createMe.exists()) {
                        createMe.delete(true);
                    }
                } catch (final Throwable e2) {
                    LOG.error(new StringBuilder().append("Temporary created IMAP folder \"").append(createMe.getFullName()).append(
                        "could not be deleted"), e2);
                }
            }
            throw new IMAPException(e);
        } catch (final Exception e) {
            if (createMe != null && created) {
                try {
                    if (createMe.exists()) {
                        createMe.delete(true);
                    }
                } catch (final Throwable e2) {
                    LOG.error(new StringBuilder().append("Temporary created IMAP folder \"").append(createMe.getFullName()).append(
                        "could not be deleted"), e2);
                }
            }
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        }
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname) || DEFAULT_FOLDER_ID.equals(newFullname)) {
            throw IMAPException.create(IMAPException.Code.NO_ROOT_MOVE, imapConfig, session, new Object[0]);
        }
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                throw new MailException(MailException.Code.NO_ROOT_FOLDER_MODIFY_DELETE);
            }
            IMAPFolder moveMe = (IMAPFolder) imapStore.getFolder(fullname);
            if (!moveMe.exists()) {
                moveMe = checkForNamespaceFolder(fullname);
                if (null == moveMe) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
                }
            }
            /*
             * Notify message storage about outstanding move
             */
            imapAccess.getMessageStorage().notifyIMAPFolderModification(fullname);
            final char separator = moveMe.getSeparator();
            final String oldParent = moveMe.getParent().getFullName();
            final String newParent;
            final String newName;
            {
                final int pos = newFullname.lastIndexOf(separator);
                if (pos == -1) {
                    newParent = "";
                    newName = newFullname;
                } else {
                    if (pos == newFullname.length() - 1) {
                        throw IMAPException.create(
                            IMAPException.Code.INVALID_FOLDER_NAME,
                            imapConfig,
                            session,
                            Character.valueOf(separator));
                    }
                    newParent = newFullname.substring(0, pos);
                    if (!checkFolderPathValidity(newParent, separator)) {
                        throw IMAPException.create(
                            IMAPException.Code.INVALID_FOLDER_NAME,
                            imapConfig,
                            session,
                            Character.valueOf(separator));
                    }
                    newName = newFullname.substring(pos + 1);
                }
            }
            /*
             * Check for move
             */
            final boolean move = !newParent.equals(oldParent);
            /*
             * Check for rename. Rename must not be performed if a move has already been done
             */
            final boolean rename = (!move && !newName.equals(moveMe.getName()));
            if (move) {
                /*
                 * Perform move operation
                 */
                final String oldFullname = moveMe.getFullName();
                if (getChecker().isDefaultFolder(oldFullname)) {
                    throw IMAPException.create(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, imapConfig, session, oldFullname);
                }
                IMAPFolder destFolder = ((IMAPFolder) (MailFolder.DEFAULT_FOLDER_ID.equals(newParent) ? imapStore.getDefaultFolder() : imapStore.getFolder(newParent)));
                if (!destFolder.exists()) {
                    destFolder = checkForNamespaceFolder(newParent);
                    if (null == destFolder) {
                        /*
                         * Destination folder could not be found, thus an invalid name was specified by user
                         */
                        throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, newParent);
                    }
                }
                if (!inferiors(destFolder)) {
                    throw IMAPException.create(
                        IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS,
                        imapConfig,
                        session,
                        destFolder.getFullName());
                }
                if (imapConfig.isSupportsACLs() && isSelectable(destFolder)) {
                    try {
                        if (!getACLExtension().canCreate(RightsCache.getCachedRights(destFolder, true, session, accountId))) {
                            throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, newParent);
                        }
                    } catch (final MessagingException e) {
                        /*
                         * MYRIGHTS command failed for given mailbox
                         */
                        if (!imapConfig.getImapCapabilities().hasNamespace() || !NamespaceFoldersCache.containedInPersonalNamespaces(
                            newParent,
                            imapStore,
                            true,
                            session,
                            accountId)) {
                            /*
                             * No namespace support or given parent is NOT covered by user's personal namespaces.
                             */
                            throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, newParent);
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("MYRIGHTS command failed on namespace folder", e);
                        }
                    }
                }
                final boolean mboxEnabled;
                {
                    final String param = MailSessionParameterNames.getParamMBox(accountId);
                    Boolean mbox = (Boolean) session.getParameter(param);
                    if (null == mbox) {
                        mbox = Boolean.valueOf(!IMAPCommandsCollection.supportsFolderType(destFolder, FOLDER_TYPE, new StringBuilder(
                            destFolder.getFullName()).append(separator).toString()));
                        session.setParameter(param, mbox);
                    }
                    mboxEnabled = mbox.booleanValue();
                }
                if (!checkFolderNameValidity(newName, separator, mboxEnabled)) {
                    throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, Character.valueOf(separator));
                }

                if (destFolder.getFullName().startsWith(oldFullname)) {
                    throw IMAPException.create(
                        IMAPException.Code.NO_MOVE_TO_SUBFLD,
                        imapConfig,
                        session,
                        moveMe.getName(),
                        destFolder.getName());
                }
                try {
                    moveMe = moveFolder(moveMe, destFolder, newName);
                } catch (final MailException e) {
                    deleteTemporaryCreatedFolder(destFolder, newName);
                    throw e;
                } catch (final MessagingException e) {
                    deleteTemporaryCreatedFolder(destFolder, newName);
                    throw e;
                }
            }
            /*
             * Is rename operation?
             */
            if (rename) {
                /*
                 * Perform rename operation
                 */
                if (getChecker().isDefaultFolder(moveMe.getFullName())) {
                    throw IMAPException.create(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, imapConfig, session, moveMe.getFullName());
                } else if (imapConfig.isSupportsACLs() && isSelectable(moveMe)) {
                    try {
                        if (!getACLExtension().canCreate(RightsCache.getCachedRights(moveMe, true, session, accountId))) {
                            throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, moveMe.getFullName());
                        }
                    } catch (final MessagingException e) {
                        throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, moveMe.getFullName());
                    }
                }
                /*
                 * Rename can only be invoked on a closed folder
                 */
                if (moveMe.isOpen()) {
                    moveMe.close(false);
                }
                final boolean mboxEnabled;
                final IMAPFolder renameFolder;
                {
                    final IMAPFolder par = (IMAPFolder) moveMe.getParent();
                    final String parentFullName = par.getFullName();
                    final StringBuilder tmp = new StringBuilder();
                    if (parentFullName.length() > 0) {
                        tmp.append(parentFullName).append(separator);
                    }
                    tmp.append(newName);
                    renameFolder = (IMAPFolder) imapStore.getFolder(tmp.toString());
                    final String param = MailSessionParameterNames.getParamMBox(accountId);
                    // Check for MBox format
                    Boolean mbox = (Boolean) session.getParameter(param);
                    if (null == mbox) {
                        mbox = Boolean.valueOf(IMAPCommandsCollection.supportsFolderType(par, FOLDER_TYPE, new StringBuilder(
                            par.getFullName()).append(separator).toString()));
                        session.setParameter(param, mbox);
                    }
                    mboxEnabled = mbox.booleanValue();
                }
                if (renameFolder.exists()) {
                    throw IMAPException.create(IMAPException.Code.DUPLICATE_FOLDER, imapConfig, session, renameFolder.getFullName());
                }
                if (!checkFolderNameValidity(newName, separator, mboxEnabled)) {
                    throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, Character.valueOf(separator));
                }
                /*
                 * Remember subscription status
                 */
                Map<String, Boolean> subscriptionStatus;
                final String newFullName = renameFolder.getFullName();
                final String oldFullName = moveMe.getFullName();
                try {
                    subscriptionStatus = getSubscriptionStatus(moveMe, oldFullName, newFullName);
                } catch (final MessagingException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(new StringBuilder(128).append("Subscription status of folder \"").append(moveMe.getFullName()).append(
                            "\" and its subfolders could not be stored prior to rename operation"));
                    }
                    subscriptionStatus = null;
                }
                removeSessionData(moveMe);
                /*
                 * Rename
                 */
                boolean success = false;
                final long start = System.currentTimeMillis();
                success = moveMe.renameTo(renameFolder);
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                /*
                 * Success?
                 */
                if (!success) {
                    throw IMAPException.create(IMAPException.Code.UPDATE_FAILED, imapConfig, session, moveMe.getFullName());
                }
                moveMe = (IMAPFolder) imapStore.getFolder(oldFullName);
                if (moveMe.exists()) {
                    deleteFolder(moveMe);
                }
                moveMe = (IMAPFolder) imapStore.getFolder(newFullName);
                /*
                 * Apply remembered subscription status
                 */
                if (subscriptionStatus == null) {
                    /*
                     * At least subscribe to renamed folder
                     */
                    moveMe.setSubscribed(true);
                } else {
                    applySubscriptionStatus(moveMe, subscriptionStatus);
                }
            }
            return moveMe.getFullName();
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        } catch (final IMAPException e) {
            throw e;
        } catch (final AbstractOXException e) {
            throw new IMAPException(e);
        }
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws MailException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                throw new MailException(MailException.Code.NO_ROOT_FOLDER_MODIFY_DELETE);
            }
            IMAPFolder updateMe = (IMAPFolder) imapStore.getFolder(fullname);
            if (!updateMe.exists()) {
                updateMe = checkForNamespaceFolder(fullname);
                if (null == updateMe) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
                }
            }
            /*
             * Notify message storage
             */
            imapAccess.getMessageStorage().notifyIMAPFolderModification(fullname);
            /*
             * Proceed update
             */
            if (imapConfig.isSupportsACLs() && toUpdate.containsPermissions()) {
                final ACL[] oldACLs = getACLSafe(updateMe);
                if (oldACLs != null) {
                    final ACL[] newACLs = permissions2ACL(toUpdate.getPermissions(), updateMe);
                    final Entity2ACL entity2ACL = Entity2ACL.getInstance(imapConfig);
                    final Entity2ACLArgs args = IMAPFolderConverter.getEntity2AclArgs(session, updateMe, imapConfig);
                    final Map<String, ACL> m = acl2map(newACLs);
                    if (!equals(oldACLs, m, entity2ACL, args)) {
                        /*
                         * Default folder is affected, check if owner still holds full rights
                         */
                        if (getChecker().isDefaultFolder(updateMe.getFullName()) && !stillHoldsFullRights(
                            updateMe,
                            newACLs,
                            imapConfig,
                            session,
                            ctx)) {
                            throw IMAPException.create(
                                IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE,
                                imapConfig,
                                session,
                                updateMe.getFullName());
                        }
                        final ACLExtension aclExtension = getACLExtension();
                        if (!aclExtension.canSetACL(RightsCache.getCachedRights(updateMe, true, session, accountId))) {
                            throw IMAPException.create(IMAPException.Code.NO_ADMINISTER_ACCESS, imapConfig, session, updateMe.getFullName());
                        }
                        /*
                         * Check new ACLs
                         */
                        if (newACLs.length == 0) {
                            throw IMAPException.create(IMAPException.Code.NO_ADMIN_ACL, imapConfig, session, updateMe.getFullName());
                        }
                        {
                            boolean adminFound = false;
                            for (int i = 0; (i < newACLs.length) && !adminFound; i++) {
                                if (aclExtension.canSetACL(newACLs[i].getRights())) {
                                    adminFound = true;
                                }
                            }
                            if (!adminFound) {
                                throw IMAPException.create(IMAPException.Code.NO_ADMIN_ACL, imapConfig, session, updateMe.getFullName());
                            }
                        }
                        /*
                         * Remove deleted ACLs
                         */
                        final ACL[] removedACLs = getRemovedACLs(m, oldACLs);
                        if (removedACLs.length > 0) {
                            for (int i = 0; i < removedACLs.length; i++) {
                                if (isKnownEntity(removedACLs[i].getName(), entity2ACL, ctx, args)) {
                                    updateMe.removeACL(removedACLs[i].getName());
                                }
                            }
                        }
                        /*
                         * Change existing ACLs according to new ACLs
                         */
                        final Map<String, ACL> om = acl2map(oldACLs);
                        for (int i = 0; i < newACLs.length; i++) {
                            updateMe.addACL(validate(newACLs[i], om));
                        }
                        /*
                         * Since the ACLs have changed remove cached rights
                         */
                        RightsCache.removeCachedRights(updateMe, session, accountId);
                    }
                }
            }
            if (!MailProperties.getInstance().isIgnoreSubscription() && toUpdate.containsSubscribed()) {
                updateMe.setSubscribed(toUpdate.isSubscribed());
                IMAPCommandsCollection.forceSetSubscribed(imapStore, updateMe.getFullName(), toUpdate.isSubscribed());
            }
            return updateMe.getFullName();
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        } catch (final IMAPException e) {
            throw e;
        } catch (final AbstractOXException e) {
            throw new IMAPException(e);
        }
    }

    private void deleteTemporaryCreatedFolder(final IMAPFolder destFolder, final String name) throws MessagingException {
        /*
         * Delete moved folder if operation failed
         */
        final IMAPFolder tmp = (IMAPFolder) imapStore.getFolder(new StringBuilder(destFolder.getFullName()).append(
            destFolder.getSeparator()).append(name).toString());
        if (tmp.exists()) {
            try {
                tmp.delete(true);
            } catch (final MessagingException e1) {
                LOG.error("Temporary created folder could not be deleted: " + tmp.getFullName(), e1);
            }
        }
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws MailException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                throw new MailException(MailException.Code.NO_ROOT_FOLDER_MODIFY_DELETE);
            }
            IMAPFolder deleteMe = (IMAPFolder) imapStore.getFolder(fullname);
            if (!deleteMe.exists()) {
                deleteMe = checkForNamespaceFolder(fullname);
                if (null == deleteMe) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
                }
            }
            imapAccess.getMessageStorage().notifyIMAPFolderModification(fullname);
            if (hardDelete) {
                /*
                 * Delete permanently
                 */
                deleteFolder(deleteMe);
            } else {
                final IMAPFolder trashFolder = (IMAPFolder) imapStore.getFolder(getTrashFolder());
                if (deleteMe.getParent().getFullName().startsWith(trashFolder.getFullName()) || !inferiors(trashFolder)) {
                    /*
                     * Delete permanently
                     */
                    deleteFolder(deleteMe);
                } else {
                    /*
                     * Just move this folder to trash
                     */
                    imapAccess.getMessageStorage().notifyIMAPFolderModification(trashFolder.getFullName());
                    final String name = deleteMe.getName();
                    int appendix = 1;
                    final StringBuilder sb = new StringBuilder();
                    IMAPFolder newFolder = (IMAPFolder) imapStore.getFolder(sb.append(trashFolder.getFullName()).append(
                        deleteMe.getSeparator()).append(name).toString());
                    while (newFolder.exists()) {
                        /*
                         * A folder of the same name already exists. Append appropriate appendix to folder name and check existence again.
                         */
                        sb.setLength(0);
                        newFolder = (IMAPFolder) imapStore.getFolder(sb.append(trashFolder.getFullName()).append(deleteMe.getSeparator()).append(
                            name).append('_').append(++appendix).toString());
                    }
                    try {
                        moveFolder(deleteMe, trashFolder, newFolder, false);
                    } catch (final MailException e) {
                        deleteTemporaryCreatedFolder(trashFolder, newFolder.getName());
                        throw e;
                    } catch (final MessagingException e) {
                        deleteTemporaryCreatedFolder(trashFolder, newFolder.getName());
                        throw e;
                    }
                }
            }
            return fullname;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws MailException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                throw new MailException(MailException.Code.NO_ROOT_FOLDER_MODIFY_DELETE);
            }
            IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullname);
            if (!f.exists()) {
                f = checkForNamespaceFolder(fullname);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
                }
            }
            imapAccess.getMessageStorage().notifyIMAPFolderModification(fullname);
            try {
                if (!isSelectable(f)) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, f.getFullName());
                }
                if (imapConfig.isSupportsACLs()) {
                    final Rights myrights = RightsCache.getCachedRights(f, true, session, accountId);
                    if (!getACLExtension().canRead(myrights)) {
                        throw IMAPException.create(IMAPException.Code.NO_READ_ACCESS, imapConfig, session, f.getFullName());
                    }
                    if (!getACLExtension().canDeleteMessages(myrights)) {
                        throw IMAPException.create(IMAPException.Code.NO_DELETE_ACCESS, imapConfig, session, f.getFullName());
                    }
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, f.getFullName());
            }
            /*
             * Remove from session storage
             */
            IMAPSessionStorageAccess.removeDeletedFolder(accountId, session, fullname);
            f.open(Folder.READ_WRITE);
            try {
                int msgCount = f.getMessageCount();
                if (msgCount == 0) {
                    /*
                     * Empty folder
                     */
                    return;
                }
                String trashFullname = null;
                final boolean backup = (!hardDelete && !UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs() && !(f.getFullName().startsWith((trashFullname = getTrashFolder()))));
                if (backup) {
                    imapAccess.getMessageStorage().notifyIMAPFolderModification(trashFullname);
                }
                final StringBuilder debug;
                if (LOG.isDebugEnabled()) {
                    debug = new StringBuilder(128);
                } else {
                    debug = null;
                }
                final int blockSize = imapConfig.getIMAPProperties().getBlockSize();
                final long startClear = System.currentTimeMillis();
                if (blockSize > 0) {
                    /*
                     * Block-wise deletion
                     */
                    while (msgCount > blockSize) {
                        /*
                         * Don't adapt sequence number since folder expunge already resets message numbering
                         */
                        if (backup) {
                            try {
                                final long startCopy = System.currentTimeMillis();
                                new CopyIMAPCommand(f, 1, blockSize, trashFullname).doCommand();
                                if (LOG.isDebugEnabled()) {
                                    debug.setLength(0);
                                    LOG.debug(debug.append("\"Soft Clear\": ").append("Messages copied to default trash folder \"").append(
                                        trashFullname).append("\" in ").append((System.currentTimeMillis() - startCopy)).append(STR_MSEC).toString());
                                }
                            } catch (final MessagingException e) {
                                if (e.getMessage().indexOf("Over quota") > -1) {
                                    /*
                                     * We face an Over-Quota-Exception
                                     */
                                    throw new MailException(MailException.Code.DELETE_FAILED_OVER_QUOTA, e, new Object[0]);
                                }
                                final Exception nestedExc = e.getNextException();
                                if (nestedExc != null && nestedExc.getMessage().indexOf("Over quota") > -1) {
                                    /*
                                     * We face an Over-Quota-Exception
                                     */
                                    throw new MailException(MailException.Code.DELETE_FAILED_OVER_QUOTA, e, new Object[0]);
                                }
                                throw IMAPException.create(IMAPException.Code.MOVE_ON_DELETE_FAILED, imapConfig, session, e, new Object[0]);
                            }
                        }
                        /*
                         * Delete through storing \Deleted flag...
                         */
                        new FlagsIMAPCommand(f, 1, blockSize, FLAGS_DELETED, true, true).doCommand();
                        /*
                         * ... and perform EXPUNGE
                         */
                        final long startExpunge = System.currentTimeMillis();
                        try {
                            IMAPCommandsCollection.fastExpunge(f);
                            if (LOG.isDebugEnabled()) {
                                debug.setLength(0);
                                LOG.debug(debug.append("EXPUNGE command executed on \"").append(f.getFullName()).append("\" in ").append(
                                    (System.currentTimeMillis() - startExpunge)).append(STR_MSEC).toString());
                            }
                        } catch (final FolderClosedException e) {
                            /*
                             * Not possible to retry since connection is broken
                             */
                            if (LOG.isDebugEnabled()) {
                                debug.setLength(0);
                                LOG.debug(debug.append("EXPUNGE command timed out in ").append((System.currentTimeMillis() - startExpunge)).append(
                                    STR_MSEC).toString());
                            }
                            throw IMAPException.create(
                                IMAPException.Code.CONNECT_ERROR,
                                imapConfig,
                                session,
                                e,
                                imapConfig.getServer(),
                                imapConfig.getLogin());
                        } catch (final StoreClosedException e) {
                            /*
                             * Not possible to retry since connection is broken
                             */
                            if (LOG.isDebugEnabled()) {
                                debug.setLength(0);
                                LOG.debug(debug.append("EXPUNGE command timed out in ").append((System.currentTimeMillis() - startExpunge)).append(
                                    STR_MSEC).toString());
                            }
                            throw IMAPException.create(
                                IMAPException.Code.CONNECT_ERROR,
                                imapConfig,
                                session,
                                e,
                                imapConfig.getServer(),
                                imapConfig.getLogin());
                        }
                        /*
                         * Decrement
                         */
                        msgCount -= blockSize;
                    }
                }
                if (msgCount == 0) {
                    /*
                     * All messages already cleared through previous block-wise deletion
                     */
                    return;
                }
                if (backup) {
                    try {
                        final long startCopy = System.currentTimeMillis();
                        new CopyIMAPCommand(f, trashFullname).doCommand();
                        if (LOG.isDebugEnabled()) {
                            debug.setLength(0);
                            LOG.debug(debug.append("\"Soft Clear\": ").append("Messages copied to default trash folder \"").append(
                                trashFullname).append("\" in ").append((System.currentTimeMillis() - startCopy)).append(STR_MSEC).toString());
                        }
                    } catch (final MessagingException e) {
                        if (e.getNextException() instanceof CommandFailedException) {
                            final CommandFailedException exc = (CommandFailedException) e.getNextException();
                            if (exc.getMessage().indexOf("Over quota") > -1) {
                                /*
                                 * We face an Over-Quota-Exception
                                 */
                                throw new MailException(MailException.Code.DELETE_FAILED_OVER_QUOTA, e, new Object[0]);
                            }
                        }
                        throw IMAPException.create(IMAPException.Code.MOVE_ON_DELETE_FAILED, imapConfig, session, e, new Object[0]);
                    }
                }
                /*
                 * Delete through storing \Deleted flag...
                 */
                new FlagsIMAPCommand(f, FLAGS_DELETED, true, true).doCommand();
                /*
                 * ... and perform EXPUNGE
                 */
                final long start = System.currentTimeMillis();
                IMAPCommandsCollection.fastExpunge(f);
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                if (LOG.isDebugEnabled()) {
                    debug.setLength(0);
                    LOG.info(debug.append("Folder '").append(fullname).append("' cleared in ").append(
                        System.currentTimeMillis() - startClear).append(STR_MSEC));
                }
            } finally {
                f.close(false);
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        } catch (final AbstractOXException e) {
            throw new IMAPException(e);
        }
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
        try {
            if (fullname.equals(DEFAULT_FOLDER_ID)) {
                return EMPTY_PATH;
            }
            IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullname);
            if (!f.exists()) {
                f = checkForNamespaceFolder(fullname);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
                }
            }
            if (imapConfig.isSupportsACLs() && isSelectable(f)) {
                try {
                    if (!getACLExtension().canLookUp(RightsCache.getCachedRights(f, true, session, accountId))) {
                        throw IMAPException.create(IMAPException.Code.NO_LOOKUP_ACCESS, imapConfig, session, fullname);
                    }
                } catch (final MessagingException e) {
                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, fullname);
                }
            }
            final List<MailFolder> list = new ArrayList<MailFolder>();
            final String defaultFolder = imapStore.getDefaultFolder().getFullName();
            while (!f.getFullName().equals(defaultFolder)) {
                list.add(IMAPFolderConverter.convertFolder(f, session, imapConfig, ctx));
                f = (IMAPFolder) f.getParent();
            }
            return list.toArray(new MailFolder[list.size()]);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    @Override
    public String getConfirmedHamFolder() throws MailException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_CONFIRMED_HAM);
    }

    @Override
    public String getConfirmedSpamFolder() throws MailException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_CONFIRMED_SPAM);
    }

    @Override
    public String getDraftsFolder() throws MailException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_DRAFTS);
    }

    @Override
    public String getSentFolder() throws MailException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_SENT);
    }

    @Override
    public String getSpamFolder() throws MailException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_SPAM);
    }

    @Override
    public String getTrashFolder() throws MailException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_TRASH);
    }

    @Override
    public void releaseResources() throws IMAPException {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws MailException {
        try {
            final IMAPFolder f;
            {
                final String fullname = folder == null ? STR_INBOX : folder;
                final boolean isDefaultFolder = fullname.equals(DEFAULT_FOLDER_ID);
                f = (IMAPFolder) (isDefaultFolder ? imapStore.getDefaultFolder() : imapStore.getFolder(fullname));
                if (!isDefaultFolder && !f.exists()) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
                }
                try {
                    if (!isSelectable(f)) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, fullname);
                    }
                    if (imapConfig.isSupportsACLs()) {
                        final Rights myrights = RightsCache.getCachedRights(f, true, session, accountId);
                        if (!getACLExtension().canRead(myrights)) {
                            throw IMAPException.create(IMAPException.Code.NO_READ_ACCESS, imapConfig, session, fullname);
                        }
                        if (!getACLExtension().canDeleteMailbox(myrights)) {
                            throw IMAPException.create(IMAPException.Code.NO_DELETE_ACCESS, imapConfig, session, fullname);
                        }
                    }
                } catch (final MessagingException e) {
                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, fullname);
                }
            }
            f.open(Folder.READ_ONLY);
            if (!imapConfig.getImapCapabilities().hasQuota()) {
                return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
            }
            Quota[] folderQuota = null;
            try {
                final long start = System.currentTimeMillis();
                folderQuota = f.getQuota();
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            } catch (final MessagingException mexc) {
                if (mexc.getNextException() instanceof ParsingException) {
                    try {
                        final long start = System.currentTimeMillis();
                        folderQuota = IMAPCommandsCollection.getQuotaRoot(f);
                        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    } catch (final MessagingException inner) {
                        /*
                         * Custom parse routine failed, too
                         */
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(inner.getMessage(), inner);
                        }
                        return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
                    }
                } else {
                    throw mexc;
                }
            }
            if (folderQuota == null || folderQuota.length == 0) {
                return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
            }
            final Quota.Resource[] resources = folderQuota[0].resources;
            if (resources.length == 0) {
                return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
            }
            final com.openexchange.mail.Quota[] quotas = new com.openexchange.mail.Quota[types.length];
            for (int i = 0; i < types.length; i++) {
                final String typeStr = types[i].toString();
                /*
                 * Find corresponding resource to current type
                 */
                Resource resource = null;
                for (int k = 0; k < resources.length && resource == null; k++) {
                    if (typeStr.equalsIgnoreCase(resources[k].name)) {
                        resource = resources[k];
                    }
                }
                if (resource == null) {
                    /*
                     * No quota limitation found that applies to current resource type
                     */
                    quotas[i] = com.openexchange.mail.Quota.getUnlimitedQuota(types[i]);
                } else {
                    quotas[i] = new com.openexchange.mail.Quota(resource.limit, resource.usage, types[i]);
                }
            }
            return quotas;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    /*
     * ++++++++++++++++++ Helper methods ++++++++++++++++++
     */

    /*-
     * Get the QUOTA resource with the highest usage-per-limitation value
     * 
     * @param resources The QUOTA resources
     * @return The QUOTA resource with the highest usage to limitation relation
     * 
     * 
     * private static Resource getMaxUsageResource(final Quota.Resource[] resources) {
     *     final Resource maxUsageResource;
     *     {
     *         int index = 0;
     *         long maxUsage = resources[0].usage / resources[0].limit;
     *         for (int i = 1; i &lt; resources.length; i++) {
     *             final long tmp = resources[i].usage / resources[i].limit;
     *             if (tmp &gt; maxUsage) {
     *                 maxUsage = tmp;
     *                 index = i;
     *             }
     *         }
     *         maxUsageResource = resources[index];
     *     }
     *     return maxUsageResource;
     * }
     */

    /**
     * Get the ACL list of specified folder
     * 
     * @param imapFolder The IMAP folder
     * @return The ACL list or <code>null</code> if any error occurred
     */
    private static ACL[] getACLSafe(final IMAPFolder imapFolder) {
        try {
            return imapFolder.getACL();
        } catch (final MessagingException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
            return null;
        }
    }

    private void deleteFolder(final IMAPFolder deleteMe) throws MailException, MessagingException {
        final String fullName = deleteMe.getFullName();
        if (getChecker().isDefaultFolder(fullName)) {
            throw IMAPException.create(IMAPException.Code.NO_DEFAULT_FOLDER_DELETE, imapConfig, session, fullName);
        } else if (!deleteMe.exists()) {
            throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
        }
        try {
            if (imapConfig.isSupportsACLs() && isSelectable(deleteMe) && !getACLExtension().canDeleteMailbox(
                RightsCache.getCachedRights(deleteMe, true, session, accountId))) {
                throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, fullName);
            }
        } catch (final MessagingException e) {
            throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, fullName);
        }
        if (deleteMe.isOpen()) {
            deleteMe.close(false);
        }
        /*
         * Unsubscribe prior to deletion
         */
        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, false);
        removeSessionData(deleteMe);
        final long start = System.currentTimeMillis();
        if (!deleteMe.delete(true)) {
            throw IMAPException.create(IMAPException.Code.DELETE_FAILED, imapConfig, session, fullName);
        }
        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        /*
         * Remove cache entries
         */
        RightsCache.removeCachedRights(deleteMe, session, accountId);
        UserFlagsCache.removeUserFlags(deleteMe, session, accountId);
    }

    private static final transient Rights FULL_RIGHTS = new Rights("lrswipcda");

    private static boolean stillHoldsFullRights(final IMAPFolder defaultFolder, final ACL[] newACLs, final IMAPConfig imapConfig, final Session session, final Context ctx) throws AbstractOXException, MessagingException {
        /*
         * Ensure that owner still holds full rights
         */
        final String ownerACLName = Entity2ACL.getInstance(imapConfig).getACLName(
            session.getUserId(),
            ctx,
            IMAPFolderConverter.getEntity2AclArgs(session, defaultFolder, imapConfig));
        for (int i = 0; i < newACLs.length; i++) {
            if (newACLs[i].getName().equals(ownerACLName) && newACLs[i].getRights().contains(FULL_RIGHTS)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Boolean> getSubscriptionStatus(final IMAPFolder f, final String oldFullName, final String newFullName) throws MessagingException {
        final Map<String, Boolean> retval = new HashMap<String, Boolean>();
        getSubscriptionStatus(retval, f, oldFullName, newFullName);
        return retval;
    }

    private static void getSubscriptionStatus(final Map<String, Boolean> m, final IMAPFolder f, final String oldFullName, final String newFullName) throws MessagingException {
        if (inferiors(f)) {
            final Folder[] folders = f.list();
            for (int i = 0; i < folders.length; i++) {
                getSubscriptionStatus(m, (IMAPFolder) folders[i], oldFullName, newFullName);
            }
        }
        m.put(f.getFullName().replaceFirst(oldFullName, quoteReplacement(newFullName)), Boolean.valueOf(f.isSubscribed()));
    }

    private static void applySubscriptionStatus(final IMAPFolder f, final Map<String, Boolean> m) throws MessagingException {
        if (inferiors(f)) {
            final Folder[] folders = f.list();
            for (int i = 0; i < folders.length; i++) {
                applySubscriptionStatus((IMAPFolder) folders[i], m);
            }
        }
        Boolean b = m.get(f.getFullName());
        if (b == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(new StringBuilder(128).append("No stored subscription status found for \"").append(f.getFullName()).append('"').toString());
            }
            b = Boolean.TRUE;
        }
        f.setSubscribed(b.booleanValue());
    }

    private IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final String folderName) throws MessagingException, MailException {
        String name = folderName;
        if (name == null) {
            name = toMove.getName();
        }
        return moveFolder(toMove, destFolder, name, true);
    }

    private IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final String folderName, final boolean checkForDuplicate) throws MessagingException, MailException {
        final String destFullname = destFolder.getFullName();
        StringBuilder sb = new StringBuilder();
        if (destFullname.length() > 0) {
            sb.append(destFullname).append(destFolder.getSeparator());
        }
        sb.append(folderName);
        final IMAPFolder newFolder = (IMAPFolder) imapStore.getFolder(sb.toString());
        sb = null;
        return moveFolder(toMove, destFolder, newFolder, checkForDuplicate);
    }

    private static final String[] ARGS_ALL = { "1:*" };

    private IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final IMAPFolder newFolder, final boolean checkForDuplicate) throws MessagingException, MailException {
        if (!inferiors(destFolder)) {
            throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, imapConfig, session, destFolder.getFullName());
        }
        final int toMoveType = toMove.getType();
        final String moveFullname = toMove.getFullName();
        if (imapConfig.isSupportsACLs() && ((toMoveType & Folder.HOLDS_MESSAGES) > 0)) {
            try {
                if (!getACLExtension().canRead(RightsCache.getCachedRights(toMove, true, session, accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_READ_ACCESS, imapConfig, session, moveFullname);
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, moveFullname);
            }
            try {
                if (!getACLExtension().canCreate(RightsCache.getCachedRights(toMove, true, session, accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, moveFullname);
                }
            } catch (final MessagingException e) {
                /*
                 * MYRIGHTS command failed for given mailbox
                 */
                if (!imapConfig.getImapCapabilities().hasNamespace() || !NamespaceFoldersCache.containedInPersonalNamespaces(
                    moveFullname,
                    imapStore,
                    true,
                    session,
                    accountId)) {
                    /*
                     * No namespace support or given parent is NOT covered by user's personal namespaces.
                     */
                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, moveFullname);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("MYRIGHTS command failed on namespace folder", e);
                }
            }
        }
        /*
         * Move by creating a new folder, copying all messages and deleting old folder
         */
        if (checkForDuplicate && newFolder.exists()) {
            throw IMAPException.create(IMAPException.Code.DUPLICATE_FOLDER, imapConfig, session, newFolder.getName());
        }
        /*
         * Create new folder. NOTE: It's not possible to create a folder only with type set to HOLDS_FOLDERS, cause created folder is
         * selectable anyway and therefore does not hold flag \NoSelect.
         */
        final String newFullname = newFolder.getFullName();
        if (!newFolder.create(toMoveType)) {
            throw IMAPException.create(
                IMAPException.Code.FOLDER_CREATION_FAILED,
                imapConfig,
                session,
                newFullname,
                destFolder instanceof DefaultFolder ? DEFAULT_FOLDER_ID : destFolder.getFullName());
        }
        /*
         * Apply original subscription status
         */
        newFolder.setSubscribed(toMove.isSubscribed());
        if (imapConfig.isSupportsACLs()) {
            /*
             * Copy ACLs
             */
            try {
                newFolder.open(Folder.READ_WRITE);
                try {
                    /*
                     * Copy ACLs
                     */
                    final ACL[] acls = toMove.getACL();
                    for (int i = 0; i < acls.length; i++) {
                        newFolder.addACL(acls[i]);
                    }
                } finally {
                    newFolder.close(false);
                }
            } catch (final ReadOnlyFolderException e) {
                throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, e, newFullname);
            }
        }
        if ((toMoveType & Folder.HOLDS_MESSAGES) > 0) {
            /*
             * Copy messages
             */
            if (!toMove.isOpen()) {
                toMove.open(Folder.READ_ONLY);
            }
            final long[] uids;
            try {
                uids = IMAPCommandsCollection.seqNums2UID(toMove, ARGS_ALL, toMove.getMessageCount());
            } finally {
                toMove.close(false);
            }
            imapAccess.getMessageStorage().copyMessagesLong(moveFullname, newFullname, uids, true);
        }
        /*
         * Iterate subfolders
         */
        final Folder[] subFolders = toMove.list();
        for (int i = 0; i < subFolders.length; i++) {
            moveFolder((IMAPFolder) subFolders[i], newFolder, subFolders[i].getName(), false);
        }
        /*
         * Delete old folder
         */
        IMAPCommandsCollection.forceSetSubscribed(imapStore, moveFullname, false);
        if (!toMove.delete(true) && LOG.isWarnEnabled()) {
            final IMAPException e = IMAPException.create(IMAPException.Code.DELETE_FAILED, moveFullname);
            LOG.warn(e.getMessage(), e);
        }
        /*
         * Notify message storage
         */
        imapAccess.getMessageStorage().notifyIMAPFolderModification(moveFullname);
        /*
         * Remove cache entries
         */
        RightsCache.removeCachedRights(toMove, session, accountId);
        UserFlagsCache.removeUserFlags(toMove, session, accountId);
        IMAPSessionStorageAccess.removeDeletedFolder(accountId, session, moveFullname);
        return newFolder;
    }

    private ACL[] permissions2ACL(final OCLPermission[] perms, final IMAPFolder imapFolder) throws AbstractOXException, MessagingException {
        final List<ACL> acls = new ArrayList<ACL>(perms.length);
        for (int i = 0; i < perms.length; i++) {
            final ACLPermission aclPermission = getACLPermission(perms[i]);
            try {
                acls.add(aclPermission.getPermissionACL(
                    IMAPFolderConverter.getEntity2AclArgs(session, imapFolder, imapConfig),
                    imapConfig,
                    ctx));
            } catch (final Entity2ACLException e) {
                if (Entity2ACLException.Code.UNKNOWN_USER.getNumber() == e.getDetailNumber()) {
                    // Obviously the user is not known, skip
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(new StringBuilder().append("User ").append(aclPermission.getEntity()).append(
                            " is not known on IMAP server \"").append(imapConfig.getImapServerAddress()).append('"').toString());
                    }
                } else {
                    throw e;
                }
            }
        }
        return acls.toArray(new ACL[acls.size()]);
    }

    private ACLPermission getACLPermission(final OCLPermission permission) {
        if (permission instanceof ACLPermission) {
            return (ACLPermission) permission;
        }
        final ACLPermission retval = new ACLPermission();
        retval.setEntity(permission.getEntity());
        retval.setDeleteObjectPermission(permission.getDeletePermission());
        retval.setFolderAdmin(permission.isFolderAdmin());
        retval.setFolderPermission(permission.getFolderPermission());
        retval.setGroupPermission(permission.isGroupPermission());
        retval.setName(permission.getName());
        retval.setReadObjectPermission(permission.getReadPermission());
        retval.setSystem(permission.getSystem());
        retval.setWriteObjectPermission(permission.getWritePermission());
        return retval;
    }

    private static ACL[] getRemovedACLs(final Map<String, ACL> newACLs, final ACL[] oldACLs) {
        final List<ACL> retval = new ArrayList<ACL>();
        for (final ACL oldACL : oldACLs) {
            final ACL newACL = newACLs.get(oldACL.getName());
            if (null == newACL) {
                retval.add(oldACL);
            }
        }
        return retval.toArray(new ACL[retval.size()]);
    }

    private static boolean isKnownEntity(final String entity, final Entity2ACL entity2ACL, final Context ctx, final Entity2ACLArgs args) {
        try {
            return entity2ACL.getEntityID(entity, ctx, args)[0] != -1;
        } catch (final AbstractOXException e) {
            return false;
        }
    }

    private boolean equals(final ACL[] oldACLs, final Map<String, ACL> newACLs, final Entity2ACL entity2ACL, final Entity2ACLArgs args) {
        int examined = 0;
        for (final ACL oldACL : oldACLs) {
            final String oldName = oldACL.getName();
            if (isKnownEntity(oldName, entity2ACL, ctx, args)) {
                final ACL newACL = newACLs.get(oldName/* .toLowerCase(Locale.ENGLISH) */);
                if (null == newACL) {
                    // No corresponding entity in new ACLs
                    return false;
                }
                // Remember number of corresponding entities
                examined++;
                // Check ACLS' rights ignoring POST right
                if (!equalRights(oldACL.getRights().toString(), newACL.getRights().toString(), true)) {
                    return false;
                }
            }
        }
        return (examined == newACLs.size());
    }

    private static String stripPOSTRight(final String rights) {
        final StringBuilder sb = new StringBuilder(rights.length());
        final char[] chars = rights.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            if ('p' != c && 'P' != c) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean equalRights(final String rights1, final String rights2, final boolean ignorePOST) {
        final char[] r1;
        final char[] r2;
        if (ignorePOST) {
            r1 = stripPOSTRight(rights1).toCharArray();
            r2 = stripPOSTRight(rights2).toCharArray();
        } else {
            r1 = rights1.toCharArray();
            r2 = rights2.toCharArray();
        }
        if (r1.length != r2.length) {
            return false;
        }
        Arrays.sort(r1);
        Arrays.sort(r2);
        return Arrays.equals(r1, r2);
    }

    private static Map<String, ACL> acl2map(final ACL[] acls) {
        final Map<String, ACL> m = new HashMap<String, ACL>(acls.length);
        for (final ACL acl : acls) {
            m.put(acl.getName()/* .toLowerCase(Locale.ENGLISH) */, acl);
        }
        return m;
    }

    private static ACL validate(final ACL newACL, final Map<String, ACL> oldACLs) {
        final ACL oldACL = oldACLs.get(newACL.getName());
        if (null == oldACL) {
            /*
             * Either no corresponding old ACL or old ACL's rights is not equal to "p"
             */
            return newACL;
        }
        final Rights newRights = newACL.getRights();
        final Rights oldRights = oldACL.getRights();
        /*
         * Handle the POST-to-NOT-MAPPABLE problem
         */
        if (oldRights.contains(Rights.Right.POST) && !newRights.contains(Rights.Right.POST)) {
            newRights.add(Rights.Right.POST);
        }
        /*
         * Handle the READ-KEEP_SEEN-to-READ problem
         */
        if (!oldRights.contains(Rights.Right.KEEP_SEEN) && newRights.contains(Rights.Right.KEEP_SEEN)) {
            newRights.remove(Rights.Right.KEEP_SEEN);
        }
        return newACL;
    }

    /*-
     * Determines if <i>altNamespace</i> is enabled for mailbox. If <i>altNamespace</i> is enabled all folder which are logically located
     * below INBOX folder are represented as INBOX's siblings in IMAP folder tree. Dependent on IMAP server's implementation the INBOX
     * folder is then marked with attribute <code>\NoInferiors</code> meaning it no longer allows subfolders.
     * 
     * @param imapStore - the IMAP store (mailbox)
     * @return <code>true</code> if altNamespace is enabled; otherwise <code>false</code>
     * @throws MessagingException - if IMAP's NAMESPACE command fails
    private static boolean isPersonalNamespaceEmpty(final IMAPStore imapStore) throws MessagingException {
        boolean altnamespace = false;
        final Folder[] pn = imapStore.getPersonalNamespaces();
        if ((pn.length != 0) && (pn[0].getFullName().trim().length() == 0)) {
            altnamespace = true;
        }
        return altnamespace;
    }*/

    /**
     * Checks id specified folder name is allowed to be used on folder creation. The folder name is valid if the separator character does
     * not appear or provided that MBox format is enabled may only appear at name's end.
     * 
     * @param name The folder name to check.
     * @param separator The separator character.
     * @param mboxEnabled <code>true</code> If MBox format is enabled; otherwise <code>false</code>
     * @return <code>true</code> if folder name is valid; otherwise <code>false</code>
     */
    private static boolean checkFolderNameValidity(final String name, final char separator, final boolean mboxEnabled) {
        final int pos = name.indexOf(separator);
        if (mboxEnabled) {
            /*
             * Allow trailing separator
             */
            return (pos == -1) || (pos == name.length() - 1);
        }
        return (pos == -1);
    }

    private static final String REGEX_TEMPL = "[\\S\\p{Blank}&&[^\\p{Cntrl}#SEP#]]+(?:\\Q#SEP#\\E[\\S\\p{Blank}&&[^\\p{Cntrl}#SEP#]]+)*";

    private static final Pattern PAT_SEP = Pattern.compile("#SEP#");

    private static boolean checkFolderPathValidity(final String path, final char separator) {
        if ((path != null) && (path.length() > 0)) {
            return Pattern.compile(PAT_SEP.matcher(REGEX_TEMPL).replaceAll(String.valueOf(separator))).matcher(path).matches();
        }
        return false;
    }

    /**
     * Checks if specified folder is selectable; meaning to check if it is capable to hold messages.
     * 
     * @param folder The folder to check
     * @return <code>true</code> if specified folder is selectable; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     */
    private static boolean isSelectable(final Folder folder) throws MessagingException {
        return (folder.getType() & Folder.HOLDS_MESSAGES) == Folder.HOLDS_MESSAGES;
    }

    /**
     * Checks if inferiors (subfolders) are allowed by specified folder; meaning to check if it is capable to hold folders.
     * 
     * @param folder The folder to check
     * @return <code>true</code> if inferiors (subfolders) are allowed by specified folder; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     */
    private static boolean inferiors(final Folder folder) throws MessagingException {
        return ((folder.getType() & Folder.HOLDS_FOLDERS) == Folder.HOLDS_FOLDERS);
    }

    private void removeSessionData(final Folder f) {
        try {
            final Folder[] fs = f.list();
            for (int i = 0; i < fs.length; i++) {
                removeSessionData(fs[i]);
            }
            IMAPSessionStorageAccess.removeDeletedFolder(accountId, session, f.getFullName());
        } catch (final MessagingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
