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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.realtime.packet;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import com.google.common.base.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.exception.RealtimeExceptionFactory;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.payload.PayloadTree;

/**
 * {@link PresenceTest}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PresenceTest {

    private static ID fromID = new ID("ox", null, "marc.arens", "premium", null);

    private static PresenceState away = PresenceState.AWAY;

    private static String message = "I'll be back!";

    private static byte priority = 1;

    @Test
    public void testInitialPresenceBuilder() throws OXException {
        RealtimeException realtimeException = RealtimeExceptionFactory.getInstance().create(RealtimeExceptionCodes.SESSION_INVALID);
        
        Presence initialPresence = Presence.builder()
            .from(fromID)
            .error(realtimeException)
            .build();

        assertEquals(fromID, initialPresence.getFrom());
        assertNull(initialPresence.getTo());
        assertEquals(PresenceState.ONLINE, initialPresence.getState());
        assertEquals("", initialPresence.getMessage());
        assertEquals(0, initialPresence.getPriority());
        assertEquals(Type.NONE, initialPresence.getType());
        assertEquals(realtimeException, initialPresence.getError());
        assertEquals(4, initialPresence.getPayloadTrees().size());
    }

    @Test
    public void testUpdatePresenceBuilder() {
        // @formatter:off
        Presence updatePresence = Presence.builder()
            .from(fromID)
            .state(away)
            .message(message)
            .priority(priority)
            .build();
        // @formatter:on

        assertEquals(fromID, updatePresence.getFrom());
        assertNull(updatePresence.getTo());
        assertEquals(away, updatePresence.getState());
        assertEquals(message, updatePresence.getMessage());
        assertEquals(priority, updatePresence.getPriority());
        assertEquals(Type.NONE, updatePresence.getType());
        assertNull(updatePresence.getError());

        // Payload checks
        assertEquals(updatePresence.getDefaultPayloads(), updatePresence.getPayloadTrees());
        assertEquals(3, updatePresence.getPayloadTrees().size());
        ArrayList<PayloadTree> statusTrees = new ArrayList<PayloadTree>(updatePresence.getPayloadTrees(Presence.STATUS_PATH));
        ArrayList<PayloadTree> messageTrees = new ArrayList<PayloadTree>(updatePresence.getPayloadTrees(Presence.MESSAGE_PATH));
        ArrayList<PayloadTree> priorityTrees = new ArrayList<PayloadTree>(updatePresence.getPayloadTrees(Presence.PRIORITY_PATH));

        assertEquals(1, statusTrees.size());
        assertEquals(1, messageTrees.size());
        assertEquals(1, priorityTrees.size());

        assertEquals(away, statusTrees.get(0).getRoot().getData());
        assertEquals(message, messageTrees.get(0).getRoot().getData());
        assertEquals(priority, priorityTrees.get(0).getRoot().getData());

    }

    @Test
    public void testCopyConstructor() throws OXException {
        // @formatter:off
        Presence awayPresence = Presence.builder()
            .from(fromID)
            .state(away)
            .message(message)
            .priority(priority)
            .build();
        // @formatter:on

        Presence copiedAwayPresence = new Presence(awayPresence);

        awayPresence.setFrom(new ID("ox", "francisco.laguna", "premium", "macbook air"));
        awayPresence.setPriority((byte) -1);
        awayPresence.setState(PresenceState.DO_NOT_DISTURB);
        awayPresence.setMessage("Planning the future of the ox backend");

        assertEquals(fromID, copiedAwayPresence.getFrom());
        assertEquals(message, copiedAwayPresence.getMessage());
        assertEquals(priority, copiedAwayPresence.getPriority());
        assertEquals(away, copiedAwayPresence.getState());

        assertEquals(message, getFirstTreeRootData(copiedAwayPresence.getPayloadTrees(Presence.MESSAGE_PATH)));
        assertEquals(priority, getFirstTreeRootData(copiedAwayPresence.getPayloadTrees(Presence.PRIORITY_PATH)));
        assertEquals(away, getFirstTreeRootData(copiedAwayPresence.getPayloadTrees(Presence.STATUS_PATH)));
    }

    private Object getFirstTreeRootData(Collection<PayloadTree> trees) {
        List<PayloadTree> payloads = new ArrayList<PayloadTree>(trees);
        return payloads.get(0).getRoot().getData();
    }

    @Test
    public void testInitialPresencePayloads() {
        Presence presence = new Presence();
        Optional<Byte> priorityOpt = presence.getSinglePayload(Presence.PRIORITY_PATH, Byte.class);
        assertEquals(0, priorityOpt.get().byteValue());

        Optional<String> messageOpt = presence.getSinglePayload(Presence.MESSAGE_PATH, String.class);
        assertTrue(Strings.isEmpty(messageOpt.get()));

        Optional<PresenceState> statusOpt = presence.getSinglePayload(Presence.STATUS_PATH, PresenceState.class);
        assertEquals(PresenceState.ONLINE, statusOpt.get());

        assertEquals(Optional.absent(), presence.getSinglePayload(Presence.ERROR_PATH, RealtimeException.class));
    }

    @Test
    public void testMessageRemoval() {
        Presence presence = new Presence();

        Optional<String> messageOpt = presence.getSinglePayload(Presence.MESSAGE_PATH, String.class);
        assertTrue(Strings.isEmpty(messageOpt.get()));

        presence.setMessage(null);
        assertEquals(Optional.absent(), presence.getSinglePayload(Presence.MESSAGE_PATH, String.class));
    }

}
