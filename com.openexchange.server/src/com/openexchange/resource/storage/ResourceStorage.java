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
public abstract class ResourceStorage {

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
    protected static final String SEARCH_PATTERN_ALL = "*";

    private static volatile ResourceStorage instance;

    /**
     * Default constructor.
     */
    protected ResourceStorage() {
        super();
    }

    /**
     * Creates a new instance implementing the resources interface.
     *
     * @param context Context.
     * @return an instance implementing the resources interface.
     */
    public static ResourceStorage getInstance() {
        return instance;
    }

    public static void setInstance(final ResourceStorage resourceStorage) {
        instance = resourceStorage;
    }

    /**
     * Releases the instance implementing the resources interface
     */
    public static void releaseInstance() {
        instance = null;
    }

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
    public Resource[] getAllResources(final Context context) throws OXException {
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
    public final void insertResource(final Context ctx, final Connection con, final Resource resource) throws OXException {
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
    public void deleteResource(final Context ctx, final Connection con, final Resource resource) throws OXException {
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
