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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.Constants;
import com.google.common.base.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.Component.EvictionPolicy;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.RealtimeConfig;
import com.openexchange.realtime.cleanup.AbstractRealtimeJanitor;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanup;
import com.openexchange.realtime.cleanup.RealtimeCleanup;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.management.ManagementHouseKeeper;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.server.ServiceLookup;


/**
 * {@link SyntheticChannel}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
/*
 * @startuml doc-files/HandleIncomingStanza.png
 * (*) -> "Stanza arrives in
 * SyntheticChannel#send()"
 * if "Addressed ComponentHandle exists" then
 *   --> [true] "Update access time"
 *   --> "Find RunLoop assigned
 *   to ComponentHandle"
 *   --> "Offer Stanza to RunLoop"
 *   if "Stanza taken" then
 *   --> [true] (*)
 *   else
 *   --> [false] "Throw RealtimeException"
 *   --> (*)
 * endif
 * else
 *   --> [false] "Throw RealtimeException"
 *   --> (*)
 * endif
 * @enduml
 */

public class SyntheticChannel extends AbstractRealtimeJanitor implements Channel, Runnable {

    public static final String PROTOCOL = "synthetic";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyntheticChannel.class);

    private static final String CONJURELOCK = "SyntheticChannel.conjureLock";

    public static final AtomicReference<GlobalRealtimeCleanup> GLOBAL_CLEANUP_REF = new AtomicReference<GlobalRealtimeCleanup>();

    private static final Dictionary<String, Object> JANITOR_PROPERTIES = new Hashtable<String, Object>(1);

    private LocalRealtimeCleanup localRealtimeCleanup = null;

    // Registered Components (ComponentHandle, GroupDispatcher factories) mapped by name e.g. office -> ConnectionComponent
    private final ConcurrentHashMap<String, Component> components = new ConcurrentHashMap<String, Component>();

    // ComponentHandles created by the factories mapped by concrete ID e.q. "synthetic.office://operations/folderId.fileId" -> Connection
    private final ConcurrentHashMap<ID, ComponentHandle> handles = new ConcurrentHashMap<ID, ComponentHandle>();

    private final ConcurrentHashMap<ID, Long> lastAccess = new ConcurrentHashMap<ID, Long>();

    private final ConcurrentHashMap<ID, TimeoutEviction> timeouts = new ConcurrentHashMap<ID, TimeoutEviction>();

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    /*
     * Each @NotThreadSafe ComponentHandle is fed by a single RunLoop to guarantee singlethreaded Stanza delivery. RunLoops come from a
     * distinct cluster per Component that created the ComponentHandles. This way they can't influence each others performance.
     */
    private final RunLoopManager runLoopManager;

    public SyntheticChannel(ServiceLookup services, LocalRealtimeCleanup localRealtimeCleanup) {
        JANITOR_PROPERTIES.put(Constants.SERVICE_RANKING, RealtimeJanitor.RANKING_SYNTHETIC_CHANNEL);
        this.localRealtimeCleanup = localRealtimeCleanup;
        runLoopManager = new RunLoopManager(services);
        ManagementHouseKeeper.getInstance().addManagementObject(runLoopManager.getManagementObject());
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public boolean canHandle(Set<ElementPath> elementPaths, ID recipient) throws OXException {
        return true;
    }

    @Override
    public int getPriority() {
        return 20000;
    }

    @Override
    public boolean isConnected(ID id) throws OXException {
        if (shuttingDown.get()) {
            return false;
        }
        return handles.containsKey(id);
    }

    @Override
    public boolean conjure(ID id) throws RealtimeException {
        if (shuttingDown.get()) {
            return false;
        }

        id.lock(CONJURELOCK);
        try {
            ComponentHandle componentHandle = handles.get(id);
            if (componentHandle != null) {
                return true;
            }
            Component component = components.get(id.getComponent());
            if (component == null) {
                return false;
            }
            ComponentHandle handle = component.create(id);
            if (handle == null) {
                return false;
            }
            handles.put(id, handle);
            runLoopManager.getRunLoopForID(id, true);
            setUpEviction(component.getEvictionPolicy(), handle, id);
            return true;
        } finally {
            id.unlock(CONJURELOCK);
        }
    }

    protected void setUpEviction(EvictionPolicy evictionPolicy, ComponentHandle handle, ID id) {
        if (shuttingDown.get()) {
            return;
        }
        if (Component.Timeout.class.isInstance(evictionPolicy)) {
            Component.Timeout timeout = (Component.Timeout) evictionPolicy;
            timeouts.put(id, new TimeoutEviction(TimeUnit.MILLISECONDS.convert(timeout.getTimeout(), timeout.getUnit()), id));
        }
    }

    @Override
    public void send(final Stanza stanza, ID recipient) throws OXException {
        if (shuttingDown.get()) {
            stanza.trace("This server is shutting down. Discarding.");
            return;
        }

        stanza.trace("SyntheticChannel delivering to " + recipient);

        final ComponentHandle handle = handles.get(recipient);
        if (handle == null) {
            stanza.trace("Unknown recipient: " + stanza.getTo());
            throw RealtimeExceptionCodes.STANZA_RECIPIENT_UNAVAILABLE.create(stanza.getTo());
        }
        stanza.trace("Delivering to handle " + handle);

        stanza.trace("Updating last access");
        lastAccess.put(stanza.getTo(), System.currentTimeMillis());

        Optional<SyntheticChannelRunLoop> runLoopForID = runLoopManager.getRunLoopForID(recipient);
        if (!runLoopForID.isPresent()) {
            throw RealtimeExceptionCodes.STANZA_RECIPIENT_UNAVAILABLE.create(stanza.getTo());
        }

        final boolean taken = runLoopForID.get().offer(new MessageDispatch(handle, stanza));
        if (!taken) {
            LOG.error("Queue refused offered Stanza");
            throw RealtimeExceptionCodes.UNEXPECTED_ERROR.create("Queue refused offered Stanza");
        }
    }

    public void addComponent(Component component) {
        runLoopManager.createRunLoops(component, RealtimeConfig.getInstance().getNumberOfRunLoops());
        components.put(component.getId(), component);
        component.setLoadFactorCalculator(runLoopManager);
    }

    public void removeComponent(Component component) {
        components.remove(component.getId());
        runLoopManager.destroyRunLoops(component);
    }

    private class TimeoutEviction {
        private final long millis;
        private final ID id;

        public TimeoutEviction(long millis, ID id) {
            super();
            this.millis = millis;
            this.id = id;
        }

        public void tick() {
            if (shuttingDown.get()) {
                return;
            }
            long last = lastAccess.get(id);
            long now = System.currentTimeMillis();
            if (now - last >= millis) {
                getRealtimeCleanup().cleanForId(id);
            }
        }


    }

    public void shutdown() {
        shuttingDown.set(true);
        if (!handles.keySet().isEmpty()) {
            RealtimeCleanup realtimeCleanup = getRealtimeCleanup();
            for (ID id : handles.keySet()) {
                try {
                    realtimeCleanup.cleanForId(id);
                } catch (Exception e) {
                    LOG.error("Failed to cleanup for ID {}", id, e);
                }
            }
        }
        runLoopManager.destroyRunLoops();
    }

    @Override
    public void run() {
        if (shuttingDown.get()) {
            return;
        }
        for(TimeoutEviction eviction: new ArrayList<TimeoutEviction>(timeouts.values())) {
                eviction.tick();
        }
    }

    @Override
    public void cleanupForId(ID id) {
        try {
            id.lock(CONJURELOCK);
        } catch (RealtimeException e) {
            LOG.error("Failed to acquire conjure lock during cleanup for ID: {}", id, e);
        }
        try {
            if(handles.containsKey(id)) {
                /*
                 * Lock conjure so no new GroupDispatcher is created until we finished cleanup. A second clean for the same id will either:
                 *  - wait for this lock and do nothin after it got the lock
                 *  - do nothing as the handle for that id was already removed
                 */
                LOG.debug("Cleanup for ID: {}. Removing  ComponentHandle and RunLoop mappings.", id);
                Optional<SyntheticChannelRunLoop> runLoop = runLoopManager.removeIDFromRunLoop(id);
                handles.remove(id);
                lastAccess.remove(id);
                timeouts.remove(id);
                if (!runLoop.isPresent()) {
                    LOG.error("RunLoop to clean was null. This should have been been prevented by mutex.");
                    return;
                }
            } else {
                LOG.debug("Couldn't find ComponentHandle for ID {}, nothing to clean up", id);
            }
        } catch (Exception e) {
            LOG.error("Error during cleanup for ID: {}", id, e);
        } finally {
            try {
                id.unlock(CONJURELOCK);
            } catch (RealtimeException e) {
                LOG.error("Failed to release conjure lock during cleanup for ID: {}", id, e);
            }
        }
    }

    @Override
    public Dictionary<String, Object> getServiceProperties() {
        return JANITOR_PROPERTIES;
    }

    /**
     * Try to get the cluster wide GlobalRealtimeCleanup service first. If that fails get the LocalRealtimeCleanup service that is provided
     * by this bundle and thus should always be available.
     *
     * @return the first available RealtimeCleanup service
     */
    private RealtimeCleanup getRealtimeCleanup() {
        RealtimeCleanup realtimeCleanup = GLOBAL_CLEANUP_REF.get();
        if (realtimeCleanup == null) {
            LOG.debug("Unable to issue cluster wide cleanup due to missing GlobalRealtimeCleanup. Falling back to node wide cleanup");
            realtimeCleanup = localRealtimeCleanup;
        }
        return realtimeCleanup;
    }

}
