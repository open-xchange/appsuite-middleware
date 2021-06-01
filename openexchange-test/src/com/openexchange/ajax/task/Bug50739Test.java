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

package com.openexchange.ajax.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AllRequest;
import com.openexchange.ajax.attach.actions.AllResponse;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * {@link Bug50739Test}
 *
 * Permissions for task attachments not correctly evaluated
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug50739Test extends Abstrac2UserAJAXSession {

    FolderObject privateFolder;
    FolderObject sharedFolder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
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
