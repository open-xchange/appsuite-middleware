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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link GlobalDbConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GlobalDbConfig {

    /** The name used for the special "default" context group */
    static final String DEFAULT_GROUP = "default";

    /**
     * Parses configuration settings for the global database from the supplied YAML map.
     *
     * @param yaml The global database configurations in a YAML map
     * @return The global db configurations, mapped by their assigned group names, or an empty map if none are defined
     */
    static Map<String, GlobalDbConfig> parse(Map<String, Object> yaml) throws OXException {
        Map<String, GlobalDbConfig> configs = new HashMap<String, GlobalDbConfig>();
        for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            if (null == entry.getValue() || false == Map.class.isInstance(entry.getValue())) {
                throw DBPoolingExceptionCodes.INVALID_GLOBALDB_CONFIGURATION.create("Malformed configuration at " + entry.getKey());
            }
            Map<String, Object> values = (Map<String, Object>) entry.getValue();
            int readPoolId;
            int writePoolId;
            try {
                readPoolId = Integer.valueOf(String.valueOf(values.get("readPoolId")));
                writePoolId = Integer.valueOf(String.valueOf(values.get("writePoolId")));
            } catch (NumberFormatException e) {
                throw DBPoolingExceptionCodes.INVALID_GLOBALDB_CONFIGURATION.create(e, "Pool IDs can't be parsed for " + entry.getKey());
            }
            String schema = String.valueOf(values.get("schema"));
            if (null == schema) {
                throw DBPoolingExceptionCodes.INVALID_GLOBALDB_CONFIGURATION.create("Schema missing for " + entry.getKey());
            }
            GlobalDbConfig dbConfig = new GlobalDbConfig(schema, readPoolId, writePoolId);
            Object groups = values.get("groups");
            if (null == groups || false == List.class.isInstance(groups)) {
                throw DBPoolingExceptionCodes.INVALID_GLOBALDB_CONFIGURATION.create("Groups missing for " + entry.getKey());
            }
            for (String group : (List<String>) groups) {
                if (null != configs.put(group, dbConfig)) {
                    throw DBPoolingExceptionCodes.INVALID_GLOBALDB_CONFIGURATION.create("Group " + group + " is defined a second time at " + entry.getKey());
                }
            }
        }
        return configs;
    }

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
    private GlobalDbConfig(String schema, int readPoolId, int writePoolId) {
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
        return new AssignmentImpl(0, Server.getServerId(), readPoolId, writePoolId, schema);
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
