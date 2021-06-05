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

package com.openexchange.admin.reseller.services;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.admin.reseller.plugins.OXResellerPluginInterface;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.osgi.ServiceListings;

/**
 * {@link PluginInterfaces}
 *
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @since v7.10.3
 */
public class PluginInterfaces {

    private static final AtomicReference<PluginInterfaces> INSTANCE_REF = new AtomicReference<PluginInterfaces>();

    /**
     * Sets the instance
     *
     * @param instance The instance to set or <code>null</code> to clear it
     */
    public static void setInstance(final PluginInterfaces instance) {
        INSTANCE_REF.set(instance);
    }

    /**
     * Gets the instance
     *
     * @return The instance or <code>null</code>
     */
    public static PluginInterfaces getInstance() {
        return INSTANCE_REF.get();
    }

    // ------------------------------------------------------------------------------------------------------------- //

    /**
     * The builder for a {@link PluginInterfaces} instance.
     */
    public static final class Builder {

        ServiceListing<OXResellerPluginInterface> resellerPlugins;

        public Builder() {
            super();
        }

        public Builder resellerPlugins(ServiceListing<OXResellerPluginInterface> serviceListing) {
            this.resellerPlugins = serviceListing;
            return this;
        }

        public PluginInterfaces build() {
            return new PluginInterfaces(this);
        }
    }

    // ------------------------------------------------------------------------------------------------------------- //

    private final ServiceListing<OXResellerPluginInterface> resellerPlugins;

    /**
     * Initializes a new {@link PluginInterfaces}.
     */
    PluginInterfaces(final Builder builder) {
        super();
        this.resellerPlugins = null == builder.resellerPlugins ? ServiceListings.<OXResellerPluginInterface> emptyList() : builder.resellerPlugins;
    }

    /**
     * Gets the rank-wise sorted user plugins.
     *
     * @return The user plugins
     */
    public ServiceListing<OXResellerPluginInterface> getResellerPlugins() {
        return resellerPlugins;
    }

}
