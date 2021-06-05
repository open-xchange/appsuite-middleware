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
import java.math.BigDecimal;
import org.junit.Test;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class FloatTest extends AbstractTaskTest {

    /**
     * @param name
     */
    public FloatTest() {
        super();
    }

    /**
     * Tests if floats can be stored correctly.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testFloats() throws Throwable {
        final Task task = new Task();
        task.setActualCosts(new BigDecimal("1"));
        task.setTargetCosts(new BigDecimal("1"));
        task.setParentFolderID(getPrivateFolder());
        final InsertResponse insertR = getClient().execute(new InsertRequest(task, getTimeZone()));

        GetResponse getR = getClient().execute(new GetRequest(insertR));
        Task reload = getR.getTask(getTimeZone());
        assertEquals("Actual duration differs.", task.getActualDuration(), reload.getActualDuration());
        assertEquals("Target duration differs.", task.getTargetDuration(), reload.getTargetDuration());
        assertEquals("Actual costs differs.", task.getActualCosts(), reload.getActualCosts());
        assertEquals("Target costs differs.", task.getTargetCosts(), reload.getTargetCosts());

        getClient().execute(new DeleteRequest(reload));
    }
}
