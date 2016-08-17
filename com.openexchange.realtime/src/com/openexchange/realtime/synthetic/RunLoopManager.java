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

package com.openexchange.realtime.synthetic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.Validate;
import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.openexchange.management.ManagementAware;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.LoadFactorCalculator;
import com.openexchange.realtime.management.RunLoopManagerMBean;
import com.openexchange.realtime.management.RunLoopManagerManagement;
import com.openexchange.realtime.packet.ID;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link RunLoopManager} -
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.2
 */
public class RunLoopManager implements ManagementAware<RunLoopManagerMBean>, LoadFactorCalculator {

    private static interface LoadBalancer {

        int nextInt(int max);
    }

    private static class RoundRobinLoadBalancer implements LoadBalancer {

        private final AtomicInteger count;

        /**
         * Initializes a new {@link RunLoopManager.RoundRobinLoadBalancer}.
         */
        RoundRobinLoadBalancer() {
            super();
            count = new AtomicInteger();
        }

        @Override
        public int nextInt(int max) {
            int cur;
            int next;
            do {
                cur = count.get();
                next = cur + 1;
                if (next < 0) {
                    next = 0;
                }
            } while (!count.compareAndSet(cur, next));
            return next % max;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    /**
     * Middle naming part of the managed RunLoops
     */
    public static final String LOOP_NAMING_INFIX = "-handler-";

    /**
     * Keep associations from component ids to distinct clusters of runloops for given ids.
     *
     * <pre>
     * "text" -> TextLoop1
     * "text" -> TextLoop2
     * ...
     * "calc" -> CalcLoop1
     * ...
     * </pre>
     */
    private final ListMultimap<String, SyntheticChannelRunLoop> loopClusters = ArrayListMultimap.create();

    /**
     * Keeps associations from componenthandles to runloops from the distinct loopCluster of the component that created that componenthandle
     *
     * <pre>
     * textdocument1 -> TextLoop1
     * textdocument1 -> TextLoop5
     * ...
     * calcdocument1 -> CalcLoop4
     * <pre>
     */
    private final ConcurrentHashMap<ID, SyntheticChannelRunLoop> loopMap = new ConcurrentHashMap<ID, SyntheticChannelRunLoop>(16, 0.9F, 1);

    private final ServiceLookup services;

    private final LoadBalancer loadBalancer;

    private final RunLoopManagerManagement runLoopManagerManagement;

    /**
     * Initializes a new {@link RunLoopManager}.
     *
     * @param services The {@link ServiceLookup} instance to use.
     */
    public RunLoopManager(ServiceLookup services) {
        this.services = services;
        loadBalancer = new RoundRobinLoadBalancer();
        this.runLoopManagerManagement = new RunLoopManagerManagement(this);
    }

    /**
     * Creates a new set of {@link SyntheticChannelRunLoop}s for the given {@link Component} if they don't already exist and starts them.
     *
     * @param component The component that needs a new set of {@link SyntheticChannelRunLoop}s to feed {@link ComponentHandle}s synchronously.
     * @param quantity The number of {@link SyntheticChannelRunLoop}s to create.
     */
    public synchronized void createRunLoops(Component component, int quantity) {
        ExecutorService executor = services.getService(ThreadPoolService.class).getExecutor();
        String componentId = component.getId();
        if (!loopClusters.containsKey(componentId)) {
            for (int i = 0; i < Math.abs(quantity); i++) {
                String loopName = componentId + LOOP_NAMING_INFIX + i;
                SyntheticChannelRunLoop newLoop = new SyntheticChannelRunLoop(loopName);
                loopClusters.put(componentId, newLoop);
                executor.execute(newLoop);
            }
        }
    }

    /**
     * Gets the {@link SyntheticChannelRunLoop} associated with the given <code>handleId</code>.
     *
     * @param handleId The {@link ID} of a {@link ComponentHandle} that is or should be fed via a {@link SyntheticChannelRunLoop}
     * @return <code>Optional.absent</code> if no {@link SyntheticChannelRunLoop} is associated with the given <code>handleId</code> ,
     *         otherwise the associated {@link SyntheticChannelRunLoop} is returned as an Optional
     */
    public Optional<SyntheticChannelRunLoop> getRunLoopForID(ID handleId) {
        return getRunLoopForID(handleId, false);
    }

    /**
     * Gets the {@link SyntheticChannelRunLoop} associated with the given <code>handleId</code>. If there is no association and
     * <code>associateIfMissing</code> is true a random {@link SyntheticChannelRunLoop} is associated with the given <code>handleId</code>.
     * The {@link SyntheticChannelRunLoop} is picked from the distinct cluster of {@link SyntheticChannelRunLoop}s that is associated with
     * the {@link Component} that created the given <code>handleId</code>.
     *
     * @param handleId The {@link ID} of a {@link ComponentHandle} that is or should be fed via a {@link SyntheticChannelRunLoop}
     * @param associateIfMissing If we are allowed to create new associations
     * @return <code>Optional.absent</code> if no {@link SyntheticChannelRunLoop} is associated with the given <code>handleId</code> and
     *         <code>associateIfMissing</code> is false. Otherwise the associated {@link SyntheticChannelRunLoop} is returned within an Optional
     */
    public Optional<SyntheticChannelRunLoop> getRunLoopForID(ID handleId, boolean associateIfMissing) {
        SyntheticChannelRunLoop runLoop = loopMap.get(handleId);
        if (runLoop == null && associateIfMissing) {
            List<SyntheticChannelRunLoop> list = loopClusters.get(handleId.getComponent());
            SyntheticChannelRunLoop nextRunLoop = list.get(loadBalancer.nextInt(list.size()));
            runLoop = loopMap.putIfAbsent(handleId, nextRunLoop);
            if (null == runLoop) {
                runLoop = nextRunLoop;
            }
        }
        return Optional.fromNullable(runLoop);
    }

    /**
     * Remove association of {@link ComponentHandle} -> {@link SyntheticChannelRunLoop} for the {@link ComponentHandle} with the given
     * <code>handleId</code>
     *
     * @param handleId The id of the {@link ComponentHandle}
     * @returns Optional.absent if no {@link SyntheticChannelRunLoop} was associated with the given handleID, the
     * {@link SyntheticChannelRunLoop} wrapped in an Optional otherwise
     */
    public Optional<SyntheticChannelRunLoop> removeIDFromRunLoop(ID handleId) {
        return Optional.fromNullable(loopMap.remove(handleId));
    }

    /**
     * Destroys all {@link SyntheticChannelRunLoop} mappings that are currently managed. Doing a complete cleanup.
     *
     * @param component The component whose set of {@link SyntheticChannelRunLoop}s should be removed.
     */
    public void destroyRunLoops() {
        //new set as the underlying cluster map will be updated from the loop
        HashSet<String> components = new HashSet<String>(loopClusters.keySet());
        for(String componentId : components) {
            destroyRunLoops(componentId);
        }
    }

    /**
     * Destroys a set of {@link SyntheticChannelRunLoop}s for the given {@link Component}.
     *
     * @param component The component whose set of {@link SyntheticChannelRunLoop}s should be removed.
     */
    public void destroyRunLoops(Component component) {
        Validate.notNull(component);
        destroyRunLoops(component.getId());
    }

    /**
     * Destroys a set of {@link SyntheticChannelRunLoop}s for the given <code>componentId</code>. This removes the
     * {@link SyntheticChannelRunLoop}s from the loop cluster per <code>componentId</code> id and from the loop mapping from
     * {@link ComponentHandle} to {@link SyntheticChannelRunLoop}.
     *
     * @param componentId The <code>componentId</code> of the {@link Component} whose set of {@link SyntheticChannelRunLoop}s should be
     * removed.
     */
    public void destroyRunLoops(String componentId) {
        Validate.notEmpty(componentId);
        List<SyntheticChannelRunLoop> removedLoops = loopClusters.removeAll(componentId);
        for(SyntheticChannelRunLoop removedLoop : removedLoops) {
            removedLoop.stop();
            for( Entry<ID, SyntheticChannelRunLoop> entry : loopMap.entrySet() ) {
                ID handleId = entry.getKey();
                SyntheticChannelRunLoop runLoop = entry.getValue();
                if(removedLoop.equals(runLoop)) {
                    loopMap.remove(handleId);
                }
            }
        }
    }

    /**
     * Get the number of {@link ComponentHandle}s being mapped by this {@link RunLoopManager} for a specific {@link Component}.
     *
     * @param component The {@link Component} that created the {@link ComponentHandle}s
     * @return The number of handles being mapped by the {@link RunLoopManager}
     */
    public int getNumberOfHandlesInCluster(Component component) throws Exception {
        Validate.notNull(component);
        int count=0;
        List<SyntheticChannelRunLoop> loopCluster = loopClusters.get(component.getId());
        for (Entry<ID, SyntheticChannelRunLoop> entry : loopMap.entrySet()) {
            if(loopCluster.contains(entry.getValue())) {
                count++;
            }
        }
        return count;
    }


    /**
     * Get the <code>ids</code> of {@link Component}s that are managed by this instance
     * @return A Set of <code>ids</code> of {@link Component}s that are managed by this instance
     */
    public Set<String> getManagedComponents() {
        return Collections.unmodifiableSet(loopClusters.keySet());
    }

    /**
     * Get the {@link ComponentHandle} to {@link SyntheticChannelRunLoop} mappings for a given component
     * @param componentId The ID of the {@link Component}
     * @return the {@link ComponentHandle} to {@link SyntheticChannelRunLoop} mappings for a given component
     */
    public List<Entry<ID, SyntheticChannelRunLoop>> getHandlesInCluster(String componentId) {
        Validate.notEmpty(componentId);
        List<Entry<ID,SyntheticChannelRunLoop>> handlesInCluster = new ArrayList<Entry<ID,SyntheticChannelRunLoop>>();
        List<SyntheticChannelRunLoop> loopCluster = loopClusters.get(componentId);
        for (Entry<ID, SyntheticChannelRunLoop> entry : loopMap.entrySet()) {
            if(loopCluster.contains(entry.getValue())) {
                handlesInCluster.add(entry);
            }
        }
        return handlesInCluster;
    }

    /**
     * Get a view of the loop clusters.
     *
     * @return a view of the loop clusters
     */
    public Collection<SyntheticChannelRunLoop> getRunLoopView() {
        return loopClusters.values();

    }

    /**
     * Get a view of the loop clusters per component.
     *
     * @return a readonly view of the loop clusters
     */
    public Map<String, Collection<SyntheticChannelRunLoop>> getRunLoopsPerComponent() {
        return loopClusters.asMap();
    }

    @Override
    public ManagementObject<RunLoopManagerMBean> getManagementObject() {
        return runLoopManagerManagement;
    }

    @Override
    public float getCurrentLoad(Component component) {
        Collection<SyntheticChannelRunLoop> runLoops = getRunLoopsPerComponent().get(component.getId());
        int runLoopCount = runLoops.size();
        long sum = 0;
        for (SyntheticChannelRunLoop syntheticChannelRunLoop : runLoops) {
            sum += syntheticChannelRunLoop.getQueueSize();
        }
        return ((float)sum/(float)runLoopCount);
    }

    @Override
    public int getRunLoopCount(Component component) {
        Collection<SyntheticChannelRunLoop> runLoops = getRunLoopsPerComponent().get(component.getId());
        return runLoops.size();
    }
}
