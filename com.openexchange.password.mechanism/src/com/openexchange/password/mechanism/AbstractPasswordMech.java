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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.password.mechanism.exceptions.PasswordMechExceptionCodes;

/**
 * {@link AbstractPasswordMech}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public abstract class AbstractPasswordMech implements IPasswordMech {

    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractPasswordMech.class);

    private final String mechIdentifier;

    /**
     * Initializes a new {@link AbstractPasswordMech}.
     * 
     * @param mechIdentifier The identifier of the algorithm
     */
    public AbstractPasswordMech(String mechIdentifier) {
        this.mechIdentifier = mechIdentifier;
    }

    @Override
    public boolean check(String toCheck, String encoded, byte[] salt) throws OXException {
        if ((Strings.isEmpty(toCheck)) && (Strings.isEmpty(encoded))) {
            return true;
        } else if ((Strings.isEmpty(toCheck)) && (Strings.isNotEmpty(encoded))) {
            return false;
        } else if ((Strings.isNotEmpty(toCheck)) && (Strings.isEmpty(encoded))) {
            return false;
        }
        return checkPassword(toCheck, encoded, salt);
    }

    /**
     * Checks the password candidate against the encoded password
     * 
     * @param candidate The plain text candidate
     * @param encoded The encoded password
     * @param salt The salt used while password hashing
     * @return <code>true</code> if the password match
     * @throws OXException In case of error
     */
    public abstract boolean checkPassword(String candidate, String encoded, byte[] salt) throws OXException;

    @Override
    public String decode(String encodedPassword, byte[] salt) throws OXException {
        throw PasswordMechExceptionCodes.UNSUPPORTED_OPERATION.create(getIdentifier());
    }

    @Override
    public String getIdentifier() {
        return mechIdentifier;
    }

    @Override
    public boolean expose() {
        return true;
    }

    protected byte[] getSalt() throws OXException {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[getHashLength()];
            sr.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Error retrieving SecureRandom instance", e);
            throw PasswordMechExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}
