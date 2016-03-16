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

package com.openexchange.database.internal;

import com.openexchange.exception.OXException;

/**
 * {@link GlobalDbConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GlobalDbConfig {

    /** The name used for the special "default" context group */
    static final String DEFAULT_GROUP = "default";

    private final String schema;
    private final int readPoolId;
    private final int writePoolId;

    /**
     * Initializes a new {@link GlobalDbConfig}.
     *
     * @param schema The schema name
     * @param readPoolId The read pool identifier
     * @param writePoolId The write pool identifier
     */
    GlobalDbConfig(String schema, int readPoolId, int writePoolId) {
        super();
        this.schema = schema;
        this.readPoolId = readPoolId;
        this.writePoolId = writePoolId;
    }

    /**
     * Gets an appropriate read-/write-pool assignment for this global database config.
     *
     * @return The assignment
     */
    public AssignmentImpl getAssignment() throws OXException {
        return getAssignment(Server.getServerId());
    }

    /**
     * Gets an appropriate read-/write-pool assignment for this global database config.
     *
     * @param serverID The server identifier to use
     * @return The assignment
     */
    public AssignmentImpl getAssignment(int serverID) throws OXException {
        return new AssignmentImpl(0, serverID, readPoolId, writePoolId, schema);
    }

    /**
     * Gets the schema name.
     *
     * @return The schema name
     */
    public String getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + readPoolId;
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        result = prime * result + writePoolId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GlobalDbConfig)) {
            return false;
        }
        GlobalDbConfig other = (GlobalDbConfig) obj;
        if (readPoolId != other.readPoolId) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }
        if (writePoolId != other.writePoolId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GlobalDbConfig [schema=" + schema + ", readPoolId=" + readPoolId + ", writePoolId=" + writePoolId + "]";
    }

}
