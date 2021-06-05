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

package com.openexchange.groupware.contexts.impl;

import com.openexchange.groupware.contexts.Context;

/**
 * ContextExtended - an extended version of <code>Context</code> interface providing additional methods
 * <ul>
 * <li>{@link #setUpdating(boolean)}</li>
 * <li>{@link #setReadOnly(boolean)}</li>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ContextExtended extends Context {

    /**
     * Sets context's update status. This causes sessions that belong to a
     * updating context to die as fast as possible to be able to maintain these
     * contexts.
     * @param updating the context's update status
     */
    void setUpdating(boolean updating);

    /**
     * Marks the context as read only. This can be used to prevent writing operations before getting a database down exception.
     * @param readOnly the context's read only status.
     */
    void setReadOnly(boolean readOnly);

    /**
     * Checks if context-associated database schema needs an update since there are pending update tasks.
     *
     * @return <code>true</code> if update is needed; otherwise <code>false</code>
     */
    boolean isUpdateNeeded();

    /**
     * Sets that context-associated database schema needs an update since there are pending update tasks. However, actual update has not yet
     * been triggered.
     *
     * @param updateNeeded The update-needed flag to set
     */
    void setUpdateNeeded(boolean updateNeeded);

}
