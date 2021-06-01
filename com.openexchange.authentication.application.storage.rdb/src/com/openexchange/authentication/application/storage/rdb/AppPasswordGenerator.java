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

package com.openexchange.authentication.application.storage.rdb;

import java.security.SecureRandom;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;

/**
 * {@link AppPasswordGenerator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class AppPasswordGenerator {

    private static final char[] PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] PASSWORD_FORMAT = "xxxx-xxxx-xxxx-xxxx".toCharArray();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("[a-z]{4}-[a-z]{4}-[a-z]{4}-[a-z]{4}");

    private final SecureRandom random;

    /**
     * Initializes a new {@link AppPasswordGenerator}.
     */
    public AppPasswordGenerator() {
        super();
        this.random = new SecureRandom();
    }

    /**
     * Gets a new randomly generated password
     *
     * @return String of new password
     */
    public String generateRandomPassword() {
        StringBuilder stringBuilder = new StringBuilder(PASSWORD_FORMAT.length);
        for (char c : PASSWORD_FORMAT) {
            if ('x' == c) {
                stringBuilder.append(PASSWORD_CHARS[random.nextInt(PASSWORD_CHARS.length)]);
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets a value indicating whether the supplied password is in the expected format as produced by this generator or not.
     *
     * @param password The password to check
     * @return <code>true</code> if the password is in the expected format, <code>false</code>, otherwise
     */
    public boolean isInExpectedFormat(String password) {
        return Strings.isNotEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

}
