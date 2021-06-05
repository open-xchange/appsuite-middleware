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

package com.openexchange.folder.json;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.folderstorage.FolderField;
import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.map.TIntObjectMap;


/**
 * {@link FolderFieldRegistry} - A simple registry for folder fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderFieldRegistry {

    /**
     * The dummy object.
     */
    protected static final Object PRESENT = new Object();

    private static volatile FolderFieldRegistry instance;

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static FolderFieldRegistry getInstance() {
        FolderFieldRegistry tmp = instance;
        if (null == tmp) {
            synchronized (FolderFieldRegistry.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = new FolderFieldRegistry();
                    instance = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the registry instance.
     */
    public static void releaseInstance() {
        instance = null;
    }

    /*-
     *
     * ----------------------- MEMBER STUFF --------------------------
     *
     */

    private final ConcurrentMap<FolderField, Object> map;

    private final ConcurrentTIntObjectHashMap<FolderField> numSet;

    private volatile ServiceTracker<FolderField, FolderField> serviceTracker;

    /**
     * Initializes a new {@link FolderFieldRegistry}.
     */
    private FolderFieldRegistry() {
        super();
        map = new ConcurrentHashMap<FolderField, Object>(8, 0.9f, 1);
        numSet = new ConcurrentTIntObjectHashMap<FolderField>(8);
    }

    /**
     * Starts up this registry.
     *
     * @param context The bundle context
     */
    public void startUp(final BundleContext context) {
        ServiceTracker<FolderField, FolderField> tmp = serviceTracker;
        if (null == tmp) {
            synchronized (map) {
                tmp = serviceTracker;
                if (null == tmp) {
                    final ConcurrentMap<FolderField, Object> m = this.map;
                    final ConcurrentTIntObjectHashMap<FolderField> ns = this.numSet;
                    tmp = new ServiceTracker<FolderField, FolderField>(context, FolderField.class, new ServiceTrackerCustomizer<FolderField, FolderField>() {

                        @Override
                        public FolderField addingService(final ServiceReference<FolderField> reference) {
                            final FolderField pair = context.getService(reference);
                            if (null == m.putIfAbsent(pair, PRESENT)) {
                                ns.put(pair.getField(), pair);
                                return pair;
                            }
                            context.ungetService(reference);
                            return null;
                        }

                        @Override
                        public void modifiedService(final ServiceReference<FolderField> reference, final FolderField pair) {
                            // Nope
                        }

                        @Override
                        public void removedService(final ServiceReference<FolderField> reference, final FolderField pair) {
                            m.remove(pair);
                            ns.remove(pair.getField());
                            context.ungetService(reference);
                        }
                    });
                    tmp.open();
                    serviceTracker = tmp;
                }
            }
        }
    }

    /**
     * Shuts down this registry.
     */
    public void shutDown() {
        ServiceTracker<FolderField, FolderField> tmp = serviceTracker;
        if (null != tmp) {
            synchronized (map) {
                tmp = serviceTracker;
                if (null != tmp) {
                    tmp.close();
                    serviceTracker = null;
                }
            }
        }
    }

    /**
     * Gets the registered pairs.
     *
     * @return The registered pairs
     */
    public Set<FolderField> getPairs() {
        return map.keySet();
    }

    /**
     * Gets the registered fields.
     *
     * @return The registered fields
     */
    public TIntObjectMap<FolderField> getFields() {
        return numSet;
    }

}
