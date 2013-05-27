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

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.NoSpamHandler;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link SpecialUseDefaultFolderChecker} - The IMAP default folder checker.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SpecialUseDefaultFolderChecker extends IMAPDefaultFolderChecker {

    static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.loggerFor(SpecialUseDefaultFolderChecker.class);

    static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link SpecialUseDefaultFolderChecker}.
     *
     * @param accountId The account ID
     * @param session The session
     * @param ctx The context
     * @param imapStore The (connected) IMAP store
     * @param imapConfig The IMAP configuration
     */
    public SpecialUseDefaultFolderChecker(final int accountId, final Session session, final Context ctx, final AccessedIMAPStore imapStore, final IMAPConfig imapConfig) {
        super(accountId, session, ctx, imapStore, imapConfig);
    }

    @Override
    protected void sequentiallyCheckFolders(final String prefix, final char sep, final int type, final MailAccountStorageService storageService, final MailSessionCache mailSessionCache) throws OXException {
        final TIntSet indexes = new TIntHashSet(new int[] { StorageUtility.INDEX_DRAFTS, StorageUtility.INDEX_SENT, StorageUtility.INDEX_SPAM, StorageUtility.INDEX_TRASH });
        try {
            final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(INBOX);
            ListLsubEntry entry = ListLsubCache.getDraftsEntry(accountId, imapFolder, session);
            if (null != entry) {
                setDefaultMailFolder(StorageUtility.INDEX_DRAFTS, entry.getFullName(), mailSessionCache);
                indexes.remove(StorageUtility.INDEX_DRAFTS);
            }
            entry = ListLsubCache.getJunkEntry(accountId, imapFolder, session);
            if (null != entry) {
                setDefaultMailFolder(StorageUtility.INDEX_SPAM, entry.getFullName(), mailSessionCache);
                indexes.remove(StorageUtility.INDEX_SPAM);
            }
            entry = ListLsubCache.getSentEntry(accountId, imapFolder, session);
            if (null != entry) {
                setDefaultMailFolder(StorageUtility.INDEX_SENT, entry.getFullName(), mailSessionCache);
                indexes.remove(StorageUtility.INDEX_SENT);
            }
            entry = ListLsubCache.getTrashEntry(accountId, imapFolder, session);
            if (null != entry) {
                setDefaultMailFolder(StorageUtility.INDEX_TRASH, entry.getFullName(), mailSessionCache);
                indexes.remove(StorageUtility.INDEX_TRASH);
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        }
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
                if (indexes.contains(index)) {
                    performTaskFor(index, prefix, fullName, names[index], sep, type, 1, modified, mailSessionCache);
                }
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

    private static final String[] SPECIAL_USES;
    static {
        final String[] sa = new String[4];
        sa[StorageUtility.INDEX_DRAFTS] = "\\Drafts";
        sa[StorageUtility.INDEX_SENT] = "\\Sent";
        sa[StorageUtility.INDEX_SPAM] = "\\Junk";
        sa[StorageUtility.INDEX_TRASH] = "\\Trash";
        SPECIAL_USES = sa;
    }

    /**
     * Internally used by {@link SpecialUseDefaultFolderChecker}.
     */
    @Override
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
                        final List<String> specialUses = index < StorageUtility.INDEX_CONFIRMED_SPAM ? Collections.singletonList(SPECIAL_USES[index]) : null;
                        IMAPCommandsCollection.createFolder(f, sep, type, false, specialUses);
                    } else {
                        if (index < StorageUtility.INDEX_CONFIRMED_SPAM) {
                            IMAPCommandsCollection.setSpecialUses(f, Collections.singletonList(SPECIAL_USES[index]));
                        }
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
                    throw e;
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
                        final List<String> specialUses = index < StorageUtility.INDEX_CONFIRMED_SPAM ? Collections.singletonList(SPECIAL_USES[index]) : null;
                        IMAPCommandsCollection.createFolder(f, sep, type, false, specialUses);
                    } else {
                        if (index < StorageUtility.INDEX_CONFIRMED_SPAM) {
                            IMAPCommandsCollection.setSpecialUses(f, Collections.singletonList(SPECIAL_USES[index]));
                        }
                    }
                    modified.set(true);
                } catch (final MessagingException e) {
                    if (isOverQuotaException(e)) {
                        throw e;
                    }
                    throw e;
                }
            } else {
                if (MailAccount.DEFAULT_ID == accountId) {
                    // Must not edit default mail account. Try to create IMAP folder
                    try {
                        if (!f.exists()) {
                            final List<String> specialUses = index < StorageUtility.INDEX_CONFIRMED_SPAM ? Collections.singletonList(SPECIAL_USES[index]) : null;
                            IMAPCommandsCollection.createFolder(f, sep, type, false, specialUses);
                        } else {
                            if (index < StorageUtility.INDEX_CONFIRMED_SPAM) {
                                IMAPCommandsCollection.setSpecialUses(f, Collections.singletonList(SPECIAL_USES[index]));
                            }
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
                        final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);

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

}
