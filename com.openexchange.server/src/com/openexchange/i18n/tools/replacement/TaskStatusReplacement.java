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
