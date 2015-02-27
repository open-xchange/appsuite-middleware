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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import org.slf4j.Logger;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.MBoxEnabledCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.services.Services;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
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
    }

    /**
     * Checks if given full name denotes a default folder.
     *
     * @param folderFullName The full name to check
     * @return <code>true</code> if given full name denotes a default folder; otherwise <code>false</code>
     * @throws OXException If check for default folder fails
     */
    public boolean isDefaultFolder(String folderFullName) throws OXException {
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
            Lock lock = getSessionLock();
            lock.lock();
            try {
                if (!isDefaultFoldersChecked(key, cache)) {
                    try {
                        /*
                         * Get INBOX folder
                         */
                        ListLsubEntry inboxListEntry;
                        IMAPFolder inboxFolder;
                        {
                            IMAPFolder tmp = (IMAPFolder) imapStore.getFolder(INBOX);
                            ListLsubEntry entry = ListLsubCache.getCachedLISTEntry(INBOX, accountId, tmp, session);
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
                        char sep = inboxFolder.getSeparator();
                        /*
                         * Get prefix for default folder names, NOT full names!
                         */
                        String namespace = imapAccess.getFolderStorage().getDefaultFolderPrefix();
                        /*
                         * Check for mbox
                         */
                        boolean mboxEnabled = MBoxEnabledCache.isMBoxEnabled(imapConfig, inboxFolder, namespace);
                        int type = mboxEnabled ? Folder.HOLDS_MESSAGES : FOLDER_TYPE;
                        sequentiallyCheckFolders(namespace, sep, type, cache);
                        /*
                         * Remember default folders
                         */
                        setDefaultFoldersChecked(key, true, cache);
                    } catch (MessagingException e) {
                        throw MimeMailException.handleMessagingException(e, imapConfig, session);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private static ListLsubEntry getByName(String name, Collection<ListLsubEntry> entries) {
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
     */
    protected void handleMarkedEntries(Collection<ListLsubEntry> entries, int index, String[] names, String[] fullNames, TIntObjectMap<String> checkedIndexes, MailSessionCache cache, AtomicBoolean modified) {
        if (null != entries && !entries.isEmpty()) {
            ListLsubEntry entry;
            if (entries.size() == 1) {
                entry = entries.iterator().next();
            } else {
                entry = (MailAccount.DEFAULT_ID == accountId ? getByName(names[index], entries) : getByFullName(fullNames[index], entries));
            }
            setDefaultMailFolder(index, entry.getFullName(), cache);
            checkedIndexes.put(index, entry.getFullName());
            if (!entry.isSubscribed()) {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, entry.getFullName(), true);
                modified.set(true);
            }
        }
    }

    /**
     * Checks for each standard folder sequentially.
     *
     * @param namespace The user's personal namespace as indicated by <code>NAMESPACE</code> command or detected manually
     * @param sep The mailbox' separator character
     * @param type The applicable folder type
     * @param cache The mail session cache
     * @throws OXException If check fails
     */
    protected void sequentiallyCheckFolders(String namespace, char sep, int type, MailSessionCache cache) throws OXException {
        // The flag to track possible modifications
        AtomicBoolean modified = new AtomicBoolean(false);

        // Detect if spam option is enabled
        boolean isSpamOptionEnabled;
        {
            UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
            isSpamOptionEnabled = usm.isSpamOptionEnabled();
        }

        // Get default folders names and full names
        DefaultFolderNamesProvider defaultFolderNamesProvider = new DefaultFolderNamesProvider(accountId, session.getUserId(), session.getContextId());
        String[] fullNames = defaultFolderNamesProvider.getDefaultFolderFullnames(imapConfig, isSpamOptionEnabled);
        String[] names = defaultFolderNamesProvider.getDefaultFolderNames(imapConfig, isSpamOptionEnabled);
        SpamHandler spamHandler = isSpamOptionEnabled ? SpamHandlerRegistry.getSpamHandlerBySession(session, accountId) : NoSpamHandler.getInstance();

        // Check for marked default folders
        TIntObjectMap<String> checkedIndexes = new TIntObjectHashMap<String>(6);
        try {
            IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(INBOX);

            // Entries with "\Drafts" marker
            Collection<ListLsubEntry> entries = ListLsubCache.getDraftsEntry(accountId, imapFolder, session);
            handleMarkedEntries(entries, StorageUtility.INDEX_DRAFTS, names, fullNames, checkedIndexes, cache, modified);

            // Entries with "\Junk" marker
            entries = ListLsubCache.getJunkEntry(accountId, imapFolder, session);
            handleMarkedEntries(entries, StorageUtility.INDEX_SPAM, names, fullNames, checkedIndexes, cache, modified);

            // Entries with "\Send" marker
            entries = ListLsubCache.getSentEntry(accountId, imapFolder, session);
            handleMarkedEntries(entries, StorageUtility.INDEX_SENT, names, fullNames, checkedIndexes, cache, modified);

            // Entries with "\Trash" marker
            entries = ListLsubCache.getTrashEntry(accountId, imapFolder, session);
            handleMarkedEntries(entries, StorageUtility.INDEX_TRASH, names, fullNames, checkedIndexes, cache, modified);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        }

        // Special handling for full names in case of primary mail account
        if (MailAccount.DEFAULT_ID == accountId) {
            /*-
             * Check full names for primary account:
             *
             * Null'ify full name if not on root level OR not equal to name; meaning not intended to create default folders next to INBOX
             * In that case create them with respect to determined prefix
             */
            TIntList indexes = new TIntLinkedList();
            for (int i = 0; i < fullNames.length; i++) {
                String fullName = fullNames[i];
                if (isEmpty(fullName)) {
                    fullNames[i] = null;
                } else {
                    if (fullName.indexOf(sep) > 0 || !fullName.equals(names[i])) {
                        // E.g. name=Sent, but fullName=INBOX/Sent or fullName=Zent
                        String expectedFullName = checkedIndexes.get(i);
                        if (null == expectedFullName) {
                            expectedFullName = namespace + names[i];
                        }
                        if (!expectedFullName.equals(fullName)) {
                            LOG.warn("Found invalid full name in settings of account {}. Should be \"{}\", but is \"{}\" (user={}, context={})", Integer.valueOf(accountId), expectedFullName, fullName, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                            indexes.add(i);
                        }
                        fullNames[i] = null;
                    }
                }
            }
            if (!indexes.isEmpty()) {
                MailAccount modifiedAccount = clearAccountFullNames(indexes.toArray());
                if (null != modifiedAccount) {
                    imapConfig.applyStandardNames(modifiedAccount);
                }
            }
        } else {
            if (!checkedIndexes.isEmpty()) {
                TIntObjectMap<String> toSet = new TIntObjectHashMap<String>(6);
                boolean added = false;
                for (int i = 0; i < fullNames.length; i++) {
                    String expectedFullName = checkedIndexes.get(i);
                    if (null != expectedFullName) {
                        String fullName = fullNames[i];
                        if (isEmpty(fullName)) {
                            toSet.put(i, expectedFullName);
                            added = true;
                        } else if (!expectedFullName.equals(fullName)) {
                            fullNames[i] = null;
                            toSet.put(i, expectedFullName);
                            added = true;
                        }
                    }
                }
                if (added) {
                    MailAccount modifiedAccount = setAccountFullNames(toSet);
                    if (null != modifiedAccount) {
                        imapConfig.applyStandardNames(modifiedAccount);
                    }
                }
            }
            LOG.debug("Checking standard folder for account {} (user={}, context={})", Integer.valueOf(accountId), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }

        // Check folders
        for (int index = 0; index < names.length; index++) {
            if (!checkedIndexes.containsKey(index)) {
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
                            checkedFullName = checkFullNameFor(index, namespace, fullName, name, sep, type, spamHandler.isUnsubscribeSpamFolders() ? 0 : -1, modified);
                        } else {
                            LOG.debug("Skipping check for {} due to SpamHandler.isCreateConfirmedHam()=false", name);
                        }
                    } else if (StorageUtility.INDEX_CONFIRMED_SPAM == index) {
                        if (spamHandler.isCreateConfirmedSpam()) {
                            checkedFullName = checkFullNameFor(index, namespace, fullName, name, sep, type, spamHandler.isUnsubscribeSpamFolders() ? 0 : -1, modified);
                        } else {
                            LOG.debug("Skipping check for {} due to SpamHandler.isCreateConfirmedSpam()=false", name);
                        }
                    } else {
                        checkedFullName = checkFullNameFor(index, namespace, fullName, name, sep, type, 1, modified);
                    }
                }

                // Set the checked full name
                setDefaultMailFolder(index, checkedFullName, cache);
            }
        }

        // Check for modifications
        if (modified.get()) {
            ListLsubCache.clearCache(accountId, session);
        }
    }

    /**
     * Performs default folder check for specified arguments.
     *
     * @param index The index
     * @param namespace The personal namespace prefix
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
    protected String checkFullNameFor(int index, String namespace, String fullName, String name, char sep, int type, int subscribe, AtomicBoolean modified) throws OXException {
        boolean isFullname = false == isEmpty(fullName);
        try {
            if (isFullname) {
                // Check by specified desired full name
                return doCheckFullNameFor(index, "", fullName, sep, type, subscribe, namespace, modified);
            }
            // Check by specified desired name
            if (isEmpty(name)) {
                // Neither full name nor name
                return doCheckFullNameFor(index, namespace, getFallbackName(index), sep, type, subscribe, namespace, modified);
            }
            return doCheckFullNameFor(index, namespace, name, sep, type, subscribe, namespace, modified);
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
        } catch (MessagingException e) {
            if (isOverQuotaException(e)) {
                /*
                 * Special handling for over-quota error
                 */
                throw MimeMailException.handleMessagingException(e, imapConfig, session);
            }
            LOG.warn("Couldn't check default folder: {}. Namespace prefix: \"{}\"", (null == fullName ? (namespace + name) : fullName), (null == namespace ? "null" : namespace), e);
            OXException warning = MimeMailException.handleMessagingException(e, imapConfig, session).setCategory(Category.CATEGORY_WARNING);
            imapAccess.addWarnings(Collections.singleton(warning));
        }
        return null;
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
     * @param modified Signals modified status
     * @return The checked full name
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If an error occurs
     */
    protected String doCheckFullNameFor(int index, String prefix, String qualifiedName, char sep, int type, int subscribe, String namespace, AtomicBoolean modified) throws MessagingException, OXException {
        /*
         * Check default folder
         */
        int prefixLen = prefix.length();
        String desiredFullName = prefixLen == 0 ? qualifiedName : new StringBuilder(prefix).append(qualifiedName).toString();
        {
            ListLsubEntry entry = modified.get() ? ListLsubCache.getActualLISTEntry(desiredFullName, accountId, imapStore, session) : ListLsubCache.getCachedLISTEntry(desiredFullName, accountId, imapStore, session);
            if (null != entry && entry.exists()) {
                // The easy one -- already existing; just check subscription status
                if (1 == subscribe) {
                    if (!entry.isSubscribed()) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, desiredFullName, true);
                        modified.set(true);
                    }
                    if (hasMetadata && index <= StorageUtility.INDEX_TRASH) {
                        // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                        String flag = SPECIAL_USES[index];
                        try {
                            IMAPCommandsCollection.setSpecialUses((IMAPFolder) imapStore.getFolder(desiredFullName), Collections.singletonList(flag));
                            modified.set(true);
                        } catch (Exception e) {
                            LOG.info("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), desiredFullName, namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                        }
                    }
                } else if (0 == subscribe) {
                    if (entry.isSubscribed()) {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, desiredFullName, false);
                        modified.set(true);
                    }
                    if (hasMetadata && index <= StorageUtility.INDEX_TRASH) {
                        // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                        String flag = SPECIAL_USES[index];
                        try {
                            IMAPCommandsCollection.setSpecialUses((IMAPFolder) imapStore.getFolder(desiredFullName), Collections.singletonList(flag));
                            modified.set(true);
                        } catch (Exception e) {
                            LOG.info("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), desiredFullName, namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
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
            List<Folder> candidates = new ArrayList<Folder>(2);
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
                    modified.set(true);
                } catch (MessagingException e) {
                    if (isOverQuotaException(e)) {
                        throw e;
                    }
                    if (isAlreadyExistsException(e)) {
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
                        String checkedFullName = doCheckFullNameFor(index, "", namespace + name, sep, type, subscribe, namespace, modified);
                        clearAllAccountFullNames();
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
    protected void checkSubscriptionStatus(int subscribe, IMAPFolder folder, boolean checkSpecialUse, String namespace, int index, AtomicBoolean modified) {
        if (1 == subscribe) {
            if (!folder.isSubscribed()) {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, folder.getFullName(), true);
                modified.set(true);
            }
            if (checkSpecialUse && hasMetadata && index <= StorageUtility.INDEX_TRASH) {
                // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                String flag = SPECIAL_USES[index];
                try {
                    IMAPCommandsCollection.setSpecialUses(folder, Collections.singletonList(flag));
                    modified.set(true);
                } catch (Exception e) {
                    LOG.info("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), folder.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                }
            }
        } else if (0 == subscribe) {
            if (folder.isSubscribed()) {
                IMAPCommandsCollection.forceSetSubscribed(imapStore, folder.getFullName(), false);
                modified.set(true);
            }
            if (checkSpecialUse && hasMetadata && index <= StorageUtility.INDEX_TRASH) {
                // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                String flag = SPECIAL_USES[index];
                try {
                    IMAPCommandsCollection.setSpecialUses(folder, Collections.singletonList(flag));
                    modified.set(true);
                } catch (Exception e) {
                    LOG.info("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), folder.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
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
                        LOG.info("Failed to set {} flag for new standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                    }
                }
            } catch (MessagingException e) {
                String msg = e.getMessage();
                if (null == msg || Strings.toUpperCase(msg).indexOf("[ALREADYEXISTS]") < 0) {
                    LOG.warn("Failed to create new standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                    throw e;
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
                    LOG.info("Failed to set {} flag for existing standard {} folder (full-name=\"{}\", namespace=\"{}\") for login {} (account={}) on IMAP server {} (user={}, context={})", flag, getFallbackName(index), f.getFullName(), namespace, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
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
                return mass.getMailAccount(accountId, session.getUserId(), session.getContextId());
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
                List<String> fullNames = new ArrayList<String>(map.size());

                for (int index : map.keys()) {
                    indexes.add(index);
                    fullNames.add(map.get(index));
                }

                mass.setFullNamesForMailAccount(accountId, indexes.toArray(), fullNames.toArray(new String[fullNames.size()]), session.getUserId(), session.getContextId());
                return mass.getMailAccount(accountId, session.getUserId(), session.getContextId());
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

}
