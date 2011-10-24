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

package com.openexchange.ajax.chat.roster.tests;

import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.chat.conversation.JSONChatUser;
import com.openexchange.ajax.chat.conversation.JSONPresence;
import com.openexchange.ajax.chat.roster.JSONRoster;
import com.openexchange.ajax.chat.roster.actions.AllChatRosterRequest;
import com.openexchange.ajax.chat.roster.actions.AllChatRosterResponse;
import com.openexchange.ajax.chat.roster.actions.GetChatRosterRequest;
import com.openexchange.ajax.chat.roster.actions.GetChatRosterResponse;
import com.openexchange.ajax.chat.roster.actions.UpdateChatRosterRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.Presence;
import com.openexchange.chat.Presence.Mode;
import com.openexchange.chat.json.roster.RosterID;
import com.openexchange.chat.util.PresenceImpl;


/**
 * {@link AllTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AllTest extends AbstractAJAXSession {

    private AJAXClient client2;

    private AJAXClient client3;

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
        client3 = new AJAXClient(User.User3);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != client2) {
            client2.logout();
            client2 = null;
        }
        if (null != client3) {
            client3.logout();
            client3 = null;
        }
        super.tearDown();
    }

    public void testAll() {
        final AJAXClient client = getClient();
        try {
            /*
             * Create
             */
            final RosterID defaultRosterID = new RosterID(ChatService.DEFAULT_SERVICE, ChatService.DEFAULT_ACCOUNT);
            final TimeZone timeZone = client.getValues().getTimeZone();
            {
                final AllChatRosterRequest request = new AllChatRosterRequest();
                final AllChatRosterResponse response = client.execute(request);

                final List<JSONRoster> rosters = response.getRosters(timeZone);
                final String defaultId = defaultRosterID.toString();
                for (final JSONRoster jsonRoster : rosters) {
                    final String id = jsonRoster.getId();
                    assertNotNull("Identifier is null", id);
                    if (defaultId.equals(id)) {
                        /*
                         * Count available; should be 3 because of 3 opened AJAXClient instances
                         */
                        int count = 0;
                        for (final JSONChatUser jsonChatUser : jsonRoster.getMembers()) {
                            final JSONPresence presence = jsonChatUser.getPresence();
                            if (Presence.Type.AVAILABLE.equals(presence.getType())) {
                                count++;
                            }

                            final GetChatRosterRequest getReq = new GetChatRosterRequest();
                            getReq.setRosterId(defaultRosterID);
                            getReq.setUser(jsonChatUser.getId());
                            final JSONChatUser fetchedUser = client.execute(getReq).getUser(timeZone);
                            assertEquals("Unexpected presence mode.", presence.getMode(), fetchedUser.getPresence().getMode());
                        }
                        assertEquals("Unexpected number of available users.", 3, count);
                    }
                }
            }
            /*
             * Update presence
             */
            final JSONPresence tmp;
            {
                final GetChatRosterRequest getReq = new GetChatRosterRequest();
                getReq.setRosterId(defaultRosterID);
                getReq.setUser(String.valueOf(client.getValues().getUserId()));
                tmp = client.execute(getReq).getUser(timeZone).getPresence();

                final UpdateChatRosterRequest request = new UpdateChatRosterRequest();
                request.setRosterId(defaultRosterID);
                final PresenceImpl presence = new PresenceImpl();
                presence.setMode(Mode.AWAY);
                presence.setStatus("Away, back in a short time.");
                request.setPresence(presence);
                client.execute(request);
            }
            /*
             * Get
             */
            {
                final GetChatRosterRequest request = new GetChatRosterRequest();
                request.setRosterId(defaultRosterID);
                request.setUser(String.valueOf(client.getValues().getUserId()));
                final GetChatRosterResponse response = client.execute(request);
                final JSONChatUser jsonChatUser = response.getUser(timeZone);
                assertEquals("Unexpected mode.", Mode.AWAY, jsonChatUser.getPresence().getMode());
            }
            /*
             * Restore
             */
            {
                final UpdateChatRosterRequest request = new UpdateChatRosterRequest();
                request.setRosterId(defaultRosterID);
                final PresenceImpl presence = new PresenceImpl();
                presence.setMode(tmp.getMode());
                presence.setStatus(tmp.getStatus());
                request.setPresence(presence);
                client.execute(request);
            }
        } catch (final Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

}
