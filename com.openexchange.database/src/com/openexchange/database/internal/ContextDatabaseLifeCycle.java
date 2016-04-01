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

package com.openexchange.database.internal;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.pooling.ExhaustedActions;

/**
 * Handles the life cycle of database connection pools for contexts databases.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContextDatabaseLifeCycle implements PoolLifeCycle {

    private static final Pattern pattern = Pattern.compile("[\\?\\&]([\\p{ASCII}&&[^=\\&]]*)=([\\p{ASCII}&&[^=\\&]]*)");

    private static final String SELECT = "SELECT url,driver,login,password,hardlimit,max,initial FROM db_pool WHERE db_pool_id=?";

    private final Management management;

    private final Timer timer;

    private final ConfigDatabaseService configDatabaseService;

    private final ConnectionPool.Config defaultPoolConfig;

    private final Map<Integer, ConnectionPool> pools = new ConcurrentHashMap<Integer, ConnectionPool>();

    public ContextDatabaseLifeCycle(final Configuration configuration, final Management management, final Timer timer, final ConfigDatabaseService configDatabaseService) {
        super();
        this.management = management;
        this.timer = timer;
        this.configDatabaseService = configDatabaseService;
        this.defaultPoolConfig = configuration.getPoolConfig();
    }

    @Override
    public ConnectionPool create(final int poolId) throws OXException {
        final ConnectionData data = loadPoolData(poolId);
        try {
            Class.forName(data.driverClass);
        } catch (final ClassNotFoundException e) {
            throw DBPoolingExceptionCodes.NO_DRIVER.create(e, data.driverClass);
        }
        final ConnectionPool retval = new ConnectionPool(data.url, data.props, getConfig(data));
        pools.put(I(poolId), retval);
        timer.addTask(retval.getCleanerTask());
        management.addPool(poolId, retval);
        return retval;
    }

    @Override
    public boolean destroy(final int poolId) {
        final ConnectionPool toDestroy = pools.remove(I(poolId));
        if (null == toDestroy) {
            return false;
        }
        management.removePool(poolId);
        timer.removeTask(toDestroy.getCleanerTask());
        toDestroy.destroy();
        return true;
    }

    private ConnectionPool.Config getConfig(final ConnectionData data) {
        final ConnectionPool.Config retval = defaultPoolConfig.clone();
        retval.maxActive = data.max;
        if (data.block) {
            retval.exhaustedAction = ExhaustedActions.BLOCK;
        } else {
            retval.exhaustedAction = ExhaustedActions.GROW;
        }
        return retval;
    }

    private static void parseUrlToProperties(final ConnectionData retval) throws OXException {
        final int paramStart = retval.url.indexOf('?');
        if (-1 != paramStart) {
            final Matcher matcher = pattern.matcher(retval.url);
            retval.url = retval.url.substring(0, paramStart);
            while (matcher.find()) {
                final String name = matcher.group(1);
                final String value = matcher.group(2);
                if (name != null && name.length() > 0 && value != null && value.length() > 0) {
                    try {
                        retval.props.put(name, URLDecoder.decode(value, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw DBPoolingExceptionCodes.PARAMETER_PROBLEM.create(e, value);
                    }
                }
            }
        }
    }

    ConnectionData loadPoolData(final int poolId) throws OXException {
        ConnectionData retval = null;
        final Connection con = configDatabaseService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT);
            stmt.setInt(1, poolId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = new ConnectionData();
                retval.props = new Properties();
                int pos = 1;
                retval.url = result.getString(pos++);
                retval.driverClass = result.getString(pos++);
                retval.props.put("user", result.getString(pos++));
                retval.props.put("password", result.getString(pos++));
                retval.block = result.getBoolean(pos++);
                retval.max = result.getInt(pos++);
                retval.min = result.getInt(pos++);
            } else {
                throw DBPoolingExceptionCodes.NO_DBPOOL.create(I(poolId));
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            configDatabaseService.backReadOnly(con);
        }
        parseUrlToProperties(retval);
        return retval;
    }
}
