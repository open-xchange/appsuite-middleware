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

/**
 * {@link DownloadParameters} - The parameters extracted from a download URL.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DownloadParameters {

    private final int contextId;
    private final int userId;
    private final String deviceId;
    private final String scenarioId;
    private final String challenge;

    /**
     * Initializes a new {@link DownloadParameters}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param deviceId The device identifier
     * @param scenarioId The scenario identifier
     * @param challenge The challenge
     */
    public DownloadParameters(int userId, int contextId, String deviceId, String scenarioId, String challenge) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.scenarioId = scenarioId;
        this.challenge = challenge;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the device identifier
     *
     * @return The device identifier
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the scenario identifier
     *
     * @return The scenario identifier
     */
    public String getScenarioId() {
        return scenarioId;
    }

    /**
     * Gets the challenge
     *
     * @return The challenge
     */
    public String getChallenge() {
        return challenge;
    }

}
