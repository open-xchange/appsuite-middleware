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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.MBoxEnabledCache;
import com.openexchange.imap.cache.NamespaceFoldersCache;
import com.openexchange.imap.cache.RootSubfolderCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.services.Services;
import com.openexchange.log.Log;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
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

    protected static final String INBOX = "INBOX";

    protected static final int FOLDER_TYPE = (IMAPFolder.HOLDS_MESSAGES | IMAPFolder.HOLDS_FOLDERS);

    // ----------------- Members ------------------

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
    protected <V> V performSynchronized(final Callable<V> task, final Session session) throws Exception {
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null == lock) {
            lock = Session.EMPTY_LOCK;
        }
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
                        final boolean mboxEnabled = MBoxEnabledCache.isMBoxEnabled(imapConfig, inboxFolder, prefix);
                        final int type = mboxEnabled ? Folder.HOLDS_MESSAGES : FOLDER_TYPE;
                        sequentiallyCheckFolders(prefix, sep, type, Services.getService(MailAccountStorageService.class), mailSessionCache);
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

    protected void sequentiallyCheckFolders(final String prefix, final char sep, final int type, final MailAccountStorageService storageService, final MailSessionCache mailSessionCache) throws OXException {
        /*
         * Load mail account
         */
        final boolean isSpamOptionEnabled;
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
                fullNames = defaultFolderNamesProvider.getDefaultFolderFullnames(imapConfig, true);
                names = defaultFolderNamesProvider.getDefaultFolderNames(imapConfig, true);
                spamHandler = SpamHandlerRegistry.getSpamHandlerBySession(session, accountId);
            } else {
                fullNames = defaultFolderNamesProvider.getDefaultFolderFullnames(imapConfig, false);
                names = defaultFolderNamesProvider.getDefaultFolderNames(imapConfig, false);
                spamHandler = NoSpamHandler.getInstance();
            }
        }
        if (MailAccount.DEFAULT_ID == accountId) {
            /*-
             * Check full names for primary account:
             *
             * Null'ify full name if not on root level OR not equal to name; meaning not intended to create default folders next to INBOX
             * In that case create them with respect determined prefix
             */
            for (int i = 0; i < fullNames.length; i++) {
                final String fullName = fullNames[i];
                if (null != fullName && (fullName.indexOf(sep) > 0 || !fullName.equals(names[i]))) {
                    fullNames[i] = null;
                }
            }
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

    /**
     * Checks for possible over-quota error.
     */
    protected static boolean isOverQuotaException(final MessagingException e) {
        return MimeMailException.isOverQuotaException(e);
    }

    /** Gets fall-back name */
    private static String getFallbackName(final int index) {
        switch (index) {
        case StorageUtility.INDEX_CONFIRMED_HAM:
            return DefaultFolderNamesProvider.DEFAULT_PROVIDER.getConfirmedHam();
        case StorageUtility.INDEX_CONFIRMED_SPAM:
            return DefaultFolderNamesProvider.DEFAULT_PROVIDER.getConfirmedSpam();
        case StorageUtility.INDEX_DRAFTS:
            return DefaultFolderNamesProvider.DEFAULT_PROVIDER.getDrafts();
        case StorageUtility.INDEX_SENT:
            return DefaultFolderNamesProvider.DEFAULT_PROVIDER.getSent();
        case StorageUtility.INDEX_SPAM:
            return DefaultFolderNamesProvider.DEFAULT_PROVIDER.getSpam();
        case StorageUtility.INDEX_TRASH:
            return DefaultFolderNamesProvider.DEFAULT_PROVIDER.getTrash();
        default:
            return "Nope";
        }
    }

    /**
     * Performs default folder check for specified arguments.
     *
     * @param index The index
     * @param prefix The prefix
     * @param fullName The full name
     * @param name The name
     * @param sep The separator character
     * @param type The folder type
     * @param subscribe Whether to subscribe
     * @param modified Whether folders has been modified during check
     * @param cache The associated cache
     * @return Dummy <code>null</code>
     * @throws OXException If an error occurs
     */
    protected Callable<Object> performTaskFor(final int index, final String prefix, final String fullName, final String name, final char sep, final int type, final int subscribe, final AtomicBoolean modified, final MailSessionCache cache) throws OXException {
        try {
            if (isEmpty(fullName)) {
                if (isEmpty(name)) {
                    // Neither full name nor name
                    setDefaultMailFolder(index, checkDefaultFolder(index, prefix, getFallbackName(index), sep, type, subscribe, false, modified), cache);
                } else {
                    setDefaultMailFolder(index, checkDefaultFolder(index, prefix, name, sep, type, subscribe, false, modified), cache);
                }
            } else {
                setDefaultMailFolder(index, checkDefaultFolder(index, "", fullName, sep, type, subscribe, true, modified), cache);
            }
        } catch (final OXException e) {
            final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(1024);
            sb.append("Couldn't check default folder: ");
            sb.append((null == fullName ? (prefix + name) : fullName));
            if (Log.appendTraceToMessage()) {
                final String lineSeparator = System.getProperty("line.separator");
                sb.append(lineSeparator).append(lineSeparator);
                appendStackTrace(e.getStackTrace(), sb, new ClassNameMatcher(IMAPDefaultFolderChecker.class.getSimpleName()), lineSeparator);
                LOG.warn(sb.toString());
            } else {
                LOG.warn(sb.toString(), e);
            }
            setDefaultMailFolder(index, null, cache);
            e.setCategory(Category.CATEGORY_WARNING);
            imapStore.getImapAccess().addWarnings(Collections.singleton(e));
        } catch (final FolderClosedException e) {
            /*
             * Not possible to retry since connection is broken
             */
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        } catch (final StoreClosedException e) {
            /*
             * Not possible to retry since connection is broken
             */
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        } catch (final MessagingException e) {
            if (isOverQuotaException(e)) {
                /*
                 * Special handling for over-quota error
                 */
                throw MimeMailException.handleMessagingException(e, imapConfig, session);
            }
            final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(1024);
            sb.append("Couldn't check default folder: ");
            sb.append((null == fullName ? (prefix + name) : fullName));
            if (Log.appendTraceToMessage()) {
                final String lineSeparator = System.getProperty("line.separator");
                sb.append(lineSeparator).append(lineSeparator);
                appendStackTrace(e.getStackTrace(), sb, new ClassNameMatcher(IMAPDefaultFolderChecker.class.getSimpleName()), lineSeparator);
                LOG.warn(sb.toString());
            } else {
                LOG.warn(sb.toString(), e);
            }
            setDefaultMailFolder(index, null, cache);
            final OXException warning = MimeMailException.handleMessagingException(e, imapConfig, session).setCategory(Category.CATEGORY_WARNING);
            imapStore.getImapAccess().addWarnings(Collections.singleton(warning));
        }
        return null;
    }

    /**
     * Gets the default folder prefix.
     *
     * @param inboxFolder The INBOX folder
     * @param inboxListEntry The associated LIST/LSUB entry
     * @param mailSessionCache The associated cache
     * @return The prefix and separator character as an array
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If an error occurs
     */
    protected String[] getDefaultFolderPrefix(final IMAPFolder inboxFolder, final ListLsubEntry inboxListEntry, final MailSessionCache mailSessionCache) throws MessagingException, OXException {
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

    protected boolean isRootInferiors() throws MessagingException {
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
                if (1 == subscribe) {
                    if (!entry.isSubscribed()) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, true);
                        modified.set(true);
                    }
                } else if (0 == subscribe) {
                    if (entry.isSubscribed()) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, false);
                        modified.set(true);
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
        if (isFullname) {
            /*
             * OK, a full name was passed. Try to create obviously non-existing IMAP folder.
             */
            try {
                if (!f.exists()) {
                    /*
                     * Check against siblings
                     */
                    final IMAPFolder parent = (IMAPFolder) f.getParent();
                    final Folder[] folders = parent.list();
                    final String mName = f.getName();
                    final List<Folder> candidates = new ArrayList<Folder>(2);
                    for (int i = 0; i < folders.length; i++) {
                        final Folder child = folders[i];
                        if (mName.equalsIgnoreCase(child.getName())) {
                            /*
                             * Detected a similarly named folder
                             */
                            candidates.add(child);
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
                            throw e;
                        }
                    } else {
                        // Found one candidate
                        closeSafe(f);
                        f = (IMAPFolder) candidates.get(0);
                    }
                }
                if (1 == subscribe) {
                    if (!f.isSubscribed()) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, true);
                        modified.set(true);
                    }
                } else if (0 == subscribe) {
                    if (f.isSubscribed()) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, false);
                        modified.set(true);
                    }
                }
                return fullName;
            } catch (final MessagingException e) {
                if (isOverQuotaException(e)) {
                    throw e;
                }
                throw e;
            }
        }
        /*
         * A name was passed. Perform a case-insensitive look-up because some IMAP servers do not allow to create a folder of which name
         * equals ignore-case to an existing folder.
         */
        if (!f.exists()) {
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
            final List<Folder> candidates = new ArrayList<Folder>(2);
            for (int i = 0; i < folders.length; i++) {
                final Folder child = folders[i];
                if (qualifiedName.equalsIgnoreCase(child.getName())) {
                    /*
                     * Detected a similarly named folder
                     */
                    candidates.add(child);
                }
            }
            final int nCandidates = candidates.size();
            if (nCandidates <= 0 || nCandidates > 1) {
                /*
                 * Zero or more than one candidate found. Try to create IMAP folder
                 */
                try {
                    IMAPCommandsCollection.createFolder(f, sep, type, false);
                    modified.set(true);
                } catch (final MessagingException e) {
                    if (isOverQuotaException(e)) {
                        throw e;
                    }
                    throw e;
                }
            } else {
                // Found one candidate
                closeSafe(f);
                f = (IMAPFolder) candidates.get(0);
            }
        }
        if (1 == subscribe) {
            if (!f.isSubscribed()) {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, true);
                modified.set(true);
            }
        } else if (0 == subscribe) {
            if (f.isSubscribed()) {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, false);
                modified.set(true);
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

    protected boolean isDefaultFoldersChecked(final String key, final MailSessionCache mailSessionCache) {
        final Boolean b = mailSessionCache.getParameter(accountId, key);
        return (b != null) && b.booleanValue();
    }

    protected void setDefaultFoldersChecked(final String key, final boolean checked, final MailSessionCache mailSessionCache) {
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

    /**
     * Matcher for {@link StackTraceElement}s.
     */
    protected static interface StackTraceElementMatcher {

        boolean accepts(StackTraceElement stackTraceElement);
    }

    private static final class ClassNameMatcher implements StackTraceElementMatcher {

        private final String className;
        private boolean found;

        protected ClassNameMatcher(String className) {
            super();
            this.className = className;
        }

        @Override
        public boolean accepts(StackTraceElement ste) {
            if (found) {
                return false;
            }
            final String className = ste.getClassName();
            if (null == className) {
                found = true;
                return false;
            }
            // Check
            if (className.indexOf(this.className) >= 0) {
                found = true;
                return true;
            }
            return true;
        }

    }

    /**
     * Appends stack trace.
     *
     * @param trace The stack trace
     * @param sb The builder
     * @param num The max. number of elements to append
     */
    protected static void appendStackTrace(final StackTraceElement[] trace, final com.openexchange.java.StringAllocator sb, final StackTraceElementMatcher matcher, final String lineSeparator) {
        if (null == trace) {
            return;
        }
        for (int i = 0; i < trace.length && matcher.accepts(trace[i]); i++) {
            final StackTraceElement ste = trace[i];
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    sb.append("(Native Method)");
                } else {
                    final String fileName = ste.getFileName();
                    if (null == fileName) {
                        sb.append("(Unknown Source)");
                    } else {
                        final int lineNumber = ste.getLineNumber();
                        sb.append('(').append(fileName);
                        if (lineNumber >= 0) {
                            sb.append(':').append(lineNumber);
                        }
                        sb.append(')');
                    }
                }
                sb.append(lineSeparator);
            }
        }
    }

    private static void closeSafe(final Folder folder) {
        if (null != folder) {
            try {
                folder.close(false);
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /** Checks for empty string */
    protected static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
