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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.ajax.config.ConfigTools.readSetting;
import static com.openexchange.ajax.config.ConfigTools.storeSetting;

import com.meterware.httpunit.WebConversation;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.SetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.tools.RandomString;

/**
 * Tests resulting from bug reports.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class BugTests extends AbstractAJAXSession {

    /**
     * Path to the configuration parameter.
     */
    private static final String DRAFTS_PATH = "mail/folder/drafts";

    /**
     * Path to the configuration parameter.
     */
    private static final String SENT_PATH = "mail/folder/sent";

    /**
     * Path to the configuration parameter.
     */
    private static final String SPAM_PATH = "mail/folder/spam";

    /**
     * Path to the configuration parameter.
     */
    private static final String TRASH_PATH = "mail/folder/trash";

    /**
     * Path to the configuration parameter if the user wants to receive calendar
     * notifications or not.
     */
    private static final String CAL_NOT_PATH = "calendarnotification";

    /**
     * Path to the configuration parameter if the user wants to receive task
     * notifications or not.
     */
    private static final String TASK_NOT_PATH = "tasknotification";

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public BugTests(final String name) {
        super(name);
    }

    private WebConversation getWebConversation() {
        return getClient().getSession().getConversation();
    }

    private String getHostName() {
        return AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
    }

    private String getSessionId() {
        return getClient().getSession().getId();
    }

    /**
     * Tests if the mail folder are sent correctly to the GUI.
     */
    public void testBug5607() throws Throwable {
        final String drafts = readSetting(getWebConversation(), getHostName(),
            getSessionId(), DRAFTS_PATH);
        assertNotNull("Can't get drafts folder.", drafts);

        final String sent = readSetting(getWebConversation(), getHostName(),
            getSessionId(), SENT_PATH);
        assertNotNull("Can't get sent folder.", sent);

        final String spam = readSetting(getWebConversation(), getHostName(),
            getSessionId(), SPAM_PATH);
        assertNotNull("Can't get spam folder.", spam);

        final String trash = readSetting(getWebConversation(), getHostName(),
            getSessionId(), TRASH_PATH);
        assertNotNull("Can't get trash folder.", trash);
    }

    /**
     * Tests if calendar and task notifications can be properly turned on and
     * off.
     * @throws Throwable if an exception occurs.
     */
    public void testBug6462() throws Throwable {
        for (String path : new String[] { CAL_NOT_PATH, TASK_NOT_PATH }) {
            final String origValue = readSetting(getWebConversation(),
                getHostName(), getSessionId(), path);
            for (boolean test : new boolean[] { true, false }) {
                storeSetting(getWebConversation(), getHostName(),
                    getSessionId(), path, Boolean.toString(test));
                final String testValue = readSetting(getWebConversation(),
                    getHostName(), getSessionId(), path);
                assertEquals("Setting calendar notification failed.", test, Boolean
                    .parseBoolean(testValue));
            }
            storeSetting(getWebConversation(), getHostName(), getSessionId(),
                path, origValue);
            final String testValue = readSetting(getWebConversation(),
                getHostName(), getSessionId(), path);
            assertEquals("Restoring original value failed.", origValue,
                testValue);
        }
    }

    /**
     * Tests if any desired senderAddress can be written to the config tree.
     * @throws Throwable
     */
    public void testWriteSenderAddress() throws Throwable {
        final AJAXClient client = getClient();
        // Get original value.
        final String origAddress = ConfigTools.get(client, new GetRequest(Tree
            .SendAddress)).getString();
        try {
            // Write something for the test.
            String garbage;
            do {
                garbage = RandomString.generateLetter(20);
            } while (garbage.equals(origAddress));
            final SetResponse response = ConfigTools.set(client, new SetRequest(Tree
                .SendAddress, garbage, false));
            if (!response.hasError()) {
                fail("SendAddress in config tree can be written with garbage.");
            }
        } finally {
            // Restore original value
            ConfigTools.set(client, new SetRequest(Tree.SendAddress,
                origAddress));
        }
    }
}
