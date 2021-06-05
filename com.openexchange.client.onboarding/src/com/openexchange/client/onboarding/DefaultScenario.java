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

package com.openexchange.client.onboarding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DefaultScenario} - The default entity implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DefaultScenario implements Scenario {

    /**
     * Creates a new {@code DefaultScenario} instance
     *
     * @param id The scenario identifier
     * @param type The associated type
     * @param link The optional link
     * @param icon The icon
     * @param capabilities The optional capabilities
     * @param i18nDisplayName The translatable display name
     * @param i18nDescription The translatable description
     * @return The new  {@code DefaultScenario} instance
     */
    public static DefaultScenario newInstance(String id, OnboardingType type, Link link, Icon icon, List<String> capabilities, String i18nDisplayName, String i18nDescription) {
        return new DefaultScenario(id, type, link, icon, capabilities, i18nDisplayName, i18nDescription);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final String id;
    private final OnboardingType type;
    private final Link link;
    private final List<OnboardingProvider> providers;
    private final List<Scenario> alternatives;
    private final Icon icon;
    private final String i18nDisplayName;
    private final String i18nDescription;
    private final List<String> capabilities;

    /**
     * Initializes a new {@link DefaultScenario}.
     *
     * @param id The scenario identifier
     * @param type The associated type
     * @param link The optional link
     * @param icon The icon
     * @param capabilities The optional capabilities
     * @param i18nDisplayName The translatable display name
     * @param i18nDescription The translatable description
     */
    protected DefaultScenario(String id, OnboardingType type, Link link, Icon icon, List<String> capabilities, String i18nDisplayName, String i18nDescription) {
        super();
        this.id = id;
        this.type = type;
        this.link = link;
        this.providers = new ArrayList<OnboardingProvider>(4);
        this.alternatives = new ArrayList<Scenario>(2);
        this.capabilities = capabilities;

        this.icon = icon;
        this.i18nDisplayName = i18nDisplayName;
        this.i18nDescription = i18nDescription;
    }

    /**
     * Adds specified provider
     *
     * @param provider The provider
     */
    public void addProvider(OnboardingProvider provider) {
        if (null != provider) {
            this.providers.add(provider);
        }
    }

    /**
     * Adds specified alternative
     *
     * @param scenario The alternative
     */
    public void addAlternative(Scenario scenario) {
        if (null != scenario) {
            this.alternatives.add(scenario);
        }
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        return OnboardingUtility.isScenarioEnabled(id, session);
    }

    @Override
    public boolean isEnabled(int userId, int contextId) throws OXException {
        return OnboardingUtility.isScenarioEnabled(id, userId, contextId);
    }

    @Override
    public String getDisplayName(int userId, int contextId) throws OXException {
        return OnboardingUtility.getTranslationFor(i18nDisplayName, userId, contextId);
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFor(i18nDisplayName, session);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return icon;
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFor(i18nDescription, session);
    }

    @Override
    public Link getLink() {
        return link;
    }

    @Override
    public OnboardingType getType() {
        return type;
    }

    @Override
    public List<String> getCapabilities(Session session) {
        return capabilities;
    }

    @Override
    public List<String> getCapabilities(int userId, int contextId) {
        return capabilities;
    }

    @Override
    public List<OnboardingProvider> getProviders(Session session) {
        return Collections.unmodifiableList(providers);
    }

    @Override
    public List<OnboardingProvider> getProviders(int userId, int contextId) {
        return Collections.unmodifiableList(providers);
    }

    @Override
    public List<Scenario> getAlternatives(Session session) {
        return Collections.unmodifiableList(alternatives);
    }

}
