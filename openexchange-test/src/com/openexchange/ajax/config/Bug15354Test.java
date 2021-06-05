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

package com.openexchange.ajax.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * Verifies that bug 15354 does not appear again.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15354Test extends AbstractAJAXSession {

    private static final int ITERATIONS = 1000;

    private final BetaWriter[] writer = new BetaWriter[5];
    private final Thread[] thread = new Thread[writer.length];

    private AJAXClient client;
    private Object[] origAliases;

    public Bug15354Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        origAliases = client.execute(new GetRequest(Tree.MailAddresses)).getArray();
        assertNotNull("Aliases are null.", origAliases);
        Arrays.sort(origAliases);
        for (int i = 0; i < writer.length; i++) {
            writer[i] = new BetaWriter(testUser, true);
            thread[i] = new Thread(writer[i]);
        }
        for (int i = 0; i < thread.length; i++) {
            thread[i].start();
            Thread.sleep(10L); // Avoid concurrent modification of last login recorder
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
    public void testAliases() throws Throwable {
        boolean stop = false;
        for (int i = 0; i < ITERATIONS && !stop; i++) {
            Object[] testAliases = client.execute(new GetRequest(Tree.MailAddresses)).getArray();
            if (null == testAliases) {
                stop = true;
            } else if (origAliases.length != testAliases.length) {
                stop = true;
            } else {
                Arrays.sort(testAliases);
                boolean match = true;
                for (int j = 0; j < origAliases.length && match; j++) {
                    if (!origAliases[j].equals(testAliases[j])) {
                        match = false;
                    }
                }
                stop = stop || !match;
            }
            for (int j = 0; j < writer.length; j++) {
                stop = stop || null != writer[j].getThrowable();
            }
        }
        // Final test.
        Object[] testAliases = client.execute(new GetRequest(Tree.MailAddresses)).getArray();
        assertNotNull("Aliases are null.", origAliases);
        assertNotNull("Aliases are null.", testAliases);
        assertEquals("Number of aliases are not equal.", origAliases.length, testAliases.length);
        Arrays.sort(origAliases);
        Arrays.sort(testAliases);
        for (int i = 0; i < origAliases.length; i++) {
            assertEquals("Aliases are not the same.", origAliases[i], testAliases[i]);
        }
    }
}
