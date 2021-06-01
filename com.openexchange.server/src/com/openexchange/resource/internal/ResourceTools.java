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

package com.openexchange.resource.internal;

import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link ResourceTools} - Utility methods for resource module
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceTools {

    /**
     * Initializes a new {@link ResourceTools}
     */
    private ResourceTools() {
        super();
    }

    private static final Pattern PATTERN_ALLOWED_CHARS = Pattern.compile("[\\S ]+");

    /**
     * Checks if specified resource identifier contains invalid characters.
     * <p>
     * Valid characters are:<br>
     * &quot;<i>&nbsp; abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ -+.%$@<i>&quot;
     *
     * @param identifier The resource identifier to check
     * @return <code>true</code> if specified resource identifier only consists of valid characters; otherwise <code>false</code>
     */
    public static boolean validateResourceIdentifier(final String identifier) {
        /*
         * Check for allowed chars: abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@
         */
        return PATTERN_ALLOWED_CHARS.matcher(identifier).matches();
    }

    // private static final Pattern PATTERN_VALID_EMAIL =
    // Pattern.compile("[$%\\.+a-zA-Z0-9_-]+@[\\.a-zA-Z0-9_-]+");

    /**
     * Checks if specified resource email address' notation is valid
     *
     * @param emailAddress The resource email address to check
     * @return <code>true</code> if specified resource email address is valid; otherwise <code>false</code>
     */
    public static boolean validateResourceEmail(final String emailAddress) {
        /*
         * Validate e-mail with InternetAddress class from JavaMail API
         */
        try {
            new QuotedInternetAddress(emailAddress, true).validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
