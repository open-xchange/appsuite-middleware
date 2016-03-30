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
import java.util.Random;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.SetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.tools.RandomString;

/**
 * Tests resulting from bug reports.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class BugTests extends AbstractAJAXSession {

    private AJAXClient client;

    private Random rand;

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public BugTests(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        rand = new Random(System.currentTimeMillis());
    }

    /**
     * Tests if the mail folder are sent correctly to the GUI.
     */
    public void testBug5607() throws Throwable {
        final String drafts = client.execute(new GetRequest(Tree.DraftsFolder)).getString();
        assertNotNull("Can't get drafts folder.", drafts);
        final String sent = client.execute(new GetRequest(Tree.SentFolder)).getString();
        assertNotNull("Can't get sent folder.", sent);
        final String spam = client.execute(new GetRequest(Tree.SpamFolder)).getString();
        assertNotNull("Can't get spam folder.", spam);
        final String trash = client.execute(new GetRequest(Tree.TrashFolder)).getString();
        assertNotNull("Can't get trash folder.", trash);
    }

    /**
     * Tests if calendar and task notifications can be properly turned on and
     * off.
     * @throws Throwable if an exception occurs.
     */
    public void testBug6462() throws Throwable {
        for (Tree tree : new Tree[] { Tree.CalendarNotification, Tree.TaskNotification }) {
            boolean origValue = client.execute(new GetRequest(tree)).getBoolean();
            for (final boolean test : new boolean[] { true, false }) {
                client.execute(new SetRequest(tree, Boolean.toString(test)));
                boolean testValue = client.execute(new GetRequest(tree)).getBoolean();
                assertEquals("Setting calendar/task notification failed.", test, testValue);
            }
            client.execute(new SetRequest(tree, B(origValue)));
            boolean testValue = client.execute(new GetRequest(tree)).getBoolean();
            assertEquals("Restoring original value failed.", origValue, testValue);
        }
    }

    /**
     * Tests if any desired senderAddress can be written to the config tree.
     * @throws Throwable
     */
    public void testWriteSenderAddress() throws Throwable {
        // Get original value.
        final String origAddress = client.execute(new GetRequest(Tree.SendAddress)).getString();
        try {
            // Write something for the test.
            String garbage;
            do {
                garbage = RandomString.generateLetter(20);
            } while (garbage.equals(origAddress));
            final SetResponse response = client.execute(new SetRequest(Tree.SendAddress, garbage, false));
            if (!response.hasError()) {
                fail("SendAddress in config tree can be written with garbage.");
            }
        } finally {
            // Restore original value
            client.execute(new SetRequest(Tree.SendAddress, origAddress));
        }
    }
}
