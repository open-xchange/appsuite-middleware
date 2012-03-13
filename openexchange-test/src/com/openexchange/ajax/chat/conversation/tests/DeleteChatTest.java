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

package com.openexchange.ajax.chat.conversation.tests;

import java.util.TimeZone;
import com.openexchange.ajax.chat.conversation.JSONChat;
import com.openexchange.ajax.chat.conversation.actions.AllChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.AllChatConversationResponse;
import com.openexchange.ajax.chat.conversation.actions.DeleteChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.NewChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.NewChatConversationResponse;
import com.openexchange.ajax.chat.conversation.actions.UpdateChatConversationRequest;
import com.openexchange.ajax.chat.conversation.actions.UpdateChatConversationResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.chat.ChatDescription;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.json.conversation.ConversationID;


/**
 * {@link DeleteChatTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeleteChatTest extends AbstractAJAXSession {
    
    private AJAXClient client, secondClient;

    private JSONChat chat;

    private TimeZone tz;

    private ConversationID cid;
    
    private int numberOfChats;

    /**
     * Initializes a new {@link DeleteChatTest}.
     * @param name
     */
    public DeleteChatTest(String name) {
        super(name);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        AllChatConversationRequest allChatRequest = new AllChatConversationRequest();
        AllChatConversationResponse allChatResponse = client.execute(allChatRequest);
        numberOfChats = allChatResponse.getConversationIds().size();
        NewChatConversationRequest newChat = new NewChatConversationRequest();
        ChatDescription chatDescription = new ChatDescription();
        chatDescription.setSubject("Delete chat test");
        chatDescription.addNewMember(String.valueOf(client.getValues().getUserId()));
        newChat.setChatDescription(chatDescription);
        newChat.setOptAccountId(ChatService.DEFAULT_ACCOUNT);
        newChat.setOptServiceId(ChatService.DEFAULT_SERVICE);
        NewChatConversationResponse response = client.execute(newChat);
        chat = response.getChat(tz);
        cid = ConversationID.valueOf(chat.getChatId());
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDeleteChat() throws Exception {
        secondClient = new AJAXClient(User.User2);
        ChatDescription chatDescription =  new ChatDescription(cid.getChatId());
        chatDescription.addNewMember(String.valueOf(secondClient.getValues().getUserId()));
        UpdateChatConversationRequest updateChatRequest = new UpdateChatConversationRequest();
        updateChatRequest.setConversationId(cid);
        updateChatRequest.setChatDescription(chatDescription);
        UpdateChatConversationResponse updateChatResponse = client.execute(updateChatRequest);
        if (updateChatResponse.hasError()) {
            fail("Second user could not join chat.");
        }
        DeleteChatConversationRequest deleteChatRequest = new DeleteChatConversationRequest();
        deleteChatRequest.addConversationIds(new ConversationID[] {cid});
        client.execute(deleteChatRequest);
        AllChatConversationRequest allChatRequest = new AllChatConversationRequest();
        AllChatConversationResponse allChatResponse = client.execute(allChatRequest);
        assertEquals("Chat chunks not deleted", numberOfChats, allChatResponse.getConversationIds().size());
    }

}
