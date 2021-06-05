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

package com.openexchange.multifactor.provider.totp;

import static com.openexchange.java.Autoboxing.I;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;

/**
 * {@link TotpRegister} creates TOTP secrets and URLs
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class TotpRegister {

    private static final String URL= "url";
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates a new, 20 bytes long, random generated String using a cryptographically secure PRNG (SecureRandom).
     *
     * @return A new random secret
     */
    private String getNewSharedSecret() {
        byte[] newSecret = new byte[20];
        random.nextBytes(newSecret);
        return new Base32().encodeToString(newSecret);
    }

    /**
     * Generate a new secret TOP URL
     *
     * @param secret The secret to include in the URL
     * @param host The host to include in the URL
     * @param name  A meaningfully name to include in the URL
     * @return A generated TOTP URL
     * @throws OXException
     */
    private static String generateUrl(String secret, String host, String name) throws OXException {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("otpauth://totp/");
            sb.append(host);
            sb.append(URLEncoder.encode(":" + name, "UTF-8").replaceAll("\\+", "%20"));
            sb.append("?secret=");
            sb.append(secret);
            sb.append("&issuer=");
            sb.append(host);
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw MultifactorExceptionCodes.UNKNOWN_ERROR.create("Problem creating TOTP URL", e);
        }
    }

    /**
     * Generates a new {@link TotpChallenge}
     *
     * @param request The {@link MultifactorRequest}
     * @param device The {@link MultifactorDevice}
     * @param maxQrSize The maximum allowed length of characters included in the challenge's QR-Code
     * @return A new {@link TotpChallenge}
     * @throws OXException
     */
    public TotpChallenge createChallenge(MultifactorRequest request, MultifactorDevice device, int maxQrSize) throws OXException {
        final String newSharedSecret = getNewSharedSecret();
        String name = device.getName() + "/" + request.getLogin();
        final String url = TotpRegister.generateUrl(newSharedSecret, request.getHost(), name);
        if (url.length() > maxQrSize) {
            throw MultifactorExceptionCodes.INVALID_ARGUMENT_LENGTH.create(URL, I(url.length()), I(maxQrSize));
        }

        String base64Image = "";
        final byte[] image = new QRCodeGenerator().generate(url);
        if (image != null) {
            base64Image = Base64.encodeBase64String(image);
        }

        return new TotpChallenge(device.getId(), newSharedSecret, url, base64Image);
    }
}
