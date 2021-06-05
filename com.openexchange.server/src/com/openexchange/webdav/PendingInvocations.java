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
