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

package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.java.Strings;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * {@link AbstractLiquibaseUtf8mb4Adapter}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class AbstractLiquibaseUtf8mb4Adapter extends AbstractConvertUtf8ToUtf8mb4Task implements CustomTaskChange {

    @Override
    public final void execute(Database database) throws CustomChangeException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (!(databaseConnection instanceof JdbcConnection)) {
            throw new CustomChangeException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getName() + ", but of type: " + databaseConnection.getClass().getName());
        }

        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractLiquibaseUtf8mb4Adapter.class);

        Connection con = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        try {
            String schemaName = con.getCatalog() != null && Strings.isNotEmpty(con.getCatalog()) ? con.getCatalog() : (database.getDefaultCatalogName() != null && Strings.isNotEmpty(database.getDefaultCatalogName()) ? database.getDefaultCatalogName() : getDefaultSchemaName());

            before(con, schemaName);
            innerPerform(con, schemaName);
            after(con, schemaName);
        } catch (SQLException e) {
            logger.error("Failed to convert schema to utf8mb4", e);
            throw new CustomChangeException("SQL error", e);
        } catch (RuntimeException e) {
            logger.error("Failed to convert schema to utf8mb4", e);
            throw new CustomChangeException("Runtime error", e);
        }
    }

    protected abstract String getDefaultSchemaName();

    protected abstract void before(Connection configDbCon, String schemaName) throws SQLException;

    protected abstract void after(Connection configDbCon, String schemaName) throws SQLException;

    @Override
    public String[] getDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final void before(PerformParameters params, Connection connection) throws SQLException {
        // cannot be used
    }

    @Override
    protected final void after(PerformParameters params, Connection connection) throws SQLException {
        // cannot be used
    }

    @Override
    public final void setUp() throws SetupException {
        // Nothing
    }

    @Override
    public final void setFileOpener(ResourceAccessor resourceAccessor) {
        // Ignore
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
