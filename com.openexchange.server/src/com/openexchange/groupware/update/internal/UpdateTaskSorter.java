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

package com.openexchange.groupware.update.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.NamesOfExecutedTasks;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.java.Strings;

/**
 * {@link UpdateTaskSorter}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class UpdateTaskSorter {

    private static final DependencyChecker[] CHECKERS = { new DependenciesResolvedChecker() };

    public UpdateTaskSorter() {
        super();
    }

    public List<UpdateTaskV2> sort(NamesOfExecutedTasks executed, List<UpdateTaskV2> toExecute) throws OXException {
        List<UpdateTaskV2> retval = new ArrayList<UpdateTaskV2>(toExecute.size());
        boolean found = true;
        while (!toExecute.isEmpty() && found) {
            found = false;
            Iterator<UpdateTaskV2> iter = toExecute.iterator();
            while (iter.hasNext() && !found) {
                UpdateTaskV2 task = iter.next();
                UpdateTaskV2[] retvalA = retval.toArray(new UpdateTaskV2[retval.size()]);
                UpdateTaskV2[] toExecuteA = toExecute.toArray(new UpdateTaskV2[toExecute.size()]);
                for (int i = 0; i < CHECKERS.length && !found; i++) {
                    found = CHECKERS[i].check(task, executed, retvalA, toExecuteA);
                }
                if (found) {
                    retval.add(task);
                    iter.remove();
                }
            }
        }
        if (!toExecute.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (UpdateTaskV2 task : toExecute) {
                sb.setLength(0);
                for (String dependency : task.getDependencies()) {
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(dependency);
                    if (executed.getSuccessfullyExecutedTasks().contains(dependency)) {
                        sb.append(" (OK)");
                    } else if (executed.getFailedExecutedTasks().contains(dependency)) {
                        sb.append(" (FAILED)");
                    }
                }
                OXException e = UpdateExceptionCodes.UNMET_DEPENDENCY.create(task.getClass().getName(), sb.toString());
                org.slf4j.LoggerFactory.getLogger(UpdateTaskSorter.class).warn(e.getMessage());
            }

            List<String> enqueuedNames = new ArrayList<>(retval.size());
            for (UpdateTaskV2 enqueuedOne : retval) {
                enqueuedNames.add(enqueuedOne.getClass().getName());
            }
            List<String> toExecuteNames = new ArrayList<>(toExecute.size());
            for (UpdateTaskV2 toExecuteOne : toExecute) {
                toExecuteNames.add(toExecuteOne.getClass().getName());
            }
            throw UpdateExceptionCodes.UNRESOLVABLE_DEPENDENCIES.create(Strings.join(executed.getSuccessfullyExecutedTasks(), ","), Strings.join(enqueuedNames, ","), Strings.join(toExecuteNames, ","));
        }
        return retval;
    }
}
