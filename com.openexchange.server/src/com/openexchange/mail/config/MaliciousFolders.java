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

import java.util.ArrayList;
import java.util.Collections;
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
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;

/**
 * {@link MaliciousFolders} - A utility class to check if a folder is considered malicious.
 * <p>
 * io.ox/mail//maliciousFolders=
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class MaliciousFolders {

    private static final MaliciousFolders EMPTY = new MaliciousFolders(null) {

        @Override
        public boolean isMalicious(String fullName, int accountId, MailServletInterface mailInterface) throws OXException {
            return false;
        }

        @Override
        public List<String> getListing(MailServletInterface mailInterface) throws OXException {
            return Collections.emptyList();
        }
    };

    private static final Cache<UserAndContext, MaliciousFolders> CACHE = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();

    /**
     * Clears the cache.
     */
    public static void invalidateCache() {
        CACHE.invalidateAll();
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
    public static MaliciousFolders instanceFor(int userId, int contextId) throws OXException {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        MaliciousFolders object = CACHE.getIfPresent(key);
        if (null != object) {
            return object;
        }

        try {
            return CACHE.get(key, new MaliciousFoldersCallable(userId, contextId));
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

        String listing = "$Spam, $Confirmed-spam";
        {
            ComposedConfigProperty<String> property = view.property("com.openexchange.mail.maliciousFolders.listing", String.class);
            if (property.isDefined()) {
                String folders = property.get();
                if (false == Strings.isEmpty(folders)) {
                    listing = folders;
                }
            }
        }

        if (Strings.isEmpty(listing) || "none".equalsIgnoreCase(listing)) {
            return EMPTY;
        }

        String[] tokens = Strings.splitByCommaNotInQuotes(listing);
        ImmutableList.Builder<Checker> builder = ImmutableList.builder();
        for (String token : tokens) {
            if (token.startsWith("$")) {
                String match = Strings.asciiLowerCase(token.substring(1));
                if (match.equals("spam")) {
                    builder.add(new StandardFolderChecker(StorageUtility.INDEX_SPAM, MailAccount.DEFAULT_ID));
                } else if (match.equals("drafts")) {
                    builder.add(new StandardFolderChecker(StorageUtility.INDEX_DRAFTS, MailAccount.DEFAULT_ID));
                } else if (match.equals("inbox")) {
                    builder.add(new StandardFolderChecker(StorageUtility.INDEX_INBOX, MailAccount.DEFAULT_ID));
                } else if (match.equals("sent")) {
                    builder.add(new StandardFolderChecker(StorageUtility.INDEX_SENT, MailAccount.DEFAULT_ID));
                } else if (match.equals("trash")) {
                    builder.add(new StandardFolderChecker(StorageUtility.INDEX_TRASH, MailAccount.DEFAULT_ID));
                } else if (match.equals("confirmed-spam")) {
                    builder.add(new StandardFolderChecker(StorageUtility.INDEX_CONFIRMED_SPAM, MailAccount.DEFAULT_ID));
                } else if (match.equals("confirmed-ham")) {
                    builder.add(new StandardFolderChecker(StorageUtility.INDEX_CONFIRMED_HAM, MailAccount.DEFAULT_ID));
                } else {
                    throw new OXException(new IllegalArgumentException("Unsupported token: " + token));
                }
            } else {
                if (false == Strings.isEmpty(token) && !"none".equalsIgnoreCase(token)) {
                    builder.add(new FullNameFolderChecker(token, MailAccount.DEFAULT_ID));
                }
            }
        }

        return new MaliciousFolders(builder.build());
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
     * Gets the listing of malicious folders
     *
     * @param mailInterface The mail interface
     * @return The listing of malicious folders
     * @throws OXException If malicious folders cannot be returned
     */
    public List<String> getListing(MailServletInterface mailInterface) throws OXException {
        List<String> listing = new ArrayList<>(checkers.size());
        for (Checker checker : checkers) {
            String fullPath = checker.getFullName(mailInterface);
            if (null != fullPath) {
                listing.add(fullPath);
            }
        }
        return listing;
    }

    @Override
    public String toString() {
        return checkers.toString();
    }

    // -----------------------------------------------------------------------------------------------------

    private static final class MaliciousFoldersCallable implements Callable<MaliciousFolders> {

        private final int userId;
        private final int contextId;

        MaliciousFoldersCallable(int userId, int contextId) {
            this.userId = userId;
            this.contextId = contextId;
        }

        @Override
        public MaliciousFolders call() throws OXException {
            return doInitializeFor(userId, contextId);
        }
    }

    private static interface Checker {

        boolean isMalicious(String fullName, int accountId, MailServletInterface mailInterface) throws OXException;

        String getFullName(MailServletInterface mailInterface) throws OXException;
    }

    private static final class StandardFolderChecker implements Checker {

        private final int index;
        private final int accountId;

        StandardFolderChecker(int index, int accountId) {
            super();
            this.index = index;
            this.accountId = accountId;
        }

        @Override
        public String getFullName(MailServletInterface mailInterface) throws OXException {
            if (StorageUtility.INDEX_INBOX == index) {
                return MailFolderUtility.prepareFullname(accountId, "INBOX");
            }

            MailSessionCache mailSessionCache = MailSessionCache.getInstance(mailInterface.getSession());
            String[] arr = mailSessionCache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
            if (null != arr && arr.length > index && arr[index] != null) {
                return MailFolderUtility.prepareFullname(accountId, arr[index]);
            }

            switch (index) {
                case StorageUtility.INDEX_CONFIRMED_HAM:
                    return mailInterface.getConfirmedHamFolder(accountId);
                case StorageUtility.INDEX_CONFIRMED_SPAM:
                    return mailInterface.getConfirmedSpamFolder(accountId);
                case StorageUtility.INDEX_DRAFTS:
                    return mailInterface.getDraftsFolder(accountId);
                case StorageUtility.INDEX_SENT:
                    return mailInterface.getSentFolder(accountId);
                case StorageUtility.INDEX_SPAM:
                    return mailInterface.getSpamFolder(accountId);
                case StorageUtility.INDEX_TRASH:
                    return mailInterface.getTrashFolder(accountId);
                default:
                    break;
            }

            return null;
        }

        @Override
        public boolean isMalicious(String fullName, int accountId, MailServletInterface mailInterface) throws OXException {
            if (null == fullName || accountId != this.accountId) {
                return false;
            }

            if (StorageUtility.INDEX_INBOX == index) {
                return "INBOX".equals(fullName);
            }

            if ("INBOX".equals(fullName)) {
                // Fail fast...
                return false;
            }

            MailSessionCache mailSessionCache = MailSessionCache.getInstance(mailInterface.getSession());
            String[] arr = mailSessionCache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
            if (null != arr) {
                if (arr.length > index && fullName.equals(arr[index])) {
                    return true;
                }
            }

            switch (index) {
                case StorageUtility.INDEX_CONFIRMED_HAM:
                    return fullName.equals(mailInterface.getConfirmedHamFolder(accountId));
                case StorageUtility.INDEX_CONFIRMED_SPAM:
                    return fullName.equals(mailInterface.getConfirmedSpamFolder(accountId));
                case StorageUtility.INDEX_DRAFTS:
                    return fullName.equals(mailInterface.getDraftsFolder(accountId));
                case StorageUtility.INDEX_SENT:
                    return fullName.equals(mailInterface.getSentFolder(accountId));
                case StorageUtility.INDEX_SPAM:
                    return fullName.equals(mailInterface.getSpamFolder(accountId));
                case StorageUtility.INDEX_TRASH:
                    return fullName.equals(mailInterface.getTrashFolder(accountId));
                default:
                    break;
            }

            return false;
        }

        @Override
        public String toString() {
            switch (index) {
                case StorageUtility.INDEX_CONFIRMED_HAM:
                    return MailStrings.CONFIRMED_HAM;
                case StorageUtility.INDEX_CONFIRMED_SPAM:
                    return MailStrings.CONFIRMED_SPAM;
                case StorageUtility.INDEX_DRAFTS:
                    return MailStrings.DRAFTS;
                case StorageUtility.INDEX_SENT:
                    return MailStrings.SENT;
                case StorageUtility.INDEX_SPAM:
                    return MailStrings.SPAM;
                case StorageUtility.INDEX_TRASH:
                    return MailStrings.TRASH;
                case StorageUtility.INDEX_INBOX:
                    return "INBOX";
                default:
                    return "none";
            }
        }
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
        public String getFullName(MailServletInterface mailInterface) throws OXException {
            return MailFolderUtility.prepareFullname(accountId, fullName);
        }

        @Override
        public boolean isMalicious(String fullName, int accountId, MailServletInterface mailInterface) throws OXException {
            return this.accountId == accountId && this.fullName.equals(fullName);
        }

        @Override
        public String toString() {
            return fullName;
        }
    }

}
