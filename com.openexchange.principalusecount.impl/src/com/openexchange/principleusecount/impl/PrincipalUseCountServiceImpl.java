/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.principleusecount.impl;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.principalusecount.PrincipalUseCountService;
import com.openexchange.principleusecount.impl.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link PrincipalUseCountServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class PrincipalUseCountServiceImpl implements PrincipalUseCountService {

    @Override
    public void increment(Session session, int principal) throws OXException {
        try {
            Task<Void> task = new PrincipalUseCountTask(session, principal, PrincipalUseCountTask.TaskType.INCREMENT);
            ThreadPools.submitElseExecute(task);
        } catch (RuntimeException e) {
            throw PrincipalUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
        } catch (Exception e) {
            throw PrincipalUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
        }
    }

    @Override
    public void reset(Session session, int principal) throws OXException {
        try {
            Task<Void> task = new PrincipalUseCountTask(session, principal, PrincipalUseCountTask.TaskType.DELETE);
            ThreadPools.submitElseExecute(task);
        } catch (RuntimeException e) {
            throw PrincipalUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
        } catch (Exception e) {
            throw PrincipalUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
        }
    }

    @Override
    public void set(Session session, int principal, int value) throws OXException {
        try {
            Task<Void> task = new PrincipalUseCountTask(session, principal, I(value));
            ThreadPools.submitElseExecute(task);
        } catch (RuntimeException e) {
            throw PrincipalUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
        } catch (Exception e) {
            throw PrincipalUseCountExceptionCode.UNKNOWN.create(e, e.getMessage());
        }

    }

    private static final String SELECT_USECOUNT = "SELECT principal, value FROM principalUseCount WHERE cid=? AND user=? AND principal IN (";

    @Override
    public Map<Integer, Integer> get(Session session, Integer... principals) throws OXException {
        if (principals == null || principals.length == 0) {
            return Collections.emptyMap();
        }
        DatabaseService dbService = Services.getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class);
        }
        String sql = DBUtils.getIN(SELECT_USECOUNT, principals.length);

        Connection con = dbService.getReadOnly(session.getContextId());
        ResultSet rs = null;
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            stmt.setInt(index++, session.getContextId());
            stmt.setInt(index++, session.getUserId());
            for (Integer id : principals) {
                stmt.setInt(index++, id.intValue());
            }
            rs = stmt.executeQuery();
            Map<Integer, Integer> result = new HashMap<>();
            // Initialize result map with 0 values
            for (Integer id : principals) {
                result.put(id, I(0));
            }
            while (rs.next()) {
                result.put(I(rs.getInt("principal")), I(rs.getInt("value")));
            }
            Map<Integer, Integer> sorted = result.entrySet().stream().sorted(Entry.comparingByValue()).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
            return sorted;
        } catch (SQLException e) {
            throw PrincipalUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs);
            if (con != null) {
                dbService.backReadOnly(session.getContextId(), con);
            }
        }
    }

}
