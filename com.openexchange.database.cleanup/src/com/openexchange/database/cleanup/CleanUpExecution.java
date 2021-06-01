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

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link CleanUpExecution} - The actual clean-up execution to perform for a certain database schema.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public interface CleanUpExecution {

    /**
     * Prepares the clean-up.
     * <p>
     * This method is invoked prior to invoking <code>isApplicableFor()</code> and <code>executeFor()</code> for each database schema.
     *
     * @param state The cross-schema state for this execution, which allows to store arbitrary data that is kept for each schema-wise invocation
     * @return <code>true</code> if this execution has successfully prepared for schema-wise clean-up executions; otherwise <code>false</code> to abort
     * @throws OXException If preparation fails and thus prevents from invoking schema-wise clean-up executions
     */
    default boolean prepareCleanUp(Map<String, Object> state) throws OXException {
        return true;
    }

    /**
     * Checks if this clean-up execution is applicable for given database schema; e.g. check if certain tables are available.
     *
     * @param schema The database schema name
     * @param representativeContextId The identifier of a representative context in that schema
     * @param databasePoolId The identifier of the associated database pool
     * @param state The cross-schema state for this execution, which allows to store arbitrary data that is kept for each schema-wise invocation
     * @param connectionProvider The connection provider instance that can be used to obtain a <b>read-only</b> connection for given database schema
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If check for applicability fails
     */
    boolean isApplicableFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException;

    /**
     * Executes this clean-up execution against given database schema.
     *
     * @param schema The database schema name
     * @param representativeContextId The identifier of a representative context in that schema
     * @param databasePoolId The identifier of the associated database pool
     * @param state The cross-schema state for this execution, which allows to store arbitrary data that is kept for each schema-wise invocation
     * @param connectionProvider The connection provider instance that can be used to obtain a <b>read-write</b> connection for given database schema
     * @throws OXException If execution fails
     */
    void executeFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException;

    /**
     * Finishes the clean-up.
     * <p>
     * This method is invoked after invoking <code>isApplicableFor()</code> and <code>executeFor()</code> for each database schema, but only
     * if <code>prepareCleanUp()</code> advertised <code>true</code>.
     *
     * @param state The cross-schema state for this execution, which allows to store arbitrary data that is kept for each schema-wise invocation
     * @throws OXException If finishing action(s) fail
     */
    default void finishCleanUp(Map<String, Object> state) throws OXException {
        // Nothing
    }

}
