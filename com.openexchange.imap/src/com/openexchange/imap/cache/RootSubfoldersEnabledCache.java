/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.imap.cache;

import static com.openexchange.imap.IMAPCommandsCollection.canCreateSubfolder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import javax.mail.Store;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.exception.OXException;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link RootSubfoldersEnabledCache} - A cache to check for root sub-folders capability for a certain IMAP account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RootSubfoldersEnabledCache {

    private static final String ROOT_FULL_NAME = "";

    private static volatile Cache<String, Boolean> CACHE;

    /**
     * Initializes a new {@link RootSubfoldersEnabledCache}.
     */
    private RootSubfoldersEnabledCache() {
        super();
    }

    /**
     * Initializes this cache.
     */
    public static void init() {
        if (CACHE == null) {
            synchronized (RootSubfoldersEnabledCache.class) {
                if (CACHE == null) {
                    CACHE = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.DAYS).build();
                }
            }
        }
    }

    /**
     * Tear-down for this cache.
     */
    public static void tearDown() {
        if (CACHE != null) {
            synchronized (RootSubfoldersEnabledCache.class) {
                Cache<String, Boolean> cache = CACHE;
                if (cache != null) {
                    clear(cache);
                    CACHE = null;
                }
            }
        }
    }

    /**
     * Clears this cache.
     */
    private static void clear(Cache<String, Boolean> map) {
        map.invalidateAll();
    }

    private static String getKeyFor(Store store, IMAPConfig imapConfig, boolean namespacePerUser) {
        if (namespacePerUser) {
            return store.getURLName().toString();
        }
        return new StringBuilder(48).append(imapConfig.isSecure() ? "imaps://" : "imap://").append(imapConfig.getServer()).append(':').append(imapConfig.getPort()).toString();
    }

    /**
     * Checks if root sub-folders capability is enabled for given IMAP account.
     *
     * @param imapConfig The IMAP configuration
     * @param imapStore The IMAP store to test with
     * @param session The session
     * @return <code>true</code> if MBox feature is enabled; otherwise <code>false</code>
     * @throws OXException If a mail error occurs
     */
    public static boolean isRootSubfoldersEnabled(IMAPConfig imapConfig, IMAPStore imapStore, Session session) throws OXException {
        if (MailAccount.DEFAULT_ID == imapConfig.getAccountId()) {
            Boolean rootSubfoldersAllowed = IMAPProperties.getInstance().areRootSubfoldersAllowed(session.getUserId(), session.getContextId());
            if (null != rootSubfoldersAllowed) {
                return rootSubfoldersAllowed.booleanValue();
            }
        }

        try {
            // Check for personal namespace
            String personalNamespace = NamespaceFoldersCache.getPersonalNamespace(imapStore, true, session, imapConfig.getAccountId());
            if (ROOT_FULL_NAME.equals(personalNamespace)) {
                // Root level is signaled as personal namespace, thus creating folder there SHOULD be possible...
                return true;
            }

            boolean namespacePerUser =  IMAPProperties.getInstance().isNamespacePerUser(session.getUserId(), session.getContextId());
            return isRootSubfoldersEnabled0(getKeyFor(imapStore, imapConfig, namespacePerUser), imapConfig, (DefaultFolder) imapStore.getDefaultFolder(), namespacePerUser);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig);
        }
    }

    /**
     * Checks if root sub-folders capability is enabled for given IMAP account.
     *
     * @param imapConfig The IMAP configuration
     * @param imapDefaultFolder The IMAP default folder to test with
     * @param session The session
     * @return <code>true</code> if MBox feature is enabled; otherwise <code>false</code>
     * @throws OXException If a mail error occurs
     */
    public static boolean isRootSubfoldersEnabled(IMAPConfig imapConfig, DefaultFolder imapDefaultFolder, Session session) throws OXException {
        if (MailAccount.DEFAULT_ID == imapConfig.getAccountId()) {
            Boolean rootSubfoldersAllowed = IMAPProperties.getInstance().areRootSubfoldersAllowed(session.getUserId(), session.getContextId());
            if (null != rootSubfoldersAllowed) {
                return rootSubfoldersAllowed.booleanValue();
            }
        }

        IMAPStore store = (IMAPStore) imapDefaultFolder.getStore();
        try {
            // Check for personal namespace
            String personalNamespace = NamespaceFoldersCache.getPersonalNamespace(store, true, session, imapConfig.getAccountId());
            if (ROOT_FULL_NAME.equals(personalNamespace)) {
                // Root level is signaled as personal namespace, thus creating folder there SHOULD be possible...
                return true;
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig);
        }

        boolean namespacePerUser = IMAPProperties.getInstance().isNamespacePerUser(session.getUserId(), session.getContextId());
        return isRootSubfoldersEnabled0(getKeyFor(store, imapConfig, namespacePerUser), imapConfig, imapDefaultFolder, namespacePerUser);
    }

    /**
     * Checks if root sub-folders capability is enabled for given IMAP account.
     */
    private static boolean isRootSubfoldersEnabled0(String key, IMAPConfig imapConfig, DefaultFolder imapDefaultFolder, boolean namespacePerUser) throws OXException {
        Cache<String, Boolean> cache = CACHE;
        Boolean rootSubfoldersEnabled = cache.getIfPresent(key);
        if (null != rootSubfoldersEnabled) {
            return rootSubfoldersEnabled.booleanValue();
        }

        try {
            rootSubfoldersEnabled = cache.get(key, new RootSubfoldersEnabledCallable(imapDefaultFolder, namespacePerUser));
            return rootSubfoldersEnabled.booleanValue();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MessagingException) {
                throw MimeMailException.handleMessagingException((MessagingException) cause, imapConfig);
            }
            if (cause instanceof RuntimeException) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    private static final class RootSubfoldersEnabledCallable implements Callable<Boolean> {

        private final DefaultFolder imapDefaultFolder;
        private final boolean mNamespacePerUser;

        RootSubfoldersEnabledCallable(DefaultFolder imapDefaultFolder, boolean namespacePerUser) {
            super();
            this.imapDefaultFolder = imapDefaultFolder;
            this.mNamespacePerUser = namespacePerUser;
        }

        @Override
        public Boolean call() throws Exception {
            return canCreateSubfolder(imapDefaultFolder, mNamespacePerUser);
        }

    }

}
