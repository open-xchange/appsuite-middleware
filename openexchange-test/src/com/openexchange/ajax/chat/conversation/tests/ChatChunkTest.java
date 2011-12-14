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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.chat.conversation.tests;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.chat.conversation.JSONChat;
import com.openexchange.ajax.chat.conversation.JSONMessage;
import com.openexchange.ajax.chat.conversation.actions.AllMessageChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.AllMessageChatConversationResponse;
import com.openexchange.ajax.chat.conversation.actions.DeleteMessageChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.NewChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.NewChatConversationResponse;
import com.openexchange.ajax.chat.conversation.actions.NewMessageChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.UpdateChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.UpdateChatConversationResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.chat.ChatDescription;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.Message;
import com.openexchange.chat.json.conversation.ConversationID;
import com.openexchange.chat.util.ChatUserImpl;
import com.openexchange.chat.util.MessageImpl;

/**
 * {@link ChatChunkTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ChatChunkTest extends AbstractAJAXSession {

    private AJAXClient client, secondClient, thirdClient;

    private JSONChat chat;

    private TimeZone tz;

    private ConversationID cid;

    /**
     * Initializes a new {@link ChatChunkTest}.
     *
     * @param name
     */
    public ChatChunkTest(final String name) {
        super(name);
    }

    @Override
    public void tearDown() throws Exception {
        DeleteMessageChatConversationRequest deleteReq = new DeleteMessageChatConversationRequest();
        deleteReq.setConversationId(cid);
        client.execute(deleteReq);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        NewChatConversationRequest newChat = new NewChatConversationRequest();
        ChatDescription chatDescription = new ChatDescription();
        chatDescription.setSubject("Chat chunk test");
        chatDescription.addNewMember(String.valueOf(client.getValues().getUserId()));
        newChat.setChatDescription(chatDescription);
        newChat.setOptAccountId(ChatService.DEFAULT_ACCOUNT);
        newChat.setOptServiceId(ChatService.DEFAULT_SERVICE);
        NewChatConversationResponse response = client.execute(newChat);
        chat = response.getChat(tz);
        cid = ConversationID.valueOf(chat.getChatId());
        NewMessageChatConversationRequest newMessage = new NewMessageChatConversationRequest();
        newMessage.setConversationId(cid);
        MessageImpl message = new MessageImpl();
        message.setFrom(new ChatUserImpl(String.valueOf(client.getValues().getUserId()), null));
        message.setText("Second user should not see me...");
        message.setType(Message.Type.CHAT);
        newMessage.setMessage(message);
        client.execute(newMessage);
    }

    public void testChatChunk() throws Exception {
        {
            secondClient = new AJAXClient(User.User2);
            ChatDescription chatDescription = new ChatDescription(cid.getChatId());
            chatDescription.addNewMember(String.valueOf(secondClient.getValues().getUserId()));
            UpdateChatConversationRequest updReq = new UpdateChatConversationRequest();
            updReq.setConversationId(cid);
            updReq.setChatDescription(chatDescription);
            UpdateChatConversationResponse updRes = client.execute(updReq);
            if (updRes.hasError()) {
                fail("Second user could not join chat.");
            }
            AllMessageChatConversationRequest allMsg = new AllMessageChatConversationRequest();
            allMsg.setConversationId(cid);
            allMsg.setSince(new Date(0), tz);
            AllMessageChatConversationResponse allMsgRes = secondClient.execute(allMsg);
            List<JSONMessage> res = allMsgRes.getMessages(tz);
            assertNotNull("Response was null.", res);
            assertEquals("Second client can see messages from old chunk.", 0, res.size());
        }

        NewMessageChatConversationRequest newMessage = new NewMessageChatConversationRequest();
        newMessage.setConversationId(cid);
        MessageImpl message = new MessageImpl();
        message.setFrom(new ChatUserImpl(String.valueOf(secondClient.getValues().getUserId())));
        message.setText("First and second user should see this...");
        message.setType(Message.Type.CHAT);
        newMessage.setMessage(message);
        secondClient.execute(newMessage);

        {
            AllMessageChatConversationRequest allMsg = new AllMessageChatConversationRequest();
            allMsg.setConversationId(cid);
            allMsg.setSince(new Date(0), tz);
            AllMessageChatConversationResponse allMsgRes = secondClient.execute(allMsg);
            List<JSONMessage> res = allMsgRes.getMessages(tz);
            assertNotNull("Response was null.", res);
            assertEquals("Second client can see messages from old chunk or no messages from actual chunk.", 1, res.size());
        }


        {
            thirdClient = new AJAXClient(User.User3);
            ChatDescription chatDescription = new ChatDescription(cid.getChatId());
            chatDescription.addNewMember(String.valueOf(thirdClient.getValues().getUserId()));
            UpdateChatConversationRequest updReq = new UpdateChatConversationRequest();
            updReq.setConversationId(cid);
            updReq.setChatDescription(chatDescription);
            UpdateChatConversationResponse updRes = client.execute(updReq);
            if (updRes.hasError()) {
                fail("Third user could not join chat.");
            }
            AllMessageChatConversationRequest allMsg = new AllMessageChatConversationRequest();
            allMsg.setConversationId(cid);
            allMsg.setSince(new Date(0), tz);
            AllMessageChatConversationResponse allMsgRes = thirdClient.execute(allMsg);
            List<JSONMessage> res = allMsgRes.getMessages(tz);
            assertNotNull("Response was null.", res);
            assertEquals("Third client can see messages from old chunk.", 0, res.size());
        }

        {
            AllMessageChatConversationRequest allMsg = new AllMessageChatConversationRequest();
            allMsg.setConversationId(cid);
            allMsg.setSince(new Date(0), tz);
            AllMessageChatConversationResponse allMsgRes = client.execute(allMsg);
            List<JSONMessage> res = allMsgRes.getMessages(tz);
            assertNotNull("Response was null.", res);
            assertEquals("First client could not read all messages.", 2, res.size());
        }

        {
            AllMessageChatConversationRequest allMsg = new AllMessageChatConversationRequest();
            allMsg.setConversationId(cid);
            allMsg.setSince(null, tz);
            AllMessageChatConversationResponse allMsgRes = client.execute(allMsg);
            List<JSONMessage> res = allMsgRes.getMessages(tz);
            assertNotNull("Response was null.", res);
            assertEquals("First client got older messages than last poll.", 0, res.size());
        }
    }

}
