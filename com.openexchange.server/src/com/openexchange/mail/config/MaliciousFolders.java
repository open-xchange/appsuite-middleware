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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.config;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link MaliciousFolders} - A utility class to check if a folder is considered malicious.
 * <p>
 * io.ox/mail//maliciousFolders=
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class MaliciousFolders {

    private static final MaliciousFolders EMPTY = new MaliciousFolders(Collections.<Checker> emptyList()) {

        @Override
        public boolean isMalicious(String fullName, int accountId, MailServletInterface mailInterface) throws OXException {
            return false;
        }
    };

    private static final Cache<UserAndContext, MaliciousFolders> CACHE = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();
    private static final Cache<String, MaliciousFolders> EXPRESSION_CACHE = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();

    /**
     * Clears the cache.
     */
    public static void invalidateCache() {
        CACHE.invalidateAll();
        EXPRESSION_CACHE.invalidateAll();
    }

    /**
     * Gets the malicious folders for specified session.
     *
     * @param session The session
     * @return The malicious folders
     * @throws OXException If malicious folders cannot be returned
     */
    public static MaliciousFolders instanceFor(Session session) throws OXException {
        return null == session ? null : instanceFor(session.getUserId(), session.getContextId());
    }

    /**
     * Gets the malicious folders for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The malicious folders
     * @throws OXException If malicious folders cannot be returned
     */
    public static MaliciousFolders instanceFor(final int userId, final int contextId) throws OXException {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        MaliciousFolders object = CACHE.getIfPresent(key);
        if (null != object) {
            return object;
        }

        Callable<MaliciousFolders> loader = new Callable<MaliciousFolders>() {

            @Override
            public MaliciousFolders call() throws OXException {
                return doInitializeFor(userId, contextId);
            }
        };

        try {
            return CACHE.get(key, loader);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw cause instanceof OXException ? (OXException) cause : new OXException(null == cause ? e : cause);
        }
    }

    static MaliciousFolders doInitializeFor(int userId, int contextId) throws OXException {
        ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = factory.getView(userId, contextId);

        boolean enabled = true;
        {
            ComposedConfigProperty<Boolean> property = view.property("com.openexchange.mail.maliciousFolders.enabled", Boolean.class);
            if (property.isDefined()) {
                enabled = property.get().booleanValue();
            }
        }

        if (false == enabled) {
            return EMPTY;
        }

        final String listing;
        {
            String tmp = "$Spam, $Confirmed-spam";
            ComposedConfigProperty<String> property = view.property("com.openexchange.mail.maliciousFolders.listing", String.class);
            if (property.isDefined()) {
                String folders = property.get();
                if (false == Strings.isEmpty(folders)) {
                    tmp = folders;
                }
            }
            listing = tmp;
        }

        if (Strings.isEmpty(listing) || "none".equalsIgnoreCase(listing)) {
            return EMPTY;
        }

        MaliciousFolders object = EXPRESSION_CACHE.getIfPresent(listing);
        if (null != object) {
            return object;
        }

        TIntList indexes = null;
        List<String> fullNames = null;
        {
            String[] tokens = Strings.splitByCommaNotInQuotes(listing);
            for (String token : tokens) {
                if (token.startsWith("$")) {
                    String match = Strings.asciiLowerCase(token.substring(1));
                    if (match.equals("spam")) {
                        if (null == indexes) {
                            indexes = new TIntArrayList(tokens.length);
                        }
                        indexes.add(StorageUtility.INDEX_SPAM);
                    } else if (match.equals("drafts")) {
                        if (null == indexes) {
                            indexes = new TIntArrayList(tokens.length);
                        }
                        indexes.add(StorageUtility.INDEX_DRAFTS);
                    } else if (match.equals("inbox")) {
                        if (null == indexes) {
                            indexes = new TIntArrayList(tokens.length);
                        }
                        indexes.add(StorageUtility.INDEX_INBOX);
                    } else if (match.equals("sent")) {
                        if (null == indexes) {
                            indexes = new TIntArrayList(tokens.length);
                        }
                        indexes.add(StorageUtility.INDEX_SENT);
                    } else if (match.equals("trash")) {
                        if (null == indexes) {
                            indexes = new TIntArrayList(tokens.length);
                        }
                        indexes.add(StorageUtility.INDEX_TRASH);
                    } else if (match.equals("confirmed-spam")) {
                        if (null == indexes) {
                            indexes = new TIntArrayList(tokens.length);
                        }
                        indexes.add(StorageUtility.INDEX_CONFIRMED_SPAM);
                    } else if (match.equals("confirmed-ham")) {
                        if (null == indexes) {
                            indexes = new TIntArrayList(tokens.length);
                        }
                        indexes.add(StorageUtility.INDEX_CONFIRMED_HAM);
                    } else {
                        throw new OXException(new IllegalArgumentException("Unsupported token: " + token));
                    }
                } else {
                    if (false == Strings.isEmpty(token) && !"none".equalsIgnoreCase(token)) {
                        if (null == fullNames) {
                            fullNames = new LinkedList<>();
                        }
                        fullNames.add(token);
                    }
                }
            }
        }

        ImmutableList.Builder<Checker> builder = ImmutableList.builder();
        if (null != indexes) {
            builder.add(new StandardFolderChecker(indexes.toArray()));
        }
        if (null != fullNames) {
            for (String fullName : fullNames) {
                FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(fullName);
                builder.add(new FullNameFolderChecker(fa.getFullName(), fa.getAccountId()));
            }
        }

        object = new MaliciousFolders(builder.build());
        EXPRESSION_CACHE.put(listing, object);
        return object;
    }

    // -----------------------------------------------------------------------------------------------------

    private final List<Checker> checkers;

    /**
     * Initializes a new {@link MaliciousFolders}.
     */
    MaliciousFolders(List<Checker> checkers) {
        super();
        this.checkers = checkers;
    }

    /**
     * Checks if the specified full name in given account appears to be malicious (according to configuration).
     *
     * @param fullName The full name
     * @param accountId The account identifier
     * @param mailInterface The mail interface
     * @return <code>true</code> if associated folder is considered malicious; otherwise <code>false</code>
     * @throws OXException If check for malicious folder fails
     */
    public boolean isMalicious(String fullName, int accountId, MailServletInterface mailInterface) throws OXException {
        for (Checker checker : checkers) {
            if (checker.isMalicious(fullName, accountId, mailInterface)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the applicable folder listing for this instance
     *
     * @return The folder listing
     */
    public List<String> getFolderListing() {
        List<String> tokens = new LinkedList<>();
        for (Checker checker : checkers) {
            checker.addTokensTo(tokens);
        }
        return tokens;
    }

    @Override
    public String toString() {
        StringBuilder sb = null;
        for (Checker checker : checkers) {
            sb = appendTo('\0', checker.toString(), sb);
        }
        return null == sb ? "" : sb.toString();
    }

    // -----------------------------------------------------------------------------------------------------

    private static interface Checker {

        boolean isMalicious(String fullName, int accountId, MailServletInterface mailInterface) throws OXException;

        void addTokensTo(List<String> tokens);
    }

    private static final class StandardFolderChecker implements Checker {

        private final String protocolUnifiedMail;
        private final int[] indexes;

        StandardFolderChecker(int[] indexes) {
            super();
            this.indexes = indexes;
            protocolUnifiedMail = UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX;
        }

        @Override
        public boolean isMalicious(String fullName, int accountId, MailServletInterface mailInterface) throws OXException {
            if (null == fullName) {
                return false;
            }

            for (int index : indexes) {
                if (checkForIndex(index, fullName, accountId, mailInterface)) {
                    return true;
                }
            }

            return false;
        }

        private boolean checkForIndex(int index, String fullName, int accountId, MailServletInterface mailInterface) throws OXException {
            if (StorageUtility.INDEX_INBOX == index) {
                return "INBOX".equals(fullName) || fullName.startsWith("INBOX" + mailInterface.getSeparator(accountId));
            }

            if ("INBOX".equals(fullName)) {
                // Fail fast...
                return false;
            }

            Session session = mailInterface.getSession();
            MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
            String[] arr = mailSessionCache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
            if (null != arr) {
                if (arr.length > index) {
                    String stdFullName = arr[index];
                    if (null != stdFullName && (fullName.equals(stdFullName) || fullName.startsWith(stdFullName + mailInterface.getSeparator(accountId)))) {
                        return true;
                    }
                }
            }

            String stdFullName = null;
            char separator = '\0';
            if (MailAccount.DEFAULT_ID != accountId) {
                MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
                MailAccount mailAccount = mass.getMailAccount(accountId, session.getUserId(), session.getContextId());
                if (protocolUnifiedMail.equals(mailAccount.getMailProtocol())) {
                    separator = MailPath.SEPERATOR;
                    switch (index) {
                        case StorageUtility.INDEX_CONFIRMED_HAM:
                            break;
                        case StorageUtility.INDEX_CONFIRMED_SPAM:
                            break;
                        case StorageUtility.INDEX_DRAFTS:
                            stdFullName = UnifiedInboxManagement.DRAFTS;
                            break;
                        case StorageUtility.INDEX_SENT:
                            stdFullName = UnifiedInboxManagement.SENT;
                            break;
                        case StorageUtility.INDEX_SPAM:
                            stdFullName = UnifiedInboxManagement.SPAM;
                            break;
                        case StorageUtility.INDEX_TRASH:
                            stdFullName = UnifiedInboxManagement.TRASH;
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (index) {
                        case StorageUtility.INDEX_CONFIRMED_HAM:
                            stdFullName = getFullNameFrom(mailAccount.getConfirmedHamFullname());
                            break;
                        case StorageUtility.INDEX_CONFIRMED_SPAM:
                            stdFullName = getFullNameFrom(mailAccount.getConfirmedSpamFullname());
                            break;
                        case StorageUtility.INDEX_DRAFTS:
                            stdFullName = getFullNameFrom(mailAccount.getDraftsFullname());
                            break;
                        case StorageUtility.INDEX_SENT:
                            stdFullName = getFullNameFrom(mailAccount.getSentFullname());
                            break;
                        case StorageUtility.INDEX_SPAM:
                            stdFullName = getFullNameFrom(mailAccount.getSpamFullname());
                            break;
                        case StorageUtility.INDEX_TRASH:
                            stdFullName = getFullNameFrom(mailAccount.getTrashFullname());
                            break;
                        default:
                            break;
                    }
                }
            }

            if (null == stdFullName) {
                switch (index) {
                    case StorageUtility.INDEX_CONFIRMED_HAM:
                        stdFullName = getFullNameFrom(mailInterface.getConfirmedHamFolder(accountId));
                        break;
                    case StorageUtility.INDEX_CONFIRMED_SPAM:
                        stdFullName = getFullNameFrom(mailInterface.getConfirmedSpamFolder(accountId));
                        break;
                    case StorageUtility.INDEX_DRAFTS:
                        stdFullName = getFullNameFrom(mailInterface.getDraftsFolder(accountId));
                        break;
                    case StorageUtility.INDEX_SENT:
                        stdFullName = getFullNameFrom(mailInterface.getSentFolder(accountId));
                        break;
                    case StorageUtility.INDEX_SPAM:
                        stdFullName = getFullNameFrom(mailInterface.getSpamFolder(accountId));
                        break;
                    case StorageUtility.INDEX_TRASH:
                        stdFullName = getFullNameFrom(mailInterface.getTrashFolder(accountId));
                        break;
                    default:
                        break;
                }
            }

            return (null != stdFullName && (fullName.equals(stdFullName) || fullName.startsWith(stdFullName + (separator > 0 ? separator : mailInterface.getSeparator(accountId)))));
        }

        private String getFullNameFrom(String preparedName) {
            if (null == preparedName) {
                return null;
            }

            return MailFolderUtility.prepareMailFolderParam(preparedName).getFullName();
        }

        @Override
        public void addTokensTo(List<String> tokens) {
            for (int index : indexes) {
                switch (index) {
                    case StorageUtility.INDEX_CONFIRMED_HAM:
                        tokens.add("$Confirmed-Ham");
                        break;
                    case StorageUtility.INDEX_CONFIRMED_SPAM:
                        tokens.add("$Confirmed-Spam");
                        break;
                    case StorageUtility.INDEX_DRAFTS:
                        tokens.add("$Drafts");
                        break;
                    case StorageUtility.INDEX_SENT:
                        tokens.add("$Sent");
                        break;
                    case StorageUtility.INDEX_SPAM:
                        tokens.add("$Spam");
                        break;
                    case StorageUtility.INDEX_TRASH:
                        tokens.add("$Trash");
                        break;
                    case StorageUtility.INDEX_INBOX:
                        tokens.add("$Inbox");
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = null;
            for (int index : indexes) {
                switch (index) {
                    case StorageUtility.INDEX_CONFIRMED_HAM:
                        sb = appendTo('$', MailStrings.CONFIRMED_HAM, sb);
                        break;
                    case StorageUtility.INDEX_CONFIRMED_SPAM:
                        sb = appendTo('$', MailStrings.CONFIRMED_SPAM, sb);
                        break;
                    case StorageUtility.INDEX_DRAFTS:
                        sb = appendTo('$', MailStrings.DRAFTS, sb);
                        break;
                    case StorageUtility.INDEX_SENT:
                        sb = appendTo('$', MailStrings.SENT, sb);
                        break;
                    case StorageUtility.INDEX_SPAM:
                        sb = appendTo('$', MailStrings.SPAM, sb);
                        break;
                    case StorageUtility.INDEX_TRASH:
                        sb = appendTo('$', MailStrings.TRASH, sb);
                        break;
                    case StorageUtility.INDEX_INBOX:
                        sb = appendTo('$', "INBOX", sb);
                        break;
                    default:
                        sb = appendTo('\0', "none", sb);
                        break;
                }
            }
            return null == sb ? "" : sb.toString();
        }
    }

    static StringBuilder appendTo(char sym, String str, StringBuilder sb) {
        if (null == sb) {
            StringBuilder newSb = new StringBuilder(24);
            if (sym > 0) {
                newSb.append(sym);
            }
            newSb.append(str);
            return newSb;
        }

        sb.append(", ");
        if (sym > 0) {
            sb.append(sym);
        }
        sb.append(str);
        return sb;
    }

    private static final class FullNameFolderChecker implements Checker {

        private final String fullName;
        private final int accountId;

        FullNameFolderChecker(String fullName, int accountId) {
            super();
            if (null == fullName) {
                throw new IllegalArgumentException("Full name must not be null.");
            }
            this.fullName = fullName;
            this.accountId = accountId;
        }

        @Override
        public boolean isMalicious(String fullName, int accountId, MailServletInterface mailInterface) throws OXException {
            return this.accountId == accountId && this.fullName.equals(fullName);
        }

        @Override
        public void addTokensTo(List<String> tokens) {
            tokens.add(MailFolderUtility.prepareFullname(accountId, fullName));
        }

        @Override
        public String toString() {
            return MailFolderUtility.prepareFullname(accountId, fullName);
        }

    }

}
