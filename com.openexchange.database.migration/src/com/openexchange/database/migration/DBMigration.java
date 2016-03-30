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

package com.openexchange.database.migration;

import liquibase.resource.ResourceAccessor;

/**
 * {@link DBMigration}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DBMigration {

    private final DBMigrationConnectionProvider connectionProvider;
    private final String fileLocation;
    private final ResourceAccessor accessor;
    private final String schema;

    /**
     * Initializes a new {@link DBMigration}.
     *
     * @param connectionProvider The connection provider used to acquire and release database connections
     * @param fileLocation Location of the changelog file (e.g. "/liquibase/configdbChangeLog.xml")
     * @param accessor The {@link ResourceAccessor} to read in the changelog file identified by <code>fileLocation</code>
     * @param schema The database schema name the migration operates on
     */
    public DBMigration(DBMigrationConnectionProvider connectionProvider, String fileLocation, ResourceAccessor accessor, String schema) {
        super();
        this.connectionProvider = connectionProvider;
        this.fileLocation = fileLocation;
        this.accessor = accessor;
        this.schema = schema;
    }

    /**
     * Gets the connection provider.
     *
     * @return The connectionProvider
     */
    public DBMigrationConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    /**
     * Gets the file location.
     *
     * @return The file location
     */
    public String getFileLocation() {
        return fileLocation;
    }

    /**
     * Gets the database schema name.
     *
     * @return The schema name
     */
    public String getSchemaName() {
        return schema;
    }

    /**
     * Gets the resource accessor.
     *
     * @return The accessor
     */
    public ResourceAccessor getAccessor() {
        return accessor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileLocation == null) ? 0 : fileLocation.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
        if (!(obj instanceof DBMigration)) {
            return false;
        }
        DBMigration other = (DBMigration) obj;
        if (fileLocation == null) {
            if (other.fileLocation != null) {
                return false;
            }
        } else if (!fileLocation.equals(other.fileLocation)) {
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
        return "DBMigration [fileLocation=" + fileLocation + ", schema=" + schema + "]";
    }

}
