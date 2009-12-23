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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.update.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateException;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.java.Strings;

/**
 * {@link UpdateTaskSorter}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class UpdateTaskSorter {

    public UpdateTaskSorter() {
        super();
    }

    public List<UpdateTask> sort(String[] executed, List<UpdateTask> toExecute) throws UpdateException {
        List<UpdateTask> retval = new ArrayList<UpdateTask>(toExecute.size());
        boolean found = true;
        while (!toExecute.isEmpty() && found) {
            found = false;
            Iterator<UpdateTask> iter = toExecute.iterator();
            while (iter.hasNext() && !found) {
                UpdateTask task = iter.next();
                int version = task.addedWithVersion();
                if (task instanceof UpdateTaskV2 && Schema.NO_VERSION == version) {
                    UpdateTaskV2 taskV2 = (UpdateTaskV2) task;
                    if (dependenciesFulfilled(taskV2, executed, retval, toExecute)) {
                        retval.add(task);
                        iter.remove();
                        found = true;
                    }
                } else {
                    if (isMinimalVersionAndHighestPriority(task, toExecute)) {
                        retval.add(task);
                        iter.remove();
                        found = true;
                    }
                }
            }
        }
        if (!toExecute.isEmpty()) {
            throw UpdateExceptionCodes.UNRESOLVABLE_DEPENDENCIES.create(Strings.join(executed, ","), Strings.join(retval, ","), Strings.join(toExecute, ","));
        }
        return retval;
    }

    private boolean dependenciesFulfilled(UpdateTaskV2 task, String[] executed, List<UpdateTask> enqueued, List<UpdateTask> toExecute) {
        for (UpdateTask other : toExecute) {
            if (Schema.NO_VERSION != other.addedWithVersion()) {
                return false;
            }
        }
        for (String dependency : task.getDependencies()) {
            if (!dependencyFulfilled(dependency, executed, enqueued)) {
                return false;
            }
        }
        return true;
    }

    private boolean dependencyFulfilled(String dependency, String[] executed, List<UpdateTask> enqueued) {
        for (String taskName : executed) {
            if (taskName.equals(dependency)) {
                return true;
            }
        }
        for (UpdateTask task : enqueued) {
            if (task.getClass().getName().equals(dependency)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMinimalVersionAndHighestPriority(UpdateTask task, List<UpdateTask> toExecute) {
        boolean retval = true;
        Iterator<UpdateTask> iter = toExecute.iterator();
        while (iter.hasNext() && retval) {
            UpdateTask other = iter.next();
            retval = Schema.NO_VERSION == other.addedWithVersion() || task.addedWithVersion() < other.addedWithVersion() || (task.addedWithVersion() == other.addedWithVersion() && task.getPriority() <= other.getPriority());
        }
        return retval;
    }
}
