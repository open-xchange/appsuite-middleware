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

package com.openexchange.index.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.index.ConfigIndexService;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexServer;
import com.openexchange.index.IndexUrl;
import com.openexchange.server.ServiceExceptionCodes;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link ConfigIndexServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ConfigIndexServiceImpl implements ConfigIndexService {
    
    private final DatabaseService dbService;
    
    private static final String SELECT_INDEX_URL = "SELECT " +
    		                                           "s.id, s.serverUrl, s.maxIndices, s.socketTimeout, s.connectionTimeout, s.maxConnections, u.index " +
    		                                       "FROM " +
    		                                           "index_servers AS s " +
    		                                       "JOIN " +
    		                                           "user_module2index AS u " +
    		                                       "ON " +
    		                                           "s.id = u.server " +
    		                                       "WHERE " +
    		                                           "u.cid = ? AND u.uid = ? AND u.module = ?";
    
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ConfigIndexServiceImpl.class));
    

    public ConfigIndexServiceImpl(final DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public IndexUrl getReadOnlyURL(final int cid, final int uid, final int module) throws OXException {
        if (dbService == null) {
            throw ServiceExceptionCodes.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        
        final Connection readCon = dbService.getReadOnly();        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = readCon.prepareStatement(SELECT_INDEX_URL);
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i, module);
            rs = stmt.executeQuery();
            
            if (!rs.next()) {
                throw IndexExceptionCodes.INDEX_NOT_FOUND.create(uid, module, cid);
            }
            
            i = 1;
            final int id = rs.getInt(i++);
            final String serverUrl = rs.getString(i++);
            final int maxIndices = rs.getInt(i++);
            final int socketTimeout = rs.getInt(i++);
            final int connectionTimeout = rs.getInt(i++);
            final int maxConnections = rs.getInt(i++);
            final String index = rs.getString(i);
            
            final IndexServerImpl server = new IndexServerImpl(id, serverUrl);
            server.setMaxIndices(maxIndices);
            server.setSoTimeout(socketTimeout);
            server.setConnectionTimeout(connectionTimeout);
            server.setMaxConnectionsPerHost(maxConnections);
            
            final IndexUrlImpl indexUrl = new IndexUrlImpl(server, index);            
            return indexUrl;
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);            
            dbService.backReadOnly(readCon);
        }
    }

    @Override
    public IndexUrl getWriteURL(final int cid, final int uid, final int module) throws OXException {
        return getWriteURL(cid, uid, module);
    }

    @Override
    public void unregisterIndexServer(int serverId, boolean deleteMappings) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int registerIndexServer(IndexServer server) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IndexServer[] getAllIndexServers() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void modifyIndexServer(IndexServer server) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addIndexMapping(int cid, int uid, int module, int server, String index) throws OXException {
        // TODO Auto-generated method stub
        
    }

}
