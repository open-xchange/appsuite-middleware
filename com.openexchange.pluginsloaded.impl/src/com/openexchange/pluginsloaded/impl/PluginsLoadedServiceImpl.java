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

package com.openexchange.pluginsloaded.impl;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import com.openexchange.osgi.Tools;
import com.openexchange.pluginsloaded.PluginsLoadedService;

/**
 * {@link PluginsLoadedServiceImpl}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.8.4
 */
public class PluginsLoadedServiceImpl implements PluginsLoadedService {

    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PluginsLoadedServiceImpl.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link PluginsLoadedServiceImpl}.
     *
     * @param context The bundle context
     */
    public PluginsLoadedServiceImpl(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public boolean allPluginsloaded() {
        Bundle[] bundles = context.getBundles();
        List<Bundle> fragments = null;
        List<Bundle> notStarted = null;
        for (Bundle bundle : bundles) {
            if (Tools.isFragment(bundle)) {
                if (fragments == null) {
                    fragments = new ArrayList<Bundle>();
                }
                fragments.add(bundle);
            } else if (Bundle.ACTIVE != bundle.getState()) {
                if (notStarted == null) {
                    notStarted = new ArrayList<Bundle>();
                }
                notStarted.add(bundle);
            }
        }

        if (notStarted == null) {
            // Nothing added to not-started collection
            return true;
        }

        if (fragments != null) {
            LOGGER.info("System contains the following fragments that stay in RESOLVED state: {}", fragments);
        }
        LOGGER.error("The following bundles aren't started: {}", notStarted);
        return false;
    }

}
