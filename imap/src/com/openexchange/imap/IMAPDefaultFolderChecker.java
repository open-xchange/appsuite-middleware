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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.MBoxEnabledCache;
import com.openexchange.imap.cache.RootSubfolderCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.notify.IMAPNotifierMessageRecentListener;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.secret.SecretService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.NoSpamHandler;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.CompletionFuture;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.UnsynchronizedStringWriter;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link IMAPDefaultFolderChecker} - The IMAP default folder checker.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPDefaultFolderChecker {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPDefaultFolderChecker.class);

    private static final String INBOX = "INBOX";

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final int FOLDER_TYPE = (IMAPFolder.HOLDS_MESSAGES | IMAPFolder.HOLDS_FOLDERS);

    private static final int DEFAULT_MAX_RUNNING_MILLIS = 120000;

    private final Session session;

    private final int accountId;

    private final AccessedIMAPStore imapStore;

    private final Context ctx;

    private final IMAPConfig imapConfig;

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
        this.accountId = accountId;
        this.session = session;
        this.imapStore = imapStore;
        this.ctx = ctx;
        this.imapConfig = imapConfig;
    }

    /**
     * Checks if given fullname denotes a default folder.
     * 
     * @param folderFullName The fullname to check
     * @return <code>true</code> if given fullname denotes a default folder; otherwise <code>false</code>
     * @throws MailException If check for default folder fails
     */
    public boolean isDefaultFolder(final String folderFullName) throws MailException {
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
     * @throws MailException If default folder retrieval fails
     */
    public String getDefaultFolder(final int index) throws MailException {
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
     * @throws MailException If default folder check fails
     */
    public void checkDefaultFolders() throws MailException {
        checkDefaultFolders(MailSessionParameterNames.getParamDefaultFolderChecked(), MailSessionCache.getInstance(session));
    }

    private Object getLockObject() {
        final Object lock = session.getParameter(Session.PARAM_LOCK);
        return null == lock ? session : lock;
    }

    private void checkDefaultFolders(final String key, final MailSessionCache mailSessionCache) throws MailException {
        if (!isDefaultFoldersChecked(key, mailSessionCache)) {
            synchronized (getLockObject()) {
                if (isDefaultFoldersChecked(key, mailSessionCache)) {
                    return;
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
                        if ((imapStore).notifyRecent()) {
                            IMAPNotifierMessageRecentListener.addNotifierFor(tmp, INBOX, accountId, session, true);
                        }
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
                                IMAPCommandsCollection.createFolder(tmp, sep, IMAPFolder.HOLDS_MESSAGES);
                            }
                            ListLsubCache.addSingle(INBOX, accountId, tmp, session);
                            inboxFolder = (IMAPFolder) imapStore.getFolder(INBOX);
                            if ((imapStore).notifyRecent()) {
                                IMAPNotifierMessageRecentListener.addNotifierFor(inboxFolder, INBOX, accountId, session, true);
                            }
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
                    /*
                     * Get prefix for default folder names, NOT fullnames!
                     */
                    final String prefix;
                    final char sep;
                    {
                        final String[] sa = getDefaultFolderPrefix(inboxFolder, inboxListEntry, mailSessionCache);
                        prefix = sa[0];
                        sep = sa[1].charAt(0);
                    }
                    /*
                     * Check for mbox
                     */
                    final int type;
                    final boolean mboxEnabled =
                        MBoxEnabledCache.isMBoxEnabled(imapConfig, inboxFolder, prefix);
                    if (mboxEnabled) {
                        type = IMAPFolder.HOLDS_MESSAGES;
                    } else {
                        type = FOLDER_TYPE;
                    }
                    /*
                     * Load mail account
                     */
                    final boolean isSpamOptionEnabled;
                    final MailAccount mailAccount;
                    try {
                        mailAccount =
                            IMAPServiceRegistry.getService(MailAccountStorageService.class, true).getMailAccount(
                                accountId,
                                session.getUserId(),
                                session.getContextId());
                        final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
                        isSpamOptionEnabled = usm.isSpamOptionEnabled();
                    } catch (final ServiceException e) {
                        throw new MailException(e);
                    } catch (final MailAccountException e) {
                        throw new MailException(e);
                    }
                    /*
                     * Check default folders
                     */
                    final DefaultFolderNamesProvider defaultFolderNamesProvider =
                        new DefaultFolderNamesProvider(accountId, session.getUserId(), session.getContextId());
                    final String[] defaultFolderFullnames =
                        defaultFolderNamesProvider.getDefaultFolderFullnames(mailAccount, isSpamOptionEnabled);
                    final String[] defaultFolderNames = defaultFolderNamesProvider.getDefaultFolderNames(mailAccount, isSpamOptionEnabled);
                    final SpamHandler spamHandler;
                    {
                        spamHandler =
                            isSpamOptionEnabled ? SpamHandlerRegistry.getSpamHandlerBySession(session, accountId) : NoSpamHandler.getInstance();
                    }
                    final CompletionFuture<Object> completionFuture;
                    final int count;
                    {
                        final List<Task<Object>> tasks = new ArrayList<Task<Object>>(defaultFolderNames.length);
                        for (int i = 0; i < defaultFolderNames.length; i++) {
                            final String fullname = defaultFolderFullnames[i];
                            final int index = i;
                            if (StorageUtility.INDEX_CONFIRMED_HAM == index) {
                                if (spamHandler.isCreateConfirmedHam()) {
                                    submitFolderCheckTask(
                                        tasks,
                                        index,
                                        prefix,
                                        fullname,
                                        defaultFolderNames[index],
                                        sep,
                                        type,
                                        spamHandler.isUnsubscribeSpamFolders() ? 0 : -1,
                                        mailSessionCache);
                                } else if (DEBUG) {
                                    LOG.debug("Skipping check for " + defaultFolderNames[index] + " due to SpamHandler.isCreateConfirmedHam()=false");
                                }
                            } else if (StorageUtility.INDEX_CONFIRMED_SPAM == index) {
                                if (spamHandler.isCreateConfirmedSpam()) {
                                    submitFolderCheckTask(
                                        tasks,
                                        index,
                                        prefix,
                                        fullname,
                                        defaultFolderNames[index],
                                        sep,
                                        type,
                                        spamHandler.isUnsubscribeSpamFolders() ? 0 : -1,
                                        mailSessionCache);
                                } else if (DEBUG) {
                                    LOG.debug("Skipping check for " + defaultFolderNames[index] + " due to SpamHandler.isCreateConfirmedSpam()=false");
                                }
                            } else {
                                submitFolderCheckTask(
                                    tasks,
                                    index,
                                    prefix,
                                    fullname,
                                    defaultFolderNames[index],
                                    sep,
                                    type,
                                    1,
                                    mailSessionCache);
                            }
                        }
                        try {
                            completionFuture =
                                IMAPServiceRegistry.getService(ThreadPoolService.class, true).invoke(
                                    tasks,
                                    CallerRunsBehavior.getInstance());
                        } catch (final ServiceException e) {
                            throw new IMAPException(e);
                        }
                        count = tasks.size();
                    }
                    final long start = System.currentTimeMillis();
                    try {
                        for (int i = 0; i < count; i++) {
                            final Future<Object> f = completionFuture.take();
                            if (null == f) {
                                // Waiting time elapsed
                                throw new MailException(
                                    MailException.Code.DEFAULT_FOLDER_CHECK_FAILED,
                                    imapConfig.getServer(),
                                    imapConfig.getLogin(),
                                    Integer.valueOf(session.getUserId()),
                                    Integer.valueOf(session.getContextId()),
                                    "Waiting time elapsed.");
                            }
                            try {
                                f.get();
                            } catch (final ExecutionException e) {
                                if (MailAccount.DEFAULT_ID == accountId) {
                                    /*
                                     * Mandatory for primary mail account
                                     */
                                    throw ThreadPools.launderThrowable(e, MailException.class);
                                }
                                final MailException me = ThreadPools.launderThrowable(e, MailException.class);
                                LOG.warn(new StringBuilder(128).append("A default folder could not be created on IMAP server ").append(
                                    imapConfig.getServer()).append(" for login ").append(imapConfig.getLogin()).append(" (user=").append(
                                    session.getUserId()).append(", context=").append(session.getContextId()).append("): ").append(
                                    me.getMessage()).toString(), me);
                            }
                        }
                    } catch (final InterruptedException e) {
                        // Keep interrupted status
                        throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
                    }
                    if (DEBUG) {
                        LOG.debug(new StringBuilder(64).append("Default folders check for account ").append(accountId).append(" took ").append(
                            System.currentTimeMillis() - start).append("msec").toString());
                    }
                    setDefaultFoldersChecked(key, true, mailSessionCache);
                } catch (final MessagingException e) {
                    throw MIMEMailException.handleMessagingException(e, imapConfig, session);
                }
            }
        }
    }

    private void submitFolderCheckTask(final List<Task<Object>> tasks, final int index, final String prefix, final String fullname, final String defaultFolderName, final char sep, final int type, final int subscribe, final MailSessionCache mailSessionCache) {
        tasks.add(new AbstractCallable(imapConfig, session) {

            public Object call() throws MailException {
                try {
                    if (null == fullname || 0 == fullname.length()) {
                        setDefaultMailFolder(
                            index,
                            checkDefaultFolder(index, prefix, defaultFolderName, sep, type, subscribe, false),
                            mailSessionCache);
                    } else {
                        setDefaultMailFolder(index, checkDefaultFolder(index, "", fullname, sep, type, subscribe, true), mailSessionCache);
                    }
                    return null;
                } catch (final MessagingException e) {
                    throw MIMEMailException.handleMessagingException(e, config, sess);
                }
            }
        });
    }

    private String[] getDefaultFolderPrefix(final IMAPFolder inboxFolder, final ListLsubEntry inboxListEntry, final MailSessionCache mailSessionCache) throws MessagingException, IMAPException {
        /*
         * Check for NAMESPACE capability
         */
        final char sep;
        final String inboxfullName = INBOX;
        final StringBuilder prefix = new StringBuilder(16);
        if (imapConfig.getImapCapabilities().hasNamespace()) {
            /*
             * Perform the NAMESPACE command to detect the subfolder prefix. From rfc2342: Clients often attempt to create mailboxes for
             * such purposes as maintaining a record of sent messages (e.g. "Sent Mail") or temporarily saving messages being composed (e.g.
             * "Drafts"). For these clients to inter-operate correctly with the variety of IMAP4 servers available, the user must enter the
             * prefix of the Personal Namespace used by the server. Using the NAMESPACE command, a client is able to automatically discover
             * this prefix without manual user configuration.
             */
            final Folder[] personalNamespaces = imapStore.getPersonalNamespaces();
            if (personalNamespaces == null || personalNamespaces[0] == null) {
                throw IMAPException.create(IMAPException.Code.MISSING_PERSONAL_NAMESPACE, imapConfig, session, new Object[0]);
            }
            sep = personalNamespaces[0].getSeparator();
            setSeparator(sep, mailSessionCache);
            final String persPrefix = personalNamespaces[0].getFullName();
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
    void setDefaultMailFolder(final int index, final String fullname, final MailSessionCache mailSessionCache) {
        final String key = MailSessionParameterNames.getParamDefaultFolderArray();
        String[] arr = mailSessionCache.getParameter(accountId, key);
        if (null == arr) {
            synchronized (this) {
                arr = mailSessionCache.getParameter(accountId, key);
                if (null == arr) {
                    arr = new String[6];
                    mailSessionCache.putParameter(accountId, key, arr);
                }
            }
        }
        arr[index] = fullname;
    }

    /**
     * Internally used by {@link IMAPDefaultFolderChecker}.
     */
    String checkDefaultFolder(final int index, final String prefix, final String name, final char sep, final int type, final int subscribe, final boolean isFullname) throws MessagingException, MailException {
        /*
         * Check default folder
         */
        final StringBuilder tmp = new StringBuilder(32);
        final boolean checkSubscribed = true;
        final long st = System.currentTimeMillis();
        final String fullName = tmp.append(prefix).append(name).toString();
        {
            final ListLsubEntry entry = ListLsubCache.getCachedLISTEntry(fullName, accountId, imapStore, session);
            if (entry.exists()) {
                if (checkSubscribed) {
                    final IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullName);
                    if ((imapStore).notifyRecent()) {
                        IMAPNotifierMessageRecentListener.addNotifierFor(f, fullName, accountId, session, true);
                    }
                    if (1 == subscribe) {
                        if (!entry.isSubscribed()) {
                            try {
                                f.setSubscribed(true);
                                ListLsubCache.addSingle(fullName, accountId, f, session);
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
                                ListLsubCache.addSingle(fullName, accountId, f, session);
                            } catch (final MethodNotSupportedException e) {
                                LOG.error(e.getMessage(), e);
                            } catch (final MessagingException e) {
                                LOG.error(e.getMessage(), e);
                            }
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
        if ((imapStore).notifyRecent()) {
            IMAPNotifierMessageRecentListener.addNotifierFor(f, fullName, accountId, session, true);
        }
        tmp.setLength(0);
        {
            if (isFullname) {
                /*
                 * OK, a fullname was passed. Try to create obviously non-existing IMAP folder.
                 */
                try {
                    IMAPCommandsCollection.createFolder(f, sep, type);
                    ListLsubCache.addSingle(fullName, accountId, f, session);
                    return null;
                } catch (final MessagingException e) {
                    LOG.warn(new StringBuilder(64).append("Creation of non-existing default IMAP folder \"").append(fullName).append(
                        "\" failed.").toString(), e);
                    ListLsubCache.clearCache(accountId, session);
                }
            } else {
                /*
                 * A name was passed. Perform a case-insensitive look-up because some IMAP servers do not allow to create a folder of which
                 * name equals ignore-case to an existing folder.
                 */
                final IMAPFolder parent;
                final int len = prefix.length();
                if (0 == len) {
                    parent = (IMAPFolder) imapStore.getDefaultFolder();
                } else {
                    /*
                     * Cut off trailing separator character
                     */
                    final String parentFullName = prefix.substring(0, len - 1);
                    parent = (IMAPFolder) imapStore.getFolder(parentFullName);
                    if ((imapStore).notifyRecent()) {
                        IMAPNotifierMessageRecentListener.addNotifierFor(parent, parentFullName, accountId, session, true);
                    }
                }
                final Folder[] folders = parent.list();
                final List<String> candidates = new ArrayList<String>(2);
                for (int i = 0; i < folders.length; i++) {
                    final String folderName = folders[i].getName();
                    if (name.equalsIgnoreCase(folderName)) {
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
                        IMAPCommandsCollection.createFolder(f, sep, type);
                        ListLsubCache.addSingle(fullName, accountId, f, session);
                    } catch (final MessagingException e) {
                        LOG.warn(new StringBuilder(64).append("Creation of non-existing default IMAP folder \"").append(fullName).append(
                            "\" failed.").toString(), e);
                        ListLsubCache.clearCache(accountId, session);
                    }
                } else {
                    if (MailAccount.DEFAULT_ID == accountId) {
                        // Must not edit default mail account. Try to create IMAP folder
                        try {
                            IMAPCommandsCollection.createFolder(f, sep, type);
                            ListLsubCache.addSingle(fullName, accountId, f, session);
                        } catch (final MessagingException e) {
                            LOG.warn(
                                new StringBuilder(64).append("Creation of non-existing default IMAP folder \"").append(fullName).append(
                                    "\" failed.").toString(),
                                e);
                            ListLsubCache.clearCache(accountId, session);
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
                        try {
                            final MailAccountStorageService storageService =
                                IMAPServiceRegistry.getService(MailAccountStorageService.class, true);
                            final SecretService secretService = IMAPServiceRegistry.getService(SecretService.class);
                            
                            storageService.updateMailAccount(
                                mad,
                                attributes,
                                session.getUserId(),
                                session.getContextId(),
                                secretService.getSecret(session));
                        } catch (final ServiceException e) {
                            throw new IMAPException(e);
                        } catch (final MailAccountException e) {
                            throw new IMAPException(e);
                        }
                        final String fn = tmp.append(prefix).append(candidate).toString();
                        tmp.setLength(0);
                        f = (IMAPFolder) imapStore.getFolder(fn);
                        if ((imapStore).notifyRecent()) {
                            IMAPNotifierMessageRecentListener.addNotifierFor(f, fullName, accountId, session, true);
                        }
                    }
                }
            }
            /*-
             * 
             * 
            final IMAPException oxme = new IMAPException(
                IMAPException.Code.NO_DEFAULT_FOLDER_CREATION,
                tmp.append(prefix).append(name).toString());
            tmp.setLength(0);
            LOG.error(oxme.getMessage(), oxme);
            checkSubscribed = false;
             */
        }
        if (checkSubscribed) {
            if (1 == subscribe) {
                if (!f.isSubscribed()) {
                    try {
                        f.setSubscribed(true);
                    } catch (final MethodNotSupportedException e) {
                        LOG.error(e.getMessage(), e);
                    } catch (final MessagingException e) {
                        LOG.error(e.getMessage(), e);
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
                    }
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

    private boolean isDefaultFoldersChecked(final String key, final MailSessionCache mailSessionCache) {
        final Boolean b = mailSessionCache.getParameter(accountId, key);
        return (b != null) && b.booleanValue();
    }

    private void setDefaultFoldersChecked(final String key, final boolean checked, final MailSessionCache mailSessionCache) {
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

    private static abstract class AbstractCallable extends AbstractTask<Object> {

        protected final IMAPConfig config;

        protected final Session sess;

        protected AbstractCallable(final IMAPConfig config, final Session sess) {
            super();
            this.config = config;
            this.sess = sess;
        }
    } // End of AbstractCallable

}
