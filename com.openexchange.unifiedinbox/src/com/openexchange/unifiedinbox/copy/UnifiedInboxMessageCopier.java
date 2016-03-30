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

package com.openexchange.unifiedinbox.copy;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mailaccount.UnifiedInboxUID;
import com.openexchange.session.Session;
import com.openexchange.threadpool.CompletionFuture;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.unifiedinbox.UnifiedInboxAccess;
import com.openexchange.unifiedinbox.UnifiedInboxException;
import com.openexchange.unifiedinbox.utility.UnifiedInboxUtility;

/**
 * {@link UnifiedInboxMessageCopier} - Copies messages from/to Unified Mail folders.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxMessageCopier {

    private static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

    private final Session session;

    private final UnifiedInboxAccess access;

    /**
     * Initializes a new {@link UnifiedInboxMessageCopier}.
     *
     * @param session The session
     * @param access The Unified Mail access
     */
    public UnifiedInboxMessageCopier(final Session session, final UnifiedInboxAccess access) {
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
     * @throws OXException If copy operation fails
     */
    public String[] doCopy(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws OXException {
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(sourceFolder)) {
            if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(destFolder)) {
                return knownFolder2KnownFolder(sourceFolder, destFolder, mailIds, fast, move);
            }
            return knownFolder2AccountFolder(sourceFolder, destFolder, mailIds, fast, move);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(destFolder)) {
            return accountFolder2KnownFolder(sourceFolder, destFolder, mailIds, fast, move);
        }
        return accountFolder2AccountFolder(sourceFolder, destFolder, mailIds, fast, move);
    }

    private String[] knownFolder2KnownFolder(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws OXException {
        /*
         * A copy from an account's default folder to same account's default folder. Resolving account's real fullnames for denoted default
         * folders is needed here.
         */
        if (move && sourceFolder.equals(destFolder)) {
            throw UnifiedInboxException.Code.NO_EQUAL_MOVE.create();
        }
        // Helper object
        final UnifiedInboxUID tmp = new UnifiedInboxUID();
        // The array to fill
        final String[] retval = new String[mailIds.length];
        // A map remembering callables
        final TIntObjectMap<KF2KFCallable> callableMap = new TIntObjectHashMap<KF2KFCallable>(mailIds.length);
        // Iterate mail IDs
        for (int i = 0; i < mailIds.length; i++) {
            tmp.setUIDString(mailIds[i]);
            final int accountId = tmp.getAccountId();
            // Look-up callable by account ID
            KF2KFCallable callable = callableMap.get(accountId);
            if (null == callable) {
                callable = new KF2KFCallable(sourceFolder, destFolder, fast, move, retval, tmp.getAccountId(), session);
                callableMap.put(accountId, callable);
            }
            callable.addIdAndIndex(tmp.getId(), i);
        }
        // Perform callables
        final ThreadPoolService threadPoolService = ThreadPools.getThreadPool();
        performCallables(callableMap.valueCollection(), threadPoolService);
        // Delete messages on move
        if (move) {
            access.getMessageStorage().deleteMessages(sourceFolder, mailIds, true);
        }
        return retval;
    }

    private String[] knownFolder2AccountFolder(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws OXException {
        /*
         * A copy/move from an account's default folder to a specific folder
         */
        // Parse destination folder
        final FullnameArgument destFullnameArgument = UnifiedInboxUtility.parseNestedFullName(destFolder);
        final UnifiedInboxUID tmp = new UnifiedInboxUID();
        // Check for possible conflict on move
        final String destFullname = destFullnameArgument.getFullname();
        final int destAccountId = destFullnameArgument.getAccountId();
        if (move) {
            for (final String mailId : mailIds) {
                tmp.setUIDString(mailId);
                // Check if accounts and fullnames are equal
                if (tmp.getAccountId() == destAccountId && tmp.getFullName().equals(destFullname)) {
                    throw UnifiedInboxException.Code.NO_EQUAL_MOVE.create();
                }
            }
        }
        // Proceed
        final String[] retval = new String[mailIds.length];
        // A map remembering callables
        final TIntObjectMap<KF2AFEqualCallable> callableMap = new TIntObjectHashMap<KF2AFEqualCallable>(mailIds.length);
        final TIntObjectMap<KF2AFDifferCallable> otherCallableMap = new TIntObjectHashMap<KF2AFDifferCallable>(mailIds.length);
        // Iterate mail IDs
        for (int i = 0; i < mailIds.length; i++) {
            tmp.setUIDString(mailIds[i]);
            final int accountId = tmp.getAccountId();
            // Check if accounts are equal...
            if (tmp.getAccountId() == destAccountId) {
                KF2AFEqualCallable callable = callableMap.get(accountId);
                if (null == callable) {
                    callable = new KF2AFEqualCallable(sourceFolder, destFullname, fast, move, retval, tmp.getAccountId(), session);
                    callableMap.put(accountId, callable);
                }
                callable.addIdAndIndex(tmp.getId(), i);
            } else {
                // Accounts differ
                KF2AFDifferCallable callable = otherCallableMap.get(accountId);
                if (null == callable) {
                    callable = new KF2AFDifferCallable(tmp.getAccountId(), destAccountId, destFullname, fast, move, retval, session);
                    otherCallableMap.put(accountId, callable);
                }
                callable.addIdAndFullnameAndIndex(tmp.getId(), tmp.getFullName(), i);
            }
        }
        // Perform callables
        final ThreadPoolService threadPoolService = ThreadPools.getThreadPool();
        performCallables(callableMap.valueCollection(), threadPoolService);
        performCallables(otherCallableMap.valueCollection(), threadPoolService);
        return retval;
    }

    private String[] accountFolder2KnownFolder(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws OXException {
        /*
         * A copy/move from a specific folder to this account's default folder
         */
        final String[] retval;
        // Parse source folder
        final FullnameArgument sourceFullnameArgument = UnifiedInboxUtility.parseNestedFullName(sourceFolder);
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, sourceFullnameArgument.getAccountId());
            mailAccess.connect();
            final String realDest = UnifiedInboxUtility.determineAccountFullName(mailAccess, destFolder);
            final String sourceFullname = sourceFullnameArgument.getFullname();
            if (move && sourceFullname.equals(realDest)) {
                throw UnifiedInboxException.Code.NO_EQUAL_MOVE.create();
            }
            if (move) {
                retval = mailAccess.getMessageStorage().moveMessages(sourceFullname, realDest, mailIds, fast);
            } else {
                retval = mailAccess.getMessageStorage().copyMessages(sourceFullname, realDest, mailIds, fast);
            }

        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
        return retval;
    }

    private String[] accountFolder2AccountFolder(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast, final boolean move) throws OXException {
        /*
         * A copy/move from a specific folder to an account's specific folder
         */
        // Parse source folder
        final FullnameArgument sourceFullnameArgument = UnifiedInboxUtility.parseNestedFullName(sourceFolder);
        // Parse destination folder
        final FullnameArgument destFullnameArgument = UnifiedInboxUtility.parseNestedFullName(destFolder);
        // Check for equal mail account
        final String sourceFullname = sourceFullnameArgument.getFullname();
        final String destFullname = destFullnameArgument.getFullname();
        if (sourceFullnameArgument.getAccountId() == destFullnameArgument.getAccountId()) {
            if (move && sourceFullname.equals(destFullname)) {
                throw UnifiedInboxException.Code.NO_EQUAL_MOVE.create();
            }
            MailAccess<?, ?> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, sourceFullnameArgument.getAccountId());
                mailAccess.connect();
                if (move) {
                    return mailAccess.getMessageStorage().moveMessages(sourceFullname, destFullname, mailIds, fast);
                }
                return mailAccess.getMessageStorage().copyMessages(sourceFullname, destFullname, mailIds, fast);
            } finally {
                if (null != mailAccess) {
                    mailAccess.close(true);
                }
            }
        }
        final String[] retval;
        MailAccess<?, ?> sourceMailAccess = null;
        try {
            sourceMailAccess = MailAccess.getInstance(session, sourceFullnameArgument.getAccountId());
            sourceMailAccess.connect();
            final MailMessage[] mails = sourceMailAccess.getMessageStorage().getMessages(sourceFullname, mailIds, FIELDS_FULL);
            MailAccess<?, ?> destMailAccess = null;
            try {
                destMailAccess = MailAccess.getInstance(session, destFullnameArgument.getAccountId());
                destMailAccess.connect();
                retval = destMailAccess.getMessageStorage().appendMessages(destFullname, mails);
            } finally {
                if (null != destMailAccess) {
                    destMailAccess.close(true);
                }
            }
            if (move) {
                sourceMailAccess.getMessageStorage().deleteMessages(sourceFullname, mailIds, true);
            }
        } finally {
            if (null != sourceMailAccess) {
                sourceMailAccess.close(true);
            }
        }
        return retval;
    }

    private static void performCallables(final Collection<? extends Task<Object>> callables, final ThreadPoolService threadPoolService) throws OXException {
        final CompletionFuture<Object> completionFuture = threadPoolService.invoke(callables, CallerRunsBehavior.getInstance());
        // Wait for completion
        try {
            final int nCallables = callables.size();
            for (int k = 0; k < nCallables; k++) {
                completionFuture.take().get();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

}
