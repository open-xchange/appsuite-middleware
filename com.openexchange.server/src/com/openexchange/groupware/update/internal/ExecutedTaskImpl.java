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

import java.util.Date;
import java.util.UUID;
import com.openexchange.groupware.update.ExecutedTask;

/**
 * {@link ExecutedTaskImpl}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ExecutedTaskImpl implements ExecutedTask {

    private final String taskName;
    private final boolean successful;
    private final Date lastModified;
    private final UUID uuid;

    /**
     * Initializes a new {@link ExecutedTaskImpl}.
     * 
     * @param lastModified when the task has been executed lately
     * @param successful if the task was executed successfully
     * @param taskName full class name of the update task
     * @param uuid the {@link UUID} of the update task
     */
    public ExecutedTaskImpl(String taskName, boolean successful, Date lastModified, UUID uuid) {
        super();
        this.taskName = taskName;
        this.successful = successful;
        this.lastModified = lastModified;
        this.uuid = uuid;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public int compareTo(final ExecutedTask o) {
        if (null == o) {
            return 1;
        }
        final Date thisLastModified = lastModified;
        final Date otherLastModified = o.getLastModified();
        if (null == thisLastModified) {
            if (null == otherLastModified) {
                return 0; // Both null
            }
            return -1; // Other is not null
        }
        if (null == otherLastModified) {
            return 1; // Other is null
        }
        return thisLastModified.compareTo(otherLastModified);
    }
}
