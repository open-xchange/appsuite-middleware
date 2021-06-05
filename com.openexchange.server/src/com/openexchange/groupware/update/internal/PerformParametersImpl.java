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

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.ConnectionProvider;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.Schema;

/**
 * {@link PerformParametersImpl}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PerformParametersImpl implements PerformParameters {

    private final Schema schema;
    private final int optContextId;
    private final ProgressState logger;
    private final ConnectionProvider connectionProvider;

    /**
     * Initializes a new {@link PerformParametersImpl}.
     */
    public PerformParametersImpl(Schema schema, ConnectionProvider connectionProvider, int optContextId, ProgressState logger) {
        super();
        this.schema = schema;
        this.optContextId = optContextId;
        this.logger = logger;
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Connection getConnection() throws OXException {
        return connectionProvider.getConnection();
    }

    @Override
    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    @Override
    public int optContextId() {
        return optContextId;
    }

    @Override
    public int[] getContextsInSameSchema() throws OXException {
        return connectionProvider.getContextsInSameSchema();
    }

    @Override
    public ProgressState getProgressState() {
        return logger;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

}
