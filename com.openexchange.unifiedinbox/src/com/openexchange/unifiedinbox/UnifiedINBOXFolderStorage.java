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

package com.openexchange.unifiedinbox;

import static com.openexchange.mail.MailPath.SEPERATOR;
import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.converters.UnifiedINBOXFolderConverter;
import com.openexchange.unifiedinbox.services.UnifiedINBOXServiceRegistry;
import com.openexchange.unifiedinbox.utility.LoggingCallable;
import com.openexchange.unifiedinbox.utility.TrackingCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXExecutors;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedINBOXFolderStorage} - The Unified INBOX folder storage implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXFolderStorage extends MailFolderStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(UnifiedINBOXFolderStorage.class);

    private final UnifiedINBOXAccess access;

    private final Session session;

    private final Context ctx;

    private Locale locale;

    /**
     * Initializes a new {@link UnifiedINBOXFolderStorage}
     * 
     * @param access The Unified INBOX access
     * @param session The session providing needed user data
     * @throws UnifiedINBOXException If context loading fails
     */
    public UnifiedINBOXFolderStorage(final UnifiedINBOXAccess access, final Session session) throws UnifiedINBOXException {
        super();
        this.access = access;
        this.session = session;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new UnifiedINBOXException(e);
        }
    }

    @Override
    public boolean exists(final String fullname) throws MailException {
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
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
        mailAccess.connect();
        try {
            return mailAccess.getFolderStorage().exists(fa.getFullname());
        } finally {
            mailAccess.close(true);
        }
    }

    @Override
    public MailFolder getFolder(final String fullname) throws MailException {
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
                throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, fullname);
            }
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, nestedAccountId);
            mailAccess.connect();
            try {
                final String nestedFullname = fa.getFullname();
                final MailFolder mailFolder = mailAccess.getFolderStorage().getFolder(nestedFullname);
                mailFolder.setFullname(UnifiedINBOXUtility.generateNestedFullname(
                    access.getAccountId(),
                    getStartingKnownFullname(fullname),
                    nestedAccountId,
                    nestedFullname));
                mailFolder.setName(getMailAccountName(nestedAccountId));
                return mailFolder;
            } finally {
                mailAccess.close(true);
            }
        }
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, fullname);
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
            return getRootSubfolders(false);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(parentFullname)) {
            return getKnownFolderSubfolders(parentFullname);
        }
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, parentFullname);
    }

    private static final String[] FULLNAMES = {
        UnifiedINBOXAccess.INBOX, UnifiedINBOXAccess.DRAFTS, UnifiedINBOXAccess.SENT, UnifiedINBOXAccess.SPAM, UnifiedINBOXAccess.TRASH };

    private MailFolder[] getRootSubfolders(final boolean byAccount) throws MailException {
        if (byAccount) {
            return getRootSubfoldersByAccount();
        }
        return getRootSubfoldersByFolder();
    }

    private MailFolder[] getRootSubfoldersByAccount() throws MailException {
        // Determine accounts
        final int unifiedINBOXAccountId = access.getAccountId();
        final MailAccount[] accounts;
        try {
            final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            final MailAccount[] arr = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
            final List<MailAccount> l = new ArrayList<MailAccount>(arr.length);
            for (int i = 0; i < arr.length; i++) {
                final MailAccount mailAccount = arr[i];
                if (unifiedINBOXAccountId != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                    l.add(mailAccount);
                }
            }
            accounts = l.toArray(new MailAccount[l.size()]);
        } catch (final ServiceException e) {
            throw new UnifiedINBOXException(e);
        } catch (final MailAccountException e) {
            throw new UnifiedINBOXException(e);
        }
        final int nAccounts = accounts.length;
        final ExecutorService executor = UnifiedINBOXExecutors.newCachedThreadPool(nAccounts);
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
            final int timeout = getMaxRunningTime();
            int completed = 0;
            int failed = 0;
            while (completed < nAccounts) {
                if (timeout <= 0) {
                    // No timeout
                    list.add(completionService.take().get());
                } else {
                    final Future<int[][]> f = completionService.poll(timeout, TimeUnit.MILLISECONDS);
                    if (null == f) {
                        // Waiting time elapsed before a completed task was present.
                        if (++failed <= 1) {
                            continue;
                        }
                        if (LOG.isWarnEnabled()) {
                            final UnifiedINBOXException e = new UnifiedINBOXException(
                                UnifiedINBOXException.Code.TIMEOUT,
                                Integer.valueOf(timeout * failed),
                                TimeUnit.MILLISECONDS.toString().toLowerCase());
                            LOG.warn(e.getMessage(), e);
                        }
                    } else {
                        list.add(f.get());
                    }
                }
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
            throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            if (MailException.class.isAssignableFrom(t.getClass())) {
                throw (MailException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new IllegalStateException("Not unchecked", t);
            }
        } finally {
            try {
                executor.shutdownNow();
                executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private MailFolder[] getRootSubfoldersByFolder() throws MailException {
        final MailFolder[] retval = new MailFolder[5];
        final ExecutorService executor = UnifiedINBOXExecutors.newCachedThreadPool(retval.length << 4);
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
            final int timeout = getMaxRunningTime();
            int completed = 0;
            int failed = 0;
            while (completed < retval.length) {
                final Retval r;
                if (timeout <= 0) {
                    // No timeout
                    r = completionService.take().get();
                } else {
                    final Future<Retval> f = completionService.poll(timeout, TimeUnit.MILLISECONDS);
                    if (null == f) {
                        // Waiting time elapsed before a completed task was present.
                        if (++failed <= 1) {
                            continue;
                        }
                        if (LOG.isWarnEnabled()) {
                            final UnifiedINBOXException e = new UnifiedINBOXException(
                                UnifiedINBOXException.Code.TIMEOUT,
                                Integer.valueOf(timeout * failed),
                                TimeUnit.MILLISECONDS.toString().toLowerCase());
                            LOG.warn(e.getMessage(), e);
                        }
                        r = null;
                    } else {
                        r = f.get();
                    }
                }
                completed++;
                if (null != r) {
                    retval[r.index] = r.mailFolder;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder("Retrieving root's subfolders took ").append(completionService.getDuration()).append("msec."));
            }
            // Return them
            return retval;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            if (MailException.class.isAssignableFrom(t.getClass())) {
                throw (MailException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new IllegalStateException("Not unchecked", t);
            }
        } finally {
            try {
                executor.shutdownNow();
                executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private MailFolder[] getKnownFolderSubfolders(final String parentFullname) throws MailException {
        final MailAccount[] accounts;
        try {
            final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            final MailAccount[] tmp = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
            final List<MailAccount> l = new ArrayList<MailAccount>(tmp.length);
            for (int i = 0; i < tmp.length; i++) {
                final MailAccount mailAccount = tmp[i];
                if (access.getAccountId() != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                    l.add(mailAccount);
                }
            }
            accounts = l.toArray(new MailAccount[l.size()]);
        } catch (final ServiceException e) {
            throw new UnifiedINBOXException(e);
        } catch (final MailAccountException e) {
            throw new UnifiedINBOXException(e);
        }
        final int unifiedInboxAccountId = access.getAccountId();
        final int length = accounts.length;
        final ExecutorService executor = UnifiedINBOXExecutors.newCachedThreadPool(length);
        final TrackingCompletionService<MailFolder> completionService = new UnifiedINBOXCompletionService<MailFolder>(executor);
        for (final MailAccount mailAccount : accounts) {
            completionService.submit(new LoggingCallable<MailFolder>(session) {

                public MailFolder call() throws Exception {
                    final MailAccess<?, ?> mailAccess;
                    try {
                        mailAccess = MailAccess.getInstance(getSession(), mailAccount.getId());
                        mailAccess.connect();
                    } catch (final MailException e) {
                        getLogger().error(e.getMessage(), e);
                        return null;
                    }
                    try {
                        final String accountFullname = UnifiedINBOXUtility.determineAccountFullname(mailAccess, parentFullname);
                        // Check if account fullname is not null
                        if (null == accountFullname) {
                            return null;
                        }
                        // Get mail folder
                        final MailFolder mailFolder = mailAccess.getFolderStorage().getFolder(accountFullname);
                        mailFolder.setFullname(new StringBuilder(MailFolderUtility.prepareFullname(unifiedInboxAccountId, parentFullname)).append(
                            SEPERATOR).append(MailFolderUtility.prepareFullname(mailAccount.getId(), mailFolder.getFullname())).toString());
                        mailFolder.setSubfolders(false);
                        mailFolder.setSubscribedSubfolders(false);
                        mailFolder.setName(mailAccount.getName());
                        return mailFolder;
                    } finally {
                        mailAccess.close(true);
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
            throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            if (MailException.class.isAssignableFrom(t.getClass())) {
                throw (MailException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new IllegalStateException("Not unchecked", t);
            }
        } finally {
            try {
                executor.shutdownNow();
                executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public MailFolder getRootFolder() throws MailException {
        return UnifiedINBOXFolderConverter.getRootFolder();
    }

    @Override
    public void checkDefaultFolders() throws MailException {
        // Nothing to do
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_CREATION_FAILED);
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.MOVE_DENIED);
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.UPDATE_DENIED);
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.DELETE_DENIED);
    }

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws MailException {
        // Shall we support clear() ? ? ?
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.CLEAR_NOT_SUPPORTED);
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return EMPTY_PATH;
        }
        if (!UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, fullname);
        }
        return new MailFolder[] {
            UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(access.getAccountId(), session, fullname, getLocalizedName(fullname)),
            UnifiedINBOXFolderConverter.getRootFolder() };
    }

    @Override
    public String getConfirmedHamFolder() throws MailException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws MailException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws MailException {
        return UnifiedINBOXAccess.DRAFTS;
    }

    @Override
    public String getSentFolder() throws MailException {
        return UnifiedINBOXAccess.SENT;
    }

    @Override
    public String getSpamFolder() throws MailException {
        return UnifiedINBOXAccess.SPAM;
    }

    @Override
    public String getTrashFolder() throws MailException {
        return UnifiedINBOXAccess.TRASH;
    }

    @Override
    public void releaseResources() throws UnifiedINBOXException {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws MailException {
        return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
    }

    /**
     * Gets session user's locale.
     * 
     * @return The session user's locale
     * @throws UnifiedINBOXException If retrieving user's locale fails
     */
    private Locale getLocale() throws UnifiedINBOXException {
        if (null == locale) {
            try {
                final UserService userService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(UserService.class, true);
                locale = userService.getUser(session.getUserId(), ctx).getLocale();
            } catch (final ServiceException e) {
                throw new UnifiedINBOXException(e);
            } catch (final UserException e) {
                throw new UnifiedINBOXException(e);
            }
        }
        return locale;
    }

    private static String getLocalizedName(final String fullname) throws UnifiedINBOXException {
        // TODO: Return real localized name
        if (UnifiedINBOXAccess.INBOX.equals(fullname)) {
            return UnifiedINBOXAccess.INBOX;
        }
        if (UnifiedINBOXAccess.DRAFTS.equals(fullname)) {
            return UnifiedINBOXAccess.DRAFTS;
        }
        if (UnifiedINBOXAccess.SENT.equals(fullname)) {
            return UnifiedINBOXAccess.SENT;
        }
        if (UnifiedINBOXAccess.SPAM.equals(fullname)) {
            return UnifiedINBOXAccess.SPAM;
        }
        if (UnifiedINBOXAccess.TRASH.equals(fullname)) {
            return UnifiedINBOXAccess.TRASH;
        }
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.UNKNOWN_DEFAULT_FOLDER_INDEX, fullname);
    }

    private static String startsWithKnownFullname(final String fullname) {
        for (final Iterator<String> iter = UnifiedINBOXAccess.KNOWN_FOLDERS.iterator(); iter.hasNext();) {
            final String knownFullname = iter.next();
            if (fullname.startsWith(knownFullname)) {
                // Cut off starting known fullname AND separator character
                return fullname.substring(knownFullname.length() + 1);
            }
        }
        return null;
    }

    private static String getStartingKnownFullname(final String fullname) {
        for (final Iterator<String> iter = UnifiedINBOXAccess.KNOWN_FOLDERS.iterator(); iter.hasNext();) {
            final String knownFullname = iter.next();
            if (fullname.startsWith(knownFullname)) {
                return knownFullname;
            }
        }
        return null;
    }

    private String getMailAccountName(final int accountId) throws UnifiedINBOXException {
        try {
            final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            return storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()).getName();
        } catch (final ServiceException e) {
            throw new UnifiedINBOXException(e);
        } catch (final MailAccountException e) {
            throw new UnifiedINBOXException(e);
        }
    }

    private boolean isMailAccountEnabled(final int accountId) throws UnifiedINBOXException {
        try {
            final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            return storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()).isUnifiedINBOXEnabled();
        } catch (final ServiceException e) {
            throw new UnifiedINBOXException(e);
        } catch (final MailAccountException e) {
            throw new UnifiedINBOXException(e);
        }
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

    private static int getMaxRunningTime() {
        final ConfigurationService cs = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        if (null != cs) {
            return cs.getIntProperty("AJP_WATCHER_MAX_RUNNING_TIME", 60000);
        }
        return -1;
    }

}
