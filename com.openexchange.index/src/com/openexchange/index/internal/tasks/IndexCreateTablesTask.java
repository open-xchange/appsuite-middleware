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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.index.internal.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.internal.IndexServiceLookup;
import com.openexchange.server.ServiceExceptionCodes;
import com.openexchange.tools.sql.DBUtils;
import static com.openexchange.index.internal.IndexDatabaseStuff.*;


/**
 * {@link IndexCreateTablesTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexCreateTablesTask extends UpdateTaskAdapter {    
    
    public IndexCreateTablesTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        final DatabaseService dbService = IndexServiceLookup.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCodes.SERVICE_UNAVAILABLE.create(DatabaseService.class.getSimpleName());
        }
        
        final Connection writeCon = dbService.getWritable();
        try {            
            PreparedStatement stmt = null;
            /* 
             * Create table index_servers in configDb
             */
            try {            
                if (DBUtils.tableExists(writeCon, TBL_IDX_SERVER)) {
                    return;
                }
                stmt = writeCon.prepareStatement(SQL_CREATE_SERVER_TBL);
                stmt.executeUpdate();            
            } catch (SQLException e) {
                throw IndexExceptionCodes.SQL_ERROR.create(e);
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            
            /* 
             * Create table user_module2index in configDb
             */
            try {            
                if (DBUtils.tableExists(writeCon, TBL_IDX_MAPPING)) {
                    return;
                }
                stmt = writeCon.prepareStatement(SQL_CREATE_MAPPING_TBL);
                stmt.executeUpdate();            
            } catch (SQLException e) {
                throw IndexExceptionCodes.SQL_ERROR.create(e);
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }   
        } finally {
            dbService.backWritable(writeCon);
        }            
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

}
