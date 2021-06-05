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
