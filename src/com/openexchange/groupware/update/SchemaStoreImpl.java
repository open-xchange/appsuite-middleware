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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.openexchange.database.Database;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.SchemaException;
import com.openexchange.groupware.update.exception.SchemaExceptionFactory;
import com.openexchange.server.DBPoolingException;

/**
 * Implements loading and storing the schema version information.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@OXExceptionSource(
    classId = Classes.SCHEMA_STORE_IMPL,
    component = Component.UPDATE
)
public class SchemaStoreImpl extends SchemaStore {

    /**
     * SQL command for selecting the version from the schema.
     */
    private static final String SELECT = "SELECT version,locked,gw_compatible,"
        + "admin_compatible,server FROM version";

    /**
     * For creating exceptions.
     */
    private static final SchemaExceptionFactory EXCEPTION =
        new SchemaExceptionFactory(SchemaStoreImpl.class);

    /**
     * Default constructor.
     */
    public SchemaStoreImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @OXThrowsMultiple(
        category = {Category.PROGRAMMING_ERROR, Category.SETUP_ERROR },
        desc = {"", "" },
        exceptionId = {1, 2 },
        msg = {"A SQL error occured while reading schema version information: "
            + "%1$s.", "No row found in table update." }
    )
    public Schema getSchema(final int contextId) throws SchemaException {
        Connection con;
        try {
            con = Database.get(contextId, false);
        } catch (DBPoolingException e) {
            throw new SchemaException(e);
        }
        Statement stmt = null;
        ResultSet result = null;
        SchemaImpl schema = null;
        try {
            final DatabaseMetaData meta = con.getMetaData();
            result = meta.getTables(null, null, "version", null);
            if (!result.next()) {
                return SchemaImpl.FIRST;
            }
            stmt = con.createStatement();
            result = stmt.executeQuery(SELECT);
            if (result.next()) {
                schema = new SchemaImpl();
                int pos = 1;
                schema.setDBVersion(result.getInt(pos++));
                schema.setLocked(result.getBoolean(pos++));
                schema.setGroupwareCompatible(result.getBoolean(pos++));
                schema.setAdminCompatible(result.getBoolean(pos++));
                schema.setServer(result.getString(pos++));
            } else {
                throw EXCEPTION.create(2);
            }
        } catch (SQLException e) {
            throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            Database.back(contextId, false, con);
        }
        return schema;
    }

}
