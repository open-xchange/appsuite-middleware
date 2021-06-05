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

package com.openexchange.database.cleanup;

import java.util.Collection;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;


/**
 * {@link CompositeCleanUpExecution} - A composite clean-up execution.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class CompositeCleanUpExecution implements CleanUpExecution {

    private final Collection<CleanUpExecution> executions;

    /**
     * Initializes a new {@link CompositeCleanUpExecution}.
     *
     * @param executions The executions to wrap
     * @throws IllegalArgumentException If given executions are <code>null</code> or empty
     */
    public CompositeCleanUpExecution(Collection<? extends CleanUpExecution> executions) {
        super();
        if (executions == null) {
            throw new IllegalArgumentException("Collection must not be null");
        }
        ImmutableList.Builder<CleanUpExecution> b = ImmutableList.builderWithExpectedSize(executions.size());
        for (CleanUpExecution execution : executions) {
            if (execution != null) {
                b.add(execution);
            }
        }
        this.executions = b.build();
        if (this.executions.isEmpty()) {
            throw new IllegalArgumentException("Empty executions");
        }
    }

    /**
     * Initializes a new {@link CompositeCleanUpExecution}.
     *
     * @param executions The executions to wrap
     * @throws IllegalArgumentException If given executions are <code>null</code> or empty
     */
    public CompositeCleanUpExecution(CleanUpExecution... executions) {
        super();
        if (executions == null) {
            throw new IllegalArgumentException("Collection must not be null");
        }
        ImmutableList.Builder<CleanUpExecution> b = ImmutableList.builderWithExpectedSize(executions.length);
        for (CleanUpExecution execution : executions) {
            if (execution != null) {
                b.add(execution);
            }
        }
        this.executions = b.build();
        if (this.executions.isEmpty()) {
            throw new IllegalArgumentException("Empty executions");
        }
    }

    @Override
    public boolean isApplicableFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        for (CleanUpExecution execution : executions) {
            if (execution.isApplicableFor(schema, representativeContextId, databasePoolId, state, connectionProvider) == false) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void executeFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        for (CleanUpExecution execution : executions) {
            execution.executeFor(schema, representativeContextId, databasePoolId, state, connectionProvider);
        }
    }

}
