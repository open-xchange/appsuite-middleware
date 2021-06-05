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

package com.openexchange.client.onboarding.download;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;

/**
 * {@link DownloadLinkProvider}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public interface DownloadLinkProvider {

    /**
     * Retrieves a user specific download link to a plist file.
     *
     * @param hostData The data of the host
     * @param userId The user id
     * @param contextId The context id
     * @param scenario The name of the scenario
     * @param device The name of the device
     * @return The download link
     * @throws OXException If link cannot be returned
     */
    String getLink(HostData hostData, int userId, int contextId, String scenario, String device) throws OXException;

    /**
     * Validates the challenge of a download link
     *
     * @param userId The user id of the request
     * @param contextId The context id of the request
     * @param scenario The scenario of the request
     * @param device The device of the request
     * @param challenge The challenge of the request
     * @return true if the challenge is valid, false otherwise
     * @throws OXException If validation fails
     */
    boolean validateChallenge(int userId, int contextId, String scenario, String device, String challenge) throws OXException;

    /**
     * Extracts the parameters from specified URL.
     *
     * @param url The download URL
     * @return The download parameters
     * @throws OXException If parameters cannot be returned
     */
    DownloadParameters getParameter(String url) throws OXException;

}
