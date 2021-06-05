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

package com.openexchange.admin.plugin.hosting.services;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.OXGroupPluginInterface;
import com.openexchange.admin.plugins.OXResourcePluginInterface;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.osgi.ServiceListings;

/**
 * {@link PluginInterfaces} - Plugin interfaces for bundle <i>com.openexchange.admin.plugin.hosting.plugin.hosting</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PluginInterfaces {

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

        ServiceListing<BasicAuthenticatorPluginInterface> basicAuthenticatorPlugins;
        ServiceListing<OXContextPluginInterface> contextPlugins;
        ServiceListing<OXUserPluginInterface> userPlugins;
        ServiceListing<OXGroupPluginInterface> groupPlugins;
        ServiceListing<OXResourcePluginInterface> resourcePlugins;

        public Builder() {
            super();
        }

        public Builder basicAuthenticatorPlugins(ServiceListing<BasicAuthenticatorPluginInterface> serviceListing) {
            this.basicAuthenticatorPlugins = serviceListing;
            return this;
        }

        public Builder contextPlugins(ServiceListing<OXContextPluginInterface> serviceListing) {
            this.contextPlugins = serviceListing;
            return this;
        }

        public Builder userPlugins(ServiceListing<OXUserPluginInterface> serviceListing) {
            this.userPlugins = serviceListing;
            return this;
        }

        public Builder groupPlugins(ServiceListing<OXGroupPluginInterface> serviceListing) {
            this.groupPlugins = serviceListing;
            return this;
        }

        public Builder resourcePlugins(ServiceListing<OXResourcePluginInterface> serviceListing) {
            this.resourcePlugins = serviceListing;
            return this;
        }

        public PluginInterfaces build() {
            return new PluginInterfaces(this);
        }
    }

    // ------------------------------------------------------------------------------------------------------------- //

    private final ServiceListing<BasicAuthenticatorPluginInterface> basicAuthenticatorPlugins;
    private final ServiceListing<OXContextPluginInterface> contextPlugins;
    private final ServiceListing<OXUserPluginInterface> userPlugins;
    private final ServiceListing<OXGroupPluginInterface> groupPlugins;
    private final ServiceListing<OXResourcePluginInterface> resourcePlugins;

    /**
     * Initializes a new {@link PluginInterfaces}.
     */
    PluginInterfaces(final Builder builder) {
        super();
        this.basicAuthenticatorPlugins = null == builder.basicAuthenticatorPlugins ? ServiceListings.<BasicAuthenticatorPluginInterface> emptyList() : builder.basicAuthenticatorPlugins;
        this.contextPlugins = null == builder.contextPlugins ? ServiceListings.<OXContextPluginInterface> emptyList() : builder.contextPlugins;
        this.userPlugins = null == builder.userPlugins ? ServiceListings.<OXUserPluginInterface> emptyList() : builder.userPlugins;
        this.groupPlugins = null == builder.groupPlugins ? ServiceListings.<OXGroupPluginInterface> emptyList() : builder.groupPlugins;
        this.resourcePlugins = null == builder.resourcePlugins ? ServiceListings.<OXResourcePluginInterface> emptyList() : builder.resourcePlugins;
    }

    /**
     * Gets the rank-wise sorted basic authenticator plugins.
     *
     * @return The basic authenticator plugins
     */
    public ServiceListing<BasicAuthenticatorPluginInterface> getBasicAuthenticatorPlugins() {
        return basicAuthenticatorPlugins;
    }

    /**
     * Gets the rank-wise sorted context plugins.
     *
     * @return The context plugins
     */
    public ServiceListing<OXContextPluginInterface> getContextPlugins() {
        return contextPlugins;
    }

    /**
     * Gets the rank-wise sorted user plugins.
     *
     * @return The user plugins
     */
    public ServiceListing<OXUserPluginInterface> getUserPlugins() {
        return userPlugins;
    }

    /**
     * Gets the rank-wise sorted group plugins.
     *
     * @return The group plugins
     */
    public ServiceListing<OXGroupPluginInterface> getGroupPlugins() {
        return groupPlugins;
    }

    /**
     * Gets the rank-wise sorted resource plugins.
     *
     * @return The resource plugins
     */
    public ServiceListing<OXResourcePluginInterface> getResourcePlugins() {
        return resourcePlugins;
    }
}
