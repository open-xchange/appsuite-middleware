/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
        } catch (final OXException e) {
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
        } catch (final OXException e) {
            throw e;
        }
        LOG.trace("Decreased number of attachments for task {} in context {} to {}", event.getAttachedId(), ctx.getContextId(), task.getNumberOfAttachments());
        return lastModified.getTime();
    }
}
