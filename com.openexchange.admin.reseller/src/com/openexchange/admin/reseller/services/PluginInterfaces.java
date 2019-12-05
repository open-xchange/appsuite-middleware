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
