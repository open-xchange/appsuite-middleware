/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Random;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.AttributeWriter;
import com.openexchange.ajax.config.BetaWriter;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.user.actions.SetAttributeRequest;
import com.openexchange.ajax.user.actions.SetAttributeResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.common.tools.RandomString;

/**
 * {@link Bug26354Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug26354Test extends AbstractAJAXSession {

    private static final String ATTRIBUTE_NAME = "testForBug26354";

    private static final int ITERATIONS = 100;

    static final TimeZone[] TIME_ZONES = new TimeZone[3];
    static {
        TIME_ZONES[0] = TimeZones.PST;
        TIME_ZONES[1] = TimeZones.UTC;
        TIME_ZONES[2] = TimeZones.EET;
    }

    private final AttributeWriter[] writer = new AttributeWriter[2];
    private final Thread[] thread = new Thread[writer.length];

    private AJAXClient client;
    private int userId;

    public Bug26354Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId = client.getValues().getUserId();

        writer[0] = new BetaWriter(testUser);
        thread[0] = new Thread(writer[0]);
        writer[1] = new AttributeWriter(Tree.TimeZone, testUser) {

            private final Random r = new Random();

            @Override
            protected Object getValue() {
                return TIME_ZONES[r.nextInt(3)].getID();
            }
        };
        thread[1] = new Thread(writer[1]);

        for (int i = 0; i < thread.length; i++) {
            thread[i].start();
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            for (int i = 0; i < writer.length; i++) {
                writer[i].stop();
            }
            for (int i = 0; i < thread.length; i++) {
                thread[i].join();
            }
            for (int i = 0; i < writer.length; i++) {
                final Throwable throwable = writer[i].getThrowable();
                assertNull("Expected no Throwable, but there is one: " + throwable, throwable);
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testForDeadlocks() throws Throwable {
        boolean stop = false;
        for (int i = 0; i < ITERATIONS && !stop; i++) {
            String value = RandomString.generateChars(64);
            SetAttributeResponse response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, value, false, false));
            if (response.hasError()) {
                OXException e = response.getException();
                String logMessage = e.getLogMessage();
                assertFalse("Bug 26354 appears again. Deadlock in database detected.", logMessage.contains("Deadlock"));
            }
            assertTrue("Setting the attribute was not successful.", response.isSuccess());
            for (int j = 0; j < writer.length; j++) {
                stop = stop || null != writer[j].getThrowable();
            }
        }
    }
}
