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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.onboarding.mailapp;

import static com.openexchange.onboarding.OnboardingSelectionKey.keyFor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.onboarding.DefaultEntityPath;
import com.openexchange.onboarding.DefaultOnboardingSelection;
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.Module;
import com.openexchange.onboarding.OnboardingAction;
import com.openexchange.onboarding.EntityPath;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingExecutor;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.OnboardingSelectionKey;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.Result;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link MailAppOnboardingConfiguration}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class MailAppOnboardingConfiguration implements OnboardingConfiguration {

    private final ServiceLookup services;
    private final String propertyPrefix;
    private final String identifier;
    private final Map<OnboardingSelectionKey, OnboardingExecutor> executors;

    /**
     * Initializes a new {@link MailAppOnboardingConfiguration}.
     */
    public MailAppOnboardingConfiguration(ServiceLookup services) {
        super();
        this.services = services;
        propertyPrefix = "com.openexchange.onboarding.mailapp";
        identifier = "mailapp";
        this.executors = new HashMap<OnboardingSelectionKey, OnboardingExecutor>(16);

        {
            OnboardingExecutor displayExecutor = new OnboardingExecutor() {

                @Override
                public Result execute(OnboardingRequest request, Session session) throws OXException {
                    return displayResult(request, session);
                }
            };

            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.EMAIL), OnboardingAction.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.EMAIL), OnboardingAction.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.ANDROID_PHONE, Module.EMAIL), OnboardingAction.DISPLAY), displayExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.ANDROID_TABLET, Module.EMAIL), OnboardingAction.DISPLAY), displayExecutor);
        }

        {
            OnboardingExecutor redirectExecutor = new OnboardingExecutor() {

                @Override
                public Result execute(OnboardingRequest request, Session session) throws OXException {
                    return redirectResult(request);
                }
            };

            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.EMAIL), OnboardingAction.DISPLAY), redirectExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.EMAIL), OnboardingAction.DISPLAY), redirectExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.ANDROID_PHONE, Module.EMAIL), OnboardingAction.DISPLAY), redirectExecutor);
            executors.put(keyFor(new DefaultEntityPath(this, Device.ANDROID_TABLET, Module.EMAIL), OnboardingAction.DISPLAY), redirectExecutor);
        }
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(propertyPrefix + ".displayName", MailAppOnboardingStrings.MAILAPP_DISPLAY_NAME, true, session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty(propertyPrefix + ".iconName", session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(propertyPrefix + ".description", MailAppOnboardingStrings.MAILAPP_DESCRIPTION, true, session);
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }

        if (false == capabilityService.getCapabilities(session).contains(Permission.WEBMAIL.getCapabilityName())) {
            return false;
        }

        return OnboardingUtility.getBoolValue(propertyPrefix + ".enabled", true, session);
    }

    @Override
    public List<EntityPath> getEntityPaths(Session session) throws OXException {
        List<EntityPath> paths = new ArrayList<EntityPath>(4);
        paths.add(new DefaultEntityPath(this, Device.APPLE_IPAD, Module.EMAIL));
        paths.add(new DefaultEntityPath(this, Device.APPLE_IPHONE, Module.EMAIL));
        paths.add(new DefaultEntityPath(this, Device.ANDROID_PHONE, Module.EMAIL));
        paths.add(new DefaultEntityPath(this, Device.ANDROID_TABLET, Module.EMAIL));
        return paths;
    }

    @Override
    public List<OnboardingSelection> getSelections(EntityPath entityPath, Session session) throws OXException {
        if (entityPath.matches(Device.APPLE_IPAD, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The link selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.LINK));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DISPLAY));

            return selections;
        } else if (entityPath.matches(Device.APPLE_IPHONE, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The link selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.LINK));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DISPLAY));

            return selections;

        } else if (entityPath.matches(Device.ANDROID_PHONE, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The link selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.LINK));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DISPLAY));

            return selections;
        } else if (entityPath.matches(Device.ANDROID_TABLET, Module.EMAIL, identifier)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(4);

            // The link selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.LINK));

            // The display settings selection
            selections.add(DefaultOnboardingSelection.newInstance(entityPath, OnboardingAction.DISPLAY));

            return selections;
        }

        throw OnboardingExceptionCodes.ENTITY_NOT_SUPPORTED.create(entityPath.getCompositeId());
    }

    @Override
    public Result execute(OnboardingRequest request, Session session) throws OXException {
        OnboardingSelection selection = request.getSelection();
        if (!selection.isEnabled(session)) {
            throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(selection.getCompositeId());
        }

        OnboardingExecutor onboardingExecutor = executors.get(new OnboardingSelectionKey(selection));
        if (null == onboardingExecutor) {
            throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(selection.getCompositeId());
        }

        return onboardingExecutor.execute(request, session);
    }

    Result displayResult(OnboardingRequest request, Session session) throws OXException {
        String resultText = OnboardingUtility.getTranslationFor(MailAppOnboardingStrings.MAILAPP_STORE_LINK, session);
        Map<String, Object> formContent = new HashMap<String, Object>();
        formContent.put("link", getAppStoreLink(request));

        return new Result(resultText, formContent);
    }

    Result redirectResult(OnboardingRequest request) throws OXException {
        return new Result(getAppStoreLink(request), "link");
    }

    private String getAppStoreLink(OnboardingRequest request) throws OXException {
        ConfigurationService configService = services.getService(ConfigurationService.class);
        if (request.getSelection().getCompositeId().startsWith(Platform.APPLE.getId())) {
            return configService.getProperty("com.openexchange.onboarding.mailapp.store.appstore");
        } else if (request.getSelection().getCompositeId().startsWith(Platform.ANDROID_GOOGLE.getId())) {
            return configService.getProperty("com.openexchange.onboarding.mailapp.store.playstore");
        }
        throw OnboardingExceptionCodes.ENTITY_NOT_SUPPORTED.create(request.getSelection().getCompositeId());
    }

}
