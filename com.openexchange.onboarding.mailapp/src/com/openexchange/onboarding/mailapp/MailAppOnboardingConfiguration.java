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

import static com.openexchange.onboarding.OnboardingUtility.isAndroidPhone;
import static com.openexchange.onboarding.OnboardingUtility.isAndroidTablet;
import static com.openexchange.onboarding.OnboardingUtility.isIPad;
import static com.openexchange.onboarding.OnboardingUtility.isIPhone;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.onboarding.ClientInfo;
import com.openexchange.onboarding.CommonEntity;
import com.openexchange.onboarding.DefaultEntity;
import com.openexchange.onboarding.DefaultEntityPath;
import com.openexchange.onboarding.DefaultOnboardingSelection;
import com.openexchange.onboarding.Entity;
import com.openexchange.onboarding.EntityPath;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingExecutor;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.OnboardingType;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.Result;
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
    private final String id;
    private final Map<String, OnboardingExecutor> executors;

    /**
     * Initializes a new {@link MailAppOnboardingConfiguration}.
     */
    public MailAppOnboardingConfiguration(ServiceLookup services) {
        super();
        this.services = services;
        this.id = "com.openexchange.onboarding.mailapp";
        this.executors = new HashMap<String, OnboardingExecutor>();

        OnboardingExecutor displayExecutor = new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return displayResult(request, session);
            }
        };
        this.executors.put("apple.ios.iphone.mailapp.display", displayExecutor);
        this.executors.put("apple.ios.ipad.mailapp.display", displayExecutor);
        this.executors.put("android.phone.mailapp.display", displayExecutor);
        this.executors.put("android.tablet.mailapp.display", displayExecutor);

        OnboardingExecutor redirectExecutor = new OnboardingExecutor() {

            @Override
            public Result execute(OnboardingRequest request, Session session) throws OXException {
                return redirectResult(request);
            }
        };
        this.executors.put("apple.ios.iphone.mailapp.link", redirectExecutor);
        this.executors.put("apple.ios.ipad.mailapp.link", redirectExecutor);
        this.executors.put("android.phone.mailapp.link", redirectExecutor);
        this.executors.put("android.tablet.mailapp.link", redirectExecutor);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(id + ".displayName", MailAppOnboardingStrings.MAILAPP_DISPLAY_NAME, true, session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty("com.openexchange.onboarding.mailapp.icon", session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(id + ".description", MailAppOnboardingStrings.MAILAPP_DESCRIPTION, true, session);
    }

    @Override
    public Result execute(OnboardingRequest request, Session session) throws OXException {
        String selectionId = request.getSelectionId();
        if (null == selectionId) {
            throw OnboardingExceptionCodes.CONFIGURATION_ID_MISSING.create();
        }

        OnboardingExecutor onboardingExecutor = executors.get(selectionId);
        if (null == onboardingExecutor) {
            throw OnboardingExceptionCodes.CONFIGURATION_NOT_SUPPORTED.create(selectionId);
        }

        return onboardingExecutor.execute(request, session);
    }

    @Override
    public boolean isEnabled(Session session) {
        ConfigurationService configService = services.getService(ConfigurationService.class);
        return configService.getBoolProperty("com.openexchange.onboarding.mailapp.enabled", true);
    }

    @Override
    public List<EntityPath> getEntityPaths(Session session) {
        List<EntityPath> paths = new ArrayList<EntityPath>(5);
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.APPLE_IOS);
            path.add(CommonEntity.APPLE_IOS_IPAD);
            path.add(DefaultEntity.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".mailapp", "com.openexchange.onboarding.mailapp.", true));
            paths.add(new DefaultEntityPath(Platform.APPLE, path));
        }
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.APPLE_IOS);
            path.add(CommonEntity.APPLE_IOS_IPHONE);
            path.add(DefaultEntity.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".mailapp", "com.openexchange.onboarding.mailapp.", true));
            paths.add(new DefaultEntityPath(Platform.APPLE, path));
        }
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.ANDROID_PHONE);
            path.add(DefaultEntity.newInstance(CommonEntity.ANDROID_PHONE.getId() + ".mailapp", "com.openexchange.onboarding.mailapp.", true));
            paths.add(new DefaultEntityPath(Platform.ANDROID_GOOGLE, path));
        }
        {
            List<Entity> path = new ArrayList<Entity>(4);
            path.add(CommonEntity.ANDROID_TABLET);
            path.add(DefaultEntity.newInstance(CommonEntity.ANDROID_TABLET.getId() + ".mailapp", "com.openexchange.onboarding.mailapp.", true));
            paths.add(new DefaultEntityPath(Platform.ANDROID_GOOGLE, path));
        }
        return paths;
    }

    @Override
    public List<OnboardingSelection> getSelections(String lastEntityId, ClientInfo clientInfo, Session session) throws OXException {
        if ((CommonEntity.APPLE_IOS_IPAD.getId() + ".mailapp").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(2);
            if (isIPad(clientInfo)) {
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".mailapp.download", id, "com.openexchange.onboarding.mailapp.link.", OnboardingType.LINK));
            }
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPAD.getId() + ".mailapp.display", id, "com.openexchange.onboarding.mailapp.display.", OnboardingType.DISPLAY));
            return selections;
        } else if ((CommonEntity.APPLE_IOS_IPHONE.getId() + ".mailapp").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(3);
            if (isIPhone(clientInfo)) {
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".mailapp.download", id, "com.openexchange.onboarding.mailapp.link.", OnboardingType.LINK));
            }
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.APPLE_IOS_IPHONE.getId() + ".mailapp.display", id, "com.openexchange.onboarding.mailapp.display.", OnboardingType.DISPLAY));
            return selections;
        } else if ((CommonEntity.ANDROID_PHONE.getId() + ".mailapp").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(3);
            if (isAndroidPhone(clientInfo)) {
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.ANDROID_PHONE.getId() + ".mailapp.download", id, "com.openexchange.onboarding.mailapp.link.", OnboardingType.LINK));
            }
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.ANDROID_PHONE.getId() + ".mailapp.display", id, "com.openexchange.onboarding.mailapp.display.", OnboardingType.DISPLAY));
            return selections;
        } else if ((CommonEntity.ANDROID_TABLET.getId() + ".mailapp").equals(lastEntityId)) {
            List<OnboardingSelection> selections = new ArrayList<OnboardingSelection>(3);
            if (isAndroidTablet(clientInfo)) {
                selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.ANDROID_TABLET.getId() + ".mailapp.download", id, "com.openexchange.onboarding.mailapp.link.", OnboardingType.LINK));
            }
            selections.add(DefaultOnboardingSelection.newInstance(CommonEntity.ANDROID_TABLET.getId() + ".mailapp.display", id, "com.openexchange.onboarding.mailapp.display.", OnboardingType.DISPLAY));
            return selections;
        }

        throw OnboardingExceptionCodes.ENTITY_NOT_SUPPORTED.create(lastEntityId);
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
        if (request.getSelectionId().startsWith(Platform.APPLE.getId())) {
            return configService.getProperty("com.openexchange.onboarding.mailapp.store.appstore");
        } else if (request.getSelectionId().startsWith(Platform.ANDROID_GOOGLE.getId())) {
            return configService.getProperty("com.openexchange.onboarding.mailapp.store.playstore");
        }
        throw OnboardingExceptionCodes.ENTITY_NOT_SUPPORTED.create(request.getSelectionId());
    }

}
