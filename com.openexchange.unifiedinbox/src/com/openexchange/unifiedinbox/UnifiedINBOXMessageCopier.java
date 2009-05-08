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

import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility;

/**
 * {@link UnifiedINBOXMessageCopier} - Copies messages.
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
        // Iterate mail IDs
        final UnifiedINBOXUID tmp = new UnifiedINBOXUID();
        final String[] arr = new String[1];
        final String[] retval = new String[mailIds.length];
        for (int i = 0; i < mailIds.length; i++) {
            tmp.setUIDString(mailIds[i]);
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, tmp.getAccountId());
            mailAccess.connect();
            try {
                final String realSource = UnifiedINBOXUtility.determineAccountFullname(mailAccess, sourceFolder);
                final String realDest = UnifiedINBOXUtility.determineAccountFullname(mailAccess, destFolder);
                arr[0] = tmp.getId();
                if (move) {
                    retval[i] = mailAccess.getMessageStorage().moveMessages(realSource, realDest, arr, fast)[0];
                } else {
                    retval[i] = mailAccess.getMessageStorage().copyMessages(realSource, realDest, arr, fast)[0];
                }
            } finally {
                mailAccess.close(true);
            }
        }
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
        final String[] arr = new String[1];
        for (int i = 0; i < mailIds.length; i++) {
            tmp.setUIDString(mailIds[i]);
            // Check if accounts are equal...
            if (tmp.getAccountId() == destAccountId) {
                final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, tmp.getAccountId());
                mailAccess.connect();
                try {
                    final String realSource = UnifiedINBOXUtility.determineAccountFullname(mailAccess, sourceFolder);
                    arr[0] = tmp.getId();
                    if (move) {
                        retval[i] = mailAccess.getMessageStorage().moveMessages(realSource, destFullname, arr, fast)[0];
                    } else {
                        retval[i] = mailAccess.getMessageStorage().copyMessages(realSource, destFullname, arr, fast)[0];
                    }
                } finally {
                    mailAccess.close(true);
                }
            } else {
                // Accounts differ
                final MailAccess<?, ?> sourceMailAccess = MailAccess.getInstance(session, tmp.getAccountId());
                sourceMailAccess.connect();
                try {
                    final MailMessage mailToCopy = sourceMailAccess.getMessageStorage().getMessage(tmp.getFullname(), tmp.getId(), false);
                    if (null == mailToCopy) {
                        retval[i] = null;
                    } else {
                        // Append to destination's storage
                        final MailAccess<?, ?> destMailAccess = MailAccess.getInstance(session, destAccountId);
                        destMailAccess.connect();
                        try {
                            // Append message to destination folder
                            retval[i] = destMailAccess.getMessageStorage().appendMessages(destFullname, new MailMessage[] { mailToCopy })[0];
                        } finally {
                            destMailAccess.close(true);
                        }
                        if (move) {
                            sourceMailAccess.getMessageStorage().deleteMessages(tmp.getFullname(), new String[] { tmp.getId() }, true);
                        }
                    }
                } finally {
                    sourceMailAccess.close(true);
                }
            }
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

}
