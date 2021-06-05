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

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * RemoteCacheAdmin
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class RemoteCacheAdmin {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoteCacheAdmin.class);

	private static final int REGISTRY_PORT = 57462;

	private static volatile RemoteCacheAdmin instance;
	/**
	 * Start the remote cache administration to handle incoming RMI calls
	 */
	public static void startRemoteCacheAdmin() {
	    RemoteCacheAdmin tmp = instance;
	    if (null == tmp) {
			synchronized (RemoteCacheAdmin.class) {
			    tmp = instance;
		        if (null == tmp) {
		            try {
                        tmp = new RemoteCacheAdmin();
                        instance = tmp;
                    } catch (RemoteException e) {
                        LOG.error("", e);
                    } catch (AlreadyBoundException e) {
                        LOG.error("", e);
                    }
		        }
			}
		}
	}

	/**
	 * @return <code>true</code> if initialized; <code>false</code> otherwise
	 */
	public static boolean isInitialized() {
		return null != instance;
	}

	/**
	 * @param args - the program arguments
	 */
	public static void main(final String[] args) {
		startRemoteCacheAdmin();
	}

	private final Registry registry;

	private RemoteCacheAdmin() throws RemoteException, AlreadyBoundException {
		super();
		/*
		 * Create registry
		 */
		LocateRegistry.createRegistry(REGISTRY_PORT);
		/*
		 * Create RMI objects (and their methods...)
		 */
		final GenericCacheInvalidationInterface gci = (GenericCacheInvalidationInterface) UnicastRemoteObject
				.exportObject(new FolderCacheInvalidation(), 0);
		/*
		 * Apply registry
		 */
		registry = LocateRegistry.getRegistry(REGISTRY_PORT);
		/*
		 * Bind interface to registry
		 */
		registry.bind(gci.getRemoteName(), gci);
		LOG.info("RemoteCacheAdmin started...");
	}

}
