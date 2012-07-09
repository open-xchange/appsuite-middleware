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

package com.openexchange.imap;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.MBoxEnabledCache;
import com.openexchange.imap.cache.NamespaceFoldersCache;
import com.openexchange.imap.cache.RootSubfolderCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.NoSpamHandler;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.tools.UnsynchronizedStringWriter;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link IMAPDefaultFolderChecker} - The IMAP default folder checker.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IMAPDefaultFolderChecker {

    static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(IMAPDefaultFolderChecker.class));

    static final boolean DEBUG = LOG.isDebugEnabled();

    private static final String INBOX = "INBOX";

    private static final int FOLDER_TYPE = (IMAPFolder.HOLDS_MESSAGES | IMAPFolder.HOLDS_FOLDERS);

    protected final Session session;

    protected final int accountId;

    protected final AccessedIMAPStore imapStore;

    protected final Context ctx;

    protected final IMAPConfig imapConfig;

    protected boolean retry;

    /**
     * Initializes a new {@link IMAPDefaultFolderChecker}.
     *
     * @param accountId The account ID
     * @param session The session
     * @param ctx The context
     * @param imapStore The (connected) IMAP store
     * @param imapConfig The IMAP configuration
     */
    public IMAPDefaultFolderChecker(final int accountId, final Session session, final Context ctx, final AccessedIMAPStore imapStore, final IMAPConfig imapConfig) {
        super();
        retry = true;
        this.accountId = accountId;
        this.session = session;
        this.imapStore = imapStore;
        this.ctx = ctx;
        this.imapConfig = imapConfig;
    }

    /**
     * Sets the retry behavior.
     *
     * @param retry <code>true</code> to retry
     */
    public void setRetry(final boolean retry) {
        this.retry = retry;
    }

    /**
     * Checks if given full name denotes a default folder.
     *
     * @param folderFullName The full name to check
     * @return <code>true</code> if given full name denotes a default folder; otherwise <code>false</code>
     * @throws OXException If check for default folder fails
     */
    public boolean isDefaultFolder(final String folderFullName) throws OXException {
        boolean isDefaultFolder = false;
        isDefaultFolder = (folderFullName.equalsIgnoreCase(INBOX));
        for (int index = 0; (index < 6) && !isDefaultFolder; index++) {
            if (folderFullName.equalsIgnoreCase(getDefaultFolder(index))) {
                return true;
            }
        }
        return isDefaultFolder;
    }

    /**
     * Gets the default folder for specified index.
     *
     * @param index The default folder index taken from class <code>StorageUtility</code>
     * @return The default folder for specified index
     * @throws OXException If default folder retrieval fails
     */
    public String getDefaultFolder(final int index) throws OXException {
        final MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        final String key = MailSessionParameterNames.getParamDefaultFolderChecked();
        if (!isDefaultFoldersChecked(key, mailSessionCache)) {
            checkDefaultFolders();
        }
        if (StorageUtility.INDEX_INBOX == index) {
            return INBOX;
        }
        final String retval = getDefaultMailFolder(index, mailSessionCache);
        if (retval != null) {
            return retval;
        }
        setDefaultFoldersChecked(key, false, mailSessionCache);
        checkDefaultFolders(key, mailSessionCache);
        return getDefaultMailFolder(index, mailSessionCache);
    }

    private String getDefaultMailFolder(final int index, final MailSessionCache mailSessionCache) {
        final String[] arr = mailSessionCache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
        return arr == null ? null : arr[index];
    }

    /**
     * Checks default folders.
     *
     * @throws OXException If default folder check fails
     */
    public void checkDefaultFolders() throws OXException {
        checkDefaultFolders(MailSessionParameterNames.getParamDefaultFolderChecked(), MailSessionCache.getInstance(session));
    }

    /**
     * Performs specified {@link Callable} instance in a synchronized manner.
     */
    protected static <V> V performSynchronized(final Callable<V> task, final Session session) throws Exception {
        final Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null == lock) {
            synchronized (session) {
                return task.call();
            }
        }
        // Use Lock instance
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check presence of default folders.
     * 
     * @param key The key for cache look-up
     * @param mailSessionCache The cache
     * @throws MailException If checking default folders' presence fails for any reason
     */
    protected void checkDefaultFolders(final String key, final MailSessionCache mailSessionCache) throws OXException {
        if (!isDefaultFoldersChecked(key, mailSessionCache)) {
            final Callable<Void> task = new Callable<Void>() {

                @Override
                public Void call() throws OXException {
                    if (isDefaultFoldersChecked(key, mailSessionCache)) {
                        return null;
                    }
                    try {
                        if (DEBUG) {
                            final StringBuilder sb = new StringBuilder(2048);
                            sb.append("\n\nDefault folder check for account ").append(accountId).append(" (");
                            sb.append(imapConfig.getServer()).append(")\n");
                            new Throwable().printStackTrace(new java.io.PrintWriter(new UnsynchronizedStringWriter(sb)));
                            sb.append('\n');
                            LOG.debug(sb.toString());
                        }
                        /*
                         * Get INBOX folder
                         */
                        ListLsubEntry inboxListEntry;
                        final IMAPFolder inboxFolder;
                        {
                            final IMAPFolder tmp = (IMAPFolder) imapStore.getFolder(INBOX);
                            ListLsubEntry entry = ListLsubCache.getCachedLISTEntry(INBOX, accountId, tmp, session);
                            if (entry.exists()) {
                                inboxFolder = tmp;
                            } else {
                                /*
                                 * Strange... No INBOX available. Try to create it.
                                 */
                                final char sep = IMAPCommandsCollection.getSeparator(tmp);
                                try {
                                    IMAPCommandsCollection.createFolder(tmp, sep, FOLDER_TYPE);
                                } catch (final MessagingException e) {
                                    IMAPCommandsCollection.createFolder(tmp, sep, Folder.HOLDS_MESSAGES);
                                }
                                ListLsubCache.addSingle(INBOX, accountId, tmp, session);
                                inboxFolder = (IMAPFolder) imapStore.getFolder(INBOX);
                                entry = ListLsubCache.getCachedLISTEntry(INBOX, accountId, inboxFolder, session);
                            }
                            inboxListEntry = entry;
                        }
                        if (!inboxListEntry.isSubscribed()) {
                            /*
                             * Subscribe INBOX folder
                             */
                            inboxFolder.setSubscribed(true);
                            ListLsubCache.addSingle(INBOX, accountId, inboxFolder, session);
                            inboxListEntry = ListLsubCache.getCachedLISTEntry(INBOX, accountId, inboxFolder, session);
                        }
                        final char sep = inboxFolder.getSeparator();
                        /*
                         * Get prefix for default folder names, NOT full names!
                         */
                        String prefix = imapStore.getImapAccess().getFolderStorage().getDefaultFolderPrefix();
                        /*
                         * Check for mbox
                         */
                        final int type;
                        final boolean mboxEnabled = MBoxEnabledCache.isMBoxEnabled(imapConfig, inboxFolder, prefix);
                        if (mboxEnabled) {
                            type = Folder.HOLDS_MESSAGES;
                        } else {
                            type = FOLDER_TYPE;
                        }
                        /*
                         * Get connection
                         */
                        {
                            /*
                             * Get storage service
                             */
                            final MailAccountStorageService storageService =
                                IMAPServiceRegistry.getService(MailAccountStorageService.class, true);
                            boolean keepgoing = true;
                            while (keepgoing) {
                                keepgoing = false;
                                try {
                                    sequentiallyCheckFolders(prefix, sep, type, storageService, mailSessionCache);
                                } catch (final RetryOtherPrefixException e) {
                                    prefix = e.getPrefix();
                                    final MailAccount mailAccount = getMailAccount(storageService);
                                    final MailAccountDescription mad = new MailAccountDescription();
                                    final Set<Attribute> attributes = EnumSet.noneOf(Attribute.class);
                                    mad.setId(accountId);
                                    {
                                        String name = mailAccount.getConfirmedHam();
                                        if (null == name) {
                                            final String fn = mailAccount.getConfirmedHamFullname();
                                            final int pos = fn.lastIndexOf(sep);
                                            name = pos < 0 ? fn : fn.substring(pos + 1);
                                        }
                                        mad.setConfirmedHamFullname(prefix + name);
                                        attributes.add(Attribute.CONFIRMED_HAM_FULLNAME_LITERAL);
                                    }
                                    {
                                        String name = mailAccount.getConfirmedSpam();
                                        if (null == name) {
                                            final String fn = mailAccount.getConfirmedSpamFullname();
                                            final int pos = fn.lastIndexOf(sep);
                                            name = pos < 0 ? fn : fn.substring(pos + 1);
                                        }
                                        mad.setConfirmedSpamFullname(prefix + name);
                                        attributes.add(Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL);
                                    }
                                    {
                                        String name = mailAccount.getDrafts();
                                        if (null == name) {
                                            final String fn = mailAccount.getDraftsFullname();
                                            final int pos = fn.lastIndexOf(sep);
                                            name = pos < 0 ? fn : fn.substring(pos + 1);
                                        }
                                        mad.setDraftsFullname(prefix + name);
                                        attributes.add(Attribute.DRAFTS_FULLNAME_LITERAL);
                                    }
                                    {
                                        String name = mailAccount.getSpam();
                                        if (null == name) {
                                            final String fn = mailAccount.getSpamFullname();
                                            final int pos = fn.lastIndexOf(sep);
                                            name = pos < 0 ? fn : fn.substring(pos + 1);
                                        }
                                        mad.setSpamFullname(prefix + name);
                                        attributes.add(Attribute.SPAM_FULLNAME_LITERAL);
                                    }
                                    {
                                        String name = mailAccount.getSent();
                                        if (null == name) {
                                            final String fn = mailAccount.getSentFullname();
                                            final int pos = fn.lastIndexOf(sep);
                                            name = pos < 0 ? fn : fn.substring(pos + 1);
                                        }
                                        mad.setSentFullname(prefix + name);
                                        attributes.add(Attribute.SENT_FULLNAME_LITERAL);
                                    }
                                    {
                                        String name = mailAccount.getTrash();
                                        if (null == name) {
                                            final String fn = mailAccount.getTrashFullname();
                                            final int pos = fn.lastIndexOf(sep);
                                            name = pos < 0 ? fn : fn.substring(pos + 1);
                                        }
                                        mad.setTrashFullname(prefix + name);
                                        attributes.add(Attribute.TRASH_FULLNAME_LITERAL);
                                    }
                                    storageService.updateMailAccount(
                                        mad,
                                        attributes,
                                        session.getUserId(),
                                        session.getContextId(),
                                        session,
                                        null,
                                        true);
                                    keepgoing = true;
                                }
                            }
                        }
                        /*
                         * Remember default folders
                         */
                        setDefaultFoldersChecked(key, true, mailSessionCache);
                        return null;
                    } catch (final MessagingException e) {
                        throw MimeMailException.handleMessagingException(e, imapConfig, session);
                    }
                }
            };
            try {
                performSynchronized(task, session);
            } catch (final OXException e) {
                throw e;
            } catch (final Exception e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    MailAccount getMailAccount(final MailAccountStorageService storageService) throws OXException {
        final DatabaseService databaseService = IMAPServiceRegistry.getService(DatabaseService.class, true);
        final int contextId = session.getContextId();
        Connection con = null;
        boolean readOnly = false;
        try {
            try {
                con = databaseService.getWritable(contextId);
            } catch (final OXException e) {
                con = databaseService.getReadOnly(contextId);
                readOnly = true;
            }
            final MailAccount[] accounts = storageService.getUserMailAccounts(session.getUserId(), contextId, con);
            for (final MailAccount acc : accounts) {
                if (acc.getId() == accountId) {
                    return acc;
                }
            }
            throw MailAccountExceptionCodes.NOT_FOUND.create(
                Integer.valueOf(accountId),
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(contextId));
        } finally {
            if (null != con) {
                if (readOnly) {
                    databaseService.backReadOnly(contextId, con);
                } else {
                    databaseService.backWritable(contextId, con);
                }
            }
        }
    }

    void sequentiallyCheckFolders(final String prefix, final char sep, final int type, final MailAccountStorageService storageService, final MailSessionCache mailSessionCache) throws OXException {
        /*
         * Load mail account
         */
        final boolean isSpamOptionEnabled;
        final MailAccount mailAccount = getMailAccount(storageService);
        {
            final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
            isSpamOptionEnabled = usm.isSpamOptionEnabled();
        }
        /*
         * Get default folders names and full names
         */
        final String[] fullNames;
        final String[] names;
        final SpamHandler spamHandler;
        {
            final DefaultFolderNamesProvider defaultFolderNamesProvider =
                new DefaultFolderNamesProvider(accountId, session.getUserId(), session.getContextId());
            if (isSpamOptionEnabled) {
                fullNames = defaultFolderNamesProvider.getDefaultFolderFullnames(mailAccount, true);
                names = defaultFolderNamesProvider.getDefaultFolderNames(mailAccount, true);
                spamHandler = SpamHandlerRegistry.getSpamHandlerBySession(session, accountId);
            } else {
                fullNames = defaultFolderNamesProvider.getDefaultFolderFullnames(mailAccount, false);
                names = defaultFolderNamesProvider.getDefaultFolderNames(mailAccount, false);
                spamHandler = NoSpamHandler.getInstance();
            }
        }
        if (MailAccount.DEFAULT_ID == accountId) {
            /*
             * No full names for primary account
             */
            Arrays.fill(fullNames, null);
        }
        /*
         * Sequentially check folders
         */
        final AtomicBoolean modified = new AtomicBoolean(false);
        final long start = DEBUG ? System.currentTimeMillis() : 0L;
        for (int i = 0; i < names.length; i++) {
            final String fullName = fullNames[i];
            final int index = i;
            if (StorageUtility.INDEX_CONFIRMED_HAM == index) {
                if (spamHandler.isCreateConfirmedHam()) {
                    performTaskFor(
                        index,
                        prefix,
                        fullName,
                        names[index],
                        sep,
                        type,
                        spamHandler.isUnsubscribeSpamFolders() ? 0 : -1,
                        modified,
                        mailSessionCache);
                } else if (DEBUG) {
                    LOG.debug("Skipping check for " + names[index] + " due to SpamHandler.isCreateConfirmedHam()=false");
                }
            } else if (StorageUtility.INDEX_CONFIRMED_SPAM == index) {
                if (spamHandler.isCreateConfirmedSpam()) {
                    performTaskFor(
                        index,
                        prefix,
                        fullName,
                        names[index],
                        sep,
                        type,
                        spamHandler.isUnsubscribeSpamFolders() ? 0 : -1,
                        modified,
                        mailSessionCache);
                } else if (DEBUG) {
                    LOG.debug("Skipping check for " + names[index] + " due to SpamHandler.isCreateConfirmedSpam()=false");
                }
            } else {
                performTaskFor(index, prefix, fullName, names[index], sep, type, 1, modified, mailSessionCache);
            }
        } // End of for loop
        if (DEBUG) {
            LOG.debug(new StringBuilder(64).append("Default folders check for account ").append(accountId).append(" took ").append(
                System.currentTimeMillis() - start).append("msec").toString());
        }
        /*
         * Check for modifications
         */
        if (modified.get()) {
            ListLsubCache.clearCache(accountId, session);
        }
    }

    private static boolean isOverQuotaException(final MessagingException e) {
        return (null != e && e.getMessage().toLowerCase(Locale.US).indexOf("quota") >= 0);
    }

    private Callable<Object> performTaskFor(final int index, final String prefix, final String fullName, final String name, final char sep, final int type, final int subscribe, final AtomicBoolean modified, final MailSessionCache cache) throws OXException {
        try {
            if (null == fullName || 0 == fullName.length()) {
                setDefaultMailFolder(index, checkDefaultFolder(index, prefix, name, sep, type, subscribe, false, modified), cache);
            } else {
                setDefaultMailFolder(index, checkDefaultFolder(index, "", fullName, sep, type, subscribe, true, modified), cache);
            }
            return null;
        } catch (final MessagingException e) {
            if (isOverQuotaException(e)) {
                /*
                 * Special handling for over-quota error
                 */
                setDefaultMailFolder(index, null, cache);
            }
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    private String[] getDefaultFolderPrefix(final IMAPFolder inboxFolder, final ListLsubEntry inboxListEntry, final MailSessionCache mailSessionCache) throws MessagingException, OXException {
        /*
         * Check for NAMESPACE capability
         */
        final char sep;
        final String inboxfullName = INBOX;
        final StringBuilder prefix = new StringBuilder(16);
        /*
         * Try NAMESPACE command...
         */
        String[] namespaces;
        try {
            namespaces = NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session, accountId);
        } catch (final MessagingException e) {
            /*
             * NAMESPACE command failed for any reason
             */
            namespaces = null;
        }
        /*
         * Check namespaces
         */
        if (null != namespaces && 0 < namespaces.length) {
            /*
             * Perform the NAMESPACE command to detect the subfolder prefix. From rfc2342: Clients often attempt to create mailboxes for
             * such purposes as maintaining a record of sent messages (e.g. "Sent Mail") or temporarily saving messages being composed (e.g.
             * "Drafts"). For these clients to inter-operate correctly with the variety of IMAP4 servers available, the user must enter the
             * prefix of the Personal Namespace used by the server. Using the NAMESPACE command, a client is able to automatically discover
             * this prefix without manual user configuration.
             */
            sep = NamespaceFoldersCache.getPersonalSeparator();
            setSeparator(sep, mailSessionCache);
            final String persPrefix = namespaces[0];
            if ((persPrefix.length() == 0)) {
                if (MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace() && IMAPCommandsCollection.canCreateSubfolder(
                    persPrefix,
                    inboxFolder)) {
                    /*
                     * Personal namespace folder allows subfolders and nested default folder are demanded, thus use INBOX as prefix although
                     * NAMESPACE signals to use no prefix.
                     */
                    prefix.append(inboxfullName).append(sep);
                }
            } else {
                prefix.append(persPrefix).append(sep);
            }
        } else {
            /*
             * Examine INBOX folder since NAMESPACE capability is not supported
             */
            sep = inboxListEntry.getSeparator();
            setSeparator(sep, mailSessionCache);
            final boolean inboxInferiors = inboxListEntry.hasInferiors();
            /*
             * Determine where to create default folders and store as a prefix for folder fullname
             */
            if (inboxInferiors) {
                if (MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace()) {
                    /*
                     * Only allow default folder below INBOX if inferiors are permitted nested default folder are explicitly allowed
                     */
                    prefix.append(inboxfullName).append(sep);
                } else if (false) {
                    // TODO: Consider NAMEPSACE
                } else {
                    /*
                     * Examine root folder if subfolders allowed
                     */
                    if (isRootInferiors()) {
                        /*
                         * Create folder beside INBOX folder
                         */
                        prefix.append("");
                    } else {
                        /*
                         * Create folder below INBOX folder
                         */
                        prefix.append(inboxfullName).append(sep);
                    }
                }
            } else {
                /*
                 * Examine root folder if subfolders allowed
                 */
                if (isRootInferiors()) {
                    /*
                     * Create folder beside INBOX folder
                     */
                    prefix.append("");
                }
                /*
                 * Cannot occur: No folders are allowed to be created, neither below INBOX nor below root folder
                 */
                throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, INBOX);
            }
        }
        return new String[] { prefix.toString(), String.valueOf(sep) };
    }

    private boolean isRootInferiors() throws MessagingException {
        return RootSubfolderCache.canCreateSubfolders((DefaultFolder) imapStore.getDefaultFolder(), true, session, accountId).booleanValue();
    }

    /**
     * Internally used by {@link IMAPDefaultFolderChecker}.
     */
    protected void setDefaultMailFolder(final int index, final String fullname, final MailSessionCache cache) {
        final String key = MailSessionParameterNames.getParamDefaultFolderArray();
        String[] arr = cache.getParameter(accountId, key);
        if (null == arr) {
            synchronized (this) {
                arr = cache.getParameter(accountId, key);
                if (null == arr) {
                    arr = new String[6];
                    Arrays.fill(arr, null);
                    cache.putParameter(accountId, key, arr);
                }
            }
        }
        arr[index] = fullname;
    }

    /**
     * Internally used by {@link IMAPDefaultFolderChecker}.
     */
    protected String checkDefaultFolder(final int index, final String prefix, final String qualifiedName, final char sep, final int type, final int subscribe, final boolean isFullname, final AtomicBoolean modified) throws MessagingException, OXException {
        /*
         * Check default folder
         */
        final StringBuilder tmp = new StringBuilder(32);
        final long st = DEBUG ? System.currentTimeMillis() : 0L;
        final int prefixLen = prefix.length();
        final String fullName = prefixLen == 0 ? qualifiedName : tmp.append(prefix).append(qualifiedName).toString();
        {
            final ListLsubEntry entry =
                modified.get() ? ListLsubCache.getActualLISTEntry(fullName, accountId, imapStore, session) : ListLsubCache.getCachedLISTEntry(
                    fullName,
                    accountId,
                    imapStore,
                    session);
            if (null != entry && entry.exists()) {
                final IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullName);
                if (1 == subscribe) {
                    if (!entry.isSubscribed()) {
                        try {
                            f.setSubscribed(true);
                            modified.set(true);
                        } catch (final MethodNotSupportedException e) {
                            LOG.error(e.getMessage(), e);
                        } catch (final MessagingException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                } else if (0 == subscribe) {
                    if (entry.isSubscribed()) {
                        try {
                            f.setSubscribed(false);
                            modified.set(true);
                        } catch (final MethodNotSupportedException e) {
                            LOG.error(e.getMessage(), e);
                        } catch (final MessagingException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                }
                if (DEBUG) {
                    tmp.setLength(0);
                    final long dur = System.currentTimeMillis() - st;
                    LOG.debug(tmp.append("Default folder \"").append(fullName).append("\" successfully checked for IMAP account ").append(
                        accountId).append(" (").append(imapConfig.getServer()).append(") in ").append(dur).append("msec.").toString());
                }
                return fullName;
            }
        }
        IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullName);
        tmp.setLength(0);
        {
            if (isFullname) {
                /*
                 * OK, a full name was passed. Try to create obviously non-existing IMAP folder.
                 */
                try {
                    if (!f.exists()) {
                        IMAPCommandsCollection.createFolder(f, sep, type, false);
                    }
                    if (1 == subscribe) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, true);
                    } else if (0 == subscribe) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, false);
                    }
                    modified.set(true);
                    return fullName;
                } catch (final MessagingException e) {
                    if (isOverQuotaException(e)) {
                        throw e;
                    }
                    if (!retry) {
                        return fullName;
                    }
                    if (MailAccount.DEFAULT_ID == accountId) {
                        throw e;
                    }
                    String prfx;
                    {
                        final ListLsubEntry inboxEntry;
                        if (modified.get()) {
                            inboxEntry = ListLsubCache.getActualLISTEntry("INBOX", accountId, imapStore, session);
                        } else {
                            inboxEntry = ListLsubCache.getCachedLISTEntry("INBOX", accountId, imapStore, session);
                        }
                        if (null != inboxEntry && inboxEntry.exists()) {
                            prfx = inboxEntry.hasInferiors() ? ("INBOX" + sep) : ("");
                        } else {
                            prfx = "";
                        }
                    }
                    if ((0 == prfx.length() && fullName.indexOf(sep) < 0) || (fullName.startsWith(prfx, 0))) {
                        // No need to retry with same prefix
                        throw e;
                    }
                    LOG.warn("Creating default folder by full name \"" + fullName + "\" failed. Retry with prefix \"" + prfx + "\".", e);
                    ListLsubCache.clearCache(accountId, session);
                    modified.set(true);
                    throw new RetryOtherPrefixException(prfx, e.getMessage(), e);
                }
            }
            /*
             * A name was passed. Perform a case-insensitive look-up because some IMAP servers do not allow to create a folder of which
             * name equals ignore-case to an existing folder.
             */
            final IMAPFolder parent;
            if (0 == prefixLen) {
                parent = (IMAPFolder) imapStore.getDefaultFolder();
            } else {
                /*
                 * Cut off trailing separator character
                 */
                final String parentFullName = prefix.substring(0, prefixLen - 1);
                parent = (IMAPFolder) imapStore.getFolder(parentFullName);
            }
            final Folder[] folders = parent.list();
            final List<String> candidates = new ArrayList<String>(2);
            for (int i = 0; i < folders.length; i++) {
                final String folderName = folders[i].getName();
                if (qualifiedName.equalsIgnoreCase(folderName)) {
                    /*
                     * Detected a similarly named folder
                     */
                    candidates.add(folderName);
                }
            }
            final int nCandidates = candidates.size();
            if (nCandidates <= 0 || nCandidates > 1) {
                /*
                 * Zero or more than one candidate found. Try to create IMAP folder
                 */
                try {
                    if (!f.exists()) {
                        IMAPCommandsCollection.createFolder(f, sep, type, false);
                    }
                    modified.set(true);
                } catch (final MessagingException e) {
                    if (isOverQuotaException(e)) {
                        throw e;
                    }
                    final String prfx = prefixLen == 0 ? "INBOX" + sep : "";
                    LOG.warn("Creating default folder by full name \"" + fullName + "\" failed. Retry with prefix \"" + prfx + "\".", e);
                    ListLsubCache.clearCache(accountId, session);
                    modified.set(true);
                    throw new RetryOtherPrefixException(prfx, e.getMessage(), e);
                }
            } else {
                if (MailAccount.DEFAULT_ID == accountId) {
                    // Must not edit default mail account. Try to create IMAP folder
                    try {
                        if (!f.exists()) {
                            IMAPCommandsCollection.createFolder(f, sep, type, false);
                        }
                        modified.set(true);
                    } catch (final MessagingException e) {
                        if (isOverQuotaException(e)) {
                            throw e;
                        }
                        LOG.warn(
                            new StringBuilder(64).append("Creation of non-existing default IMAP folder \"").append(fullName).append(
                                "\" failed.").toString(),
                            e);
                        ListLsubCache.clearCache(accountId, session);
                        modified.set(true);
                    }
                } else {
                    /*
                     * Found _ONE_ candidate of which name passed ignore-case comparison
                     */
                    final String candidate = candidates.get(0);
                    final MailAccountDescription mad = new MailAccountDescription();
                    final Set<Attribute> attributes;
                    mad.setId(accountId);
                    switch (index) {
                    case StorageUtility.INDEX_CONFIRMED_HAM:
                        mad.setConfirmedHam(candidate);
                        attributes = EnumSet.of(Attribute.CONFIRMED_HAM_LITERAL);
                        break;
                    case StorageUtility.INDEX_CONFIRMED_SPAM:
                        mad.setConfirmedSpam(candidate);
                        attributes = EnumSet.of(Attribute.CONFIRMED_SPAM_LITERAL);
                        break;
                    case StorageUtility.INDEX_DRAFTS:
                        mad.setDrafts(candidate);
                        attributes = EnumSet.of(Attribute.DRAFTS_LITERAL);
                        break;
                    case StorageUtility.INDEX_SENT:
                        mad.setSent(candidate);
                        attributes = EnumSet.of(Attribute.SENT_LITERAL);
                        break;
                    case StorageUtility.INDEX_SPAM:
                        mad.setSpam(candidate);
                        attributes = EnumSet.of(Attribute.SPAM_LITERAL);
                        break;
                    case StorageUtility.INDEX_TRASH:
                        mad.setTrash(candidate);
                        attributes = EnumSet.of(Attribute.TRASH_LITERAL);
                        break;
                    default:
                        throw new MessagingException("Unexpected index: " + index);
                    }
                    {
                        final MailAccountStorageService storageService =
                            IMAPServiceRegistry.getService(MailAccountStorageService.class, true);

                        storageService.updateMailAccount(
                            mad,
                            attributes,
                            session.getUserId(),
                            session.getContextId(),
                            session);
                    }
                    final String fn = tmp.append(prefix).append(candidate).toString();
                    tmp.setLength(0);
                    f = (IMAPFolder) imapStore.getFolder(fn);
                }
            }
        }
        if (1 == subscribe) {
            if (!f.isSubscribed()) {
                try {
                    f.setSubscribed(true);
                } catch (final MethodNotSupportedException e) {
                    LOG.error(e.getMessage(), e);
                } catch (final MessagingException e) {
                    LOG.error(e.getMessage(), e);
                } finally {
                    modified.set(true);
                }
            }
        } else if (0 == subscribe) {
            if (f.isSubscribed()) {
                try {
                    f.setSubscribed(false);
                } catch (final MethodNotSupportedException e) {
                    LOG.error(e.getMessage(), e);
                } catch (final MessagingException e) {
                    LOG.error(e.getMessage(), e);
                } finally {
                    modified.set(true);
                }
            }
        }
        if (DEBUG) {
            final long dur = System.currentTimeMillis() - st;
            LOG.debug(tmp.append("Default folder \"").append(f.getFullName()).append("\" successfully checked for IMAP account ").append(
                accountId).append(" (").append(imapConfig.getServer()).append(") in ").append(dur).append("msec.").toString());
            tmp.setLength(0);
        }
        return f.getFullName();
    }

    boolean isDefaultFoldersChecked(final String key, final MailSessionCache mailSessionCache) {
        final Boolean b = mailSessionCache.getParameter(accountId, key);
        return (b != null) && b.booleanValue();
    }

    void setDefaultFoldersChecked(final String key, final boolean checked, final MailSessionCache mailSessionCache) {
        mailSessionCache.putParameter(accountId, key, Boolean.valueOf(checked));
    }

    /**
     * Stores specified separator character in session parameters for future look-ups.
     *
     * @param separator The separator character
     */
    private void setSeparator(final char separator, final MailSessionCache mailSessionCache) {
        mailSessionCache.putParameter(accountId, MailSessionParameterNames.getParamSeparator(), Character.valueOf(separator));
    }

    private static final class RetryOtherPrefixException extends RuntimeException {

        private static final long serialVersionUID = 544473465523324664L;

        private final String prefix;

        public RetryOtherPrefixException(final String prefix, final String message, final Throwable cause) {
            super(message, cause);
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

}
