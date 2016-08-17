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

package com.openexchange.imap.cache;

import static com.openexchange.imap.IMAPCommandsCollection.canCreateSubfolder;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.mail.MessagingException;
import javax.mail.Store;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.services.Services;
import com.openexchange.imap.util.ImmutableReference;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mailaccount.MailAccount;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link RootSubfoldersEnabledCache} - A cache to check for root sub-folders capability for a certain IMAP account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RootSubfoldersEnabledCache {

    private static volatile ConcurrentMap<String, Future<Boolean>> MAP;

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
        if (MAP == null) {
            synchronized (RootSubfoldersEnabledCache.class) {
                if (MAP == null) {
                    MAP = new NonBlockingHashMap<String, Future<Boolean>>();
                }
            }
        }
    }

    /**
     * Tear-down for this cache.
     */
    public static void tearDown() {
        if (MAP != null) {
            synchronized (RootSubfoldersEnabledCache.class) {
                ConcurrentMap<String, Future<Boolean>> map = MAP;
                if (map != null) {
                    clear(map);
                    MAP = null;
                }
            }
        }
    }

    /**
     * Clears this cache.
     */
    private static void clear(ConcurrentMap<String, Future<Boolean>> map) {
        map.clear();
    }

    private static volatile ImmutableReference<Boolean> rootSubfoldersAllowed;
    private static Boolean rootSubfoldersAllowed() {
        ImmutableReference<Boolean> tmp = rootSubfoldersAllowed;
        if (null == tmp) {
            synchronized (RootSubfoldersEnabledCache.class) {
                tmp = rootSubfoldersAllowed;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    Boolean defaultValue = null;
                    if (null == service) {
                        return defaultValue;
                    }

                    String property = service.getProperty("com.openexchange.imap.rootSubfoldersAllowed");
                    tmp = new ImmutableReference<Boolean>(Strings.isEmpty(property) ? null : Boolean.valueOf(property.trim()));
                    rootSubfoldersAllowed = tmp;
                }
            }
        }
        return tmp.getValue();
    }

    private static volatile Boolean namespacePerUser;
    private static boolean namespacePerUser() {
        Boolean tmp = namespacePerUser;
        if (null == tmp) {
            synchronized (RootSubfoldersEnabledCache.class) {
                tmp = namespacePerUser;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    boolean defaultValue = true;
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Boolean.valueOf(service.getBoolProperty("com.openexchange.imap.namespacePerUser", defaultValue));
                    namespacePerUser = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                namespacePerUser = null;
                rootSubfoldersAllowed = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.imap.rootSubfoldersAllowed", "com.openexchange.imap.namespacePerUser");
            }
        });
    }

    private static String getKeyFor(Store store, IMAPConfig imapConfig, boolean namespacePerUser) {
        if (namespacePerUser) {
            return store.getURLName().toString();
        }
        return new StringBuilder(24).append(imapConfig.isSecure() ? "imaps://" : "imap://").append(imapConfig.getServer()).append(':').append(imapConfig.getPort()).toString();
    }

    /**
     * Checks if root sub-folders capability is enabled for given IMAP account.
     *
     * @param imapConfig The IMAP configuration
     * @param imapStore The IMAP store to test with
     * @return <code>true</code> if MBox feature is enabled; otherwise <code>false</code>
     * @throws OXException If a mail error occurs
     */
    public static boolean isRootSubfoldersEnabled(final IMAPConfig imapConfig, final IMAPStore imapStore) throws OXException {
        if (MailAccount.DEFAULT_ID == imapConfig.getAccountId()) {
            Boolean rootSubfoldersAllowed = rootSubfoldersAllowed();
            if (null != rootSubfoldersAllowed) {
                return rootSubfoldersAllowed.booleanValue();
            }
        }

        try {
            boolean namespacePerUser = namespacePerUser();
            return isRootSubfoldersEnabled0(getKeyFor(imapStore, imapConfig, namespacePerUser), imapConfig, (DefaultFolder) imapStore.getDefaultFolder(), namespacePerUser);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig);
        }
    }

    /**
     * Checks if root sub-folders capability is enabled for given IMAP account.
     *
     * @param imapConfig The IMAP configuration
     * @param imapDefaultFolder The IMAP default folder to test with
     * @return <code>true</code> if MBox feature is enabled; otherwise <code>false</code>
     * @throws OXException If a mail error occurs
     */
    public static boolean isRootSubfoldersEnabled(final IMAPConfig imapConfig, final DefaultFolder imapDefaultFolder) throws OXException {
        if (MailAccount.DEFAULT_ID == imapConfig.getAccountId()) {
            Boolean rootSubfoldersAllowed = rootSubfoldersAllowed();
            if (null != rootSubfoldersAllowed) {
                return rootSubfoldersAllowed.booleanValue();
            }
        }

        boolean namespacePerUser = namespacePerUser();
        return isRootSubfoldersEnabled0(getKeyFor(imapDefaultFolder.getStore(), imapConfig, namespacePerUser), imapConfig, imapDefaultFolder, namespacePerUser);
    }

    /**
     * Checks if root sub-folders capability is enabled for given IMAP account.
     */
    private static boolean isRootSubfoldersEnabled0(String key, IMAPConfig imapConfig, DefaultFolder imapDefaultFolder, boolean namespacePerUser) throws OXException {
        final ConcurrentMap<String, Future<Boolean>> map = MAP;
        Future<Boolean> f = map.get(key);
        if (null == f) {
            final FutureTask<Boolean> ft = new FutureTask<Boolean>(new RootSubfoldersEnabledCallable(imapDefaultFolder, namespacePerUser));
            f = map.putIfAbsent(key, ft);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }
        try {
            return f.get().booleanValue();
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        } catch (final CancellationException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
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

        RootSubfoldersEnabledCallable(final DefaultFolder imapDefaultFolder, boolean namespacePerUser) {
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
