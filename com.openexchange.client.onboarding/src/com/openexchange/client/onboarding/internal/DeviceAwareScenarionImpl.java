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

package com.openexchange.client.onboarding.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.client.onboarding.CompositeId;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.DeviceAwareScenario;
import com.openexchange.client.onboarding.Icon;
import com.openexchange.client.onboarding.Link;
import com.openexchange.client.onboarding.OnboardingAction;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingType;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DeviceAwareScenarionImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DeviceAwareScenarionImpl implements DeviceAwareScenario {

    private final String id;
    private final boolean enabled;
    private final Scenario scenario;
    private final Device device;
    private final CompositeId compositeId;
    private final List<OnboardingAction> actions;
    private final Collection<String> missingCapabilities;

    /**
     * Initializes a new {@link DeviceAwareScenarionImpl}.
     */
    public DeviceAwareScenarionImpl(Scenario scenario, boolean enabled, Collection<String> missingCapabilities, Device device, List<OnboardingAction> actions) {
        super();
        this.id = new StringBuilder(32).append(device.getId()).append('/').append(scenario.getId()).toString();
        this.enabled = enabled;
        this.missingCapabilities = missingCapabilities;
        this.scenario = scenario;
        this.device = device;
        this.actions = actions;
        this.compositeId = new CompositeId(device, scenario.getId());
    }

    @Override
    public List<String> getCapabilities(Session session) {
        return scenario.getCapabilities(session);
    }

    @Override
    public List<String> getCapabilities(int userId, int contextId) {
        return scenario.getCapabilities(userId, contextId);
    }

    @Override
    public CompositeId getCompositeId() {
        return compositeId;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public List<OnboardingAction> getActions() {
        return actions;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public OnboardingType getType() {
        return scenario.getType();
    }

    @Override
    public Link getLink() {
        return scenario.getLink();
    }

    @Override
    public Collection<String> getMissingCapabilities(Session session) {
        return null == missingCapabilities ? Collections.<String> emptyList() : Collections.<String> unmodifiableCollection(missingCapabilities);
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        // Return pre-computed flag
        return enabled;
    }

    @Override
    public boolean isEnabled(int userId, int contextId) throws OXException {
        // Return pre-computed flag
        return enabled;
    }

    @Override
    public List<OnboardingProvider> getProviders(Session session) {
        return scenario.getProviders(session);
    }

    @Override
    public List<OnboardingProvider> getProviders(int userId, int contextId) {
        return scenario.getProviders(userId, contextId);
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return scenario.getDisplayName(session);
    }

    @Override
    public String getDisplayName(int userId, int contextId) throws OXException {
        return scenario.getDisplayName(userId, contextId);
    }

    @Override
    public List<Scenario> getAlternatives(Session session) {
        return scenario.getAlternatives(session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return scenario.getIcon(session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return scenario.getDescription(session);
    }

}
