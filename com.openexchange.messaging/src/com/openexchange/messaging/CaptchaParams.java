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

package com.openexchange.messaging;

/**
 * {@link CaptchaParams} - Simple container class for captcha parameters,
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CaptchaParams {

    private String challenge;

    private String response;

    private String address;

    /**
     * Initializes a new {@link CaptchaParams}.
     */
    public CaptchaParams() {
        super();
    }

    /**
     * Gets the challenge.
     *
     * @return The challenge
     */
    public String getChallenge() {
        return challenge;
    }

    /**
     * Sets the challenge.
     *
     * @param challenge The challenge
     */
    public void setChallenge(final String challenge) {
        this.challenge = challenge;
    }

    /**
     * Gets the response string.
     *
     * @return The response string
     */
    public String getResponse() {
        return response;
    }

    /**
     * Sets the response string.
     *
     * @param response The response string.
     */
    public void setResponse(final String response) {
        this.response = response;
    }

    /**
     * Gets the host; either an IP address or a host name.
     *
     * @return The host name
     */
    public String getHost() {
        return address;
    }

    /**
     * Sets the remote address.
     *
     * @param address The remote address
     */
    public void setAddress(final String address) {
        this.address = address;
    }

}
