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
package com.openexchange.report;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
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
     * @param usersInContext, map with context ids and users in a list, that belong to that context
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
    public Map<String, Integer> getFileSizeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException;

    /**
     * Get all file types and their amount from all relevant schemas. Relevent schemas are determined by
     * the context ids from the given map.
     * 
     * @param usersInContext, map with context ids and users in a list, that belong to that context
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
    public Map<String, Integer> getFileCountMimetypeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException;

    /**
     * Calculate min/max/avg/total for the amount of storage every drive user is using. Users, that do not use drive are irrelevant
     * and have no effect. The function Iterates over all schemas relevant, determined by
     * the given contextIds.
     * 
     * Returns a map with all calculated values regarding all schemas combined.
     * 
     * @param usersInContext, map with context ids and users in a list, that belong to that context
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
    public Map<String, Integer> getStorageUseMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException;

    /**
     * Calculate min/max/avg/total for the amount of files every drive user possesses. Users, that do not use drive are irrelevant
     * and have no effect. The function Iterates over all schemas relevant, determined by
     * the given contextIds. Versions are treated as files, also.
     * 
     * Returns a map with all calculated values regarding all schemas combined.
     * 
     * @param usersInContext, map with context ids and users in a list, that belong to that context
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
    public Map<String, Integer> getFileCountMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException;

    /**
     * Calculate min/max/avg/total for the amount of files every drive user possessed, in the given timeframe.
     * Users, that have not used drive in that timeframe are irrelevant and have no effect. The function Iterates over all schemas relevant, determined by
     * the given contextIds.
     * 
     * Returns a map with all calculated values regarding all schemas combined.
     * 
     * @param usersInContext, map with context ids and users in a list, that belong to that context
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
    public Map<String, Integer> getFileCountInTimeframeMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext, Date start, Date end) throws SQLException, OXException;

    /**
     * Calculate min/max/avg/total for the number of external storages every drive user possesses.
     * Users, that have no external storage are irrelevant and have no effect. The function Iterates over all schemas relevant, determined by
     * the given contextIds.
     * 
     * Returns a map with all calculated values regarding all schemas combined.
     * 
     * @param usersInContext, map with context ids and users in a list, that belong to that context
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
    public Map<String, Integer> getExternalStorageMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException;

    /**
     * Get the number of files without taking their versions into account. The function Iterates over all schemas relevant, determined by
     * the given contextIds.
     * 
     * @param usersInContext, map with context ids and users in a list, that belong to that context
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
    public Map<String, Integer> getFileCountNoVersions(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException;

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
    public Map<String, Integer> getQuotaUsageMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException;

    /**
     * Return all used connections to the pool. Should be called after the last metric has been calculated.
     */
    public void closeAllDBConnections();

}
