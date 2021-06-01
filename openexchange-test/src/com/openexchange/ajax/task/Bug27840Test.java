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

import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;
import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * Verifies that the issue described in bug 27840 does not appear again.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug27840Test extends AbstractTaskTest {

    private AJAXClient client;
    private Task task;
    private TimeZone tz;

    public Bug27840Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = getTimeZone();
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test for bug 27840");
        // NUMERIC(12,2) in the database. The following should be the corresponding maximal and minimal possible values.
        task.setTargetCosts(new BigDecimal("9999999999.99"));
        task.setActualCosts(new BigDecimal("-9999999999.99"));
    }

    @Test
    public void testForBug() throws OXException, IOException, JSONException {
        client.execute(new InsertRequest(task, tz)).fillTask(task);
        GetResponse response = client.execute(new GetRequest(task));
        Task test = response.getTask(tz);
        assertThat("Actual costs not equal", test.getActualCosts(), CoreMatchers.equalTo(task.getActualCosts()));
        assertThat("Target costs not equal", test.getTargetCosts(), CoreMatchers.equalTo(task.getTargetCosts()));
        task = test;
    }
}
