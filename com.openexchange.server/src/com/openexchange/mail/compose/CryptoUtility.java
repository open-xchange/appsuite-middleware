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

package com.openexchange.mail.compose;

import java.io.InputStream;
import java.security.Key;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CryptoUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CryptoUtility {

    /**
     * Initializes a new {@link CryptoUtility}.
     */
    private CryptoUtility() {
        super();
    }

    /**
     * Encrypts specified string with given key.
     *
     * @param toEncrypt The string to encrypt
     * @param key The key
     * @param cryptoService The crypto service to use
     * @return The encrypted string as Base64 encoded string
     * @throws OXException If string encryption fails
     */
    public static String encrypt(String toEncrypt, Key key, CryptoService cryptoService) throws OXException {
        if (Strings.isEmpty(toEncrypt)) {
            return toEncrypt;
        }

        return cryptoService.encrypt(toEncrypt, key);
    }

    /**
     * Decrypts specified encrypted string with given key.
     *
     * @param encryptedString The Base64 encoded encrypted string
     * @param key The key
     * @param cryptoService The crypto service to use
     * @return The decrypted string
     * @throws OXException If string decryption fails
     * @see CryptoErrorMessage#BadPassword
     */
    public static String decrypt(String encryptedString, Key key, CryptoService cryptoService) throws OXException {
        if (Strings.isEmpty(encryptedString)) {
            return encryptedString;
        }

        return cryptoService.decrypt(encryptedString, key);
    }

    /**
     * Gets the encrypting input stream for given stream using specified key.
     *
     * @param in The stream to encrypt
     * @param key The key
     * @return The encrypting input stream
     * @throws OXException If encrypting input stream cannot be returned
     */
    public static InputStream encryptingStreamFor(InputStream in, Key key, CryptoService cryptoService) throws OXException {
        if (null == in) {
            return null;
        }

        return cryptoService.encryptingStreamFor(in, key);
    }

    /**
     * Gets the decrypting input stream for given stream using specified key.
     *
     * @param in The stream to decrypt
     * @param key The key
     * @return The decrypting input stream
     * @throws OXException If decrypting input stream cannot be returned
     */
    public static InputStream decryptingStreamFor(InputStream in, Key key, CryptoService cryptoService) throws OXException {
        if (null == in) {
            return null;
        }

        return cryptoService.decryptingStreamFor(in, key);
    }

    /**
     * Checks whether encryption is needed for specified session.
     * <p>
     * Currently encryption is needed for session-associated user when
     * <ul>
     * <li>Property "com.openexchange.mail.compose.security.encryptionEnabled" is set to "true" (default)</li>
     * <li>Capability "guard" is available</li>
     * </ul>
     *
     * @param session The session
     * @param services The service look-up providing <code>CapabilityService</code> and <code>ConfigViewFactory</code> services
     * @return <code>true</code> if encryption is needed; otherwise <code>false</code>
     * @throws OXException If need for encryption cannot be checked
     */
    public static boolean needsEncryption(Session session, ServiceLookup services) throws OXException {
        return isEncryptionEnabled(session, services) && getCapabilitySet(session, services).contains("guard");
    }

    private static CapabilitySet getCapabilitySet(Session session, ServiceLookup services) throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }
        return capabilityService.getCapabilities(session);
    }

    private static boolean isEncryptionEnabled(Session session, ServiceLookup services) throws OXException {
        boolean defaultValue = true;

        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            return defaultValue;
        }

        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        return ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.mail.compose.security.encryptionEnabled", defaultValue, view);
    }

}
