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

package com.openexchange.unifiedinbox.copy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.UnifiedINBOXAccess;
import com.openexchange.unifiedinbox.UnifiedINBOXException;
import com.openexchange.unifiedinbox.UnifiedINBOXUID;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXExecutors;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility;

/**
 * {@link UnifiedINBOXMessageCopier} - Copies messages from/to Unified INBOX folders.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXMessageCopier {

    private static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

    private final Session session;

    private final UnifiedINBOXAccess access;

    /**
     * Initializes a new {@link UnifiedINBOXMessageCopier}.
     * 
     * @param session The session
     * @param access The Unified INBOX access
     */
    public UnifiedINBOXMessageCopier(final Session session, final UnifiedINBOXAccess access) {
        super();
        this.session = session;
        this.access = access;
    }

    /**
     * Performs the copy operation.
     * 
     * @param sourceFolder The source folder
     * @param destFolder The destination folder
     * @param mailIds The mail IDs denoting the mails to copy from source folder
     * @param fast <code>true</code> to perform fast copy; otherwise <code>false</code>
     * @param move <code>true</code> to perform a move operation; otherwise <code>false</code> for a copy operation
     * @return The corresponding mail IDs of copied messages in destination folder
     * @throws MailException If copy operation fails
     */
    public String[] doCopy(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws MailException {
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(sourceFolder)) {
            if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(destFolder)) {
                return knownFolder2KnownFolder(sourceFolder, destFolder, mailIds, fast, move);
            }
            return knownFolder2AccountFolder(sourceFolder, destFolder, mailIds, fast, move);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(destFolder)) {
            return accountFolder2KnownFolder(sourceFolder, destFolder, mailIds, fast, move);
        }
        return accountFolder2AccountFolder(sourceFolder, destFolder, mailIds, fast, move);
    }

    private String[] knownFolder2KnownFolder(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws MailException {
        /*
         * A copy from an account's default folder to same account's default folder. Resolving account's real fullnames for denoted default
         * folders is needed here.
         */
        if (move && sourceFolder.equals(destFolder)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.NO_EQUAL_MOVE);
        }
        // Helper object
        final UnifiedINBOXUID tmp = new UnifiedINBOXUID();
        // The array to fill
        final String[] retval = new String[mailIds.length];
        // A map remembering callables
        final Map<Integer, KF2KFCallable> callableMap = new HashMap<Integer, KF2KFCallable>(mailIds.length);
        // Iterate mail IDs
        for (int i = 0; i < mailIds.length; i++) {
            tmp.setUIDString(mailIds[i]);
            final Integer accountId = Integer.valueOf(tmp.getAccountId());
            // Look-up callable by account ID
            KF2KFCallable callable = callableMap.get(accountId);
            if (null == callable) {
                callable = new KF2KFCallable(sourceFolder, destFolder, fast, move, retval, tmp.getAccountId(), session);
                callableMap.put(accountId, callable);
            }
            callable.addIdAndIndex(tmp.getId(), Integer.valueOf(i));
        }
        // Perform callables
        final ExecutorService executorService = UnifiedINBOXExecutors.newUnlimitedCachedThreadPool("UnifiedINBOXMessageCopier-");
        final CompletionService<Object> completionService = new ExecutorCompletionService<Object>(executorService);
        try {
            performCallables(callableMap.values(), completionService);
        } finally {
            executorService.shutdown();
        }
        // Delete messages on move
        if (move) {
            access.getMessageStorage().deleteMessages(sourceFolder, mailIds, true);
        }
        return retval;
    }

    private String[] knownFolder2AccountFolder(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws MailException {
        /*
         * A copy/move from an account's default folder to a specific folder
         */
        // Parse destination folder
        final FullnameArgument destFullnameArgument = UnifiedINBOXUtility.parseNestedFullname(destFolder);
        final UnifiedINBOXUID tmp = new UnifiedINBOXUID();
        // Check for possible conflict on move
        final String destFullname = destFullnameArgument.getFullname();
        final int destAccountId = destFullnameArgument.getAccountId();
        if (move) {
            for (int i = 0; i < mailIds.length; i++) {
                tmp.setUIDString(mailIds[i]);
                // Check if accounts and fullnames are equal
                if (tmp.getAccountId() == destAccountId && tmp.getFullname().equals(destFullname)) {
                    throw new UnifiedINBOXException(UnifiedINBOXException.Code.NO_EQUAL_MOVE);
                }
            }
        }
        // Proceed
        final String[] retval = new String[mailIds.length];
        // A map remembering callables
        final Map<Integer, KF2AFEqualCallable> callableMap = new HashMap<Integer, KF2AFEqualCallable>(mailIds.length);
        final Map<Integer, KF2AFDifferCallable> otherCallableMap = new HashMap<Integer, KF2AFDifferCallable>(mailIds.length);
        // Iterate mail IDs
        for (int i = 0; i < mailIds.length; i++) {
            tmp.setUIDString(mailIds[i]);
            final Integer accountId = Integer.valueOf(tmp.getAccountId());
            // Check if accounts are equal...
            if (tmp.getAccountId() == destAccountId) {
                KF2AFEqualCallable callable = callableMap.get(accountId);
                if (null == callable) {
                    callable = new KF2AFEqualCallable(sourceFolder, destFullname, fast, move, retval, tmp.getAccountId(), session);
                    callableMap.put(accountId, callable);
                }
                callable.addIdAndIndex(tmp.getId(), Integer.valueOf(i));
            } else {
                // Accounts differ
                KF2AFDifferCallable callable = otherCallableMap.get(accountId);
                if (null == callable) {
                    callable = new KF2AFDifferCallable(tmp.getAccountId(), destAccountId, destFullname, fast, move, retval, session);
                    otherCallableMap.put(accountId, callable);
                }
                callable.addIdAndFullnameAndIndex(tmp.getId(), tmp.getFullname(), Integer.valueOf(i));
            }
        }
        // Perform callables
        final ExecutorService executorService = UnifiedINBOXExecutors.newUnlimitedCachedThreadPool("UnifiedINBOXMessageCopier-");
        final CompletionService<Object> completionService = new ExecutorCompletionService<Object>(executorService);
        try {
            performCallables(callableMap.values(), completionService);
            performCallables(otherCallableMap.values(), completionService);
        } finally {
            executorService.shutdown();
        }
        return retval;
    }

    private String[] accountFolder2KnownFolder(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws MailException {
        /*
         * A copy/move from a specific folder to this account's default folder
         */
        final String[] retval;
        // Parse source folder
        final FullnameArgument sourceFullnameArgument = UnifiedINBOXUtility.parseNestedFullname(sourceFolder);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, sourceFullnameArgument.getAccountId());
        mailAccess.connect();
        try {
            final String realDest = UnifiedINBOXUtility.determineAccountFullname(mailAccess, destFolder);
            final String sourceFullname = sourceFullnameArgument.getFullname();
            if (move && sourceFullname.equals(realDest)) {
                throw new UnifiedINBOXException(UnifiedINBOXException.Code.NO_EQUAL_MOVE);
            }
            if (move) {
                retval = mailAccess.getMessageStorage().moveMessages(sourceFullname, realDest, mailIds, fast);
            } else {
                retval = mailAccess.getMessageStorage().copyMessages(sourceFullname, realDest, mailIds, fast);
            }

        } finally {
            mailAccess.close(true);
        }
        return retval;
    }

    private String[] accountFolder2AccountFolder(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws MailException {
        /*
         * A copy/move from a specific folder to an account's specific folder
         */
        // Parse source folder
        final FullnameArgument sourceFullnameArgument = UnifiedINBOXUtility.parseNestedFullname(sourceFolder);
        // Parse destination folder
        final FullnameArgument destFullnameArgument = UnifiedINBOXUtility.parseNestedFullname(destFolder);
        // Check for equal mail account
        final String sourceFullname = sourceFullnameArgument.getFullname();
        final String destFullname = destFullnameArgument.getFullname();
        if (sourceFullnameArgument.getAccountId() == destFullnameArgument.getAccountId()) {
            if (move && sourceFullname.equals(destFullname)) {
                throw new UnifiedINBOXException(UnifiedINBOXException.Code.NO_EQUAL_MOVE);
            }
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, sourceFullnameArgument.getAccountId());
            mailAccess.connect();
            try {
                if (move) {
                    return mailAccess.getMessageStorage().moveMessages(sourceFullname, destFullname, mailIds, fast);
                }
                return mailAccess.getMessageStorage().copyMessages(sourceFullname, destFullname, mailIds, fast);
            } finally {
                mailAccess.close(true);
            }
        }
        final String[] retval;
        final MailAccess<?, ?> sourceMailAccess = MailAccess.getInstance(session, sourceFullnameArgument.getAccountId());
        sourceMailAccess.connect();
        try {
            final MailMessage[] mails = sourceMailAccess.getMessageStorage().getMessages(sourceFullname, mailIds, FIELDS_FULL);
            final MailAccess<?, ?> destMailAccess = MailAccess.getInstance(session, destFullnameArgument.getAccountId());
            destMailAccess.connect();
            try {
                retval = destMailAccess.getMessageStorage().appendMessages(destFullname, mails);
            } finally {
                destMailAccess.close(true);
            }
            if (move) {
                sourceMailAccess.getMessageStorage().deleteMessages(sourceFullname, mailIds, true);
            }
        } finally {
            sourceMailAccess.close(true);
        }
        return retval;
    }

    private static void performCallables(final Collection<? extends Callable<Object>> callables, final CompletionService<Object> completionService) throws MailException {
        for (final Callable<Object> callable : callables) {
            completionService.submit(callable);
        }
        // Wait for completion
        try {
            final int nCallables = callables.size();
            for (int k = 0; k < nCallables; k++) {
                completionService.take().get();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
        } catch (final ExecutionException e) {
            launderThrowable(e);
        }
    }

    private static void launderThrowable(final ExecutionException e) throws MailException {
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
    }

}
