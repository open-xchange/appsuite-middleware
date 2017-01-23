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

package com.openexchange.ajax.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInBodyRequest;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInParametersRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TestTask;
import com.openexchange.test.TaskTestManager;

/**
 * {@link ConfirmTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ConfirmTest extends AbstractTaskTestForAJAXClient {

    private TaskTestManager manager;
    private int userId;
    private TestTask task;

    public ConfirmTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager = new TaskTestManager(getClient());
        task = getNewTask(this.getClass().getCanonicalName());

        userId = getClient().getValues().getUserId();
        task.addParticipant(new UserParticipant(userId));

        manager.insertTaskOnServer(task);

    }

    @After
    public void tearDown() throws Exception {
        try {
            task.setLastModified(new Date(Long.MAX_VALUE));
            manager.cleanUp();
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testConfirmWithTaskInParameters() throws OXException, IOException, SAXException, JSONException {
        ConfirmWithTaskInParametersRequest request = new ConfirmWithTaskInParametersRequest(task, Task.ACCEPT, "Confirmanize!");
        getClient().execute(request);

        checkTaskOnServer(Task.ACCEPT, "Confirmanize!");
    }

    @Test
    public void testConfirmWithTaskInBody() throws OXException, IOException, SAXException, JSONException {
        ConfirmWithTaskInBodyRequest request = new ConfirmWithTaskInBodyRequest(task, Task.ACCEPT, "Confirmanize!");
        getClient().execute(request);

        checkTaskOnServer(Task.ACCEPT, "Confirmanize!");
    }

    private void checkTaskOnServer(int confirmmation, String message) {
        Task reloaded = manager.getTaskFromServer(task);

        boolean found = false;
        for (UserParticipant user : reloaded.getUsers()) {
            if (user.getIdentifier() == userId) {
                assertEquals(confirmmation, user.getConfirm());
                assertEquals(message, user.getConfirmMessage());
                found = true;
            }
        }

        assertTrue(found);

    }

}
