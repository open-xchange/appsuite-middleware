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

package com.openexchange.config.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.config.internal.filewatcher.FileListener;
import com.openexchange.java.Streams;

/**
 * {@link PropertyWatcher}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PropertyWatcher implements FileListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertyWatcher.class);

    private static final ConcurrentMap<String, PropertyWatcher> WATCHER_MAP = new ConcurrentHashMap<String, PropertyWatcher>();

    /**
     * Gets an existing property watcher bound to given property name
     *
     * @param name The property name
     * @return The corresponding property watcher or <code>null</code> if none bound to given property name
     */
    public static PropertyWatcher getPropertyWatcher(final String name) {
        return WATCHER_MAP.get(name);
    }

    /**
     * Removes an existing property watcher bound to given property name
     *
     * @param name The property name
     * @return The removed property watcher
     */
    public static PropertyWatcher removePropertWatcher(final String name) {
        final PropertyWatcher removed = WATCHER_MAP.remove(name);
        if (null != removed) {
            removed.listeners.clear();
        }
        return removed;
    }

    /**
     * Gets all watchers.
     *
     * @return The watchers map
     */
    public static Map<String, PropertyWatcher> getAllWatchers() {
        return WATCHER_MAP;
    }

    /**
     * Adds a new property watcher bound to given property name. If a previous property watcher has already been bound to specified property
     * name, the existing one is returned and no new watcher is created.
     *
     * @param name The property name
     * @param value The property value
     * @param caseInsensitive <code>true</code> to compare changed values case-insensitive; otherwise <code>false</code>
     * @return The either newly created or existing property watcher bound to given property name
     */
    public static PropertyWatcher addPropertyWatcher(final String name, final String value, final boolean caseInsensitive) {
        PropertyWatcher watcher = WATCHER_MAP.get(name);
        if (null == watcher) {
            final PropertyWatcher newWatcher = new PropertyWatcher(name, value, caseInsensitive);
            watcher = WATCHER_MAP.putIfAbsent(name, newWatcher);
            if (null == watcher) {
                watcher = newWatcher;
            }
        }
        return watcher;
    }

    // -------------------------------------------------------------------------------------------------- //

    private final Map<Class<? extends PropertyListener>, PropertyListener> listeners;
    private final boolean caseInsensitive;
    private final String name;
    private String value;

    /**
     * Initializes a new property watcher
     *
     * @param name The property name to watch
     * @param value The current property value
     * @param caseInsensitive <code>true</code> to compare changed values case-insensitive; otherwise <code>false</code>
     */
    private PropertyWatcher(final String name, final String value, final boolean caseInsensitive) {
        super();
        listeners = new ConcurrentHashMap<Class<? extends PropertyListener>, PropertyListener>();
        this.name = name;
        this.value = value;
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Adds an instance of {@link PropertyListener} to this watcher's listeners that is going to be notified on property change or delete
     * events.
     *
     * @param listener The listener to add
     */
    public void addPropertyListener(final PropertyListener listener) {
        if (!listeners.containsKey(listener.getClass())) {
            listeners.put(listener.getClass(), listener);
        }
    }

    /**
     * Removes specified instance of {@link PropertyListener} from this watcher's listeners
     *
     * @param listener The listener to remove
     */
    public void removePropertyListener(final PropertyListener listener) {
        if (listeners.containsKey(listener.getClass())) {
            listeners.remove(listener.getClass());
        }
    }

    /**
     * Checks if this property watcher is empty; meaning zero number of added property listeners
     *
     * @return <code>true</code> if this property watcher is empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    @Override
    public void onChange(final File file) {
        final InputStream fis;
        try {
            fis = new BufferedInputStream(new FileInputStream(file), 65536);
        } catch (final FileNotFoundException e) {
            LOG.debug("", e);
            /*
             * Obviously file does no more exist
             */
            value = null;
            notifyListeners(true);
            return;
        }
        try {
            final Properties properties = new Properties();
            properties.load(fis);
            final String newValue = properties.getProperty(name);
            if (newValue == null) {
                value = null;
                notifyListeners(true);
                return;
            }
            if ((caseInsensitive ? (!newValue.equalsIgnoreCase(value)) : (!newValue.equals(value)))) {
                value = newValue;
                notifyListeners(false);
            }
        } catch (final IOException e) {
            LOG.error("", e);
        } finally {
            Streams.close(fis);
        }
    }

    /**
     * Gets the name of the property being watched.
     *
     * @return The property name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the current value of the property being watched.
     *
     * @return The property value
     */
    public String getValue() {
        return value;
    }

    @Override
    public void onDelete() {
        value = null;
        notifyListeners(true);
    }

    private void notifyListeners(final boolean isDelete) {
        final PropertyEvent event = new PropertyEventImpl(name, value, isDelete ? PropertyEvent.Type.DELETED : PropertyEvent.Type.CHANGED);
        for (final PropertyListener propertyListener : listeners.values()) {
            propertyListener.onPropertyChange(event);
        }
    }

}
