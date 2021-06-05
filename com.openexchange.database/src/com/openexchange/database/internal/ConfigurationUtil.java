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

package com.openexchange.database.internal;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConfigurationUtil}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ConfigurationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUtil.class);

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

    /**
     * Matches if two {@link Properties} can be considered equal
     * 
     * @param p1 The first {@link Properties}
     * @param p2 The second {@link Properties}
     * @return <code>true</code> if both properties contain equal objects
     *         <code>false</code> otherwise
     */
    public static boolean matches(Properties p1, Properties p2) {
        if (p1.size() != p2.size()) {
            return false;
        }
        for (Entry<Object, Object> f : p1.entrySet()) {
            if (false == p2.contains(f.getKey())) {
                return false;
            }
            Object p2Value = p2.get(f.getKey());
            if (null == p2Value) {
                if (null != f.getValue()) {
                    return false;
                }
            } else {
                if (false == p2Value.equals(f.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

}
