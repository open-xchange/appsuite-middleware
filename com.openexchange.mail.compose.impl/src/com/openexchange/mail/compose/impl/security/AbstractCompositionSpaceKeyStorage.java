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

package com.openexchange.mail.compose.impl.security;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;

/**
 * {@link AbstractCompositionSpaceKeyStorage} - The abstract class for a composition space key storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public abstract class AbstractCompositionSpaceKeyStorage implements CompositionSpaceKeyStorage {

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractCompositionSpaceKeyStorage}.
     */
    protected AbstractCompositionSpaceKeyStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Obfuscates given string.
     *
     * @param s The string
     * @return The obfuscated string
     * @throws OXException If service is missing
     */
    protected String obfuscate(String s) throws OXException {
        ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
        if (null == obfuscatorService) {
            throw ServiceExceptionCode.absentService(ObfuscatorService.class);
        }
        return obfuscatorService.obfuscate(s);
    }

    /**
     * Un-Obfuscates given string.
     *
     * @param s The obfuscated string
     * @return The plain string
     * @throws OXException If service is missing
     */
    protected String unobfuscate(String s) throws OXException {
        ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
        if (null == obfuscatorService) {
            throw ServiceExceptionCode.absentService(ObfuscatorService.class);
        }
        return obfuscatorService.unobfuscate(s);
    }

    /**
     * The "AES" algorithm.
     */
    private static final String ALGORITHM = "AES";

    /**
     * Creates a random 128-bit AES key
     */
    protected static Key generateRandomKey() throws OXException {
        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the base64-encoded version of specified key.
     *
     * @param key The key to encode
     * @return The base64-encoded version of specified key
     */
    protected static String key2Base64EncodedString(Key key) {
        if (null == key) {
            return null;
        }

        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Gets the key representation for given base64-encoded version of a key.
     *
     * @param encodedKey The base64-encoded version of the key
     * @return The key instance
     */
    protected static Key base64EncodedString2Key(String encodedKey) {
        if (null == encodedKey) {
            return null;
        }

        // Decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);

        // Rebuild key using SecretKeySpec
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

}
