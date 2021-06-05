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

package com.openexchange.multifactor;

import java.util.Map;

/**
 * {@link DefaultRegistrationChallenge} is a default implementation for a {@link RegistrationChallenge}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class DefaultRegistrationChallenge implements RegistrationChallenge {

    protected final String deviceId;
    protected final Map<String, Object> challenge;

    /**
     * Initializes a new {@link DefaultRegistrationChallenge}.
     *
     * @param deviceId The device id
     * @param challenge The challenge
     */
    public DefaultRegistrationChallenge(String deviceId, Map<String, Object> challenge) {
        super();
        this.deviceId = deviceId;
        this.challenge = challenge;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public Map<String, Object> getChallenge() {
        return challenge;
    }

}
