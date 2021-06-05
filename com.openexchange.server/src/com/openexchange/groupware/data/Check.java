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

package com.openexchange.groupware.data;

import org.jdom2.Verifier;

/**
 * This class contains methods for checking data.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Check {

    /**
     * Prevent instantiation.
     */
    private Check() {
        super();
    }

    /**
     * This method checks a string if it contains invalid characters that can't
     * be used with one of the interfaces.
     * @param check string to check.
     * @return <code>null</code> if the string does not contain invalid
     * characters or a message what is wrong with the data.
     */
    public static String containsInvalidChars(final String check) {
        if (null == check) {
            return check;
        }
        return Verifier.checkCharacterData(check);
    }
}
