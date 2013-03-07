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

package com.openexchange.realtime.example.chineseRoom;

import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.group.GroupDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.ActionHandler;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ChineseRoom}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ChineseRoom extends GroupDispatcher implements ComponentHandle {
    
    private static final ActionHandler handler = new ActionHandler(ChineseRoom.class);
    
    private CopyOnWriteArrayList<LoggedMessage> messages = new CopyOnWriteArrayList<LoggedMessage>();
    
    
    public ChineseRoom(ID id) {
        super(id, handler);
    }
    
    // Say something in the chat room
    public void handleSay(Stanza stanza) throws OXException {
        if (!isMember(stanza.getFrom())) {
            return; // Discard
        }
        StringBuilder message = new StringBuilder();
        for(PayloadTree messages: stanza.getPayloads(new ElementPath("china", "message"))){
            message.append(messages.getRoot().getData().toString());
        }
        String chineseMessage = chineseVersionOf (message.toString());
        
        messages.add(new LoggedMessage(chineseMessage, stanza.getFrom()));
        sendToAll( stanza, chineseMessage);
    }
    
    // Get a replay of old messages
    public void handleGetLog(Stanza stanza) throws OXException {
        if (!isMember(stanza.getFrom())) {
            return; // Discard
        }
        Message message = new Message();
        message.setFrom(getId());
        message.setTo(stanza.getFrom());
        for(LoggedMessage logged: messages) {
            message.addPayload(new PayloadTree(
                PayloadTreeNode.builder()
                .withPayload(
                    new PayloadElement(logged, LoggedMessage.class.getName(), "china", "replay")
                )
            .build()
            ));
        }
        
        send(message);
    }

    private String chineseVersionOf(String string) {
        return string.replaceAll("[aeiou]", "o");
    }

    private void sendToAll(Stanza stanza, String chineseVersion) throws OXException {
        Message message = new Message();
        message.setFrom(stanza.getFrom());
        message.addPayload(
            new PayloadTree(
                PayloadTreeNode.builder()
                    .withPayload(
                        new PayloadElement(chineseVersion, "string", "china", "message")
                    )
                .build()
            )
        );
        
        relayToAll(message);
    }
    
    @Override
    protected void onDispose() {
        System.out.println("Persist data");
    }

    
}
