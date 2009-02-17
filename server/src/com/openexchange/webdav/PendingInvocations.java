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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import com.openexchange.api2.SQLInterface;

/**
 * MetaObject for handling pending invocations and considering last modified changes during a "transaction".
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @param <I>
 */
public class PendingInvocations<I extends SQLInterface> implements Queue<QueuedAction<I>> {

    private Queue<QueuedAction<I>> pendingInvocations;

    private LastModifiedCache lastModifiedCache;

    public PendingInvocations(final Queue<QueuedAction<I>> pendingInvocations, final LastModifiedCache lastModifiedCache) {
        this.pendingInvocations = pendingInvocations;
        this.lastModifiedCache = lastModifiedCache;
    }

    public Queue<QueuedAction<I>> getPendingInvocations() {
        return pendingInvocations;
    }

    public void setPendingInvocations(Queue<QueuedAction<I>> pendingInvocations) {
        this.pendingInvocations = pendingInvocations;
    }

    public LastModifiedCache getLastModifiedCache() {
        return lastModifiedCache;
    }

    public void setLastModifiedCache(LastModifiedCache lastModifiedCache) {
        this.lastModifiedCache = lastModifiedCache;
    }

    public boolean add(QueuedAction<I> o) {
        return pendingInvocations.add(o);
    }

    public boolean addAll(Collection<? extends QueuedAction<I>> c) {
        return pendingInvocations.addAll(c);
    }

    public void clear() {
        pendingInvocations.clear();
    }

    public boolean contains(Object o) {
        return pendingInvocations.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return pendingInvocations.containsAll(c);
    }

    public QueuedAction<I> element() {
        return pendingInvocations.element();
    }

    public boolean isEmpty() {
        return pendingInvocations.isEmpty();
    }

    public Iterator<QueuedAction<I>> iterator() {
        return pendingInvocations.iterator();
    }

    public boolean offer(QueuedAction<I> o) {
        return pendingInvocations.offer(o);
    }

    public QueuedAction<I> peek() {
        return pendingInvocations.peek();
    }

    public QueuedAction<I> poll() {
        return pendingInvocations.poll();
    }

    public QueuedAction<I> remove() {
        return pendingInvocations.remove();
    }

    public boolean remove(Object o) {
        return pendingInvocations.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return pendingInvocations.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return pendingInvocations.retainAll(c);
    }

    public int size() {
        return pendingInvocations.size();
    }

    public Object[] toArray() {
        return pendingInvocations.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return pendingInvocations.toArray(a);
    }

}
