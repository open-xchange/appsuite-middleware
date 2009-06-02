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

package com.openexchange.tools.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link SpecificServiceChooser}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SpecificServiceChooser<T> {
    
    private static final Log LOG = LogFactory.getLog(SpecificServiceChooser.class);
    
    private SortedSet<WeightedRegistration<T>> general = new TreeSet<WeightedRegistration<T>>();

    private Map<Integer, SortedSet<WeightedRegistration<T>>> contextSpecific = new HashMap<Integer, SortedSet<WeightedRegistration<T>>>();

    private Map<String, SortedSet<WeightedRegistration<T>>> folderSpecific = new HashMap<String, SortedSet<WeightedRegistration<T>>>();

    private Map<Integer, Map<String, SortedSet<WeightedRegistration<T>>>> contextAndFolderSpecific = new HashMap<Integer, Map<String, SortedSet<WeightedRegistration<T>>>>();

    public SpecificServiceChooser() {
        super();

    }

    public synchronized void registerForEverything(T serviceInstance, int ranking) throws ServicePriorityConflictException {
        add(general, serviceInstance, ranking);
    }

    public synchronized void removeForEverything(T serviceInstance) {
        remove(general, serviceInstance);
    }

    private void remove(SortedSet<WeightedRegistration<T>> set, T serviceInstance) {
        if(set == null) {
            return;
        }
        Set<WeightedRegistration<T>> remove = new HashSet<WeightedRegistration<T>>();
        for (WeightedRegistration<T> weightedRegistration : set) {
            if(weightedRegistration.payload.equals(serviceInstance)) {
                remove.add(weightedRegistration);
            }
        }
        set.removeAll(remove);
    }
    
    private void add(SortedSet<WeightedRegistration<T>> set, T serviceInstance, int ranking) throws ServicePriorityConflictException {
        notNull(serviceInstance);
        WeightedRegistration<T> newRegistration = new WeightedRegistration<T>(ranking);
        newRegistration.payload = serviceInstance;
        if(!set.add(newRegistration)) {
            throw new ServicePriorityConflictException();
        }
    }

    private void notNull(T serviceInstance) {
        if(serviceInstance == null) {
            LOG.fatal("Trying to register Null Service!");
            throw new NullPointerException("Service Instance may not be null!");
        }
    }

    public synchronized void registerForContext(T serviceInstance, int ranking, int cid) throws ServicePriorityConflictException {
        if (!contextSpecific.containsKey(cid)) {
            contextSpecific.put(cid, new TreeSet<WeightedRegistration<T>>());
        }
        add(contextSpecific.get(cid), serviceInstance, ranking);
    }

    public synchronized void removeForContext(T serviceInstance, int cid) {
        if (contextSpecific.containsKey(cid)) {
            remove(contextSpecific.get(cid), serviceInstance);
        }
    }

    public synchronized void registerForContextAndFolder(T serviceInstance, int ranking, int cid, int folderId) throws ServicePriorityConflictException {
        registerForContextAndFolder(serviceInstance, ranking, cid, String.valueOf(folderId));
    }
    
    public synchronized void removeForContextAndFolder(T serviceInstance, int cid, int folderId) {
        removeForContextAndFolder(serviceInstance, cid, String.valueOf(folderId));
    }

    public synchronized void registerForContextAndFolder(T serviceInstance, int ranking, int cid, String folderId) throws ServicePriorityConflictException {
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

    public synchronized void removeForContextAndFolder(T serviceInstance, int cid, String folderId) {
        Map<String, SortedSet<WeightedRegistration<T>>> folderSpecific = contextAndFolderSpecific.get(cid);
        if(folderSpecific == null) {
            return;
        }
        SortedSet<WeightedRegistration<T>> registeredServices = folderSpecific.get(folderId);
        remove(registeredServices, serviceInstance);
    }
    
    public void registerForFolder(T serviceInstance, int ranking, int folderId) throws ServicePriorityConflictException {
        registerForFolder(serviceInstance, ranking, String.valueOf(folderId));
    }

    public synchronized void registerForFolder(T serviceInstance, int ranking, String folderId) throws ServicePriorityConflictException {
        if (!folderSpecific.containsKey(folderId)) {
            folderSpecific.put(folderId, new TreeSet<WeightedRegistration<T>>());
        }
        add(folderSpecific.get(folderId), serviceInstance, ranking);
    }
    
    public void removeForFolder(T serviceInstance, int folderId) {
        removeForFolder(serviceInstance, String.valueOf(folderId));
    }
    
    public synchronized void removeForFolder(T serviceInstance, String folderId) {
        remove (folderSpecific.get(folderId), serviceInstance);
    }

    public T choose(int cid, int folderId) throws ServicePriorityConflictException {
        return choose(cid, String.valueOf(folderId));
    }

    public T choose(int cid, String folderId) throws ServicePriorityConflictException {
        T service = tryFolderSpecific(cid, folderId);
        if (service != null) {
            return service;
        }

        WeightedRegistration<T> contextSpecific = tryContextSpecific(cid);
        WeightedRegistration<T> folderSpecific = tryFolderSpecific(folderId);

        if (contextSpecific != null || folderSpecific != null) {
            return bestMatch(contextSpecific, folderSpecific);
        }

        if (general.isEmpty()) {
            return null;
        }
        return general.first().getPayload();
    }

    private T bestMatch(WeightedRegistration<T> reg1, WeightedRegistration<T> reg2) throws ServicePriorityConflictException {
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

    private T tryFolderSpecific(int cid, String folderId) {
        Map<String, SortedSet<WeightedRegistration<T>>> folderSpecific = contextAndFolderSpecific.get(cid);
        if (folderSpecific == null) {
            return null;
        }
        if (folderSpecific.containsKey(folderId)) {
            return folderSpecific.get(folderId).first().getPayload();
        }
        return null;
    }

    private WeightedRegistration<T> tryContextSpecific(int cid) {
        if (contextSpecific.containsKey(cid) && !contextSpecific.get(cid).isEmpty()) {
            return contextSpecific.get(cid).first();
        }
        return null;
    }

    private WeightedRegistration<T> tryFolderSpecific(String folderId) {
        if (folderSpecific.containsKey(folderId)) {
            return folderSpecific.get(folderId).first();
        }
        return null;
    }

    private static final class WeightedRegistration<T> implements Comparable<WeightedRegistration<T>> {

        public T payload = null;

        public int ranking;

        public WeightedRegistration(int ranking) {
            this.ranking = ranking;
        }

        public int compareTo(WeightedRegistration<T> o) {
            return o.ranking - ranking;
        }

        public int hashCode() {
            return ranking;
        }
        
        public boolean equals(Object o) {
            return ranking == ((WeightedRegistration<T>)o).ranking;
        }
        
        public T getPayload() {
            return payload;
        }

    }

}
