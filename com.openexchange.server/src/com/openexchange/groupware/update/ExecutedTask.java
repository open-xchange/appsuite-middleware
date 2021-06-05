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

import java.util.Date;
import java.util.UUID;

/**
 * {@link ExecutedTask}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface ExecutedTask extends Comparable<ExecutedTask> {

    /**
     * Returns the task's name
     * 
     * @return the task's name
     */
    String getTaskName();

    /**
     * Returns whether the task was successfully executed
     * 
     * @return <code>true</code> if the task was successfully executed; <code>false otherwise</code>
     */
    boolean isSuccessful();

    /**
     * Returns the last modified {@link Date}
     * 
     * @return the last modified {@link Date}
     */
    Date getLastModified();

    /**
     * Returns the task's {@link UUID}
     * 
     * @return the task's {@link UUID}
     */
    UUID getUUID();
}
