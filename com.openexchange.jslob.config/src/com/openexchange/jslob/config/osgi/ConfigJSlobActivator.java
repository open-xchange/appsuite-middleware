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

package com.openexchange.jslob.config.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.config.ConfigJSlobReloadable;
import com.openexchange.jslob.config.ConfigJSlobService;
import com.openexchange.jslob.config.JSlobEntryRegistry;
import com.openexchange.jslob.shared.SharedJSlobService;
import com.openexchange.jslob.storage.registry.JSlobStorageRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link ConfigJSlobActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConfigJSlobActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ConfigJSlobActivator}.
     */
    public ConfigJSlobActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { JSlobStorageRegistry.class, ConfigViewFactory.class, SessiondService.class, ConfigurationService.class, EventAdmin.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;

        // Registry for JSlobEntry instances
        JSlobEntryRegistry jSlobEntryRegistry = new JSlobEntryRegistry();
        track(JSlobEntry.class, new JSlobEntryTracker(jSlobEntryRegistry, context));

        // The JSlob service
        final ConfigJSlobService service = new ConfigJSlobService(jSlobEntryRegistry, this);

        // More trackers
        track(SharedJSlobService.class, new SharedJSlobServiceTracker(context, service));
        track(ConfigTreeEquivalent.class, new ConfigTreeEquivalentTracker(service, context));
        openTrackers();

        // Register services
        registerService(JSlobService.class, service);
        registerService(Reloadable.class, new ConfigJSlobReloadable(service));
    }
}
