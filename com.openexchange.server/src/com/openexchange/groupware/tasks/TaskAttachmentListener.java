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

import java.sql.Connection;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.contexts.Context;

/**
 * This class implements the update of a task if a file is attached or detached to the task.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskAttachmentListener implements AttachmentListener {

    private static final int[] UPDATE_FIELDS = new int[] { Task.LAST_MODIFIED, Task.MODIFIED_BY, Task.NUMBER_OF_ATTACHMENTS };

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskAttachmentListener.class);

    public TaskAttachmentListener() {
        super();
    }

    @Override
    public long attached(final AttachmentEvent event) throws OXException {
        final TaskStorage storage = TaskStorage.getInstance();
        final Context ctx = event.getContext();
        final Date lastModified = new Date();
        final Task task = new Task();
        task.setObjectID(event.getAttachedId());
        task.setLastModified(lastModified);
        task.setModifiedBy(event.getUser().getId());
        final Connection con = event.getWriteConnection();
        try {
            final Task oldTask = storage.selectTask(ctx, con,
                event.getAttachedId(), StorageType.ACTIVE);
            final Date lastRead = oldTask.getLastModified();
            task.setNumberOfAttachments(oldTask.getNumberOfAttachments() + 1);
            UpdateData.updateTask(ctx, con, task, lastRead, UPDATE_FIELDS, null,
                null, null, null);
        } catch (OXException e) {
            throw e;
        }
        LOG.trace("Increased number of attachments for task {} in context {} to {}", event.getAttachedId(), ctx.getContextId(), task.getNumberOfAttachments());
        return lastModified.getTime();
    }

    @Override
    public long detached(final AttachmentEvent event) throws OXException {
        final TaskStorage storage = TaskStorage.getInstance();
        final Context ctx = event.getContext();
        final Task task = new Task();
        task.setObjectID(event.getAttachedId());
        final Date lastModified = new Date();
        task.setLastModified(lastModified);
        task.setModifiedBy(event.getUser().getId());
        final Connection con = event.getWriteConnection();
        try {
            final Task oldTask = storage.selectTask(ctx, con,
                event.getAttachedId(), StorageType.ACTIVE);
            final Date lastRead = oldTask.getLastModified();
            final int numOfAttachments = oldTask.getNumberOfAttachments()
                - event.getDetached().length;
            if (numOfAttachments < 0) {
                throw TaskExceptionCode.WRONG_ATTACHMENT_COUNT.create();
            }
            task.setNumberOfAttachments(numOfAttachments);
            UpdateData.updateTask(ctx, con, task, lastRead, UPDATE_FIELDS, null,
                null, null, null);
        } catch (OXException e) {
            throw e;
        }
        LOG.trace("Decreased number of attachments for task {} in context {} to {}", event.getAttachedId(), ctx.getContextId(), task.getNumberOfAttachments());
        return lastModified.getTime();
    }
}
