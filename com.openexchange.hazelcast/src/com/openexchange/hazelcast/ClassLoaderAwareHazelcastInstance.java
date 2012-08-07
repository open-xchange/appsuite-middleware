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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.hazelcast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import com.hazelcast.config.Config;
import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.ClientService;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IList;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.Instance;
import com.hazelcast.core.InstanceListener;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.Transaction;
import com.hazelcast.logging.LoggingService;
import com.hazelcast.partition.PartitionService;

/**
 * {@link ClassLoaderAwareHazelcastInstance} - A simple wrapper for a {@link HazelcastInstance} that {@link Log#warn(Object) logs a warning} if any
 * resource is accessed without an appropriate configuration available.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ClassLoaderAwareHazelcastInstance implements HazelcastInstance {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ClassLoaderAwareHazelcastInstance.class);

    private final HazelcastInstance hazelcastInstance;

    private final Config config;

    /**
     * Initializes a new {@link ClassLoaderAwareHazelcastInstance}.
     * 
     * @param hazelcastInstance
     */
    public ClassLoaderAwareHazelcastInstance(final HazelcastInstance hazelcastInstance) {
        super();
        this.hazelcastInstance = hazelcastInstance;
        config = hazelcastInstance.getConfig();
    }

    @Override
    public int hashCode() {
        return hazelcastInstance.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return hazelcastInstance.equals(obj);
    }

    @Override
    public String getName() {
        return hazelcastInstance.getName();
    }

    @Override
    public <E> IQueue<E> getQueue(final String name) {
        if (null == config.getQueueConfig(name)) {
            LOG.warn("No QueueConfig available for \"" + name + "\". Please provide appropriate QueueConfig prior to acquiring IQueue instance.");
        }
        return new ClassLoaderAwareIQueue(hazelcastInstance.getQueue(name));
    }

    @Override
    public <E> ITopic<E> getTopic(final String name) {
        if (null == config.getTopicConfig(name)) {
            LOG.warn("No TopicConfig available for \"" + name + "\". Please provide appropriate TopicConfig prior to acquiring ITopic instance.");
        }
        return new ClassLoaderAwareITopic(hazelcastInstance.getTopic(name));
    }

    @Override
    public <E> ISet<E> getSet(final String name) {
        return new ClassLoaderAwareISet(hazelcastInstance.getSet(name));
    }

    @Override
    public <E> IList<E> getList(final String name) {
        return new ClassLoaderAwareIList(hazelcastInstance.getList(name));
    }

    @Override
    public <K, V> IMap<K, V> getMap(final String name) {
        if (null == config.getMapConfig(name)) {
            LOG.warn("No MapConfig available for \"" + name + "\". Please provide appropriate MapConfig prior to acquiring IMap instance.");
        }
        return new ClassLoaderAwareIMap(hazelcastInstance.getMap(name));
    }

    @Override
    public <K, V> MultiMap<K, V> getMultiMap(final String name) {
        if (null == config.getMultiMapConfig(name)) {
            LOG.warn("No MultiMapConfig available for \"" + name + "\". Please provide appropriate MultiMapConfig prior to acquiring MultiMap instance.");
        }
        return new ClassLoaderAwareMultiMap(hazelcastInstance.getMultiMap(name));
    }

    @Override
    public ILock getLock(final Object key) {
        return hazelcastInstance.getLock(key);
    }

    @Override
    public Cluster getCluster() {
        return hazelcastInstance.getCluster();
    }

    @Override
    public ExecutorService getExecutorService() {
        return hazelcastInstance.getExecutorService();
    }

    @Override
    public ExecutorService getExecutorService(final String name) {
        if (null == config.getExecutorConfig(name)) {
            LOG.warn("No ExecutorConfig available for \"" + name + "\". Please provide appropriate ExecutorConfig prior to acquiring ExecutorService instance.");
        }
        return hazelcastInstance.getExecutorService(name);
    }

    @Override
    public Transaction getTransaction() {
        return hazelcastInstance.getTransaction();
    }

    @Override
    public IdGenerator getIdGenerator(final String name) {
        return hazelcastInstance.getIdGenerator(name);
    }

    @Override
    public AtomicNumber getAtomicNumber(final String name) {
        return hazelcastInstance.getAtomicNumber(name);
    }

    @Override
    public ICountDownLatch getCountDownLatch(final String name) {
        return hazelcastInstance.getCountDownLatch(name);
    }

    @Override
    public ISemaphore getSemaphore(final String name) {
        if (null == config.getSemaphoreConfig(name)) {
            LOG.warn("No SemaphoreConfig available for \"" + name + "\". Please provide appropriate SemaphoreConfig prior to acquiring ISemaphore instance.");
        }
        return hazelcastInstance.getSemaphore(name);
    }

    @Deprecated
    @Override
    public void shutdown() {
        hazelcastInstance.shutdown();
    }

    @Deprecated
    @Override
    public void restart() {
        hazelcastInstance.restart();
    }

    @Override
    public Collection<Instance> getInstances() {
        Collection<Instance> c = hazelcastInstance.getInstances();
        Collection<Instance> clone = new ArrayList<Instance>(c.size());
        for (final Instance inst : c) {
            if (inst instanceof IMap) {
                clone.add(new ClassLoaderAwareIMap((IMap) inst));
            } else if (inst instanceof MultiMap) {
                clone.add(new ClassLoaderAwareMultiMap((MultiMap) inst));
            } else if (inst instanceof IList) {
                clone.add(new ClassLoaderAwareIList((IList) inst));
            } else if (inst instanceof ISet) {
                clone.add(new ClassLoaderAwareISet((ISet) inst));
            } else if (inst instanceof ITopic) {
                clone.add(new ClassLoaderAwareITopic((ITopic) inst));
            } else if (inst instanceof IQueue) {
                clone.add(new ClassLoaderAwareIQueue((IQueue) inst));
            } else {
                clone.add(inst);
            }
        }
        return clone;
    }

    @Override
    public void addInstanceListener(final InstanceListener instanceListener) {
        hazelcastInstance.addInstanceListener(instanceListener);
    }

    @Override
    public void removeInstanceListener(final InstanceListener instanceListener) {
        hazelcastInstance.removeInstanceListener(instanceListener);
    }

    @Override
    public Config getConfig() {
        return hazelcastInstance.getConfig();
    }

    @Override
    public PartitionService getPartitionService() {
        return hazelcastInstance.getPartitionService();
    }

    @Override
    public ClientService getClientService() {
        return hazelcastInstance.getClientService();
    }

    @Override
    public LoggingService getLoggingService() {
        return hazelcastInstance.getLoggingService();
    }

    @Override
    public LifecycleService getLifecycleService() {
        return hazelcastInstance.getLifecycleService();
    }

}
