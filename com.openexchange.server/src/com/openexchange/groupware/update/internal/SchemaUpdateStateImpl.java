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
import java.util.HashSet;
import java.util.Set;
import com.openexchange.groupware.update.NamesOfExecutedTasks;
import com.openexchange.groupware.update.SchemaUpdateState;

/**
 * {@link SchemaUpdateStateImpl}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SchemaUpdateStateImpl extends SchemaImpl implements SchemaUpdateState {

    private static final long serialVersionUID = -2760325392823131336L;

    private final Set<String> successfullyExecutedTasks = new HashSet<String>();
    private final Set<String> failedExecutedTasks = new HashSet<String>();

    private boolean backgroundUpdatesRunning;
    private Date backgroundUpdatesRunningSince;
    private Date lockedSince;

    SchemaUpdateStateImpl() {
        super();
    }

    public SchemaUpdateStateImpl(SchemaUpdateState schema) {
        super(schema);
        for (String task : schema.getExecutedList()) {
            if (schema.isExecutedSuccessfully(task)) {
                successfullyExecutedTasks.add(task);
            } else {
                failedExecutedTasks.add(task);
            }
        }
        backgroundUpdatesRunning = schema.backgroundUpdatesRunning();
    }

    @Override
    public void addExecutedTask(String taskName, boolean success) {
        if (success) {
            successfullyExecutedTasks.add(taskName);
        } else {
            failedExecutedTasks.add(taskName);
        }
    }

    @Override
    public boolean isExecuted(String taskName) {
        return successfullyExecutedTasks.contains(taskName) || failedExecutedTasks.contains(taskName);
    }

    @Override
    public boolean isExecutedSuccessfully(String taskName) {
        return successfullyExecutedTasks.contains(taskName);
    }

    @Override
    public String[] getExecutedList() {
        return getExecutedList(false);
    }

    @Override
    public String[] getExecutedList(boolean successfulOnly) {
        if (successfulOnly) {
            return successfullyExecutedTasks.toArray((new String[successfullyExecutedTasks.size()]));
        }
        Set<String> executedTasks = new HashSet<String>(successfullyExecutedTasks.size() + failedExecutedTasks.size());
        executedTasks.addAll(successfullyExecutedTasks);
        executedTasks.addAll(failedExecutedTasks);
        return executedTasks.toArray(new String[executedTasks.size()]);
    }

    @Override
    public NamesOfExecutedTasks getExecuted() {
        return NamesOfExecutedTasks.builder().withSuccessfullyExecutedTasks(successfullyExecutedTasks).withFailedTasks(failedExecutedTasks).build();
    }

    void setBlockingUpdatesRunningSince(Date date) {
        this.lockedSince = date;
    }

    @Override
    public boolean backgroundUpdatesRunning() {
        return backgroundUpdatesRunning;
    }

    void setBackgroundUpdatesRunning(boolean backgroundUpdatesRunning) {
        this.backgroundUpdatesRunning = backgroundUpdatesRunning;
    }

    @Override
    public Date blockingUpdatesRunningSince() {
        Date lockedSince = this.lockedSince;
        return null == lockedSince ? null : new Date(lockedSince.getTime());
    }

    @Override
    public Date backgroundUpdatesRunningSince() {
        Date backgroundUpdatesRunningSince = this.backgroundUpdatesRunningSince;
        return null == backgroundUpdatesRunningSince ? null : new Date(backgroundUpdatesRunningSince.getTime());
    }

    void setBackgroundUpdatesRunningSince(Date date) {
        this.backgroundUpdatesRunningSince = date;
    }

}
