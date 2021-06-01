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

package com.openexchange.i18n.tools.replacement;

import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link TaskStatusReplacement} - Replacement for task status.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class TaskStatusReplacement extends FormatLocalizedStringReplacement {

    private static final String[] STATUSES = { Notifications.TASK_STATUS_NOT_STARTED,
            Notifications.TASK_STATUS_IN_PROGRESS, Notifications.TASK_STATUS_DONE, Notifications.TASK_STATUS_WAITING,
            Notifications.TASK_STATUS_DEFERRED };

    public static final int STATUS_NOT_STARTED = Task.NOT_STARTED;

    public static final int STATUS_IN_PROGRESS = Task.IN_PROGRESS;

    public static final int STATUS_DONE = Task.DONE;

    public static final int STATUS_WAITING = Task.WAITING;

    public static final int STATUS_DEFERRED = Task.DEFERRED;

    /**
     * Gets an empty task status replacement
     *
     * @return An empty task status replacement
     */
    public static TaskStatusReplacement emptyTaskStatusReplacement() {
        return new TaskStatusReplacement();
    }

    private int taskStatus;

    private int percentComplete;

    /**
     * Initializes a new {@link TaskStatusReplacement}
     */
    private TaskStatusReplacement() {
        super(TemplateToken.TASK_STATUS, null, "");
        this.taskStatus = 0;
        this.percentComplete = 0;
    }

    /**
     * Initializes a new {@link TaskStatusReplacement}
     *
     * @param taskStatus The task status; is supposed to be either
     *            {@link #STATUS_NOT_STARTED}, {@link #STATUS_IN_PROGRESS},
     *            {@link #STATUS_DONE}, {@link #STATUS_WAITING}, or
     *            {@link #STATUS_DEFERRED}
     * @param percentComplete The percent complete
     * @throws IllegalArgumentException If specified task status is invalid
     */
    public TaskStatusReplacement(final int taskStatus, final int percentComplete) {
        super(TemplateToken.TASK_STATUS, Notifications.FORMAT_STATUS, STATUSES[checkDecrStatus(taskStatus - 1)]);
        this.taskStatus = taskStatus;
        this.percentComplete = percentComplete;
    }

    private static int checkDecrStatus(final int taskStatus) {
        if (taskStatus < 0 || taskStatus >= STATUSES.length) {
            throw new IllegalArgumentException("Invalid task status specified: " + (taskStatus + 1));
        }
        return taskStatus;
    }

    @Override
    public String getReplacement() {
        final StringHelper sh = getStringHelper();
        if (taskStatus < STATUS_NOT_STARTED || taskStatus > STATUS_DEFERRED) {
            return String.format(sh.getString(Notifications.FORMAT_STATUS), sh.getString(Notifications.NOT_SET));
        }
        final String result = String.format(sh.getString(Notifications.FORMAT_STATUS), sh
                .getString(STATUSES[taskStatus - 1]));
        final StringBuilder b = new StringBuilder(result.length() + 16);
        if (changed()) {
            b.append(PREFIX_MODIFIED);
        }
        b.append(result);
        b.append(" (").append(percentComplete).append("%)");
        return b.toString();
    }

    @Override
    public boolean merge(final TemplateReplacement other) {
        if (!TaskStatusReplacement.class.isInstance(other)) {
            /*
             * Class mismatch or null
             */
            return false;
        }
        if (super.merge(other)) {
            final TaskStatusReplacement o = (TaskStatusReplacement) other;
            this.taskStatus = o.taskStatus;
            this.percentComplete = o.percentComplete;
            return true;
        }
        return false;
    }
}
