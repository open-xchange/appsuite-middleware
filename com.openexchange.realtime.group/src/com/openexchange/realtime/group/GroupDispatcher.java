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

package com.openexchange.realtime.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.util.ActionHandler;
import com.openexchange.server.ServiceLookup;


/**
 * {@link GroupDispatcher}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GroupDispatcher implements ComponentHandle {
    
    public static ServiceLookup services = null;
    
    private List<ID> ids = new CopyOnWriteArrayList<ID>();
    private Map<ID, String> stamps = new ConcurrentHashMap<ID, String>();
    
    private ID id;
    
    private ActionHandler handler = null;
    
    public GroupDispatcher(ID id) {
        this(id, null);
    }
    
    public GroupDispatcher(ID id, ActionHandler handler) {
        this.id = id;
        this.handler = handler;
    }
    
    
    @Override
    public void process(Stanza stanza) throws OXException {
        if (handleGroupCommand(stanza)) {
            return;
        }
        processStanza(stanza);
    }
    
    protected void processStanza(Stanza stanza) throws OXException {
        if (handler == null || !handler.callMethod(this, stanza)) {
            relayToAll(stanza);
        }
    }


    private boolean handleGroupCommand(Stanza stanza) throws OXException {
        PayloadElement payload = stanza.getPayload();
        if (payload == null) {
            return true;
        }
        
        Object data = payload.getData();
        if (GroupCommand.class.isInstance(data)) {
            ((GroupCommand) data).perform(stanza, this);
            return true;
        }
        
        return false;
    }

    public void relayToAll(Stanza stanza, ID...excluded) throws OXException {
        MessageDispatcher dispatcher = services.getService(MessageDispatcher.class);
        Set<ID> ex = new HashSet<ID>(Arrays.asList(excluded));
        for(ID id: ids) {
            if (!ex.contains(id)) {
                // Send a copy of the stanza
                Stanza copy = copyFor(stanza, id);
                stamp(copy);
                dispatcher.send(copy);
            }
        }
    }
    
    public void relayToAllExceptSender(Stanza stanza) throws OXException {
        relayToAll(stanza, stanza.getFrom());
    }
    
    public void send(Stanza stanza) throws OXException {
        stamp(stanza);
        MessageDispatcher dispatcher = services.getService(MessageDispatcher.class);
        
        dispatcher.send(stanza);
    }
    
    

    public void join(ID id, String stamp) throws OXException {
        if (ids.contains(id)) {
            return;
        }
        beforeJoin(id);
        
        if (!mayJoin(id)) {
            return;
        }
        
        ids.add(id);
        stamps.put(id, stamp);
        id.on("dispose", LEAVE);
        onJoin(id);
    }
    
    public void leave(ID id) throws OXException {
        beforeLeave(id);
        id.off("dispose", LEAVE);
        ids.remove(id);
        stamps.remove(id);
        if (ids.isEmpty()) {
            onDispose();
            id.trigger("dispose", this);
        }
        onLeave(id);
    }
    
    public String getStamp(ID id) {
        return stamps.get(id);
    }
    
    public void stamp(Stanza s) {
        s.setSelector(getStamp(s.getTo()));
    }
    

    public List<ID> getIds() {
        return ids;
    }
    
    public ID getId() {
        return id;
    }
    
    protected boolean isMember(ID id) {
        return ids.contains(id);
    }
    
    protected Stanza copyFor(Stanza stanza, ID to) throws OXException {
        Stanza copy = stanza.newInstance();
        copy.setTo(to);
        copy.setFrom(stanza.getFrom());
        copyPayload(stanza, copy);
        
        return copy;
    }
    
    protected void copyPayload(Stanza stanza, Stanza copy) throws OXException {
        List<PayloadTree> copyList = new ArrayList<PayloadTree>(stanza.getPayloads().size());
        for(PayloadTree tree: stanza.getPayloads()) {
            copyList.add(tree.internalClone());
        }
        copy.setPayloads(copyList);
    }
    
    protected boolean mayJoin(ID id) {
        return true;
    }
    
    protected void beforeJoin(ID id) {
        
    }
    
    protected void onJoin(ID id) {
        
    }
    
    protected void beforeLeave(ID id) {
        
    }
    
    protected void onLeave(ID id) {
        
    }
    
    protected void onDispose() {
        
    }
    
    private IDEventHandler LEAVE = new IDEventHandler() {
        
        @Override
        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
            try {
                leave(id);
            } catch (OXException e) {
            }
        }
    };

}
