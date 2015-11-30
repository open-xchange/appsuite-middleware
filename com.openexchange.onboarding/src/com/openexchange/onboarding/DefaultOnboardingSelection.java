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

import com.openexchange.exception.OXException;
import com.openexchange.onboarding.osgi.Services;
import com.openexchange.onboarding.service.OnboardingConfigurationService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;

/**
 * {@link DefaultOnboardingSelection} - The default {@code OnboardingPossibility} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DefaultOnboardingSelection implements OnboardingSelection {

    /**
     * Parses the on-boarding selection from specified composite identifier
     *
     * @param compositeId The composite identifier
     * @return The parsed on-boarding selection
     * @throws OXException If composite identifier is invalid or cannot be parsed
     */
    public static DefaultOnboardingSelection parseFrom(String compositeId) throws OXException {
        if (null == compositeId) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create("null");
        }

        char delim = '/';

        int off = 0;
        int pos = compositeId.indexOf(delim, off);
        if (pos < 0) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create(compositeId);
        }

        Device device = Device.deviceFor(compositeId.substring(off, pos));
        if (null == device) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create(compositeId);
        }

        off = pos + 1;
        pos = compositeId.indexOf(delim, off);
        if (pos < 0) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create(compositeId);
        }

        Module module = Module.moduleFor(compositeId.substring(off, pos));
        if (null == module) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create(compositeId);
        }

        off = pos + 1;
        pos = compositeId.indexOf(delim, off);
        if (pos < 0) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create(compositeId);
        }

        OnboardingConfiguration configuration;
        {
            String serviceId = compositeId.substring(off, pos);
            OnboardingConfigurationService onboardingConfigurationService = Services.optService(OnboardingConfigurationService.class);
            if (null == onboardingConfigurationService) {
                throw ServiceExceptionCode.absentService(OnboardingConfigurationService.class);
            }
            configuration = onboardingConfigurationService.getConfiguration(serviceId);
        }

        off = pos + 1;

        OnboardingAction action = OnboardingAction.actionFor(compositeId.substring(off));
        if (null == action) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create(compositeId);
        }

        return newInstance(new DefaultEntityPath(configuration, device.getPlatform(), device, module), action);
    }

    /**
     * Creates a new {@code DefaultOnboardingSelection} instance
     *
     * @param entityPath The entity path associated with the selection
     * @param action The on-boarding action
     * @return A new {@code DefaultOnboardingSelection} instance
     */
    public static DefaultOnboardingSelection newInstance(EntityPath entityPath, OnboardingAction action) {
        return new DefaultOnboardingSelection(entityPath, action);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final String id;
    private final String enabledProperty;
    private final String displayNameProperty;
    private final String imageNameProperty;
    private final String descriptionProperty;
    private final OnboardingAction action;
    private final EntityPath entityPath;

    private DefaultOnboardingSelection(EntityPath entityPath, OnboardingAction action) {
        super();
        StringBuilder propertyNameBuilder = new StringBuilder("com.openexchange.onboarding.").append(entityPath.getService().getId()).append('.').append(action.getId()).append('.');
        int len = propertyNameBuilder.length();

        this.id = new StringBuilder(entityPath.getCompositeId()).append('/').append(action.getId()).toString();
        this.action = action;
        this.entityPath = entityPath;

        this.enabledProperty = propertyNameBuilder.append("enabled").toString();

        propertyNameBuilder.setLength(len);
        this.displayNameProperty = propertyNameBuilder.append("displayName").toString();

        propertyNameBuilder.setLength(len);
        this.imageNameProperty = propertyNameBuilder.append("iconName").toString();

        propertyNameBuilder.setLength(len);
        this.descriptionProperty = propertyNameBuilder.append("description").toString();
    }

    @Override
    public EntityPath getEntityPath() {
        return entityPath;
    }

    @Override
    public OnboardingAction getAction() {
        return action;
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
    public String getCompositeId() {
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

}
