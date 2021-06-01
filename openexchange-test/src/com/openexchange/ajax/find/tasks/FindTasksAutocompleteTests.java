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

package com.openexchange.ajax.find.tasks;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.find.Module;
import com.openexchange.find.tasks.TasksStrings;

/**
 * {@link FindTasksAutocompleteTests}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindTasksAutocompleteTests extends AbstractFindTasksTest {

    /**
     *
     * @throws Exception
     */
    @Test
    public void testAutocompleteFieldFacets() throws Exception {
        String prefix = getClient().getValues().getDefaultAddress().substring(0, 3);
        AutocompleteRequest request = new AutocompleteRequest(prefix, Module.TASKS.getIdentifier());
        AutocompleteResponse response = getClient().execute(request);

        assertNotNull(findByDisplayName(response.getFacets(), prefix));
        assertNotNull(findByDisplayName(response.getFacets(), prefix, TasksStrings.FACET_TASK_TITLE));
        assertNotNull(findByDisplayName(response.getFacets(), prefix, TasksStrings.FACET_TASK_DESCRIPTION));
        assertNotNull(findByDisplayName(response.getFacets(), prefix, TasksStrings.FACET_TASK_ATTACHMENT_NAME));
    }
}
