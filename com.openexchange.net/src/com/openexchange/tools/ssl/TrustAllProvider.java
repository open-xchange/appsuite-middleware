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

package com.openexchange.tools.ssl;

import java.security.Provider;

/**
 * Provider for registering the TrustAllManagerFactorySpi in the Java platform.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TrustAllProvider extends Provider {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 5201461401969086130L;

    /**
     * Default constructor.
     */
    public TrustAllProvider() {
        super("TrustAllProvider", 1.0, "Trust all certificates");
        put("TrustManagerFactory.TrustAllCertificates", TrustAllManagerFactory.class.getName());
    }
}
