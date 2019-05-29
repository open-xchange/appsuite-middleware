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
