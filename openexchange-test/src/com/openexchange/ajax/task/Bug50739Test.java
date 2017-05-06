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
import java.io.ByteArrayInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AllRequest;
import com.openexchange.ajax.attach.actions.AllResponse;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug50739Test}
 *
 * Permissions for task attachments not correctly evaluated
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug50739Test extends AbstractAJAXSession {

    private AJAXClient client1;
    private AJAXClient client2;
    FolderObject privateFolder;
    FolderObject sharedFolder;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        client2 = getClient2();
        privateFolder = ftm.insertFolderOnServer(ftm.generatePrivateFolder(
            UUIDs.getUnformattedStringFromRandom(), FolderObject.TASK, client1.getValues().getPrivateTaskFolder(), client1.getValues().getUserId()));
        sharedFolder = ftm.insertFolderOnServer(ftm.generateSharedFolder(
            UUIDs.getUnformattedStringFromRandom(), FolderObject.TASK, client1.getValues().getPrivateTaskFolder(), client1.getValues().getUserId(), client2.getValues().getUserId()));
    }

    @Test
    public void testAccessAttachments() throws Exception {
        /*
         * create task with attachments in private folder as user a
         */
        Task task = Create.createWithDefaults(privateFolder.getObjectID(), "test");
        client1.execute(new InsertRequest(task, client1.getValues().getTimeZone(), true)).fillTask(task);
        client1.execute(new AttachRequest(task, "test.txt", new ByteArrayInputStream("test".getBytes()), "text/plain"));
        /*
         * try to access attachment of task in private folder as user b, using the identifier of the shared folder
         */
        int columns[] = { 800, 801, 802, 803, 804, 805, 806 };
        Task requestedObject = new Task();
        requestedObject.setParentFolderID(sharedFolder.getObjectID());
        requestedObject.setObjectID(task.getObjectID());
        AllRequest allRequest = new AllRequest(requestedObject, columns, false);
        AllResponse allResponse = client2.execute(allRequest);
        assertTrue(allResponse.hasError());
        assertEquals("TSK-0046", allResponse.getException().getErrorCode());
        /*
         * try to access attachment of task in private folder as user b, using the identifier of another visible folder
         */
        requestedObject.setParentFolderID(client2.getValues().getPrivateTaskFolder());
        allResponse = client2.execute(allRequest);
        assertTrue(allResponse.hasError());
        assertEquals("TSK-0046", allResponse.getException().getErrorCode());
    }

}
