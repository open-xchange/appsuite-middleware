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
import javax.mail.MethodNotSupportedException;
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
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.NoSpamHandler;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;
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

    private final Session session;

    private final Context ctx;

    private final IMAPConfig imapConfig;

    private final ACLExtension aclExtension;

    private Character separator;

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
        this.session = session;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new IMAPException(e);
        }
        imapConfig = imapAccess.getIMAPConfig();
        aclExtension = ACLExtensionFactory.getInstance().getACLExtension(imapConfig);
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
            throw MIMEMailException.handleMessagingException(e, imapConfig);
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
                throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
            }
            return IMAPFolderConverter.convertFolder(f, session, imapConfig, ctx);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
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
                        mergeWithNamespaceFolders(
                            subfolders,
                            NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session),
                            subscribed,
                            parent);
                    }
                    {
                        mergeWithNamespaceFolders(
                            subfolders,
                            NamespaceFoldersCache.getUserNamespaces(imapStore, true, session),
                            subscribed,
                            parent);
                    }
                    {
                        mergeWithNamespaceFolders(
                            subfolders,
                            NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session),
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
                        if (!aclExtension.canLookUp(RightsCache.getCachedRights(parent, true, session))) {
                            throw new IMAPException(IMAPException.Code.NO_LOOKUP_ACCESS, parentFullname);
                        }
                    } catch (final MessagingException e) {
                        throw new IMAPException(IMAPException.Code.NO_ACCESS, e, parentFullname);
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
            throw MIMEMailException.handleMessagingException(e, imapConfig);
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
        if (NamespaceFoldersCache.containedInPersonalNamespaces(fullname, imapStore, true, session)) {
            return new NamespaceFolder(imapStore, fullname, getSeparator());
        }
        if (NamespaceFoldersCache.containedInUserNamespaces(fullname, imapStore, true, session)) {
            return new NamespaceFolder(imapStore, fullname, getSeparator());
        }
        if (NamespaceFoldersCache.containedInSharedNamespaces(fullname, imapStore, true, session)) {
            return new NamespaceFolder(imapStore, fullname, getSeparator());
        }
        return null;
    }

    @Override
    public MailFolder getRootFolder() throws MailException {
        try {
            return IMAPFolderConverter.convertFolder((IMAPFolder) imapStore.getDefaultFolder(), session, imapConfig, ctx);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    private boolean isDefaultFoldersChecked() {
        final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG);
        return (b != null) && b.booleanValue();
    }

    private void setDefaultFoldersChecked(final boolean checked) {
        session.setParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG, Boolean.valueOf(checked));
    }

    /**
     * Stores specified separator character in session parameters for future look-ups
     * 
     * @param separator The separator character
     */
    private void setSeparator(final char separator) {
        session.setParameter(MailSessionParameterNames.PARAM_SEPARATOR, Character.valueOf(separator));
    }

    private void setDefaultMailFolder(final int index, final String fullname) {
        String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
        if (null == arr) {
            arr = new String[6];
            session.setParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR, arr);
        }
        arr[index] = fullname;
    }

    @Override
    public void checkDefaultFolders() throws MailException {
        if (!isDefaultFoldersChecked()) {
            synchronized (session) {
                try {
                    if (isDefaultFoldersChecked()) {
                        return;
                    }
                    /*
                     * Get INBOX folder
                     */
                    final Folder inboxFolder = imapStore.getFolder(STR_INBOX);
                    if (!inboxFolder.exists()) {
                        throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, STR_INBOX);
                    }
                    if (!inboxFolder.isSubscribed()) {
                        /*
                         * Subscribe INBOX folder
                         */
                        inboxFolder.setSubscribed(true);
                    }
                    final StringBuilder tmp = new StringBuilder(128);
                    /*
                     * Check for NAMESPACE capability
                     */
                    if (imapConfig.getImapCapabilities().hasNamespace()) {
                        /*
                         * Perform the NAMESPACE command to detect the subfolder prefix. From rfc2342: Clients often attempt to create
                         * mailboxes for such purposes as maintaining a record of sent messages (e.g. "Sent Mail") or temporarily saving
                         * messages being composed (e.g. "Drafts"). For these clients to inter-operate correctly with the variety of IMAP4
                         * servers available, the user must enter the prefix of the Personal Namespace used by the server. Using the
                         * NAMESPACE command, a client is able to automatically discover this prefix without manual user configuration.
                         */
                        final Folder[] personalNamespaces = imapStore.getPersonalNamespaces();
                        if (personalNamespaces == null || personalNamespaces[0] == null) {
                            throw new IMAPException(IMAPException.Code.MISSING_PERSONAL_NAMESPACE);
                        }
                        setSeparator(personalNamespaces[0].getSeparator());
                        final String persPrefix = personalNamespaces[0].getFullName();
                        if ((persPrefix.length() == 0)) {
                            if (MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace() && IMAPCommandsCollection.canCreateSubfolder(
                                persPrefix,
                                (IMAPFolder) inboxFolder)) {
                                /*
                                 * Personal namespace folder allows subfolders and nested default folder are demanded, thus use INBOX as
                                 * prefix although NAMESPACE signals to use no prefix.
                                 */
                                tmp.append(inboxFolder.getFullName()).append(inboxFolder.getSeparator());
                            }
                        } else {
                            tmp.append(persPrefix).append(personalNamespaces[0].getSeparator());
                        }
                    } else {
                        /*
                         * Examine INBOX folder since NAMESPACE capability is not supported
                         */
                        setSeparator(inboxFolder.getSeparator());
                        final boolean noInferiors = !inferiors(inboxFolder);
                        /*
                         * Determine where to create default folders and store as a prefix for folder fullname
                         */
                        if (!noInferiors && (MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace())) {
                            /*
                             * Only allow default folder below INBOX if inferiors are permitted nested default folder are explicitly allowed
                             */
                            tmp.append(inboxFolder.getFullName()).append(inboxFolder.getSeparator());
                        }
                    }
                    final String prefix = tmp.toString();
                    tmp.setLength(0);
                    final int type;
                    if (IMAPConfig.isMBoxEnabled()) {
                        type = Folder.HOLDS_MESSAGES;
                    } else {
                        type = FOLDER_TYPE;
                    }
                    /*
                     * Check default folders
                     */
                    final String[] defaultFolderNames;
                    final SpamHandler spamHandler;
                    {
                        final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
                        defaultFolderNames = StorageUtility.getDefaultFolderNames(usm, usm.isSpamOptionEnabled());
                        spamHandler = usm.isSpamOptionEnabled() ? SpamHandlerRegistry.getSpamHandlerBySession(session) : NoSpamHandler.getInstance();
                    }
                    for (int i = 0; i < defaultFolderNames.length; i++) {
                        if ((i != StorageUtility.INDEX_CONFIRMED_HAM) && (i != StorageUtility.INDEX_CONFIRMED_SPAM)) {
                            setDefaultMailFolder(i, checkDefaultFolder(prefix, defaultFolderNames[i], type, 1, tmp));
                        } else {
                            if (i == StorageUtility.INDEX_CONFIRMED_SPAM) {
                                if (spamHandler.isCreateConfirmedSpam()) {
                                    setDefaultMailFolder(i, checkDefaultFolder(
                                        prefix,
                                        defaultFolderNames[i],
                                        type,
                                        spamHandler.isUnsubscribeSpamFolders() ? 0 : -1,
                                        tmp));
                                } else if (LOG.isDebugEnabled()) {
                                    LOG.debug("Skipping check for " + defaultFolderNames[i] + " due to SpamHandler.isCreateConfirmedSpam()=false");
                                }
                            } else if (i == StorageUtility.INDEX_CONFIRMED_HAM) {
                                if (spamHandler.isCreateConfirmedHam()) {
                                    setDefaultMailFolder(i, checkDefaultFolder(
                                        prefix,
                                        defaultFolderNames[i],
                                        type,
                                        spamHandler.isUnsubscribeSpamFolders() ? 0 : -1,
                                        tmp));
                                } else if (LOG.isDebugEnabled()) {
                                    LOG.debug("Skipping check for " + defaultFolderNames[i] + " due to SpamHandler.isCreateConfirmedHam()=false");
                                }
                            }
                        }
                    }
                    setDefaultFoldersChecked(true);
                } catch (final MessagingException e) {
                    throw MIMEMailException.handleMessagingException(e, imapConfig);
                }
            }
        }
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
                    throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character.valueOf(toCreate.getSeparator()));
                }
                parent = (IMAPFolder) imapStore.getFolder(parentFullname);
                isParentDefault = false;
            }
            if (!parent.exists()) {
                parent = checkForNamespaceFolder(parentFullname);
                if (null == parent) {
                    throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, parentFullname);
                }
            }
            /*
             * Check if parent holds folders
             */
            if (!inferiors(parent)) {
                throw new IMAPException(
                    IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS,
                    parent instanceof DefaultFolder ? DEFAULT_FOLDER_ID : parentFullname);
            }
            /*
             * Check ACLs if enabled
             */
            if (imapConfig.isSupportsACLs()) {
                try {
                    if (!aclExtension.canCreate(RightsCache.getCachedRights(parent, true, session))) {
                        throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, parentFullname);
                    }
                } catch (final MessagingException e) {
                    /*
                     * MYRIGHTS command failed for given mailbox
                     */
                    if (!imapConfig.getImapCapabilities().hasNamespace() || !NamespaceFoldersCache.containedInPersonalNamespaces(
                        parent.getFullName(),
                        imapStore,
                        true,
                        session)) {
                        /*
                         * No namespace support or given parent is NOT covered by user's personal namespaces.
                         */
                        throw new IMAPException(IMAPException.Code.NO_ACCESS, e, parentFullname);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("MYRIGHTS command failed on namespace folder", e);
                    }
                }
            }
            if (!checkFolderNameValidity(name, parent.getSeparator())) {
                throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character.valueOf(parent.getSeparator()));
            }
            if (isParentDefault) {
                /*
                 * Below default folder
                 */
                createMe = (IMAPFolder) imapStore.getFolder(name);
            } else {
                createMe = (IMAPFolder) imapStore.getFolder(new StringBuilder(parent.getFullName()).append(parent.getSeparator()).append(
                    name).toString());
            }
            if (createMe.exists()) {
                throw new IMAPException(IMAPException.Code.DUPLICATE_FOLDER, createMe.getFullName());
            }
            final int ftype;
            if (IMAPConfig.isMBoxEnabled()) {
                /*
                 * Determine folder creation type dependent on folder name
                 */
                ftype = createMe.getName().endsWith(String.valueOf(parent.getSeparator())) ? Folder.HOLDS_FOLDERS : Folder.HOLDS_MESSAGES;
            } else {
                ftype = FOLDER_TYPE;
            }
            try {
                if (!(created = createMe.create(ftype))) {
                    throw new IMAPException(
                        IMAPException.Code.FOLDER_CREATION_FAILED,
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
                        throw new IMAPException(
                            IMAPException.Code.FOLDER_CREATION_FAILED,
                            e,
                            createMe.getFullName(),
                            parent instanceof DefaultFolder ? DEFAULT_FOLDER_ID : parent.getFullName());
                    }
                    if (LOG.isInfoEnabled()) {
                        LOG.info("IMAP folder created with fallback type HOLDS_MESSAGES");
                    }
                } else {
                    throw MIMEMailException.handleMessagingException(e, imapConfig);
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
                        if (!aclExtension.canSetACL(createMe.myRights())) {
                            throw new IMAPException(IMAPException.Code.NO_ADMINISTER_ACCESS_ON_INITIAL, createMe.getFullName());
                        }
                        boolean adminFound = false;
                        for (int i = 0; (i < newACLs.length) && !adminFound; i++) {
                            if (aclExtension.canSetACL(newACLs[i].getRights())) {
                                adminFound = true;
                            }
                        }
                        if (!adminFound) {
                            throw new IMAPException(IMAPException.Code.NO_ADMIN_ACL, createMe.getFullName());
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
            if (created) {
                try {
                    if (createMe.exists()) {
                        createMe.delete(true);
                    }
                } catch (final Throwable e2) {
                    LOG.error(new StringBuilder().append("Temporary created IMAP folder \"").append(createMe.getFullName()).append(
                        "could not be deleted"), e2);
                }
            }
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        } catch (final MailException e) {
            if (created) {
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
            if (created) {
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
            if (created) {
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
            throw new IMAPException(IMAPException.Code.NO_ROOT_MOVE);
        }
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                throw new MailException(MailException.Code.NO_ROOT_FOLDER_MODIFY_DELETE);
            }
            IMAPFolder moveMe = (IMAPFolder) imapStore.getFolder(fullname);
            if (!moveMe.exists()) {
                moveMe = checkForNamespaceFolder(fullname);
                if (null == moveMe) {
                    throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
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
                        throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character.valueOf(separator));
                    }
                    newParent = newFullname.substring(0, pos);
                    if (!checkFolderPathValidity(newParent, separator)) {
                        throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character.valueOf(separator));
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
                if (isStandardFolder(moveMe.getFullName())) {
                    throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, moveMe.getFullName());
                }
                IMAPFolder destFolder = ((IMAPFolder) (MailFolder.DEFAULT_FOLDER_ID.equals(newParent) ? imapStore.getDefaultFolder() : imapStore.getFolder(newParent)));
                if (!destFolder.exists()) {
                    destFolder = checkForNamespaceFolder(newParent);
                    if (null == destFolder) {
                        /*
                         * Destination folder could not be found, thus an invalid name was specified by user
                         */
                        throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, newParent);
                    }
                }
                if (destFolder instanceof DefaultFolder) {
                    if (!inferiors(destFolder)) {
                        throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, destFolder.getFullName());
                    }
                } else if (imapConfig.isSupportsACLs() && isSelectable(destFolder)) {
                    try {
                        if (!aclExtension.canCreate(RightsCache.getCachedRights(destFolder, true, session))) {
                            throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, newParent);
                        }
                    } catch (final MessagingException e) {
                        /*
                         * MYRIGHTS command failed for given mailbox
                         */
                        if (!imapConfig.getImapCapabilities().hasNamespace() || !NamespaceFoldersCache.containedInPersonalNamespaces(
                            newParent,
                            imapStore,
                            true,
                            session)) {
                            /*
                             * No namespace support or given parent is NOT covered by user's personal namespaces.
                             */
                            throw new IMAPException(IMAPException.Code.NO_ACCESS, e, newParent);
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("MYRIGHTS command failed on namespace folder", e);
                        }
                    }
                }
                if (!checkFolderNameValidity(newName, separator)) {
                    throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character.valueOf(separator));
                }
                if (destFolder.getFullName().startsWith(moveMe.getFullName())) {
                    throw new IMAPException(IMAPException.Code.NO_MOVE_TO_SUBFLD, moveMe.getName(), destFolder.getName());
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
                if (isStandardFolder(moveMe.getFullName())) {
                    throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, moveMe.getFullName());
                } else if (imapConfig.isSupportsACLs() && isSelectable(moveMe)) {
                    try {
                        if (!aclExtension.canCreate(RightsCache.getCachedRights(moveMe, true, session))) {
                            throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, moveMe.getFullName());
                        }
                    } catch (final MessagingException e) {
                        throw new IMAPException(IMAPException.Code.NO_ACCESS, e, moveMe.getFullName());
                    }
                }
                if (!checkFolderNameValidity(newName, separator)) {
                    throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character.valueOf(separator));
                }
                /*
                 * Rename can only be invoked on a closed folder
                 */
                if (moveMe.isOpen()) {
                    moveMe.close(false);
                }
                final IMAPFolder renameFolder;
                {
                    final String parentFullName = moveMe.getParent().getFullName();
                    final StringBuilder tmp = new StringBuilder();
                    if (parentFullName.length() > 0) {
                        tmp.append(parentFullName).append(separator);
                    }
                    tmp.append(newName);
                    renameFolder = (IMAPFolder) imapStore.getFolder(tmp.toString());
                }
                if (renameFolder.exists()) {
                    throw new IMAPException(IMAPException.Code.DUPLICATE_FOLDER, renameFolder.getFullName());
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
                    throw new IMAPException(IMAPException.Code.UPDATE_FAILED, moveMe.getFullName());
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
            throw MIMEMailException.handleMessagingException(e, imapConfig);
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
                    throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
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
                        if (isStandardFolder(updateMe.getFullName()) && !stillHoldsFullRights(updateMe, newACLs, imapConfig, session, ctx)) {
                            throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, updateMe.getFullName());
                        }
                        if (!aclExtension.canSetACL(RightsCache.getCachedRights(updateMe, true, session))) {
                            throw new IMAPException(IMAPException.Code.NO_ADMINISTER_ACCESS, updateMe.getFullName());
                        }
                        /*
                         * Check new ACLs
                         */
                        if (newACLs.length == 0) {
                            throw new IMAPException(IMAPException.Code.NO_ADMIN_ACL, updateMe.getFullName());
                        }
                        {
                            boolean adminFound = false;
                            for (int i = 0; (i < newACLs.length) && !adminFound; i++) {
                                if (aclExtension.canSetACL(newACLs[i].getRights())) {
                                    adminFound = true;
                                }
                            }
                            if (!adminFound) {
                                throw new IMAPException(IMAPException.Code.NO_ADMIN_ACL, updateMe.getFullName());
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
                        RightsCache.removeCachedRights(updateMe, session);
                    }
                }
            }
            if (!MailProperties.getInstance().isIgnoreSubscription() && toUpdate.containsSubscribed()) {
                updateMe.setSubscribed(toUpdate.isSubscribed());
                IMAPCommandsCollection.forceSetSubscribed(imapStore, updateMe.getFullName(), toUpdate.isSubscribed());
            }
            return updateMe.getFullName();
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
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
                    throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
                }
            }
            imapAccess.getMessageStorage().notifyIMAPFolderModification(fullname);
            if (hardDelete) {
                /*
                 * Delete permanently
                 */
                deleteFolder(deleteMe);
                return fullname;
            }
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
                IMAPFolder newFolder = (IMAPFolder) imapStore.getFolder(sb.append(trashFolder.getFullName()).append(deleteMe.getSeparator()).append(
                    name).toString());
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
            return fullname;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
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
                    throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
                }
            }
            imapAccess.getMessageStorage().notifyIMAPFolderModification(fullname);
            try {
                if (!isSelectable(f)) {
                    throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, f.getFullName());
                }
                if (imapConfig.isSupportsACLs()) {
                    final Rights myrights = RightsCache.getCachedRights(f, true, session);
                    if (!aclExtension.canRead(myrights)) {
                        throw new IMAPException(IMAPException.Code.NO_READ_ACCESS, f.getFullName());
                    }
                    if (!aclExtension.canDeleteMessages(myrights)) {
                        throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, f.getFullName());
                    }
                }
            } catch (final MessagingException e) {
                throw new IMAPException(IMAPException.Code.NO_ACCESS, e, f.getFullName());
            }
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
                final int blockSize = IMAPConfig.getBlockSize();
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
                                throw new IMAPException(IMAPException.Code.MOVE_ON_DELETE_FAILED, e, new Object[0]);
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
                            throw new IMAPException(IMAPException.Code.CONNECT_ERROR, e, imapConfig.getServer(), imapConfig.getLogin());
                        } catch (final StoreClosedException e) {
                            /*
                             * Not possible to retry since connection is broken
                             */
                            if (LOG.isDebugEnabled()) {
                                debug.setLength(0);
                                LOG.debug(debug.append("EXPUNGE command timed out in ").append((System.currentTimeMillis() - startExpunge)).append(
                                    STR_MSEC).toString());
                            }
                            throw new IMAPException(IMAPException.Code.CONNECT_ERROR, e, imapConfig.getServer(), imapConfig.getLogin());
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
                        throw new IMAPException(IMAPException.Code.MOVE_ON_DELETE_FAILED, e, new Object[0]);
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
            throw MIMEMailException.handleMessagingException(e, imapConfig);
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
                    throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
                }
            }
            if (imapConfig.isSupportsACLs() && isSelectable(f)) {
                try {
                    if (!aclExtension.canLookUp(RightsCache.getCachedRights(f, true, session))) {
                        throw new IMAPException(IMAPException.Code.NO_LOOKUP_ACCESS, fullname);
                    }
                } catch (final MessagingException e) {
                    throw new IMAPException(IMAPException.Code.NO_ACCESS, e, fullname);
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
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public String getConfirmedHamFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_CONFIRMED_HAM);
    }

    @Override
    public String getConfirmedSpamFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_CONFIRMED_SPAM);
    }

    @Override
    public String getDraftsFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_DRAFTS);
    }

    @Override
    public String getSentFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_SENT);
    }

    @Override
    public String getSpamFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_SPAM);
    }

    @Override
    public String getTrashFolder() throws MailException {
        return getStandardFolder(StorageUtility.INDEX_TRASH);
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
                    throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
                }
                try {
                    if (!isSelectable(f)) {
                        throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
                    }
                    if (imapConfig.isSupportsACLs()) {
                        final Rights myrights = RightsCache.getCachedRights(f, true, session);
                        if (!aclExtension.canRead(myrights)) {
                            throw new IMAPException(IMAPException.Code.NO_READ_ACCESS, fullname);
                        }
                        if (!aclExtension.canDeleteMailbox(myrights)) {
                            throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, fullname);
                        }
                    }
                } catch (final MessagingException e) {
                    throw new IMAPException(IMAPException.Code.NO_ACCESS, e, fullname);
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
            throw MIMEMailException.handleMessagingException(e, imapConfig);
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
        if (isStandardFolder(deleteMe.getFullName())) {
            throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_DELETE, deleteMe.getFullName());
        } else if (!deleteMe.exists()) {
            throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, deleteMe.getFullName());
        }
        try {
            if (imapConfig.isSupportsACLs() && isSelectable(deleteMe) && !aclExtension.canDeleteMailbox(RightsCache.getCachedRights(
                deleteMe,
                true,
                session))) {
                throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, deleteMe.getFullName());
            }
        } catch (final MessagingException e) {
            throw new IMAPException(IMAPException.Code.NO_ACCESS, e, deleteMe.getFullName());
        }
        if (deleteMe.isOpen()) {
            deleteMe.close(false);
        }
        /*
         * Unsubscribe prior to deletion
         */
        IMAPCommandsCollection.forceSetSubscribed(imapStore, deleteMe.getFullName(), false);
        final long start = System.currentTimeMillis();
        if (!deleteMe.delete(true)) {
            throw new IMAPException(IMAPException.Code.DELETE_FAILED, deleteMe.getFullName());
        }
        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        /*
         * Remove cache entries
         */
        RightsCache.removeCachedRights(deleteMe, session);
        UserFlagsCache.removeUserFlags(deleteMe, session);
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
            throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, destFolder.getFullName());
        }
        final int toMoveType = toMove.getType();
        final String moveFullname = toMove.getFullName();
        if (imapConfig.isSupportsACLs() && ((toMoveType & Folder.HOLDS_MESSAGES) > 0)) {
            try {
                if (!aclExtension.canRead(RightsCache.getCachedRights(toMove, true, session))) {
                    throw new IMAPException(IMAPException.Code.NO_READ_ACCESS, moveFullname);
                }
            } catch (final MessagingException e) {
                throw new IMAPException(IMAPException.Code.NO_ACCESS, e, moveFullname);
            }
            try {
                if (!aclExtension.canCreate(RightsCache.getCachedRights(toMove, true, session))) {
                    throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, moveFullname);
                }
            } catch (final MessagingException e) {
                /*
                 * MYRIGHTS command failed for given mailbox
                 */
                if (!imapConfig.getImapCapabilities().hasNamespace() || !NamespaceFoldersCache.containedInPersonalNamespaces(
                    moveFullname,
                    imapStore,
                    true,
                    session)) {
                    /*
                     * No namespace support or given parent is NOT covered by user's personal namespaces.
                     */
                    throw new IMAPException(IMAPException.Code.NO_ACCESS, e, moveFullname);
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
            throw new IMAPException(IMAPException.Code.DUPLICATE_FOLDER, newFolder.getName());
        }
        /*
         * Create new folder. NOTE: It's not possible to create a folder only with type set to HOLDS_FOLDERS, cause created folder is
         * selectable anyway and therefore does not hold flag \NoSelect.
         */
        final String newFullname = newFolder.getFullName();
        if (!newFolder.create(toMoveType)) {
            throw new IMAPException(
                IMAPException.Code.FOLDER_CREATION_FAILED,
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
                throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, e, newFullname);
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
            imapAccess.getMessageStorage().copyMessages(moveFullname, newFullname, uids, true);
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
            final IMAPException e = new IMAPException(IMAPException.Code.DELETE_FAILED, moveFullname);
            LOG.warn(e.getMessage(), e);
        }
        /*
         * Notify message storage
         */
        imapAccess.getMessageStorage().notifyIMAPFolderModification(moveFullname);
        /*
         * Remove cache entries
         */
        RightsCache.removeCachedRights(toMove, session);
        UserFlagsCache.removeUserFlags(toMove, session);
        return newFolder;
    }

    private boolean isStandardFolder(final String folderFullName) throws MailException {
        boolean isDefaultFolder = false;
        isDefaultFolder = (folderFullName.equalsIgnoreCase(STR_INBOX));
        for (int index = 0; (index < 6) && !isDefaultFolder; index++) {
            if (folderFullName.equalsIgnoreCase(getStandardFolder(index))) {
                return true;
            }
        }
        return isDefaultFolder;
    }

    private String getStandardFolder(final int index) throws MailException {
        if (!isDefaultFoldersChecked()) {
            checkDefaultFolders();
        }
        if (StorageUtility.INDEX_INBOX == index) {
            return STR_INBOX;
        }
        final String retval = getDefaultMailFolder(index);
        if (retval != null) {
            return retval;
        }
        setDefaultFoldersChecked(false);
        checkDefaultFolders();
        return getDefaultMailFolder(index);
    }

    private String getDefaultMailFolder(final int index) {
        final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
        return arr == null ? null : arr[index];
    }

    private ACL[] permissions2ACL(final OCLPermission[] perms, final IMAPFolder imapFolder) throws AbstractOXException, MessagingException {
        final ACL[] acls = new ACL[perms.length];
        for (int i = 0; i < perms.length; i++) {
            acls[i] = ((ACLPermission) perms[i]).getPermissionACL(
                IMAPFolderConverter.getEntity2AclArgs(session, imapFolder, imapConfig),
                imapConfig,
                ctx);
        }
        return acls;
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

    private static final Pattern PAT_RIGHT_POST = Pattern.compile("p|P");

    private boolean equals(final ACL[] oldACLs, final Map<String, ACL> newACLs, final Entity2ACL entity2ACL, final Entity2ACLArgs args) {
        int examined = 0;
        for (final ACL oldACL : oldACLs) {
            final String aclName = oldACL.getName();
            if (isKnownEntity(aclName, entity2ACL, ctx, args)) {
                final ACL newACL = newACLs.get(aclName);
                if (null == newACL) {
                    // No corresponding entity in new ACLs
                    return false;
                }
                // Remember number of corresponding entities
                examined++;
                // Check ACLS' rights ignoring POST right
                if (!PAT_RIGHT_POST.matcher(oldACL.getRights().toString()).replaceFirst("").equals(
                    PAT_RIGHT_POST.matcher(newACL.getRights().toString()).replaceFirst(""))) {
                    return false;
                }
            }
        }
        return (examined == newACLs.size());
    }

    private static Map<String, ACL> acl2map(final ACL[] acls) {
        final Map<String, ACL> m = new HashMap<String, ACL>(acls.length);
        for (final ACL acl : acls) {
            m.put(acl.getName(), acl);
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

    private String checkDefaultFolder(final String prefix, final String name, final int type, final int subscribe, final StringBuilder tmp) throws MessagingException {
        /*
         * Check default folder
         */
        boolean checkSubscribed = true;
        final Folder f = imapStore.getFolder(tmp.append(prefix).append(name).toString());
        tmp.setLength(0);
        if (!f.exists() && !f.create(type)) {
            final IMAPException oxme = new IMAPException(
                IMAPException.Code.NO_DEFAULT_FOLDER_CREATION,
                tmp.append(prefix).append(name).toString());
            tmp.setLength(0);
            LOG.error(oxme.getMessage(), oxme);
            checkSubscribed = false;
        }
        if (checkSubscribed) {
            if (1 == subscribe && !f.isSubscribed()) {
                try {
                    f.setSubscribed(true);
                } catch (final MethodNotSupportedException e) {
                    LOG.error(e.getMessage(), e);
                } catch (final MessagingException e) {
                    LOG.error(e.getMessage(), e);
                }
            } else if (0 == subscribe && f.isSubscribed()) {
                try {
                    f.setSubscribed(false);
                } catch (final MethodNotSupportedException e) {
                    LOG.error(e.getMessage(), e);
                } catch (final MessagingException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(tmp.append("Default folder \"").append(f.getFullName()).append("\" successfully checked").toString());
            tmp.setLength(0);
        }
        return f.getFullName();
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
     * @return <code>true</code> if folder name is valid; otherwise <code>false</code>
     */
    private static boolean checkFolderNameValidity(final String name, final char separator) {
        final int pos = name.indexOf(separator);
        if (IMAPConfig.isMBoxEnabled()) {
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
}
