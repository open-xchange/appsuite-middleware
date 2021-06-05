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
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;

/**
 * Verifies that the charset handling is correct.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CharsetTest extends AbstractTaskTest {

    public CharsetTest() {
        super();
    }

    /**
     * Tests if the charset handling is correct.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testCharset() throws Throwable {
        AJAXClient client = getClient();
        Task task = new Task();
        task.setTitle("\u00E4\u00F6\u00FC\u00DF\u00C4\u00D6\u00DC");
        task.setNote("\uC11C\uC601\uC9C4");
        int folderId = getPrivateFolder();

        task.setParentFolderID(folderId);
        InsertResponse insertR = client.execute(new InsertRequest(task, getTimeZone()));

        GetResponse getR = client.execute(new GetRequest(insertR));
        Task reload = getR.getTask(getTimeZone());
        try {
            assertEquals("Title differs.", task.getTitle(), reload.getTitle());
            assertEquals("Description differs.", task.getNote(), reload.getNote());
        } finally {
            client.execute(new DeleteRequest(reload));
        }
    }
}
