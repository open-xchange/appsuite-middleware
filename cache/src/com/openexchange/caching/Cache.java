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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.caching;

import java.io.Serializable;

/**
 * {@link Cache} - This class provides an interface for all types of access to
 * the cache.
 * <p>
 * An instance of this class is bound to a specific cache region.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface Cache {

	/**
	 * Removes all of the elements from cache.
	 * 
	 * @throws CacheException
	 *             If cache cannot be cleared
	 */
	public void clear() throws CacheException;

	/**
	 * Disposes this cache. Flushes objects to and closes auxiliary caches. This
	 * is a shutdown command.
	 * <p>
	 * To simply remove all elements from the cache use {@link #clear()}
	 */
	public void dispose();

	/**
	 * Retrieves the object from the cache which is bound to specified key.
	 * 
	 * @param key
	 *            The key
	 * @return The cached object if found or <code>null</code>
	 */
	public Object get(Serializable key);

	/**
	 * Retrieves a <b>copy</b> of the default element attributes used by this
	 * cache. This does not provide a reference to the element attributes.
	 * <p>
	 * Each time an element is added to the cache without element attributes,
	 * the default element attributes are cloned.
	 * 
	 * @return The default element attributes used by this cache.
	 * @throws CacheException
	 *             If default element attributes cannot be returned
	 */
	public ElementAttributes getDefaultElementAttributes() throws CacheException;

	/**
	 * Gets an item out of the cache that is in specified group.
	 * 
	 * @param key
	 *            The key
	 * @param group
	 *            The group name.
	 * @return The cached value, <code>null</code> if not found.
	 */
	public Object getFromGroup(Serializable key, String group);

	/**
	 * Invalidates a group: remove all the group members
	 * 
	 * @param group
	 *            The name of the group to invalidate
	 */
	public void invalidateGroup(String group);

	/**
	 * Place a new object in the cache, associated with key name. If there is
	 * currently an object associated with name in the cache it is replaced.
	 * Names are scoped to a cache so they must be unique within the cache they
	 * are placed. ObjectExistsException
	 * 
	 * @param key
	 *            The key
	 * @param obj
	 *            Object to store
	 * @exception CacheException
	 *                If put operation on cache fails
	 */
	public void put(Serializable key, Serializable obj) throws CacheException;

	/**
	 * Constructs a cache element with these attributes, and puts it into the
	 * cache.
	 * <p>
	 * If the key or the value is null, and InvalidArgumentException is thrown.
	 * 
	 * @param key
	 *            The key
	 * @param val
	 *            The object to store
	 * @param attr
	 *            The object's element attributes
	 * @exception CacheException
	 *                If put operation on cache fails
	 */
	public void put(Serializable key, Serializable val, ElementAttributes attr) throws CacheException;

	/**
	 * Allows the user to put an object into a group within a particular cache
	 * cache. This method allows the object's attributes to be individually
	 * specified.
	 * 
	 * @param key
	 *            The key
	 * @param groupName
	 *            The group name.
	 * @param value
	 *            The object to cache
	 * @param attr
	 *            The objects attributes.
	 * @throws CacheException
	 *             If put operation on cache fails
	 */
	public void putInGroup(Serializable key, String groupName, Object value, ElementAttributes attr)
			throws CacheException;

	/**
	 * Allows the user to put an object into a group within a particular cache
	 * cache. This method sets the object's attributes to the default for the
	 * cache.
	 * 
	 * @param key
	 *            The key
	 * @param groupName
	 *            The group name.
	 * @param value
	 *            The object to cache
	 * @throws CacheException
	 *             If put operation on cache fails
	 */
	public void putInGroup(Serializable key, String groupName, Serializable value) throws CacheException;

	/**
	 * Place a new object in the cache, associated with key. If there is
	 * currently an object associated with key in the cache an exception is
	 * thrown. Keys are scoped to a cache so they must be unique within the
	 * cache they are placed.
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            Object to store
	 * @exception CacheException
	 *                If the item is already in the cache.
	 */
	public void putSafe(Serializable key, Serializable value) throws CacheException;

	/**
	 * Removes the object from the cache which is bound to specified key.
	 * 
	 * @param key
	 *            The key
	 * @throws CacheException
	 *             If remove operation on cache fails
	 */
	public void remove(Serializable key) throws CacheException;

	/**
	 * Removes the object located in specified group and bound to given key.
	 * 
	 * @param key
	 *            The key
	 * @param group
	 *            The group name.
	 */
	public void removeFromGroup(Serializable key, String group);

	/**
	 * This method does not reset the attributes for items already in the cache.
	 * It could potentially do this for items in memory, and maybe on disk
	 * (which would be slow) but not remote items. Rather than have
	 * unpredictable behavior, this method just sets the default attributes.
	 * Items subsequently put into the cache will use these defaults if they do
	 * not specify specific attributes.
	 * 
	 * @param attr
	 *            The default attributes.
	 * @throws CacheException
	 *             If default element attributes cannot be applied.
	 */
	public void setDefaultElementAttributes(ElementAttributes attr) throws CacheException;
}
