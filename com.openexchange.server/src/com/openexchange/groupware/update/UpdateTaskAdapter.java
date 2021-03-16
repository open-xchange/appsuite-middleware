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

package com.openexchange.groupware.update;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.internal.AbstractConnectionProvider;
import com.openexchange.groupware.update.internal.ContextConnectionProvider;
import com.openexchange.groupware.update.internal.PerformParametersImpl;
import com.openexchange.groupware.update.internal.PoolAndSchemaConnectionProvider;
import com.openexchange.groupware.update.internal.ProgressStatusImpl;

/**
 * {@link UpdateTaskAdapter}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class UpdateTaskAdapter implements UpdateTaskV2 {

    /**
     * Initializes a new {@link UpdateTaskAdapter}.
     */
    protected UpdateTaskAdapter() {
        super();
    }

    public static final void perform(final UpdateTaskV2 task, final Schema schema, final int contextId) throws OXException {
        AbstractConnectionProvider connectionProvider = contextId > 0 ? new ContextConnectionProvider(contextId) : new PoolAndSchemaConnectionProvider(schema.getPoolId(), schema.getSchema());
        try {
            ProgressState logger = new ProgressStatusImpl(task.getClass().getName(), schema.getSchema());
            task.perform(new PerformParametersImpl(schema, connectionProvider, contextId, logger));
        } finally {
            connectionProvider.close();
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }
}
