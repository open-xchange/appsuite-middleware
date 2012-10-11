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

package com.openexchange.index.solr.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link IndexedFoldersCreateTableTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexedFoldersCreateTableTask extends UpdateTaskAdapter {
    
    private final IndexedFoldersCreateTableService service;

    public IndexedFoldersCreateTableTask(IndexedFoldersCreateTableService service) {
        super();
        this.service = service;
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        int contextId = params.getContextId();
        Connection con = dbService.getForUpdateTask(contextId);
        try {
            String[] tables = service.tablesToCreate();
            String[] statements = service.getCreateStatements();
            DBUtils.startTransaction(con);
            for (int i = 0; i < service.tablesToCreate().length; i++) {
                String table = tables[i];
                if (!DBUtils.tableExists(con, table)) {
                    PreparedStatement stmt = null;
                    try {
                        String statement = statements[i];
                        stmt = con.prepareStatement(statement);
                        stmt.executeUpdate();
                    } finally {
                        DBUtils.closeSQLStuff(stmt);
                    }
                }
            }
            con.commit();
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            dbService.backForUpdateTask(contextId, con);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

}
