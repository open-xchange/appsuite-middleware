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

package com.openexchange.push.impl.balancing;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.openexchange.push.PushManagerExtendedService;
import com.openexchange.push.PushUser;
import com.openexchange.push.impl.PushManagerRegistry;


/**
 * {@link PermanentListenerRescheduler} - Reschedules permanent listeners equally among cluster nodes, whenever cluster members do change.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PermanentListenerRescheduler implements ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PermanentListenerRescheduler.class);

    private final PushManagerRegistry pushManagerRegistry;
    private final BundleContext context;
    private final AtomicReference<String> registrationIdRef;
    private final AtomicReference<HazelcastInstance> hzInstancerRef;

    /**
     * Initializes a new {@link PermanentListenerRescheduler}.
     */
    public PermanentListenerRescheduler(PushManagerRegistry pushManagerRegistry, BundleContext context) {
        super();
        hzInstancerRef = new AtomicReference<HazelcastInstance>();
        registrationIdRef = new AtomicReference<String>();
        this.pushManagerRegistry = pushManagerRegistry;
        this.context = context;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    /*
    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        reschedule(membershipEvent.getMembers(), hzInstancerRef.get());
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        reschedule(membershipEvent.getMembers(), hzInstancerRef.get());
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        // Don't care
    }
    */

    // -------------------------------------------------------------------------------------------------------------------------------

    /**
     * Reschedules permanent listeners among cluster members.
     */
    public void reschedule() {
        HazelcastInstance hzInstance = hzInstancerRef.get();
        if (null == hzInstance) {
            reschedule(null, null);
        } else {
            reschedule(hzInstance.getCluster().getMembers(), hzInstance);
        }
    }

    private void reschedule(Set<Member> allMembers, HazelcastInstance hzInstance) {
        synchronized (this) {
            try {
                // Determine push users
                List<PushUser> allPushUsers = pushManagerRegistry.getUsersWithPermanentListeners();
                if (allPushUsers.isEmpty()) {
                    // Nothing to do
                    return;
                }

                if (null == hzInstance) {
                    // No cluster members available. Start off listeners for all push users on this node.
                    applyPermanentListenersUsing(allPushUsers);
                    return;
                }

                // Get local member
                Member localMember = hzInstance.getCluster().getLocalMember();

                // Determine other cluster members
                Set<Member> otherMembers = getOtherMembers(allMembers, localMember);

                if (otherMembers.isEmpty()) {
                    // No other cluster members - assign all available permanent listeners to this node
                    applyPermanentListenersUsing(allPushUsers);
                    return;
                }

                // Identify capable members
                List<Member> candidates = new LinkedList<Member>();
                candidates.add(localMember);
                {
                    IExecutorService executor = hzInstance.getExecutorService("default");
                    Map<Member, Future<Boolean>> futureMap = executor.submitToMembers(new PortableCheckForExtendedServiceCallable(localMember.getUuid()), otherMembers);
                    for (Map.Entry<Member, Future<Boolean>> entry : futureMap.entrySet()) {
                        // Check Future's return value
                        Future<Boolean> future = entry.getValue();
                        if (future.get().booleanValue()) {
                            candidates.add(entry.getKey());
                            LOG.info("Cluster member {} has a {} running, hence considered for rescheduling computation.", entry.getKey(), PushManagerExtendedService.class.getSimpleName());
                        } else {
                            LOG.info("Cluster member {} has no {} running, hence ignored for rescheduling computation.", entry.getKey(), PushManagerExtendedService.class.getSimpleName());
                        }
                    }
                }

                // First, sort by UUID
                Collections.sort(candidates, new Comparator<Member>() {

                    @Override
                    public int compare(Member m1, Member m2) {
                        return m1.getUuid().compareTo(m2.getUuid());
                    }
                });

                // Determine the position of this cluster node
                int pos = 0;
                while (!localMember.getUuid().equals(candidates.get(pos).getUuid())) {
                    pos = pos + 1;
                }

                // Determine the permanent listeners for this node
                List<PushUser> ps = new LinkedList<PushUser>();
                int numMembers = candidates.size();
                int numPushUsers = allPushUsers.size();
                for (int i = 0; i < numPushUsers; i++) {
                    if ((i % numMembers) == pos) {
                        ps.add(allPushUsers.get(i));
                    }
                }

                // Apply newly calculated initial permanent listeners
                applyPermanentListenersUsing(ps);
            } catch (Exception e) {
                LOG.warn("Failed to distribute permanent listeners among cluster nodes", e);
            }
        } // End of synchronized block
    }

    private void applyPermanentListenersUsing(List<PushUser> pushUsers) {
        pushManagerRegistry.applyInitialListeners(pushUsers);
    }

    private Set<Member> getOtherMembers(Set<Member> allMembers, Member localMember) {
        Set<Member> otherMembers = new LinkedHashSet<Member>(allMembers);
        if (!otherMembers.remove(localMember)) {
            LOG.warn("Couldn't remove local member from cluster members.");
        }
        return otherMembers;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    @Override
    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
        HazelcastInstance hzInstance = context.getService(reference);
        try {
            Cluster cluster = hzInstance.getCluster();
            // String registrationId = cluster.addMembershipListener(this);
            // registrationIdRef.set(registrationId);

            hzInstancerRef.set(hzInstance);

            reschedule(cluster.getMembers(), hzInstance);

            return hzInstance;
        } catch (Exception e) {
            LOG.warn("Failed to distribute permanent listeners among cluster nodes", e);
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
        // Nothing
    }

    @Override
    public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
        String registrationId = registrationIdRef.get();
        if (null != registrationId) {
            hzInstance.getCluster().removeMembershipListener(registrationId);
            registrationIdRef.set(null);
        }

        hzInstancerRef.set(null);
        context.ungetService(reference);
    }

}
