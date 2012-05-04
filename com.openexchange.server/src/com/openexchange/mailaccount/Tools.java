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

package com.openexchange.mailaccount;

import java.sql.Connection;
import java.util.EnumSet;
import java.util.Set;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.json.actions.AbstractMailAccountAction;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link Tools} - A utility class for folder storage processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Tools {

    /**
     * Initializes a new {@link Tools}.
     */
    private Tools() {
        super();
    }

    /**
     * The radix for base <code>10</code>.
     */
    private static final int RADIX = 10;

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    public static final int getUnsignedInteger(final String s) {
        if (s == null) {
            return -1;
        }

        final int max = s.length();

        if (max <= 0) {
            return -1;
        }
        if (s.charAt(0) == '-') {
            return -1;
        }

        int result = 0;
        int i = 0;

        final int limit = -Integer.MAX_VALUE;
        final int multmin = limit / RADIX;
        int digit;

        if (i < max) {
            digit = Character.digit(s.charAt(i++), RADIX);
            if (digit < 0) {
                return -1;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = Character.digit(s.charAt(i++), RADIX);
            if (digit < 0) {
                return -1;
            }
            if (result < multmin) {
                return -1;
            }
            result *= RADIX;
            if (result < limit + digit) {
                return -1;
            }
            result -= digit;
        }
        return -result;
    }

    /**
     * Checks for presence of default folder full names and creates them if absent.
     *
     * @param account The corresponding account
     * @param storageService The storage service (needed for update)
     * @param session The session providing needed user information
     * @return The mail account with full names present
     * @throws OXException If check for full names fails
     */
    public static MailAccount checkFullNames(final MailAccount account, final MailAccountStorageService storageService, final Session session) throws OXException {
        final int contextId = session.getContextId();
        final Connection rcon = Database.get(contextId, false);
        try {
            return checkFullNames(account, storageService, session, rcon);
        } finally {
            Database.back(contextId, false, rcon);
        }
        
    }

    /**
     * Checks for presence of default folder full names and creates them if absent.
     *
     * @param account The corresponding account
     * @param storageService The storage service (needed for update)
     * @param session The session providing needed user information
     * @param con The connection or <code>null</code>
     * @return The mail account with full names present
     * @throws OXException If check for full names fails
     */
    public static MailAccount checkFullNames(final MailAccount account, final MailAccountStorageService storageService, final Session session, final Connection con) throws OXException {
        final int accountId = account.getId();
        if (MailAccount.DEFAULT_ID == accountId) {
            /*
             * No check for primary account
             */
            return account;
        }
        final ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        final MailAccountDescription mad = new MailAccountDescription();
        mad.setId(accountId);
        final Set<Attribute> attributes = EnumSet.noneOf(Attribute.class);
        /*
         * Variables
         */
        final String mailServerURL = account.generateMailServerURL();
        String prefix = null != mailServerURL && mailServerURL.startsWith("pop3") ? "" : null;
        StringBuilder tmp = null;
        MailAccount primaryAccount = null;
        /*
         * Check full names
         */
        try {
            String fullName = account.getConfirmedHamFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, serverSession);
                }
                String name = account.getConfirmedHam();
                if (null == name) {
                    primaryAccount = storageService.getDefaultMailAccount(serverSession.getUserId(), serverSession.getContextId());
                    name = getName(StorageUtility.INDEX_CONFIRMED_HAM, primaryAccount);
                }
                mad.setConfirmedHamFullname((tmp = new StringBuilder(prefix)).append(name).toString());
                attributes.add(Attribute.CONFIRMED_HAM_FULLNAME_LITERAL);
            }
            // Confirmed-Ham
            fullName = account.getConfirmedSpamFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, serverSession);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getConfirmedSpam();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(serverSession.getUserId(), serverSession.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_CONFIRMED_SPAM, primaryAccount);
                }
                mad.setConfirmedSpamFullname(tmp.append(name).toString());
                attributes.add(Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL);
            }
            // Drafts
            fullName = account.getDraftsFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, serverSession);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getDrafts();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(serverSession.getUserId(), serverSession.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_DRAFTS, primaryAccount);
                }
                if ("Drafts".equals(name) && account.getMailServer().endsWith("yahoo.com")) {
                    name = "Draft";
                    mad.setDrafts(name);
                    attributes.add(Attribute.DRAFTS_LITERAL);
                }
                mad.setDraftsFullname(tmp.append(name).toString());
                attributes.add(Attribute.DRAFTS_FULLNAME_LITERAL);
            }
            // Sent
            fullName = account.getSentFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, serverSession);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getSent();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(serverSession.getUserId(), serverSession.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_SENT, primaryAccount);
                }
                if ("Sent Items".equals(name) && account.getMailServer().endsWith("yahoo.com")) {
                    name = "Sent";
                    mad.setSent(name);
                    attributes.add(Attribute.SENT_LITERAL);
                }
                mad.setSentFullname(tmp.append(name).toString());
                attributes.add(Attribute.SENT_FULLNAME_LITERAL);
            }
            // Spam
            fullName = account.getSpamFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, serverSession);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getSpam();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(serverSession.getUserId(), serverSession.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_SPAM, primaryAccount);
                }
                mad.setSpamFullname(tmp.append(name).toString());
                attributes.add(Attribute.SPAM_FULLNAME_LITERAL);
            }
            // Trash
            fullName = account.getTrashFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, serverSession);
                    tmp = new StringBuilder(prefix);
                } else {
                    tmp.setLength(prefix.length());
                }
                String name = account.getTrash();
                if (null == name) {
                    if (null == primaryAccount) {
                        primaryAccount = storageService.getDefaultMailAccount(serverSession.getUserId(), serverSession.getContextId());
                    }
                    name = getName(StorageUtility.INDEX_TRASH, primaryAccount);
                }
                mad.setTrashFullname(tmp.append(name).toString());
                attributes.add(Attribute.TRASH_FULLNAME_LITERAL);
            }
            /*
             * Something to update?
             */
            if (attributes.isEmpty()) {
                return account;
            }
            /*
             * Update and return re-fetched account instance
             */
            if (null == con) {
                storageService.updateMailAccount(mad, attributes, serverSession.getUserId(), serverSession.getContextId(), serverSession);
                return storageService.getMailAccount(accountId, serverSession.getUserId(), serverSession.getContextId());
            }
            storageService.updateMailAccount(mad, attributes, serverSession.getUserId(), serverSession.getContextId(), serverSession, con, false);
            final MailAccount[] accounts = storageService.getUserMailAccounts(serverSession.getUserId(), serverSession.getContextId(), con);
            for (final MailAccount macc : accounts) {
                if (macc.getId() == accountId) {
                    return macc;
                }
            }
            return null;
        } catch (final OXException e) {
            /*
             * Checking full names failed
             */
            final StringBuilder sb = new StringBuilder("Checking default folder full names for account ");
            sb.append(account.getId()).append(" failed with user ").append(serverSession.getUserId());
            sb.append(" in context ").append(serverSession.getContextId());
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AbstractMailAccountAction.class)).warn(sb.toString(), e);
            return account;
        }
    }

    private static String getName(final int index, final MailAccount primaryAccount) {
        String retval;
        switch (index) {
        case StorageUtility.INDEX_DRAFTS:
            retval = primaryAccount.getDrafts();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getDrafts();
            }
            break;
        case StorageUtility.INDEX_SENT:
            retval = primaryAccount.getSent();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getSent();
            }
            break;
        case StorageUtility.INDEX_SPAM:
            retval = primaryAccount.getSpam();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getSpam();
            }
            break;
        case StorageUtility.INDEX_TRASH:
            retval = primaryAccount.getTrash();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getTrash();
            }
            break;
        case StorageUtility.INDEX_CONFIRMED_SPAM:
            retval = primaryAccount.getConfirmedSpam();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getConfirmedSpam();
            }
            break;
        case StorageUtility.INDEX_CONFIRMED_HAM:
            retval = primaryAccount.getConfirmedHam();
            if (null == retval) {
                retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getConfirmedHam();
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown index value: " + index);
        }
        return retval;
    }

    private static String getPrefix(final int accountId, final ServerSession session) throws OXException {
        try {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access =
                MailAccess.getInstance(session, accountId);
            access.connect(false);
            try {
                return access.getFolderStorage().getDefaultFolderPrefix();
            } finally {
                access.close(true);
            }
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

}
