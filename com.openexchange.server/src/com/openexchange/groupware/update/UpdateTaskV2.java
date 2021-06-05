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

package com.openexchange.groupware.update;

import com.openexchange.exception.OXException;

/**
 * Second generation of update tasks.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface UpdateTaskV2 {

    /**
     * Performs the database schema upgrade. Once this method returns either successful or unsuccessful, this task is written as executed
     * successful or unsuccessful in the schemas updateTask table. This can not be changed afterwards automatically.
     *
     * @param params Interface carrying some useful parameters for performing the update. This is a parameter interface to be extendable for
     *               future requirements without breaking the API.
     * @throws OXException should be thrown if the update fails. Then it can be tried to execute this task again.
     */
    void perform(PerformParameters params) throws OXException;

    /**
     * This method is used to determine the order when executing update tasks. Check VERY carefully what update tasks must be run before
     * your task can run. For all currently existing update task the dependency returns always the previous update task to remain the same
     * order as with the versions.
     * <p/>
     * <b>WARNING:</b> Please check always carefully which tasks did touch the same tables, this task touches. This includes other tables
     * if foreign key reference these other tables. Please check also all tasks changing these referenced tables. The update tasks
     * framework executes tasks in a completely random order if no dependencies are defined. Normally there are previous update tasks
     * that already changed the table you want to change. Mention this task as a dependency!
     * <p/>
     * <b>WARNING 2:</b> Ensure to never include a task decorated with the {@link UpdateConcurrency#BACKGROUND} attribute for a regular,
     * i.e. {@link UpdateConcurrency#BLOCKING} update task.
     *
     * @return A string array containing the update tasks that must be run before running this one. You may return an empty array if you can
     *         not discover any dependencies. Never return <code>null</code>.
     */
    String[] getDependencies();

    /**
     * Defines the attributes of a database update task. Please read the corresponding java documentation for the interfaces and enums to
     * get an understanding for the attributes.
     *
     * @return the attributes.
     */
    TaskAttributes getAttributes();

}
