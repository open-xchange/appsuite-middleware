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
package com.openexchange.report.appsuite.storage;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.report.appsuite.internal.Services;

/**
 * The {@link ContextLoader} class is used to load data from the database, that is needed
 * by the report creation functions.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.2
 */
public class ContextLoader {

    DatabaseService dbService;

    public ContextLoader() {
        super();
        this.dbService = Services.getService(DatabaseService.class);
    }

    /**
     * Loads all context-ids that are in the same schema, as the given cid. The given cid
     * will be also returned in the list.
     * 
     * @param cid
     * @return a list with all context ids in the same schema
     * @throws SQLException
     * @throws OXException
     */
    public List<Integer> getAllContextIdsInSameSchema(int cid) throws SQLException, OXException {
        ArrayList<Integer> result = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet sqlResult = null;
        Connection currentConnection = this.dbService.getReadOnly();
        try {
            stmt = currentConnection.prepareStatement("SELECT cid FROM context_server2db_pool WHERE db_schema = (SELECT db_schema FROM context_server2db_pool where cid =?)");
            stmt.setInt(1, cid);
            sqlResult = stmt.executeQuery();
            while (sqlResult.next()) {
                result.add(sqlResult.getInt(1));
            }
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(sqlResult, stmt);
            dbService.backReadOnly(currentConnection);
        }
        return result;
    }

}
