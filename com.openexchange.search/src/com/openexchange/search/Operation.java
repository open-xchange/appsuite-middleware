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

package com.openexchange.search;

import com.openexchange.search.SearchTerm.OperationPosition;

/**
 * {@link Operation} - A search term operation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Operation {

    /**
     * Gets this operation's string representation.
     *
     * @return The operation's string representation.
     */
    public String getOperation();

    /**
     * @return What the operation would look like in SQL
     */
    public String getSqlRepresentation();

    /**
     * @return What the operation would look like in LDAP
     */
    public String getLdapRepresentation();

    /**
     * Checks if specified string equals this operation's string representation.
     *
     * @param other The operation string to check for equality
     * @return <code>true</code> if specified string equals this operation's string representation; otherwise <code>false</code>.
     */
    public boolean equalsOperation(String other);

    /**
     * tells you where the operator is positioned in relation to the operand(s) in a SQL query
     */
    public OperationPosition getSqlPosition();

}
