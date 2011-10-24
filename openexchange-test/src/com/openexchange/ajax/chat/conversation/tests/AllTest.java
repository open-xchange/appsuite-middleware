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

import java.util.List;
import com.openexchange.ajax.chat.conversation.JSONChat;
import com.openexchange.ajax.chat.conversation.JSONChatUser;
import com.openexchange.ajax.chat.conversation.actions.AllChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.AllChatConversationResponse;
import com.openexchange.ajax.chat.conversation.actions.DeleteChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.GetChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.GetChatConversationResponse;
import com.openexchange.ajax.chat.conversation.actions.NewChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.NewChatConversationResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.chat.ChatDescription;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.json.conversation.ConversationID;


/**
 * {@link AllTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AllTest extends AbstractAJAXSession {

    private AJAXClient client2;

    /**
     * Initializes a new {@link AllTest}.
     */
    public AllTest() {
        super("AllTest");
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
        try {
            /*
             * Create
             */
            final NewChatConversationRequest newChatRequest = new NewChatConversationRequest();
            final ChatDescription chatDescription = new ChatDescription();
            chatDescription.setSubject("New chat");
            chatDescription.addNewMember(String.valueOf(client2.getValues().getUserId()));
            newChatRequest.setChatDescription(chatDescription);
            newChatRequest.setOptAccountId(ChatService.DEFAULT_ACCOUNT);
            newChatRequest.setOptServiceId(ChatService.DEFAULT_SERVICE);
            final NewChatConversationResponse newChatResponse = client.execute(newChatRequest);
            chat = newChatResponse.getChat();
            /*
             * Request via all request
             */
            final AllChatConversationRequest request = new AllChatConversationRequest();
            final AllChatConversationResponse response = client.execute(request);
            /*
             * Get conversation identifiers
             */
            final List<ConversationID> conversationIds = response.getConversationIds();
            boolean found = false;
            final String chatId = chat.getChatId();
            for (final ConversationID conversationId : conversationIds) {
                if (conversationId.getChatId().equals(chatId)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Newly created chat not found in all response.", found);
            /*
             * Request chat
             */
            final GetChatConversationRequest getRequest = new GetChatConversationRequest();
            getRequest.setConversationId(new ConversationID(ChatService.DEFAULT_SERVICE, ChatService.DEFAULT_ACCOUNT, chat.getChatId()));
            final GetChatConversationResponse getResponse = client.execute(getRequest);
            final JSONChat fetchedChat = getResponse.getChat();
            assertEquals("Chat identifier mismatch.", chatId, fetchedChat.getChatId());
            assertEquals("Chat subject mismatch.", chat.getSubject(), fetchedChat.getSubject());

            final List<JSONChatUser> members = chat.getMembers();
            final List<JSONChatUser> fetchedMembers = fetchedChat.getMembers();
            assertEquals("Members differ.", members.size(), fetchedMembers.size());
        } catch (final Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        } finally {
            if (null != chat) {
                try {
                    final DeleteChatConversationRequest req = new DeleteChatConversationRequest();
                    req.addConversationIds(new ConversationID(ChatService.DEFAULT_SERVICE, ChatService.DEFAULT_ACCOUNT, chat.getChatId()));
                    client.execute(req);
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }
    }

}
