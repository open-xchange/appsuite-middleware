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

package com.openexchange.chat.json.conversation.action;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chat.Chat;
import com.openexchange.chat.ChatAccess;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.ChatServiceRegistry;
import com.openexchange.chat.json.conversation.ChatConversationAJAXRequest;
import com.openexchange.chat.json.conversation.ConversationID;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link JoinAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Action(method = RequestMethod.PUT, name = "join", description = "Add users to conversation", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Id of the requested conversation.")
}, requestBody = "Array of user Ids that join the conversation.")
public class JoinAction extends AbstractChatConversationAction {

    /**
     * Initializes a new {@link JoinAction}.
     * @param services
     */
    public JoinAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ChatConversationAJAXRequest req) throws OXException, JSONException {
        final ServerSession session = req.getSession();
        final JSONArray json = req.getData();
        final int length = json.length();
        if (length > 0) {
            final ConversationID conversationID = ConversationID.valueOf(req.getParameter("id"));
            final ChatServiceRegistry registry = getService(ChatServiceRegistry.class);
            final ChatService chatService = registry.getChatService(conversationID.getServiceId(), session.getUserId(), session.getContextId());
            ChatAccess chatAccess = null;
            try {
                chatAccess = chatService.access(conversationID.getAccountId(), session);
                chatAccess.login();
                final Chat chat = chatAccess.getChat(conversationID.getChatId());
                final Set<String> existing = new HashSet<String>(chat.getMembers());
                for (int i = 0; i < length; i++) {
                    final String userId = json.getString(i);
                    if (!existing.contains(userId)) {
                        chat.join(userId);
                        existing.add(userId);
                    }
                }
            } finally {
                if (chatAccess != null) {
                    chatAccess.disconnect();
                }
            }
        }
        return new AJAXRequestResult(new JSONObject(0), "json");
    }

}
