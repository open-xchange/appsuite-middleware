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

package com.openexchange.ajax.config;

import static com.openexchange.java.Autoboxing.B;
import java.util.Arrays;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
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
    private boolean origValue;
    private Object[] origAliases;

    public Bug15354Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        origValue = client.execute(new GetRequest(Tree.Beta)).getBoolean();
        origAliases = client.execute(new GetRequest(Tree.MailAddresses)).getArray();
        assertNotNull("Aliases are null.", origAliases);
        Arrays.sort(origAliases);
        for (int i = 0; i < writer.length; i++) {
            writer[i] = new BetaWriter(User.User1, true);
            thread[i] = new Thread(writer[i]);
        }
        for (int i = 0; i < thread.length; i++) {
            thread[i].start();
            Thread.sleep(10L); // Avoid concurrent modification of last login recorder
        }
    }

    @Override
    public void tearDown() throws Exception {
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
        client.execute(new SetRequest(Tree.Beta, B(origValue)));
        super.tearDown();
    }

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
