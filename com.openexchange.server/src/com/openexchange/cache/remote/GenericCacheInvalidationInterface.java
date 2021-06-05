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

package com.openexchange.cache.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * GenericCacheInvalidationInterface
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public interface GenericCacheInvalidationInterface extends Remote {

	/**
	 * Indicates the <b>unique</b> name of implementing class which is then
	 * used to bind it to registry
	 *
	 * @return unique name of implementing class
	 */
	String getRemoteName();

	/**
	 * Invalidates all context-associated elements in cache. This is possible if
	 * cache is strucutred by groups and if a group represents a context.
	 * Following JCS API method
	 * <code>org.apache.jcs.JCS.invalidateGroup(String groupName)</code>
	 *
	 * @param contextId -
	 *            the context ID
	 * @throws RemoteException
	 */
	void invalidateContext(final int contextId) throws RemoteException;

	/**
	 * Invalidates a single element in cache whose key is concatenated by given
	 * context ID and object ID.
	 *
	 * @param contextId -
	 *            the context ID
	 * @param objectId -
	 *            the object ID
	 * @throws RemoteException
	 */
	void invalidateCacheElement(final int contextId, final int objectId) throws RemoteException;

}
