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

package com.openexchange.realtime.synthetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.Component.EvictionPolicy;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.RealtimeExceptionCodes;
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
 */
public class SyntheticChannel implements Channel, Runnable {
    
    private static final int NUMBER_OF_RUNLOOPS = 16;
    
    private static final Log LOG = LogFactory.getLog(SyntheticChannel.class);
    
    private final ConcurrentHashMap<String, Component> components = new ConcurrentHashMap<String, Component>();
    private final ConcurrentHashMap<ID, ComponentHandle> handles = new ConcurrentHashMap<ID, ComponentHandle>();
    private final ConcurrentHashMap<ID, SyntheticChannelRunLoop> runLoopsPerID = new ConcurrentHashMap<ID, SyntheticChannelRunLoop>();
    private final List<SyntheticChannelRunLoop> runLoops = new ArrayList<SyntheticChannelRunLoop>(NUMBER_OF_RUNLOOPS);
    
    private final ConcurrentHashMap<ID, Long> lastAccess = new ConcurrentHashMap<ID, Long>();
    private final CopyOnWriteArrayList<TimeoutEviction> timeouts = new CopyOnWriteArrayList<TimeoutEviction>();
    
    private Random loadBalancer = new Random();
    
    public SyntheticChannel(ServiceLookup services) {
        for (int i = 0; i < NUMBER_OF_RUNLOOPS; i++) {
            SyntheticChannelRunLoop rl = new SyntheticChannelRunLoop("message-handler-" + i);
            runLoops.add(rl);
            services.getService(ThreadPoolService.class).getExecutor().execute(rl);
        }
    }
    
    @Override
    public String getProtocol() {
        return "synthetic";
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
        return handles.containsKey(id);
    }

    @Override
    public boolean conjure(ID id) throws OXException {
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
        
        id.on("dispose", new IDEventHandler() {
            
            @Override
            public void handle(String event, ID id, Object source, Map<String, Object> properties) {
                handles.remove(id);
                runLoopsPerID.remove(id);
                lastAccess.remove(id);
                timeouts.remove(this);

            }
        });
        
        return true;
    }

    protected void setUpEviction(EvictionPolicy evictionPolicy, ComponentHandle handle, ID id) {
        if (Component.Timeout.class.isInstance(evictionPolicy)) {
            Component.Timeout timeout = (Component.Timeout) evictionPolicy;
            timeouts.add(new TimeoutEviction(TimeUnit.MILLISECONDS.convert(timeout.getTimeout(), timeout.getUnit()), id));
        }
    }

    @Override
    public void send(final Stanza stanza, ID recipient) throws OXException {
        stanza.trace("SyntheticChannel delivering to " + recipient);
        final ComponentHandle handle = handles.get(recipient);
        if (handle == null) {
            stanza.trace("Unknown recipient: "+ stanza.getTo());
            throw RealtimeExceptionCodes.INVALID_ID.create(stanza.getTo());
        }
        stanza.trace("Delivering to handle " + handle);
        stanza.trace("Updating last access");
        lastAccess.put(stanza.getTo(), System.currentTimeMillis());

        SyntheticChannelRunLoop runLoop = runLoopsPerID.get(recipient);
        if (runLoop == null) {
            throw RealtimeExceptionCodes.INVALID_ID.create(stanza.getTo());
        }
        runLoop.offer(new MessageDispatch(handle, stanza));
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
            long last = lastAccess.get(id);
            long now = System.currentTimeMillis();
            
            if (now - last >= millis) {                
                id.trigger("dispose", SyntheticChannel.this);
            }
        }
        
    
    }

    @Override
    public void run() {
        for(TimeoutEviction e: new ArrayList<TimeoutEviction>(timeouts)) {
            try {
                e.tick();
            } catch (OXException e1) {
                LOG.error(e1.getMessage(), e1);
            }
        }
    }

}
