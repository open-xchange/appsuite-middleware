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

package com.openexchange.session.inspector.internal;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


/**
 * A {@link ServiceSet} is backed by the service registry and contains all services registered for a given interface in order of Service Ranking
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ServiceSet<E> implements NavigableSet<E>, ServiceTrackerCustomizer<E, E> {

    private final BundleContext context;
    private final ConcurrentHashMap<E, Integer> serviceRankings;
    private final ConcurrentHashMap<E, Long> serviceIds;
    private final ConcurrentSkipListSet<E> entries;
    private volatile boolean empty;

    /**
     * Initializes a new {@link ServiceSet}.
     */
    public ServiceSet(BundleContext context) {
        super();
        this.context = context;
        final ConcurrentHashMap<E, Integer> serviceRankings = new ConcurrentHashMap<E, Integer>();
        this.serviceRankings = serviceRankings;
        final ConcurrentHashMap<E, Long> serviceIds = new ConcurrentHashMap<E, Long>();
        this.serviceIds = serviceIds;
        empty = true;
        entries = new ConcurrentSkipListSet<E>(new Comparator<E>() {

            @Override
            public int compare(final E o1, final E o2) {
                // First order is ranking, second order is service identifier
                final int result = getRanking(o1) - getRanking(o2);
                return 0 == result ? (int) (getServiceId(o1) - getServiceId(o2)) : result;
            }

            private int getRanking(final E e) {
                final Integer i = serviceRankings.get(e);
                return null == i ? 0 : i.intValue();
            }

            private long getServiceId(final E e) {
                final Long l = serviceIds.get(e);
                return null == l ? 0L : l.longValue();
            }
        });
    }

    @Override
    public Comparator<? super E> comparator() {
        return entries.comparator();
    }

    @Override
    public E first() {
        return entries.first();
    }

    @Override
    public E last() {
        return entries.last();
    }

    /**
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Not supported and therefore throws an {@code UnsupportedOperationException}.
     * </div>
     * <p>
     */
    @Override
    public boolean add(E service) {
        throw new UnsupportedOperationException("This set can only be modified by the backing service registry");
    }

    /**
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Not supported and therefore throws an {@code UnsupportedOperationException}.
     * </div>
     * <p>
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("This set can only be modified by the backing service registry");
    }

    /**
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Not supported and therefore throws an {@code UnsupportedOperationException}.
     * </div>
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("This set can only be modified by the backing service registry");
    }

    @Override
    public boolean contains(Object o) {
        return entries.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return entries.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    /**
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Not supported and therefore throws an {@code UnsupportedOperationException}.
     * </div>
     * <p>
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("This set can only be modified by the backing service registry");
    }

    /**
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Not supported and therefore throws an {@code UnsupportedOperationException}.
     * </div>
     * <p>
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("This set can only be modified by the backing service registry");
    }

    /**
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Not supported and therefore throws an {@code UnsupportedOperationException}.
     * </div>
     * <p>
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("This set can only be modified by the backing service registry");
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public Object[] toArray() {
        return entries.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return entries.toArray(a);
    }

    @Override
    public E addingService(ServiceReference<E> reference) {
        E service = context.getService(reference);
        Integer ranking = (Integer) reference.getProperty(Constants.SERVICE_RANKING);
        if (ranking != null) {
            serviceRankings.put(service, ranking);
        }
        Long id = (Long) reference.getProperty(Constants.SERVICE_ID);
        serviceIds.put(service, id);
        entries.add(service);
        empty = false;
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<E> reference, E service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<E> reference, E service) {
        empty = entries.isEmpty();
        entries.remove(service);
        serviceRankings.remove(service);
        serviceIds.remove(service);
        context.ungetService(reference);
    }

    @Override
    public E ceiling(E service) {
        return entries.ceiling(service);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return entries.descendingIterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return entries.descendingSet();
    }

    @Override
    public E floor(E service) {
        return entries.floor(service);
    }

    @Override
    public SortedSet<E> headSet(E service) {
        return entries.headSet(service);
    }

    @Override
    public NavigableSet<E> headSet(E service, boolean inclusive) {
        return entries.headSet(service, inclusive);
    }

    @Override
    public E higher(E service) {
        return entries.higher(service);
    }

    @Override
    public Iterator<E> iterator() {
        return entries.iterator();
    }

    @Override
    public E lower(E service) {
        return entries.lower(service);
    }

    /**
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Not supported and therefore throws an {@code UnsupportedOperationException}.
     * </div>
     * <p>
     */
    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("This set can only be modified by the backing service registry");
    }

    /**
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Not supported and therefore throws an {@code UnsupportedOperationException}.
     * </div>
     * <p>
     */
    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("This set can only be modified by the backing service registry");
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return entries.subSet(fromElement, toElement);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return entries.subSet(fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public SortedSet<E> tailSet(E service) {
        return entries.tailSet(service);
    }

    @Override
    public NavigableSet<E> tailSet(E service, boolean inclusive) {
        return entries.tailSet(service, inclusive);
    }

}
