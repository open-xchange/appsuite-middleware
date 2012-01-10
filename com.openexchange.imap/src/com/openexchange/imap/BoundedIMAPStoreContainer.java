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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.imap;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import javax.mail.MessagingException;
import com.openexchange.imap.services.IMAPServiceRegistry;
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
        REENTRANT_SEMAPHORE, SEMAPHORE, SYNCHRONIZED;
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

    protected static void appendStackTrace(final StackTraceElement[] trace, final StringBuilder sb) {
        if (null == trace) {
            sb.append("<missing stack trace>\n");
            return;
        }
        for (final StackTraceElement ste : trace) {
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("\tat ").append(className).append('.').append(ste.getMethodName());
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
                sb.append("\n");
            }
        }
    }

    private static final class ReentrantSemaphoredBoundedIMAPStoreContainer extends UnboundedIMAPStoreContainer {

        protected final ConcurrentMap<Thread, CountedIMAPStore> stores;

        private final Semaphore semaphore;

        private volatile ScheduledTimerTask timerTask;

        protected ReentrantSemaphoredBoundedIMAPStoreContainer(final String server, final int port, final String login, final String pw, final int maxCount) {
            super(server, port, login, pw);
            semaphore = new Semaphore(maxCount, true);
            stores = new ConcurrentHashMap<Thread, CountedIMAPStore>(maxCount);
            if (DEBUG) {
                final TimerService service = IMAPServiceRegistry.getService(TimerService.class);
                if (null != service) {
                    final Runnable task = new Runnable() {

                        @Override
                        public void run() {
                            if (stores.isEmpty()) {
                                LOG.info("No occupied IMAPStore \"" + server + "\" instance for login " + login);
                            }
                            final StringBuilder sb = new StringBuilder(512);
                            for (final Entry<Thread, CountedIMAPStore> entry : stores.entrySet()) {
                                sb.setLength(0);
                                final Thread t = entry.getKey();
                                sb.append(t.getName()).append(" occupies IMAPStore \"");
                                sb.append(server).append("\" instance for login ").append(login).append('\n');
                                appendStackTrace(t.getStackTrace(), sb);
                                LOG.info(sb.toString());
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
            semaphore.acquire();
            final IMAPStore imapStore = super.getStore(imapSession);
            stores.put(thread, new CountedIMAPStore(imapStore));
            return imapStore;
        }

        @Override
        public void backStore(final IMAPStore imapStore) {
            final Thread thread = Thread.currentThread();
            final CountedIMAPStore cImapStore = stores.get(thread);
            cImapStore.count = cImapStore.count - 1;
            if (cImapStore.count > 0) {
                return;
            }
            stores.remove(thread);
            // Release IMAPStore instance orderly
            super.backStore(imapStore);
            semaphore.release();
        }
    }

    private static final class SemaphoredBoundedIMAPStoreContainer extends UnboundedIMAPStoreContainer {

        private final Semaphore semaphore;

        protected SemaphoredBoundedIMAPStoreContainer(final String server, final int port, final String login, final String pw, final int maxCount) {
            super(server, port, login, pw);
            semaphore = new Semaphore(maxCount, true);
        }

        @Override
        public IMAPStore getStore(final javax.mail.Session imapSession) throws MessagingException, InterruptedException {
            if (DEBUG) {
                LOG.debug("IMAPStoreContainer.getStore(): " + semaphore.getQueueLength() + " threads currently waiting for available IMAPStore instance.");
            }
            semaphore.acquire();
            return super.getStore(imapSession);
        }

        @Override
        public void backStore(final IMAPStore imapStore) {
            try {
                super.backStore(imapStore);
            } finally {
                semaphore.release();
            }
        }
    }

    private static final class SynchronizedBoundedIMAPStoreContainer extends UnboundedIMAPStoreContainer {

        private final Object mutex;

        private final int maxCount;

        private int count;
        
        protected SynchronizedBoundedIMAPStoreContainer(final String server, final int port, final String login, final String pw, final int maxCount) {
            super(server, port, login, pw);
            mutex = new Object();
            this.maxCount = maxCount;
            count = 0;
        }

        @Override
        public IMAPStore getStore(final javax.mail.Session imapSession) throws MessagingException, InterruptedException {
            synchronized (mutex) {
                while (count >= maxCount) {
                    mutex.wait();
                }
                count++;
                return super.getStore(imapSession);
            }
        }

        @Override
        public void backStore(final IMAPStore imapStore) {
            synchronized (mutex) {
                super.backStore(imapStore);
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
        case REENTRANT_SEMAPHORE:
            impl = new ReentrantSemaphoredBoundedIMAPStoreContainer(server, port, login, pw, maxCount);
            break;
        case SEMAPHORE:
            impl = new SemaphoredBoundedIMAPStoreContainer(server, port, login, pw, maxCount);
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

}
