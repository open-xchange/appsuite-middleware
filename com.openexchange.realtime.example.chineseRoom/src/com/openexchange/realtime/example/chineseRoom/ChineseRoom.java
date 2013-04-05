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


/**
 * This is the nitty gritty. A {@link ChineseRoom} has members, keeps a log of messages (shared state of all members), allows members to speak {@link #handleSay(Stanza)} and
 * thereby modify the shared state and allows members to retrieve the state {@link #handleGetLog(Stanza)}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ChineseRoom extends GroupDispatcher implements ComponentHandle {
    // An introspecting handler that allows clients to formulate stanzas that call all handle* methods
    private static final ActionHandler handler = new ActionHandler(ChineseRoom.class);
    
    // Our simple minded shared state
    private final CopyOnWriteArrayList<LoggedMessage> messages = new CopyOnWriteArrayList<LoggedMessage>();
    
    // Create a new chinese room instance
    public ChineseRoom(ID id) {
        super(id, handler);
    }
    
    // Say something in the chat room
    // Stanzas look like this:
    // {
    //    element: "message",
    //    payloads: [
    //      { element: "action", data: "say" },
    //      { element: "message", namespace: "china",  data: "Hello World" }
    //    ],
    //    to: "synthetic.china://room1",
    //    session: "72306eae544b4ca6aabab1485ec8a666"
    //    }
    public void handleSay(Stanza stanza) throws OXException {
        // We only allow members to say something in the chat room
        if (!isMember(stanza.getFrom())) {
            return; // Discard
        }
        // Retrieve the message from the payloads
        StringBuilder message = new StringBuilder();
        // We're iterating over all messages that are constructed with the china.message element path
        for(PayloadTree messages: stanza.getPayloads(new ElementPath("china", "message"))){
            // Simply append all messages
            message.append(messages.getRoot().getData().toString());
        }
        // Turn the message into pseudo chinese
        String chineseMessage = chineseVersionOf (message.toString());
        
        // Modify the shared state
        messages.add(new LoggedMessage(chineseMessage, stanza.getFrom()));
    
        // Send the message to all participants in the chat (including the one who said it originally
        sendToAll( stanza, chineseMessage);
    }
    
    // Get a replay of old messages
    // {
    //    element: "message",
    //    payloads: [
    //      { element: "action", data: "getLog" }
    //    ],
    //    to: "synthetic.china://room1",
    //    session: "72306eae544b4ca6aabab1485ec8a666"
    //    }
    public void handleGetLog(Stanza stanza) throws OXException {
        // Again, only members may retrieve the history
        if (!isMember(stanza.getFrom()) && !stanza.getFrom().getProtocol().equals("call")) {
            return; // Discard
        }
        // As an answer, we create a new message
        // to the one who asked for the log containing all
        // LoggedMessages
        Message message = new Message();
        message.setFrom(getId());
        message.setTo(stanza.getFrom());
        for(LoggedMessage logged: messages) {
            // Add a payload entry for every logged message
            // We can just throw in the LoggedMessage instances, because
            // We have registered a JSON converter
            message.addPayload(new PayloadTree(
                PayloadTreeNode.builder()
                .withPayload(
                    new PayloadElement(logged, LoggedMessage.class.getName(), "china", "replay") 
                )
            .build()
            ));
        }
        // Send the message to the one who asked
        send(message);
    }
    
    // Droo Chonosen mot nom Kontrobo§, so§on oof dor Stro§o ond orzohtlon soch wos
    // Do kom do Polozoo jo wos ost donn doss? Droo Chonoson mot nom Kontrobo§
    private String chineseVersionOf(String string) {
        return string.replaceAll("[aeiouŠŸš]", "o");
    }
        
    // Build a message out of a string
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
    protected void firstJoined(ID id) {
        // Hooray! Someone joined! This could be a good place to initialise the shared state, if we'd save it externally
        System.out.println("Load data");
    }
    
    @Override
    protected void onDispose(ID id) {
        // Seems we're being closed down, this would be the place to persist our shared state
        System.out.println("Persist data");
    }

    
}
