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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.ms.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ItemEvent;
import com.openexchange.java.util.UUIDs;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.Queue;

/**
 * {@link HzQueue}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HzQueue<E> implements Queue<E> {

    private final IQueue<E> hzQueue;
    private final String senderId;
    private final ConcurrentMap<MessageListener<E>, com.hazelcast.core.ItemListener<E>> registeredListeners;

    /**
     * Initializes a new {@link HzQueue}.
     */
    public HzQueue(final IQueue<E> hzQueue) {
        super();
        senderId = UUIDs.getUnformattedString(UUID.randomUUID());
        this.hzQueue = hzQueue;
        registeredListeners = new ConcurrentHashMap<MessageListener<E>, com.hazelcast.core.ItemListener<E>>(8);
    }

    @Override
    public String getSenderId() {
        return senderId;
    }

    @Override
    public String getName() {
        return hzQueue.getName();
    }

    @Override
    public void addMessageListener(final MessageListener<E> listener) {
        final HzMessageListener hzListener = new HzMessageListener(listener);
        hzQueue.addItemListener(hzListener, true);
        registeredListeners.put(listener, hzListener);
    }

    @Override
    public void removeMessageListener(final MessageListener<E> listener) {
        final com.hazelcast.core.ItemListener<E> hzListener = registeredListeners.remove(listener);
        if (null != hzListener) {
            hzQueue.removeItemListener(hzListener);
        }
    }

    @Override
    public void destroy() {
        hzQueue.destroy();
    }

    @Override
    public int size() {
        return hzQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return hzQueue.isEmpty();
    }

    @Override
    public boolean add(final E e) {
        return hzQueue.add(e);
    }

    @Override
    public Iterator<E> iterator() {
        return hzQueue.iterator();
    }

    @Override
    public E remove() {
        return hzQueue.remove();
    }

    @Override
    public boolean offer(final E e) {
        return hzQueue.offer(e);
    }

    @Override
    public Object[] toArray() {
        return hzQueue.toArray();
    }

    @Override
    public E poll() {
        return hzQueue.poll();
    }

    @Override
    public E element() {
        return hzQueue.element();
    }

    @Override
    public E peek() {
        return hzQueue.peek();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return hzQueue.toArray(a);
    }

    @Override
    public void put(final E e) throws InterruptedException {
        hzQueue.put(e);
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return hzQueue.offer(e, timeout, unit);
    }

    @Override
    public E take() throws InterruptedException {
        return hzQueue.take();
    }

    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return hzQueue.poll(timeout, unit);
    }

    @Override
    public int remainingCapacity() {
        return hzQueue.remainingCapacity();
    }

    @Override
    public boolean remove(final Object o) {
        return hzQueue.remove(o);
    }

    @Override
    public boolean contains(final Object o) {
        return hzQueue.contains(o);
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        return hzQueue.drainTo(c);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return hzQueue.containsAll(c);
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        return hzQueue.drainTo(c, maxElements);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return hzQueue.addAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return hzQueue.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return hzQueue.retainAll(c);
    }

    @Override
    public void clear() {
        hzQueue.clear();
    }

    // ------------------------------------------------------------------------ //

    private final class HzMessageListener implements com.hazelcast.core.ItemListener<E> {

        private final MessageListener<E> listener;

        /**
         * Initializes a new {@link HzMessageListener}.
         */
        protected HzMessageListener(final MessageListener<E> listener) {
            super();
            this.listener = listener;
        }

        @Override
        public void itemAdded(final ItemEvent<E> item) {
            listener.onMessage(new Message<E>(getName(), getSenderId(), item.getItem()));
        }

        @Override
        public void itemRemoved(final ItemEvent<E> item) {
            // Ignore
        }

    }

}
