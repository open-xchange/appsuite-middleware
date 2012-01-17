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

package com.openexchange.unifiedinbox;

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.unifiedinbox.converters.UnifiedINBOXFolderConverter;
import com.openexchange.unifiedinbox.services.UnifiedINBOXServiceRegistry;
import com.openexchange.unifiedinbox.utility.LoggingCallable;
import com.openexchange.unifiedinbox.utility.TrackingCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedINBOXFolderStorage} - The Unified INBOX folder storage implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXFolderStorage extends MailFolderStorage {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(UnifiedINBOXFolderStorage.class));

    private final UnifiedINBOXAccess access;

    private final Session session;

    private final Context ctx;

    private Locale locale;

    /**
     * Initializes a new {@link UnifiedINBOXFolderStorage}
     *
     * @param access The Unified INBOX access
     * @param session The session providing needed user data
     * @throws OXException If context loading fails
     */
    public UnifiedINBOXFolderStorage(final UnifiedINBOXAccess access, final Session session) throws OXException {
        super();
        this.access = access;
        this.session = session;
        ctx = ContextStorage.getStorageContext(session.getContextId());
    }

    @Override
    public boolean exists(final String fullname) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return true;
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            return true;
        }
        final String fn = startsWithKnownFullname(fullname);
        if (null == fn) {
            return false;
        }
        final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(fn);
        if (!isMailAccountEnabled(fa.getAccountId())) {
            return false;
        }
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            mailAccess.connect();
            return mailAccess.getFolderStorage().exists(fa.getFullname());
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailFolder getFolder(final String fullname) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return UnifiedINBOXFolderConverter.getRootFolder();
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            return UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(access.getAccountId(), session, fullname, getLocalizedName(fullname));
        }
        final String fn = startsWithKnownFullname(fullname);
        if (null != fn) {
            final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(fn);
            final int nestedAccountId = fa.getAccountId();
            if (!isMailAccountEnabled(nestedAccountId)) {
                throw UnifiedINBOXException.Code.FOLDER_NOT_FOUND.create(fullname);
            }
            MailAccess<?, ?> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, nestedAccountId);
                mailAccess.connect();
                final String nestedFullname = fa.getFullname();
                final MailFolder mailFolder = mailAccess.getFolderStorage().getFolder(nestedFullname);
                mailFolder.setFullname(UnifiedINBOXUtility.generateNestedFullname(
                    access.getAccountId(),
                    getStartingKnownFullname(fullname),
                    nestedAccountId,
                    nestedFullname));
                mailFolder.setName(getMailAccountName(nestedAccountId));
                mailFolder.setSubfolders(false);
                mailFolder.setSubscribedSubfolders(false);
                mailFolder.setDefaultFolder(false);
                mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
                UnifiedINBOXFolderConverter.setPermissions(mailFolder);
                UnifiedINBOXFolderConverter.setOwnPermission(mailFolder, session.getUserId());
                return mailFolder;
            } finally {
                if (null != mailAccess) {
                    mailAccess.close(true);
                }
            }
        }
        throw UnifiedINBOXException.Code.FOLDER_NOT_FOUND.create(fullname);
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
            return getRootSubfolders(false);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(parentFullname)) {
            return getKnownFolderSubfolders(parentFullname);
        }
        final String fn = startsWithKnownFullname(parentFullname);
        if (null != fn) {
            final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(fn);
            final int nestedAccountId = fa.getAccountId();
            if (!isMailAccountEnabled(nestedAccountId)) {
                throw UnifiedINBOXException.Code.FOLDER_NOT_FOUND.create(parentFullname);
            }
            /*
             * Return empty array since mapped default folders have no subfolders in Unified INBOX account
             */
            return EMPTY_PATH;
        }
        throw UnifiedINBOXException.Code.FOLDER_NOT_FOUND.create(parentFullname);
    }

    private static final String[] FULLNAMES = {
        UnifiedINBOXAccess.INBOX, UnifiedINBOXAccess.DRAFTS, UnifiedINBOXAccess.SENT, UnifiedINBOXAccess.SPAM, UnifiedINBOXAccess.TRASH };

    private MailFolder[] getRootSubfolders(final boolean byAccount) throws OXException {
        if (byAccount) {
            return getRootSubfoldersByAccount();
        }
        return getRootSubfoldersByFolder();
    }

    private MailFolder[] getRootSubfoldersByAccount() throws OXException {
        // Determine accounts
        final int unifiedINBOXAccountId = access.getAccountId();
        final MailAccount[] accounts;
        {
            final MailAccountStorageService storageService =
                UnifiedINBOXServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
            final MailAccount[] arr = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
            final List<MailAccount> l = new ArrayList<MailAccount>(arr.length);
            for (final MailAccount mailAccount : arr) {
                if (unifiedINBOXAccountId != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                    l.add(mailAccount);
                }
            }
            accounts = l.toArray(new MailAccount[l.size()]);
        }
        final int nAccounts = accounts.length;
        final Executor executor = ThreadPools.getThreadPool().getExecutor();
        final TrackingCompletionService<int[][]> completionService = new UnifiedINBOXCompletionService<int[][]>(executor);
        // Create a task for each account
        for (int i = 0; i < nAccounts; i++) {
            final int accountId = accounts[i].getId();
            completionService.submit(new LoggingCallable<int[][]>(session, unifiedINBOXAccountId) {

                public int[][] call() throws Exception {
                    return UnifiedINBOXFolderConverter.getAccountDefaultFolders(accountId, session, FULLNAMES);
                }
            });
        }
        // Wait for completion
        final List<int[][]> list = new ArrayList<int[][]>(nAccounts);
        try {
            int completed = 0;
            while (completed < nAccounts) {
                // No timeout
                list.add(completionService.take().get());
                completed++;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder("Retrieving root's subfolders took ").append(completionService.getDuration()).append("msec."));
            }
            // Merge them
            final String[] names = new String[5];
            names[0] = getLocalizedName(UnifiedINBOXAccess.INBOX);
            names[1] = getLocalizedName(UnifiedINBOXAccess.DRAFTS);
            names[2] = getLocalizedName(UnifiedINBOXAccess.SENT);
            names[3] = getLocalizedName(UnifiedINBOXAccess.SPAM);
            names[4] = getLocalizedName(UnifiedINBOXAccess.TRASH);
            return UnifiedINBOXFolderConverter.mergeAccountDefaultFolders(list, FULLNAMES, names);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    private MailFolder[] getRootSubfoldersByFolder() throws OXException {
        final MailFolder[] retval = new MailFolder[5];
        final Executor executor = ThreadPools.getThreadPool().getExecutor();
        final TrackingCompletionService<Retval> completionService = new UnifiedINBOXCompletionService<Retval>(executor);
        // Init names
        final String[][] names = new String[5][];
        names[0] = new String[] { UnifiedINBOXAccess.INBOX, getLocalizedName(UnifiedINBOXAccess.INBOX) };
        names[1] = new String[] { UnifiedINBOXAccess.DRAFTS, getLocalizedName(UnifiedINBOXAccess.DRAFTS) };
        names[2] = new String[] { UnifiedINBOXAccess.SENT, getLocalizedName(UnifiedINBOXAccess.SENT) };
        names[3] = new String[] { UnifiedINBOXAccess.SPAM, getLocalizedName(UnifiedINBOXAccess.SPAM) };
        names[4] = new String[] { UnifiedINBOXAccess.TRASH, getLocalizedName(UnifiedINBOXAccess.TRASH) };
        // Create a Callable for each known subfolder
        for (int i = 0; i < retval.length; i++) {
            final int index = i;
            final String[] tmp = names[index];
            completionService.submit(new LoggingCallable<Retval>(session, access.getAccountId()) {

                public Retval call() throws Exception {
                    return new Retval(UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(
                        getAccountId(),
                        getSession(),
                        tmp[0],
                        tmp[1],
                        executor), index);
                }
            });
        }
        // Wait for completion of each submitted task
        try {
            int completed = 0;
            while (completed < retval.length) {
                // No timeout
                final Future<Retval> future = completionService.poll(UnifiedINBOXUtility.getMaxRunningMillis(), TimeUnit.MILLISECONDS);
                completed++;
                if (null != future) {
                    final Retval r = future.get();
                    if (null != r) {
                        retval[r.index] = r.mailFolder;
                    }
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder("Retrieving root's subfolders took ").append(completionService.getDuration()).append("msec."));
            }
            // Return them
            return retval;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    private MailFolder[] getKnownFolderSubfolders(final String parentFullname) throws OXException {
        final MailAccount[] accounts;
        {
            final MailAccountStorageService storageService =
                UnifiedINBOXServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
            final MailAccount[] tmp = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
            final List<MailAccount> l = new ArrayList<MailAccount>(tmp.length);
            for (final MailAccount mailAccount : tmp) {
                if (access.getAccountId() != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                    l.add(mailAccount);
                }
            }
            accounts = l.toArray(new MailAccount[l.size()]);
        }
        final Session s = session;
        final int unifiedInboxAccountId = access.getAccountId();
        final int length = accounts.length;
        final Executor executor = ThreadPools.getThreadPool().getExecutor();
        final TrackingCompletionService<MailFolder> completionService = new UnifiedINBOXCompletionService<MailFolder>(executor);
        for (final MailAccount mailAccount : accounts) {
            completionService.submit(new LoggingCallable<MailFolder>(session) {

                public MailFolder call() throws Exception {
                    MailAccess<?, ?> mailAccess = null;
                    try {
                        mailAccess = MailAccess.getInstance(getSession(), mailAccount.getId());
                        mailAccess.connect();
                        final String accountFullname = UnifiedINBOXUtility.determineAccountFullname(mailAccess, parentFullname);
                        // Check if account fullname is not null
                        if (null == accountFullname) {
                            return null;
                        }
                        // Get mail folder
                        final MailFolder mailFolder = mailAccess.getFolderStorage().getFolder(accountFullname);
                        mailFolder.setFullname(UnifiedINBOXUtility.generateNestedFullname(
                            unifiedInboxAccountId,
                            parentFullname,
                            mailAccount.getId(),
                            mailFolder.getFullname()));
                        UnifiedINBOXFolderConverter.setPermissions(mailFolder);
                        UnifiedINBOXFolderConverter.setOwnPermission(mailFolder, s.getUserId());
                        mailFolder.setSubfolders(false);
                        mailFolder.setSubscribedSubfolders(false);
                        mailFolder.setName(mailAccount.getName());
                        mailFolder.setDefaultFolder(false);
                        mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
                        return mailFolder;
                    } catch (final OXException e) {
                        getLogger().debug(e.getMessage(), e);
                        return null;
                    } finally {
                        if (null != mailAccess) {
                            mailAccess.close(true);
                        }
                    }
                }
            });
        }
        // Wait for completion of each submitted task
        try {
            final List<MailFolder> folders = new ArrayList<MailFolder>(length << 2);
            for (int i = 0; i < length; i++) {
                final MailFolder f = completionService.take().get();
                if (null != f) {
                    folders.add(f);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder("Retrieving subfolders of \"").append(parentFullname).append("\" took ").append(
                    completionService.getDuration()).append("msec."));
            }
            // Sort them
            Collections.sort(folders, new MailFolderNameComparator(getLocale()));
            // Return as array
            return folders.toArray(new MailFolder[folders.size()]);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return UnifiedINBOXFolderConverter.getRootFolder();
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        // Nothing to do
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        throw UnifiedINBOXException.Code.FOLDER_CREATION_FAILED.create();
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws OXException {
        throw UnifiedINBOXException.Code.MOVE_DENIED.create();
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws OXException {
        throw UnifiedINBOXException.Code.UPDATE_DENIED.create();
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws OXException {
        throw UnifiedINBOXException.Code.DELETE_DENIED.create();
    }

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws OXException {
        // Shall we support clear() ? ? ?
        throw UnifiedINBOXException.Code.CLEAR_NOT_SUPPORTED.create();
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return EMPTY_PATH;
        }
        if (!UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            throw UnifiedINBOXException.Code.FOLDER_NOT_FOUND.create(fullname);
        }
        return new MailFolder[] {
            UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(access.getAccountId(), session, fullname, getLocalizedName(fullname)),
            UnifiedINBOXFolderConverter.getRootFolder() };
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return UnifiedINBOXAccess.DRAFTS;
    }

    @Override
    public String getSentFolder() throws OXException {
        return UnifiedINBOXAccess.SENT;
    }

    @Override
    public String getSpamFolder() throws OXException {
        return UnifiedINBOXAccess.SPAM;
    }

    @Override
    public String getTrashFolder() throws OXException {
        return UnifiedINBOXAccess.TRASH;
    }

    @Override
    public void releaseResources() throws UnifiedINBOXException {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws OXException {
        return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
    }

    /**
     * Gets session user's locale.
     *
     * @return The session user's locale
     * @throws OXException If retrieving user's locale fails
     */
    private Locale getLocale() throws OXException {
        if (null == locale) {
            final UserService userService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(UserService.class, true);
            locale = userService.getUser(session.getUserId(), ctx).getLocale();
        }
        return locale;
    }

    private String getLocalizedName(final String fullname) throws OXException {
        if (UnifiedINBOXAccess.INBOX.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_INBOX);
        }
        if (UnifiedINBOXAccess.DRAFTS.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_DRAFTS);
        }
        if (UnifiedINBOXAccess.SENT.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_SENT);
        }
        if (UnifiedINBOXAccess.SPAM.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_SPAM);
        }
        if (UnifiedINBOXAccess.TRASH.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_TRASH);
        }
        throw UnifiedINBOXException.Code.UNKNOWN_DEFAULT_FOLDER_INDEX.create(fullname);
    }

    private static String startsWithKnownFullname(final String fullname) {
        for (final String knownFullname : UnifiedINBOXAccess.KNOWN_FOLDERS) {
            if (fullname.startsWith(knownFullname)) {
                // Cut off starting known fullname AND separator character
                return fullname.substring(knownFullname.length() + 1);
            }
        }
        return null;
    }

    private static String getStartingKnownFullname(final String fullname) {
        for (final String knownFullname : UnifiedINBOXAccess.KNOWN_FOLDERS) {
            if (fullname.startsWith(knownFullname)) {
                return knownFullname;
            }
        }
        return null;
    }

    private String getMailAccountName(final int accountId) throws OXException {
        final MailAccountStorageService storageService =
            UnifiedINBOXServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
        return storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()).getName();
    }

    private boolean isMailAccountEnabled(final int accountId) throws OXException {
        final MailAccountStorageService storageService =
            UnifiedINBOXServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
        return storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()).isUnifiedINBOXEnabled();
    }

    private static class MailFolderNameComparator implements Comparator<MailFolder> {

        private final Collator collator;

        public MailFolderNameComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        public int compare(final MailFolder o1, final MailFolder o2) {
            final String name1 = o1.getName();
            final String name2 = o2.getName();
            return collator.compare(name1 == null ? "" : name1, name2 == null ? "" : name2);
        }

    }

    /**
     * Tiny helper class.
     */
    private static final class Retval {

        final MailFolder mailFolder;

        final int index;

        public Retval(final MailFolder mailFolder, final int index) {
            super();
            this.index = index;
            this.mailFolder = mailFolder;
        }
    } // End of Retval

}
