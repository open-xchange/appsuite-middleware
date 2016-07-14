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

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Random;
import org.json.JSONObject;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.tools.RandomString;

/**
 * This class contains tests for added funtionalities of the configuration tree.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class FunctionTests extends AbstractAJAXSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FunctionTests.class);

    private AJAXClient client;

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public FunctionTests(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    /**
     * Tests if the idle timeout for uploaded files is sent to the GUI.
     * @throws Throwable if an exception occurs.
     */
    public void testMaxUploadIdleTimeout() throws Throwable {
        final int value = client.execute(new GetRequest(Tree.MaxUploadIdleTimeout)).getInteger();
        LOG.info("Max upload idle timeout: {}", value);
        assertTrue("Got no value for the maxUploadIdleTimeout configuration " + "parameter.", value > 0);
    }

    /**
     * Maximum time difference between server and client. This test fails if
     * a greater difference is detected.
     */
    private static final long MAX_DIFFERENCE = 1000;

    /**
     * Tests if the current time is sent by the server through the configuration
     * interface.
     */
    public void testCurrentTime() throws Throwable {
        final long firstServerTime;
        {
            final GetRequest request = new GetRequest(Tree.CurrentTime);
            final GetResponse response = Executor.execute(client, request);
            firstServerTime = response.getLong();
        }
        final int randomWait = new Random(System.currentTimeMillis()).nextInt(10000);
        Thread.sleep(randomWait);
        final long secondServerTime;
        final long totalDuration;
        {
            final GetRequest request = new GetRequest(Tree.CurrentTime);
            final GetResponse response = Executor.execute(client, request);
            secondServerTime = response.getLong();
            totalDuration = response.getTotalDuration();
        }
        final Date sTime = client.getValues().getServerTime();
        final long localTime = System.currentTimeMillis();
        LOG.info("Local time: " + localTime + " Server time: " + sTime.getTime());
        final long difference = Math.abs(secondServerTime - totalDuration - randomWait - firstServerTime);
        LOG.info("Time difference: " + difference);
        assertTrue("Too big time difference: " + difference, difference < MAX_DIFFERENCE);
    }

    /**
     * Tests if the server gives the context identifier.
     */
    public void testContextID() throws Throwable {
        final int value = client.execute(new GetRequest(Tree.ContextID)).getInteger();
        LOG.info("Context identifier: " + value);
        assertTrue("Got no value for the contextID configuration parameter.", value > 0);
    }

    /**
     * Tests if the GUI value can be written and read correctly.
     */
    public void testGUI() throws Throwable {
        GetResponse origGet = client.execute(new GetRequest(Tree.GUI));
        String testValue = RandomString.generateChars(20);
        try {
            client.execute(new SetRequest(Tree.GUI, testValue));
            GetResponse testGet = client.execute(new GetRequest(Tree.GUI));
            assertEquals("Written GUI value differs from read one.", testValue, testGet.getString());
        } finally {
            client.execute(new SetRequest(Tree.GUI, origGet.getData()));
        }
    }

    /**
     * Checks if the new preferences entry extras works.
     */
    public void testConfigJumpFlag() throws Throwable {
        final GetResponse get = client.execute(new GetRequest(Tree.Extras));
        LOG.info("Should extras link be displayed: " + get.getBoolean());
    }

    /**
     * Checks if the new preferences entry flag for showing participants dialog
     * works.
     */
    public void testShowParticipantsDialogFlag() throws Throwable {
        final GetResponse get = client.execute(new GetRequest(Tree.ShowParticipantDialog));
        LOG.info("Should participant dialog be displayed: " + get.getBoolean());
    }

    /**
     * Checks if the search for all users, groups and resources is triggered
     * when the participant selection dialog is opened.
     */
    public void testAutoSearchFlag() throws Throwable {
        final GetResponse get = client.execute(new GetRequest(Tree.ParticipantAutoSearch));
        LOG.info("Is search triggered on opened participant dialog: " + get.getBoolean());
    }

    /**
     * Checks if the flag works that determines if external participants without
     * an email address are shown in the participant selection dialog.
     */
    public void testShowWithoutEmailFlag() throws Throwable {
        final GetResponse get = client.execute(new GetRequest(Tree.ShowWithoutEmail));
        LOG.info("Are external participants without email address shown in participant dialog: " + get.getBoolean());
    }

    public void testMailAddressAutoSearchFlag() throws Throwable {
        final GetResponse get = client.execute(new GetRequest(Tree.MailAddressAutoSearch));
        LOG.info("Is search triggered on opened recipient dialog: " + get.getBoolean());
    }

    public void testMinimumSearchCharacters() throws Throwable {
        final GetResponse response = client.execute(new GetRequest(Tree.MinimumSearchCharacters));
        LOG.info("Minimum of characters for a search pattern: " + response.getInteger());
    }

    public void testSingleFolderSearch() throws Throwable {
        final GetResponse response = client.execute(new GetRequest(Tree.SingleFolderSearch));
        LOG.info("User is only allowed to search in a single folder: " + response.getBoolean());
    }

    public void testNotifySwitches() throws Throwable {
        for (final Tree param : new Tree[] {
            Tree.CalendarNotifyNewModifiedDeleted,
            Tree.CalendarNotifyNewAcceptedDeclinedAsCreator,
            Tree.CalendarNotifyNewAcceptedDeclinedAsParticipant,
            Tree.TasksNotifyNewModifiedDeleted,
            Tree.TasksNotifyNewAcceptedDeclinedAsCreator,
            Tree.TasksNotifyNewAcceptedDeclinedAsParticipant }) {
            testBoolean(param, true);
        }
    }

    public void testCharacterSearch() throws Throwable {
        final GetResponse response = client.execute(new GetRequest(Tree.CharacterSearch));
        LOG.info("User is only allowed to search via character side bar in contacts: " + response.getBoolean());
    }

    public void testAllFolderForAutoComplete() throws Throwable {
        final GetResponse response = client.execute(new GetRequest(Tree.AllFolderForAutoComplete));
        LOG.info("User is allowed to search via auto complete in all folders: " + response.getBoolean());
    }

    public void testFolderTree() throws Throwable {
        final int defaultValue = client.execute(new GetRequest(Tree.FolderTree)).getInteger();
        client.execute(new SetRequest(Tree.FolderTree, I(0)));
        assertEquals("Selecting OX folder tree did not work.", 0, client.execute(new GetRequest(Tree.FolderTree)).getInteger());
        client.execute(new SetRequest(Tree.FolderTree, I(1)));
        assertEquals("Selecting new Outlook folder tree did not work.", 1, client.execute(new GetRequest(Tree.FolderTree)).getInteger());
        // Restore default
        client.execute(new SetRequest(Tree.FolderTree, I(defaultValue)));
    }

    public void testAvailableTimeZones() throws Throwable {
        final GetResponse response = client.execute(new GetRequest(Tree.AvailableTimeZones));
        JSONObject json = response.getJSON();
        for (Entry<String, Object> entry : json.entrySet()) {
            LOG.info("Time zone: " + entry.getKey() + ", localized name: " + entry.getValue());
        }
    }

    public void testOLOX20Module() throws Throwable {
        final GetResponse response = client.execute(new GetRequest(Tree.OLOX20Module));
        assertFalse("Module for OLOX20 must be always false to prevent UI plugin loading.", response.getBoolean());
    }

    public void testOLOX20Active() throws Throwable {
        final GetResponse response = client.execute(new GetRequest(Tree.OLOX20Active));
        LOG.info("Is the user allowed to use OXtender for Microsoft Outlook 2: " + response.getBoolean());
    }

    private void testBoolean(final Tree param, final boolean testWrite) throws Throwable {
        // Remember for restore.
        final boolean oldValue = client.execute(new GetRequest(param)).getBoolean();
        if (testWrite) {
            testWriteTrue(param);
            testWriteFalse(param);
            // Restore original value.
            client.execute(new SetRequest(param, Boolean.valueOf(oldValue)));
        }
    }

    private void testWriteTrue(final Tree param) throws Throwable {
        client.execute(new SetRequest(param, Boolean.TRUE));
        assertTrue(client.execute(new GetRequest(param)).getBoolean());
    }

    private void testWriteFalse(final Tree param) throws Throwable {
        client.execute(new SetRequest(param, Boolean.FALSE));
        assertFalse(client.execute(new GetRequest(param)).getBoolean());
    }
}
