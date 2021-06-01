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

package com.openexchange.multifactor.provider.backupString.impl;

import java.security.SecureRandom;

/**
 * {@link BackupStringCodeGenerator} generates a random text code used as multifactor "backup-String" recovery
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class BackupStringCodeGenerator {

    private static final char[] CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Initializes a new {@link BackupStringCodeGenerator}.
     */
    private BackupStringCodeGenerator() {
        super();
    }

    private static char getChar(SecureRandom random) {
        return CHARACTERS[random.nextInt(CHARACTERS.length)];
    }

    /**
     * Generates a random string over an internal alphabet [A-Z,0-1] using a cryptographically secure PRNG
     *
     * @param length The length of the random string to create
     * @return The random string
     */
    public static String generateString(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Lenght must not be less than 0 (zero)");
        }
        if (length == 0) {
            return "";
        }

        char[] chars = new char[length];
        for (int i = length; i-- > 0;) {
            chars[i] = getChar(RANDOM);
        }
        return new String(chars);
    }

}
