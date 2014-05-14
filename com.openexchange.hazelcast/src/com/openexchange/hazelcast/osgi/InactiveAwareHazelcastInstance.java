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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.hazelcast.osgi;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import com.hazelcast.config.Config;
import com.hazelcast.core.ClientService;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IList;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.PartitionService;
import com.hazelcast.logging.LoggingService;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionException;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalTask;

/**
 * {@link InactiveAwareHazelcastInstance}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class InactiveAwareHazelcastInstance implements HazelcastInstance {

    private final HazelcastInstance hazelcastInstance;
    private final Unregisterer unregisterer;

    /**
     * Initializes a new {@link InactiveAwareHazelcastInstance}.
     */
    public InactiveAwareHazelcastInstance(final HazelcastInstance hazelcastInstance, final Unregisterer unregisterer) {
        super();
        this.unregisterer = unregisterer;
        this.hazelcastInstance = hazelcastInstance;
    }

    private HazelcastInstanceNotActiveException handleNotActiveException(HazelcastInstanceNotActiveException e) {
        unregisterer.unregisterHazelcastInstance();
        return e;
    }

    @Override
    public String getName() {
        return hazelcastInstance.getName();
    }

    @Override
    public <E> IQueue<E> getQueue(String name) {
        try {
            return hazelcastInstance.getQueue(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public <E> ITopic<E> getTopic(String name) {
        try {
            return hazelcastInstance.getTopic(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public <E> ISet<E> getSet(String name) {
        try {
            return hazelcastInstance.getSet(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public <E> IList<E> getList(String name) {
        try {
            return hazelcastInstance.getList(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public <K, V> IMap<K, V> getMap(String name) {
        try {
            return hazelcastInstance.getMap(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public <K, V> MultiMap<K, V> getMultiMap(String name) {
        try {
            return hazelcastInstance.getMultiMap(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public ILock getLock(String key) {
        try {
            return hazelcastInstance.getLock(key);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public ILock getLock(Object key) {
        try {
            return hazelcastInstance.getLock(key);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public Cluster getCluster() {
        try {
            return hazelcastInstance.getCluster();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public IExecutorService getExecutorService(String name) {
        try {
            return hazelcastInstance.getExecutorService(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public <T> T executeTransaction(TransactionalTask<T> task) throws TransactionException {
        try {
            return hazelcastInstance.executeTransaction(task);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public <T> T executeTransaction(TransactionOptions options, TransactionalTask<T> task) throws TransactionException {
        try {
            return hazelcastInstance.executeTransaction(options, task);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public TransactionContext newTransactionContext() {
        try {
            return hazelcastInstance.newTransactionContext();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public TransactionContext newTransactionContext(TransactionOptions options) {
        try {
            return hazelcastInstance.newTransactionContext(options);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public IdGenerator getIdGenerator(String name) {
        try {
            return hazelcastInstance.getIdGenerator(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public IAtomicLong getAtomicLong(String name) {
        try {
            return hazelcastInstance.getAtomicLong(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public ICountDownLatch getCountDownLatch(String name) {
        try {
            return hazelcastInstance.getCountDownLatch(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public ISemaphore getSemaphore(String name) {
        try {
            return hazelcastInstance.getSemaphore(name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public Collection<DistributedObject> getDistributedObjects() {
        try {
            return hazelcastInstance.getDistributedObjects();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public String addDistributedObjectListener(DistributedObjectListener distributedObjectListener) {
        try {
            return hazelcastInstance.addDistributedObjectListener(distributedObjectListener);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public boolean removeDistributedObjectListener(String registrationId) {
        try {
            return hazelcastInstance.removeDistributedObjectListener(registrationId);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public Config getConfig() {
        try {
            return hazelcastInstance.getConfig();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public PartitionService getPartitionService() {
        try {
            return hazelcastInstance.getPartitionService();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public ClientService getClientService() {
        try {
            return hazelcastInstance.getClientService();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public LoggingService getLoggingService() {
        try {
            return hazelcastInstance.getLoggingService();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public LifecycleService getLifecycleService() {
        try {
            return hazelcastInstance.getLifecycleService();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public <T extends DistributedObject> T getDistributedObject(String serviceName, Object id) {
        try {
            return hazelcastInstance.getDistributedObject(serviceName, id);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public <T extends DistributedObject> T getDistributedObject(String serviceName, String name) {
        try {
            return hazelcastInstance.getDistributedObject(serviceName, name);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public ConcurrentMap<String, Object> getUserContext() {
        try {
            return hazelcastInstance.getUserContext();
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public void shutdown() {
        hazelcastInstance.shutdown();
    }

}
