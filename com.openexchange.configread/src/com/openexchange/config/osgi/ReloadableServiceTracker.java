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
import com.openexchange.config.Reloadable;
import com.openexchange.config.internal.ConfigurationImpl;


/**
 * {@link ReloadableServiceTracker}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class ReloadableServiceTracker extends AbstractReloadableServiceTracker<Reloadable> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReloadableServiceTracker.class);

    private final ConfigurationImpl configService;

    /**
     * Initializes a new {@link ReloadableServiceTracker}.
     */
    public ReloadableServiceTracker(BundleContext context, ConfigurationImpl configService) {
        super(context);
        this.configService = configService;
    }

    @Override
    protected boolean handleTrackedReloadable(Reloadable reloadable) {
        if (configService.addReloadable(reloadable)) {
            LOG.debug("Reloadable service added: {}", reloadable.getClass().getName());
            return true;
        }
        return false;
    }

    @Override
    protected void handleUntrackedReloadable(Reloadable reloadable) {
        configService.removeReloadable(reloadable);
        LOG.debug("Reloadable service removed: {}", reloadable.getClass().getName());
    }

}
