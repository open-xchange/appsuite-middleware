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

package com.openexchange.gdpr.dataexport;

import com.openexchange.java.Strings;

/**
 * {@link DataExportStatus} - The statuses for a data export task.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public enum DataExportStatus {

    /**
     * The task is non-existent.
     */
    NONE("NONE"),
    /**
     * The task is pending and awaits execution.
     */
    PENDING("PENDING"),
    /**
     * The task is currently executed.
     */
    RUNNING("RUNNING"),
    /**
     * The task is currently paused.
     */
    PAUSED("PAUSED"),
    /**
     * The task is done/completed.
     */
    DONE("DONE"),
    /**
     * The task failed.
     */
    FAILED("FAILED"),
    /**
     * The task has been aborted.
     */
    ABORTED("ABORTED"),
    ;

    private final String id;

    private DataExportStatus(String id) {
        this.id = id;
    }

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * Checks if this status signals that execution of data export task is pending.
     *
     * @return <code>true</code> if pending; otherwise <code>false</code>
     */
    public boolean isPending() {
        return PENDING == this;
    }

    /**
     * Checks if this status signals that execution of data export task is running.
     *
     * @return <code>true</code> if running; otherwise <code>false</code>
     */
    public boolean isRunning() {
        return RUNNING == this;
    }

    /**
     * Checks if this status signals that data export task has been paused.
     *
     * @return <code>true</code> if paused; otherwise <code>false</code>
     */
    public boolean isPaused() {
        return PAUSED == this;
    }

    /**
     * Checks if this status signals that data export task has been aborted.
     *
     * @return <code>true</code> if aborted; otherwise <code>false</code>
     */
    public boolean isAborted() {
        return ABORTED == this;
    }

    /**
     * Checks if this status signals that data export task has failed.
     *
     * @return <code>true</code> if failed; otherwise <code>false</code>
     */
    public boolean isFailed() {
        return FAILED == this;
    }

    /**
     * Checks if this status signals that data export task is done/completed.
     *
     * @return <code>true</code> if done; otherwise <code>false</code>
     */
    public boolean isDone() {
        return DONE == this;
    }

    /**
     * Whether this status signals that data export task is terminated.
     * <p>
     * A task is considered as terminated if its status is {@link #DONE}, {@link #FAILED} or {@link #ABORTED}.
     *
     * @return <code>true</code> if terminated; otherwise <code>false</code>
     */
    public boolean isTerminated() {
        return DONE == this || FAILED == this || ABORTED == this;
    }

    /**
     * Gets the status for given identifier.
     *
     * @param id The identifier to look-up by
     * @return The status or <code>null</code>
     */
    public static DataExportStatus statusFor(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }

        String toCheck = Strings.toUpperCase(id);
        for (DataExportStatus status : DataExportStatus.values()) {
            if (toCheck.equals(status.id)) {
                return status;
            }
        }
        return null;
    }

}
