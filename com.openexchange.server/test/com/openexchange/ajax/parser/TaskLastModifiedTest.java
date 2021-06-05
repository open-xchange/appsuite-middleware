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

package com.openexchange.ajax.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link TaskLastModifiedTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TaskLastModifiedTest {

    @Test
    public void testNotParsing() throws Throwable {
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        Task task = new Task();
        task.setLastModified(new Date());
        JSONObject json = new JSONObject();
        new TaskWriter(UTC).writeTask(task, json);
        Task parsed = new Task();
        new TaskParser(UTC).parse(parsed, json, Locale.ENGLISH);
        assertFalse("lastModified has been set but should not.", parsed.containsLastModified());
        assertNull("lastModified is not null but should.", parsed.getLastModified());
    }

    @Test
    public void testParsing() throws Throwable {
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        Task task = new Task();
        task.setLastModified(new Date());
        JSONObject json = new JSONObject();
        new TaskWriter(UTC).writeTask(task, json);
        Task parsed = new Task();
        new TaskParser(true, UTC).parse(parsed, json, Locale.ENGLISH);
        assertTrue("lastModified has not been set but should.", parsed.containsLastModified());
        assertNotNull("lastModified is null but should not.", parsed.getLastModified());
    }
}
