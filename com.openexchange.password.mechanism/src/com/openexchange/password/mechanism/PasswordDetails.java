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

package com.openexchange.password.mechanism;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * {@link PasswordDetails} - Container for password related data
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class PasswordDetails {

    /** The plain password */
    private final String plainPassword;

    /** The password mechanism identifier the password was encrypted with */
    private final String passwordMech;

    /** The salt that was used while encrypting */
    private final byte[] salt;

    /* The encrypted password */
    private final String encodedPassword;

    /**
     * Initializes a new {@link PasswordDetails}.
     * 
     * @param plainPassword The password in plain text (unencoded)
     * @param encodedPassword The encoded password
     * @param passwordMech The password mechanism used to encode the password
     * @param salt The salt used while encoding the password
     */
    public PasswordDetails(String plainPassword, String encodedPassword, String passwordMech, byte[] salt) {
        this.plainPassword = plainPassword;
        this.encodedPassword = encodedPassword;
        this.passwordMech = passwordMech;
        this.salt = salt;
    }

    /**
     * Returns the salt
     * 
     * @return byte[] The salt used to encode the password
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Returns the encoded password
     * 
     * @return String The encoded password
     */
    public String getEncodedPassword() {
        return encodedPassword;
    }

    /**
     * Returns the plain password
     * 
     * @return String The plain (unencoded) password
     */
    public String getPlainPassword() {
        return plainPassword;
    }

    /**
     * Returns the identifier of the password mechanism used to encode the password
     * 
     * @return String The password mechanism used to encode the password
     */
    public String getPasswordMech() {
        return passwordMech;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
