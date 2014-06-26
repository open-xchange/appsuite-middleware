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

package com.openexchange.chat.json.conversation.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chat.Chat;
import com.openexchange.chat.ChatAccess;
import com.openexchange.chat.ChatDescription;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.ChatServiceRegistry;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.Presence;
import com.openexchange.chat.Roster;
import com.openexchange.chat.json.conversation.ChatConversationAJAXRequest;
import com.openexchange.chat.json.conversation.ConversationID;
import com.openexchange.chat.json.conversation.JSONConversationParser;
import com.openexchange.chat.json.conversation.JSONConversationWriter;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "new", description = "Create a new conversation", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "serviceId", optional=true, description = "Set the service Id."),
    @Parameter(name = "accountId", optional=true, description = "Set the account Id.")
}, requestBody = "JSON array describing the conversation.",
responseDescription = "A JSON array containing the created conversation.")
public final class NewAction extends AbstractChatConversationAction {

    /**
     * Initializes a new {@link NewAction}.
     *
     * @param services
     */
    public NewAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ChatConversationAJAXRequest req) throws OXException, JSONException {
        final ServerSession session = req.getSession();
        /*
         * Get parameters
         */
        JSONObject jsonChatObject = req.getData();
        if (jsonChatObject == null) {
            try {
                final InputStream inputStream = req.getRequest().getUploadStream();
                if (null != inputStream) {
                    final int buflen = 2048;
                    final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(buflen << 1);
                    final byte[] buf = new byte[buflen];
                    for (int read; (read = inputStream.read(buf, 0, buflen)) > 0;) {
                        out.write(buf, 0, read);
                    }
                    jsonChatObject = new JSONObject(new String(out.toByteArray()));
                }
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
        String serviceId = req.getParameter("serviceId");
        if (null == serviceId) {
            serviceId = ChatService.DEFAULT_SERVICE;
        }
        String accountId = req.getParameter("accountId");
        if (null == accountId) {
            accountId = ChatService.DEFAULT_ACCOUNT;
        }
        /*
         * Get service
         */
        final ChatServiceRegistry registry = getService(ChatServiceRegistry.class);
        final ChatService chatService = registry.getChatService(serviceId, session.getUserId(), session.getContextId());
        ChatAccess access = null;
        try {
            access = chatService.access(accountId, session);
            access.login();
            /*
             * Get roster
             */
            final Roster roster = access.getRoster();
            final Map<String, ChatUser> entries = roster.getEntries();
            /*
             * Parse chat description
             */
            final ChatDescription chatDescription = JSONConversationParser.parseJSONChatDescriptionForCreate(jsonChatObject);
            List<String> newMembers = chatDescription.getNewMembers();
            if (null == newMembers) {
                newMembers = Collections.emptyList();
            }
            final int size = newMembers.size();
            final List<ChatUser> chatUsers = new ArrayList<ChatUser>(size);
            final List<Presence> presences = new ArrayList<Presence>(size);
            for (int i = 0; i < size; i++) {
                final ChatUser chatUser = entries.get(newMembers.get(i));
                if (null == chatUser) {
                    /*
                     * TODO: User is unknown in roster
                     */
                } else {
                    chatUsers.add(chatUser);
                    presences.add(roster.getPresence(chatUser));
                }
            }
            Chat newChat = access.openChat(null, null, chatUsers.toArray(new ChatUser[chatUsers.size()]));
            /*
             * Add session user to members
             */
            final List<ChatUser> users = new ArrayList<ChatUser>(chatUsers);
            users.add(access.getUser());
            presences.add(roster.getPresence(access.getUser()));
            /*
             * Subject
             */
            final String subject = chatDescription.getSubject();
            final Collection<OXException> warnings = req.getWarnings();
            if (null != subject) {
                final ChatDescription desc = new ChatDescription(newChat.getChatId());
                if (subject.length() > 256) {
                    desc.setSubject(subject.substring(0, 256));
                    final OXException warning = ChatExceptionCodes.SUBJECT_TOO_LONG.create();
                    warning.setCategory(Category.CATEGORY_WARNING);
                    warnings.add(warning);
                } else {
                    desc.setSubject(subject);
                }
                access.updateChat(desc);
                /*
                 * Reload
                 */
                newChat = access.getChat(newChat.getChatId());
            }
            /*
             * Create JSON object for new chat
             */
            final JSONObject jsonChat = JSONConversationWriter.writeChat(newChat, users, presences, session.getUser().getTimeZone());
            jsonChat.put("id", new ConversationID(serviceId, accountId, newChat.getChatId()));
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(jsonChat, "json").addWarnings(warnings);
        } finally {
            if (null != access) {
                access.disconnect();
            }
        }
    }

}
