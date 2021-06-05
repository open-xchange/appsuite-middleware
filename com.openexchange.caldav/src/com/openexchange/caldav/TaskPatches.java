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

package com.openexchange.caldav;

import java.util.Date;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link TaskPatches}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TaskPatches {

    /**
     * {@link Incoming}
     *
     * Patches for incoming iCal files.
     *
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     */
    public static final class Incoming {

        private Incoming() {
        	// prevent instantiation
        }

        /**
         * Tries to restore the original task status and percent-completed in case the updated task does not look like a 'done' / 'undone'
         * operation by the client (relevant for bugs #23058, #25240, 24812)
         *
         * @param originalTask
         * @param updatedTask
         */
        public static void adjustTaskStatus(Task originalTask, Task updatedTask) {
            if (false == originalTask.containsStatus()) {
                /*
                 * Nothing to restore
                 */
                return;
            } else if (Task.DONE == updatedTask.getStatus() && Task.DONE != originalTask.getStatus()) {
                /*
                 * 'Done' in Mac OS client: STATUS:COMPLETED / PERCENT-COMPLETE:100
                 */
                updatedTask.setPercentComplete(100);
                updatedTask.setStatus(Task.DONE);
            } else if (Task.NOT_STARTED == updatedTask.getStatus() && Task.DONE == originalTask.getStatus()) {
                /*
                 * 'Undone' in Mac OS client: STATUS:NEEDS-ACTION
                 */
                updatedTask.setPercentComplete(0);
            } else if (Task.NOT_STARTED == updatedTask.getStatus() && Task.NOT_STARTED != originalTask.getStatus()) {
                /*
                 * neither done/undone transition, restore from original task
                 */
                updatedTask.setPercentComplete(originalTask.getPercentComplete());
                updatedTask.setStatus(originalTask.getStatus());
            }
        }

        /**
         * Removes the start date of a task in case the updated task's end date is set to a time before the set start date.
         *
         * @param originalTask The original task
         * @param updatedTask The updated task
         */
        public static void adjustTaskStart(Task originalTask, Task updatedTask) {
            Date startDate = updatedTask.containsStartDate() ? updatedTask.getStartDate() : originalTask.getStartDate();
            if (null != startDate && updatedTask.containsEndDate() && null != updatedTask.getEndDate() && updatedTask.getEndDate().before(startDate)) {
                /*
                 * remove currently set start date
                 */
                updatedTask.setStartDate(null);
            }
        }

    }

}
