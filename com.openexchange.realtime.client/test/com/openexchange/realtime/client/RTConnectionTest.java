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

package com.openexchange.realtime.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.realtime.client.RTConnectionProperties.RTConnectionType;

/**
 * {@link RTConnectionTest}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class RTConnectionTest {

    @Test
    public void testDirectChat() {
        RTConnectionProperties properties1 = buildProperties("steffen.templin@premium", "secret");
        RTConnectionProperties properties2 = buildProperties("thorben.betten@premium", "secret");
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<JSONValue> expectedMessage = new AtomicReference<JSONValue>();
            RTConnection con1 = RTConnectionFactory.newConnection(properties1);
            RTConnection con2 = RTConnectionFactory.newConnection(properties2);
            String id1 = con1.connect(null);
            String id2 = con2.connect(new RTMessageHandler() {
                @Override
                public void onMessage(JSONValue message) {
                    expectedMessage.set(message);
                    latch.countDown();
                }
            });

            JSONObject json = new JSONObject();
            json.put("element", "message");
            json.put("to", id2.toString());
            json.put("selector", "default");
            json.put("from", "ox://" + properties1.getUser() + '/' + id1);
            json.put("to", "ox://" + properties2.getUser() + '/' + id2);
            con1.post(json);

            try {
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    Assert.fail("Did not receive expected message.");
                }
            } catch (InterruptedException e) {
            }

            Assert.assertNotNull("Expected message was null.", expectedMessage.get());
            con1.close();
            con2.close();
        } catch (RTException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static RTConnectionProperties buildProperties(String user, String password) {
        return RTConnectionProperties.newBuilder(user, password)
            .setConnectionType(RTConnectionType.LONG_POLLING)
            .setProtocol("http")
            .setHost("localhost")
            .setPort(80)
            .build();
    }

}
