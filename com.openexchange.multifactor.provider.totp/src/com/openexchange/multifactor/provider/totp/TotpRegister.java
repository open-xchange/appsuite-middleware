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

    private final int maxQrSize;
    private static final String URL= "url";

    /**
     * Initializes a new {@link TotpRegister}.
     *
     * @param maxQrSize The maximum allowed length of characters included in the challenge's QR-Code
     */
    public TotpRegister(int maxQrSize) {
        this.maxQrSize = maxQrSize;
    }
    /**
     * Creates a new, 20 bytes long, random generated String using a cryptographically secure PRNG (SecureRandom).
     *
     * @return A new random secret
     */
    private static String getNewSharedSecret() {
        SecureRandom random = new SecureRandom();
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
            sb.append(":");
            sb.append(URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20"));
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
     * @return A new {@link TotpChallenge}
     * @throws OXException
     */
    public TotpChallenge createChallenge(MultifactorRequest request, MultifactorDevice device) throws OXException {
        final String newSharedSecret = TotpRegister.getNewSharedSecret();
        final String url = TotpRegister.generateUrl(newSharedSecret, request.getHost(), device.getName());
        if (url.length() > maxQrSize) {
            throw MultifactorExceptionCodes.INVALID_ARGUMENT_LENGTH.create(URL, url.length(), maxQrSize);
        }

        String base64Image = "";
        final byte[] image = new QRCodeGenerator().generate(url);
        if (image != null) {
            base64Image = Base64.encodeBase64String(image);
        }

        return new TotpChallenge(device.getId(), newSharedSecret, url, base64Image);
    }
}
