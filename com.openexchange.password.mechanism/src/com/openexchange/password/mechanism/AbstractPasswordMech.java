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
