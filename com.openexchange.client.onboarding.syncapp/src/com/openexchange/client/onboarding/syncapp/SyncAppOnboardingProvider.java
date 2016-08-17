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

package com.openexchange.client.onboarding.syncapp;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import com.openexchange.client.onboarding.AvailabilityResult;
import com.openexchange.client.onboarding.BuiltInProvider;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.Link;
import com.openexchange.client.onboarding.LinkResult;
import com.openexchange.client.onboarding.LinkType;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingRequest;
import com.openexchange.client.onboarding.OnboardingType;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.Result;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link SyncAppOnboardingProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class SyncAppOnboardingProvider implements OnboardingProvider {

    private final ServiceLookup services;
    private final String identifier;
    private final Set<Device> supportedDevices;
    private final Set<OnboardingType> supportedTypes;

    /**
     * Initializes a new {@link SyncAppOnboardingProvider}.
     */
    public SyncAppOnboardingProvider(ServiceLookup services) {
        super();
        this.services = services;
        identifier = BuiltInProvider.SYNC_APP.getId();
        supportedDevices = EnumSet.of(Device.ANDROID_PHONE, Device.ANDROID_TABLET);
        supportedTypes = EnumSet.of(OnboardingType.LINK);
    }

    @Override
    public String getDescription() {
        return "Provides a link to the Sync App in Google Play Store.";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public AvailabilityResult isAvailable(Session session) throws OXException {
        boolean available = OnboardingUtility.hasCapability(Permission.CALDAV.getCapabilityName(), session);
        return new AvailabilityResult(available, Permission.CALDAV.getCapabilityName());
    }

    @Override
    public AvailabilityResult isAvailable(int userId, int contextId) throws OXException {
        boolean available = OnboardingUtility.hasCapability(Permission.CALDAV.getCapabilityName(), userId, contextId);
        return new AvailabilityResult(available, Permission.CALDAV.getCapabilityName());
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public Set<OnboardingType> getSupportedTypes() {
        return supportedTypes;
    }

    @Override
    public Set<Device> getSupportedDevices() {
        return Collections.unmodifiableSet(supportedDevices);
    }

    @Override
    public Result execute(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        Device device = request.getDevice();
        if (!supportedDevices.contains(device)) {
            throw OnboardingExceptionCodes.UNSUPPORTED_DEVICE.create(identifier, device.getId());
        }

        Scenario scenario = request.getScenario();
        if (!Device.getActionsFor(device, scenario.getType(), session).contains(request.getAction())) {
            throw OnboardingExceptionCodes.UNSUPPORTED_ACTION.create(request.getAction().getId());
        }

        switch(scenario.getType()) {
            case LINK:
                return doExecuteLink(request, session);
            case MANUAL:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            case PLIST:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            default:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
        }
    }

    private Result doExecuteLink(OnboardingRequest request, Session session) throws OXException {
        return linkResult(request, session);
    }

    private Result linkResult(OnboardingRequest request, Session session) throws OXException {
        return new LinkResult(getAppStoreLink(request, session));
    }

    private Link getAppStoreLink(OnboardingRequest request, Session session) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        LinkType linkType;
        String propertyName;
        {
            Device device = request.getDevice();
            switch (device.getPlatform()) {
                case ANDROID_GOOGLE:
                    propertyName = "com.openexchange.client.onboarding.syncapp.store.google.playstore";
                    linkType = LinkType.GOOGLE_PLAY_STORE;
                    break;
                default:
                    throw OnboardingExceptionCodes.UNSUPPORTED_DEVICE.create(identifier, device.getId());
            }
        }

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            throw OnboardingExceptionCodes.MISSING_PROPERTY.create(propertyName);
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            throw OnboardingExceptionCodes.MISSING_PROPERTY.create(propertyName);
        }

        return new Link(value, linkType);
    }

}
