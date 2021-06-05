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

package com.openexchange.multifactor.provider.sms.token;

import java.security.SecureRandom;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.multifactor.TokenCreationStrategy;

/**
 * {@link HOTPTokenCreationStrategy} creates a OTP using SecureRandom
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */

public class HOTPTokenCreationStrategy implements TokenCreationStrategy {

    private final SecureRandom random;

    /**
     * Initializes a new {@link HOTPTokenCreationStrategy}.
     */
    public HOTPTokenCreationStrategy() {
        super();
        random = new SecureRandom();
    }

    /**
     * Internal method to create a random secret
     *
     * @param length The length of the secret to create
     * @return The random secret
     */
    private String createRandomSecret(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Lenght must not be less than 0 (zero)");
        }
        if (length == 0) {
            return "";
        }

        char[] chars = new char[length];
        int bound = 10;
        for (int i = length; i-- > 0;) {
            chars[i] = Strings.charForDigit(random.nextInt(bound));
        }
        return new String(chars);
    }

    @Override
    public String createToken(int length) throws OXException {
        return createRandomSecret(length);
    }

}
