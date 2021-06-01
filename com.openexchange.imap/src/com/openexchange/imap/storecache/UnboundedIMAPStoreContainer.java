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

package com.openexchange.imap.storecache;

import static com.openexchange.java.Autoboxing.I;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPAccess;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.imap.storecache.IMAPStoreCache.Key;
import com.openexchange.log.LogProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.sun.mail.imap.GreetingListener;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link UnboundedIMAPStoreContainer} - The unbounded {@link IMAPStoreContainer}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UnboundedIMAPStoreContainer extends AbstractIMAPStoreContainer {

    private final IMAPStorePriorityQueue availableQueue;
    private final boolean checkConnectivityIfPolled;
    private final Key key;
    private int numOfObtainedStores;
    private boolean invalid;

    protected final Lock lock;
    protected final String server;
    protected final int port;

    /**
     * Initializes a new {@link UnboundedIMAPStoreContainer}.
     */
    public UnboundedIMAPStoreContainer(int accountId, Session session, String server, int port, boolean propagateClientIp, boolean checkConnectivityIfPolled, IMAPStoreCache.Key key) {
        super(accountId, session, propagateClientIp);
        this.key = key;
        numOfObtainedStores = 0;
        invalid = false;
        lock = new ReentrantLock();
        availableQueue = new IMAPStorePriorityQueue();
        this.port = port;
        this.server = server;
        this.checkConnectivityIfPolled = checkConnectivityIfPolled;
    }

    @Override
    public IMAPStore getStore(javax.mail.Session imapSession, String login, String pw, Session session) throws IMAPStoreContainerInvalidException, MessagingException, InterruptedException {
        // Poll or create IMAP store
        IMAPStoreWrapper imapStoreWrapper;
        lock.lock();
        try {
            if (invalid) {
                throw new IMAPStoreContainerInvalidException();
            }
            numOfObtainedStores++;
            imapStoreWrapper = availableQueue.poll();
        } finally {
            lock.unlock();
        }

        if (null == imapStoreWrapper) {
            // No existent instance available
            IMAPStore imapStore = newStore(server, port, login, pw, imapSession, session);
            LOG.debug("UnboundedIMAPStoreContainer.getStore(): Returning newly established IMAP store instance. {} -- {}", imapStore.toString(), I(imapStore.hashCode()));
            return imapStore;
        }

        // Polled an existing instance
        IMAPStore imapStore = imapStoreWrapper.imapStore;

        if ((checkConnectivityIfPolled && (false == imapStore.isConnected())) || (imapSession.getDebug() != imapStore.getDebug())) {
            // IMAPStore instance is no more connected OR debug flag does not match
            IMAPAccess.closeSafely(imapStore);
            imapStore = newStore(server, port, login, pw, imapSession, session);
            LOG.debug("UnboundedIMAPStoreContainer.getStore(): Returning newly established IMAP store instance. {} -- {}", imapStore.toString(), I(imapStore.hashCode()));
            return imapStore;
        }

        // Grab associated IMAP session identifier (as advertised via "ID" command)
        String sessionInformation = imapStore.getGeneratedExternalId();
        if (null != sessionInformation) {
            LogProperties.put(LogProperties.Name.MAIL_SESSION, sessionInformation);
        }
        if (accountId == MailAccount.DEFAULT_ID) {
            GreetingListener greetingListener = IMAPProperties.getInstance().getHostNameRegex(session.getUserId(), session.getContextId());
            if (null != greetingListener) {
                String greeting = imapStore.getGreeting();
                greetingListener.onGreetingProcessed(greeting, imapStore.getHost(), imapStore.getPort());
            }
        }
        java.net.InetAddress remoteAddress = imapStore.getRemoteAddress();
        if (null != remoteAddress) {
            LogProperties.put(LogProperties.Name.MAIL_HOST_REMOTE_ADDRESS, remoteAddress.getHostAddress());
        }

        // Should we set properties from passed session?
        // imapStore.getServiceSession().getProperties().putAll(imapSession.getProperties());
        // imapStore.setPropagateClientIpAddress(imapSession.getProperty("mail.imap.propagate.clientipaddress"));
        LOG.debug("IMAPStoreContainer.getStore(): Returning _cached_ IMAP store instance. {} -- {}", imapStore.toString(), I(imapStore.hashCode()));
        return imapStore;
    }

    @Override
    public void backStore(IMAPStore imapStore) {
        // Try to put back given IMAP store w/o validity check
        boolean enqueued;
        lock.lock();
        try {
            if (invalid) {
                IMAPAccess.closeSafely(imapStore);
                return;
            }
            if (numOfObtainedStores > 0) {
                numOfObtainedStores--;
            }
            enqueued = availableQueue.offer(new IMAPStoreWrapper(imapStore));
        } finally {
            lock.unlock();
        }

        if (enqueued) {
            // System.out.println("IMAPStoreContainer.backStore(): Added IMAPStore instance to cache." + imapStore.toString() + " -- " +
            // imapStore.hashCode());
            LOG.debug("IMAPStoreContainer.backStore(): Added IMAP store instance to cache. {} -- {}", imapStore.toString(), I(imapStore.hashCode()));
        } else {
            IMAPAccess.closeSafely(imapStore);
        }
    }

    @Override
    public void closeElapsed(long stamp) {
        LOG.debug("IMAPStoreContainer.closeElapsed(): Closing elapsed IMAP store instances from queue for {}:{}", server, I(port));

        List<IMAPStoreWrapper> wrappersToClose = new ArrayList<>();
        boolean anyAdded = false;
        lock.lock();
        try {
            // Poll all elapsed instances
            for (IMAPStoreWrapper wrapper; (wrapper = availableQueue.pollIfElapsed(stamp)) != null;) {
                wrappersToClose.add(wrapper);
                anyAdded = true;
            }

            if (numOfObtainedStores <= 0 && availableQueue.isEmpty()) {
                invalid = true;
                IMAPStoreCache.getInstance().remove(key);
            }
        } finally {
            lock.unlock();
        }

        if (anyAdded) {
            boolean debugEnabled = LOG.isDebugEnabled();
            for (IMAPStoreWrapper wrapper : wrappersToClose) {
                try {
                    // System.out.println("IMAPStoreContainer.closeElapsed(): Closing elapsed IMAP store: " +
                    // imapStoreWrapper.imapStore.toString() + "-" + imapStoreWrapper.imapStore.hashCode());
                    if (debugEnabled) {
                        LOG.debug("IMAPStoreContainer.closeElapsed(): Closed elapsed IMAP store: {} -- {}", wrapper.imapStore.toString(), I(wrapper.imapStore.hashCode()));
                    }
                    IMAPAccess.closeSafely(wrapper.imapStore);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public void clear() {
        List<IMAPStoreWrapper> wrappersToClose = new ArrayList<>();
        lock.lock();
        try {
            for (IMAPStoreWrapper wrapper; (wrapper = availableQueue.poll()) != null;) {
                wrappersToClose.add(wrapper);
            }
        } finally {
            lock.unlock();
        }

        for (IMAPStoreWrapper wrapper : wrappersToClose) {
            try {
                IMAPAccess.closeSafely(wrapper.imapStore);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    @Override
    public boolean hasElapsed(long millis) {
        lock.lock();
        try {
            return availableQueue.hasElapsed(millis);
        } finally {
            lock.unlock();
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class IMAPStorePriorityQueue extends AbstractQueue<IMAPStoreWrapper> implements java.io.Serializable {

        private static final long serialVersionUID = 1337510919245408276L;

        final transient PriorityQueue<IMAPStoreWrapper> q;

        /**
         * Creates a <tt>IMAPStorePriorityQueue</tt> with the default initial capacity (11) that orders its elements according to
         * their {@linkplain Comparable natural ordering}.
         */
        protected IMAPStorePriorityQueue() {
            super();
            q = new PriorityQueue<IMAPStoreWrapper>();
        }

        /**
         * Checks if this queue contains any elapsed entry.
         *
         * @param minTimeStamp The minimum time stamp; any queued entry that has a last-accessed time stamp lower than given one is considered as elapsed
         * @return <code>true</code> if there is an elapsed entry, otherwise <code>false</code>
         */
        public boolean hasElapsed(long minTimeStamp) {
            final IMAPStoreWrapper e = q.peek();
            return (null != e && e.lastAccessed < minTimeStamp);
        }

        @Override
        public boolean add(IMAPStoreWrapper e) {
            return offer(e);
        }

        @Override
        public boolean offer(IMAPStoreWrapper e) {
            return q.offer(e);
        }

        /**
         * Retrieves and removes the head of this queue if elapsed compared to given time stamp, or returns <code>null</code> if this queue
         * is empty or head is not elapsed.
         *
         * @param minTimeStamp The minimum time stamp; any queued entry that has a last-accessed time stamp lower than given one is considered as elapsed
         * @return The elapsed head of this queue
         */
        public IMAPStoreWrapper pollIfElapsed(long minTimeStamp) {
            final IMAPStoreWrapper e = q.peek();
            if ((null != e) && (e.lastAccessed < minTimeStamp)) {
                return q.poll();
            }
            return null;
        }

        @Override
        public IMAPStoreWrapper poll() {
            return q.poll();
        }

        @Override
        public IMAPStoreWrapper peek() {
            return q.peek();
        }

        @Override
        public int size() {
            return q.size();
        }

        @Override
        public boolean remove(Object o) {
            return q.remove(o);
        }

        @Override
        public boolean contains(Object o) {
            return q.contains(o);
        }

        @Override
        public Object[] toArray() {
            return q.toArray();
        }

        @Override
        public String toString() {
            return q.toString();
        }

        @Override
        public void clear() {
            q.clear();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return q.toArray(a);
        }

        @Override
        public Iterator<IMAPStoreWrapper> iterator() {
            return new Itr(toArray());
        }

        /**
         * Snapshot iterator that works off copy of underlying q array.
         */
        private class Itr implements Iterator<IMAPStoreWrapper> {

            final Object[] array; // Array of all elements

            int cursor; // index of next element to return;

            int lastRet; // index of last element, or -1 if no such

            Itr(Object[] array) {
                lastRet = -1;
                this.array = array;
            }

            @Override
            public boolean hasNext() {
                return cursor < array.length;
            }

            @Override
            public IMAPStoreWrapper next() {
                if (cursor >= array.length) {
                    throw new NoSuchElementException();
                }
                lastRet = cursor;
                return (IMAPStoreWrapper) array[cursor++];
            }

            @Override
            public void remove() {
                if (lastRet < 0) {
                    throw new IllegalStateException();
                }
                final Object x = array[lastRet];
                lastRet = -1;
                // Traverse underlying queue to find == element,
                // not just a .equals element.
                for (Iterator<IMAPStoreWrapper> it = q.iterator(); it.hasNext();) {
                    if (it.next() == x) {
                        it.remove();
                        return;
                    }
                }
            }
        }

        private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
            s.defaultWriteObject();
        }

    } // End of class

}
