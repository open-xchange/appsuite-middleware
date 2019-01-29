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
 * @since v7.10.2
 */
public abstract class AbstractPasswordMech implements PasswordMech {

    protected final static Logger LOG = LoggerFactory.getLogger(AbstractPasswordMech.class);

    /*
     * The salt generator used by this class to create random
     * salts. In a holder class to defer initialization until needed.
     */
    private static class Holder {
        static final SecureRandom saltGenerator = new SecureRandom();
    }

    private final String mechIdentifier;

    private final int hashSize;

    static SecureRandom initSecureRandom() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] generateSeed = secureRandom.generateSeed(20); // use same number of bytes as guard uses
        secureRandom.setSeed(generateSeed);
        return secureRandom;
    }

    /**
     * Initializes a new {@link AbstractPasswordMech}.
     *
     * @param mechIdentifier The identifier of the algorithm
     * @hashSize hashSize The size of the resulting hash
     * @throws OXException
     */
    public AbstractPasswordMech(String mechIdentifier, int hashSize) {
        this.mechIdentifier = mechIdentifier;
        this.hashSize = hashSize;
    }

    @Override
    public final PasswordDetails encode(String password) throws OXException {
        if (Strings.isEmpty(password)) {
            return new PasswordDetails(password, null, getIdentifier(), null);
        }
        return encodePassword(password);
    }

    /**
     * Encodes the given plain password
     *
     * Note: Empty strings will not be validated by the underlying {@link PasswordMech} so these checks have to be done before (see {@link #encode(String)})
     *
     * @param password The password to encode
     * @return {@link PasswordDetails} containing information about the password encoding
     * @throws OXException In case of error
     */
    protected abstract PasswordDetails encodePassword(String password) throws OXException;

    @Override
    public final boolean check(String toCheck, String encoded, byte[] salt) throws OXException {
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
     * Checks the password candidate against the encoded password.
     *
     * Note: Empty strings will not be validated by the underlying {@link PasswordMech} so these checks have to be done before (see {@link #check(String, String, byte[])})
     *
     * @param candidate The plain text candidate (non-empty)
     * @param encoded The encoded password (non-empty)
     * @param salt The salt used while password hashing
     * @return <code>true</code> if the password match
     * @throws OXException In case of error
     */
    protected abstract boolean checkPassword(String candidate, String encoded, byte[] salt) throws OXException;

    @Override
    public String decode(String encodedPassword, byte[] salt) throws OXException {
        throw PasswordMechExceptionCodes.UNSUPPORTED_OPERATION.create(getIdentifier());
    }

    @Override
    public String getIdentifier() {
        return mechIdentifier;
    }

    @Override
    public boolean isExposed() {
        return true;
    }

    protected byte[] getSalt() {
        SecureRandom ng = Holder.saltGenerator;
        byte[] salt = new byte[this.hashSize];
        ng.nextBytes(salt);
        return salt;
    }
}
