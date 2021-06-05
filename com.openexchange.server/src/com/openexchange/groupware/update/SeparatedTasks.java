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

/**
 * Provides blocking and background update tasks.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface SeparatedTasks {

    /**
     * Gets an immutable listing of blocking update tasks.
     *
     * @return The blocking update tasks
     */
    List<UpdateTaskV2> getBlocking();

    /**
     * Gets an immutable listing of background update tasks.
     *
     * @return The background update tasks
     */
    List<UpdateTaskV2> getBackground();

    /**
     * Checks if this instance holds blocking update tasks.
     *
     * @return <code>true</code> if blocking update tasks are available; otherwise <code>false</code>
     */
    boolean hasBlocking();

    /**
     * Checks if this instance holds background update tasks.
     *
     * @return <code>true</code> if background update tasks are available; otherwise <code>false</code>
     */
    boolean hasBackground();
}
