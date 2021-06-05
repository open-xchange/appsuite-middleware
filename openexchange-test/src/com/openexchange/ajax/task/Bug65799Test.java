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
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.attach.actions.AttachResponse;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * {@link Bug65799Test}
 *
 * Attachment API allows access to private tasks
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug65799Test extends Abstrac2UserAJAXSession {

    @Test
    public void testAddAttachment() throws Exception {
        /*
         * as user a, create a task folder, shared to user b
         */
        FolderObject folder = ftm.insertFolderOnServer(ftm.generateSharedFolder(UUIDs.getUnformattedStringFromRandom(), FolderObject.TASK,
            getClient().getValues().getPrivateTaskFolder(), getClient().getValues().getUserId(), client2.getValues().getUserId()));
        /*
         * create a 'private' task in private folder as user a
         */
        Task task = Create.createWithDefaults(folder.getObjectID(), "test");
        task.setPrivateFlag(true);
        getClient().execute(new InsertRequest(task, getClient().getValues().getTimeZone(), true)).fillTask(task);
        /*
         * try and attach a file to the tasks as user 2
         */
        AttachResponse attachResponse = client2.execute(new AttachRequest(task, "test.txt", new ByteArrayInputStream("wurst".getBytes()), "text/plain"));
        assertTrue(attachResponse.hasError());
        assertEquals("TSK-0046", attachResponse.getException().getErrorCode());
    }

}
