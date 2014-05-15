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

package com.openexchange.realtime.synthetic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.Component.EvictionPolicy;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanup;
import com.openexchange.realtime.cleanup.RealtimeCleanup;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link SyntheticChannel}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class SyntheticChannel implements Channel, Runnable {

    private static final int NUMBER_OF_RUNLOOPS = 16;
    public static final String PROTOCOL = "synthetic";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyntheticChannel.class);
    private static final String SENDLOCK = "syntheticChannel";

    public static final AtomicReference<GlobalRealtimeCleanup> GLOBAL_CLEANUP_REF = new AtomicReference<GlobalRealtimeCleanup>();
    private LocalRealtimeCleanup localRealtimeCleanup = null;

    private final ConcurrentHashMap<String, Component> components = new ConcurrentHashMap<String, Component>();
    private final ConcurrentHashMap<ID, ComponentHandle> handles = new ConcurrentHashMap<ID, ComponentHandle>();
    private final ConcurrentHashMap<ID, SyntheticChannelRunLoop> runLoopsPerID = new ConcurrentHashMap<ID, SyntheticChannelRunLoop>();
    private final List<SyntheticChannelRunLoop> runLoops = new ArrayList<SyntheticChannelRunLoop>(NUMBER_OF_RUNLOOPS);
    private final ConcurrentHashMap<ID, Long> lastAccess = new ConcurrentHashMap<ID, Long>();
    private final ConcurrentHashMap<ID, TimeoutEviction> timeouts = new ConcurrentHashMap<ID, TimeoutEviction>();

    /** Only one CLEANUP handler may clean a loop at a time */
    private final ConcurrentHashMap<SyntheticChannelRunLoop, Lock> cleanUpLocks = new ConcurrentHashMap<SyntheticChannelRunLoop, Lock>();

    private final Random loadBalancer = new Random();

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public SyntheticChannel(ServiceLookup services, LocalRealtimeCleanup localRealtimeCleanup) {
        this.localRealtimeCleanup = localRealtimeCleanup;
        for (int i = 0; i < NUMBER_OF_RUNLOOPS; i++) {
            SyntheticChannelRunLoop rl = new SyntheticChannelRunLoop("message-handler-" + i);
            runLoops.add(rl);
            cleanUpLocks.put(rl, new ReentrantLock());
            services.getService(ThreadPoolService.class).getExecutor().execute(rl);
        }
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
        runLoopsPerID.put(id, runLoops.get(loadBalancer.nextInt(NUMBER_OF_RUNLOOPS)));

        setUpEviction(component.getEvictionPolicy(), handle, id);

        id.on(ID.Events.BEFOREDISPOSE, CLEANUP);

        return true;
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
            throw RealtimeExceptionCodes.INVALID_ID.create(stanza.getTo());
        }
        stanza.trace("Delivering to handle " + handle);
        stanza.trace("Updating last access");
        lastAccess.put(stanza.getTo(), System.currentTimeMillis());

        SyntheticChannelRunLoop runLoop = runLoopsPerID.get(recipient);
        if (runLoop == null) {
            throw RealtimeExceptionCodes.INVALID_ID.create(stanza.getTo());
        }
        
        Lock sendLock = recipient.getLock(SENDLOCK);
        
        try {
            sendLock.lock();
            final boolean taken = runLoop.offer(new MessageDispatch(handle, stanza));
            if (!taken) {
                LOG.error("Queue refused offered Stanza");
                throw RealtimeExceptionCodes.UNEXPECTED_ERROR.create("Queue refused offered Stanza");
            }
        } finally {
            sendLock.unlock();
        }
    }

    public void addComponent(Component component) {
        components.put(component.getId(), component);
    }

    public void removeComponent(Component component) {
        components.remove(component.getId());
    }

    private class TimeoutEviction {
        private final long millis;
        private final ID id;

        public TimeoutEviction(long millis, ID id) {
            super();
            this.millis = millis;
            this.id = id;
        }

        public void tick() throws OXException {
            if (shuttingDown.get()) {
                return;
            }
            long last = lastAccess.get(id);
            long now = System.currentTimeMillis();

            if (now - last >= millis) {
                if(id.isDisposable()) {
                    getRealtimeCleanup().cleanForId(id);
                }
            }
        }


    }

    public void shutdown() {
        RealtimeCleanup realtimeCleanup = getRealtimeCleanup();
        for (ID id : handles.keySet()) {
            try {
                if (id.isDisposable()) {
                    realtimeCleanup.cleanForId(id);
                }
            } catch (Exception e) {
                LOG.error("Failed to cleanup for ID {}", id, e);
            }
        }
    }

    @Override
    public void run() {
        if (shuttingDown.get()) {
            return;
        }
        for(TimeoutEviction eviction: new ArrayList<TimeoutEviction>(timeouts.values())) {
            try {
                eviction.tick();
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     *
     * A GroupDispatcher is going to be disposed, messages that are already handed off to RunLoops might have to be reordered:
     * - lock this channel for the GD ID to stop accepting new messages for the handle
     * - clean lastAccess and timeouts which are used for eviction of handles
     * - get the associated Runloop
     * - stop loop from handling
     * - check for MessageDispatchs directed to the handle
     * - continue RunLoop
     * - create new handle if necessary and remove the old one from handles, otherwise just remove the old one
     * - rewrite MessageDispatchs to use new handle and add them to the new RunLoop
     * - unlock for ID to start accepting new messages for this ID again
     */
    private final IDEventHandler CLEANUP = new IDEventHandler() {

        @Override
        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
            Lock cleanUpLock = null;
            Lock sendLock = null;
            try {
                SyntheticChannelRunLoop runLoopForId = runLoopsPerID.get(id);
                if (runLoopForId != null) {
                    cleanUpLock = cleanUpLocks.get(runLoopForId);
                    sendLock = id.getLock(SENDLOCK);
                    cleanUpLock.lock();
                    sendLock.lock();
                    lastAccess.remove(id);
                    timeouts.remove(id);
                    handles.remove(id);
                    SyntheticChannelRunLoop runLoop = runLoopsPerID.remove(id);
                    if (runLoop == null) {
                        LOG.error("RunLoop to clean was null. This should have been been prevented by mutex.");
                        return;
                    }
                    Collection<MessageDispatch> messagesForHandle = runLoop.removeMessagesForHandle(id);
                    if (!messagesForHandle.isEmpty()) {
                        /*
                         * The ID.Events.BEFOREDISPOSAL which triggered this handler allows us to veto the complete disposal of this ID if
                         * we still see need for it which is the case when messagesForHandle isn't empty.
                         */
                        properties.put("veto", true);
                        LOG.debug("Vetoed disposal of id: {}", id);
                        if (conjure(id)) {
                            ComponentHandle newHandle = handles.get(id);
                            SyntheticChannelRunLoop newRunLoop = runLoopsPerID.get(id);
                            for (MessageDispatch messageDispatch : messagesForHandle) {
                                messageDispatch.setHandle(newHandle);
                                boolean taken = newRunLoop.offer(messageDispatch);
                                if (!taken) {
                                    LOG.error("Queue refused offered Stanza for id: {}", id);
                                }
                            }
                            LOG.debug("Migrated MessageDispatchs to new Handle for id: {}", id);
                        } else {
                            LOG.error("Unable to conjure ID and migrate MessageDispatchs to new handle for id: {}", id);
                        }
                    } else {
                        LOG.debug("No MessageDispatchs to migrate for id: {}", id);
                    }
                } else {
                    LOG.error("RunLoopForID was null while trying to clean up.");
                }
            } catch (Exception e) {
                LOG.error("Error during RunLoop cleanup for ID: {}", id, e);
            } finally {
                if (sendLock != null) {
                    sendLock.unlock();
                }
                if (cleanUpLock != null) {
                    cleanUpLock.unlock();
                }
            }
        }

    };
    
    /**
     * Try to get the cluster wide GlobalRealtimeCleanup service first. If that fails get the LocalRealtimeCleanup service that is provided
     * by this bundle and thus should always be available.
     * 
     * @return the first available RealtimeCleanup service
     */
    private RealtimeCleanup getRealtimeCleanup() {
        RealtimeCleanup realtimeCleanup = GLOBAL_CLEANUP_REF.get();
        if (realtimeCleanup == null) {
            LOG.error("Unable to issue cluster wide cleanup due to missing GlobalRealtimeCleanup. Falling back to node wide cleanup");
            realtimeCleanup = localRealtimeCleanup;
        }
        return realtimeCleanup;
    }

}
