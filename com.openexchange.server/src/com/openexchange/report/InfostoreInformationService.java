
package com.openexchange.report;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * 
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
     *         key | value
     *         ----------------
     *         "min" | lowest file size
     *         "max" | highest file size
     *         "avg" | average file size per file
     *         "total" | file size of all files combined
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
     *         key | value
     *         -----------
     *         "MimeType" | number of files with that mimetype
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
     *         key | value
     *         -----------
     *         "min" | lowest storage use in bytes
     *         "max" | highest storage use in bytes
     *         "avg" | average storage use in bytes
     *         "total" | file storage use in bytes of all files combined
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
     *         key | value
     *         -----------
     *         "min" | least files in storage
     *         "max" | most files in storage
     *         "avg" | average files in storage
     *         "total" | all user files in storage
     *         "users" | amount of users using drive
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
     *         key | value
     *         -----------
     *         "min" | least files created in storage
     *         "max" | most files created in storage
     *         "avg" | average files created in storage
     *         "total" | all user files created in storage
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
     *         key | value
     *         -----------
     *         "min" | least number of external storages
     *         "max" | most number of external storages
     *         "avg" | average number of external storages
     *         "total" | total number of external storages
     *         "users" | amount of users with external storages
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
     *         key | value
     *         -----------
     *         "total" | number of files
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
     *         key | value
     *         -----------
     *         "min" | least quota usage in %
     *         "max" | most quota usage in %
     *         "avg" | average quota usage in %
     *         "sum" | all used quota cumulated.
     *         "total" | amount of filestores
     * @throws SQLException
     * @throws OXException
     */
    public Map<String, Integer> getQuotaUsageMetrics(LinkedHashMap<Integer, ArrayList<Integer>> usersInContext) throws SQLException, OXException;

    /**
     * Return all used connections to the pool. Should be called after the last metric has been calculated.
     */
    public void closeAllDBConnections();

    public Long getQuotaForUser(Integer contextId, Integer userId) throws SQLException, OXException;

}
