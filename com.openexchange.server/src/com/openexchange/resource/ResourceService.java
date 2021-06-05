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

package com.openexchange.resource;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link ResourceService} - This service defines the API to the resource component.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface ResourceService {

    /**
     * Gets the resource identified by specified ID located in given context.
     *
     * @param resourceId The unique identifier of the resource to return.
     * @param context The context.
     * @return The data object of the resource.
     * @throws OXException If the resource can't be found or an exception appears while reading it.
     */
    public Resource getResource(int resourceId, Context context) throws OXException;

    /**
     * Searches all resources which identifier matches the given pattern.
     *
     * @param pattern The identifier of all returned resources will match this pattern.
     * @param context The context.
     * @return a string array with the resource identifiers. If no identifiers match, an empty array will be returned.
     * @throws OXException If an exception occurs while reading from the underlying persistent storage.
     */
    public Resource[] searchResources(String pattern, Context context) throws OXException;

    /**
     * Searches all resources which email address matches the given pattern.
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
     * Creates a resource.
     *
     * @param user The user in whose name the insertion takes place
     * @param ctx The context.
     * @param resource The resource to create.
     * @throws OXException If resource insertion fails
     */
    public void create(User user, Context ctx, Resource resource) throws OXException;

    /**
     * Updates a resource.
     *
     * @param user The user in whose name the update takes place
     * @param ctx The context.
     * @param resource The resource to update.
     * @param clientLastModified The client last-modified timestamp; may be <code>null</code> to omit timestamp comparison
     * @throws OXException If resource update fails
     */
    public void update(User user, Context ctx, Resource resource, Date clientLastModified) throws OXException;

    /**
     * Deletes a resource.
     *
     * @param user The user in whose name the deletion takes place
     * @param ctx The context.
     * @param resource The resource to delete.
     * @param clientLastModified The client last-modified timestamp; may be <code>null</code> to omit timestamp comparison
     * @throws OXException If resource deletion fails
     */
    public void delete(User user, Context ctx, Resource resource, Date clientLastModified) throws OXException;

    /**
     * Searches all resources which identifier matches the given pattern and sorts the results according to their use count.
     *
     * @param session The user session
     * @param pattern The identifier of all returned resources will match this pattern.
     * @return An array of found resources
     * @throws OXException If an exception occurs while reading from the underlying persistent storage.
     */
    public Resource[] searchResources(Session session, String pattern) throws OXException;

    /**
     * Searches all resources which mail address matches the given pattern and sorts the results according to their use count.
     *
     * @param session The user session
     * @param pattern The identifier of all returned resources will match this pattern.
     * @return An array of found resources
     * @throws OXException If an exception occurs while reading from the underlying persistent storage.
     */
    public Resource[] searchResourcesByMail(Session session, String pattern) throws OXException;
}
