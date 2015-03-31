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

package com.openexchange.push.impl.osgi;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.openexchange.push.PushUser;
import com.openexchange.push.impl.PushManagerRegistry;


/**
 * {@link PermanentListenerRescheduler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PermanentListenerRescheduler implements ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>, MembershipListener {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PermanentListenerRescheduler.class);

    private final PushManagerRegistry pushManagerRegistry;
    private final BundleContext context;
    private final AtomicReference<String> registrationIdRef;
    private final AtomicReference<Member> localMemberRef;

    /**
     * Initializes a new {@link PermanentListenerRescheduler}.
     */
    public PermanentListenerRescheduler(PushManagerRegistry pushManagerRegistry, BundleContext context) {
        super();
        localMemberRef = new AtomicReference<Member>();
        registrationIdRef = new AtomicReference<String>();
        this.pushManagerRegistry = pushManagerRegistry;
        this.context = context;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        reschedule(localMemberRef.get(), membershipEvent.getMembers());
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        reschedule(localMemberRef.get(), membershipEvent.getMembers());
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        // Don't care
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private void reschedule(Member localMember, Set<Member> allMembers) {
        try {
            List<PushUser> allPushUsers = pushManagerRegistry.getUsersWithPermanentListeners();
            if (false == allPushUsers.isEmpty()) {
                // Determine cluster members
                Set<Member> otherMembers = new HashSet<Member>(allMembers);
                if (!otherMembers.remove(localMember)) {
                    LOG.warn("Couldn't remove local member from cluster members.");
                }

                if (otherMembers.isEmpty()) {
                    // No other cluster members - assign all available permanent listeners to this node
                    pushManagerRegistry.applyInitialListeners(allPushUsers);
                } else {
                    // Otherwise equally distribute among available cluster nodes
                    // First, sort by UUID
                    List<Member> ms = new LinkedList<Member>(allMembers);
                    Collections.sort(ms, new Comparator<Member>() {

                        @Override
                        public int compare(Member m1, Member m2) {
                            return m1.getUuid().compareTo(m2.getUuid());
                        }
                    });

                    // Determine the position of this cluster node
                    int pos = 0;
                    while (!localMember.getUuid().equals(ms.get(pos).getUuid())) {
                        pos = pos + 1;
                    }

                    // Determine the permanent listeners for this node
                    List<PushUser> ps = new LinkedList<PushUser>();
                    int numMembers = ms.size();
                    int numPushUsers = allPushUsers.size();
                    for (int i = 0; i < numPushUsers; i++) {
                        if ((i % numMembers) == pos) {
                            ps.add(allPushUsers.get(i));
                        }
                    }
                    pushManagerRegistry.applyInitialListeners(ps);
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to distribute permanent listeners among cluster nodes", e);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    @Override
    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
        HazelcastInstance hzInstance = context.getService(reference);
        try {
            Cluster cluster = hzInstance.getCluster();
            String registrationId = cluster.addMembershipListener(this);
            registrationIdRef.set(registrationId);

            Member localMember = cluster.getLocalMember();
            localMemberRef.set(localMember);

            reschedule(localMember, cluster.getMembers());

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
        localMemberRef.set(null);

        String registrationId = registrationIdRef.get();
        if (null != registrationId) {
            hzInstance.getCluster().removeMembershipListener(registrationId);
            registrationIdRef.set(null);
        }

        context.ungetService(reference);
    }

}
