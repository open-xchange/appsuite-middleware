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

package com.openexchange.chat.json.roster.action;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chat.ChatAccess;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.ChatServiceRegistry;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.Roster;
import com.openexchange.chat.json.roster.ChatRosterAJAXRequest;
import com.openexchange.chat.json.roster.JSONRosterWriter;
import com.openexchange.chat.json.roster.RosterID;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "get", description = "Get a chat roster", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Id of the requested roster."),
    @Parameter(name = "user", description = "Name of requesting user.")
}, responseDescription = "A JSON array containing the requested roster.")
public final class GetAction extends AbstractChatRosterAction {

    /**
     * Initializes a new {@link GetAction}.
     */
    public GetAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ChatRosterAJAXRequest req) throws OXException, JSONException {
        final ServerSession session = req.getSession();
        /*
         * Get parameters
         */
        final RosterID rosterId;
        {
            final String id = req.getParameter("id");
            if (null == id) {
                rosterId = new RosterID(ChatService.DEFAULT_SERVICE, ChatService.DEFAULT_ACCOUNT);
            } else {
                rosterId = RosterID.valueOf(id);
            }
        }
        final String user = req.checkParameter("user");
        /*
         * Get service
         */
        final ChatServiceRegistry registry = getService(ChatServiceRegistry.class);
        final ChatService chatService = registry.getChatService(rosterId.getServiceId(), session.getUserId(), session.getContextId());
        ChatAccess access = null;
        try {
            access = chatService.access(rosterId.getAccountId(), session);
            access.login();
            final Roster roster = access.getRoster();
            final ChatUser chatUser = roster.getEntries().get(user);
            if (null == chatUser) {
                throw ChatExceptionCodes.MEMBER_NOT_FOUND.create(user);
            }
            /*
             * Write JSON
             */
            final JSONObject json = JSONRosterWriter.writeChatUser(chatUser, roster.getPresence(chatUser), session.getUser().getTimeZone());
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(json, "json");
        } finally {
            if (null != access) {
                access.disconnect();
            }
        }
    }

}
