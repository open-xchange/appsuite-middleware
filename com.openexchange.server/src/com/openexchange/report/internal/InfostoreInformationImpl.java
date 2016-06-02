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
package com.openexchange.report.internal;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.report.InfostoreInformationService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * The {@link InfostoreInformationImpl} class is an implementations of {@link InfostoreInformationService} class. 
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public class InfostoreInformationImpl implements InfostoreInformationService {

    
    /**
     * nececarryConnections, stores all schema connections, that are needed for executing all
     * functions with the same set of contextIds/userIds
     */
    private ArrayList<Connection> nececarryConnections;
    private DatabaseService dbService;

    /**
     * Initializes a new {@link InfostoreInformationImpl}.
     */
    public InfostoreInformationImpl() {
        super();
        this.nececarryConnections = new ArrayList<>();
        this.dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
    }

    @Override
    public Map<String, Integer> getFileSizeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
        String whereQuery = buildWhereClause(usersInContext, "created_by");
        String query = "SELECT min(file_size), max(file_size), avg(file_size), sum(file_size), count(*) FROM infostore_document" + whereQuery + "AND version_number > 0;";
        return this.getDataFromDB(usersInContext, query, "count", true, false, null, null);
    }

    @Override
    public Map<String, Integer> getFileCountMimetypeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
        LinkedHashMap<String, Integer> resultMap = new LinkedHashMap<>();

        // DB Connection
        if (this.nececarryConnections.size() == 0)
            this.loadAllNecessaryDBConnections(usersInContext);

        String whereQuery = buildWhereClause(usersInContext, "created_by");
        PreparedStatement stmt = null;
        ResultSet sqlResult = null;
        for (Connection currentConnection : this.nececarryConnections) {
            try {

                LinkedHashMap<String, Integer> queryMap = new LinkedHashMap<>();
                stmt = currentConnection.prepareStatement("SELECT file_mimetype, count(file_mimetype) FROM infostore_document" + whereQuery + " AND version_number > 0 GROUP BY file_mimetype;");
                sqlResult = stmt.executeQuery();
                while (sqlResult.next()) {
                    queryMap.put(sqlResult.getString(1), sqlResult.getInt(2));
                }
                calculateMinMaxAdds(resultMap, queryMap);

            } catch (final SQLException e) {
                throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(sqlResult, stmt);
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Integer> getStorageUseMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
        String whereQuery = buildWhereClause(usersInContext, "created_by");
        String query = "SELECT min(roundup.sum), max(roundup.sum), avg(roundup.sum), sum(roundup.sum), count(*) FROM " + "(SELECT cid, created_by, sum(file_size) AS sum FROM infostore_document" + whereQuery + "GROUP BY cid, created_by) AS roundup;";
        return this.getDataFromDB(usersInContext, query, "count", true, false, null, null);
    }

    @Override
    public Map<String, Integer> getFileCountMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
        String whereQuery = buildWhereClause(usersInContext, "created_by");
        String query = "SELECT min(roundup.count), max(roundup.count), avg(roundup.count), sum(roundup.count), count(DISTINCT roundup.created_by) FROM " + "(SELECT cid, created_by, count(version_number) AS count FROM infostore_document " + whereQuery + " AND version_number > 0 GROUP BY cid, created_by) AS roundup;";
        return this.getDataFromDB(usersInContext, query, "users", false, false, null, null);
    }

    @Override
    public Map<String, Integer> getFileCountInTimeframeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext, Date start, Date end) throws SQLException, OXException {
        String whereQuery = buildWhereClause(usersInContext, "created_by");
        String query = "SELECT min(roundup.count), max(roundup.count), avg(roundup.count), sum(roundup.count), count(*) FROM " + "(SELECT cid, created_by, count(version_number) AS count FROM infostore_document " + whereQuery + " AND creating_date > ? AND creating_date < ? AND version_number > 0 " + "GROUP BY cid, created_by) AS roundup;";
        return this.getDataFromDB(usersInContext, query, "count", true, true, start, end);
    }

    @Override
    public Map<String, Integer> getExternalStorageMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
        String whereQuery = buildWhereClause(usersInContext, "user");
        String query = "SELECT min(roundup.sum), max(roundup.sum), avg(roundup.sum), sum(roundup.sum), sum(roundup.users) FROM " + "(SELECT cid, user, count(id) AS sum, count(DISTINCT user) AS users FROM oauthAccounts" + whereQuery + " GROUP BY cid, user) AS roundup;";
        return this.getDataFromDB(usersInContext, query, "users", false, false, null, null);
    }

    @Override
    public Map<String, Integer> getFileCountNoVersions(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
        LinkedHashMap<String, Integer> resultMap = new LinkedHashMap<>();

        // DB Connection
        if (this.nececarryConnections.size() == 0)
            this.loadAllNecessaryDBConnections(usersInContext);

        String whereQuery = buildWhereClause(usersInContext, "created_by");
        PreparedStatement stmt = null;
        ResultSet sqlResult = null;
        for (Connection currentConnection : this.nececarryConnections) {
            try {
                LinkedHashMap<String, Integer> queryMap = new LinkedHashMap<>();
                stmt = currentConnection.prepareStatement("SELECT count(version_number) FROM infostore_document" + whereQuery + " AND version_number = 0;");
                sqlResult = stmt.executeQuery();
                while (sqlResult.next()) {
                    queryMap.put("total", sqlResult.getInt(1));
                }
                calculateMinMaxAdds(resultMap, queryMap);
            } catch (final SQLException e) {
                throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(sqlResult, stmt);
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Integer> getQuotaUsageMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        HashMap<String, Integer> resultMap = new HashMap<>();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }
        HashMap<String, Integer> filestoreMap = new HashMap<>();
        for (Entry<Integer, ArrayList<Integer>> contexts : usersInContext.entrySet()) {
            for (Integer userId : contexts.getValue()) {
                QuotaFileStorage userStorage = storageService.getQuotaFileStorage(userId, contexts.getKey());
                Long percent = userStorage.getUsage() * 100 / userStorage.getQuota();
                filestoreMap.put(userStorage.getUri().toString(), percent.intValue());
            }
        }
        Integer sum = 0;
        for (Entry<String, Integer> currentFilestore : filestoreMap.entrySet()) {
            sum += currentFilestore.getValue();
            if (resultMap.get("min") == null || resultMap.get("min") == 0 || resultMap.get("min") > currentFilestore.getValue()) {
                resultMap.put("min", currentFilestore.getValue());
            }
            if (resultMap.get("max") == null || resultMap.get("max") == 0 || resultMap.get("max") < currentFilestore.getValue()) {
                resultMap.put("max", currentFilestore.getValue());
            }
            resultMap.put("avg", sum / filestoreMap.size());
            resultMap.put("sum", sum);
        }

        resultMap.put("total", filestoreMap.size());

        return resultMap;
    }

    @Override
    public void closeAllDBConnections() {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        for (Connection currentConnection : this.nececarryConnections) {
            try {
                if (!currentConnection.isClosed())
                    dbService.backReadOnly(currentConnection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        this.nececarryConnections = new ArrayList<>();
    }

    //--------------------Private helper functions--------------------

    /**
     * Compose the where clause for the given contextId/userId map. Each contexdID/userID entry forms a query and
     * gets concatenated by an OR. Everything is wrapped in parentheses and gets a WHERE prefix.
     * 
     * Example:
     * WHERE ((cid="1" AND @param userIdColumn in (1,2,3)) OR (cid="2" AND @param userIdColumn in (5,7,9)))
     * 
     * @param usersInContext, map with context ids and users in a list, that belong to that context
     * @param userIdColumn, the colums that contains the userId
     * @return a where query with all contextIds/userIds like in the example
     */
    private String buildWhereClause(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext, String userIdColumn) {
        String whereQuery = " WHERE (";
        boolean isFirst = true;
        for (Entry<Integer, ArrayList<Integer>> currentContext : usersInContext.entrySet()) {
            String contextUserQuery = buildWhereQueryCtxsUsrs(currentContext.getKey(), currentContext.getValue(), userIdColumn);
            if (isFirst) {
                whereQuery += contextUserQuery;
                isFirst = false;
            } else
                whereQuery += " OR " + contextUserQuery;
        }
        whereQuery += ")";
        return whereQuery;
    }

    /**
     * Combine the given contextId and the given userIds {@link ArrayList} to a part of a where query.
     * 
     * Example:
     * (cid="1" AND @param userIdColumn in (1,2,3))
     * 
     * @param contextId, the contextId
     * @param userIds, all user Ids in the context
     * @param userIdColumn, the column that contains the userId
     * @return
     */
    private static String buildWhereQueryCtxsUsrs(Integer contextId, ArrayList<Integer> userIds, String userIdColumn) {
        String resultQuery = "(cid=" + contextId + " AND " + userIdColumn + " in (";
        boolean isFirst = true;
        for (Integer id : userIds) {
            if (isFirst) {
                resultQuery += id;
                isFirst = false;
            } else
                resultQuery += "," + id;
        }
        resultQuery += "))";
        return resultQuery;
    }

    /**
     * Load all necessary Connections for all schemas, that contain the given contextIds. The connections are
     * saved locally.
     * 
     * @param contextUserMap, map with context ids and users in a list, that belong to that context
     * @throws SQLException
     * @throws OXException
     */

    //TODO QS-VS Need optimization?
    private void loadAllNecessaryDBConnections(LinkedHashMap<Integer, ArrayList<Integer>> contextUserMap) throws SQLException, OXException {

        Set<Integer> alreadyProcessed = new HashSet<Integer>();

        for (Entry<Integer, ArrayList<Integer>> current : contextUserMap.entrySet()) {

            if (alreadyProcessed.contains(current.getKey()))
                continue;

            if (this.dbService == null) {
                this.dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
            }

            Connection currentConnection = this.dbService.getReadOnly(current.getKey());
            this.nececarryConnections.add(currentConnection);
            for (int iContextId : dbService.getContextsInSameSchema(current.getKey())) {
                alreadyProcessed.add(Integer.valueOf(iContextId));
            }
        }
    }

    /**
     * Calculate min,max values and add all other values with each other, as long as the key is not "avg".
     * 
     * @param oldValues, map with values calculated so far
     * @param newValues, new values to manipulate old ones
     */
    private void calculateMinMaxAdds(LinkedHashMap<String, Integer> oldValues, LinkedHashMap<String, Integer> newValues) {
        for (Entry<String, Integer> newValue : newValues.entrySet()) {
            if (oldValues.get(newValue.getKey()) == null) {
                oldValues.put(newValue.getKey(), newValue.getValue());
                continue;
            }
            if (newValue.getKey().equals("min") && (newValue.getValue() != 0 && newValue.getValue() < oldValues.get("min"))) {
                oldValues.put("min", newValue.getValue());
                continue;
            }
            if (newValue.getKey().equals("max") && (oldValues.get("max") < newValue.getValue())) {
                oldValues.put("max", newValue.getValue());
                continue;
            }
            if (!newValue.getKey().equals("avg")) {
                oldValues.put(newValue.getKey(), oldValues.get(newValue.getKey()) + newValue.getValue());
                continue;
            }

        }
    }

    /**
     * Perform the given query for all schemas that are relevant, determined from the given contextId/userId map.
     * 
     * @param usersInContext, map with context ids and users in a list, that belong to that context
     * @param query, the query that should be performed
     * @param counter, the counter for calculating "avg"-key
     * @param deleteCounter, delete counter after calculation of "avg"
     * @param hasTimerange, has a timerange
     * @param start, start of timerange
     * @param end, end of timerange
     * @return A map with the following information:
     *         key | value
     *         -----------
     *         "min" | calculated min
     *         "max" | calculated min
     *         "avg" | calculated min
     *         "total" | calculated min
     * @param counter | calculated counter, if not set to be deleted
     * @throws SQLException
     * @throws OXException
     */
    private LinkedHashMap<String, Integer> getDataFromDB(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext, String query, String counter, boolean deleteCounter, boolean hasTimerange, Date start, Date end) throws SQLException, OXException {
        LinkedHashMap<String, Integer> resultMap = new LinkedHashMap<>();
        // DB Connection
        if (this.nececarryConnections.size() == 0)
            this.loadAllNecessaryDBConnections(usersInContext);

        PreparedStatement stmt = null;
        ResultSet sqlResult = null;
        for (Connection currentConnection : this.nececarryConnections) {
            try {
                LinkedHashMap<String, Integer> queryMap = new LinkedHashMap<>();
                stmt = currentConnection.prepareStatement(query);
                if (hasTimerange) {
                    stmt.setLong(1, start.getTime());
                    stmt.setLong(2, end.getTime());
                }
                sqlResult = stmt.executeQuery();
                while (sqlResult.next()) {
                    queryMap.put("min", sqlResult.getInt(1));
                    queryMap.put("max", sqlResult.getInt(2));
                    queryMap.put("avg", sqlResult.getInt(3));
                    queryMap.put("total", sqlResult.getInt(4));
                    queryMap.put(counter, sqlResult.getInt(5));
                }
                calculateMinMaxAdds(resultMap, queryMap);
            } catch (final SQLException e) {
                throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(sqlResult, stmt);
            }
        }
        if (resultMap.get(counter) != 0)
            resultMap.put("avg", resultMap.get("total") / resultMap.get(counter));
        if (deleteCounter)
            resultMap.remove(counter);
        return resultMap;
    }
}
