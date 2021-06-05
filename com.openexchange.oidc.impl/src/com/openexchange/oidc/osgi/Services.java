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

package com.openexchange.oidc.osgi;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * Contains all needed Services of this bundle.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class Services {

    public static AtomicReference<ServiceLookup> services = new AtomicReference<ServiceLookup>();

    private Services() {
        super();
    }

    public static void setServices(ServiceLookup lookup) {
        services.set(lookup);
    }

    public static <T> T getService(Class<T> klass) {
        final ServiceLookup serviceLookup = services.get();
        if (serviceLookup == null) {
            throw new IllegalStateException("Missing ServiceLookup instance. Bundle \"com.openexchange.oidc.impl\" not started?");
        }
        return serviceLookup.getService(klass);
    }

    public static <T> T getServiceSafe(Class<T> klass) throws OXException {
        final ServiceLookup serviceLookup = services.get();
        if (serviceLookup == null) {
            throw new IllegalStateException("Missing ServiceLookup instance. Bundle \"com.openexchange.oidc.impl\" not started?");
        }
        return serviceLookup.getServiceSafe(klass);
    }

    public static <T> T getOptionalService(Class<T> klass) {
        final ServiceLookup serviceLookup = services.get();
        return serviceLookup != null ? services.get().getOptionalService(klass) : null;
    }
}
