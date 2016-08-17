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
