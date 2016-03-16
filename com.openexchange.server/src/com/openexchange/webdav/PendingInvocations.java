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

package com.openexchange.webdav;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * {@link PendingInvocations} - Meta object for handling pending invocations and considering last modified changes during a "transaction".
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @param <I>
 */
public class PendingInvocations<I> implements Queue<QueuedAction<I>> {

    private final Queue<QueuedAction<I>> pendingInvocations;

    private final LastModifiedCache lastModifiedCache;

    /**
     * Initializes a new {@link PendingInvocations}.
     *
     * @param pendingInvocations The backing queue (or delegate queue)
     * @param lastModifiedCache The last-modified cache
     */
    public PendingInvocations(final Queue<QueuedAction<I>> pendingInvocations, final LastModifiedCache lastModifiedCache) {
        this.pendingInvocations = pendingInvocations;
        this.lastModifiedCache = lastModifiedCache;
    }

    /**
     * Gets the last-modified cache.
     *
     * @return The last-modified cache.
     */
    public LastModifiedCache getLastModifiedCache() {
        return lastModifiedCache;
    }

    @Override
    public boolean add(final QueuedAction<I> o) {
        return pendingInvocations.add(o);
    }

    @Override
    public boolean addAll(final Collection<? extends QueuedAction<I>> c) {
        return pendingInvocations.addAll(c);
    }

    @Override
    public void clear() {
        pendingInvocations.clear();
    }

    @Override
    public boolean contains(final Object o) {
        return pendingInvocations.contains(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return pendingInvocations.containsAll(c);
    }

    @Override
    public QueuedAction<I> element() {
        return pendingInvocations.element();
    }

    @Override
    public boolean isEmpty() {
        return pendingInvocations.isEmpty();
    }

    @Override
    public Iterator<QueuedAction<I>> iterator() {
        return pendingInvocations.iterator();
    }

    @Override
    public boolean offer(final QueuedAction<I> o) {
        return pendingInvocations.offer(o);
    }

    @Override
    public QueuedAction<I> peek() {
        return pendingInvocations.peek();
    }

    @Override
    public QueuedAction<I> poll() {
        return pendingInvocations.poll();
    }

    @Override
    public QueuedAction<I> remove() {
        return pendingInvocations.remove();
    }

    @Override
    public boolean remove(final Object o) {
        return pendingInvocations.remove(o);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return pendingInvocations.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return pendingInvocations.retainAll(c);
    }

    @Override
    public int size() {
        return pendingInvocations.size();
    }

    @Override
    public Object[] toArray() {
        return pendingInvocations.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return pendingInvocations.toArray(a);
    }

}
