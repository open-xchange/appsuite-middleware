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

package com.openexchange.admin.storage.sqlStorage;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.database.Assignment;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;

public class OXAdminPoolDBPool implements OXAdminPoolInterface {

    private final Log log = LogFactory.getLog(this.getClass());
    private DatabaseService service;

    public OXAdminPoolDBPool() {
        super();
    }

    @Override
    public void setService(DatabaseService service) {
        this.service = service;
    }

    @Override
    public void removeService() {
        setService(null);
    }

    public DatabaseService getService() throws PoolException {
        if (null == service) {
            throw new PoolException("DatabaseService is missing.");
        }
        return service;
    }

    @Override
    public Connection getConnectionForConfigDB() throws PoolException {
        final Connection con;
        try {
            con = getService().getWritable();
        } catch (OXException e) {
            log.error("Error pickup configdb database write connection from pool!", e);
            throw new PoolException(e.getMessage());
        }
        return con;
    }

    @Override
    public Connection getConnectionForContext(int contextId) throws PoolException {
        final Connection con;
        try {
            con = getService().getWritable(contextId);
        } catch (OXException e) {
            log.error("Error pickup context database write connection from pool!", e);
            throw new PoolException(e.getMessage());
        }
        return con;
    }

    @Override
    public Connection getConnectionForContextNoTimeout(int contextId) throws PoolException {
        final Connection con;
        try {
            con = getService().getForUpdateTask(contextId);
        } catch (OXException e) {
            log.error("Error pickup context database write connection from pool!", e);
            throw new PoolException(e.getMessage());
        }
        return con;
    }

    @Override
    public boolean pushConnectionForConfigDB(Connection con) throws PoolException {
        try {
            if (con != null && !con.getAutoCommit() && !con.isClosed()) {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Error pushing configdb write connection to pool!", e);
            throw new PoolException(e.getMessage());
        } finally {
            getService().backWritable(con);
        }
        return true;
    }

    @Override
    public boolean pushConnectionForContext(int contextId, Connection con) throws PoolException {
        try {
            if (con != null && !con.getAutoCommit() && !con.isClosed()) {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Error pushing context database write connection to pool!", e);
            throw new PoolException(e.getMessage());
        } finally {
            getService().backWritable(contextId, con);
        }
        return true;
    }

    @Override
    public boolean pushConnectionForContextNoTimeout(int contextId, Connection con) throws PoolException {
        try {
            if (null != con && !con.getAutoCommit() && !con.isClosed()) {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Error pushing context database write connection to pool!", e);
            throw new PoolException(e.getMessage());
        } finally {
            getService().backForUpdateTask(contextId, con);
        }
        return true;
    }

    @Override
    public int getServerId() throws PoolException {
        final int serverId;
        try {
            serverId = getService().getServerId();
        } catch (OXException e) {
            log.error("Error getting the identifier of the server! This is normal until at least one server is configured.", e);
            throw new PoolException(e.getMessage());
        }
        return serverId;
    }

    @Override
    public void writeAssignment(Connection con, Assignment assign) throws PoolException {
        try {
            getService().writeAssignment(con, assign);
        } catch (OXException e) {
            log.error("Error writing a context to database assigment.", e);
            throw new PoolException(e.getMessage());
        }
    }
}
