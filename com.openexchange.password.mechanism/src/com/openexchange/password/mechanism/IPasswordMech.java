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

import com.openexchange.exception.OXException;

/**
 * {@link IPasswordMech}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface IPasswordMech {

    /**
     * Returns the origin string representation of the password mechanism identifier
     *
     * @return The identifier
     */
    String getIdentifier();

    /**
     * Returns if the password mechanism should be exposed or just used internally.
     * 
     * @return <code>true</code> if the password mechanism should be exposed. Otherwise <code>false</code>
     */
    boolean expose();

    /**
     * Encodes the given string according to this password mechanism and returns the encoded string.
     *
     * @param password The password to encode
     * @return {@link PasswordDetails} containing details about the generation result
     * @throws OXException
     */
    PasswordDetails encode(String password) throws OXException;

    /**
     * Decodes the given string according to its password mechanism and returns the decoded string.
     *
     * @param password The password to decode
     * @param salt The salt used for encoding or <code>null</code> if no salt was used while encoding
     * @return The decoded string
     * @throws OXException
     */
    String decode(String encodedPassword, byte[] salt) throws OXException;

    /**
     * Checks if given password matches the encoded string according to this password mechanism.
     *
     * @param toCheck The password to check
     * @param encoded The encoded string to check against
     * @param salt The salt used for encoding or <code>null</code> if no salt was used while encoding
     * @return <code>true</code> if string matches; otherwise <code>false</code>
     * @throws OXException
     */
    boolean check(String toCheck, String encoded, byte[] salt) throws OXException;

    /**
     * Character length of the resulting hash
     * 
     * @return int defining the length of the resulting password hash
     */
    int getHashLength();
}
