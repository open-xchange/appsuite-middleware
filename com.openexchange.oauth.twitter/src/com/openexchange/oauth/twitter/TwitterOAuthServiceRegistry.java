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

package com.openexchange.oauth.twitter;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.server.ServiceLookup;

/**
 * {@link TwitterOAuthServiceRegistry} - Container class for the service registry of <i>com.openexchange.oauth.twitter</i> bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterOAuthServiceRegistry {

    private static final AtomicReference<ServiceLookup> SERVICE_LOOKUP = new AtomicReference<ServiceLookup>();

    public static ServiceLookup getServiceLookup() {
        return SERVICE_LOOKUP.get();
    }

    public static void setServiceLookup(final ServiceLookup serviceLookup) {
        SERVICE_LOOKUP.set(serviceLookup);
    }

    /**
     * Initializes a new {@link TwitterOAuthServiceRegistry}.
     */
    private TwitterOAuthServiceRegistry() {
        super();
    }

}
