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

package com.openexchange.ajax.task;

import static com.openexchange.ajax.task.TaskTools.insertTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.RandomString;

/**
 * @author marcus
 *
 */
public class TruncationTest extends AbstractTaskTest {

    private AJAXClient client;

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(TruncationTest.class);
    
    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public TruncationTest(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(getSession());
    }

    /**
     * Creates a task with a to long title and checks if the data truncation
     * is detected.
     * @throws Throwable if an error occurs.
     */
    public void testTruncation() throws Throwable {
        final Task task = new Task();
        // Title length in database is 128.
        task.setTitle(RandomString.generateFixLetter(200));
        // Trip meter length in database is 255.
        task.setTripMeter(RandomString.generateFixLetter(300));
        task.setParentFolderID(getPrivateFolder());
        final InsertResponse response = TaskTools.insert(getSession(),
            new InsertRequest(task, client.getValues().getTimeZone(), false));
        assertTrue("Server did not detect truncated data.", response
            .hasError());
        assertTrue("Array of truncated attribute identifier is empty.", response
            .getTruncatedIds().length > 0);
        final StringBuilder sb = new StringBuilder();
        sb.append("Truncated attribute identifier: [");
        for (int i : response.getTruncatedIds()) {
            sb.append(i);
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
        LOG.info(sb.toString());
    }
}
