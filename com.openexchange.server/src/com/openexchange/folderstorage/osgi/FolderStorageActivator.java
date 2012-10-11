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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.osgi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.customizer.folder.AdditionalFieldsUtils;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.osgi.CacheFolderStorageActivator;
import com.openexchange.folderstorage.database.osgi.DatabaseFolderStorageActivator;
import com.openexchange.folderstorage.filestorage.osgi.FileStorageFolderStorageActivator;
import com.openexchange.folderstorage.internal.ContentTypeRegistry;
import com.openexchange.folderstorage.internal.FolderServiceImpl;
import com.openexchange.folderstorage.mail.osgi.MailFolderStorageActivator;
import com.openexchange.folderstorage.messaging.osgi.MessagingFolderStorageActivator;
import com.openexchange.folderstorage.outlook.osgi.OutlookFolderStorageActivator;
import com.openexchange.folderstorage.virtual.osgi.VirtualFolderStorageActivator;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FolderStorageActivator} - {@link BundleActivator Activator} for folder
 * storage framework.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderStorageActivator implements BundleActivator {

	private static final class Key {

		public static Key valueOf(final int userId, final int cid) {
			return new Key(userId, cid);
		}

		private final int userId;

		private final int cid;

		private final int hash;

		public Key(final int userId, final int cid) {
			super();
			this.userId = userId;
			this.cid = cid;
			final int prime = 31;
			int result = 1;
			result = prime * result + cid;
			result = prime * result + userId;
			hash = result;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Key)) {
				return false;
			}
			final Key other = (Key) obj;
			if (cid != other.cid) {
				return false;
			}
			if (userId != other.userId) {
				return false;
			}
			return true;
		}

	}

	private static final class DisplayNameFolderField implements
			AdditionalFolderField {

		private final ConcurrentMap<Key, String> cache;

		protected DisplayNameFolderField() {
			super();
			final Lock lock = new ReentrantLock();
			cache = new LockBasedConcurrentMap<Key, String>(lock, lock,
					new MaxCapacityLinkedHashMap<Key, String>(1000));
		}

		@Override
        public Object renderJSON(final Object value) {
		    return value == null ? JSONObject.NULL : value;
		}

		@Override
        public Object getValue(final FolderObject folder,
				final ServerSession session) {
			final int createdBy = folder.getCreatedBy();
			if (createdBy <= 0) {
				return JSONObject.NULL;
			}
			final Context context = session.getContext();
			final String displayName = cache.get(Key.valueOf(createdBy,
					context.getContextId()));
			return null == displayName ? UserStorage.getStorageUser(createdBy,
					context).getDisplayName() : displayName;
		}

		@Override
        public String getColumnName() {
			return "com.openexchange.folderstorage.displayName";
		}

		@Override
        public int getColumnID() {
			return 3030;
		}

		@Override
        public List<Object> getValues(final List<FolderObject> folder,
				final ServerSession session) {
			return AdditionalFieldsUtils.bulk(this, folder, session);
		}

	}

	private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log
			.valueOf(com.openexchange.log.LogFactory
					.getLog(FolderStorageActivator.class));


	private List<ServiceRegistration<?>> serviceRegistrations;

	private List<ServiceTracker<?, ?>> serviceTrackers;

	private List<BundleActivator> activators;

	/**
	 * Initializes a new {@link FolderStorageActivator}.
	 */
	public FolderStorageActivator() {
		super();
	}

	@Override
    public void start(final BundleContext context) throws Exception {
		try {
			// Register error component
			// Register services
			serviceRegistrations = new ArrayList<ServiceRegistration<?>>(4);
			// Register folder service
			serviceRegistrations.add(context.registerService(
					FolderService.class.getName(), new FolderServiceImpl(),
					null));
			serviceRegistrations.add(context.registerService(
					ContentTypeDiscoveryService.class.getName(),
					ContentTypeRegistry.getInstance(), null));
			serviceRegistrations.add(context.registerService(
					AdditionalFolderField.class.getName(),
					new DisplayNameFolderField(), null));
			// Register service trackers
			serviceTrackers = new ArrayList<ServiceTracker<?, ?>>(4);
			serviceTrackers.add(new ServiceTracker<FolderStorage,FolderStorage>(context, FolderStorage.class
					.getName(), new FolderStorageTracker(context)));
			for (final ServiceTracker<?,?> serviceTracker : serviceTrackers) {
				serviceTracker.open();
			}

			// Start other activators
			activators = new ArrayList<BundleActivator>(8);
			activators.add(new DatabaseFolderStorageActivator()); // Database impl
			activators.add(new MailFolderStorageActivator()); // Mail impl
			activators.add(new MessagingFolderStorageActivator()); // Messaging impl
			activators.add(new FileStorageFolderStorageActivator()); // File storage impl
			activators.add(new CacheFolderStorageActivator()); // Cache impl
			activators.add(new OutlookFolderStorageActivator()); // MS Outlook storage activator
			activators.add(new VirtualFolderStorageActivator()); // Virtual storage activator
			BundleActivator activator = null;
			for (final Iterator<BundleActivator> iter = activators.iterator(); iter
					.hasNext();) {
				try {
					if (isBundleResolved(context)) {
						if (null != activator) {
							logFailedStartup(activator);
						}
						return;
					}
				} catch (final IllegalStateException e) {
					if (null != activator) {
						logFailedStartup(activator);
					}
					return;
				}
				activator = iter.next();
				activator.start(context);
			}

			if (LOG.isInfoEnabled()) {
				final StringBuilder sb = new StringBuilder(32);
				sb.append("Bundle \"");
				sb.append("com.openexchange.folderstorage");
				sb.append("\" successfully started!");
				LOG.info(sb.toString());
			}
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

	private static boolean isBundleResolved(final BundleContext context) {
		return Bundle.RESOLVED == context.getBundle().getState();
	}

	private static void logFailedStartup(final BundleActivator activator) {
		final StringBuilder sb = new StringBuilder(32);
		sb.append("Failed start of folder storage bundle \"");
		sb.append(activator.getClass().getName());
		sb.append("\"!");
		LOG.error(sb.toString(), new Throwable());
	}

	@Override
    public void stop(final BundleContext context) throws Exception {
		try {
			// Drop activators
			if (null != activators) {
				for (final BundleActivator activator : activators) {
					activator.stop(context);
				}
				activators.clear();
				activators = null;
			}
			// Drop service trackers
			if (null != serviceTrackers) {
				for (final ServiceTracker<?,?> serviceTracker : serviceTrackers) {
					serviceTracker.close();
				}
				serviceTrackers.clear();
				serviceTrackers = null;
			}
			// Unregister previously registered services
			if (null != serviceRegistrations) {
				for (final ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
					serviceRegistration.unregister();
				}
				serviceRegistrations.clear();
				serviceRegistrations = null;
			}
			// Unregister previously registered component


			if (LOG.isInfoEnabled()) {
				final StringBuilder sb = new StringBuilder(32);
				sb.append("Bundle \"");
				sb.append("com.openexchange.folderstorage");
				sb.append("\" successfully stopped!");
				LOG.info(sb.toString());
			}
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

}
