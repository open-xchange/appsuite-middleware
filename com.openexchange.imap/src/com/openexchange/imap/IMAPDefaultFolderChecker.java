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

package com.openexchange.imap;

import static com.openexchange.java.Strings.isEmpty;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.MBoxEnabledCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.services.Services;
import com.openexchange.java.BoolReference;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
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
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPDefaultFolderChecker} - The IMAP default folder checker.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IMAPDefaultFolderChecker {

    /** The logger constant */
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPDefaultFolderChecker.class);

    /** The INBOX full name. */
    protected static final String INBOX = "INBOX";

    /** The folder type for both - holds messages and holds folders. */
    protected static final int FOLDER_TYPE = (IMAPFolder.HOLDS_MESSAGES | IMAPFolder.HOLDS_FOLDERS);

    /** The flags for SPECIAL-USE markers */
    protected static final String[] SPECIAL_USES;
    static {
        final String[] sa = new String[4];
        sa[StorageUtility.INDEX_DRAFTS] = "\\Drafts";
        sa[StorageUtility.INDEX_SENT] = "\\Sent";
        sa[StorageUtility.INDEX_SPAM] = "\\Junk";
        sa[StorageUtility.INDEX_TRASH] = "\\Trash";
        SPECIAL_USES = sa;
    }

    // -------------------------------------------------------------------------------------------------------- //

    protected final Session session;
    protected final int accountId;
    protected final IMAPStore imapStore;
    protected final Context ctx;
    protected final IMAPAccess imapAccess;
    protected final IMAPConfig imapConfig;
    protected final boolean hasMetadata;
    protected final boolean ignoreSubscription;

    /**
     * Initializes a new {@link IMAPDefaultFolderChecker}.
     *
     * @param accountId The account ID
     * @param session The session
     * @param ctx The context
     * @param imapStore The (connected) IMAP store
     * @param imapConfig The IMAP configuration
     */
    public IMAPDefaultFolderChecker(int accountId, Session session, Context ctx, IMAPStore imapStore, IMAPAccess imapAccess, boolean hasMetadata) {
        super();
        this.accountId = accountId;
        this.session = session;
        this.imapStore = imapStore;
        this.ctx = ctx;
        this.imapAccess = imapAccess;
        imapConfig = imapAccess.getIMAPConfig();
        this.hasMetadata = hasMetadata;
        ignoreSubscription = imapConfig.getIMAPProperties().isIgnoreSubscription();
    }

    /**
     * Checks if given full name denotes a default folder (w/o considering archive folder).
     *
     * @param folderFullName The full name to check
     * @return <code>true</code> if given full name denotes a default folder; otherwise <code>false</code>
     * @throws OXException If check for default folder fails
     */
    public boolean isDefaultFolder(String folderFullName) throws OXException {
        return isDefaultFolder(folderFullName, false);
    }

    /**
     * Checks if given full name denotes a default folder.
     *
     * @param folderFullName The full name to check
     * @param checkArchive Whether the archive folder should also be checked
     * @return <code>true</code> if given full name denotes a default folder; otherwise <code>false</code>
     * @throws OXException If check for default folder fails
     */
    public boolean isDefaultFolder(String folderFullName, boolean checkArchive) throws OXException {
        if (folderFullName.equalsIgnoreCase(INBOX)) {
            return true;
        }

        for (int index = 6; index-- > 0;) {
            if (folderFullName.equalsIgnoreCase(getDefaultFolder(index))) {
                return true;
            }
        }

        if (checkArchive) {
            MailAccountStorageService storageService = com.openexchange.imap.services.Services.getService(MailAccountStorageService.class);
            MailAccount mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
            String archiveFullName = optArchiveFullName(mailAccount, imapAccess);
            if (null != archiveFullName && archiveFullName.equals(folderFullName)) {
                return true;
            }
        }

        return false;
    }

    private String optArchiveFullName(MailAccount mailAccount, MailAccess<?, ?> mailAccess) throws OXException {
        if (null == mailAccount) {
            return null;
        }
        String fn = mailAccount.getArchiveFullname();
        if (null == fn) {
            String name = mailAccount.getArchive();
            if (null == name) {
                return null;
            }
            fn = mailAccess.getFolderStorage().getDefaultFolderPrefix() + name;
        }
        return fn;
    }

    /**
     * Checks if given full name denotes a default folder.
     *
     * @param folderFullName The full name to check
     * @return A default folder type if given full name denotes a default folder; otherwise <code>DefaultFolderType.NONE</code>
     * @throws OXException If check for default folder fails
     */
    public MailFolder.DefaultFolderType getDefaultFolderType(String folderFullName) throws OXException {
        if (folderFullName.equalsIgnoreCase(INBOX)) {
            return DefaultFolderType.INBOX;
        }
        for (int index = 0; (index < 6); index++) {
            if (folderFullName.equalsIgnoreCase(getDefaultFolder(index))) {
                switch (index) {
                case StorageUtility.INDEX_CONFIRMED_HAM:
                    return DefaultFolderType.CONFIRMED_HAM;
                case StorageUtility.INDEX_CONFIRMED_SPAM:
                    return DefaultFolderType.CONFIRMED_SPAM;
                case StorageUtility.INDEX_DRAFTS:
                    return DefaultFolderType.DRAFTS;
                case StorageUtility.INDEX_SENT:
                    return DefaultFolderType.SENT;
                case StorageUtility.INDEX_SPAM:
                    return DefaultFolderType.SPAM;
                case StorageUtility.INDEX_TRASH:
                    return DefaultFolderType.TRASH;
                default:
                    break;
                }
            }
        }
        return DefaultFolderType.NONE;
    }

    /**
     * Gets the default folder for specified index.
     *
     * @param index The default folder index taken from class <code>StorageUtility</code>
     * @return The default folder for specified index
     * @throws OXException If default folder retrieval fails
     */
    public String getDefaultFolder(int index) throws OXException {
        MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        String key = MailSessionParameterNames.getParamDefaultFolderChecked();
        if (!isDefaultFoldersChecked(key, mailSessionCache)) {
            checkDefaultFolders();
        }
        if (StorageUtility.INDEX_INBOX == index) {
            return INBOX;
        }

        // Get default folder array
        String[] arr = mailSessionCache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
        String retval = arr == null ? null : arr[index];
        if (retval != null) {
            return retval;
        }

        // Check for confirmed_spam / confirmed-ham
        if (null != arr) {
            if (StorageUtility.INDEX_CONFIRMED_HAM == index) {
                SpamHandler spamHandler = SpamHandlerRegistry.getSpamHandlerBySession(session, accountId);
                if (!spamHandler.isCreateConfirmedHam()) {
                    return retval;
                }
            } else if (StorageUtility.INDEX_CONFIRMED_SPAM == index) {
                SpamHandler spamHandler = SpamHandlerRegistry.getSpamHandlerBySession(session, accountId);
                if (!spamHandler.isCreateConfirmedSpam()) {
                    return retval;
                }
            }
        }

        // Continue (re-)checking default folders
        setDefaultFoldersChecked(key, false, mailSessionCache);
        checkDefaultFolders();
        return getDefaultMailFolder(index, mailSessionCache);
    }

    private String getDefaultMailFolder(int index, MailSessionCache mailSessionCache) {
        String[] arr = mailSessionCache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
        return arr == null ? null : arr[index];
    }

    /**
     * Checks default folders.
     *
     * @throws OXException If default folder check fails
     */
    public void checkDefaultFolders() throws OXException {
        MailSessionCache cache = MailSessionCache.getInstance(session);
        String key = MailSessionParameterNames.getParamDefaultFolderChecked();
        if (!isDefaultFoldersChecked(key, cache)) {

            /*-
             * ----------------------------------------------------------- NOTE -----------------------------------------------------------
             *
             * In case there are switching standard folder; e.g. change from next to INBOX to below INBOX.
             * Please check for following prominent WARN logging:
             *   "NAMESPACE from IMAP server <imap-host> indicates to use root level for login <imap-login>, but IMAP server denies to create such folders!"
             *
             * This hints to a failed probe whether IMAP server allows to create folders on root level; see IMAPCommandsCollection.canCreateSubfolder()
             *
             * ----------------------------------------------------------------------------------------------------------------------------
             */

            Lock lock = getSessionLock();
            lock.lock();
            try {
                BoolReference accountChanged = new BoolReference(false);
                if (!isDefaultFoldersChecked(key, cache)) {
                    try {
                        /*
                         * Get INBOX folder
                         */
                        ListLsubEntry inboxListEntry;
                        IMAPFolder inboxFolder;
                        {
                            IMAPFolder tmp = (IMAPFolder) imapStore.getFolder(INBOX);

                            /*
                             * Bug #41825: Changed the handling of special-use folders.
                             *
                             * Now special-use folder used only when creating a new mail account to
                             * initial fill the default folder fields.
                             *
                             * After that the values within the database are used instead. Therefore
                             * changes to the database now influence the default folders.
                             */
                            boolean reinitSpecialUseIfLoaded = false;
                            ListLsubEntry entry = ListLsubCache.getCachedLISTEntry(INBOX, accountId, tmp, session, ignoreSubscription, reinitSpecialUseIfLoaded);

                            if (entry.exists()) {
                                inboxFolder = tmp;
                            } else {
                                /*
                                 * Strange... No INBOX available. Try to create it.
                                 */
                                char sep = IMAPCommandsCollection.getSeparator(tmp);
                                try {
                                    IMAPCommandsCollection.createFolder(tmp, sep, FOLDER_TYPE);
                                } catch (MessagingException e) {
                                    IMAPCommandsCollection.createFolder(tmp, sep, Folder.HOLDS_MESSAGES);
                                }
                                ListLsubCache.addSingle(INBOX, accountId, tmp, session, ignoreSubscription);
                                inboxFolder = (IMAPFolder) imapStore.getFolder(INBOX);
                                entry = ListLsubCache.getCachedLISTEntry(INBOX, accountId, inboxFolder, session, ignoreSubscription);
                            }
                            inboxListEntry = entry;
                        }
                        if (!inboxListEntry.isSubscribed()) {
                            /*
                             * Subscribe INBOX folder
                             */
                            inboxFolder.setSubscribed(true);
                            ListLsubCache.addSingle(INBOX, accountId, inboxFolder, session, ignoreSubscription);
                            inboxListEntry = ListLsubCache.getCachedLISTEntry(INBOX, accountId, inboxFolder, session, ignoreSubscription);
                        }
                        char sep = inboxListEntry.getSeparator();
                        /*
                         * Get prefix for default folder names, NOT full names!
                         */
                        String namespace = imapAccess.getFolderStorage().getDefaultFolderPrefix();
                        /*
                         * Check for mbox
                         */
                        boolean mboxEnabled = MBoxEnabledCache.isMBoxEnabled(imapConfig, inboxFolder, namespace);
                        int type = mboxEnabled ? Folder.HOLDS_MESSAGES : FOLDER_TYPE;
                        sequentiallyCheckFolders(namespace, sep, type, accountChanged, cache);
                        /*
                         * Remember default folders
                         */
                        setDefaultFoldersChecked(key, true, cache);
                    } catch (MessagingException e) {
                        throw MimeMailException.handleMessagingException(e, imapConfig, session);
                    }
                }
                /*
                 * Mail account data changed?
                 */
                if (accountChanged.getValue()) {
                    final Logger logger = LOG;
                    ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                        @Override
                        public Void call() throws Exception {
                            try {
                                FolderService folderService = Services.getServiceLookup().getOptionalService(FolderService.class);
                                if (null != folderService) {
                                    // Reinitialize the EAS favorite folder tree
                                    folderService.reinitialize("20", session);
                                }
                            } catch (Exception x) {
                                logger.warn("Failed to propagate changed mail default folders", x);
                            }
                            return null;
                        }

                    }, CallerRunsBehavior.<Void> getInstance());
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private static ListLsubEntry getByNameOrFullName(boolean isPrimary, int index, String[] names, String[] fullNames, Collection<ListLsubEntry> entries) {
        return isPrimary ? getByName(names[index], entries) : getByFullName(fullNames[index], entries);
    }

    private static ListLsubEntry getByName(String name, Collection<ListLsubEntry> entries) {
        if (entries.isEmpty()) {
            return null;
        }
        if (null == name) {
            // First one
            return entries.iterator().next();
        }
        for (ListLsubEntry entry : entries) {
            if (name.equals(entry.getName())) {
                return entry;
            }
        }
        // First one as none matches
        return entries.iterator().next();
    }

    private static ListLsubEntry getByFullName(String fullName, Collection<ListLsubEntry> entries) {
        if (entries.isEmpty()) {
            return null;
        }
        if (null == fullName) {
            // First one
            return entries.iterator().next();
        }
        for (ListLsubEntry entry : entries) {
            if (fullName.equals(entry.getFullName())) {
                return entry;
            }
        }
        // First one as none matches
        return entries.iterator().next();
    }

    /**
     * Handles the marked entries and looks-up appropriate standard folder for designated type.
     *
     * @param entries The (optional) marked entries
     * @param index The index
     * @param names The expected names
     * @param fullNames The expected full names
     * @param checkedIndexes The checked indexes so far
     * @param cache The mail session cache
     * @param modified The <i>modified</i> boolean
     * @return <code>Boolean.TRUE</code> if successfully handled, <code>Boolean.FALSE</code> if such an entry does not exist, or <code>null</code> if not handled at all
     * @throws OXException If operation fails
     */
    protected Boolean handleMarkedEntries(Collection<ListLsubEntry> entries, int index, String[] names, String[] fullNames, TIntObjectMap<String> checkedIndexes, MailSessionCache cache, BoolReference modified) throws OXException {
        if (null != entries) {
            int size = entries.size();
            if (size > 0) {
                // Determine the SPECIAL-USE entry to use
                ListLsubEntry entry = size == 1 ? entries.iterator().next() : getByNameOrFullName(MailAccount.DEFAULT_ID == accountId, index, names, fullNames, entries);

                // Check entry
                ListLsubEntry cached = ListLsubCache.getCachedLISTEntry(entry.getFullName(), accountId, imapStore, session, ignoreSubscription);
                if (!cached.exists()) {
                    LOG.warn("{} SPECIAL-USE marked folder \"{}\" does not exist. Skipping... (user={}, context={})", SPECIAL_USES[index], entry.getFullName(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                    return Boolean.FALSE;
                }

                // Set as default folder
                setDefaultMailFolder(index, entry.getFullName(), cache);
                checkedIndexes.put(index, entry.getFullName());
                if (!entry.isSubscribed()) {
                    IMAPCommandsCollection.forceSetSubscribed(imapStore, entry.getFullName(), true);
                    modified.setValue(true);
                }
                return Boolean.TRUE;
            }
        }
        return null;
    }

    /**
     * Gets the SPECIAL-USE information for the connected IMAP store.
     *
     * @param names The expected standard folder names
     * @param fullNames The expected standard folder full names
     * @return The SPECIAL-USE information
     * @throws OXException If SPECIAL-USE information cannot be returned
     */
    protected TIntObjectMap<String> getSpecialUseInfo(String[] names, String[] fullNames) throws OXException {
        try {
            TIntObjectMap<String> indexes = new TIntObjectHashMap<>(6);
            IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(INBOX);
            boolean isPrimary = MailAccount.DEFAULT_ID == accountId;

            // Entries with "\Drafts" marker
            Collection<ListLsubEntry> entries = ListLsubCache.getDraftsEntry(accountId, imapFolder, session, ignoreSubscription);
            int size = entries.size();
            if (size > 0) {
                int index = StorageUtility.INDEX_DRAFTS;
                ListLsubEntry entry = size == 1 ? entries.iterator().next() : getByNameOrFullName(isPrimary, index, names, fullNames, entries);
                if (null != entry) {
                    indexes.put(index, entry.getFullName());
                }
            }

            // Entries with "\Junk" marker
            entries = ListLsubCache.getJunkEntry(accountId, imapFolder, session, ignoreSubscription);
            size = entries.size();
            if (size > 0) {
                int index = StorageUtility.INDEX_SPAM;
                ListLsubEntry entry = size == 1 ? entries.iterator().next() : getByNameOrFullName(isPrimary, index, names, fullNames, entries);
                if (null != entry) {
                    indexes.put(index, entry.getFullName());
                }
            }

            // Entries with "\Send" marker
            entries = ListLsubCache.getSentEntry(accountId, imapFolder, session, ignoreSubscription);
            size = entries.size();
            if (size > 0) {
                int index = StorageUtility.INDEX_SENT;
                ListLsubEntry entry = size == 1 ? entries.iterator().next() : getByNameOrFullName(isPrimary, index, names, fullNames, entries);
                if (null != entry) {
                    indexes.put(index, entry.getFullName());
                }
            }

            // Entries with "\Trash" marker
            entries = ListLsubCache.getTrashEntry(accountId, imapFolder, session, ignoreSubscription);
            size = entries.size();
            if (size > 0) {
                int index = StorageUtility.INDEX_TRASH;
                ListLsubEntry entry = size == 1 ? entries.iterator().next() : getByNameOrFullName(isPrimary, index, names, fullNames, entries);
                if (null != entry) {
                    indexes.put(index, entry.getFullName());
                }
            }

            return indexes;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    /**
     * Checks for each standard folder sequentially.
     *
     * @param namespace The user's personal namespace as indicated by <code>NAMESPACE</code> command or detected manually
     * @param sep The mailbox' separator character
     * @param type The applicable folder type
     * @param accountChanged The boolean reference to signal whether mail account has been changed
     * @param cache The mail session cache
     * @throws OXException If check fails
     */
    protected void sequentiallyCheckFolders(String namespace, char sep, int type, BoolReference accountChanged, MailSessionCache cache) throws OXException {
        // The flag to track possible modifications
        BoolReference modified = new BoolReference(false);

        // Detect if spam option is enabled
        boolean isSpamOptionEnabled;
        {
            UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
            isSpamOptionEnabled = usm.isSpamOptionEnabled();
        }

        // Get default folders names and full names
        DefaultFolderNamesProvider defaultFolderNamesProvider = new DefaultFolderNamesProvider(accountId, session.getUserId(), session.getContextId());
        String[] fullNames = defaultFolderNamesProvider.getDefaultFolderFullnames(imapConfig, isSpamOptionEnabled);
        String[] names = Arrays.copyOfRange(imapConfig.getStandardNames(), 0, isSpamOptionEnabled ? 6 : 4);
        UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContextId());
        String[] defaultNames = defaultFolderNamesProvider.getDefaultFolderNames(usm.getStdTrashName(), usm.getStdSentName(), usm.getStdDraftsName(), usm.getStdSpamName(), usm.getConfirmedSpam(), usm.getConfirmedHam(), isSpamOptionEnabled);
        SpamHandler spamHandler = isSpamOptionEnabled ? SpamHandlerRegistry.getSpamHandlerBySession(session, accountId) : NoSpamHandler.getInstance();

        // Collect SPECIAL-USE information
        TIntObjectMap<String> specialUseInfo = getSpecialUseInfo(names, fullNames);
        TIntObjectMap<String> indexes = null;

        // Check if it is the first connect attempt for primary mail account
        if (MailAccount.DEFAULT_ID == accountId) {
            boolean checkSpecialUseFolder = false;
            ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
            if (viewFactory != null) {
                ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
                ComposedConfigProperty<Boolean> prop = view.property("com.openexchange.imap.initWithSpecialUse", Boolean.class);
                if (prop.isDefined()) {
                    Boolean b = prop.get();
                    checkSpecialUseFolder = null != b && b.booleanValue();
                }
            }

            if (checkSpecialUseFolder) {
                // Check for marked default folders on first connect attempt for primary mail account
                indexes = specialUseInfo;
            }
        }

        // Sanitize given names and full-names against mail account settings
        sanitizeAgainstMailAccount(names, fullNames, defaultNames, namespace, sep, indexes, accountChanged);

        // Check folders
        TIntObjectMap<String> toSet = (MailAccount.DEFAULT_ID == accountId) ? null : new TIntObjectHashMap<String>(6);
        boolean added = false;
        for (int index = 0; index < names.length; index++) {
            String checkedFullName = null;

            // Determine the checked full name
            {
                // Get desired name and full name --> full name dominates name
                String name = names[index];
                String fullName = fullNames[index];
                LOG.debug("Standard folder check for {} with name={} and fullName={} for account {} (user={}, context={})", getFallbackName(index), null == name ? "null" : name, null == fullName ? "null" : fullName, Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));

                // Check folder & return its full name
                if (StorageUtility.INDEX_CONFIRMED_HAM == index) {
                    if (spamHandler.isCreateConfirmedHam()) {
                        checkedFullName = checkFullNameFor(index, namespace, specialUseInfo, fullName, name, sep, type, spamHandler.isUnsubscribeSpamFolders() ? 0 : -1, modified, accountChanged);
                    } else {
                        LOG.debug("Skipping check for {} due to SpamHandler.isCreateConfirmedHam()=false", name);
                    }
                } else if (StorageUtility.INDEX_CONFIRMED_SPAM == index) {
                    if (spamHandler.isCreateConfirmedSpam()) {
                        checkedFullName = checkFullNameFor(index, namespace, specialUseInfo, fullName, name, sep, type, spamHandler.isUnsubscribeSpamFolders() ? 0 : -1, modified, accountChanged);
                    } else {
                        LOG.debug("Skipping check for {} due to SpamHandler.isCreateConfirmedSpam()=false", name);
                    }
                } else {
                    checkedFullName = checkFullNameFor(index, namespace, specialUseInfo, fullName, name, sep, type, 1, modified, accountChanged);
                }

                // Check against account data
                if ((null != toSet) && (null != checkedFullName) && (false == isEmpty(fullName)) && !checkedFullName.equals(fullName)) {
                    toSet.put(index, checkedFullName);
                    added = true;
                }
            }

            // Set the checked full name
            setDefaultMailFolder(index, checkedFullName, cache);
        }

        // Update account data if necessary
        if (added && (null != toSet)) {
            MailAccount modifiedAccount = setAccountFullNames(toSet);
            accountChanged.setValue(true);
            if (null != modifiedAccount) {
                imapConfig.applyStandardNames(modifiedAccount, true);
            }
        }

        // Check for modifications
        if (modified.getValue()) {
            ListLsubCache.clearCache(accountId, session);
        }
    }

    /**
     * Sanitizes given names and full-names against mail account settings.
     *
     * @param names The names for standard folders
     * @param fullNames The full-names for standard folders
     * @param namespace The user's namespace; e.g. <code>"INBOX/"</code>
     * @param sep The separator character; e.g. <code>'/'</code>
     * @param checkedIndexes The checked indexes according to SPECIAL-USE flags advertised by IMAP server (if any)
     * @param accountChanged The boolean reference to signal whether mail account has been changed
     */
    protected void sanitizeAgainstMailAccount(String[] names, String[] fullNames, String[] defaultNames, String namespace, char sep, TIntObjectMap<String> checkedIndexes, BoolReference accountChanged) {
        // Special handling for full names in case of primary mail account
        if (MailAccount.DEFAULT_ID == accountId) {
            /*-
             * Check full names for primary account:
             *
             * Null'ify full name if not on root level OR not equal to name; meaning not intended to create default folders next to INBOX
             * In that case create them with respect to determined prefix
             */
            TIntObjectMap<String> namesToSet = new TIntObjectHashMap<>(6);
            TIntList indexes = new TIntLinkedList();
            for (int i = 0; i < fullNames.length; i++) {
                String fullName = fullNames[i];
                if (isEmpty(fullName)) {
                    if (!isEmpty(names[i])) {
                        continue;
                    }
                    // No full name given
                    if (null != checkedIndexes) {
                        String expectedFullName = checkedIndexes.get(i);
                        if (null != expectedFullName) {
                            // Deduce expected name from SPECIAL-USE full name
                            String expectedName = expectedFullName.substring(expectedFullName.lastIndexOf(sep) + 1);
                            if (!expectedName.equals(names[i])) {
                                LOG.warn("Replacing invalid name in settings of primary account. Should be \"{}\", but is \"{}\" (user={}, context={})", expectedName, names[i], Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                                names[i] = expectedName;
                                namesToSet.put(i, expectedName);
                            }
                        } else {
                            if (Strings.isEmpty(names[i])) {
                                names[i] = defaultNames[i];
                                namesToSet.put(i, names[i]);
                            }
                        }
                    } else {
                        if (Strings.isEmpty(names[i])) {
                            names[i] = defaultNames[i];
                            namesToSet.put(i, names[i]);
                        }
                    }
                    fullNames[i] = null;
                } else {
                    // Full name specified
                    if (fullName.indexOf(sep) > 0 || !fullName.equals(names[i])) {
                        // E.g. name=Sent, but fullName=INBOX/Sent or fullName=Zent

                        String expectedFullName = null;
                        if (null != checkedIndexes) {
                            expectedFullName = checkedIndexes.get(i);
                        }
                        if (null == expectedFullName) {
                            // Deduce expected full-name from namespace + name concatenation
                            expectedFullName = namespace + names[i];
                            if (!expectedFullName.equals(fullName)) {
                                LOG.warn("Clearing invalid full name in settings of primary account. Should be \"{}\", but is \"{}\" (user={}, context={})", expectedFullName, fullName, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                                indexes.add(i);
                            }
                        } else {
                            // Check against actual full-name
                            if (!expectedFullName.equals(fullName)) {
                                LOG.warn("Clearing invalid full name in settings of primary account. Should be \"{}\", but is \"{}\" (user={}, context={})", expectedFullName, fullName, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                                indexes.add(i);
                            }

                            // Deduce expected name from SPECIAL-USE full name
                            String expectedName = expectedFullName.substring(expectedFullName.lastIndexOf(sep) + 1);
                            if (!expectedName.equals(names[i])) {
                                LOG.warn("Replacing invalid name in settings of primary account. Should be \"{}\", but is \"{}\" (user={}, context={})", expectedName, names[i], Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                                names[i] = expectedName;
                                namesToSet.put(i, expectedName);
                            }
                        }
                        fullNames[i] = null;
                    }
                }
            }
            if (!indexes.isEmpty()) {
                MailAccount modifiedAccount = clearAccountFullNames(indexes.toArray());
                accountChanged.setValue(true);
                if (null != modifiedAccount) {
                    imapConfig.applyStandardNames(modifiedAccount, true);
                }
            }
            if (!namesToSet.isEmpty()) {
                MailAccount modifiedAccount = setAccountNames(namesToSet);
                accountChanged.setValue(true);
                if (null != modifiedAccount) {
                    imapConfig.applyStandardNames(modifiedAccount, true);
                }
            }
        } else {
            if (null != checkedIndexes && !checkedIndexes.isEmpty()) {
                TIntObjectMap<String> fullNamesToSet = new TIntObjectHashMap<>(6);
                TIntObjectMap<String> namesToSet = new TIntObjectHashMap<>(6);
                for (int i = 0; i < fullNames.length; i++) {
                    String expectedFullName = checkedIndexes.get(i);
                    if (null != expectedFullName) {
                        String fullName = fullNames[i];
                        if (isEmpty(fullName)) {
                            fullNamesToSet.put(i, expectedFullName);
                            // Check name, too
                            String expectedName = expectedFullName.substring(expectedFullName.lastIndexOf(sep) + 1);
                            if (!expectedName.equals(names[i])) {
                                names[i] = expectedName;
                                namesToSet.put(i, expectedFullName);
                            }
                        } else if (!expectedFullName.equals(fullName)) {
                            fullNames[i] = null;
                            fullNamesToSet.put(i, expectedFullName);
                            // Check name, too
                            String expectedName = expectedFullName.substring(expectedFullName.lastIndexOf(sep) + 1);
                            if (!expectedName.equals(names[i])) {
                                names[i] = expectedName;
                                namesToSet.put(i, expectedFullName);
                            }
                        }
                    }
                }
                if (!fullNamesToSet.isEmpty()) {
                    MailAccount modifiedAccount = setAccountFullNames(fullNamesToSet);
                    accountChanged.setValue(true);
                    if (null != modifiedAccount) {
                        imapConfig.applyStandardNames(modifiedAccount, true);
                    }
                }
                if (!namesToSet.isEmpty()) {
                    MailAccount modifiedAccount = setAccountNames(namesToSet);
                    accountChanged.setValue(true);
                    if (null != modifiedAccount) {
                        imapConfig.applyStandardNames(modifiedAccount, true);
                    }
                }
            }
            LOG.debug("Checking standard folder for account {} (user={}, context={})", Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
    }

    /**
     * Performs default folder check for specified arguments.
     *
     * @param index The index
     * @param namespace The personal namespace prefix
     * @param specialUseInfo The SPECIAL-USE information
     * @param fullName The full name
     * @param name The name
     * @param sep The separator character
     * @param type The folder type
     * @param subscribe Whether to subscribe
     * @param modified Whether folders has been modified during check
     * @param accountChanged The boolean reference to signal whether mail account has been changed
     * @return The checked full name
     * @throws OXException If an error occurs
     */
    protected String checkFullNameFor(int index, String namespace, TIntObjectMap<String> specialUseInfo, String fullName, String name, char sep, int type, int subscribe, BoolReference modified, BoolReference accountChanged) throws OXException {
        boolean isFullname = false == isEmpty(fullName);
        try {
            if (isFullname) {
                // Check by specified desired full name
                return doCheckFullNameFor(index, "", fullName, sep, type, subscribe, namespace, specialUseInfo, modified, accountChanged);
            }
            // Check by specified desired name
            if (isEmpty(name)) {
                // Neither full name nor name
                return doCheckFullNameFor(index, namespace, getFallbackName(index), sep, type, subscribe, namespace, specialUseInfo, modified, accountChanged);
            }
            return doCheckFullNameFor(index, namespace, name, sep, type, subscribe, namespace, specialUseInfo, modified, accountChanged);
        } catch (OXException e) {
            LOG.warn("Couldn't check default folder: {}. Namespace prefix: \"{}\"", (null == fullName ? (namespace + name) : fullName), (null == namespace ? "null" : namespace), e);
            e.setCategory(Category.CATEGORY_WARNING);
            imapAccess.addWarnings(Collections.singleton(e));
        } catch (FolderClosedException e) {
            /*
             * Not possible to retry since connection is broken
             */
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        } catch (StoreClosedException e) {
            /*
             * Not possible to retry since connection is broken
             */
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        } catch (AlreadyLoggedMessagingException alreadyLogged) {
            handleMessagingErrorOnCheckFullName(alreadyLogged.messagingException, namespace, fullName, name, false);
        } catch (MessagingException e) {
            handleMessagingErrorOnCheckFullName(e, namespace, fullName, name, true);
        }
        return null;
    }

    private void handleMessagingErrorOnCheckFullName(MessagingException e, String namespace, String fullName, String name, boolean logWarning) throws OXException {
        if (isOverQuotaException(e)) {
            /*
             * Special handling for over-quota error
             */
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        }
        if (logWarning) {
            LOG.warn("Couldn't check default folder: {}. Namespace prefix: \"{}\"", (null == fullName ? (namespace + name) : fullName), (null == namespace ? "null" : namespace), e);
        }
        OXException warning = MimeMailException.handleMessagingException(e, imapConfig, session).setCategory(Category.CATEGORY_WARNING);
        imapAccess.addWarnings(Collections.singleton(warning));
    }

    /**
     * Signals whether an attempt should be performed to set an appropriate SPECIAL-USE flag for an existing folder or not.
     *
     * @return <code>true</code> to set appropriate SPECIAL-USE flag; otherwise <code>false</code>
     */
    protected boolean setSpecialUseForExisting() {
        return false;
    }

    /**
     * Performs the actual folder check
     *
     * @param index The index
     * @param prefix The prefix
     * @param qualifiedName The qualified name
     * @param sep The separator character
     * @param type The folder type
     * @param subscribe The subscribed flag
     * @param namespace The personal namespace prefix
     * @param specialUseInfo The SPECIAL-USE information
     * @param modified Signals modified status
     * @param accountChanged The boolean reference to signal whether mail account has been changed
     * @return The checked full name
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If an error occurs
     */
    protected String doCheckFullNameFor(int index, String prefix, String qualifiedName, char sep, int type, int subscribe, String namespace, TIntObjectMap<String> specialUseInfo, BoolReference modified, BoolReference accountChanged) throws MessagingException, OXException {
        /*
         * Check default folder
         */
        int prefixLen = prefix.length();
        String desiredFullName = prefixLen == 0 ? qualifiedName : new StringBuilder(prefix).append(qualifiedName).toString();
        {
            ListLsubEntry entry = modified.getValue() ? ListLsubCache.getActualLISTEntry(desiredFullName, accountId, imapStore, session, ignoreSubscription) : ListLsubCache.getCachedLISTEntry(desiredFullName, accountId, imapStore, session, ignoreSubscription);
            if (null != entry && entry.exists()) {
                // The easy one -- already existing; just check subscription status
                boolean checkSpecialUseForExisting = false;
                if (1 == subscribe) {
                    if (!entry.isSubscribed()) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, desiredFullName, true);
                        modified.setValue(true);
                    }
                    checkSpecialUseForExisting = true;
                } else if (0 == subscribe) {
                    if (entry.isSubscribed()) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, desiredFullName, false);
                        modified.setValue(true);
                    }
                    checkSpecialUseForExisting = true;
                }
                if (checkSpecialUseForExisting && hasMetadata && index <= StorageUtility.INDEX_TRASH && setSpecialUseForExisting()) {
                    String specialUseFullName = specialUseInfo.get(index);
                    if (false == desiredFullName.equals(specialUseFullName)) {
                        // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                        String flag = SPECIAL_USES[index];
                        try {
                            IMAPCommandsCollection.setSpecialUses((IMAPFolder) imapStore.getFolder(desiredFullName), Collections.singletonList(flag));
                            modified.setValue(true);
                        } catch (Exception e) {
                            LOG.debug("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), desiredFullName, namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                        }
                    }
                }
                return desiredFullName;
            }
        }
        // No such folder -- Probably need to create it if no appropriate candidate can be found
        IMAPFolder f = (IMAPFolder) imapStore.getFolder(desiredFullName);
        boolean checkSpecialUse = true;
        if (isEmpty(namespace)) {
            if (desiredFullName.indexOf(sep) > 0) {
                // Standard folder is NOT supposed to be created within personal namespace
                IMAPFolder probableCandidate = lookupFolder(getNamespaceFolder(namespace, sep), f.getName());
                if (null != probableCandidate) {
                    checkSubscriptionStatus(subscribe, probableCandidate, checkSpecialUse, namespace, index, modified);
                    String fullName = probableCandidate.getFullName();
                    LOG.info("Standard {} folder set to \"{}\" for account {} (user={}, context={})", getFallbackName(index), fullName, Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                    return fullName;
                }
                LOG.warn("Standard {} folder \"{}\" is NOT supposed to be created within personal namespace \"\" for account {} (user={}, context={})", getFallbackName(index), desiredFullName, Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            }
        } else {
            if (!isFullNameLocatedInNamespace(desiredFullName, namespace, sep)) {
                // Standard folder is NOT supposed to be created within personal namespace
                IMAPFolder probableCandidate = lookupFolder(getNamespaceFolder(namespace, sep), f.getName());
                if (null != probableCandidate) {
                    checkSubscriptionStatus(subscribe, probableCandidate, checkSpecialUse, namespace, index, modified);
                    String fullName = probableCandidate.getFullName();
                    LOG.info("Standard {} folder set to \"{}\" for account {} (user={}, context={})", getFallbackName(index), fullName, Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                    return fullName;
                }
                LOG.warn("Standard {} folder \"{}\" is NOT supposed to be created within personal namespace \"{}\" for account {} (user={}, context={})", getFallbackName(index), desiredFullName, namespace, Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            }
        }
        // Go ahead
        if (!f.exists()) {
            // Check against siblings
            IMAPFolder parent = (IMAPFolder) f.getParent();
            List<Folder> candidates = new ArrayList<>(2);
            {
                Folder[] folders = parent.list();
                String mName = f.getName();
                for (int i = 0; i < folders.length; i++) {
                    Folder child = folders[i];
                    if (mName.equalsIgnoreCase(child.getName())) {
                        // Detected a similarly named folder
                        candidates.add(child);
                    }
                }
            }
            int nCandidates = candidates.size();
            if (nCandidates <= 0 || nCandidates > 1) {
                // Zero or more than one candidate found. Try to create IMAP folder
                if (nCandidates > 1) {
                    LOG.warn("Detected multiple existing IMAP folders with name equal ignore-case to \"{}\" for account {} (user={}, context={})", f.getName(), Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                }
                try {
                    createIfNonExisting(f, type, sep, namespace, index);
                    checkSpecialUse = false;
                    modified.setValue(true);
                } catch (MessagingException e) {
                    MessagingException toAnalyze = (e instanceof AlreadyLoggedMessagingException) ? ((AlreadyLoggedMessagingException) e).messagingException : e;
                    if (isOverQuotaException(toAnalyze)) {
                        throw e;
                    }
                    if (isAlreadyExistsException(toAnalyze)) {
                        // Grab the first in sight
                        closeSafe(f);
                        if (!candidates.isEmpty()) {
                            f = (IMAPFolder) candidates.get(0);
                            desiredFullName = f.getFullName();
                        }
                        checkSubscriptionStatus(subscribe, f, true, namespace, index, modified);
                        return desiredFullName;
                    }
                    // Check for possibly wrong namespace
                    if (!Strings.isEmpty(namespace) && !isFullNameLocatedInNamespace(desiredFullName, namespace, sep)) {
                        int sepPos = desiredFullName.lastIndexOf(sep);
                        String name = sepPos > 0 ? desiredFullName.substring(sepPos + 1) : desiredFullName;
                        String checkedFullName = doCheckFullNameFor(index, "", namespace + name, sep, type, subscribe, namespace, specialUseInfo, modified, accountChanged);
                        clearAllAccountFullNames();
                        accountChanged.setValue(true);
                        return checkedFullName;
                    }
                    // Failed for any reason
                    throw e;
                }
            } else {
                // Found one candidate
                closeSafe(f);
                f = (IMAPFolder) candidates.get(0);
                desiredFullName = f.getFullName();
            }
        }
        checkSubscriptionStatus(subscribe, f,  checkSpecialUse, namespace, index, modified);
        return desiredFullName;
    }

    /**
     * Checks if given full name indicates to be located in specified namespace.
     * <p>
     * <table>
     *  <tr><td align="center">&bull;</td><td align="right"><code>"INBOX/"</code></td><td>--&gt; <code>"INBOX/Trash"</code>, but not <code>"Trash"</code> and not <code>"INBOX/foobar/Trash"</code></td></tr>
     *  <tr><td align="center">&bull;</td><td align="right"><code>""</code></td><td>--&gt; <code>"Trash"</code>, but not <code>"INBOX/Trash"</code></td></tr>
     * </table>
     *
     * @param fullName The full name to check
     * @param namespace The namespace location
     * @param sep The separator character
     * @return <code>true</code> if given full name is located in name-space; otherwise <code>false</code>
     */
    protected boolean isFullNameLocatedInNamespace(String fullName, String namespace, char sep) {
        if (isEmpty(namespace)) {
            return fullName.indexOf(sep) < 0;
        }
        return (fullName.startsWith(namespace) && (fullName.indexOf(sep, namespace.length()) < 0));
    }

    /**
     * Checks the subscription status (and the SPECIAL-USE flag in case <code>checkSpecialUse</code> is <code>true</code>).
     *
     * @param subscribe The desired subscription status
     * @param folder The folder to check
     * @param checkSpecialUse Whether to check for SPECIAL-USE flag
     * @param namespace The detected prefix according to <code>NAMESPACE</code> command
     * @param index The index
     * @param modified The modified flag
     */
    protected void checkSubscriptionStatus(int subscribe, IMAPFolder folder, boolean checkSpecialUse, String namespace, int index, BoolReference modified) {
        if (1 == subscribe) {
            if (!folder.isSubscribed()) {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, folder.getFullName(), true);
                modified.setValue(true);
            }
            if (checkSpecialUse && hasMetadata && index <= StorageUtility.INDEX_TRASH) {
                // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                String flag = SPECIAL_USES[index];
                try {
                    IMAPCommandsCollection.setSpecialUses(folder, Collections.singletonList(flag));
                    modified.setValue(true);
                } catch (Exception e) {
                    LOG.debug("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), folder.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                }
            }
        } else if (0 == subscribe) {
            if (folder.isSubscribed()) {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, folder.getFullName(), false);
                modified.setValue(true);
            }
            if (checkSpecialUse && hasMetadata && index <= StorageUtility.INDEX_TRASH) {
                // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                String flag = SPECIAL_USES[index];
                try {
                    IMAPCommandsCollection.setSpecialUses(folder, Collections.singletonList(flag));
                    modified.setValue(true);
                } catch (Exception e) {
                    LOG.debug("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), folder.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                }
            }
        }
    }

    /**
     * Creates the folder in case non-existing.
     *
     * @param f The IMAP folder
     * @param type The folder type
     * @param sep The separator character
     * @param namespace The detected prefix according to <code>NAMESPACE</code> command
     * @param index The index
     * @throws MessagingException If create attempt fails
     */
    protected void createIfNonExisting(IMAPFolder f, int type, char sep, String namespace, int index) throws MessagingException {
        if (!f.exists()) {
            try {
                IMAPCommandsCollection.createFolder(f, sep, type, false);
                LOG.info("Created new standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));

                if (hasMetadata && index <= StorageUtility.INDEX_TRASH) {
                    // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                    String flag = SPECIAL_USES[index];
                    try {
                        IMAPCommandsCollection.setSpecialUses(f, Collections.singletonList(flag));
                    } catch (Exception e) {
                        LOG.debug("Failed to set {} flag for new standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                    }
                }
            } catch (MessagingException e) {
                String msg = e.getMessage();
                if (null == msg || Strings.toUpperCase(msg).indexOf("[ALREADYEXISTS]") < 0) {
                    LOG.warn("Failed to create new standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                    throw new AlreadyLoggedMessagingException(e);
                }
                // Obviously such a folder does already exist; treat as being successfully created
                LOG.info("Standard {} folder does already exist (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            }
        } else {
            if (hasMetadata && index <= StorageUtility.INDEX_TRASH) {
                // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                String flag = SPECIAL_USES[index];
                try {
                    IMAPCommandsCollection.setSpecialUses(f, Collections.singletonList(flag));
                } catch (Exception e) {
                    LOG.debug("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                }
            }
        }
    }

    /**
     * Performs a look-up by given name in subfolders of specified parent folder.
     *
     * @param parent The parent folder
     * @param name The name to look for
     * @return The matching subfolder or <code>null</code>
     * @throws MessagingException If look-up fails
     */
    protected IMAPFolder lookupFolder(IMAPFolder parent, String name) throws MessagingException {
        for (Folder subfolder : parent.list()) {
            if (name.equalsIgnoreCase(subfolder.getName())) {
                return (IMAPFolder) subfolder;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------- //

    /**
     * Gets the namespace folder.
     *
     * @param namespace The namespace path
     * @param sep The separator character
     * @return The namespace folder
     * @throws MessagingException If namespace folder cannot be returned
     */
    protected IMAPFolder getNamespaceFolder(String namespace, char sep) throws MessagingException {
        if (isEmpty(namespace)) {
            return (IMAPFolder) imapStore.getDefaultFolder();
        }
        int lastIndex = namespace.length() - 1;
        return (IMAPFolder) imapStore.getFolder(sep == namespace.charAt(lastIndex) ? namespace.substring(0, lastIndex) : namespace);
    }

    /**
     * Clears all the full names in settings of associated account
     */
    protected void clearAllAccountFullNames() {
        clearAccountFullNames(null);
    }

    /**
     * Clears the specified full names in settings of associated account
     *
     * @return The modified mail account or <code>null</code> if service is absent
     */
    protected MailAccount clearAccountFullNames(int[] indexes) {
        // Invalidate mail account settings as obviously wrong
        MailAccountStorageService mass = Services.optService(MailAccountStorageService.class);
        if (null != mass) {
            try {
                if (null == indexes) {
                    mass.clearFullNamesForMailAccount(accountId, session.getUserId(), session.getContextId());
                } else {
                    mass.clearFullNamesForMailAccount(accountId, indexes, session.getUserId(), session.getContextId());
                }

                return mass.getRawMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (Exception x) {
                LOG.warn("Failed to clear full names for mail account {}", Integer.valueOf(accountId), x);
            }
        }
        return null;
    }

    /**
     * Sets the specified full names in settings of associated account
     *
     * @return The modified mail account or <code>null</code> if service is absent
     */
    protected MailAccount setAccountFullNames(TIntObjectMap<String> map) {
        // Invalidate mail account settings as obviously wrong
        MailAccountStorageService mass = Services.optService(MailAccountStorageService.class);
        if (null != mass) {
            try {
                TIntList indexes = new TIntArrayList(map.size());
                List<String> fullNames = new ArrayList<>(map.size());
                for (int index : map.keys()) {
                    indexes.add(index);
                    fullNames.add(map.get(index));
                }

                mass.setFullNamesForMailAccount(accountId, indexes.toArray(), fullNames.toArray(new String[fullNames.size()]), session.getUserId(), session.getContextId());
                return mass.getRawMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (Exception x) {
                LOG.warn("Failed to set full names for mail account {}", Integer.valueOf(accountId), x);
            }
        }
        return null;
    }

    /**
     * Sets the specified names in settings of associated account
     *
     * @return The modified mail account or <code>null</code> if service is absent
     */
    protected MailAccount setAccountNames(TIntObjectMap<String> map) {
        // Invalidate mail account settings as obviously wrong
        MailAccountStorageService mass = Services.optService(MailAccountStorageService.class);
        if (null != mass) {
            try {
                TIntList indexes = new TIntArrayList(map.size());
                List<String> names = new ArrayList<>(map.size());
                for (int index : map.keys()) {
                    indexes.add(index);
                    names.add(map.get(index));
                }

                mass.setNamesForMailAccount(accountId, indexes.toArray(), names.toArray(new String[names.size()]), session.getUserId(), session.getContextId());
                return mass.getRawMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (Exception x) {
                LOG.warn("Failed to set full names for mail account {}", Integer.valueOf(accountId), x);
            }
        }
        return null;
    }

    /**
     * Gets the value of <code>"mail.deffldflag"</code> cache entry.
     *
     * @param key The <code>"mail.deffldflag"</code> string
     * @param mailSessionCache The session cache
     * @return The value
     */
    protected boolean isDefaultFoldersChecked(String key, MailSessionCache mailSessionCache) {
        Boolean b = mailSessionCache.getParameter(accountId, key);
        return (b != null) && b.booleanValue();
    }

    /**
     * Sets the value of <code>"mail.deffldflag"</code> cache entry.
     *
     * @param key The <code>"mail.deffldflag"</code> string
     * @param checked The value to set
     * @param mailSessionCache The session cache
     */
    protected void setDefaultFoldersChecked(String key, boolean checked, MailSessionCache mailSessionCache) {
        mailSessionCache.putParameter(accountId, key, Boolean.valueOf(checked));
    }

    /**
     * Sets the standard folder full name for given index in cache.
     *
     * @param index The index of the standard folder; e.g. <code>StorageUtility.INDEX_TRASH</code>
     * @param fullName The full name to set or <code>null</code>
     * @param cache The cache reference
     */
    protected void setDefaultMailFolder(int index, String fullName, MailSessionCache cache) {
        String key = MailSessionParameterNames.getParamDefaultFolderArray();
        String[] arr = cache.getParameter(accountId, key);
        if (null == arr) {
            synchronized (this) {
                arr = cache.getParameter(accountId, key);
                if (null == arr) {
                    arr = new String[6];
                    cache.putParameter(accountId, key, arr);
                }
            }
        }
        arr[index] = fullName;
    }

    /**
     * Checks for possible over-quota error.
     */
    protected static boolean isOverQuotaException(MessagingException e) {
        return MimeMailException.isOverQuotaException(e);
    }

    /**
     * Checks for possible already-exists error.
     */
    protected static boolean isAlreadyExistsException(MessagingException e) {
        return MimeMailException.isAlreadyExistsException(e);
    }

    /** Gets fall-back name */
    protected static String getFallbackName(int index) {
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
     * Gets the lock associated with this checker's session.
     *
     * @return The lock
     */
    protected Lock getSessionLock() {
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        return null == lock ? Session.EMPTY_LOCK : lock;
    }

    private static void closeSafe(Folder folder) {
        if (null != folder) {
            try {
                folder.close(false);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------

    /**
     * Wraps an already logged {@link MessagingException} instance.
     */
    protected static class AlreadyLoggedMessagingException extends MessagingException {

        private static final long serialVersionUID = 1973917919275287767L;

        /** The associated {@link MessagingException} instance. */
        protected final MessagingException messagingException;

        /**
         * Initializes a new {@link AlreadyLoggedMessagingException}.
         *
         * @param messagingException The already logged messaging exception instance.
         */
        protected AlreadyLoggedMessagingException(MessagingException messagingException) {
            super(messagingException.getMessage(), messagingException);
            this.messagingException = messagingException;
        }
    }

}
