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

package com.openexchange.client.onboarding.plist;

import java.util.Collections;
import java.util.List;
import com.openexchange.client.onboarding.Icon;
import com.openexchange.client.onboarding.Link;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingType;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link PlistScenario} - The implementation to create a synthetic scenario that yields a PLIST dictionary.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class PlistScenario implements Scenario {

    /**
     * Creates a new synthetic scenario instance for yielding a PLIST dictionary from given providers.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Each provider is expected to be of type {@link OnboardingPlistProvider}.
     * </div>
     *
     * @param id The scenario identifier
     * @param providers The providers that apply to the scenario
     * @return The new {@code PlistScenario} instance
     * @throws IllegalArgumentException If given arguments cannot be used to yield a PLIST scenario
     */
    public static PlistScenario newInstance(String id, List<? extends OnboardingProvider> providers) {
        if (Strings.isEmpty(id)) {
            throw new IllegalArgumentException("Scenario identifier must not be null or empty");
        }
        if (providers == null || providers.isEmpty()) {
            throw new IllegalArgumentException("Provider listing must not be null or empty");
        }
        for (OnboardingProvider provider : providers) {
            if (!OnboardingPlistProvider.class.isInstance(provider)) {
                throw new IllegalArgumentException("Specified provider '" + provider.getId() + "' is not suitable to retrieve a PLIST dictionary.");
            }
        }
        return new PlistScenario(id, providers);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final String id;
    private final List<OnboardingProvider> providers;

    /**
     * Initializes a new {@link PlistScenario}.
     *
     * @param id The scenario identifier
     * @param providers The providers that apply to the scenario
     */
    protected PlistScenario(String id, List<? extends OnboardingProvider> providers) {
        super();
        this.id = id;
        this.providers = Collections.unmodifiableList(providers);
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        return true;
    }

    @Override
    public boolean isEnabled(int userId, int contextId) throws OXException {
        return true;
    }

    @Override
    public String getDisplayName(int userId, int contextId) throws OXException {
        return id;
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return null;
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return null;
    }

    @Override
    public Link getLink() {
        return null;
    }

    @Override
    public OnboardingType getType() {
        return OnboardingType.PLIST;
    }

    @Override
    public List<String> getCapabilities(Session session) {
        return null;
    }

    @Override
    public List<String> getCapabilities(int userId, int contextId) {
        return null;
    }

    @Override
    public List<OnboardingProvider> getProviders(Session session) {
        return providers;
    }

    @Override
    public List<OnboardingProvider> getProviders(int userId, int contextId) {
        return providers;
    }

    @Override
    public List<Scenario> getAlternatives(Session session) {
        return Collections.emptyList();
    }

}
