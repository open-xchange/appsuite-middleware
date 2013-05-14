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
package com.openexchange.realtime.atmosphere.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResource.TRANSPORT;
import org.atmosphere.cpr.AtmosphereResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.atmosphere.impl.stanza.writer.StanzaWriter;
import com.openexchange.realtime.atmosphere.osgi.AtmosphereServiceRegistry;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link RTAtmosphereHandlerTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class RTAtmosphereHandlerTest extends RTAtmosphereHandler {

    @Before
    public void before() {
        init();
    }

    @Test
    public void testDrainOutboxWithNextSequenceMessageOnly() throws Exception {
        AtmosphereResponse mockResponse = mock(AtmosphereResponse.class);
        AtmosphereResource mockResource = mock(AtmosphereResource.class);
        when(mockResource.getResponse()).thenReturn(mockResponse);
        when(mockResource.isCancelled()).thenReturn(false);
        when(mockResource.transport()).thenReturn(TRANSPORT.LONG_POLLING);
        when(mockResource.getResponse().isCommitted()).thenReturn(false);

        ID id = new ID("synthetic", "testcomponent", "", "", "resource");
        concreteIDToResourceMap.put(id, mockResource);

        Message msg = new Message();
        msg.setTo(id);
        msg.setFrom(id);
        msg.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(0, "json", "atmosphere", "nextSequence").build()));

        assertTrue("Message was not sent", drainOutbox(id, msg));
        JSONArray expected = new JSONArray();
        expected.put(new StanzaWriter().write(msg));
        verify(mockResponse).write(expected.toString());
        assertNull("Committed resource was not removed", concreteIDToResourceMap.get(id));
    }

    @Test
    public void testDrainOutboxWithNonEmptyOutboxOnly() throws Exception {
        AtmosphereResponse mockResponse = mock(AtmosphereResponse.class);
        AtmosphereResource mockResource = mock(AtmosphereResource.class);
        when(mockResource.getResponse()).thenReturn(mockResponse);
        when(mockResource.isCancelled()).thenReturn(false);
        when(mockResource.transport()).thenReturn(TRANSPORT.LONG_POLLING);
        when(mockResource.getResponse().isCommitted()).thenReturn(false);

        ID id = new ID("synthetic", "testcomponent", "", "", "resource");
        concreteIDToResourceMap.put(id, mockResource);

        List<EnqueuedStanza> outbox = outboxFor(id);
        Stanza stanza = createStanza(id, "key", "value");
        outbox.add(new EnqueuedStanza(stanza));

        assertTrue("Message was not sent", drainOutbox(id, null));
        assertNull("Outbox was not removed", outboxes.get(id));

        JSONArray expected = new JSONArray();
        expected.put(new StanzaWriter().write(stanza));
        verify(mockResponse).write(expected.toString());
        assertNull("Committed resource was not removed", concreteIDToResourceMap.get(id));
    }

    @Test
    public void testDrainOutboxWithResendBuffer() throws Exception {
        AtmosphereResponse mockResponse = mock(AtmosphereResponse.class);
        AtmosphereResource mockResource = mock(AtmosphereResource.class);
        when(mockResource.getResponse()).thenReturn(mockResponse);
        when(mockResource.isCancelled()).thenReturn(false);
        when(mockResource.transport()).thenReturn(TRANSPORT.LONG_POLLING);
        when(mockResource.getResponse().isCommitted()).thenReturn(false);

        ID id = new ID("synthetic", "testcomponent", "", "", "resource");
        concreteIDToResourceMap.put(id, mockResource);

        SortedSet<EnqueuedStanza> resendBuffer = resendBufferFor(id);
        Stanza stanza = createStanza(id, "key", "value");
        stanza.setSequenceNumber(0L);
        resendBuffer.add(new EnqueuedStanza(stanza));

        assertTrue("Message was not sent", drainOutbox(id, null));
        resendBuffer = resendBufferFor(id);
        assertTrue("Buffer was empty although no ACK was received.", resendBuffer.size() == 1);

        JSONArray expected = new JSONArray();
        expected.put(new StanzaWriter().write(stanza));
        verify(mockResponse).write(expected.toString());

        JSONObject ack = new JSONObject();
        ack.put("type", "ack");
        ack.put("seq", 0);
        handlePost(ack.toString(), id, null);
        assertTrue("Buffer was not empty although ACK was received.", resendBuffer.size() == 0);
        assertNull("Committed resource was not removed", concreteIDToResourceMap.get(id));
    }

    @Test
    public void testResourceWasAlreadyCommitted() throws Exception {
        AtmosphereResponse mockResponse = mock(AtmosphereResponse.class);
        AtmosphereResource mockResource = mock(AtmosphereResource.class);
        when(mockResource.getResponse()).thenReturn(mockResponse);
        when(mockResource.isCancelled()).thenReturn(false);
        when(mockResource.transport()).thenReturn(TRANSPORT.LONG_POLLING);
        when(mockResource.getResponse().isCommitted()).thenReturn(true);

        ID id = new ID("synthetic", "testcomponent", "", "", "resource");
        concreteIDToResourceMap.put(id, mockResource);

        EnqueuedStanza outboxStanza = new EnqueuedStanza(createStanza(id, "key", "value"));
        List<EnqueuedStanza> outbox = outboxFor(id);
        outbox.add(outboxStanza);

        EnqueuedStanza rbStanza = new EnqueuedStanza(createStanza(id, "key", "value"));
        SortedSet<EnqueuedStanza> resendBuffer = resendBufferFor(id);
        resendBuffer.add(rbStanza);

        assertFalse("Something was sent although resource was already committed", drainOutbox(id));
        outbox = outboxes.get(id);
        assertNotNull("Outbox was null", outbox);
        assertEquals("Wrong outbox size", 1, outbox.size());
        assertEquals("Send counter was not incremented for outbox stanza", 1, outboxStanza.count);

        resendBuffer = resendBuffers.get(id);
        assertNotNull("ResendBuffer was null", resendBuffer);
        assertEquals("Wrong resend buffer size", 1, resendBuffer.size());
        assertEquals("Send counter was not incremented for rb stanza", 1, rbStanza.count);

        assertNull("Committed resource was not removed", concreteIDToResourceMap.get(id));
    }

    @Test
    public void testStanzasAreRemovedAfterInfiniteResendTries() throws Exception {
        AtmosphereResponse mockResponse = mock(AtmosphereResponse.class);
        AtmosphereResource mockResource = mock(AtmosphereResource.class);
        when(mockResource.getResponse()).thenReturn(mockResponse);
        when(mockResource.isCancelled()).thenReturn(false);
        when(mockResource.transport()).thenReturn(TRANSPORT.LONG_POLLING);
        when(mockResource.getResponse().isCommitted()).thenReturn(false);
        when(mockResource.getResponse().write(anyString())).thenThrow(new RuntimeException());

        ID id = new ID("synthetic", "testcomponent", "", "", "resource");
        concreteIDToResourceMap.put(id, mockResource);

        EnqueuedStanza outboxStanza = new EnqueuedStanza(createStanza(id, "key", "value"));
        List<EnqueuedStanza> outbox = outboxFor(id);
        outbox.add(outboxStanza);

        EnqueuedStanza rbStanza = new EnqueuedStanza(createStanza(id, "key", "value"));
        SortedSet<EnqueuedStanza> resendBuffer = resendBufferFor(id);
        resendBuffer.add(rbStanza);

        for (int i = 0; i < EnqueuedStanza.INFINITY; i++) {
            assertFalse("Something was sent although an exception was thrown", drainOutbox(id));
            resendBuffer = resendBuffers.get(id);
            outbox = outboxes.get(id);
            assertTrue("Buffer was empty after less than infinite resend tries", resendBuffer.size() == 1);
            assertTrue("Outbox was empty after less than infinite resend tries", outbox.size() == 1);

            concreteIDToResourceMap.put(id, mockResource);
        }

        assertFalse("Something was sent although an exception was thrown", drainOutbox(id));
        resendBuffer = resendBuffers.get(id);
        outbox = outboxes.get(id);
        assertTrue("Buffer was not empty after infinite resend tries", resendBuffer.size() == 0);
        assertNull("Outbox existed after infinite resend tries", outbox);
    }

    @Override
    public void init() {
        // Don't init resource reaper
        atmosphereServiceRegistry = AtmosphereServiceRegistry.getInstance();
        generalToConcreteIDMap = new IDMap<Set<ID>>();
        concreteIDToResourceMap = new ConcurrentHashMap<ID, AtmosphereResource>();
        outboxes = new ConcurrentHashMap<ID, List<EnqueuedStanza>>();
        idsPerSession = new ConcurrentHashMap<String, Set<ID>>();
        sequenceNumbers = new ConcurrentHashMap<ID, Long>();
        resendBuffers = new ConcurrentHashMap<ID, SortedSet<EnqueuedStanza>>();
    }

    private Stanza createStanza(ID id, String key, Object value) {
        Message msg = new Message();
        msg.setTo(id);
        msg.setFrom(id);
        msg.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(value, "json", "atmosphere", key).build()));

        return msg;
    }

}
