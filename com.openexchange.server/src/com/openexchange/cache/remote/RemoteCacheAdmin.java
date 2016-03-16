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

package com.openexchange.cache.remote;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private final Registry registry;

	/**
	 * Start the remote cache administration to handle incoming RMI calls
	 */
	public static void startRemoteCacheAdmin() {
		if (!initialized.get()) {
			synchronized (initialized) {
				if (instance == null) {
					try {
						instance = new RemoteCacheAdmin();
						initialized.set(true);
					} catch (final RemoteException e) {
						LOG.error("", e);
					} catch (final AlreadyBoundException e) {
						LOG.error("", e);
					}
				}
			}
		}
	}

	/**
	 * @return <code>true</code> if intialized; <code>false</code> otherwise
	 */
	public static boolean isInitialized() {
		return initialized.get();
	}

	/**
	 * @param args - the program arguments
	 */
	public static void main(final String[] args) {
		startRemoteCacheAdmin();
	}

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
