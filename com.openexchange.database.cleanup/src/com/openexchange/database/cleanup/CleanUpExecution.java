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
