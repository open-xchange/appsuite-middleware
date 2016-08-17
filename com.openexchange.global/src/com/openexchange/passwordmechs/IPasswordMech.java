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

package com.openexchange.passwordmechs;

import com.openexchange.exception.OXException;

/**
 * {@link IPasswordMech}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface IPasswordMech {

    public static final String CRYPT = "{CRYPT}";

    public static final String SHA = "{SHA}";

    public static final String BCRYPT = "{BCRYPT}";

    /**
     * Returns the string representation of the password mechanism identifier
     *
     * @return The identifier
     */
    public String getIdentifier();

    /**
     * Encodes the given string according to this password mechanism and returns the encoded string.
     *
     * @param password The password to encode
     * @return The encoded string
     * @throws OXException
     */
    public String encode(String password) throws OXException;

    /**
     * Decodes the given string according to its password mechanism and returns the decoded string.
     *
     * @param password The password to decode
     * @return The decoded string
     * @throws OXException
     */
    public String decode(String encodedPassword) throws OXException;

    /**
     * Checks if given password matches the encoded string according to this password mechanism.
     *
     * @param toCheck The password to check
     * @param encoded The encoded string to check against
     * @return <code>true</code> if string matches; otherwise <code>false</code>
     * @throws OXException
     */
    public boolean check(String toCheck, String encoded) throws OXException;
}
