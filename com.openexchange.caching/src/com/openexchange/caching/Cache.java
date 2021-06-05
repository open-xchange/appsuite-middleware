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

package com.openexchange.caching;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link Cache} - This class provides an interface for all types of access to the cache.
 * <p>
 * An instance of this class is bound to a specific cache region.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Cache {

    /**
     * Indicates if this cache is distributed.
     * <ul>
     * <li>Data in the cluster is almost evenly distributed (partitioned) across all nodes. So each node carries ~ (1/n
     * <code class="literal">*</code> total-data) + backups , n being the number of nodes in the cluster.<br>&nbsp;</li>
     * <li>If a member goes down, its backup replica that also holds the same data, will dynamically redistribute the data including the
     * ownership and locks on them to remaining live nodes. As a result, no data will get lost.<br>&nbsp;</li>
     * <li>When a new node joins the cluster, new node takes ownership(responsibility) and load of -some- of the entire data in the cluster.
     * Eventually the new node will carry almost (1/n <code class="literal">*</code> total-data) + backups and becomes the new partition
     * reducing the load on others.<br>&nbsp;</li>
     * <li>There is no single cluster master or something that can cause single point of failure. Every node in the cluster has equal rights
     * and responsibilities. No-one is superior. And no dependency on external 'server' or 'master' kind of concept.<br>&nbsp;</li>
     * </ul>
     *
     * @return <code>true</code> if this cache has a distributed nature; otherwise <code>false</code> (a replicated nature)
     */
    boolean isDistributed();

    /**
     * Indicates if this cache is replicated.
     * <p>
     * Data is kept redundantly on every linked node.
     *
     * @return <code>true</code> if this cache has a replicated nature; otherwise <code>false</code> (a distributed nature)
     */
    boolean isReplicated();

    /**
     * Gets the currently cached elements.
     *
     * @return The values
     */
    Collection<Serializable> values();

    /**
     * Checks if this cache is local-only.
     *
     * @return <code>true</code> if local-only; otherwise <code>false</code>
     */
    boolean isLocal();

    /**
     * Removes all of the elements from cache.
     *
     * @throws OXException If cache cannot be cleared
     */
    void clear() throws OXException;

    /**
     * Disposes this cache. Flushes objects to and closes auxiliary caches. This is a shutdown command.
     * <p>
     * To simply remove all elements from the cache use {@link #clear()}
     */
    void dispose();

    /**
     * Retrieves the object from the cache which is bound to specified key.
     *
     * @param key The key
     * @return The cached object if found or <code>null</code>
     * @throws IllegalArgumentException If given key is <code>null</code>
     */
    Object get(Serializable key);

    /**
     * This method returns the cache element wrapper which provides access to element info and other attributes.
     * <p>
     * This returns a reference to the wrapper. Any modifications will be reflected in the cache. No defensive copy is made.
     * <p>
     * This method is most useful if you want to determine things such as how long the element has been in the cache.
     * <p>
     * The last access time in the element attributes should be current.
     *
     * @param key The key
     * @return A reference to cache element wrapper if found or <code>null</code>
     * @throws IllegalArgumentException If given key is <code>null</code>
     */
    CacheElement getCacheElement(Serializable key);

    /**
     * Retrieves a <b>copy</b> of the default element attributes used by this cache. This does not provide a reference to the element
     * attributes.
     * <p>
     * Each time an element is added to the cache without element attributes, the default element attributes are cloned.
     *
     * @return The default element attributes used by this cache
     * @throws OXException If default element attributes cannot be returned
     */
    ElementAttributes getDefaultElementAttributes() throws OXException;

    /**
     * Gets an item out of the cache that is in specified group.
     *
     * @param key The key
     * @param group The group name.
     * @return The cached value, <code>null</code> if not found
     * @throws IllegalArgumentException If given key or group is <code>null</code>
     */
    Object getFromGroup(Serializable key, String group);

    /**
     * Invalidates a group: remove all the group members
     *
     * @param group The name of the group to invalidate
     * @throws IllegalArgumentException If given group is <code>null</code>
     */
    void invalidateGroup(String group);

    /**
     * Place a new object in the cache, associated with key name. If there is currently an object associated with name in the cache it is
     * replaced. Names are scoped to a cache so they must be unique within the cache they are placed. ObjectExistsException
     *
     * @param key The key
     * @param object Object to store
     * @throws OXException If put operation on cache fails
     * @throws IllegalArgumentException If given key or object is <code>null</code>
     * @deprecated Use {@link #put(Serializable, Serializable, boolean)} instead and supply the <code>invalidate</code>-flag explicitly.
     */
    @Deprecated
    void put(Serializable key, Serializable object) throws OXException;

    /**
     * Place a new object in the cache, associated with key name. If there is currently an object associated with name in the cache it is
     * replaced. Names are scoped to a cache so they must be unique within the cache they are placed.
     * <p>
     * If the put-operation is caused by an existing item being changed, set the <code>invalidate</code>-flag to <code>true</code> so that
     * auxiliary caches get informed, too. If it is caused by just populating the cache from the database or another persistent storage,
     * set it to<code>false</code>.
     *
     * @param key The key
     * @param object Object to store
     * @param invalidate <code>true</code> to trigger remote invalidation processing for the cache entry, <code>false</code>, otherwise.
     * @throws OXException If put operation on cache fails
     * @throws IllegalArgumentException If given key or object is <code>null</code>
     */
    void put(Serializable key, Serializable object, boolean invalidate) throws OXException;

    /**
     * Constructs a cache element with these attributes, and puts it into the cache.
     * <p>
     * If the key or the value is null, and InvalidArgumentException is thrown.
     *
     * @param key The key
     * @param value The object to store
     * @param attr The object's element attributes
     * @exception OXException If put operation on cache fails
     * @throws IllegalArgumentException If given key or value is <code>null</code>
     * @deprecated Use {@link #put(Serializable, Serializable, ElementAttributes, boolean)} instead and supply the <code>invalidate</code>-
     *             flag explicitly.
     */
    @Deprecated
    void put(Serializable key, Serializable value, ElementAttributes attr) throws OXException;

    /**
     * Constructs a cache element with these attributes, and puts it into the cache.
     * <p>
     * If the key or the value is null, and InvalidArgumentException is thrown.
     * <p>
     * If the put-operation is caused by an existing item being changed, set the <code>invalidate</code>-flag to <code>true</code> so that
     * auxiliary caches get informed, too. If it is caused by just populating the cache from the database or another persistent storage,
     * set it to<code>false</code>.
     *
     * @param key The key
     * @param value The object to store
     * @param attr The object's element attributes
     * @param invalidate <code>true</code> to trigger remote invalidation processing for the cache entry, <code>false</code>, otherwise.
     * @throws OXException If put operation on cache fails
     * @throws IllegalArgumentException If given key or value is <code>null</code>
     */
    void put(Serializable key, Serializable value, ElementAttributes attr, boolean invalidate) throws OXException;

    /**
     * Allows the user to put an object into a group within a particular cache. This method allows the object's attributes to be
     * individually specified.
     *
     * @param key The key
     * @param groupName The group name.
     * @param value The object to cache
     * @param attr The objects attributes.
     * @throws OXException If put operation on cache fails
     * @throws IllegalArgumentException If either one of passed arguments is <code>null</code>
     * @deprecated Use {@link #putInGroup(Serializable, String, Object, ElementAttributes, boolean)} instead and supply the
     *             <code>invalidate</code>-flag explicitly.
     */
    @Deprecated
    void putInGroup(Serializable key, String groupName, Object value, ElementAttributes attr) throws OXException;

    /**
     * Allows the user to put an object into a group within a particular cache. This method allows the object's attributes to be
     * individually specified.
     * <p>
     * If the put-operation is caused by an existing item being changed, set the <code>invalidate</code>-flag to <code>true</code> so that
     * auxiliary caches get informed, too. If it is caused by just populating the cache from the database or another persistent storage,
     * set it to<code>false</code>.
     *
     * @param key The key
     * @param groupName The group name.
     * @param value The object to cache
     * @param attr The objects attributes.
     * @param invalidate <code>true</code> to trigger remote invalidation processing for the cache entry, <code>false</code>, otherwise.
     * @throws OXException If put operation on cache fails
     * @throws IllegalArgumentException If either one of passed arguments is <code>null</code>
     */
    void putInGroup(Serializable key, String groupName, Object value, ElementAttributes attr, boolean invalidate) throws OXException;

    /**
     * Allows the user to put an object into a group within a particular cache. This method sets the object's attributes to the default for
     * the cache.
     *
     * @param key The key
     * @param groupName The group name.
     * @param value The object to cache
     * @throws OXException If put operation on cache fails
     * @throws IllegalArgumentException If either one of passed arguments is <code>null</code>
     * @deprecated Use {@link #putInGroup(Serializable, String, Serializable, boolean)} instead and supply the <code>invalidate</code>-
     *             flag explicitly.
     */
    @Deprecated
    void putInGroup(Serializable key, String groupName, Serializable value) throws OXException;

    /**
     * Allows the user to put an object into a group within a particular cache. This method sets the object's attributes to the default for
     * the cache.
     * <p>
     * If the put-operation is caused by an existing item being changed, set the <code>invalidate</code>-flag to <code>true</code> so that
     * auxiliary caches get informed, too. If it is caused by just populating the cache from the database or another persistent storage,
     * set it to<code>false</code>.
     *
     * @param key The key
     * @param groupName The group name.
     * @param value The object to cache
     * @param invalidate <code>true</code> to trigger remote invalidation processing for the cache entry, <code>false</code>, otherwise.
     * @throws OXException If put operation on cache fails
     * @throws IllegalArgumentException If either one of passed arguments is <code>null</code>
     */
    void putInGroup(Serializable key, String groupName, Serializable value, boolean invalidate) throws OXException;

    /**
     * Place a new object in the cache, associated with key. If there is currently an object associated with key in the cache an exception
     * is thrown. Keys are scoped to a cache so they must be unique within the cache they are placed.
     *
     * @param key The key
     * @param value Object to store
     * @exception OXException If the item is already in the cache.
     * @throws IllegalArgumentException If either one of passed arguments is <code>null</code>
     */
    void putSafe(Serializable key, Serializable value) throws OXException;

    /**
     * Removes the object from the cache which is bound to specified key.
     *
     * @param key The key
     * @throws OXException If remove operation on cache fails
     * @throws IllegalArgumentException If given key is <code>null</code>
     */
    void remove(Serializable key) throws OXException;

    /**
     * Removes multiple objects from the cache which are bound to the specified keys.
     *
     * @param keys The keys
     * @throws OXException If remove operation on cache fails
     */
    void remove(List<Serializable> keys) throws OXException;

    /**
     * Clears (optional operation) the cache without propagating that operation neither laterally nor remotely.
     *
     * @throws OXException If clears operation
     * @see SupportsLocalOperations SupportsLocalOperations marker interface to check if supported
     */
    void localClear() throws OXException;

    /**
     * Removes (optional operation) the object from the cache which is bound to specified key without propagating that operation neither
     * laterally nor remotely.
     *
     * @param key The key
     * @throws OXException If remove operation on cache fails
     * @throws IllegalArgumentException If given key is <code>null</code>
     * @see SupportsLocalOperations SupportsLocalOperations marker interface to check if supported
     */
    void localRemove(Serializable key) throws OXException;

    /**
     * Puts (optional operation) the object into the cache which is bound to specified key without propagating that operation neither
     * laterally nor remotely.
     *
     * @param key The key
     * @param value Object to store
     * @throws OXException If put operation on cache fails
     * @throws IllegalArgumentException If either one of passed arguments is <code>null</code>
     * @see SupportsLocalOperations SupportsLocalOperations marker interface to check if supported
     */
    void localPut(Serializable key, Serializable value) throws OXException;

    /**
     * Removes the object located in specified group and bound to given key.
     *
     * @param key The key
     * @param group The group name.
     * @throws IllegalArgumentException If given key or group is <code>null</code>
     */
    void removeFromGroup(Serializable key, String group);

    /**
     * Removes multiple objects located in a specified group and bound the given keys.
     *
     * @param keys The keys
     * @param group The group name.
     * @throws IllegalArgumentException If given group is <code>null</code>
     */
    void removeFromGroup(List<Serializable> keys, String group);

    /**
     * Removes (optional operation) the object located in specified group and bound to given key without propagating that operation neither
     * laterally nor remotely.
     *
     * @param key The key
     * @param group The group name.
     * @throws IllegalArgumentException If given key or group is <code>null</code>
     * @see SupportsLocalOperations SupportsLocalOperations marker interface to check if supported
     */
    void localRemoveFromGroup(Serializable key, String group);

    /**
     * This method does not reset the attributes for items already in the cache. It could potentially do this for items in memory, and maybe
     * on disk (which would be slow) but not remote items. Rather than have unpredictable behavior, this method just sets the default
     * attributes. Items subsequently put into the cache will use these defaults if they do not specify specific attributes.
     *
     * @param attr The default attributes.
     * @throws OXException If default element attributes cannot be applied.
     */
    void setDefaultElementAttributes(ElementAttributes attr) throws OXException;

    /**
     * This returns the cache statistics with information on this region and its auxiliaries.
     * <p>
     * This data can be formatted as needed.
     *
     * @return The cache statistics with information on this region and its auxiliaries.
     */
    CacheStatistics getStatistics();

    /**
     * Creates a new instance of {@link CacheKey} consisting of specified context ID and object ID.
     * <p>
     * This is a convenience method that delegates to {@link CacheService#newCacheKey(int, int)}.
     *
     * @param contextId The context ID
     * @param objectId The object ID
     * @return The new instance of {@link CacheKey}
     */
    CacheKey newCacheKey(int contextId, int objectId);

    /**
     * Creates a new instance of {@link CacheKey} consisting of specified context ID and serializable object.
     * <p>
     * This is a convenience method that delegates to {@link CacheService#newCacheKey(int, Serializable)}.
     *
     * @param contextId The context ID
     * @param objs The objects for the key
     * @return new instance of {@link CacheKey}
     */
    CacheKey newCacheKey(int contextId, String... objs);

    /**
     * Gets the set of keys of objects currently in the group.
     * @param group
     * @return a set of keys
     * @throws IllegalArgumentException If given group is <code>null</code>
     */
    Set<?> getGroupKeys(String group) throws OXException;

    /**
     * Gets the set of group names in the cache
     * @return a set of group names
     */
    Set<String> getGroupNames() throws OXException;

    /**
     * Gets all the set of keys in the cache
     * @return a set of keys in the cache
     * @throws OXException
     */
    Set<?> getAllKeys() throws OXException;

    /**
     * Get a ranged set of keys
     * @param start of range
     * @param end of range
     * @return a set of keys in cache
     * @throws OXException
     */
    Set<?> getKeysInRange(int start, int end) throws OXException;
}
