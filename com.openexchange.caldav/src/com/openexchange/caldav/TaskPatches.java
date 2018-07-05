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
