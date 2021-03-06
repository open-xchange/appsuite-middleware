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
package com.openexchange.report;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.exception.OXException;

/**
 * The {@link InfostoreInformationService} class is used to load data from the database, that is needed
 * by the reports drive-metric calculation functions. This classes functions are designed to use and store
 * all needed schema connections for one usersInContext-map. Therefore after the usage of one of these functions, all connections
 * should be released by calling {@link #closeAllDBConnections}.
 * 
 * Attention, if intended to call more than one function without releasing the connections after each call,
 * the developer should use the same usersInContext map. The needed connections are only gathered once, unless they are
 * released manually.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public interface InfostoreInformationService {

    /**
     * Calculate min/max/avg/total for the given context/Id map. Iterates over all schemas relevant, determined by
     * the given contextIds. Returns a map with all calculated values regarding all schemas combined. Every
     * Version of a file is considered.
     * 
     * @param dbContextToUserBash, map with one context id as key, to identify the needed schema. 
     *      The value is a map with context ids and users in a list, that belong to that context
     * @return A map with the following information about file sizes:
     * <table>
     * <tr>
     *   <td><code>key</code></td><td><code>value</code></td>
     * </tr>
     * <tr>
     *   <td><code>"min"</code></td><td><code>lowest file size</code></td>
     * </tr>
     * <tr>
     *   <td><code>"max"</code></td><td><code>highest file size</code></td>
     * </tr>
     * <tr>
     *   <td><code>"avg"</code></td><td><code>average file size per file</code></td>
     * </tr>
     * <tr>
     *   <td><code>"total"</code></td><td><code>file size of all files combined</code></td>
     * </tr>
     * </table>
     * @throws SQLException
     * @throws OXException
     */
    public Map<String, Integer> getFileSizeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException;

    /**
     * Get all file types and their amount from all relevant schemas. Relevent schemas are determined by
     * the context ids from the given map.
     * 
     * @param dbContextToUserBash, map with one context id as key, to identify the needed schema. 
     *      The value is a map with context ids and users in a list, that belong to that context
     * @return A map with the following information about mime-types:
     * <table>
     * <tr>
     *   <td><code>key</code></td><td><code>value</code></td>
     * </tr>
     * <tr>
     *   <td><code>"MimeType"</code></td><td><code>number of files with that mimetype</code></td>
     * </tr>
     * </table>
     * @throws SQLException
     * @throws OXException
     */
    public Map<String, Integer> getFileCountMimetypeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException;

    /**
     * Calculate min/max/avg/total for the amount of storage every drive user is using. Users, that do not use drive are irrelevant
     * and have no effect. The function Iterates over all schemas relevant, determined by
     * the given contextIds.
     * 
     * Returns a map with all calculated values regarding all schemas combined.
     * 
     * @param @param dbContextToUserBash, map with one context id as key, to identify the needed schema. 
     *      The value is a map with context ids and users in a list, that belong to that context
     * @return A map with the following information about used storage:
     * <table>
     * <tr>
     *   <td><code>key</code></td><td><code>value</code></td>
     * </tr>
     * <tr>
     *   <td><code>"min"</code></td><td><code>lowest storage use in bytes</code></td>
     * </tr>
     * <tr>
     *   <td><code>"max"</code></td><td><code>highest storage use in bytes</code></td>
     * </tr>
     * <tr>
     *   <td><code>"avg"</code></td><td><code>average storage use in bytes</code></td>
     * </tr>
     * <tr>
     *   <td><code>"total"</code></td><td><code>file storage use in bytes of all files combined</code></td>
     * </tr>
     * </table>
     * @throws SQLException
     * @throws OXException
     */
    public Map<String, Integer> getStorageUseMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException;

    /**
     * Calculate min/max/avg/total for the amount of files every drive user possesses. Users, that do not use drive are irrelevant
     * and have no effect. The function Iterates over all schemas relevant, determined by
     * the given contextIds. Versions are treated as files, also.
     * 
     * Returns a map with all calculated values regarding all schemas combined.
     * 
     * @param dbContextToUserBash, map with one context id as key, to identify the needed schema. 
     *      The value is a map with context ids and users in a list, that belong to that context
     * @return A map with the following information about file count:
     * <table>
     * <tr>
     *   <td><code>key</code></td><td><code>value</code></td>
     * </tr>
     * <tr>
     *   <td><code>"min"</code></td><td><code>least files in storage</code></td>
     * </tr>
     * <tr>
     *   <td><code>"max"</code></td><td><code>most files in storage</code></td>
     * </tr>
     * <tr>
     *   <td><code>"avg"</code></td><td><code>average files in storage</code></td>
     * </tr>
     * <tr>
     *   <td><code>"total"</code></td><td><code>all user files in storage</code></td>
     * </tr>
     * <tr>
     *   <td><code>"users"</code></td><td><code>amount of users using drive</code></td>
     * </tr>
     * </table>
     * @throws SQLException
     * @throws OXException
     */
    public Map<String, Integer> getFileCountMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException;

    /**
     * Calculate min/max/avg/total for the amount of files every drive user possessed, in the given timeframe.
     * Users, that have not used drive in that timeframe are irrelevant and have no effect. The function Iterates over all schemas relevant, determined by
     * the given contextIds.
     * 
     * Returns a map with all calculated values regarding all schemas combined.
     * 
     * @param dbContextToUserBash, map with one context id as key, to identify the needed schema. 
     *      The value is a map with context ids and users in a list, that belong to that context
     * @param start, start of timeframe
     * @param end, end of timeframe
     * @return A map with the following information about file count:
     * <table>
     * <tr>
     *   <td><code>key</code></td><td><code>value</code></td>
     * </tr>
     * <tr>
     *   <td><code>"min"</code></td><td><code>least files created in storage</code></td>
     * </tr>
     * <tr>
     *   <td><code>"max"</code></td><td><code>most files created in storage</code></td>
     * </tr>
     * <tr>
     *   <td><code>"avg"</code></td><td><code>average files created in storage</code></td>
     * </tr>
     * <tr>
     *   <td><code>"total"</code></td><td><code>all user files created in storage</code></td>
     * </tr>
     * </table>
     * @throws SQLException
     * @throws OXException
     */
    public Map<String, Integer> getFileCountInTimeframeMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash, Date start, Date end) throws SQLException, OXException;

    /**
     * Calculate min/max/avg/total for the number of external storages every drive user possesses.
     * Users, that have no external storage are irrelevant and have no effect. The function Iterates over all schemas relevant, determined by
     * the given contextIds.
     * 
     * Returns a map with all calculated values regarding all schemas combined.
     * 
     * @param dbContextToUserBash, map with one context id as key, to identify the needed schema. 
     *      The value is a map with context ids and users in a list, that belong to that context
     * @return A map with the following information about external storages:
     * <table>
     * <tr>
     *   <td><code>key</code></td><td><code>value</code></td>
     * </tr>
     * <tr>
     *   <td><code>"min"</code></td><td><code>least number of external storages</code></td>
     * </tr>
     * <tr>
     *   <td><code>"max"</code></td><td><code>most number of external storages</code></td>
     * </tr>
     * <tr>
     *   <td><code>"avg"</code></td><td><code>average number of external storages</code></td>
     * </tr>
     * <tr>
     *   <td><code>"total"</code></td><td><code>total number of external storages</code></td>
     * </tr>
     * <tr>
     *   <td><code>"users"</code></td><td><code>amount of users with external storages</code></td>
     * </tr>
     * </table>
     * @throws SQLException
     * @throws OXException
     */
    public Map<String, Integer> getExternalStorageMetrics(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException;

    /**
     * Get the number of files without taking their versions into account. The function Iterates over all schemas relevant, determined by
     * the given contextIds.
     * 
     * @param dbContextToUserBash, map with one context id as key, to identify the needed schema. 
     *      The value is a map with context ids and users in a list, that belong to that context
     * @return A map with the following information about file count:
     * <table>
     * <tr>
     *   <td><code>key</code></td><td><code>value</code></td>
     * </tr>
     * <tr>
     *   <td><code>"total"</code></td><td><code>number of files</code></td>
     * </tr>
     * </table>
     * @throws SQLException
     * @throws OXException
     */
    public Map<String, Integer> getFileCountNoVersions(Map<PoolAndSchema, Map<Integer, List<Integer>>> dbContextToUserBash) throws SQLException, OXException;

    /**
     * Calculate min/max/avg/total in percent for the quota per context-/dedicated user-filestore.
     * The function Iterates over all schemas relevant, determined by the given contextIds.
     * 
     * Returns a map with all calculated values regarding all schemas combined.
     * 
     * @param usersInContext, map with context ids and users in a list, that belong to that context
     * @return A map with the following information about quota usage:
     * <table>
     * <tr>
     *   <td><code>key</code></td><td><code>value</code></td>
     * </tr>
     * <tr>
     *   <td><code>"min"</code></td><td><code>least quota usage in %</code></td>
     * </tr>
     * <tr>
     *   <td><code>"max"</code></td><td><code>most quota usage in %</code></td>
     * </tr>
     * <tr>
     *   <td><code>"avg"</code></td><td><code>average quota usage in %</code></td>
     * </tr>
     * <tr>
     *   <td><code>"sum"</code></td><td><code>all used quota cumulated</code></td>
     * </tr>
     * <tr>
     *   <td><code>"total"</code></td><td><code>amount of filestores</code></td>
     * </tr>
     * </table>
     * @throws SQLException
     * @throws OXException
     */
    public Map<String, Integer> getQuotaUsageMetrics(Map<Integer, List<Integer>> usersInContext) throws SQLException, OXException;

}
