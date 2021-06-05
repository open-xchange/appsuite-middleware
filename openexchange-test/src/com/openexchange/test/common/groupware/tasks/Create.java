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

package com.openexchange.test.common.groupware.tasks;

import static com.openexchange.java.Autoboxing.L;
import java.math.BigDecimal;
import java.util.Date;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Create {

    /**
     * Prevent instantiation
     */
    private Create() {
        super();
    }

    /**
     * Creates a task and fills all fields.
     */
    public static Task createTask() {
        final Task task = new Task();
        task.setTitle("Private task");
        task.setPrivateFlag(false);
        task.setCreationDate(new Date());
        task.setLastModified(new Date());
        task.setStartDate(new Date(1133964000000L));
        task.setEndDate(new Date(1133967600000L));
        task.setAfterComplete(new Date(1133971200000L));
        task.setNote("Description");
        task.setStatus(Task.NOT_STARTED);
        task.setPriority(Task.NORMAL);
        task.setCategories("Categories");
        task.setTargetDuration(L(1440));
        task.setActualDuration(L(1440));
        task.setTargetCosts(new BigDecimal("1.0"));
        task.setActualCosts(new BigDecimal("1.0"));
        task.setCurrency("\u20ac");
        task.setTripMeter("trip meter");
        task.setBillingInformation("billing information");
        task.setCompanies("companies");
        return task;
    }

    /**
     * Creates an empty task.
     */
    public static Task createWithDefaults() {
        final Task task = new Task();
        task.setPrivateFlag(false);
        return task;
    }

    /**
     * Creates a task for a given folder and with a given title.
     * 
     * @param folder pre-fill parent folder field.
     * @param title title of the task.
     */
    public static final Task createWithDefaults(int folder, String title) {
        final Task task = createWithDefaults();
        task.setParentFolderID(folder);
        task.setTitle(title);
        return task;
    }

    /**
     * Creates a task with the specified title, description status and folder
     * 
     * @param title task's title
     * @param description task's description
     * @param status task's status
     * @param folder task's parent folder
     * @return the task
     */
    public static final Task createWithDefaults(String title, String description, int status, int folder) {
        Task task = createWithDefaults(folder, title);
        task.setNote(description);
        task.setStatus(status);
        return task;
    }

    public static final Task cloneForUpdate(Task task) {
        Task retval = new Task();
        retval.setObjectID(task.getObjectID());
        retval.setParentFolderID(task.getParentFolderID());
        retval.setLastModified(task.getLastModified());
        return retval;
    }
}
