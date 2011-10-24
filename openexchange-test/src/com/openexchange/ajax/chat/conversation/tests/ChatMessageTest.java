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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import com.openexchange.ajax.chat.conversation.actions.DeleteChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.DeleteMessageChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.GetMessageChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.GetMessageChatConversationResponse;
import com.openexchange.ajax.chat.conversation.actions.NewChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.NewChatConversationResponse;
import com.openexchange.ajax.chat.conversation.actions.NewMessageChatConversationRequest;
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
 * {@link ChatMessageTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ChatMessageTest extends AbstractAJAXSession {

    private AJAXClient client2;

    /**
     * Initializes a new {@link ChatMessageTest}.
     */
    public ChatMessageTest() {
        super("AllMessageTest");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != client2) {
            client2.logout();
            client2 = null;
        }
        super.tearDown();
    }

    public void testAllRequest() {
        final AJAXClient client = getClient();
        JSONChat chat = null;
        ConversationID conversationId = null;
        try {
            /*
             * Create
             */
            final TimeZone timeZone = client.getValues().getTimeZone();
            {
                final NewChatConversationRequest newChatRequest = new NewChatConversationRequest();
                final ChatDescription chatDescription = new ChatDescription();
                chatDescription.setSubject("New chat");
                chatDescription.addNewMember(String.valueOf(client2.getValues().getUserId()));
                newChatRequest.setChatDescription(chatDescription);
                newChatRequest.setOptAccountId(ChatService.DEFAULT_ACCOUNT);
                newChatRequest.setOptServiceId(ChatService.DEFAULT_SERVICE);
                final NewChatConversationResponse newChatResponse = client.execute(newChatRequest);
                chat = newChatResponse.getChat(timeZone);
                conversationId = new ConversationID(ChatService.DEFAULT_SERVICE, ChatService.DEFAULT_ACCOUNT, chat.getChatId());
            }
            /*
             * Post a new message
             */
            final String text = "Hey there, this is my message!";
            {
                final NewMessageChatConversationRequest newMesRequest = new NewMessageChatConversationRequest();
                newMesRequest.setConversationId(conversationId);
                final MessageImpl message = new MessageImpl();
                message.setFrom(new ChatUserImpl(String.valueOf(client.getValues().getUserId()), null));
                message.setSubject("My new message");
                message.setText(text);
                message.setType(Message.Type.CHAT);
                newMesRequest.setMessage(message);
                client.execute(newMesRequest);
            }
            /*
             * All messages
             */
            {
                AllMessageChatConversationRequest request = new AllMessageChatConversationRequest();
                request.setConversationId(conversationId);
                request.setSince(new Date(0), timeZone);
                AllMessageChatConversationResponse response = client.execute(request);
                List<JSONMessage> messages = response.getMessages(timeZone);
                String messageId = null;
                for (final JSONMessage jsonMessage : messages) {
                    if (text.equals(jsonMessage.getText())) {
                        messageId = jsonMessage.getMessageId();
                        break;
                    }
                }
                assertNotNull("New message not found.", messageId);
                /*
                 * Get
                 */
                final GetMessageChatConversationRequest getReq = new GetMessageChatConversationRequest();
                getReq.setConversationId(conversationId);
                getReq.setMessageId(messageId);
                final GetMessageChatConversationResponse getResp = client.execute(getReq);
                final JSONMessage message = getResp.getMessage(timeZone);
                assertNotNull("New message not found.", message);
                assertEquals("Message identifier mismatch.", messageId, message.getMessageId());
                /*
                 * Delete
                 */
                final DeleteMessageChatConversationRequest delReq = new DeleteMessageChatConversationRequest();
                delReq.setConversationId(conversationId);
                delReq.addMessageIds(messageId);
                client.execute(delReq);
                /*
                 * All
                 */
                request = new AllMessageChatConversationRequest();
                request.setConversationId(conversationId);
                request.setSince(new Date(0), timeZone);
                response = client.execute(request);
                messages = response.getMessages(timeZone);
                messageId = null;
                for (final JSONMessage jsonMessage : messages) {
                    if (text.equals(jsonMessage.getText())) {
                        messageId = jsonMessage.getMessageId();
                        break;
                    }
                }
                assertNull("New message found although deleted.", messageId);
            }
        } catch (final Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        } finally {
            if (null != chat) {
                try {
                    final DeleteChatConversationRequest req = new DeleteChatConversationRequest();
                    req.addConversationIds(conversationId);
                    client.execute(req);
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }
    }

    public void testForeignAllRequest() {
        final AJAXClient client = getClient();
        JSONChat chat = null;
        ConversationID conversationId = null;
        try {
            /*
             * Create
             */
            final TimeZone timeZone = client.getValues().getTimeZone();
            {
                final NewChatConversationRequest newChatRequest = new NewChatConversationRequest();
                final ChatDescription chatDescription = new ChatDescription();
                chatDescription.setSubject("New chat");
                chatDescription.addNewMember(String.valueOf(client2.getValues().getUserId()));
                newChatRequest.setChatDescription(chatDescription);
                newChatRequest.setOptAccountId(ChatService.DEFAULT_ACCOUNT);
                newChatRequest.setOptServiceId(ChatService.DEFAULT_SERVICE);
                final NewChatConversationResponse newChatResponse = client.execute(newChatRequest);
                chat = newChatResponse.getChat(timeZone);
                conversationId = new ConversationID(ChatService.DEFAULT_SERVICE, ChatService.DEFAULT_ACCOUNT, chat.getChatId());
            }
            /*
             * Post a new message
             */
            final String text = "Hey there, this is my message!";
            {
                final NewMessageChatConversationRequest newMesRequest = new NewMessageChatConversationRequest();
                newMesRequest.setConversationId(conversationId);
                final MessageImpl message = new MessageImpl();
                message.setFrom(new ChatUserImpl(String.valueOf(client.getValues().getUserId()), null));
                message.setSubject("My new message");
                message.setText(text);
                message.setType(Message.Type.CHAT);
                newMesRequest.setMessage(message);
                client.execute(newMesRequest);
            }
            /*
             * All messages
             */
            {
                final AllMessageChatConversationRequest request = new AllMessageChatConversationRequest();
                request.setConversationId(conversationId);
                request.setSince(new Date(0), timeZone);
                final AllMessageChatConversationResponse response = client.execute(request);
                final List<JSONMessage> messages = response.getMessages(timeZone);
                String messageId = null;
                for (final JSONMessage jsonMessage : messages) {
                    if (text.equals(jsonMessage.getText())) {
                        messageId = jsonMessage.getMessageId();
                        break;
                    }
                }
                assertNotNull("New message not found.", messageId);
            }
            /*
             * Let the other user post a message
             */
            final String otherText = "Yeah, read this!";
            {
                final NewMessageChatConversationRequest newMesRequest = new NewMessageChatConversationRequest();
                newMesRequest.setConversationId(conversationId);
                final MessageImpl message = new MessageImpl();
                message.setFrom(new ChatUserImpl(String.valueOf(client2.getValues().getUserId()), null));
                message.setSubject("Message from " + client2.getValues().getUserId());
                message.setText(otherText);
                message.setType(Message.Type.CHAT);
                newMesRequest.setMessage(message);
                client2.execute(newMesRequest);
            }
            /*
             * All messages
             */
            {
                final AllMessageChatConversationRequest request = new AllMessageChatConversationRequest();
                request.setConversationId(conversationId);
                request.setSince(new Date(0), timeZone);
                final AllMessageChatConversationResponse response = client.execute(request);
                final List<JSONMessage> messages = response.getMessages(timeZone);
                String messageId = null;
                for (final JSONMessage jsonMessage : messages) {
                    if (otherText.equals(jsonMessage.getText())) {
                        messageId = jsonMessage.getMessageId();
                        break;
                    }
                }
                assertNotNull("New message not found.", messageId);
            }
        } catch (final Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        } finally {
            if (null != chat) {
                try {
                    final DeleteChatConversationRequest req = new DeleteChatConversationRequest();
                    req.addConversationIds(conversationId);
                    client.execute(req);
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }
    }

}
