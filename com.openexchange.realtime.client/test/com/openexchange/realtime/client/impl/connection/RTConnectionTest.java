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

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTConnectionProperties.RTConnectionType;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.impl.config.ConfigurationProvider;

/**
 * {@link RTConnectionTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class RTConnectionTest extends AbstractRTConnection implements RTMessageHandler {

    public RTConnectionTest() throws RTException {
        super();
        init(RTConnectionProperties.newBuilder("steffen.templin@premium", "secret", "ce9a19d9-b6ec-bbdf-741e-22123110db07")
            .setConnectionType(RTConnectionType.LONG_POLLING)
            .setHost("host")
            .setPort(1234)
            .setSecure(false)
            .build(), null);
    }

    private static final String SELECTOR = "rt-group-1";

    private CyclicBarrier barrier;

    private JSONValue lastMessage;

    private Throwable lastException;

    private long lastResponse;

    private JSONObject lastPing;

    private long lastAck = -1L;

    @Before
    public void before() throws Exception {
        barrier = new CyclicBarrier(2);
    }

    @Test
    public void testRTChat() throws Throwable {
        registerHandler(SELECTOR, this);

        // We joined the room. Now we expect:
        String joinResp =
            "[" +
                "{\"selector\":\"rt-group-1\",\"element\":\"message\",\"seq\":0,\"payloads\":" +
                "[" +
                    "{\"element\":\"update\",\"data\":" +
                        "{" +
                            "\"editUserId\":\"ox://steffen.templin@premium/ce9a19d9-b6ec-bbdf-741e-22123110db07\"," +
                            "\"editUser\":\"Steffen Templin\"," +
                            "\"activeClients\":0," +
                            "\"hasErrors\":false," +
                            "\"writeProtected\":false" +
                        "},\"namespace\":\"office\"" +
                    "}" +
                "],\"from\":\"synthetic.office://operations@premium/2580.24335\"}" +
            "]";
        onReceive(joinResp, true);
        barrier.await(1, TimeUnit.SECONDS);
        if (lastException != null) {
            throw lastException;
        }
        Assert.assertNotNull("lastMessage was null", lastMessage);
        Assert.assertEquals("Wrong ack to send", 0, lastAck);

        long toSleep = (lastResponse + 61000L) - System.currentTimeMillis();
        Thread.sleep(toSleep);
        Assert.assertNotNull("lastPing was null", lastPing);

        close();
    }

    @Override
    public void onMessage(JSONValue message) {
        lastMessage = message;
        lastResponse = System.currentTimeMillis();
        try {
            barrier.await();
        } catch (Throwable t) {
            lastException = t;
        }
    }

    @Override
    public void login(RTMessageHandler messageHandler) throws RTException {
        if (messageHandler != null) {
            registerHandler0(ConfigurationProvider.getInstance().getDefaultSelector(), messageHandler);
        }
    }

    @Override
    public void sendACK(JSONObject ack) throws RTException {
        try {
            JSONArray jsonArray = ack.getJSONArray("seq");
            lastAck = jsonArray.getLong(0);
        } catch (Throwable t) {
            lastException = t;
        }
    }

    @Override
    public void sendPing(JSONObject ping) throws RTException {
        lastPing = ping;
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.client.impl.connection.AbstractRTConnection#reconnect()
     */
    @Override
    protected void reconnect() throws RTException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.client.impl.connection.AbstractRTConnection#doSend(org.json.JSONValue)
     */
    @Override
    protected void doSend(JSONValue message) throws RTException {
        // TODO Auto-generated method stub

    }

}
