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

package com.openexchange.java.util;

import java.util.regex.Pattern;
import com.openexchange.java.Strings;

/**
 * {@link MsisdnCheck} - Checks for valid MSISDN numbers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MsisdnCheck {

    /**
     * Initializes a new {@link MsisdnCheck}.
     */
    private MsisdnCheck() {
        super();
    }

    private static final Pattern PATTERN_VALIDATE = Pattern.compile("(^\\+[0-9]{2}|^\\+[0-9]{2}\\(0\\)|^\\(\\+[0-9]{2}\\)\\(0\\)|^00[0-9]{2}|^0)([0-9]{9}$|[0-9\\-\\s]{10}$)");

    /**
     * Validates specified MSISDN number.
     * <p>
     * Matches:
     * <ul>
     * <li><code>+31235256677</code></li>
     * <li><code>+31(0)235256677</code></li>
     * <li><code>023-5256677</code></li>
     * </ul>
     *
     * @param number The possible MSISDN number to validate
     * @return <code>true</code> if valid MSISDN number; otherwise <code>false</code>
     */
    public static boolean validate(final String number) {
        if (com.openexchange.java.Strings.isEmpty(number)) {
            return false;
        }
        return PATTERN_VALIDATE.matcher(number).matches();
    }

    /**
     * Checks if the given MSISDN number only consists of digits.
     *
     * @param number the mobile phone number to check
     * @return returns either "invalid" if the given number is not valid (contains non-digits) or the phone number in the format xxyyyzzzzzzzz
     **/
    public static boolean checkMsisdn(final String number) {
        if (com.openexchange.java.Strings.isEmpty(number)) {
            return false;
        }
        String num = number;
        {
            final int pos = num.indexOf('/');
            if (pos > 0) {
                num = num.substring(0, pos);
            }
        }
        num = cleanup(num);
        final int len = num.length();
        boolean isDigit = true;
        for (int i = 0; isDigit && i < len; i++) {
            isDigit = Strings.isDigit(num.charAt(i));
        }
        return isDigit;
    }

    private static final Pattern PATTERN_CLEANUP = Pattern.compile("[+()/ ]");

    /**
     * Cleans-up specified MSISDN number.
     *
     * @param number The MSISDN number
     * @return The cleaned-up MSISDN number
     */
    public static String cleanup(String number) {
        return PATTERN_CLEANUP.matcher(number).replaceAll("");
    }
}
