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

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.mail.MessagingException;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeMailException;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link MBoxEnabledCache} - A cache to check for MBox feature for a certain IMAP server.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MBoxEnabledCache {

    private static volatile ConcurrentMap<InetSocketAddress, Future<Boolean>> MAP;

    /**
     * Initializes a new {@link MBoxEnabledCache}.
     */
    private MBoxEnabledCache() {
        super();
    }

    /**
     * Initializes this cache.
     */
    public static void init() {
        if (MAP == null) {
            synchronized (MBoxEnabledCache.class) {
                if (MAP == null) {
                    MAP = new NonBlockingHashMap<InetSocketAddress, Future<Boolean>>();
                }
            }
        }
    }

    /**
     * Tear-down for this cache.
     */
    public static void tearDown() {
        if (MAP != null) {
            synchronized (MBoxEnabledCache.class) {
                if (MAP != null) {
                    clear();
                    MAP = null;
                }
            }
        }
    }

    /**
     * Clears this cache.
     */
    public static void clear() {
        MAP.clear();
    }

    /**
     * Checks if MBox feature is enabled for given IMAP server.
     *
     * @param imapConfig The IMAP configuration
     * @param imapFolder The IMAP folder to test with
     * @param prefix The full name prefix to use
     * @return <code>true</code> if MBox feature is enabled; otherwise <code>false</code>
     * @throws OXException If a mail error occurs
     */
    public static boolean isMBoxEnabled(IMAPConfig imapConfig, IMAPFolder imapFolder, String prefix) throws OXException {
        final ConcurrentMap<InetSocketAddress, Future<Boolean>> map = MAP;
        Future<Boolean> f = map.get(imapConfig.getImapServerSocketAddress());
        if (null == f) {
            final SettableFutureTask<Boolean> ft = new SettableFutureTask<Boolean>(new MBoxEnabledCallable(imapFolder, prefix, MailProperties.getInstance().getDefaultSeparator()));
            f = map.putIfAbsent(imapConfig.getImapServerSocketAddress(), ft);
            if (null == f) {
                f = ft;
                Boolean mbox;
                try {
                    mbox = ListLsubCache.consideredAsMBox(imapConfig.getAccountId(), imapFolder, imapConfig.getSession(), imapConfig.getIMAPProperties().isIgnoreSubscription());
                } catch (MessagingException e) {
                    throw MimeMailException.handleMessagingException(e, imapConfig, imapConfig.getSession());
                }
                if (null == mbox) {
                    ft.run();
                } else {
                    ft.set(mbox);
                }
            }
        }
        try {
            return f.get().booleanValue();
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        } catch (CancellationException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (ExecutionException e) {
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

    private static final class MBoxEnabledCallable implements Callable<Boolean> {

        private static final int FOLDER_TYPE = (IMAPFolder.HOLDS_MESSAGES | IMAPFolder.HOLDS_FOLDERS);
        private final IMAPFolder imapFolder;
        private final String prefix;
        private final char defaultSeparator;

        public MBoxEnabledCallable(IMAPFolder imapFolder, String prefix, char defaultSeparator) {
            super();
            this.imapFolder = imapFolder;
            this.prefix = prefix;
            this.defaultSeparator = defaultSeparator;
        }

        @Override
        public Boolean call() throws Exception {
            return Boolean.valueOf(!IMAPCommandsCollection.supportsFolderType(imapFolder, FOLDER_TYPE, prefix, defaultSeparator));
        }

    }

    private static final class SettableFutureTask<V> extends FutureTask<V> {

        public SettableFutureTask(Callable<V> callable) {
            super(callable);
        }

        @Override
        public void set(V v) {
            super.set(v);
        }

    }

}
