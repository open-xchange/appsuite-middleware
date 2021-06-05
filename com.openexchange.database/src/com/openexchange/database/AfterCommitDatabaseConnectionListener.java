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
import java.util.function.Consumer;

/**
 * {@link AfterCommitDatabaseConnectionListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class AfterCommitDatabaseConnectionListener implements DatabaseConnectionListener {

    private final Consumer<Connection> consumer;

    /**
     * Initializes a new {@link AfterCommitDatabaseConnectionListener}.
     *
     * @param consumer The callback to invoke on after the connection has been committed
     */
    public AfterCommitDatabaseConnectionListener(Consumer<Connection> consumer) {
        super();
        this.consumer = consumer;
    }

    @Override
    public void onAutoCommitChanged(boolean autoCommit, Connection connection) {
        // no
    }

    @Override
    public void onBeforeRollbackPerformed(Connection connection) {
        // no
    }

    @Override
    public void onAfterRollbackPerformed(Connection connection) {
        // no
    }

    @Override
    public void onBeforeCommitPerformed(Connection connection) {
        // no
    }

    @Override
    public void onAfterCommitPerformed(Connection connection) {
        try {
            consumer.accept(connection);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AfterCommitDatabaseConnectionListener.class).warn("Error invoking callback after commit", e);
        }
    }

}
