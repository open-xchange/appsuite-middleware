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
