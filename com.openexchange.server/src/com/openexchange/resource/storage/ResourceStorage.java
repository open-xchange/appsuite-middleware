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

package com.openexchange.resource.storage;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceGroup;

/**
 * {@link ResourceStorage} - This class provides abstract methods to read resources and their groups from the directory service. This class
 * is implemented according the DAO design pattern.
 *
 * @author <a href="mailto:marcus@open-xchange.de">Marcus Klein </a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ResourceStorage {

    public static enum StorageType {
        /**
         * Storage type for currently active resources.
         */
        ACTIVE,
        /**
         * Storage type for deleted resources. This must be filled with deleted resources to inform synchronizing clients about not more
         * existing resources.
         */
        DELETED
    }

    /**
     * The search pattern to return all resources: "*"
     */
    static final String SEARCH_PATTERN_ALL = "*";

    /**
     * Reads the data of resource group from the underlying persistent data storage.
     *
     * @param groupId Identifier of the resource group.
     * @param context The context.
     * @return a resource group object.
     * @throws OXException if an error occurs while reading from the persistent storage or the resource group doesn't exist.
     */
    public abstract ResourceGroup getGroup(int groupId, Context context) throws OXException;

    public abstract ResourceGroup[] getGroups(Context context) throws OXException;

    /**
     * Reads a resource from the underlying persistent storage and returns it in a data object.
     *
     * @param resourceId The unique identifier of the resource to return.
     * @param context The context.
     * @return The data object of the resource.
     * @throws OXException If the resource can't be found or an exception appears while reading it.
     */
    public abstract Resource getResource(int resourceId, Context context) throws OXException;

    /**
     * Searches all groups whose identifier matches the given pattern.
     *
     * @param pattern The identifier of all returned groups will match this pattern.
     * @param context The context.
     * @return a string array with resource group identifiers. If no identifiers match an empty array will be returned.
     * @throws OXException If an exception occurs while reading from the underlying persistent storage.
     */
    public abstract ResourceGroup[] searchGroups(String pattern, Context context) throws OXException;

    /**
     * Gets all resources located in specified context
     *
     * @param context The context
     * @return All resources located in specified context
     * @throws OXException If an exception occurs while reading from the underlying persistent storage.
     */
    public default Resource[] getAllResources(final Context context) throws OXException {
        return searchResources(SEARCH_PATTERN_ALL, context);
    }

    /**
     * Searches all resources that identifier matches the given pattern.
     *
     * @param pattern The identifier of all returned resources will match this pattern.
     * @param context The context.
     * @return a string array with the resource identifiers. If no identifiers match, an empty array will be returned.
     * @throws OXException If an exception occurs while reading from the underlying persistent storage.
     */
    public abstract Resource[] searchResources(String pattern, Context context) throws OXException;

    /**
     * Searches all resources whose email address matches the given pattern.
     *
     * @param pattern The email address pattern to search for
     * @param context The context
     * @return An array of {@link Resource resources} whose email address matches the given pattern.
     * @throws OXException If searching for resources fails
     */
    public abstract Resource[] searchResourcesByMail(String pattern, Context context) throws OXException;

    /**
     * This method returns resources that have been modified since the given timestamp.
     *
     * @param modifiedSince timestamp after that the resources have been modified.
     * @param context The context.
     * @return an array of resources.
     * @throws OXException If an error occurs.
     */
    public abstract Resource[] listModified(Date modifiedSince, Context context) throws OXException;


    /**
     * This method returns resources that have been deleted since the given timestamp.
     *
     * @param modifiedSince timestamp after that the resources have been modified.
     * @param context The context.
     * @return an array of resources.
     * @throws OXException If an error occurs.
     */
    public abstract Resource[] listDeleted(Date modifiedSince, Context context) throws OXException;

    /**
     * This method inserts a resource into the storage.
     *
     * @param ctx The context.
     * @param con A writable database connection.
     * @param resource The resource to insert.
     * @throws OXException If resource insertion fails.
     */
    public default void insertResource(final Context ctx, final Connection con, final Resource resource) throws OXException {
        insertResource(ctx, con, resource, StorageType.ACTIVE);
    }

    /**
     * This method inserts a resource into the storage.
     *
     * @param ctx The context.
     * @param con A writable database connection.
     * @param resource The resource to insert.
     * @param type Defines if group is inserted {@link StorageType#ACTIVE ACTIVE} or {@link StorageType#DELETED DELETED}.
     * @throws OXException If resource insertion fails.
     */
    public abstract void insertResource(Context ctx, Connection con, Resource resource, StorageType type) throws OXException;

    /**
     * This method updates the resource in storage referenced by {@link Resource#getIdentifier() resource identifier}.
     *
     * @param ctx The context.
     * @param con A writable database connection.
     * @param resource The resource to update.
     * @throws OXException If resource update fails.
     */
    public abstract void updateResource(Context ctx, Connection con, Resource resource) throws OXException;

    /**
     * A convenience method that invokes {@link #deleteResourceById(Context, Connection, int)} with the latter parameter filled with
     * {@link Resource#getIdentifier()}
     *
     * @param ctx The context
     * @param con A writable database connection.
     * @param resource The resource to delete
     * @throws OXException If resource deletion fails.
     */
    public default void deleteResource(final Context ctx, final Connection con, final Resource resource) throws OXException {
        deleteResourceById(ctx, con, resource.getIdentifier());
    }

    /**
     * This method deletes the resource in storage referenced by specified <code>resourceId</code>.
     *
     * @param ctx The context
     * @param con A writable database connection.
     * @param resourceId The ID of the resource to delete
     * @throws OXException If resource deletion fails.
     */
    public abstract void deleteResourceById(Context ctx, Connection con, int resourceId) throws OXException;
}
