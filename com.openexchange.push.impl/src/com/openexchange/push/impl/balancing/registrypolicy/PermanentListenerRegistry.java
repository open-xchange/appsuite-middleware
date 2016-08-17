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

package com.openexchange.push.impl.balancing.registrypolicy;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.openexchange.concurrent.Blocker;
import com.openexchange.concurrent.ConcurrentBlocker;
import com.openexchange.push.PushUser;
import com.openexchange.push.impl.balancing.registrypolicy.portable.PortableOwner;
import com.openexchange.push.impl.portable.HazelcastInstanceNotActiveExceptionHandler;
import com.openexchange.push.impl.portable.PortablePushUser;


/**
 * {@link PermanentListenerRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PermanentListenerRegistry implements ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>, MembershipListener {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PermanentListenerRegistry.class);

    private final BundleContext context;
    private final ConcurrentMap<PushUser, Owner> localRegistry;
    private final AtomicReference<String> registrationIdRef;
    private final AtomicReference<HazelcastInstance> hzInstancerRef;
    private final HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler;
    private final Blocker blocker;
    private volatile String hzMapName;
    private volatile boolean useHzMap = false;

    /**
     * Initializes a new {@link PermanentListenerRegistry}.
     */
    public PermanentListenerRegistry(HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler, BundleContext context) {
        super();
        this.context = context;
        localRegistry = new ConcurrentHashMap<PushUser, Owner>(256);
        this.notActiveExceptionHandler = notActiveExceptionHandler;
        blocker = new ConcurrentBlocker();
        hzInstancerRef = new AtomicReference<HazelcastInstance>();
        registrationIdRef = new AtomicReference<String>();
    }

    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        LOG.warn("Encountered a {} error.", HazelcastInstanceNotActiveException.class.getSimpleName());
        changeBackingMapToLocalMap();

        HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler = this.notActiveExceptionHandler;
        if (null != notActiveExceptionHandler) {
            notActiveExceptionHandler.propagateNotActive(e);
        }
    }

    /**
     * Gets the Hazelcast map or <code>null</code> if unavailable.
     */
    private IMap<PortablePushUser, PortableOwner> hzMap(String mapIdentifier) {
        if (null == mapIdentifier) {
            LOG.trace("Name of Hazelcast map is missing for token login service.");
            return null;
        }
        HazelcastInstance hazelcastInstance = hzInstancerRef.get();
        if (hazelcastInstance == null) {
            LOG.trace("Hazelcast instance is not available.");
            return null;
        }
        try {
            return hazelcastInstance.getMap(mapIdentifier);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            return null;
        }
    }

    /**
     * Sets the name for the Hazelcast map.
     *
     * @param hzMapName The map name to set
     */
    public void setHzMapName(String hzMapName) {
        this.hzMapName = hzMapName;
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Puts the specified owner into this registry associated with given push user.
     * <p>
     * Existing entries are replaced.
     *
     * @param user The push user
     * @param newOwner The owner
     * @return The previous owner or <code>null</code>
     */
    public Owner putOwner(PushUser user, Owner newOwner) {
        blocker.acquire();
        try {
            return useHzMap ? putOwnerToHzMap(hzMapName, user, newOwner) : localRegistry.put(user, newOwner);
        } finally {
            blocker.release();
        }
    }

    private Owner putOwnerToHzMap(String mapIdentifier, PushUser user, Owner newOwner) {
        IMap<PortablePushUser, PortableOwner> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote credentials is not available.");
            return null;
        }

        PortableOwner portableOwner = hzMap.put(new PortablePushUser(user), new PortableOwner(newOwner));
        if (null == portableOwner) {
            return null;
        }
        return new Owner(portableOwner.getMember(), Reason.byOrdinal(portableOwner.getReason()));
    }

    /**
     * Gets the owner from this registry associated with given push user.
     *
     * @param user The associated push user
     * @return The owner or <code>null</code>
     */
    public Owner getOwner(PushUser user) {
        blocker.acquire();
        try {
            return useHzMap ? peekOwnerFromHzMap(hzMapName, user) : localRegistry.get(user);
        } finally {
            blocker.release();
        }
    }

    private Owner peekOwnerFromHzMap(String mapIdentifier, PushUser user) {
        IMap<PortablePushUser, PortableOwner> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote credentials is not available.");
            return null;
        }

        PortableOwner portableOwner = hzMap.get(new PortablePushUser(user));
        if (null == portableOwner) {
            return null;
        }
        return new Owner(portableOwner.getMember(), Reason.byOrdinal(portableOwner.getReason()));
    }

    /**
     * Removes the owner from this registry associated with given push user.
     *
     * @param user The associated push user
     * @return The removed owner or <code>null</code>
     */
    public Owner removeOwner(PushUser user) {
        blocker.acquire();
        try {
            return useHzMap ? pollOwnerFromHzMap(hzMapName, user) : localRegistry.remove(user);
        } finally {
            blocker.release();
        }
    }

    private Owner pollOwnerFromHzMap(String mapIdentifier, PushUser user) {
        IMap<PortablePushUser, PortableOwner> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote credentials is not available.");
            return null;
        }

        PortableOwner portableOwner = hzMap.remove(new PortablePushUser(user));
        if (null == portableOwner) {
            return null;
        }
        return new Owner(portableOwner.getMember(), Reason.byOrdinal(portableOwner.getReason()));
    }

    // ------------------------------------ MembershipListener methods ------------------------------------------------------------

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        // TODO Auto-generated method stub

    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        // TODO Auto-generated method stub

    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        // TODO Auto-generated method stub

    }

    // ----------------------------------- ServiceTrackerCustomizer methods -------------------------------------------------------

    @Override
    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
        HazelcastInstance hzInstance = context.getService(reference);
        try {
            Cluster cluster = hzInstance.getCluster();
            String registrationId = cluster.addMembershipListener(this);
            registrationIdRef.set(registrationId);
            hzInstancerRef.set(hzInstance);
            return hzInstance;
        } catch (Exception e) {
            LOG.warn("Failed to initialize {}", PermanentListenerRegistry.class.getSimpleName(), e);
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
        changeBackingMapToLocalMap();

        String registrationId = registrationIdRef.get();
        if (null != registrationId) {
            hzInstance.getCluster().removeMembershipListener(registrationId);
            registrationIdRef.set(null);
        }

        hzInstancerRef.set(null);
        context.ungetService(reference);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

   /**
    *
    */
    public void changeBackingMapToLocalMap() {
        blocker.block();
        try {
            // This happens if Hazelcast is removed in the meantime. We cannot copy any information back to the local map.
            useHzMap = false;
            LOG.info("Registry backing map changed to local");
        } finally {
            blocker.unblock();
        }
    }

   /**
    *
    */
    public void changeBackingMapToHz() {
        blocker.block();
        try {
            if (useHzMap) {
                return;
            }

            IMap<PortablePushUser, PortableOwner> hzMap = hzMap(hzMapName);
            if (null == hzMap) {
                LOG.trace("Hazelcast map is not available.");
            } else {
                // This MUST be synchronous!
                for (Entry<PushUser, Owner> entry : localRegistry.entrySet()) {
                    hzMap.put(new PortablePushUser(entry.getKey()), new PortableOwner(entry.getValue()));
                }
                localRegistry.clear();
            }
            useHzMap = true;
            LOG.info("Registry backing map changed to hazelcast");
        } finally {
            blocker.unblock();
        }
    }

}
