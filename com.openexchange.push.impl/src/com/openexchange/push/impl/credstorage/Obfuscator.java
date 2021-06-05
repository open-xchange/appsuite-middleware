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

package com.openexchange.push.impl.credstorage;

import static com.openexchange.java.Strings.isEmpty;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.push.credstorage.Credentials;
import com.openexchange.push.credstorage.DefaultCredentials;
import com.openexchange.push.impl.credstorage.osgi.CredStorageServices;

/**
 * Obfuscator class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Obfuscator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Obfuscator.class);

    private final String obfuscationKey;

    /**
     * Initializes a new {@link Obfuscator}.
     *
     * @param obfuscationKey The key used to (un)obfuscate secret data
     */
    public Obfuscator(String obfuscationKey) {
        super();
        this.obfuscationKey = obfuscationKey;
    }

    /**
     * Obfuscates given credentials
     *
     * @param credentials The credentials
     * @return The obfuscated credentials or <code>null</code>
     */
    public Credentials obfuscateCredentials(Credentials credentials) {
        if (null == credentials) {
            return null;
        }
        DefaultCredentials.Builder defaultCredentials = DefaultCredentials.builder().copyFromCredentials(credentials);
        defaultCredentials.withPassword(obfuscate(credentials.getPassword()));
        return defaultCredentials.build();
    }

    private String obfuscate(String string) {
        if (isEmpty(string)) {
            return string;
        }
        try {
            CryptoService cryptoService = CredStorageServices.requireService(CryptoService.class);
            return cryptoService.encrypt(string, obfuscationKey);
        } catch (OXException e) {
            LOG.error("Could not obfuscate string", e);
            return string;
        }
    }

    /**
     * Un-Obfuscates given credentials
     *
     * @param credentials The credentials
     * @return The un-obfuscated credentials or <code>null</code>
     */
    public Credentials unobfuscateCredentials(Credentials credentials) {
        if (null == credentials) {
            return null;
        }
        DefaultCredentials.Builder defaultCredentials = DefaultCredentials.builder().copyFromCredentials(credentials);
        defaultCredentials.withPassword(unobfuscate(credentials.getPassword()));
        return defaultCredentials.build();
    }

    private String unobfuscate(String string) {
        if (isEmpty(string)) {
            return string;
        }
        try {
            CryptoService cryptoService = CredStorageServices.requireService(CryptoService.class);
            return cryptoService.decrypt(string, obfuscationKey);
        } catch (OXException e) {
            LOG.error("Could not unobfuscate string", e);
            return string;
        }
    }

}
