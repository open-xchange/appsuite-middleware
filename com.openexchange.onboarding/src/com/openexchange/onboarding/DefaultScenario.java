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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding;

import java.util.ArrayList;
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
     * Creates a new {@code DefaultEntity} instance
     *
     * @param id The identifier
     * @param prefix The prefix to use to look-up properties; e.g. <code>"apple.iphone.calendar.caldav."</code>
     * @param withDescription <code>true</code> to also look-up the description property; otherwise <code>false</code>
     * @param type The type; e.g. <code>"plist"</code>
     * @return A new {@code DefaultEntity} instance
     */
    public static DefaultScenario newInstance(String id, String prefix, boolean withDescription, OnboardingType type) {
        return new DefaultScenario(id, prefix, withDescription, type);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final String id;
    private final String enabledProperty;
    private final String displayNameProperty;
    private final String imageNameProperty;
    private final String descriptionProperty;
    private final OnboardingType type;
    private final List<OnboardingProvider> providers;
    private final List<Scenario> alternatives;

    private DefaultScenario(String id, String prefix, boolean withDescription, OnboardingType type) {
        super();
        int len = prefix.length();
        StringBuilder propertyNameBuilder = new StringBuilder(48).append(prefix);

        this.id = id;
        this.type = type;
        this.providers = new ArrayList<OnboardingProvider>(4);
        this.alternatives = new ArrayList<Scenario>(2);

        this.enabledProperty = propertyNameBuilder.append("enabled").toString();

        propertyNameBuilder.setLength(len);
        this.displayNameProperty = propertyNameBuilder.append("displayName").toString();

        propertyNameBuilder.setLength(len);
        this.imageNameProperty = propertyNameBuilder.append("icon").toString();

        propertyNameBuilder.setLength(len);
        this.descriptionProperty = withDescription ? propertyNameBuilder.append("description").toString() : null;
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
        return OnboardingUtility.getBoolValue(enabledProperty, true, session);
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(displayNameProperty, session);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty(imageNameProperty, session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return null == descriptionProperty ? null : OnboardingUtility.getTranslationFromProperty(descriptionProperty, session);
    }

    @Override
    public OnboardingType getType() {
        return type;
    }

    @Override
    public List<OnboardingProvider> getProviders(Session session) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Scenario> getAlternatives(Session session) {
        // TODO Auto-generated method stub
        return null;
    }

}
