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

package com.openexchange.folder.json;

import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.map.TIntObjectMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.folderstorage.FolderField;


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
