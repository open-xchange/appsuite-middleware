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

package com.openexchange.database;

import java.sql.Connection;
import com.openexchange.osgi.annotation.Service;

/**
 * {@link DatabaseConnectionListener} - A listener which receives various call-backs for certain connection-associated events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
@Service
public interface DatabaseConnectionListener {

    /**
     * Called right after specified connection's auto-commit mode has been changed to the given state.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: This call-back should not throw an exception.
     * </div>
     *
     * @param autoCommit <code>true</code> to enable auto-commit mode; <code>false</code> to disable it
     * @param connection The connection
     */
    void onAutoCommitChanged(boolean autoCommit, Connection connection);

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Called right before a roll-back on specified connection is executed.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: This call-back should not throw an exception.
     * </div>
     *
     * @param connection The connection which is about to be roll-backed
     */
    void onBeforeRollbackPerformed(Connection connection);

    /**
     * Called right after a roll-back on specified connection has been executed.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: This call-back should not throw an exception.
     * </div>
     *
     * @param connection The connection which was roll-backed
     */
    void onAfterRollbackPerformed(Connection connection);

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Called right before a commit on specified connection is executed.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: This call-back should not throw an exception.
     * </div>
     *
     * @param connection The connection which is about to be committed
     */
    void onBeforeCommitPerformed(Connection connection);

    /**
     * Called right after a commit on specified connection has been executed.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: This call-back should not throw an exception.
     * </div>
     *
     * @param connection The connection which was committed
     */
    void onAfterCommitPerformed(Connection connection);

}
