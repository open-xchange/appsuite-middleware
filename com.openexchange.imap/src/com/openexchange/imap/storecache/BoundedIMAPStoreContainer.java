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

package com.openexchange.imap.storecache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.services.Services;
import com.openexchange.log.Log;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link BoundedIMAPStoreContainer} - The bounded {@link IMAPStoreContainer}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoundedIMAPStoreContainer extends UnboundedIMAPStoreContainer {

    public static enum ImplType {
        SEMAPHORE, SYNCHRONIZED;

        public static ImplType implTypeFor(final String identifier) {
            for (final ImplType it : ImplType.values()) {
                if (it.name().equalsIgnoreCase(identifier)) {
                    return it;
                }
            }
            return null;
        }
    }

    private static final class CountedIMAPStore {
        int count;
        final IMAPStore imapStore;

        CountedIMAPStore (final IMAPStore imapStore) {
            super();
            count = 1;
            this.imapStore = imapStore;
        }
    }

    protected static void appendStackTrace(final StackTraceElement[] trace, final com.openexchange.java.StringAllocator sb, final String lineSeparator) {
        if (null == trace) {
            sb.append("<missing stack trace>\n");
            return;
        }
        for (final StackTraceElement ste : trace) {
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    sb.append("(Native Method)");
                } else {
                    final String fileName = ste.getFileName();
                    if (null == fileName) {
                        sb.append("(Unknown Source)");
                    } else {
                        final int lineNumber = ste.getLineNumber();
                        sb.append('(').append(fileName);
                        if (lineNumber >= 0) {
                            sb.append(':').append(lineNumber);
                        }
                        sb.append(')');
                    }
                }
                sb.append(lineSeparator);
            }
        }
    }

    private static final class ReentrantSemaphoredBoundedIMAPStoreContainer extends UnboundedIMAPStoreContainer {

        protected final ConcurrentMap<Thread, CountedIMAPStore> stores;

        protected final Semaphore semaphore;

        private final int timeoutMillis;

        private volatile ScheduledTimerTask timerTask;

        protected ReentrantSemaphoredBoundedIMAPStoreContainer(final String server, final int port, final String login, final String pw, final int maxCount) {
            super(server, port, login, pw);
            semaphore = new Semaphore(maxCount, true);
            stores = new NonBlockingHashMap<Thread, CountedIMAPStore>(maxCount);
            final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
            timeoutMillis = null == configurationService ? 20000 : configurationService.getIntProperty("com.openexchange.imap.imapConnectionTimeout", 20000);
            if (DEBUG) {
                final TimerService service = Services.getService(TimerService.class);
                if (null != service) {
                    final Runnable task = new Runnable() {

                        @Override
                        public void run() {
                            LOG.info(semaphore.getQueueLength() + " threads waiting to acquire a connection to \"" + server + "\" for login " + login);
                            if (stores.isEmpty()) {
                                return;
                            }
                            final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(512);
                            for (final Entry<Thread, CountedIMAPStore> entry : stores.entrySet()) {
                                if (sb.length() > 0) {
                                    sb.reinitTo(0);
                                }
                                final Thread t = entry.getKey();
                                sb.append(t.getName()).append(" occupies IMAPStore \"");
                                sb.append(server).append("\" instance for login ").append(login);
                                if (Log.appendTraceToMessage()) {
                                    final String lineSeparator = System.getProperty("line.separator");
                                    sb.append(lineSeparator);
                                    appendStackTrace(t.getStackTrace(), sb, lineSeparator);
                                    LOG.info(sb.toString());
                                } else {
                                    final Throwable throwable = new Throwable();
                                    throwable.setStackTrace(t.getStackTrace());
                                    LOG.info(sb.toString(), throwable);
                                }
                            }
                        }
                    };
                    timerTask = service.scheduleWithFixedDelay(task, 15000, 15000);
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            final ScheduledTimerTask task = timerTask;
            if (null != task) {
                task.cancel();
                timerTask = null;
            }
            super.finalize();
        }

        @Override
        public IMAPStore getStore(final javax.mail.Session imapSession) throws MessagingException, InterruptedException {
            if (DEBUG) {
                LOG.debug("IMAPStoreContainer.getStore(): " + semaphore.getQueueLength() + " threads currently waiting for available IMAPStore instance.");
            }
            final Thread thread = Thread.currentThread();
            // Reentrant thread?
            final CountedIMAPStore cImapStore = stores.get(thread);
            if (null != cImapStore) {
                cImapStore.count = cImapStore.count + 1;
                return cImapStore.imapStore;
            }
            // Acquire a new IMAPStore instance
            if (!semaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
                // Timed out...
                final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(512);
                sb.append(semaphore.getQueueLength()).append(" threads waiting. ");
                sb.append(semaphore.availablePermits()).append(" permits available for \"").append(server);
                sb.append("\" for login ").append(login);
                for (final Entry<Thread, CountedIMAPStore> entry : stores.entrySet()) {
                    final Thread t = entry.getKey();
                    sb.append("\n--- ").append(t.getName()).append(" occupies IMAPStore \"");
                    sb.append(server).append("\" instance for login ").append(login);
                    if (Log.appendTraceToMessage()) {
                        final String lineSeparator = System.getProperty("line.separator");
                        sb.append(lineSeparator);
                        appendStackTrace(t.getStackTrace(), sb, lineSeparator);
                        LOG.info(sb.toString());
                    } else {
                        final Throwable throwable = new Throwable();
                        throwable.setStackTrace(t.getStackTrace());
                        LOG.info(sb.toString(), throwable);
                    }
                }
                final Throwable t = new Throwable(sb.toString());
                final OXException e = IMAPException.create(IMAPException.Code.CONNECTION_UNAVAILABLE, t, server, login);
                throw new MessagingException(e.getMessage(), e);
            }
            // Obtain new IMAPStore instance
            final IMAPStore imapStore = getStoreErrorAware(imapSession);
            stores.put(thread, new CountedIMAPStore(imapStore));
            return imapStore;
        }

        private IMAPStore getStoreErrorAware(final javax.mail.Session imapSession) throws MessagingException, InterruptedException {
            boolean releasePermit = true;
            try {
                final IMAPStore imapStore = super.getStore(imapSession);
                releasePermit = false;
                return imapStore;
            } finally {
                if (releasePermit) {
                    // An exception/throwable was thrown; release previously acquired permit
                    semaphore.release();
                }
            }
        }

        @Override
        public void backStore(final IMAPStore imapStore) {
            final Thread thread = Thread.currentThread();
            final CountedIMAPStore cImapStore = stores.get(thread);
            if (null != cImapStore) {
                cImapStore.count = cImapStore.count - 1;
                if (cImapStore.count > 0) {
                    return;
                }
                stores.remove(thread);
            }
            try {
                // Release IMAPStore instance orderly
                super.backStoreNoValidityCheck(imapStore);
            } finally {
                semaphore.release();
            }
        }
    }

    private static final class SynchronizedBoundedIMAPStoreContainer extends UnboundedIMAPStoreContainer {

        private final Object mutex;

        private final int maxCount;

        private final int timeoutMillis;

        protected final Map<Thread, CountedIMAPStore> stores;

        private int count;

        protected SynchronizedBoundedIMAPStoreContainer(final String server, final int port, final String login, final String pw, final int maxCount) {
            super(server, port, login, pw);
            mutex = new Object();
            this.maxCount = maxCount;
            stores = new HashMap<Thread, CountedIMAPStore>(maxCount);
            count = 0;
            final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
            timeoutMillis = null == configurationService ? 20000 : configurationService.getIntProperty("com.openexchange.imap.imapConnectionTimeout", 20000);
        }

        @Override
        public IMAPStore getStore(final javax.mail.Session imapSession) throws MessagingException, InterruptedException {
            synchronized (mutex) {
                final Thread thread = Thread.currentThread();
                // Reentrant thread?
                final CountedIMAPStore cImapStore = stores.get(thread);
                if (null != cImapStore) {
                    cImapStore.count = cImapStore.count + 1;
                    return cImapStore.imapStore;
                }
                // Try to acquire a new IMAPStore instance
                while (count >= maxCount) {
                    mutex.wait(timeoutMillis);
                    if (count >= maxCount) {
                        // Timed out...
                        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(512);
                        sb.append(maxCount - count).append(" permits available for \"").append(server);
                        sb.append("\" for login ").append(login);
                        final String lineSeparator = System.getProperty("line.separator");
                        for (final Entry<Thread, CountedIMAPStore> entry : stores.entrySet()) {
                            final Thread t = entry.getKey();
                            sb.append(lineSeparator).append("--- ").append(t.getName()).append(" occupies IMAPStore \"");
                            sb.append(server).append("\" instance for login ").append(login);
                            sb.append(lineSeparator);
                            appendStackTrace(t.getStackTrace(), sb, lineSeparator);
                        }
                        final Throwable t = new Throwable(sb.toString());
                        final OXException e = IMAPException.create(IMAPException.Code.CONNECTION_UNAVAILABLE, t, server, login);
                        throw new MessagingException(e.getMessage(), e);
                    }
                }
                final IMAPStore imapStore = super.getStore(imapSession);
                stores.put(thread, new CountedIMAPStore(imapStore));
                count++;
                return imapStore;
            }
        }

        @Override
        public void backStore(final IMAPStore imapStore) {
            synchronized (mutex) {
                final Thread thread = Thread.currentThread();
                final CountedIMAPStore cImapStore = stores.get(thread);
                if (null != cImapStore) {
                    cImapStore.count = cImapStore.count - 1;
                    if (cImapStore.count > 0) {
                        return;
                    }
                    stores.remove(thread);
                }
                // Release IMAPStore instance orderly
                super.backStoreNoValidityCheck(imapStore);
                count--;
                mutex.notify();
            }
        }
    }

    private final IMAPStoreContainer impl;

    /**
     * Initializes a new {@link BoundedIMAPStoreContainer}.
     */
    public BoundedIMAPStoreContainer(final String server, final int port, final String login, final String pw, final int maxCount, final ImplType implType) {
        super();
        switch (implType) {
        case SEMAPHORE:
            impl = new ReentrantSemaphoredBoundedIMAPStoreContainer(server, port, login, pw, maxCount);
            break;
        default:
            impl = new SynchronizedBoundedIMAPStoreContainer(server, port, login, pw, maxCount);
            break;
        }
    }

    @Override
    public IMAPStore getStore(final javax.mail.Session imapSession) throws MessagingException, InterruptedException {
        return impl.getStore(imapSession);
    }

    @Override
    public void backStore(final IMAPStore imapStore) {
        impl.backStore(imapStore);
    }

    @Override
    public void clear() {
        impl.clear();
    }

    @Override
    public void closeElapsed(final long stamp, final StringBuilder debugBuilder) {
        impl.closeElapsed(stamp, debugBuilder);
    }

}
