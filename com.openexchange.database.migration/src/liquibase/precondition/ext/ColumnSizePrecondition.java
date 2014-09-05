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

package liquibase.precondition.ext;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.precondition.CustomPrecondition;

/**
 * Verifies the size of the varchar column.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ColumnSizePrecondition implements CustomPrecondition {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ColumnSizePrecondition.class);

    private int expectedSize;

    private String tableName;

    private String columnName;

    /**
     * Sets the expected size of the column
     *
     * @param expectedSize - int with the size
     */
    public void setExpectedSize(String expectedSize) {
        this.expectedSize = Integer.parseInt(expectedSize);
    }

    /**
     * Sets the tableName
     *
     * @param tableName The tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Sets the columnName
     *
     * @param columnName The columnName to set
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void check(Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException {
        JdbcConnection connection = (JdbcConnection) database.getConnection();

        DatabaseMetaData meta;
        try {
            meta = connection.getUnderlyingConnection().getMetaData();
            ResultSet rsColumns = meta.getColumns(null, null, tableName, null);
            while (rsColumns.next()) {
                final String lColumnName = rsColumns.getString("COLUMN_NAME");
                if (columnName.equals(lColumnName)) {
                    final int size = rsColumns.getInt("COLUMN_SIZE");
                    if (size == expectedSize) {
                        throw new CustomPreconditionFailedException("Column size is already up to date! Nothing to do.");
                    }
                }
            }
        } catch (SQLException sqlException) {
            LOG.error("Error while evaluating type of column " + columnName + " in table " + tableName + ".", sqlException);
        }
    }
}
