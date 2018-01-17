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

package com.openexchange.multifactor.provider.totp;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import com.openexchange.exception.OXException;

/**
 * {@link TotpCore} provides basic functionality to verify TOTP tokens
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class TotpCore {

    /**
     * Initializes a new {@link TotpCore}.
     */
    private TotpCore() {
        super();
    }

    /**
     * Internal method to get the next time interval
     *
     * @return The next time interval
     */
    private static long getCurrentInterval() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) / 30; // 30 second intervals
    }

    /**
     * Internal method to calculate the token for a given shared secret and a time interval
     *
     * @param sharedSecret The shared secret to use for calculation
     * @param timeInterval The time interval to use for calculation
     * @return The token calculated from the given shared secret and the time interval
     * @throws OXException
     */
    private static int getHash(byte[] sharedSecret, long timeInterval) throws OXException {
        byte[] hash = null;
        try {
            byte[] challenge = ByteBuffer.allocate(8).putLong(timeInterval).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec macKey = new SecretKeySpec(sharedSecret, "RAW");
            mac.init(macKey);
            hash = mac.doFinal(challenge);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw OXException.general("Unable to generate hash for TOTP login", e);
        }
        return getIntFromHash(hash);
    }

    /**
     * Convert the hash to proper int representation
     *
     * @param h The hash
     * @return The int representation
     */
    private static int getIntFromHash(byte[] h) {
        int offset = h[h.length - 1] & 0xf;
        int toReturn = ((h[offset] & 0x7f) << 24) | ((h[offset + 1] & 0xff) << 16) | ((h[offset + 2] & 0xff) << 8) | (h[offset + 3] & 0xff);
        return toReturn % 1000000;
    }

    /**
     * Check the response against time based hash, +/- 30 seconds
     *
     * @param sharedSecret - Base32 representation of the shared secret
     * @param response
     * @return <code>true</code>, if the given response matches the given TOTP sharedSecret considering a 30 seconds tolerance, <code>false</code> if they don't match
     * @throws OXException
     */
    public static boolean verify(String sharedSecret, String response) throws OXException {
        byte[] secret = new Base32().decode(sharedSecret);
        int resp = 0;
        try {
            resp = Integer.parseInt(response);
        } catch (NumberFormatException e) {
            return false;
        }

        long curInt = getCurrentInterval();
        for (int i = 1; i >= -1; --i) {
            if (resp == getHash(secret, curInt - i)) {
                return true;
            }
        }
        return false;
    }
}
