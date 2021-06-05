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
