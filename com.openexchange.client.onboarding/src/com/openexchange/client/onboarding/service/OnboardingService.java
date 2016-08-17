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

package com.openexchange.client.onboarding.service;

import java.util.Collection;
import java.util.List;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.DeviceAwareScenario;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingRequest;
import com.openexchange.client.onboarding.ResultObject;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link OnboardingService} - The on-boarding service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
@SingletonService
public interface OnboardingService {

    /**
     * Gets all currently registered {@link OnboardingProvider providers}.
     *
     * @return All providers
     * @throws OXException If providers cannot be returned
     */
    Collection<OnboardingProvider> getAllProviders() throws OXException;

    /**
     * Gets the specified {@link OnboardingProvider provider}.
     *
     * @return The specified provider
     * @throws OXException If provider cannot be returned
     */
    OnboardingProvider getProvider(String id) throws OXException;

    /**
     * Gets the currently available {@link OnboardingProvider providers} for the session-associated user.
     *
     * @param session The session
     * @return The currently available providers
     * @throws OXException If providers cannot be returned
     */
    Collection<OnboardingProvider> getAvailableProvidersFor(Session session) throws OXException;

    /**
     * Gets the on-boarding view for specified session
     *
     * @param session The session
     * @return The on-boarding view
     * @throws OXException If view cannot be returned
     */
    OnboardingView getViewFor(Session session) throws OXException;

    /**
     * Gets the scenario by specified identifier
     *
     * @param scenarioId The scenario identifier
     * @param session The session
     * @return The scenario
     * @throws OXException If scenario cannot be returned
     */
    Scenario getScenario(String scenarioId, Session session) throws OXException;

    /**
     * Checks if specified scenario is capability- / permission-wise available to given user.
     *
     * @param scenarioId The scenario identifier
     * @param session The session
     * @return <code>true</code> if available; otherwise <code>false</code>
     * @throws OXException If availability cannot be checked
     */
    boolean isAvailableFor(String scenarioId, Session session) throws OXException;

    /**
     * Gets the device-aware scenario by specified identifier
     *
     * @param scenarioId The scenario identifier
     * @param device The associated device
     * @param session The session
     * @return The device-aware scenario
     * @throws OXException If scenario cannot be returned
     */
    DeviceAwareScenario getScenario(String scenarioId, Device device, Session session) throws OXException;

    /**
     * Gets the device-aware scenario by specified identifier
     *
     * @param scenarioId The scenario identifier
     * @param device The associated device
     * @param userId The user id
     * @param contextId The context id
     * @return The device-aware scenario
     * @throws OXException If scenario cannot be returned
     */
    DeviceAwareScenario getScenario(String scenarioId, Device device, int userId, int contextId) throws OXException;

    /**
     * Gets the device-aware scenarios for specified device.
     *
     * @param device The device
     * @param session The session
     * @return The scenarios for specified device
     * @throws OXException If scenarios cannot be returned
     */
    List<DeviceAwareScenario> getScenariosFor(Device device, Session session) throws OXException;

    /**
     * Executes the denoted scenario
     *
     * @param request The on-boarding request
     * @param session The session
     * @return The result object
     * @throws OXException If scenario cannot be returned
     */
    ResultObject execute(OnboardingRequest request, Session session) throws OXException;

}
