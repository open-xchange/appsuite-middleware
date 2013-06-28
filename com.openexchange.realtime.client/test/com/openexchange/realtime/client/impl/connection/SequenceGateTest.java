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

package com.openexchange.realtime.client.impl.connection;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.client.impl.connection.SequenceGate;

/**
 * {@link SequenceGateTest}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SequenceGateTest {

    private SequenceGate gate;

    private CyclicBarrier barrier;

    private Taker taker;

    private Thread takerThread;

    @Before
    public void before() {
        gate = new SequenceGate();
        barrier = new CyclicBarrier(2);
        taker = new Taker(gate, barrier);
        takerThread = new Thread(taker);
        takerThread.start();
    }

    @After
    public void after() {
        takerThread.interrupt();
        takerThread = null;
        gate = null;
        taker = null;
        barrier = null;
    }

    @Test
    public void testFirstStanzaGetsLost() throws Exception {
        JSONValue m0 = createMessage();
        JSONValue m1 = createMessage();
        JSONValue m2 = createMessage();

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m1, 1L));
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m0, 0L));

        barrier.await(3, TimeUnit.SECONDS);
        List<JSONValue> receivedMessages = taker.getReceivedMessages();
        Assert.assertNotNull("receivedMessages was null.", receivedMessages);
        Assert.assertEquals("Wrong size for receivedMessages", 2, receivedMessages.size());
        Assert.assertEquals("Wrong order of receivedMessages", m0, receivedMessages.get(0));
        Assert.assertEquals("Wrong order of receivedMessages", m1, receivedMessages.get(1));

        barrier.reset();
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m2, 2L));
        barrier.await(3, TimeUnit.SECONDS);
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNotNull("receivedMessages was null.", receivedMessages);
        Assert.assertEquals("Wrong size for receivedMessages", 1, receivedMessages.size());
        Assert.assertEquals("Wrong order of receivedMessages", m2, receivedMessages.get(0));
    }

    @Test
    public void testLostStanza() throws Exception {
        JSONValue m0 = createMessage();
        JSONValue m1 = createMessage();
        JSONValue m2 = createMessage();

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m0, 0L));
        barrier.await(3, TimeUnit.SECONDS);
        List<JSONValue> receivedMessages = taker.getReceivedMessages();
        Assert.assertNotNull("receivedMessages was null.", receivedMessages);
        Assert.assertEquals("Wrong order of receivedMessages", m0, receivedMessages.get(0));

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m2, 2L));
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNull("receivedMessages was not null.", receivedMessages);

        barrier.reset();
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m1, 1L));
        barrier.await(3, TimeUnit.SECONDS);
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNotNull("receivedMessages was null.", receivedMessages);
        Assert.assertEquals("Wrong size for receivedMessages", 2, receivedMessages.size());
        Assert.assertEquals("Wrong order of receivedMessages", m1, receivedMessages.get(0));
        Assert.assertEquals("Wrong order of receivedMessages", m2, receivedMessages.get(1));
    }

    @Test
    public void testGapGreaterOne() throws Exception {
        JSONValue m0 = createMessage();
        JSONValue m1 = createMessage();
        JSONValue m2 = createMessage();
        JSONValue m3 = createMessage();

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m2, 2L));
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m3, 3L));
        List<JSONValue> receivedMessages = taker.getReceivedMessages();
        Assert.assertNull("receivedMessages was not null.", receivedMessages);

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m1, 1L));
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNull("receivedMessages was not null.", receivedMessages);

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m0, 0L));
        barrier.await(3, TimeUnit.SECONDS);
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNotNull("receivedMessages was null.", receivedMessages);
        Assert.assertEquals("Wrong size for receivedMessages", 4, receivedMessages.size());
        Assert.assertEquals("Wrong order of receivedMessages", m0, receivedMessages.get(0));
        Assert.assertEquals("Wrong order of receivedMessages", m1, receivedMessages.get(1));
        Assert.assertEquals("Wrong order of receivedMessages", m2, receivedMessages.get(2));
        Assert.assertEquals("Wrong order of receivedMessages", m3, receivedMessages.get(3));
    }

    @Test
    public void testFirstFillHigherThenFillLowerGap() throws Exception {
        JSONValue m0 = createMessage();
        JSONValue m1 = createMessage();
        JSONValue m2 = createMessage();
        JSONValue m3 = createMessage();
        JSONValue m4 = createMessage();
        JSONValue m5 = createMessage();

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m0, 0L));
        barrier.await(3, TimeUnit.SECONDS);
        List<JSONValue> receivedMessages = taker.getReceivedMessages();
        Assert.assertNotNull("receivedMessages was null.", receivedMessages);
        Assert.assertEquals("Wrong size for receivedMessages", 1, receivedMessages.size());
        Assert.assertEquals("Wrong order of receivedMessages", m0, receivedMessages.get(0));

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m2, 2L));
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m3, 3L));
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m5, 5L));
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNull("receivedMessages was not null.", receivedMessages);

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m4, 4L));
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNull("receivedMessages was not null.", receivedMessages);

        barrier.reset();
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m1, 1L));
        barrier.await(3, TimeUnit.SECONDS);
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNotNull("receivedMessages was null.", receivedMessages);
        Assert.assertEquals("Wrong size for receivedMessages", 5, receivedMessages.size());
        Assert.assertEquals("Wrong order of receivedMessages", m1, receivedMessages.get(0));
        Assert.assertEquals("Wrong order of receivedMessages", m2, receivedMessages.get(1));
        Assert.assertEquals("Wrong order of receivedMessages", m3, receivedMessages.get(2));
        Assert.assertEquals("Wrong order of receivedMessages", m4, receivedMessages.get(3));
        Assert.assertEquals("Wrong order of receivedMessages", m5, receivedMessages.get(4));
    }
    
    @Test
    public void testFirstFillLowerThenFillHigherGap() throws Exception {
        JSONValue m0 = createMessage();
        JSONValue m1 = createMessage();
        JSONValue m2 = createMessage();
        JSONValue m3 = createMessage();
        JSONValue m4 = createMessage();
        JSONValue m5 = createMessage();

        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m1, 1L));
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m2, 2L));
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m3, 3L));
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m5, 5L));
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m0, 0L));
        barrier.await(3, TimeUnit.SECONDS);
        List<JSONValue> receivedMessages = taker.getReceivedMessages();
        
        Assert.assertNotNull("receivedMessages was null.", receivedMessages);
        Assert.assertEquals("Wrong size for receivedMessages", 4, receivedMessages.size());
        Assert.assertEquals("Wrong order of receivedMessages", m0, receivedMessages.get(0));
        Assert.assertEquals("Wrong order of receivedMessages", m1, receivedMessages.get(1));
        Assert.assertEquals("Wrong order of receivedMessages", m2, receivedMessages.get(2));
        Assert.assertEquals("Wrong order of receivedMessages", m3, receivedMessages.get(3));

        barrier.reset();
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m4, 4L));
        barrier.await(3, TimeUnit.SECONDS);
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNotNull("receivedMessages was not null.", receivedMessages);
        Assert.assertEquals("Wrong size for receivedMessages", 2, receivedMessages.size());
        Assert.assertEquals("Wrong order of receivedMessages", m4, receivedMessages.get(0));
        Assert.assertEquals("Wrong order of receivedMessages", m5, receivedMessages.get(1));
    }

    @Test
    public void testBufferSize() throws Exception {
        JSONValue m0 = createMessage();
        for (int i = 1; i <= SequenceGate.BUFFER_SIZE; i++) {
            JSONValue tmp = createMessage();
            Assert.assertTrue("Could not enqueue message.", gate.enqueue(tmp, (long) i));
        }

        List<JSONValue> receivedMessages = taker.getReceivedMessages();
        Assert.assertNull("receivedMessages was not null.", receivedMessages);
        JSONValue last = createMessage();
        Assert.assertFalse("Message was added to full buffer", gate.enqueue(last, (long) (SequenceGate.BUFFER_SIZE + 1)));
        Assert.assertTrue("Could not enqueue message.", gate.enqueue(m0, 0L));

        barrier.await(3, TimeUnit.SECONDS);
        receivedMessages = taker.getReceivedMessages();
        Assert.assertNotNull("receivedMessages was null.", receivedMessages);
        Assert.assertEquals("Wrong size for receivedMessages", SequenceGate.BUFFER_SIZE + 1, receivedMessages.size());
    }

    private JSONValue createMessage() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", UUID.randomUUID().toString());
        return json;
    }

    private static class Taker implements Runnable {

        private final SequenceGate gate;

        private final CyclicBarrier barrier;

        private final AtomicReference<List<JSONValue>> receivedMessagesRef;

        public Taker(SequenceGate gate, CyclicBarrier barrier) {
            super();
            this.gate = gate;
            this.barrier = barrier;
            receivedMessagesRef = new AtomicReference<List<JSONValue>>();
        }

        @Override
        public void run() {
            while (true) {
                if (Thread.interrupted()) {
                    return;
                }

                try {
                    List<JSONValue> receivedMessages = gate.take();
                    receivedMessagesRef.compareAndSet(null, receivedMessages);
                    barrier.await();
                } catch (InterruptedException e) {
                    return;
                } catch (BrokenBarrierException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        public List<JSONValue> getReceivedMessages() {
            List<JSONValue> receivedMessages = receivedMessagesRef.get();
            receivedMessagesRef.compareAndSet(receivedMessages, null);
            return receivedMessages;
        }
    }

}
