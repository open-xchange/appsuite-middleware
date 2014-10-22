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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.NoSpamHandler;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link SpecialUseDefaultFolderChecker} - The IMAP default folder checker.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SpecialUseDefaultFolderChecker extends IMAPDefaultFolderChecker {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SpecialUseDefaultFolderChecker.class);

    private static final String[] SPECIAL_USES;
    static {
        final String[] sa = new String[4];
        sa[StorageUtility.INDEX_DRAFTS] = "\\Drafts";
        sa[StorageUtility.INDEX_SENT] = "\\Sent";
        sa[StorageUtility.INDEX_SPAM] = "\\Junk";
        sa[StorageUtility.INDEX_TRASH] = "\\Trash";
        SPECIAL_USES = sa;
    }

    // -------------------------------------------------------------------------------------------------------------------------------- //

    private final boolean hasCreateSpecialUse;
    private final boolean hasMetadata;

    /**
     * Initializes a new {@link SpecialUseDefaultFolderChecker}.
     *
     * @param accountId The account ID
     * @param session The session
     * @param ctx The context
     * @param imapStore The (connected) IMAP store
     * @param imapAccess The IMAP access
     * @param hasCreateSpecialUse Whether the IMAP server advertises "CREATE-SPECIAL-USE" capability string
     * @param hasMetadata Whether the IMAP server advertises "METADATA" capability string
     */
    public SpecialUseDefaultFolderChecker(final int accountId, final Session session, final Context ctx, final IMAPStore imapStore, final IMAPAccess imapAccess, boolean hasCreateSpecialUse, boolean hasMetadata) {
        super(accountId, session, ctx, imapStore, imapAccess);
        this.hasCreateSpecialUse = hasCreateSpecialUse;
        this.hasMetadata = hasMetadata;
    }

    @Override
    protected void sequentiallyCheckFolders(final String prefix, final char sep, final int type, final MailSessionCache cache) throws OXException {
        final AtomicBoolean modified = new AtomicBoolean(false);
        final TIntSet indexes = new TIntHashSet(new int[] { StorageUtility.INDEX_DRAFTS, StorageUtility.INDEX_SENT, StorageUtility.INDEX_SPAM, StorageUtility.INDEX_TRASH });
        try {
            final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(INBOX);
            ListLsubEntry entry = ListLsubCache.getDraftsEntry(accountId, imapFolder, session);
            if (null != entry) {
                setDefaultMailFolder(StorageUtility.INDEX_DRAFTS, entry.getFullName(), cache);
                indexes.remove(StorageUtility.INDEX_DRAFTS);
                if (!entry.isSubscribed()) {
                    IMAPCommandsCollection.forceSetSubscribed(imapStore, entry.getFullName(), true);
                    modified.set(true);
                }
            }
            entry = ListLsubCache.getJunkEntry(accountId, imapFolder, session);
            if (null != entry) {
                setDefaultMailFolder(StorageUtility.INDEX_SPAM, entry.getFullName(), cache);
                indexes.remove(StorageUtility.INDEX_SPAM);
                if (!entry.isSubscribed()) {
                    IMAPCommandsCollection.forceSetSubscribed(imapStore, entry.getFullName(), true);
                    modified.set(true);
                }
            }
            entry = ListLsubCache.getSentEntry(accountId, imapFolder, session);
            if (null != entry) {
                setDefaultMailFolder(StorageUtility.INDEX_SENT, entry.getFullName(), cache);
                indexes.remove(StorageUtility.INDEX_SENT);
                if (!entry.isSubscribed()) {
                    IMAPCommandsCollection.forceSetSubscribed(imapStore, entry.getFullName(), true);
                    modified.set(true);
                }
            }
            entry = ListLsubCache.getTrashEntry(accountId, imapFolder, session);
            if (null != entry) {
                setDefaultMailFolder(StorageUtility.INDEX_TRASH, entry.getFullName(), cache);
                indexes.remove(StorageUtility.INDEX_TRASH);
                if (!entry.isSubscribed()) {
                    IMAPCommandsCollection.forceSetSubscribed(imapStore, entry.getFullName(), true);
                    modified.set(true);
                }
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        }
        // Detect if spam option is enabled
        final boolean isSpamOptionEnabled;
        {
            final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
            isSpamOptionEnabled = usm.isSpamOptionEnabled();
        }
        // Get default folders names and full names
        final String[] fullNames;
        final String[] names;
        final SpamHandler spamHandler;
        {
            final DefaultFolderNamesProvider defaultFolderNamesProvider = new DefaultFolderNamesProvider(accountId, session.getUserId(), session.getContextId());
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
        // Special handling for full names in case of primary mail account
        if (MailAccount.DEFAULT_ID == accountId) {
            /*-
             * Check full names for primary account:
             *
             * Null'ify full name if not on root level OR not equal to name; meaning not intended to create default folders next to INBOX
             * In that case create them with respect to determined prefix
             */
            for (int i = 0; i < fullNames.length; i++) {
                final String fullName = fullNames[i];
                if (isEmpty(fullName) || fullName.indexOf(sep) > 0 || !fullName.equals(names[i])) {
                    // ^^^
                    // E.g. name=Sent, but fullName=INBOX/Sent or fullName=Zent
                    fullNames[i] = null;
                }
            }
        }
        // Check folders
        for (int index = 0; index < names.length; index++) {
            String checkedFullName = null;
            boolean apply = true;

            // Determine the checked full name
            {
                // Get desired name and full name --> full name dominates name
                final String name = names[index];
                final String fullName = fullNames[index];

                // Check folder & return its full name
                if (StorageUtility.INDEX_CONFIRMED_HAM == index) {
                    if (spamHandler.isCreateConfirmedHam()) {
                        checkedFullName = checkFullNameFor(index, prefix, fullName, name, sep, type, spamHandler.isUnsubscribeSpamFolders() ? 0 : -1, modified);
                    } else {
                        LOG.debug("Skipping check for {} due to SpamHandler.isCreateConfirmedHam()=false", name);
                    }
                } else if (StorageUtility.INDEX_CONFIRMED_SPAM == index) {
                    if (spamHandler.isCreateConfirmedSpam()) {
                        checkedFullName = checkFullNameFor(index, prefix, fullName, name, sep, type, spamHandler.isUnsubscribeSpamFolders() ? 0 : -1, modified);
                    } else {
                        LOG.debug("Skipping check for {} due to SpamHandler.isCreateConfirmedSpam()=false", name);
                    }
                } else {
                    if (indexes.contains(index)) {
                        checkedFullName = checkFullNameFor(index, prefix, fullName, name, sep, type, 1, modified);
                    } else {
                        // Already applied above
                        apply = false;
                    }
                }
            }

            // Set the checked full name (if applicable)
            if (apply) {
                setDefaultMailFolder(index, checkedFullName, cache);
            }
        }
        /*
         * Check for modifications
         */
        if (modified.get()) {
            ListLsubCache.clearCache(accountId, session);
        }
    }

    @Override
    protected void createIfNonExisting(final IMAPFolder f, final int type, final char sep, final String detectedPrefix, final int index) throws MessagingException {
        if (!f.exists()) {
            try {
                if (hasCreateSpecialUse) {
                    // E.g. CREATE MyDrafts (USE (\Drafts))
                    final List<String> specialUses = index < StorageUtility.INDEX_CONFIRMED_SPAM ? Collections.singletonList(SPECIAL_USES[index]) : null;
                    IMAPCommandsCollection.createFolder(f, sep, type, false, specialUses);
                } else {
                    IMAPCommandsCollection.createFolder(f, sep, type, false);
                    if (index < StorageUtility.INDEX_CONFIRMED_SPAM) {
                        if (hasMetadata) {
                            // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                            IMAPCommandsCollection.setSpecialUses(f, Collections.singletonList(SPECIAL_USES[index]));
                        }
                    }
                }
                LOG.info("Created new standard {} folder (full-name={}, namespace={}) for login {} (account={}) on IMAP server {} (user={}, context={})", getFallbackName(index), f.getFullName(), detectedPrefix, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
            } catch (final MessagingException e) {
                LOG.warn("Failed to create new standard {} folder (full-name={}, namespace={}) for login {} (account={}) on IMAP server {} (user={}, context={})", getFallbackName(index), f.getFullName(), detectedPrefix, imapConfig.getLogin(), Integer.valueOf(accountId), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                throw e;
            }
        } else {
            if (index < StorageUtility.INDEX_CONFIRMED_SPAM) {
                if (hasMetadata) {
                    // E.g. SETMETADATA "SavedDrafts" (/private/specialuse "\\Drafts")
                    IMAPCommandsCollection.setSpecialUses(f, Collections.singletonList(SPECIAL_USES[index]));
                }
            }
        }
    }

}
