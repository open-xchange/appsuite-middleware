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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.report.InfostoreInformationService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * The {@link InfostoreInformationImpl} class is an implementations of {@link InfostoreInformationService} class.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.2
 */
public class InfostoreInformationImpl implements InfostoreInformationService {

    /**
     * Initializes a new {@link InfostoreInformationImpl}.
     */
    public InfostoreInformationImpl() {
        super();
    }

    @Override
    public Map<String, Integer> getFileSizeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
        Map<Integer, String> whereQueries = buildMultipleWhereClause(dbContextToUserBash, "created_by");
        for (Map.Entry<Integer, String> singleSchemaQuery : whereQueries.entrySet()) {
            String whereQuery = singleSchemaQuery.getValue();
            String query = "SELECT min(file_size), max(file_size), avg(file_size), sum(file_size), count(*) FROM infostore_document" + whereQuery + "AND version_number > 0;";
            whereQueries.put(singleSchemaQuery.getKey(), query);
        }
        return this.getDataFromDB(whereQueries, "count", true, false, null, null);
    }

    @Override
    public Map<String, Integer> getFileCountMimetypeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        for (Map.Entry<PoolAndSchema, Map<Integer, List<Integer>>> cidToUsersEntry : dbContextToUserBash.entrySet()) {
            Map<Integer, List<Integer>> usersInContext = cidToUsersEntry.getValue();
            int representativeContextId = usersInContext.keySet().iterator().next().intValue();
            Connection connection = dbService.getReadOnly(representativeContextId);
            try {
                PreparedStatement stmt = null;
                ResultSet sqlResult = null;
                try {
                    String whereQuery = buildWhereClause(usersInContext, "created_by");
                    stmt = connection.prepareStatement("SELECT file_mimetype, count(file_mimetype) FROM infostore_document" + whereQuery + " AND version_number > 0 GROUP BY file_mimetype;");
                    sqlResult = stmt.executeQuery();
                    Map<String, Integer> queryMap = new LinkedHashMap<>();
                    while (sqlResult.next()) {
                        queryMap.put(sqlResult.getString(1), sqlResult.getInt(2));
                    }
                    calculateMinMaxAdds(resultMap, queryMap);

                } catch (final SQLException e) {
                    throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    Databases.closeSQLStuff(sqlResult, stmt);
                }
            } finally {
                dbService.backReadOnly(representativeContextId, connection);
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Integer> getStorageUseMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
        Map<Integer, String> whereQueries = buildMultipleWhereClause(dbContextToUserBash, "created_by");
        for (Map.Entry<Integer, String> singleSchemaQuery : whereQueries.entrySet()) {
            String whereQuery = singleSchemaQuery.getValue();
            String query = "SELECT min(roundup.sum), max(roundup.sum), avg(roundup.sum), sum(roundup.sum), count(*) FROM " + "(SELECT cid, created_by, sum(file_size) AS sum FROM infostore_document" + whereQuery + "GROUP BY cid, created_by) AS roundup;";
            whereQueries.put(singleSchemaQuery.getKey(), query);
        }
        return this.getDataFromDB(whereQueries, "count", true, false, null, null);
    }

    @Override
    public Map<String, Integer> getFileCountMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
        Map<Integer, String> whereQueries = buildMultipleWhereClause(dbContextToUserBash, "created_by");
        for (Map.Entry<Integer, String> singleSchemaQuery : whereQueries.entrySet()) {
            String whereQuery = singleSchemaQuery.getValue();
            String query = "SELECT min(roundup.count), max(roundup.count), avg(roundup.count), sum(roundup.count), count(DISTINCT roundup.created_by) FROM " + "(SELECT cid, created_by, count(version_number) AS count FROM infostore_document " + whereQuery + " AND version_number > 0 GROUP BY cid, created_by) AS roundup;";
            whereQueries.put(singleSchemaQuery.getKey(), query);
        }
        return this.getDataFromDB(whereQueries, "users", false, false, null, null);
    }

    @Override
    public Map<String, Integer> getFileCountInTimeframeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash, Date start, Date end) throws SQLException, OXException {
        Map<Integer, String> whereQueries = buildMultipleWhereClause(dbContextToUserBash, "created_by");
        for (Map.Entry<Integer, String> singleSchemaQuery : whereQueries.entrySet()) {
            String whereQuery = singleSchemaQuery.getValue();
            String query = "SELECT min(roundup.count), max(roundup.count), avg(roundup.count), sum(roundup.count), count(*) FROM " + "(SELECT cid, created_by, count(version_number) AS count FROM infostore_document " + whereQuery + " AND creating_date > ? AND creating_date < ? AND version_number > 0 " + "GROUP BY cid, created_by) AS roundup;";
            whereQueries.put(singleSchemaQuery.getKey(), query);
        }
        return this.getDataFromDB(whereQueries, "count", true, true, start, end);
    }

    @Override
    public Map<String, Integer> getExternalStorageMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
        Map<Integer, String> whereQueries = buildMultipleWhereClause(dbContextToUserBash, "user");
        for (Map.Entry<Integer, String> singleSchemaQuery : whereQueries.entrySet()) {
            String whereQuery = singleSchemaQuery.getValue();
            String query = "SELECT min(roundup.sum), max(roundup.sum), avg(roundup.sum), sum(roundup.sum), sum(roundup.users) FROM " + "(SELECT cid, user, count(id) AS sum, count(DISTINCT user) AS users FROM oauthAccounts" + whereQuery + " GROUP BY cid, user) AS roundup;";
            whereQueries.put(singleSchemaQuery.getKey(), query);
        }
        return this.getDataFromDB(whereQueries, "users", false, false, null, null);
    }

    @Override
    public Map<String, Integer> getFileCountNoVersions(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException {
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        for (Map.Entry<PoolAndSchema, Map<Integer, List<Integer>>> cidToUsersEntry : dbContextToUserBash.entrySet()) {
            Map<Integer, List<Integer>> usersInContext = cidToUsersEntry.getValue();
            int representativeContextId = usersInContext.keySet().iterator().next().intValue();
            Connection connection = dbService.getReadOnly(representativeContextId);
            try {
                PreparedStatement stmt = null;
                ResultSet sqlResult = null;
                try {
                    String whereQuery = buildWhereClause(usersInContext, "created_by");
                    stmt = connection.prepareStatement("SELECT count(version_number) FROM infostore_document" + whereQuery + " AND version_number = 0;");
                    sqlResult = stmt.executeQuery();
                    Map<String, Integer> queryMap = new LinkedHashMap<>();
                    while (sqlResult.next()) {
                        queryMap.put("total", sqlResult.getInt(1));
                    }
                    calculateMinMaxAdds(resultMap, queryMap);
                } catch (final SQLException e) {
                    throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    Databases.closeSQLStuff(sqlResult, stmt);
                }
            } finally {
                dbService.backReadOnly(representativeContextId, connection);
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Integer> getQuotaUsageMetrics(Map<Integer, List<Integer>> usersInContext) throws SQLException, OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        HashMap<String, Integer> resultMap = new HashMap<>();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }
        HashMap<String, Integer> filestoreMap = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> contexts : usersInContext.entrySet()) {
            for (Integer userId : contexts.getValue()) {
                QuotaFileStorage userStorage = storageService.getQuotaFileStorage(userId, contexts.getKey(), Info.drive());
                long quota = userStorage.getQuota();
                Long percent = quota < 0 ? 0 : (quota == 0 ? 100 : userStorage.getUsage() * 100 / quota);
                filestoreMap.put(userStorage.getUri().toString(), percent.intValue());
            }
        }
        Integer sum = 0;
        for (Map.Entry<String, Integer> currentFilestore : filestoreMap.entrySet()) {
            sum += currentFilestore.getValue();
            if (resultMap.get("min") == null || resultMap.get("min") == 0 || (resultMap.get("min") > currentFilestore.getValue() && currentFilestore.getValue() != 0)) {
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
    private String buildWhereClause(Map<Integer, List<Integer>> usersInContext, String userIdColumn) {
        String whereQuery = " WHERE (";
        boolean isFirst = true;
        for (Map.Entry<Integer, List<Integer>> currentContext : usersInContext.entrySet()) {
            String contextUserQuery = buildWhereQueryCtxsUsrs(currentContext.getKey(), currentContext.getValue(), userIdColumn);
            if (isFirst) {
                whereQuery += contextUserQuery;
                isFirst = false;
            } else {
                whereQuery += " OR " + contextUserQuery;
            }
        }
        whereQuery += ")";
        return whereQuery;
    }

    private Map<Integer, String> buildMultipleWhereClause(Map<PoolAndSchema, Map<Integer,List<Integer>>> dbContextToUserBash, String userIdColumn) {
        Map<Integer, String> resultMap = new LinkedHashMap<>();

        StringBuilder whereQuery = new StringBuilder(128);
        for (Map.Entry<PoolAndSchema, Map<Integer, List<Integer>>> dbToUsersEntry : dbContextToUserBash.entrySet()) {
            Map<Integer,List<Integer>> usersInContext = dbToUsersEntry.getValue();

            whereQuery.setLength(0);
            whereQuery.append(" WHERE (");

            boolean isFirst = true;
            Integer representativeContextId = null;
            for (Map.Entry<Integer, List<Integer>> currentContext : usersInContext.entrySet()) {
                Integer contextId = currentContext.getKey();
                String contextUserQuery = buildWhereQueryCtxsUsrs(contextId, currentContext.getValue(), userIdColumn);
                if (isFirst) {
                    whereQuery.append(contextUserQuery);
                    isFirst = false;
                    representativeContextId = contextId;
                } else {
                    whereQuery.append(" OR ").append(contextUserQuery);
                }
            }
            whereQuery.append(")");
            resultMap.put(representativeContextId, whereQuery.toString());
        }

        return resultMap;
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
    private static String buildWhereQueryCtxsUsrs(Integer contextId, List<Integer> userIds, String userIdColumn) {
        StringBuilder resultQuery = new StringBuilder(32).append("(cid=").append(contextId).append(" AND ").append(userIdColumn).append(" IN (");
        boolean isFirst = true;
        for (Integer id : userIds) {
            if (isFirst) {
                isFirst = false;
            } else {
                resultQuery.append(',');
            }
            resultQuery.append(id);
        }
        resultQuery.append("))");
        return resultQuery.toString();
    }

    /**
     * Calculate min,max values and add all other values with each other, as long as the key is not "avg".
     *
     * @param oldValues, map with values calculated so far
     * @param newValues, new values to manipulate old ones
     */
    private void calculateMinMaxAdds(Map<String, Integer> oldValues, Map<String, Integer> newValues) {
        for (Map.Entry<String, Integer> newValue : newValues.entrySet()) {
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
            if (!newValue.getKey().equals("avg") && !newValue.getKey().equals("min") && !newValue.getKey().equals("max")) {
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
     * <pre>
     *     key | value
     *     -----------
     *     "min" | calculated min
     *     "max" | calculated min
     *     "avg" | calculated min
     *     "total" | calculated min
     * </pre>
     * @param counter | calculated counter, if not set to be deleted
     * @throws SQLException
     * @throws OXException
     */
    private Map<String, Integer> getDataFromDB(Map<Integer, String> dbCidToQuery, String counter, boolean deleteCounter, boolean hasTimerange, Date start, Date end) throws SQLException, OXException {
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        for (Map.Entry<Integer, String> cidToQuery : dbCidToQuery.entrySet()) {
            String query = cidToQuery.getValue();
            int representativeContextId = cidToQuery.getKey().intValue();
            Connection connection = dbService.getReadOnly(representativeContextId);
            try {
                PreparedStatement stmt = null;
                ResultSet sqlResult = null;
                try {
                    stmt = connection.prepareStatement(query);
                    if (hasTimerange) {
                        stmt.setLong(1, start.getTime());
                        stmt.setLong(2, end.getTime());
                    }
                    sqlResult = stmt.executeQuery();
                    Map<String, Integer> queryMap = new LinkedHashMap<>();
                    while (sqlResult.next()) {
                        queryMap.put("min", sqlResult.getInt(1));
                        queryMap.put("max", sqlResult.getInt(2));
                        queryMap.put("avg", sqlResult.getInt(3));
                        queryMap.put("total", sqlResult.getInt(4));
                        queryMap.put(counter, sqlResult.getInt(5));
                    }
                    calculateMinMaxAdds(resultMap, queryMap);
                } finally {
                    Databases.closeSQLStuff(sqlResult, stmt);
                }
            } finally {
                dbService.backReadOnly(representativeContextId, connection);
            }
        }
        if (resultMap.get(counter) != 0) {
            resultMap.put("avg", resultMap.get("total") / resultMap.get(counter));
        }
        if (deleteCounter) {
            resultMap.remove(counter);
        }
        return resultMap;
    }
}
