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

import java.util.Set;
import com.openexchange.groupware.update.NamesOfExecutedTasks;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * Checks if all dependencies are resolved or will be resolved before the current update task is executed.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DependenciesResolvedChecker implements DependencyChecker {

    /**
     * Initializes a new {@link DependenciesResolvedChecker}.
     */
    public DependenciesResolvedChecker() {
        super();
    }

    @Override
    public boolean check(UpdateTaskV2 task, NamesOfExecutedTasks executed, UpdateTaskV2[] enqueued, UpdateTaskV2[] toExecute) {
        // Check all dependencies.
        Set<String> successfullyExecutedTasks = executed.getSuccessfullyExecutedTasks();
        for (String dependency : task.getDependencies()) {
            if (!dependencyFulfilled(dependency, successfullyExecutedTasks, enqueued)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if given dependency is fulfilled.
     *
     * @param dependency The name of the update task that is considered as dependency
     * @param successfullyExecuted The names of successfully executed update tasks
     * @param enqueued Currently enqueued updat tasks
     * @return <code>true</code> if fulfilled; otherwise <code>false</code>
     */
    boolean dependencyFulfilled(String dependency, Set<String> successfullyExecuted, UpdateTaskV2[] enqueued) {
        if (successfullyExecuted.contains(dependency)) {
            return true;
        }
        for (UpdateTaskV2 task : enqueued) {
            if (task.getClass().getName().equals(dependency)) {
                return true;
            }
        }
        return false;
    }

}
