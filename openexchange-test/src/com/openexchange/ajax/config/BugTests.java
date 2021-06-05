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

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.SetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.test.common.tools.RandomString;

/**
 * Tests resulting from bug reports.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class BugTests extends AbstractAJAXSession {

    /**
     * Tests if the mail folder are sent correctly to the GUI.
     */
    @Test
    public void testBug5607() throws Throwable {
        final String drafts = getClient().execute(new GetRequest(Tree.DraftsFolder)).getString();
        assertNotNull("Can't get drafts folder.", drafts);
        final String sent = getClient().execute(new GetRequest(Tree.SentFolder)).getString();
        assertNotNull("Can't get sent folder.", sent);
        final String spam = getClient().execute(new GetRequest(Tree.SpamFolder)).getString();
        assertNotNull("Can't get spam folder.", spam);
        final String trash = getClient().execute(new GetRequest(Tree.TrashFolder)).getString();
        assertNotNull("Can't get trash folder.", trash);
    }

    /**
     * Tests if calendar and task notifications can be properly turned on and
     * off.
     * 
     * @throws Throwable if an exception occurs.
     */
    @Test
    public void testBug6462() throws Throwable {
        for (Tree tree : new Tree[] { Tree.CalendarNotification, Tree.TaskNotification }) {
            boolean origValue = getClient().execute(new GetRequest(tree)).getBoolean();
            for (final Boolean test : new Boolean[] { Boolean.TRUE, Boolean.FALSE}) {
                getClient().execute(new SetRequest(tree, test.toString()));
                boolean testValue = getClient().execute(new GetRequest(tree)).getBoolean();
                assertEquals("Setting calendar/task notification failed.", test, B(testValue));
            }
            getClient().execute(new SetRequest(tree, B(origValue)));
            boolean testValue = getClient().execute(new GetRequest(tree)).getBoolean();
            assertTrue("Restoring original value failed.", origValue == testValue);
        }
    }

    /**
     * Tests if any desired senderAddress can be written to the config tree.
     * 
     * @throws Throwable
     */
    @Test
    public void testWriteSenderAddress() throws Throwable {
        // Get original value.
        final String origAddress = getClient().execute(new GetRequest(Tree.SendAddress)).getString();
        try {
            // Write something for the test.
            String garbage;
            do {
                garbage = RandomString.generateLetter(20);
            } while (garbage.equals(origAddress));
            final SetResponse response = getClient().execute(new SetRequest(Tree.SendAddress, garbage, false));
            if (!response.hasError()) {
                fail("SendAddress in config tree can be written with garbage.");
            }
        } finally {
            // Restore original value
            getClient().execute(new SetRequest(Tree.SendAddress, origAddress));
        }
    }
}
