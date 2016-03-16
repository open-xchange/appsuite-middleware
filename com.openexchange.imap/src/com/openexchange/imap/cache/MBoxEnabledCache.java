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
    public static boolean isMBoxEnabled(final IMAPConfig imapConfig, final IMAPFolder imapFolder, final String prefix) throws OXException {
        final ConcurrentMap<InetSocketAddress, Future<Boolean>> map = MAP;
        Future<Boolean> f = map.get(imapConfig.getImapServerSocketAddress());
        if (null == f) {
            final SettableFutureTask<Boolean> ft = new SettableFutureTask<Boolean>(new MBoxEnabledCallable(imapFolder, prefix));
            f = map.putIfAbsent(imapConfig.getImapServerSocketAddress(), ft);
            if (null == f) {
                f = ft;
                Boolean mbox;
                try {
                    mbox = ListLsubCache.consideredAsMBox(imapConfig.getAccountId(), imapFolder, imapConfig.getSession(), imapConfig.getIMAPProperties().isIgnoreSubscription());
                } catch (final MessagingException e) {
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

    private static final class MBoxEnabledCallable implements Callable<Boolean> {

        private static final int FOLDER_TYPE = (IMAPFolder.HOLDS_MESSAGES | IMAPFolder.HOLDS_FOLDERS);

        private final IMAPFolder imapFolder;

        private final String prefix;

        public MBoxEnabledCallable(final IMAPFolder imapFolder, final String prefix) {
            super();
            this.imapFolder = imapFolder;
            this.prefix = prefix;
        }

        @Override
        public Boolean call() throws Exception {
            return Boolean.valueOf(!IMAPCommandsCollection.supportsFolderType(imapFolder, FOLDER_TYPE, prefix));
        }

    }

    private static final class SettableFutureTask<V> extends FutureTask<V> {

        public SettableFutureTask(final Callable<V> callable) {
            super(callable);
        }

        @Override
        public void set(final V v) {
            super.set(v);
        }

    }

}
