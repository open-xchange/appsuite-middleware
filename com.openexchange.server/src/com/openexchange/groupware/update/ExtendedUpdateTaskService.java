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

package com.openexchange.groupware.update;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link ExtendedUpdateTaskService} - The extended service for update tasks.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface ExtendedUpdateTaskService {

    /**
     * Executes all pending update tasks for the contact-associated database schema.
     *
     * @param contextId The identifier of the context whose associated schema is supposed to be updated
     * @return Returns a {@link List} with all failed tasks
     * @throws OXException If update fails fatally
     */
    List<TaskFailure> runUpdateFor(int contextId) throws OXException;

    /**
     * Executes all pending update tasks for the specified database schema.
     *
     * @param schemaName The name of the database schema
     * @return Returns a {@link List} with all failed tasks
     * @throws OXException If update fails
     */
    List<TaskFailure> runUpdateFor(String schemaName) throws OXException;
}
