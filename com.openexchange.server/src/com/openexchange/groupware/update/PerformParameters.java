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

/**
 * Interface to be able to extend the parameters for the {@link UpdateTaskV2#perform(PerformParameters)} method without getting an
 * incompatible API change.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface PerformParameters extends ConnectionProvider {

    /**
     * Gets the schema.
     *
     * @return The schema
     */
    Schema getSchema();

    /**
     * Gets the optional (representative) identifier of the context for which the update process has been initialized.
     * <p/>
     * <strong>Note:</strong> for update tasks working on {@link WorkingLevel#SCHEMA}-level, the context ID should not be used as
     * restriction in <code>WHERE</code> clauses; instead the task should be executed for all contexts in the schema.
     *
     * @return The context ID or <code>-1</code>
     */
    int optContextId();

    /**
     * Gets thread-specific connection provider.
     *
     * @return The connection provider
     */
    ConnectionProvider getConnectionProvider();

    /**
     * Gets the progress state.
     *
     * @return The progress state
     */
    ProgressState getProgressState();

}
