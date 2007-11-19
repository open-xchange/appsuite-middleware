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

package com.openexchange.config.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.config.internal.filewatcher.FileListener;

/**
 * {@link PropertyWatcher}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class PropertyWatcher implements FileListener {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(PropertyWatcher.class);

	private static final Map<String, PropertyWatcher> watchers = new ConcurrentHashMap<String, PropertyWatcher>();

	/**
	 * Gets an existing property watcher bound to given property name
	 * 
	 * @param name
	 *            The property name
	 * @return The corresponding property watcher or <code>null</code> if none
	 *         bound to given property name
	 */
	public static PropertyWatcher getPropertyWatcher(final String name) {
		return watchers.get(name);
	}

	/**
	 * Adds a new property watcher bound to given property name. If a previous
	 * property watcher has already been bound to specified property name, the
	 * existing one is returned and no new watcher is created.
	 * 
	 * @param name
	 *            The property name
	 * @param value
	 *            The property value
	 * @param caseInsensitive
	 *            <code>true</code> to compare changed values
	 *            case-insensitive; otherwise <code>false</code>
	 * @return The either newly created or existing property watcher bound to
	 *         given property name
	 */
	public static PropertyWatcher addPropertyWatcher(final String name, final String value,
			final boolean caseInsensitive) {
		if (watchers.containsKey(name)) {
			return watchers.get(name);
		}
		final PropertyWatcher watcher = new PropertyWatcher(name, value, caseInsensitive);
		watchers.put(name, watcher);
		return watcher;
	}

	private final Map<Class<? extends PropertyListener>, PropertyListener> listeners = new ConcurrentHashMap<Class<? extends PropertyListener>, PropertyListener>();

	private final boolean caseInsensitive;

	private final String name;

	private String value;

	/**
	 * Initializes a new property watcher
	 * 
	 * @param name
	 *            The property name to watch
	 * @param value
	 *            The current property value
	 * @param caseInsensitive
	 *            <code>true</code> to compare changed values
	 *            case-insensitive; otherwise <code>false</code>
	 */
	private PropertyWatcher(final String name, final String value, final boolean caseInsensitive) {
		super();
		this.name = name;
		this.value = value;
		this.caseInsensitive = caseInsensitive;
	}

	/**
	 * Adds an instance of {@link PropertyListener} to this watcher's listeners
	 * that is going to be notified on property change or delete events.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void addPropertyListener(final PropertyListener listener) {
		if (!listeners.containsKey(listener.getClass())) {
			listeners.put(listener.getClass(), listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.internal.filewatcher.FileListener#onChange(java.io.File)
	 */
	public void onChange(final File file) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			final Properties properties = new Properties();
			properties.load(fis);
			final String newValue = properties.getProperty(name);
			if (newValue == null) {
				this.value = null;
				notifyListeners(true);
				return;
			}
			if ((caseInsensitive ? (!newValue.equalsIgnoreCase(this.value)) : (!newValue.equals(this.value)))) {
				this.value = newValue;
				notifyListeners(false);
			}
		} catch (final FileNotFoundException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getLocalizedMessage(), e);
			}
			/*
			 * Obviously file does no more exist
			 */
			this.value = null;
			notifyListeners(true);
		} catch (final IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} finally {
			if (null != fis) {
				try {
					fis.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.internal.filewatcher.FileListener#onDelete()
	 */
	public void onDelete() {
		this.value = null;
		notifyListeners(true);
	}

	private void notifyListeners(final boolean isDelete) {
		final PropertyEvent event = new PropertyEventImpl(name, value, isDelete ? PropertyEvent.Type.DELETED
				: PropertyEvent.Type.CHANGED);
		for (final Iterator<PropertyListener> iter = listeners.values().iterator(); iter.hasNext();) {
			iter.next().onPropertyChange(event);
		}
	}
}
