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

package com.openexchange.file.storage.composition.crypto;

import java.util.EnumSet;

/**
 * {@link CryptographyMode} specifies a group of cryptographic "actions".
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.3
 */
public enum CryptographyMode {

    ENCRYPT,
    DECRYPT,
    SIGN,
    VERIFY;

    /**
     * Simple factory method to create an EnumSet from a given string.
     *
     * @param string The case insensitive string to create the EnumSet for. For example <code>"Encrypt"</code> or <code>"EncryptDecrypt"</code>.
     * @return The enum set parsed from the given string.
     */
    public static EnumSet<CryptographyMode> createSet(String string) {
        EnumSet<CryptographyMode> cryptMode = EnumSet.noneOf(CryptographyMode.class);
        if (string != null) {
            string = string.toUpperCase();
            if (string.contains(CryptographyMode.ENCRYPT.name())) {
                cryptMode.add(ENCRYPT);
            }
            if (string.contains(CryptographyMode.DECRYPT.name())) {
                cryptMode.add(DECRYPT);
            }
            if (string.contains(CryptographyMode.SIGN.name())) {
                cryptMode.add(SIGN);
            }
            if (string.contains(CryptographyMode.VERIFY.name())) {
                cryptMode.add(VERIFY);
            }
        }
        return cryptMode;
    }
}
