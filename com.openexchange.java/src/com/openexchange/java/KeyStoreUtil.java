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

package com.openexchange.java;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link KeyStoreUtil}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class KeyStoreUtil {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreUtil.class);

    /**
     * Calculates the hashCode for a KeyStore
     * 
     * @param store The store to get the value for
     * @return The hashCode
     */
    public static int getHashSum(KeyStore store) {
        int hashCode = 0;
        if (null == store) {
            return hashCode;
        }
        hashCode += store.getType().hashCode();
        Enumeration<String> aliases;
        try {
            aliases = store.aliases();
            while (aliases.hasMoreElements()) {
                String nextElement = aliases.nextElement();
                hashCode += nextElement.hashCode();
                Certificate certificate = store.getCertificate(nextElement);
                hashCode += certificate.hashCode();
            }
        } catch (KeyStoreException e) {
            LOGGER.debug("Not initialized", e);
        }

        return hashCode;
    }

    /**
     * Compares two key stores
     * 
     * @param k1 The one {@link KeyStore}
     * @param k2 The other {@link KeyStore}
     * @return See {@link Comparable#compareTo(Object)}
     */
    public static int compare(KeyStore k1, KeyStore k2) {
        int hashSum = getHashSum(k1);
        int hashSum2 = getHashSum(k2);

        if (hashSum == hashSum2) {
            return 0;
        }

        return hashSum > hashSum2 ? 1 : -1;
    }

}