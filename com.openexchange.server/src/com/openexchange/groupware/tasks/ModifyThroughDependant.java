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
        } catch (final OXException e) {
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
        } catch (final OXException e) {
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
