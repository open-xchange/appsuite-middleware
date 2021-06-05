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

package com.openexchange.config.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;


/**
 * {@link AbstractReloadableServiceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public abstract class AbstractReloadableServiceTracker<R extends Reloadable> implements ServiceTrackerCustomizer<R, R> {

    /** The bundle context */
    private final BundleContext context;

    /**
     * Initializes a new {@link AbstractReloadableServiceTracker}.
     */
    protected AbstractReloadableServiceTracker(BundleContext context) {
        super();
        this.context = context;
    }

    /**
     * Handles specified reloadable instance.
     *
     * @param reloadable The tracked reloadable instance
     * @return <code>true</code> if successfully handled; otherwise <code>false</code> to discard
     */
    protected abstract boolean handleTrackedReloadable(R reloadable);

    /**
     * Handles specified reloadable instance.
     *
     * @param reloadable The untracked reloadable instance
     */
    protected abstract void handleUntrackedReloadable(R reloadable);

    @Override
    public R addingService(ServiceReference<R> serviceRef) {
        R reloadable = context.getService(serviceRef);

        try {
            Interests interests = reloadable.getInterests();

            String[] propertiesOfInterest = null == interests ? null : interests.getPropertiesOfInterest();
            if (null != propertiesOfInterest && propertiesOfInterest.length > 0) {
                for (String propertyName : propertiesOfInterest) {
                    Reloadables.validatePropertyName(propertyName);
                }
            }

            String[] configFileNames = null == interests ? null : interests.getConfigFileNames();
            if (null != configFileNames && configFileNames.length > 0) {
                for (String configFileName : configFileNames) {
                    Reloadables.validateFileName(configFileName);
                }
            }

        } catch (IllegalArgumentException e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractReloadableServiceTracker.class);
            logger.error("Reloadable {} specifies an invalid property name and/or file name and will therefore be discarded.", reloadable.getClass().getName(), e);
            context.ungetService(serviceRef);
            return null;
        }

        if (handleTrackedReloadable(reloadable)) {
            return reloadable;
        }

        // Discard
        context.ungetService(serviceRef);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<R> serviceRef, R reloadable) {
        // nothing to do
    }

    @Override
    public void removedService(ServiceReference<R> serviceRef, R reloadable) {
        handleUntrackedReloadable(reloadable);
        context.ungetService(serviceRef);
    }
}
