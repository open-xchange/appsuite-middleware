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

package com.openexchange.password.mechanism.impl.mech;

import java.security.NoSuchAlgorithmException;
import com.openexchange.exception.OXException;
import com.openexchange.password.mechanism.AbstractPasswordMech;
import com.openexchange.password.mechanism.PasswordDetails;
import com.openexchange.password.mechanism.exceptions.PasswordMechExceptionCodes;
import com.openexchange.password.mechanism.impl.algorithm.SHACrypt;

/**
 * {@link SHAMech}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> moved
 * @since v7.10.2
 */
public class SHAMech extends AbstractPasswordMech {

    private final SHACrypt crypt;

    /**
     * Initializes a new {@link SHAMech}.
     *
     * @param crypt The {@link SHACrypt} to use
     */
    public SHAMech(SHACrypt crypt) {
        super(crypt.getIdentifier(), getHashLength(crypt));
        this.crypt = crypt;
    }

    @Override
    public PasswordDetails encodePassword(String str) throws OXException {
        try {
            byte[] salt = getSalt();
            return new PasswordDetails(str, crypt.makeSHAPasswd(str, salt), getIdentifier(), salt);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error encrypting password according to SHA mechanism", e);
            throw PasswordMechExceptionCodes.UNSUPPORTED_ENCODING.create(e, e.getMessage());
        }
    }

    @Override
    public boolean checkPassword(String candidate, String encoded, byte[] salt) throws OXException {
        try {
            if (salt == null) {
                return crypt.makeSHAPasswd(candidate).equals(encoded);
            }
            return crypt.makeSHAPasswd(candidate, salt).equals(encoded);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error checking password according to SHA mechanism", e);
            throw PasswordMechExceptionCodes.UNSUPPORTED_ENCODING.create(e, e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    private static int getHashLength(SHACrypt crypt) {
        switch (crypt) {
            case SHA1:
                return 32;
            case SHA256:
                return 64;
            default:
                return 128;
        }
    }
}
