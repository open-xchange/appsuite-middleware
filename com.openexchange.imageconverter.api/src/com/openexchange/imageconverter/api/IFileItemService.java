package com.openexchange.imageconverter.api;

import java.io.Closeable;
import java.util.Properties;
import java.util.function.Consumer;
import com.openexchange.annotation.Nullable;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link IFileItemService}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
/**
 * {@link IFileItemService}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
@SingletonService
public interface IFileItemService {

    /**
     * {@link DatabaseType}
     *
     * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
     * @since v7.10.0
     */
    public enum DatabaseType {
        /**
         * A database, that supports only standard SQL queries.
         */
        STANDARD_SQL,

        /**
         * A database, that supports standard SQL queries as well as MySQL specific queries.
         */
        MYSQL
    }

    /**
     * Retrieving the type of database, internally used to store and retrieve file metadata.
     *
     * @return The {@link DatabaseType}.
     */
    public DatabaseType getDatabaseType();

    /**
     * Registering a group and potential custom keys for this specific group,
     * that can be used as user defined properties of a FileItem for the group.
     * The registering of groups and keys needs to be done prior to the usage of
     * that group and key(s) when accessing the {@link IFileItemReadAccess#getKeyValue(String)}
     *
     * @param groupId The groupId to register the key for
     * @param keyNames The key(s) to be used as user defined properties
     * @throws FileItemException
     */
    public void registerGroup(final String groupId, final String... customKeyN) throws FileItemException;

    /**
     * Getting the array of custom keys, registered for a specific group.
     *
     * @param groupId The groupId for which all custom keys are to be retrieved
     * @return The array of custom keys for the given group
     * @throws FileItemException
     */
    public String[] getCustomKeys(final String groupId) throws FileItemException;

    /**
     * Querying, if a custom key is already registered for the group and thus can be used
     *
     * @param groupId The groupId to query for the given custom key
     * @param customKey The custom key to query the group for.
     * @return <code>true</code>, if the given key has been registered for the group,
     *  <code>false</code> otherwise
     */
    public boolean hasCustomKey(final String groupId, final String customKey) throws FileItemException;

    // -------------------------------------------------------------------------

    /**
     * Querying, if a group is contained in the collection.
     *
     * @param groupId
     * @return true, if the collections contains the group with the given id
     * @throws FileItemException
     */
    public boolean containsGroup(final String groupId) throws FileItemException;

    /**
     * Querying, if a subgroup is contained in the collection.
     *
     * @param groupId
     * @param subGroupId
     * @return true, if the collections contains the group-subgroup with the given ids
     * @throws FileItemException
     */
    public boolean containsSubGroup(final String groupId, final String subGroupId) throws FileItemException;

    /**
     * Querying, if a file is contained in the collection.
     *
     * @param groupId
     * @param subGroupId
     * @return true, if the collections contains the group-subgroup with the given ids
     * @throws FileItemException
     */
    public boolean contains(final String groupId, final String subGroupId, final String fileId) throws FileItemException;

    // -------------------------------------------------------------------------

    /**
     * Getting the {@link IFileItem} interface for the given file item.
     * If no such item exists, <code>null</code> is returned.
     *
     * @return The {@link IFileItem} interface or <code>null</code>.
     * @throws FileItemException
     */
    public IFileItem get(final String groupId, final String subGroupId, final String fileId) throws FileItemException;

    /**
     * Getting all {@link IFileItem} interfaces of the given group-subgroup as an array.
     *
     * @param groupId
     * @param subGroupId
     * @return The array of {@link IFileItem} interfaces
     * @throws FileItemException
     */
    public IFileItem[] get(final String groupId, final String subGroupId) throws FileItemException;

    /**
     * Getting all {@link IFileItem} interfaces of the given group,
     * that match the given search properties, as an array.
     *
     * @param groupId
     * @return The array of subgroup ids
     * @throws FileItemException
     */
    public IFileItem[] get(final String groupId, final Properties properties) throws FileItemException;

    /**
     * Getting all {@link IFileItem} interfaces that are selected
     * by the customQuery SQL string.
     * The query has to be created with returning all following columns
     * of the appropriate FileItem database table(s) in the correct order:</br>
     *  1. FileContent.FileStoreNumber</br>
     *  2. FileContent.FileStoreId</br>
     *  3. FileContent.SubGroupId</br>
     *  4. FileContent.FileId
     *
     * @param groupId The groupId for which the query is performed
     * @param customQuery The SQL query string string
     * @param returnValues The optional return values of the query.
     *  The number of return values depends on the provided, optional
     *  number of return values given. The first 5 return values are always:</br>
     *    1. fileStoreNumber </br>
     *    2. fileStoreId</br>
     *    3. groupId</br>
     *    4. subGroupId</br>
     *    5. fileId</br>
     *    </br>
     *  Further return values are possible and depend on the given SQL query
     * @return The array of {@link IFileItem} interfaces queried.
     * @throws FileItemException
     */
    public IFileItem[] getByCustomQuery(final String groupId, final String customQuery, Object... returnValues) throws FileItemException;

    /**
     * Getting the number of distinct subgroup ids.
     *
     * @return The number of distinct subgroup ids.
     * @throws FileItemException
     */
    public long getSubGroupCount(final String groupId) throws FileItemException;

    /**
     * Getting all subgroup ids of the given group
     *
     * @param groupId
     * @return The array of subgroup ids
     * @throws FileItemException
     */
    public String[] getSubGroups(final String groupId) throws FileItemException;

    /**
     * Getting all subgroup ids, satisfying the optionally given WHERE and/or LIMIT clauses
     * The clauses are to be given as SQL expression without (!) the keywords WHERE and LIMIT.
     *
     * @param groupId
     * @return The array of subgroup ids
     */
     public String[] getSubGroupsBy(final String groupId,
         @Nullable final String whereClause,
         @Nullable final String limitClause) throws FileItemException;

    /**
     * Getting all subgroup ids, satisfying the optionally given WHERE and/or LIMIT clauses
     * The clauses are to be given as SQL expression without (!) the keywords WHERE and LIMIT.
     *
     * @param groupId The GroupId for which the query is to be performed
     * @param whereClause The SQL 'WHERE' clause without the 'WHERE keyword. This parameter can be {@code null}.
     * @param whereClause The SQL 'LIMIT' clause without the 'LIMIT' keyword. This parameter can be {@code null}.
     * @param subGroupConsumer The {@link Consumer}, receiving each single {@link ISubGroup} instance interface.
     * @return The number of found SubGroup items.
     * @throws FileItemException
     */
    public long getSubGroupsBy(final String groupId,
        @Nullable final String whereClause,
        @Nullable final String limitClause,
        final Consumer<ISubGroup> subGroupConsumer) throws FileItemException;

    /**
     * Getting the number of distinct group ids.
     *
     * @return The number of distinct group ids.
     * @throws FileItemException
     */
    public long getGroupCount() throws FileItemException;

    /**
     * Getting all group ids of the collection
     *
     * @return The array of group ids
     * @throws FileItemException
     */
    public String[] getGroups() throws FileItemException;

    /**
     * Getting the summed up length of all items within the given group
     *
     * @return The total length
     * @throws FileItemException
     */
    public long getGroupLength(final String groupId) throws FileItemException;

    /**
     * Getting the summed up length of all items within the given group,
     * satisfying the given properties
     *
     * @return The total length
     * @throws FileItemException
     */
    public long getGroupLength(final String groupId, final Properties properties) throws FileItemException;

    /**
     * Getting the summed up length of all items within the given group and subgroup,
     *
     * @return The total length
     * @throws FileItemException
     */
    public long getSubGroupLength(final String groupId, final String subGroupId) throws FileItemException;

    /**
     * Getting the summed up length of all items within the given group and subgroup,
     * satisfying the given properties
     *
     * @return The total length
     * @throws FileItemException
     */
    public long getSubGroupLength(final String groupId, final String subGroupId, final Properties properties) throws FileItemException;

    // -------------------------------------------------------------------------

    /**
     * removing a single file item
     *
     * @param fileItem
     * @return true, if the file item was removed; false otherwise
     * @throws FileItemException
     */
    public boolean remove(final IFileItem fileItem) throws FileItemException;

    /**
     * removing a single file item
     *
     * @param groupId
     * @param subGroupId
     * @param fileId
     * @return true, if the file item was removed; false otherwise
     * @throws FileItemException
     */
    public boolean remove(final String groupId, final String subGroupId, final String fileId) throws FileItemException;

    /**
     * removing an array of file items
     *
     * @param fileElements
     * @return The number of removed file items
     * @throws FileItemException
     */
    public int remove(final IFileItem[] fileElements) throws FileItemException;

    /**
     * removing all file item, whose properties satisfy the search properties
     *
     * @param groupId
     * @param properties
     * @return The number of removed file items
     * @throws FileItemException
     */
    public int remove(final String groupId, final Properties properties) throws FileItemException;

    /**
     * Removing all file items with the given group-subgruop
     *
     * @param groupId
     * @param subGroupId
     * @return The number of removed file items
     * @throws FileItemException
     */
    public int removeSubGroup(final String groupId, final String subGroupId) throws FileItemException;

    /**
     * Removing all file items with the given group-subgroup ids
     *
     * @param groupId
     * @param subGroupId
     * @return The number of removed file items
     * @throws FileItemException
     */
    public int removeSubGroups(final String groupId, final String[] subGroupIds) throws FileItemException;

    /**
     * Removing all file items with the given group
     *
     * @param groupId
     * @return The number of removed file items
     * @throws FileItemException
     */
    public int removeGroup(final String groupId) throws FileItemException;

    // -------------------------------------------------------------------------

    /**
     * Acquiring read access to a single file item.
     * The  file item must exist, otherwise a {@link FileItemException}
     * is thrown.</br>
     * The {@link IFileItemReadAccess} interface inherits from {@link Closeable},
     * so that care has to be taken to properly call close when finished with
     * accessing the file item in order to ensure correct behaviour and to prevent memory leaks.
     *
     * @param fileItem The identifier of the file, created via {@link IFileItemService#implCreateFileItem(String, String, String)}
     * @param accessOption The list of {@link AccessOption}s
     * @return The {@link IFileItemReadAccess} interface to access the physical file item.
     * @throws FileItemException
     */
    public IFileItemReadAccess getReadAccess(final IFileItem fileItem, final AccessOption ... accessOption) throws FileItemException;

    /**
     * Convenience method for {@link IFileItemService#getReadAccess(IFileItem, AccessOption...)}
     * to save a prior call to {@link IFileItemService#implCreateFileItem(String, String, String)}</br>
     * The {@link IFileItemReadAccess} interface inherits from {@link Closeable},
     * so that care has to be taken to properly call close when finished with
     * accessing the file item in order to ensure correct behaviour and to prevent memory leaks.
     *
     * @param groupId
     * @param subGroupId
     * @param fileId
     * @param accessOption The list of {@link AccessOption}s
     * @return The {@link IFileItemReadAccess} interface to access the physical file item.
     * @throws FileItemException
     */
    public IFileItemReadAccess getReadAccess(final String groupId, final String subGroupId, final String fileId, AccessOption... accessOptions) throws FileItemException;

    /**
     * Acquiring write access to a single file item.
     * If the  file item does not yet exist, it will be created.</br>
     * The {@link IFileItemWriteAccess} interface inherits from {@link Closeable},
     * so that care has to be taken to properly call close when finished with
     * accessing the file item in order to ensure correct behaviour and to prevent memory leaks.
     *
     * @param fileItem The identifier of the file, created via {@link IFileItemService#implCreateFileItem(String, String, String)}
     * @param The list of {@link AccessOption}s
     * @return The {@link IFileItemWriteAccess} interface to access the physical file item.
     * @throws FileItemException
     */
    public IFileItemWriteAccess getWriteAccess(final IFileItem fileItem, final AccessOption ... accessOption) throws FileItemException;

    /**
     * Convenience method for {@link IFileItemService#getWriteAccess(IFileItem, AccessOption...)}
     * to save a prior call to {@link IFileItemService#implCreateFileItem(String, String, String)}</br>
     * The {@link IFileItemWriteAccess} interface inherits from {@link Closeable},
     * so that care has to be taken to properly call close when finished with
     * accessing the file item in order to ensure correct behaviour and to prevent memory leaks.
     *
     * @param groupId
     * @param subGroupId
     * @param fileId
     * @param accessOption The list of {@link AccessOption}s
     * @return The {@link IFileItemWriteAccess} interface to access the physical file item.
     * @throws FileItemException
     */
    public IFileItemWriteAccess getWriteAccess(String groupId, String subGroupId, String fileId, AccessOption... accessOptions) throws FileItemException;
}
