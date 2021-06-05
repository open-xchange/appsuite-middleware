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

package com.openexchange.password.mechanism.impl.algorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.openexchange.java.Charsets;

/**
 * {@link SHACrypt}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - moved from global & update to new versions
 * @since v7.10.2
 */
public enum SHACrypt {

    // -------------------------------------------------------------------

    /**
     * SHA-1 algorithm.
     *
     * @deprecated Use SHA-256 to generate new password instead
     */
    @Deprecated
    SHA1("{SHA}", "SHA"),
    /**
     * SHA-256 algorithm
     */
    SHA256("{SHA-256}", "SHA-256"),
    /**
     * SHA-512 algorithm.
     * Note: Might not run on all JVMs.
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html">MessageDigest</a>
     *
     */
    SHA512("{SHA-512}", "SHA-512"),
    ;

    private final String identifier;
    private final String algorithm;

    private SHACrypt(String lIdentifier, String algorithm) {
        this.identifier = lIdentifier;
        this.algorithm = algorithm;
    }

    public String makeSHAPasswd(String raw) throws NoSuchAlgorithmException {
        return makeSHAPasswd(raw, null);
    }

    public String makeSHAPasswd(String raw, byte[] salt) throws NoSuchAlgorithmException {
        final MessageDigest sha = MessageDigest.getInstance(algorithm);
        if (null != salt) {
            sha.update(salt);
        }
        sha.update(raw.getBytes(com.openexchange.java.Charsets.UTF_8));
        final byte[] hash = sha.digest();
        return Charsets.toAsciiString(org.apache.commons.codec.binary.Base64.encodeBase64(hash));
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

}
