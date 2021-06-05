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

package com.openexchange.cluster.lock;

import com.openexchange.exception.OXException;

/**
 * {@link ClusterTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface ClusterTask<T> {

    /**
     * Returns the context identifier for this task if the task is ought to be run for a
     * specific context Otherwise, the implementation must return <code>-1</code> to indicate
     * that it's a global task.
     * 
     * @return the context identifier or <code>-1</code> for a global task
     */
    int getContextId();

    /**
     * Returns the user identifier for this task if the task is ought to be run for a
     * specific user. Otherwise, the implementation must return <code>-1</code> to indicate
     * that it's a global task.
     * 
     * @return the user identifier or <code>-1</code> for a global task
     */
    int getUserId();

    /**
     * Returns the name of this task
     * 
     * @return the name of this task
     */
    String getTaskName();

    /**
     * Performs the implemented cluster task.
     * 
     * @return {@link T}
     * @throws OXException if an error is occurred during the execution
     */
    T perform() throws OXException;
}
