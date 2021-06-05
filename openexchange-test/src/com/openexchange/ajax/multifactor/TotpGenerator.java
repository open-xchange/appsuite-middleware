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

package com.openexchange.ajax.multifactor;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import com.openexchange.exception.OXException;

/**
 * {@link TotpGenerator}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.1
 */
public class TotpGenerator {

    private long getCurrentInterval() {
        Calendar calendar = GregorianCalendar.getInstance();
        long curTimeSec = calendar.getTimeInMillis() / 1000;
        return curTimeSec / 30;  // 30 second intervals
    }

    private int getIntFromHash(byte[] h) {
        int offset = h[h.length - 1] & 0xf;
        int toReturn = ((h[offset] & 0x7f) << 24) | ((h[offset + 1] & 0xff) << 16) | ((h[offset + 2] & 0xff) << 8) | (h[offset + 3] & 0xff);
        return toReturn % 1000000;
    }

    public int create(String sharedSecret) throws OXException {
        byte[] rawSharedSecret = new Base32().decode(sharedSecret);
        return create(rawSharedSecret, getCurrentInterval());
    }

    public int create(byte[] sharedSecret, long timeInterval) throws OXException {
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
}
