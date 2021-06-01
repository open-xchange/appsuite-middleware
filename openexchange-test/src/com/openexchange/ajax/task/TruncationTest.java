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
import org.junit.Test;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.exception.OXException.Truncated;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.tools.RandomString;

/**
 * Tests if too long values for task attributes are correctly catched in the
 * server and sent to the AJAX client.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TruncationTest extends AbstractTaskTest {

    /**
     * Default constructor.
     * 
     * @param name Name of the test.
     */
    public TruncationTest() {
        super();
    }

    /**
     * Creates a task with a to long title and checks if the data truncation
     * is detected.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testTruncation() throws Throwable {
        final Task task = new Task();
        // Title length in database is 256.
        task.setTitle(RandomString.generateChars(257));
        // Trip meter length in database is 255.
        task.setTripMeter(RandomString.generateChars(256));
        task.setParentFolderID(getPrivateFolder());
        final InsertResponse response = getClient().execute(new InsertRequest(task, getTimeZone(), false));
        assertTrue("Server did not detect truncated data.", response.hasError());
        assertTrue("Array of truncated attribute identifier is empty.", response.getProblematics().length > 0);
        final StringBuilder sb = new StringBuilder();
        sb.append("Truncated attribute identifier: [");
        int truncatedAttributeId = -1;
        for (final ProblematicAttribute problematic : response.getProblematics()) {
            if (problematic instanceof Truncated) {
                truncatedAttributeId = ((Truncated) problematic).getId();
                sb.append(truncatedAttributeId);
                sb.append(',');
            }
        }
        sb.setCharAt(sb.length() - 1, ']');
        assertEquals("Wrong attribute discovered as truncated.", Task.TITLE, truncatedAttributeId);
    }
}
