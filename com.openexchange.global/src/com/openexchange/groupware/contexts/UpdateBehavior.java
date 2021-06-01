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

package com.openexchange.groupware.contexts;

/**
 * {@link UpdateBehavior} - Specifies the behavior to apply when detecting one or
 * more pending update task for context-associated database schema.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public enum UpdateBehavior {

    /**
     * No explicit update behavior.
     * <p>
     * Fall-back to default behavior as specified through configuration property
     * <code>"com.openexchange.groupware.update.denyImplicitUpdateOnContextLoad"</code> (default is <code>false</code>).
     */
    NONE,
    /**
     * Trigger update if there are pending update tasks.
     * <p>
     * Trying to load a context with running update tasks will throw <code>c.o.groupware.contexts.impl.ContextExceptionCodes.UPDATE</code>.
     * <p>
     * Trying to load a context with pending update tasks will throw <code>c.o.groupware.contexts.impl.ContextExceptionCodes.UPDATE</code>
     * since update is triggered.
     */
    TRIGGER_UPDATE,
    /**
     * Don't trigger update if there are pending update tasks.
     * <p>
     * Trying to load a context with running update tasks will throw <code>c.o.groupware.contexts.impl.ContextExceptionCodes.UPDATE</code>.
     * <p>
     * Trying to load a context with pending update tasks will throw <code>c.o.groupware.contexts.impl.ContextExceptionCodes.UPDATE_NEEDED</code>.
     */
    DENY_UPDATE;

}
