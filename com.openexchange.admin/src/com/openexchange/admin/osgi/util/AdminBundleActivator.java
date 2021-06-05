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

package com.openexchange.admin.osgi.util;

import com.openexchange.admin.daemons.AdminDaemonService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AdminBundleActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AdminBundleActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link AdminBundleActivator}.
     */
    public AdminBundleActivator() {
        super();
    }

    private Class<?>[] requires() {
        return new Class<?>[] { ConfigurationService.class, AdminDaemonService.class };
    }

    @Override
    protected final Class<?>[] getNeededServices() {
        Class<?>[] required = requires();
        Class<?>[] more = getMoreNeededServices();
        if (null == more || 0 == more.length) {
            return required;
        }

        Class<?>[] needed = new Class<?>[required.length + more.length];
        System.arraycopy(required, 0, needed, 0, required.length);
        System.arraycopy(more, 0, needed, required.length, more.length);
        return needed;
    }

    /**
     * Advertises more needed services beside <code>ConfigurationService</code> and <code>AdminDaemonService</code>.
     *
     * @return More needed services or <code>null</code>
     */
    protected abstract Class<?>[] getMoreNeededServices();

}
