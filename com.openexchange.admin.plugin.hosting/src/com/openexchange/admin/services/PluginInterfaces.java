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

package com.openexchange.admin.services;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.OXGroupPluginInterface;
import com.openexchange.admin.plugins.OXResourcePluginInterface;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.osgi.ServiceListings;

/**
 * {@link PluginInterfaces} - Plugin interfaces for bundle <i>com.openexchange.admin.plugin.hosting</i>.
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
    private PluginInterfaces(final Builder builder) {
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
