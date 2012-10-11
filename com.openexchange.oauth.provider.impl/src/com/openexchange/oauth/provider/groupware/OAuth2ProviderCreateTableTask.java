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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.groupware;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.internal.OAuthProviderServiceLookup;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link OAuth2ProviderCreateTableTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuth2ProviderCreateTableTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link OAuth2ProviderCreateTableTask}.
     *
     * @param dbService
     */
    public OAuth2ProviderCreateTableTask() {
        super();
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final DatabaseService dbService = OAuthProviderServiceLookup.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final int contextId = params.getContextId();
        final Connection writeCon = dbService.getForUpdateTask(contextId);
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            DBUtils.startTransaction(writeCon); // BEGIN
            rollback = true;
            final String[] tableNames = OAuth2ProviderCreateTableService.getTablesToCreate();
            final String[] createStmts = OAuth2ProviderCreateTableService.getCreateStmts();
            for (int i = 0; i < tableNames.length; i++) {
                try {
                    if (tableExists(writeCon, tableNames[i])) {
                        continue;
                    }
                    stmt = writeCon.prepareStatement(createStmts[i]);
                    stmt.executeUpdate();
                } catch (final SQLException e) {
                    throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                }
            }
            writeCon.commit(); // COMMIT
        } catch (final OXException e) {
            if (rollback) {
                DBUtils.rollback(writeCon);
            }
            throw e;
        } catch (final Exception e) {
            if (rollback) {
                DBUtils.rollback(writeCon);
            }
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (rollback) {
                DBUtils.autocommit(writeCon);
            }
            dbService.backForUpdateTask(contextId, writeCon);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    private static final boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equalsIgnoreCase(table));
        } finally {
            DBUtils.closeSQLStuff(rs);
        }
        return retval;
    }

}
