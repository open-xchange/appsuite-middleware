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

package com.openexchange.database.internal;

import static com.openexchange.database.internal.DBUtils.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogProperties;

/**
 * Implements the database service to the config database.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ConfigDatabaseServiceImpl implements ConfigDatabaseService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigDatabaseServiceImpl.class);

    // ------------------------------------------------------------------------------------------------ //

    private final Pools pools;
    private final ConfigDatabaseAssignmentService assignmentService;
    private final ReplicationMonitor monitor;

    ConfigDatabaseServiceImpl(ConfigDatabaseAssignmentService assignmentService, Pools pools, ReplicationMonitor monitor) {
        super();
        this.assignmentService = assignmentService;
        this.pools = pools;
        this.monitor = monitor;
    }

    private Connection get(final boolean write) throws OXException {
        final AssignmentImpl assign = assignmentService.getConfigDBAssignment();
        LogProperties.putProperty(LogProperties.Name.DATABASE_SCHEMA, "configdb");
        return monitor.checkFallback(pools, assign, false, write);
        // TODO Enable the following if the configuration database gets a table replicationMonitor.
        // return ReplicationMonitor.checkActualAndFallback(pools, assign, false, write);
    }

    private static void back(final Connection con) {
        if (null == con) {
            LogProperties.putProperty(LogProperties.Name.DATABASE_SCHEMA, null);
            final OXException e = DBPoolingExceptionCodes.NULL_CONNECTION.create();
            LOG.error("", e);
            return;
        }
        try {
            con.close();
        } catch (SQLException e) {
            OXException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            LOG.error("", e1);
        } finally {
            LogProperties.putProperty(LogProperties.Name.DATABASE_SCHEMA, null);
        }
    }

    @Override
    public Connection getReadOnly() throws OXException {
        return get(false);
    }

    @Override
    public Connection getWritable() throws OXException {
        return get(true);
    }

    @Override
    public void backReadOnly(final Connection con) {
        back(con);
    }

    @Override
    public void backWritable(final Connection con) {
        back(con);
    }

    @Override
    public int[] listContexts(final int poolId) throws OXException {
        final List<Integer> tmp = new ArrayList<Integer>();
        final Connection con = getReadOnly();
        final String getcid = "SELECT cid FROM context_server2db_pool WHERE read_db_pool_id=? OR write_db_pool_id=?";
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getcid);
            stmt.setInt(1, poolId);
            stmt.setInt(2, poolId);
            result = stmt.executeQuery();
            while (result.next()) {
                tmp.add(I(result.getInt(1)));
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            backReadOnly(con);
        }
        final int[] retval = new int[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            retval[i] = tmp.get(i).intValue();
        }
        return retval;
    }

    @Override
    public int getServerId() throws OXException {
        return Server.getServerId();
    }

    @Override
    public String getServerName() throws OXException {
        return Server.getServerName();
    }
}
