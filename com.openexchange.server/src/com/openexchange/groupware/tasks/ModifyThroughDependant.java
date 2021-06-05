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

package com.openexchange.groupware.tasks;

import static com.openexchange.groupware.tasks.StorageType.ACTIVE;
import java.sql.Connection;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.reminder.TargetService;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ModifyThroughDependant implements TargetService {

    private static final int[] UPDATE_FIELDS = new int[] { Task.LAST_MODIFIED, Task.MODIFIED_BY };

    private static final TaskStorage stor = TaskStorage.getInstance();

    public ModifyThroughDependant() {
        super();
    }

    @Override
    public void updateTargetObject(final Context ctx, final Connection con, final int targetId) throws OXException {
        final Task task;
        try {
            task = stor.selectTask(ctx, con, targetId, ACTIVE);
        } catch (OXException e) {
            if (TaskExceptionCode.TASK_NOT_FOUND.equals(e)) {
                return;
            }
            throw e;
        }
        final Date lastModified = task.getLastModified();
        task.setLastModified(new Date());
        stor.updateTask(ctx, con, task, lastModified, new int[] { DataObject.LAST_MODIFIED }, ACTIVE);
    }

    @Override
    public void updateTargetObject(final Context ctx, final Connection con, final int targetId, final int userId) throws OXException {
        final Task task;
        try {
            task = stor.selectTask(ctx, con, targetId, ACTIVE);
        } catch (OXException e) {
            if (TaskExceptionCode.TASK_NOT_FOUND.equals(e)) {
                return;
            }
            throw e;
        }
        final Date lastModified = task.getLastModified();
        task.setLastModified(new Date());
        task.setModifiedBy(userId);
        stor.updateTask(ctx, con, task, lastModified, UPDATE_FIELDS, ACTIVE);
    }
}
