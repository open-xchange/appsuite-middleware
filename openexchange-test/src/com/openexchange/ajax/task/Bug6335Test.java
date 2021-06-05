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

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskExceptionCode;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * Tests if bug 6335 appears again in tasks.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug6335Test extends AbstractTaskTest {

    /**
     * @param name
     */
    public Bug6335Test() {
        super();
    }

    /**
     * Tests if invalid characters are detected.
     * 
     * @throws Throwable if an exception occurs.
     */
    @Test
    public void testCharacter() throws Throwable {
        final AJAXClient client = getClient();
        final Task task = Create.createWithDefaults();
        task.setTitle("\u001f");
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertResponse iResponse = client.execute(new InsertRequest(task, client.getValues().getTimeZone(), false));
        assertTrue("Invalid character was not detected.", iResponse.hasError());
        OXException expected = TaskExceptionCode.INVALID_DATA.create("foo");
        final OXException actual = iResponse.getException();
        assertTrue("Wrong exception", actual.similarTo(expected));
    }
}
