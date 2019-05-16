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

import com.openexchange.multifactor.AbstractMultifactorDevice;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.provider.totp.impl.MultifactorTotpProvider;

/**
 * {@link TotpMultifactorDevice} represents a TOTP compatible multifactor device
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class TotpMultifactorDevice extends AbstractMultifactorDevice {

    public static final String URL_PARAMETER = "url";
    public static final String BASE64_IMAGE_PARAMETER = "base64Image";
    public static final String SHARED_SECRET_PARAMETER = "sharedSecret";
    public static final String SECRET_CODE_PARAMETER = "secret_code";

    /**
     * Initializes a new {@link TotpMultifactorDeviceOLD}.
     *
     * @param id The unique ID of the device
     * @param name The user friendly name of the device
     * @param secret The share TOTP secret
     */
    public TotpMultifactorDevice(String id, String name, String secret) {
        super(id, MultifactorTotpProvider.NAME, name);
        setSharedSecret(secret);
    }

    /**
     * Initializes a new {@link TotpMultifactorDevice} on base of an existing {@link MultifactorDevice}.
     *
     * @param source The {@link MultifactorDevice} to create the new device from
     */
    public TotpMultifactorDevice(MultifactorDevice source) {
        super(source.getId(),
              source.getProviderName(),
              source.getName(),
              source.getParameters());
        setBackup(source.isBackup());
    }

    /**
     * Gets the device's shared secret
     *
     * @return The shared secret
     */
    public String getSharedSecret() {
        return getParameters() != null ? (String) getParameters().get(SHARED_SECRET_PARAMETER) : null;
    }

    /**
     * Convenience method to add the shared TOTP secret to the device's parameters
     *
     * @param sharedSecret The TOPT shared secret
     * @return this
     */
    public TotpMultifactorDevice setSharedSecret(String sharedSecret) {
        setParameter(SHARED_SECRET_PARAMETER, sharedSecret);
        return this;
    }

    /**
     * Convenience method to add the TOTP URL to the device's parameters
     *
     * @param url The url to add
     * @return this
     */
    public TotpMultifactorDevice setUrl(String url) {
        setParameter(URL_PARAMETER, url);
        return this;
    }

    /**
     * Convenience method to add the TOTP QR-Code to the device's parameters
     *
     * @param qrCode The code as base64 encoded string
     * @return this
     */
    public TotpMultifactorDevice setQRCode(String qrCode) {
        setParameter(BASE64_IMAGE_PARAMETER, qrCode);
        return this;
    }

    /**
     * Convenience method to add the secret TOTP code to the device's paramters
     *
     * @return The secret TOTP code required for authentication
     */
    public String getSecretCode() {
        return getParameter(SECRET_CODE_PARAMETER);
    }

    /**
     * Convenience method to add the secret TOTP code to the device's parameter
     *
     * @param code The code to add
     * @return this
     */
    public TotpMultifactorDevice setSecretCode(String code) {
        setParameter(SECRET_CODE_PARAMETER, code);
        return this;
    }
}
