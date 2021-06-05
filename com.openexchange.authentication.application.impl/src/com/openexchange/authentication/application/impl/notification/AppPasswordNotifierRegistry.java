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

package com.openexchange.authentication.application.impl.notification;

import com.openexchange.authentication.application.ApplicationPassword;
import com.openexchange.authentication.application.notification.AppPasswordNotifier;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;

/**
 * {@link AppPasswordNotifierRegistry} Registry of notification services
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordNotifierRegistry {

    private final ServiceListing<AppPasswordNotifier> notifiers;

    public AppPasswordNotifierRegistry(ServiceListing<AppPasswordNotifier> notifiers) {
        this.notifiers = notifiers;
    }

    /**
     * Notify each notification service of the added password
     *
     * @param password the application password
     * @throws OXException if an error is occurred
     */
    public void notifyAddPassword(ApplicationPassword password) throws OXException {
        for (AppPasswordNotifier notify : notifiers.getServiceList()) {
            notify.notifyAddPassword(password);
        }
    }

    /**
     * Notify each notification service of the removed password
     *
     * @param password The application password
     * @throws OXException if an error is occurred
     */
    public void notifyRemovePassword(String passwordId) throws OXException {
        for (AppPasswordNotifier notify : notifiers.getServiceList()) {
            notify.notifyRemovePassword(passwordId);
        }
    }
}
