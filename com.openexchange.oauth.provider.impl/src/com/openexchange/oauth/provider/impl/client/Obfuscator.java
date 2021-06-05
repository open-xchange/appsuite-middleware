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

package com.openexchange.oauth.provider.impl.client;

import static com.openexchange.java.Strings.isEmpty;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * Obfuscator class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Obfuscator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Obfuscator.class);

    private final String obfuscationKey;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link Obfuscator}.
     *
     * @param obfuscationKey The key used to (un)obfuscate secret data
     */
    public Obfuscator(String obfuscationKey, ServiceLookup services) {
        super();
        this.services = services;
        this.obfuscationKey = obfuscationKey;
    }

    public String obfuscate(String string) {
        if (isEmpty(string)) {
            return string;
        }
        try {
            return services.getService(CryptoService.class).encrypt(string, obfuscationKey);
        } catch (OXException e) {
            LOG.error("Could not obfuscate string", e);
            return string;
        }
    }

    public String unobfuscate(String string) {
        if (isEmpty(string)) {
            return string;
        }
        try {
            return services.getService(CryptoService.class).decrypt(string, obfuscationKey);
        } catch (OXException e) {
            LOG.error("Could not unobfuscate string", e);
            return string;
        }
    }

}
