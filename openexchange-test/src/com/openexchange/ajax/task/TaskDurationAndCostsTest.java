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

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link TaskDurationAndCostsTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class TaskDurationAndCostsTest extends AbstractAJAXSession {

    private Task task;

    private TimeZone tz;

    /**
     * Initializes a new {@link TaskDurationAndCostsTest}.
     */
    public TaskDurationAndCostsTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        tz = getClient().getValues().getTimeZone();
        task = new Task();
        task.setParentFolderID(getClient().getValues().getPrivateTaskFolder());
        task.setTitle("Set task duration and costs test");
        task.setActualDuration(L(2));
        task.setActualCosts(new BigDecimal("2.0"));
        task.setTargetDuration(L(10));
        task.setTargetCosts(new BigDecimal("10.0"));
        InsertRequest request = new InsertRequest(task, tz);
        InsertResponse response = getClient().execute(request);
        response.fillTask(task);
    }

    @Test
    public void testDurationAndCosts() throws Exception {
        task.setTargetCosts(new BigDecimal("11.5"));
        task.setActualCosts(new BigDecimal("4.728"));
        task.setActualDuration(L(7));
        task.setTargetDuration(L(15));
        UpdateRequest req = new UpdateRequest(task, tz, false);
        try {
            UpdateResponse response = getClient().execute(req);
            task.setLastModified(response.getTimestamp());
        } catch (Exception e) {
            fail("Setting costs and duration failed!");
        }
        GetRequest request = new GetRequest(task);
        GetResponse response = getClient().execute(request);
        task.setLastModified(response.getTimestamp());
        Task test = response.getTask(tz);
        // We have in the database NUMERIC(12,2). So round to 3 valid digits in this case. Rounding is necessary because JSON internally
        // parses a float value to java.lang.Double causing rounding issues.
        assertEquals("Actual costs not equal", task.getActualCosts().round(new MathContext(3)), test.getActualCosts().round(new MathContext(3)));
        assertEquals("Target costs not equal", task.getTargetCosts(), test.getTargetCosts());
        assertEquals("Actual duration not equal", task.getActualDuration(), test.getActualDuration());
        assertEquals("Target duration not equal", task.getTargetDuration(), test.getTargetDuration());
    }
}
