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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.TestTask;

/**
 * Test that we can remove start and end date from tasks with an update.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Bug10941Test extends AbstractAJAXSession {

    private TestTask task;

    public Bug10941Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        task = ttm.newTask(this.getClass().getCanonicalName() + " Task").startsTomorrow().endsTheFollowingDay();

        ttm.insertTaskOnServer(task);
    }

    @Test
    public void testRemoveStartAndEndDateOnUpdate() {
        TestTask update = new TestTask().relatedTo(task);

        update.setStartDate(null);
        update.setEndDate(null);

        ttm.updateTaskOnServer(update);

        Task saved = ttm.getTaskFromServer(update);

        assertEquals(null, saved.getStartDate());
        assertEquals(null, saved.getEndDate());
    }
}
