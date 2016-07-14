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

package com.openexchange.admin.storage.sqlStorage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.database.Assignment;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;

public class OXAdminPoolDBPool implements OXAdminPoolInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXAdminPoolDBPool.class);

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
        try {
            return getService().getWritable();
        } catch (OXException e) {
            log.error("Error pickup configdb database write connection from pool!", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public Connection getWriteConnectionForConfigDB() throws PoolException {
        try {
            return getService().getWritable();
        } catch (OXException e) {
            log.error("Error pickup configdb database write connection from pool!", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public Connection getReadConnectionForConfigDB() throws PoolException {
        try {
            return getService().getReadOnly();
        } catch (OXException e) {
            log.error("Error pickup configdb database read connection from pool!", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public Connection getConnectionForContext(int contextId) throws PoolException {
        try {
            return getService().getWritable(contextId);
        } catch (OXException e) {
            log.error("Error pickup context database write connection from pool!", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public Connection getConnection(int poolId, String schema) throws PoolException {
        try {
            return getService().get(poolId, schema);
        } catch (OXException e) {
            log.error("Error pickup context database write connection from pool!", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public Connection getConnectionForContextNoTimeout(int contextId) throws PoolException {
        try {
            return getService().getForUpdateTask(contextId);
        } catch (OXException e) {
            log.error("Error pickup context database write connection from pool!", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public boolean pushConnectionForConfigDB(Connection con) throws PoolException {
        if (null != con) {
            try {
                if (!con.getAutoCommit() && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                log.error("Error pushing configdb write connection to pool!", e);
                throw new PoolException(e.getMessage());
            } finally {
                getService().backWritable(con);
            }
        }
        return true;
    }

    @Override
    public boolean pushReadConnectionForConfigDB(Connection con) throws PoolException {
        if (null != con) {
            try {
                if (!con.getAutoCommit() && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                log.error("Error pushing configdb read connection to pool!", e);
                throw new PoolException(e.getMessage());
            } finally {
                getService().backReadOnly(con);
            }
        }
        return true;
    }

    @Override
    public boolean pushWriteConnectionForConfigDB(Connection con) throws PoolException {
        if (null != con) {
            try {
                if (!con.getAutoCommit() && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                log.error("Error pushing configdb write connection to pool!", e);
                throw new PoolException(e.getMessage());
            } finally {
                getService().backWritable(con);
            }
        }
        return true;
    }

    @Override
    public boolean pushConnectionForContext(int contextId, Connection con) throws PoolException {
        if (null != con) {
            try {
                if (!con.getAutoCommit() && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                log.error("Error pushing context database write connection to pool!", e);
                throw new PoolException(e.getMessage());
            } finally {
                getService().backWritable(contextId, con);
            }
        }
        return true;
    }

    @Override
    public boolean pushConnectionForContextAfterReading(int contextId, Connection con) throws PoolException {
        if (null != con) {
            try {
                if (!con.getAutoCommit() && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                log.error("Error pushing context database write connection to pool!", e);
                throw new PoolException(e.getMessage());
            } finally {
                getService().backWritableAfterReading(contextId, con);
            }
        }
        return true;
    }

    @Override
    public boolean pushConnectionForContextNoTimeout(int contextId, Connection con) throws PoolException {
        if (null != con) {
            try {
                if (!con.getAutoCommit() && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                log.error("Error pushing context database write connection to pool!", e);
                throw new PoolException(e.getMessage());
            } finally {
                getService().backForUpdateTask(contextId, con);
            }
        }
        return true;
    }

    @Override
    public boolean pushConnection(int poolId, Connection con) throws PoolException {
        if (null != con) {
            try {
                if (!con.getAutoCommit() && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                log.error("Error pushing context database write connection to pool!", e);
                throw new PoolException(e.getMessage());
            } finally {
                getService().back(poolId, con);
            }
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

    @Override
    public void deleteAssignment(Connection con, int contextId) throws PoolException {
        try {
            getService().deleteAssignment(con, contextId);
        } catch (OXException e) {
            log.error("Error deleting a context to database assigment.", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public int[] getContextInSameSchema(Connection con, int contextId) throws PoolException {
        try {
            return getService().getContextsInSameSchema(con, contextId);
        } catch (OXException e) {
            log.error("Error getting all contexts from the same schema.", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public int[] getContextInSchema(Connection con, int poolId, String schema) throws PoolException {
        try {
            return getService().getContextsInSchema(con, poolId, schema);
        } catch (OXException e) {
            log.error("Error getting all contexts from the same schema.", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public int[] listContexts(int poolId) throws PoolException {
        try {
            return getService().listContexts(poolId);
        } catch (OXException e) {
            log.error("Error getting all contexts from the same schema.", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts) throws PoolException {
        try {
            return getService().getUnfilledSchemas(con, poolId, maxContexts);
        } catch (OXException e) {
            log.error("Error getting unfilled schemas", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public Map<String, Integer> getContextCountPerSchema(Connection con, int poolId, int maxContexts) throws PoolException {
        try {
            return getService().getContextCountPerSchema(con, poolId, maxContexts);
        } catch (OXException e) {
            log.error("Error getting unfilled schemas", e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public int getWritePool(int contextId) throws PoolException {
        try {
            return getService().getWritablePool(contextId);
        } catch (OXException e) {
            log.error("Error getting the write pool identifier for context {}.", contextId, e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public String getSchemaName(int contextId) throws PoolException {
        try {
            return getService().getSchemaName(contextId);
        } catch (OXException e) {
            log.error("Error getting the schema name for context {}.", contextId, e);
            throw new PoolException(e.getMessage());
        }
    }

    @Override
    public void lock(Connection con, int writePoolId) throws PoolException {
        try {
            getService().lock(con, writePoolId);
        } catch (OXException e) {
            log.error("Error locking context_server2db_pool table", e);
            throw new PoolException(e.getMessage());
        }
    }
}
