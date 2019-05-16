/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
