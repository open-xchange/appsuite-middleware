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

package com.openexchange.ajax.writer;

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import java.io.StringWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;

/**
 * {@link TaskWriterTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.8.0
 */
@SuppressWarnings("static-method")
public class TaskWriterTest {

    public TaskWriterTest() {
        super();
    }

    /**
     * Tests if a priority value is written correctly.
     * @throws JSONException
     */
     @Test
     public void testWriteTaskPriority() throws JSONException {
        Task task = new Task();
        task.setPriority(I(0));
        JSONObject json = new JSONObject();
        new TaskWriter(TimeZones.UTC).writeTask(task, json);
        Assert.assertTrue("Task priority was not written.", json.has(TaskFields.PRIORITY));
        StringWriter sw = new StringWriter();
        json.write(sw);
        Assert.assertThat("Task priority was not written as expected.", sw.toString(), containsString('\"' + TaskFields.PRIORITY + "\":0"));
    }

    /**
     * Tests if a null priority is not written.
     * @throws JSONException
     */
     @Test
     public void testWriteTaskPriorityNull() throws JSONException {
        Task task = new Task();
        task.setPriority(null);
        JSONObject json = new JSONObject();
        new TaskWriter(TimeZones.UTC).writeTask(task, json);
        StringWriter sw = new StringWriter();
        json.write(sw);
        Assert.assertThat("Task priority should not be written.", sw.toString(), not(containsString(TaskFields.PRIORITY)));
        Assert.assertFalse("Task priority should not be written.", json.has(TaskFields.PRIORITY));
    }

     @Test
     public void testWriteTaskArrayPriority() throws JSONException {
        Task task = new Task();
        task.setPriority(I(0));
        JSONArray tmp = new JSONArray();
        new TaskWriter(TimeZones.UTC).writeArray(task, new int[] { Task.PRIORITY }, tmp);
        JSONArray json = tmp.getJSONArray(0);
        Assert.assertEquals("Written array should contain exactly and only written priority.", 1, json.length());
        StringWriter sw = new StringWriter();
        json.write(sw);
        Assert.assertEquals("Written json array does not look like expected.", "[0]", sw.toString());
    }

     @Test
     public void testWriteTaskArrayPriorityNull() throws JSONException {
        Task task = new Task();
        task.setPriority(null);
        JSONArray tmp = new JSONArray();
        new TaskWriter(TimeZones.UTC).writeArray(task, new int[] { Task.PRIORITY }, tmp);
        JSONArray json = tmp.getJSONArray(0);
        Assert.assertEquals("Written array should contain exactly and only written priority.", 1, json.length());
        StringWriter sw = new StringWriter();
        json.write(sw);
        Assert.assertEquals("Written json array does not look like expected.", "[null]", sw.toString());
    }
}
