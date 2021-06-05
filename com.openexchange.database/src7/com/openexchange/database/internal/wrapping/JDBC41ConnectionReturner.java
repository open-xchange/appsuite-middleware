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

package com.openexchange.database.internal.wrapping;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import com.openexchange.database.internal.AssignmentImpl;
import com.openexchange.database.internal.Pools;
import com.openexchange.database.internal.ReplicationMonitor;

/**
 * {@link JDBC41ConnectionReturner}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class JDBC41ConnectionReturner extends JDBC4ConnectionReturner {

    public JDBC41ConnectionReturner(Pools pools, ReplicationMonitor monitor, AssignmentImpl assign, Connection delegate, boolean noTimeout, boolean write, boolean usedAsRead) {
        super(pools, monitor, assign, delegate, noTimeout, write, usedAsRead);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        checkForAlreadyClosed();
        delegate.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        checkForAlreadyClosed();
        delegate.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        checkForAlreadyClosed();
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getNetworkTimeout();
    }
}
