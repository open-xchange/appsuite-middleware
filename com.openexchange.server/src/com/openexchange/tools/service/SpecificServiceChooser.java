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

package com.openexchange.tools.service;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * {@link SpecificServiceChooser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SpecificServiceChooser<T> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpecificServiceChooser.class);

    private final SortedSet<WeightedRegistration<T>> general = new TreeSet<WeightedRegistration<T>>();

    private final TIntObjectMap<SortedSet<WeightedRegistration<T>>> contextSpecific = new TIntObjectHashMap<SortedSet<WeightedRegistration<T>>>();

    private final Map<String, SortedSet<WeightedRegistration<T>>> folderSpecific = new HashMap<String, SortedSet<WeightedRegistration<T>>>();

    private final TIntObjectMap<Map<String, SortedSet<WeightedRegistration<T>>>> contextAndFolderSpecific = new TIntObjectHashMap<Map<String, SortedSet<WeightedRegistration<T>>>>();

    public SpecificServiceChooser() {
        super();
    }

    public synchronized void registerForEverything(final T serviceInstance, final int ranking) throws ServicePriorityConflictException {
        add(general, serviceInstance, ranking);
    }

    public synchronized void removeForEverything(final T serviceInstance) {
        remove(general, serviceInstance);
    }

    private void remove(final SortedSet<WeightedRegistration<T>> set, final T serviceInstance) {
        if(set == null) {
            return;
        }
        final Set<WeightedRegistration<T>> remove = new HashSet<WeightedRegistration<T>>();
        for (final WeightedRegistration<T> weightedRegistration : set) {
            if(weightedRegistration.payload.equals(serviceInstance)) {
                remove.add(weightedRegistration);
            }
        }
        set.removeAll(remove);
    }

    private void add(final SortedSet<WeightedRegistration<T>> set, final T serviceInstance, final int ranking) throws ServicePriorityConflictException {
        notNull(serviceInstance);
        final WeightedRegistration<T> newRegistration = new WeightedRegistration<T>(ranking);
        newRegistration.payload = serviceInstance;
        if(!set.add(newRegistration)) {
            throw new ServicePriorityConflictException();
        }
    }

    private void notNull(final T serviceInstance) {
        if(serviceInstance == null) {
            LOG.error("Trying to register Null Service!");
            throw new NullPointerException("Service Instance may not be null!");
        }
    }

    public synchronized void registerForContext(final T serviceInstance, final int ranking, final int cid) throws ServicePriorityConflictException {
        if (!contextSpecific.containsKey(cid)) {
            contextSpecific.put(cid, new TreeSet<WeightedRegistration<T>>());
        }
        add(contextSpecific.get(cid), serviceInstance, ranking);
    }

    public synchronized void removeForContext(final T serviceInstance, final int cid) {
        if (contextSpecific.containsKey(cid)) {
            remove(contextSpecific.get(cid), serviceInstance);
        }
    }

    public synchronized void registerForContextAndFolder(final T serviceInstance, final int ranking, final int cid, final int folderId) throws ServicePriorityConflictException {
        registerForContextAndFolder(serviceInstance, ranking, cid, String.valueOf(folderId));
    }

    public synchronized void removeForContextAndFolder(final T serviceInstance, final int cid, final int folderId) {
        removeForContextAndFolder(serviceInstance, cid, String.valueOf(folderId));
    }

    public synchronized void registerForContextAndFolder(final T serviceInstance, final int ranking, final int cid, final String folderId) throws ServicePriorityConflictException {
        Map<String, SortedSet<WeightedRegistration<T>>> folderSpecific = contextAndFolderSpecific.get(cid);
        if (folderSpecific == null) {
            folderSpecific = new HashMap<String, SortedSet<WeightedRegistration<T>>>();
            contextAndFolderSpecific.put(cid, folderSpecific);
        }
        if (!folderSpecific.containsKey(folderId)) {
            folderSpecific.put(folderId, new TreeSet<WeightedRegistration<T>>());
        }

        add( folderSpecific.get(folderId), serviceInstance, ranking);
    }

    public synchronized void removeForContextAndFolder(final T serviceInstance, final int cid, final String folderId) {
        final Map<String, SortedSet<WeightedRegistration<T>>> folderSpecific = contextAndFolderSpecific.get(cid);
        if(folderSpecific == null) {
            return;
        }
        final SortedSet<WeightedRegistration<T>> registeredServices = folderSpecific.get(folderId);
        remove(registeredServices, serviceInstance);
    }

    public void registerForFolder(final T serviceInstance, final int ranking, final int folderId) throws ServicePriorityConflictException {
        registerForFolder(serviceInstance, ranking, String.valueOf(folderId));
    }

    public synchronized void registerForFolder(final T serviceInstance, final int ranking, final String folderId) throws ServicePriorityConflictException {
        if (!folderSpecific.containsKey(folderId)) {
            folderSpecific.put(folderId, new TreeSet<WeightedRegistration<T>>());
        }
        add(folderSpecific.get(folderId), serviceInstance, ranking);
    }

    public void removeForFolder(final T serviceInstance, final int folderId) {
        removeForFolder(serviceInstance, String.valueOf(folderId));
    }

    public synchronized void removeForFolder(final T serviceInstance, final String folderId) {
        remove (folderSpecific.get(folderId), serviceInstance);
    }

    public T choose(final int cid, final int folderId) throws ServicePriorityConflictException {
        return choose(cid, String.valueOf(folderId));
    }

    public T choose(final int cid, final String folderId) throws ServicePriorityConflictException {
        final T service = tryFolderSpecific(cid, folderId);
        if (service != null) {
            return service;
        }

        final WeightedRegistration<T> contextSpecific = tryContextSpecific(cid);
        final WeightedRegistration<T> folderSpecific = tryFolderSpecific(folderId);

        if (contextSpecific != null || folderSpecific != null) {
            return bestMatch(contextSpecific, folderSpecific);
        }

        if (general.isEmpty()) {
            return null;
        }
        return general.first().getPayload();
    }

    private T bestMatch(final WeightedRegistration<T> reg1, final WeightedRegistration<T> reg2) throws ServicePriorityConflictException {
        if (reg1 == null && reg2 == null) {
            return null;
        }
        if (reg1 == null) {
            return reg2.getPayload();
        }

        if (reg2 == null) {
            return reg1.getPayload();
        }

        if (reg1.ranking > reg2.ranking) {
            return reg1.getPayload();
        }

        if (reg2.ranking > reg1.ranking) {
            return reg2.getPayload();
        }

        if (reg1.ranking == reg2.ranking) {
            throw new ServicePriorityConflictException();
        }

        return null;
    }

    private T tryFolderSpecific(final int cid, final String folderId) {
        final Map<String, SortedSet<WeightedRegistration<T>>> folderSpecific = contextAndFolderSpecific.get(cid);
        if (folderSpecific == null) {
            return null;
        }
        if (folderSpecific.containsKey(folderId)) {
            return folderSpecific.get(folderId).first().getPayload();
        }
        return null;
    }

    private WeightedRegistration<T> tryContextSpecific(final int cid) {
        if (contextSpecific.containsKey(cid) && !contextSpecific.get(cid).isEmpty()) {
            return contextSpecific.get(cid).first();
        }
        return null;
    }

    private WeightedRegistration<T> tryFolderSpecific(final String folderId) {
        if (folderSpecific.containsKey(folderId)) {
            return folderSpecific.get(folderId).first();
        }
        return null;
    }

    private static final class WeightedRegistration<T> implements Comparable<WeightedRegistration<T>> {

        public T payload = null;

        public int ranking;

        public WeightedRegistration(final int ranking) {
            this.ranking = ranking;
        }

        @Override
        public int compareTo(final WeightedRegistration<T> o) {
            return o.ranking - ranking;
        }

        @Override
        public int hashCode() {
            return ranking;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WeightedRegistration)) {
                return false;
            }
            return ranking == ((WeightedRegistration<T>)o).ranking;
        }

        public T getPayload() {
            return payload;
        }

    }

}
