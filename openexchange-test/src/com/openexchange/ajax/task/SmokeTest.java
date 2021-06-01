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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertTrue;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.task.actions.AbstractTaskRequest;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * Implements test case 1803 partly.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class SmokeTest extends AbstractTaskTest {

    /**
     * @param name
     */
    public SmokeTest() {
        super();
    }

    /**
     * Tests inserting a private task.
     * http://testlink6.open-xchange.com/testlink/lib/execute/execSetResults.php?level=testcase&id=1803
     *
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testCase1803() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateTaskFolder();
        final Task task = Create.createWithDefaults();
        task.setParentFolderID(folderId);
        task.setTitle("Buy a birthday gift for Mr. K\u00e4rner");
        final TimeZone timeZone = client.getValues().getTimeZone();
        final DateFormat dateF = new SimpleDateFormat("dd.MM.yyyy", client.getValues().getLocale());
        dateF.setTimeZone(timeZone);
        task.setStartDate(dateF.parse("26.02.2007"));
        task.setEndDate(dateF.parse("27.02.2007"));
        task.setStatus(Task.IN_PROGRESS);
        task.setPriority(I(Task.HIGH));
        task.setPercentComplete(75);
        task.setTargetDuration(L(2));
        task.setActualDuration(L(2));
        final InsertResponse insertR = client.execute(new InsertRequest(task, timeZone));
        final GetResponse getR = client.execute(new GetRequest(insertR));
        final Task reload = getR.getTask(timeZone);
        TaskTools.compareAttributes(task, reload);
        final CommonAllResponse allR = TaskTools.all(client, new AllRequest(folderId, AbstractTaskRequest.GUI_COLUMNS, AllRequest.GUI_SORT, AllRequest.GUI_ORDER));
        boolean foundObject = false;
        for (final Object[] rowValues : allR) {
            if (rowValues[0].equals(Integer.valueOf(insertR.getId()))) {
                foundObject = true;
            }
        }
        assertTrue("All request on folder did not found created object.", foundObject);
        // TODO Use list an check if list contains the entered attributes.
        client.execute(new DeleteRequest(reload));
    }
}
