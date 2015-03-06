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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.GlobalDatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link GlobalDatabaseServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GlobalDatabaseServiceImpl implements GlobalDatabaseService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GlobalDatabaseServiceImpl.class);

    private final Pools pools;
    private final ReplicationMonitor monitor;
    private final Map<String, GlobalDbConfig> globalDbConfigs;
    private final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link GlobalDatabaseServiceImpl}.
     *
     * @param pools A reference to the connection pool
     * @param monitor The replication monitor
     * @param globalDbConfigs The known global database configurations
     * @param configViewFactory The config view factory
     */
    public GlobalDatabaseServiceImpl(Pools pools, ReplicationMonitor monitor, Map<String, GlobalDbConfig> globalDbConfigs, ConfigViewFactory configViewFactory) {
        super();
        this.pools = pools;
        this.monitor = monitor;
        this.globalDbConfigs = globalDbConfigs;
        this.configViewFactory = configViewFactory;
    }

    @Override
    public Connection getReadOnlyForGlobal(String group) throws OXException {
        return get(getAssignment(group), false, false);
    }

    @Override
    public Connection getReadOnlyForGlobal(int contextId) throws OXException {
        return get(getAssignment(contextId), false, false);
    }

    @Override
    public void backReadOnlyForGlobal(String group, Connection connection) {
        back(connection);
    }

    @Override
    public void backReadOnlyForGlobal(int contextId, Connection connection) {
        back(connection);
    }

    @Override
    public Connection getWritableForGlobal(String group) throws OXException {
        return get(getAssignment(group), true, false);
    }

    @Override
    public Connection getWritableForGlobal(int contextId) throws OXException {
        return get(getAssignment(contextId), true, false);
    }

    @Override
    public void backWritableForGlobal(String group, Connection connection) {
        back(connection);
    }

    @Override
    public void backWritableForGlobal(int contextId, Connection connection) {
        back(connection);
    }

    private AssignmentImpl getAssignment(String group) throws OXException {
        String name = Strings.isEmpty(group) ? GlobalDbConfig.DEFAULT_GROUP : group;
        GlobalDbConfig dbConfig = globalDbConfigs.get(name);
        if (null == dbConfig) {
            // TODO: fall back to "default" also in that case?
            throw DBPoolingExceptionCodes.NO_GLOBALDB_CONFIG_FOR_GROUP.create(group);
        }
        return dbConfig.getAssignment();
    }

    private AssignmentImpl getAssignment(int contextId) throws OXException {
        String group = configViewFactory.getView(-1, contextId).opt("com.openexchange.context.group", String.class, null);
        return getAssignment(group);
    }

    private Connection get(AssignmentImpl assignment, boolean write, boolean noTimeout) throws OXException {
        return monitor.checkActualAndFallback(pools, assignment, noTimeout, write);
    }

    private void back(Connection connection) {
        if (null == connection) {
            LOG.error("", DBPoolingExceptionCodes.NULL_CONNECTION.create());
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            LOG.error("", DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage()));
        }
    }

}
