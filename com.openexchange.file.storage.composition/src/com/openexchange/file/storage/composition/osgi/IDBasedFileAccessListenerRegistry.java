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

package com.openexchange.file.storage.composition.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.file.storage.composition.IDBasedFileAccessListener;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;


/**
 * {@link IDBasedFileAccessListenerRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class IDBasedFileAccessListenerRegistry extends RankingAwareNearRegistryServiceTracker<IDBasedFileAccessListener> {

    private static volatile IDBasedFileAccessListenerRegistry instance;

    /**
     * Initializes the <code>IDBasedFileAccessListenerRegistry</code> instance
     *
     * @param context The required bundle context
     * @return The initialized instance
     */
    public static synchronized IDBasedFileAccessListenerRegistry initInstance(BundleContext context) {
        IDBasedFileAccessListenerRegistry inst = new IDBasedFileAccessListenerRegistry(context);
        instance = inst;
        return inst;
    }

    /**
     * Drops the <code>IDBasedFileAccessListenerRegistry</code> instance
     */
    public static synchronized void dropInstance() {
        instance = null;
    }

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static IDBasedFileAccessListenerRegistry getInstance() {
        return instance;
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link IDBasedFileAccessListenerRegistry}.
     *
     * @param context The bundle context
     */
    private IDBasedFileAccessListenerRegistry(BundleContext context) {
        super(context, IDBasedFileAccessListener.class);
    }
}
