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

package com.openexchange.mailaccount;

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import java.sql.Connection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageDefaultFolderAware;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link Tools} - A utility class for folder storage processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Tools {

    private static enum Policy {
        /**
         * Detect standard folder name by primary account
         */
        BY_PRIMARY_ACCOUNT,
        /**
         * Detect standard folder name by user's locale
         */
        BY_LOCALE;
    }

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
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = digit(s.charAt(i++));
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

    private static int digit(final char c) {
        switch (c) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        default:
            return -1;
        }
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
    public static MailAccount checkFullNames(final MailAccount account, final MailAccountFacade mailAccountFacade, final Session session) throws OXException {
        if (MailAccount.DEFAULT_ID == account.getId()) {
            /*
             * No check for primary account
             */
            return account;
        }
        final int contextId = session.getContextId();
        final DatabaseService databaseService = ServerServiceRegistry.getServize(DatabaseService.class);
        final Connection wcon = databaseService.getWritable(contextId);
        try {
            return checkFullNames(account, mailAccountFacade, session, wcon, null);
        } finally {
            databaseService.backWritable(contextId, wcon);
        }
    }

    /**
     * Checks for presence of default folder full names and creates them if absent.
     *
     * @param account The corresponding account
     * @param storageService The storage service (needed for update)
     * @param session The session providing needed user information
     * @param con The connection or <code>null</code>
     * @param folderNames Array of predefined folder names (e.g. Special-Use Folders) or null entries. If present, these names are used for creation.
     * @return The mail account with full names present
     * @throws OXException If check for full names fails
     */
    public static MailAccount checkFullNames(final MailAccount account, final MailAccountFacade mailAccountFacade, final Session session, final Connection con, final Map<String, String> folderNames) throws OXException {

        Map<String, String> given_names = folderNames;
        if (null == given_names) {
            given_names = new HashMap<String, String>();
        }
        
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
        String prefix = null;
        {
            final String mailProtocol = account.getMailProtocol();
            if (null != mailProtocol && toLowerCase(mailProtocol).startsWith("pop3")) {
                prefix = "";
            }
        }
        StringBuilder tmp = null;
        MailAccount primaryAccount = null;
        Locale locale = null;
        /*
         * Check full names
         */
        final int userId = serverSession.getUserId();
        final int contextId = serverSession.getContextId();
        try {
            //Confirmed-Ham
            String fullName = account.getConfirmedHamFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, serverSession);
                }
                String name = account.getConfirmedHam();
                if (null == name) {
                    locale = serverSession.getUser().getLocale();
                    primaryAccount = mailAccountFacade.getDefaultMailAccount(userId, contextId);
                    name = getName(StorageUtility.INDEX_CONFIRMED_HAM, primaryAccount, locale, Policy.BY_LOCALE);

                    mad.setConfirmedHam(name);
                    attributes.add(Attribute.CONFIRMED_HAM_LITERAL);
                }
                mad.setConfirmedHamFullname((tmp = new StringBuilder(prefix)).append(name).toString());
                attributes.add(Attribute.CONFIRMED_HAM_FULLNAME_LITERAL);
            }
            // Confirmed-Spam
            fullName = account.getConfirmedSpamFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, serverSession);
                    tmp = new StringBuilder(prefix);
                } else {
                    if (null == tmp) {
                        tmp = new StringBuilder(prefix);
                    } else {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = account.getConfirmedSpam();
                if (null == name) {
                    if (null == locale) {
                        locale = serverSession.getUser().getLocale();
                    }
                    if (null == primaryAccount) {
                        primaryAccount = mailAccountFacade.getDefaultMailAccount(userId, contextId);
                    }
                    name = getName(StorageUtility.INDEX_CONFIRMED_SPAM, primaryAccount, locale, Policy.BY_LOCALE);

                    mad.setConfirmedSpam(name);
                    attributes.add(Attribute.CONFIRMED_SPAM_LITERAL);
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
                    if (null == tmp) {
                        tmp = new StringBuilder(prefix);
                    } else {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = given_names.get(IMailFolderStorageDefaultFolderAware.DRAFTS);
                if (null == name) {
                    name = account.getDrafts();
                }
                if (null == name) {
                    if (null == locale) {
                        locale = serverSession.getUser().getLocale();
                    }
                    name = StringHelper.valueOf(locale).getString(MailStrings.DRAFTS);
                    mad.setDrafts(name);
                    attributes.add(Attribute.DRAFTS_LITERAL);
                }
                if ("Drafts".equalsIgnoreCase(name) && account.getMailServer().endsWith("yahoo.com")) {
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
                    if (null == tmp) {
                        tmp = new StringBuilder(prefix);
                    } else {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = given_names.get(IMailFolderStorageDefaultFolderAware.SENT);
                if (null == name) {
                    name = account.getSent();
                }
                if (null == name) {
                    if (null == locale) {
                        locale = serverSession.getUser().getLocale();
                    }
                    name = StringHelper.valueOf(locale).getString(MailStrings.SENT);

                    mad.setSent(name);
                    attributes.add(Attribute.SENT_LITERAL);
                }
                if ("Sent Items".equalsIgnoreCase(name) && account.getMailServer().endsWith("yahoo.com")) {
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
                    if (null == tmp) {
                        tmp = new StringBuilder(prefix);
                    } else {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = given_names.get(IMailFolderStorageDefaultFolderAware.SPAM);
                if (null == name) {
                    name = account.getSpam();
                }
                if (null == name) {
                    if (null == locale) {
                        locale = serverSession.getUser().getLocale();
                    }
                    name = StringHelper.valueOf(locale).getString(MailStrings.SPAM);

                    mad.setSpam(name);
                    attributes.add(Attribute.SPAM_LITERAL);
                }
                if ("Spam".equalsIgnoreCase(name) && account.getMailServer().endsWith("yahoo.com")) {
                    name = "Bulk Mail";
                    mad.setSpam(name);
                    attributes.add(Attribute.SPAM_LITERAL);
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
                    if (null == tmp) {
                        tmp = new StringBuilder(prefix);
                    } else {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = given_names.get(IMailFolderStorageDefaultFolderAware.TRASH);
                if (null == name) {
                    name = account.getTrash();
                }
                if (null == name) {
                    if (null == locale) {
                        locale = serverSession.getUser().getLocale();
                    }
                    name = StringHelper.valueOf(locale).getString(MailStrings.TRASH);

                    mad.setTrash(name);
                    attributes.add(Attribute.TRASH_LITERAL);
                }
                mad.setTrashFullname(tmp.append(name).toString());
                attributes.add(Attribute.TRASH_FULLNAME_LITERAL);
            }
            // archive
            fullName = account.getArchiveFullname();
            if (null == fullName) {
                if (null == prefix) {
                    prefix = getPrefix(accountId, serverSession);
                    tmp = new StringBuilder(prefix);
                } else {
                    if (null == tmp) {
                        tmp = new StringBuilder(prefix);
                    } else {
                        tmp.setLength(prefix.length());
                    }
                }
                String name = given_names.get(IMailFolderStorageDefaultFolderAware.ARCHIVE);
                if (null == name) {
                    name = account.getArchive();
                }
                if (null == name) {
                    if (null == locale) {
                        locale = serverSession.getUser().getLocale();
                    }
                    name = StringHelper.valueOf(locale).getString(MailStrings.ARCHIVE);

                    mad.setArchive(name);
                    attributes.add(Attribute.ARCHIVE_LITERAL);
                }
                mad.setArchiveFullname(tmp.append(name).toString());
                attributes.add(Attribute.ARCHIVE_FULLNAME_LITERAL);
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
                final DatabaseService databaseService = ServerServiceRegistry.getServize(DatabaseService.class);
                final Connection wcon = databaseService.getWritable(contextId);
                try {
                    mailAccountFacade.updateMailAccount(mad, attributes, userId, contextId, serverSession, wcon, false);
                    return mailAccountFacade.getMailAccount(accountId, userId, contextId, con);
                } finally {
                    databaseService.backWritable(contextId, wcon);
                }
            }
            mailAccountFacade.updateMailAccount(mad, attributes, userId, contextId, serverSession, con, false);
            return mailAccountFacade.getMailAccount(accountId, userId, contextId, con);
        } catch (final OXException e) {
            /*
             * Checking full names failed
             */
            final StringBuilder sb = new StringBuilder("Checking default folder full names for account ");
            sb.append(account.getId()).append(" failed with user ").append(userId);
            sb.append(" in context ").append(contextId);
            LoggerFactory.getLogger(Tools.class).warn(sb.toString(), e);
            return account;
        }
    }

    private static String getName(final int index, final MailAccount primaryAccount, final Locale locale, final Policy policy) {
        String retval;
        switch (index) {
        case StorageUtility.INDEX_DRAFTS:
            if (Policy.BY_PRIMARY_ACCOUNT == policy) {
                retval = primaryAccount.getDrafts();
                if (null == retval) {
                    retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getDrafts();
                }
            } else {
                retval = StringHelper.valueOf(locale).getString(MailStrings.DRAFTS);
            }
            break;
        case StorageUtility.INDEX_SENT:
            if (Policy.BY_PRIMARY_ACCOUNT == policy) {
                retval = primaryAccount.getSent();
                if (null == retval) {
                    retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getSent();
                }
            } else {
                retval = StringHelper.valueOf(locale).getString(MailStrings.SENT);
            }
            break;
        case StorageUtility.INDEX_SPAM:
            if (Policy.BY_PRIMARY_ACCOUNT == policy) {
                retval = primaryAccount.getSpam();
                if (null == retval) {
                    retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getSpam();
                }
            } else {
                retval = StringHelper.valueOf(locale).getString(MailStrings.SPAM);
            }
            break;
        case StorageUtility.INDEX_TRASH:
            if (Policy.BY_PRIMARY_ACCOUNT == policy) {
                retval = primaryAccount.getTrash();
                if (null == retval) {
                    retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getTrash();
                }
            } else {
                retval = StringHelper.valueOf(locale).getString(MailStrings.TRASH);
            }
            break;
        case StorageUtility.INDEX_CONFIRMED_SPAM:
            if (Policy.BY_PRIMARY_ACCOUNT == policy) {
                retval = primaryAccount.getConfirmedSpam();
                if (null == retval) {
                    retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getConfirmedSpam();
                }
            } else {
                // Special handling for confirmed-spam; see AdminUser.properties: no translation for that folder
                retval = "confirmed-spam";
                // retval = StringHelper.valueOf(locale).getString(MailStrings.CONFIRMED_SPAM);
            }
            break;
        case StorageUtility.INDEX_CONFIRMED_HAM:
            if (Policy.BY_PRIMARY_ACCOUNT == policy) {
                retval = primaryAccount.getConfirmedHam();
                if (null == retval) {
                    retval = DefaultFolderNamesProvider.DEFAULT_PROVIDER.getConfirmedHam();
                }
            } else {
                // Special handling for confirmed-ham; see AdminUser.properties: no translation for that folder
                retval = "confirmed-ham";
                // retval = StringHelper.valueOf(locale).getString(MailStrings.CONFIRMED_HAM);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown index value: " + index);
        }
        return retval;
    }

    private static String getPrefix(final int accountId, final ServerSession session) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> access = null;
        try {
            access = MailAccess.getInstance(session, accountId);
            access.connect(false);
            return access.getFolderStorage().getDefaultFolderPrefix();
        } finally {
            if (null != access) {
                access.close(true);
            }
        }
    }

    /**
     * Checks names for standard folders in case associated full-name is given.
     *
     * @param accountDescription The parsed account data
     * @param fieldsToUpdate The fields/attributes to update
     * @param sep The separator character
     */
    public static void checkNames(final MailAccountDescription accountDescription, final Set<Attribute> fieldsToUpdate, final Character separator) {
        if (null == separator) {
            return;
        }
        char sep = separator.charValue();
        if (fieldsToUpdate.contains(Attribute.TRASH_FULLNAME_LITERAL) && !isEmpty(accountDescription.getTrashFullname()) && !fieldsToUpdate.contains(Attribute.TRASH_LITERAL)) {
            final String name = Tools.getName(accountDescription.getTrashFullname(), sep);
            accountDescription.setTrash(name);
            fieldsToUpdate.add(Attribute.TRASH_LITERAL);
        }
        if (fieldsToUpdate.contains(Attribute.SENT_FULLNAME_LITERAL) && !isEmpty(accountDescription.getSentFullname()) && !fieldsToUpdate.contains(Attribute.SENT_LITERAL)) {
            final String name = Tools.getName(accountDescription.getSentFullname(), sep);
            accountDescription.setSent(name);
            fieldsToUpdate.add(Attribute.SENT_LITERAL);
        }
        if (fieldsToUpdate.contains(Attribute.DRAFTS_FULLNAME_LITERAL) && !isEmpty(accountDescription.getDraftsFullname()) && !fieldsToUpdate.contains(Attribute.DRAFTS_LITERAL)) {
            final String name = Tools.getName(accountDescription.getDraftsFullname(), sep);
            accountDescription.setDrafts(name);
            fieldsToUpdate.add(Attribute.DRAFTS_LITERAL);
        }
        if (fieldsToUpdate.contains(Attribute.SPAM_FULLNAME_LITERAL) && !isEmpty(accountDescription.getSpamFullname()) && !fieldsToUpdate.contains(Attribute.SPAM_LITERAL)) {
            final String name = Tools.getName(accountDescription.getSpamFullname(), sep);
            accountDescription.setSpam(name);
            fieldsToUpdate.add(Attribute.SPAM_LITERAL);
        }
        if (fieldsToUpdate.contains(Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL) && !isEmpty(accountDescription.getConfirmedSpamFullname()) && !fieldsToUpdate.contains(Attribute.CONFIRMED_SPAM_LITERAL)) {
            final String name = Tools.getName(accountDescription.getConfirmedSpamFullname(), sep);
            accountDescription.setConfirmedSpam(name);
            fieldsToUpdate.add(Attribute.CONFIRMED_SPAM_LITERAL);
        }
        if (fieldsToUpdate.contains(Attribute.CONFIRMED_HAM_FULLNAME_LITERAL) && !isEmpty(accountDescription.getConfirmedHamFullname()) && !fieldsToUpdate.contains(Attribute.CONFIRMED_HAM_LITERAL)) {
            final String name = Tools.getName(accountDescription.getConfirmedHamFullname(), sep);
            accountDescription.setConfirmedHam(name);
            fieldsToUpdate.add(Attribute.CONFIRMED_HAM_LITERAL);
        }
        if (fieldsToUpdate.contains(Attribute.ARCHIVE_FULLNAME_LITERAL) && !isEmpty(accountDescription.getArchiveFullname()) && !fieldsToUpdate.contains(Attribute.ARCHIVE_LITERAL)) {
            final String name = Tools.getName(accountDescription.getArchiveFullname(), sep);
            accountDescription.setArchive(name);
            fieldsToUpdate.add(Attribute.ARCHIVE_LITERAL);
        }
    }

    /**
     * Gets the separator character from associated account.
     *
     * @param accountId The account identifier
     * @param session The session
     * @return The separator character
     * @throws OXException If separator character retrieval fails
     */
    public static Character getSeparator(final int accountId, final ServerSession session) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = MailAccess.getInstance(session, accountId);
        return getSeparator(mailAccess);
    }

    /**
     * Gets the separator character from associated MailAccess.
     *
     * @param mailAccess - {@link MailAccess} to get the separator from.
     * @return The separator character
     * @throws OXException If separator character retrieval fails
     */
    public static Character getSeparator(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        if (null == mailAccess) {
            return null;
        }

        AbstractTask<Character> task = new AbstractTask<Character>() {

            @Override
            public Character call() throws Exception {
                try {
                    mailAccess.connect(false);
                    return Character.valueOf(mailAccess.getFolderStorage().getFolder("INBOX").getSeparator());
                } finally {
                    mailAccess.close(true);
                }
            }
        };

        Future<Character> f = ThreadPools.getThreadPool().submit(task);
        try {
            return f.get(3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        } catch (TimeoutException e) {
            f.cancel(true);
            return null;
        }
    }

    /**
     * Extracts the name from passed full name.
     *
     * @param fullName The full name
     * @param separator The separator character
     * @return The extracted name or <code>null</code>
     */
    public static String getName(final String fullName, final char separator) {
        if (Strings.isEmpty(fullName)) {
            return null;
        }
        return fullName.substring(fullName.lastIndexOf(separator) + 1);
    }
}
