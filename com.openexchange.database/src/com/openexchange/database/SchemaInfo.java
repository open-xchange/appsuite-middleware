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

package com.openexchange.database;

/**
 * {@link SchemaInfo} - An immutable simple wrapper for a pool identifier and schema name tuple.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SchemaInfo {

    /**
     * Creates a new {@code SchemaInfo} instance for specified pool identifier and schema name.
     *
     * @param poolId The identifier of the database pool, in which the schema resides
     * @param schema The schema name
     * @return The resulting {@code SchemaInfo} instance
     */
    public static SchemaInfo valueOf(int poolId, String schema) {
        return new SchemaInfo(poolId, schema);
    }

    // ------------------------------------------------------------------------------------------------------------

    private final int poolId;
    private final String schema;
    private int hash = 0;

    /**
     * Initializes a new {@link SchemaInfo}.
     */
    private SchemaInfo(int poolId, String schema) {
        super();
        this.poolId = poolId;
        this.schema = schema;
    }

    /**
     * Gets the identifier of the database pool, in which the schema resides
     *
     * @return The pool identifier
     */
    public int getPoolId() {
        return poolId;
    }

    /**
     * Gets the schema name
     *
     * @return The schema name
     */
    public String getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        // Not thread-safe, but does not matter. In worst case a thread calculates the hash code itself
        int result = hash;
        if (result == 0) {
            result = 31 * 1 + poolId;
            result = 31 * result + ((schema == null) ? 0 : schema.hashCode());
            this.hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SchemaInfo)) {
            return false;
        }
        SchemaInfo other = (SchemaInfo) obj;
        if (poolId != other.poolId) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32);
        builder.append("[poolId=").append(poolId).append(", ");
        builder.append("schema=").append(schema);
        builder.append("]");
        return builder.toString();
    }

}
