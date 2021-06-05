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

package com.openexchange.recaptcha;

/**
 * {@link ReCaptchaService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public interface ReCaptchaService {

    /**
     * Gets the configured HTML snippet to display the captcha
     * @return
     */
    public String getHTML();

    /**
     * Checks if the entered string matches the displayd captcha challenge.
     *
     * @param address The remote address eg. request.getRemoteAddr()
     * @param challenge The challenge id
     * @param response The String entered by the user
     * @param strict If true, the method returns false if any problem occurs (Service down, no connection, etc.).
     *               If false, the method returns true in such cases.
     * @return
     */
    public boolean check(String address, String challenge, String response, boolean strict);

}
