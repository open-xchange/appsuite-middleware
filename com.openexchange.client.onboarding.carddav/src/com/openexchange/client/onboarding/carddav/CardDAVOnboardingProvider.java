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

package com.openexchange.client.onboarding.carddav;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.client.onboarding.AvailabilityResult;
import com.openexchange.client.onboarding.BuiltInProvider;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.DisplayResult;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingRequest;
import com.openexchange.client.onboarding.OnboardingType;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.Result;
import com.openexchange.client.onboarding.ResultReply;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.client.onboarding.net.HostAndPort;
import com.openexchange.client.onboarding.net.NetUtility;
import com.openexchange.client.onboarding.plist.OnboardingPlistProvider;
import com.openexchange.client.onboarding.plist.PlistResult;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.Strings;
import com.openexchange.plist.PListDict;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link CardDAVOnboardingProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class CardDAVOnboardingProvider implements OnboardingPlistProvider {

    private final ServiceLookup services;
    private final String identifier;
    private final Set<Device> supportedDevices;
    private final Set<OnboardingType> supportedTypes;

    /**
     * Initializes a new {@link CardDAVOnboardingProvider}.
     */
    public CardDAVOnboardingProvider(ServiceLookup services) {
        super();
        this.services = services;
        identifier = BuiltInProvider.CARDDAV.getId();
        supportedDevices = EnumSet.of(Device.APPLE_IPAD, Device.APPLE_IPHONE, Device.APPLE_MAC);
        supportedTypes = EnumSet.of(OnboardingType.PLIST, OnboardingType.MANUAL);
    }

    @Override
    public String getDescription() {
        return "Configures CardDAV.";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public AvailabilityResult isAvailable(Session session) throws OXException {
        boolean available = OnboardingUtility.hasCapability(Permission.CARDDAV.getCapabilityName(), session);
        return new AvailabilityResult(available, Permission.CARDDAV.getCapabilityName());
    }

    @Override
    public AvailabilityResult isAvailable(int userId, int contextId) throws OXException {
        boolean available = OnboardingUtility.hasCapability(Permission.CARDDAV.getCapabilityName(), userId, contextId);
        return new AvailabilityResult(available, Permission.CARDDAV.getCapabilityName());
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
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            case MANUAL:
                return doExecuteManual(request, previousResult, session);
            case PLIST:
                return doExecutePlist(request, previousResult, session);
            default:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
        }
    }

    private Result doExecutePlist(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        return plistResult(request, previousResult, session);
    }

    private Result doExecuteManual(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        return displayResult(request, previousResult, session);
    }

    // --------------------------------------------- Display utils --------------------------------------------------------------


    private final static String CARDDAV_LOGIN_FIELD = "carddav_login";
    private final static String CARDDAV_URL_FIELD = "carddav_url";

    private Result displayResult(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        Map<String, Object> configuration = null == previousResult ? new HashMap<String, Object>(8) : ((DisplayResult) previousResult).getConfiguration();
        configuration.put(CARDDAV_LOGIN_FIELD, session.getLogin());
        configuration.put(CARDDAV_URL_FIELD, getCardDAVUrl(false, request.getHostData(), session.getUserId(), session.getContextId()));
        return new DisplayResult(configuration, ResultReply.NEUTRAL);
    }

    // --------------------------------------------- PLIST utils --------------------------------------------------------------

    private Result plistResult(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        PListDict previousPListDict = null == previousResult ? null : ((PlistResult) previousResult).getPListDict();
        PListDict pListDict = getPlist(previousPListDict, request.getScenario(), request.getHostData().getHost(), session.getUserId(), session.getContextId());
        return new PlistResult(pListDict, ResultReply.NEUTRAL);
    }

    @Override
    public PListDict getPlist(PListDict optPrevPListDict, Scenario scenario, String hostName, int userId, int contextId) throws OXException {
        // Get the PListDict to contribute to
        PListDict pListDict;
        if (null == optPrevPListDict) {
            pListDict = new PListDict();
            pListDict.setPayloadIdentifier("com.open-xchange." + scenario.getId());
            pListDict.setPayloadType("Configuration");
            pListDict.setPayloadUUID(OnboardingUtility.craftUUIDFrom(scenario.getId(), userId, contextId).toString());
            pListDict.setPayloadVersion(1);
            pListDict.setPayloadDisplayName(scenario.getDisplayName(userId, contextId));
        } else {
            pListDict = optPrevPListDict;
        }

        // Generate payload content dictionary
        PListDict payloadContent = new PListDict();
        payloadContent.setPayloadType("com.apple.carddav.account");
        payloadContent.setPayloadUUID(OnboardingUtility.craftUUIDFrom(identifier, userId, contextId).toString());
        payloadContent.setPayloadIdentifier("com.open-xchange.carddav");
        payloadContent.setPayloadVersion(1);
        payloadContent.addStringValue("PayloadOrganization", "Open-Xchange");
        payloadContent.addStringValue("CardDAVUsername", OnboardingUtility.getUserLogin(userId, contextId));

        {
            String cardDAVUrl = getCardDAVUrl(false, null, userId, contextId);
            boolean isSsl = NetUtility.impliesSsl(cardDAVUrl);
            HostAndPort hostAndPort = NetUtility.parseHostNameString(cardDAVUrl);

            payloadContent.addStringValue("CardDAVHostName", hostAndPort.getHost());
            if (hostAndPort.getPort() > 0) {
                payloadContent.addIntegerValue("CardDAVPort", hostAndPort.getPort());
            }
            payloadContent.addBooleanValue("CardDAVUseSSL", isSsl);
        }

        payloadContent.addStringValue("CardDAVAccountDescription", OnboardingUtility.getProductName(hostName, userId, contextId) + " CardDAV");

        // Add payload content dictionary to top-level dictionary
        pListDict.addPayloadContent(payloadContent);

        // Return result
        return pListDict;
    }

    private String getCardDAVUrl(boolean generateIfAbsent, HostData hostData, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);
        String propertyName = "com.openexchange.client.onboarding.carddav.url";
        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            if (generateIfAbsent) {
                return OnboardingUtility.constructURLWithParameters(hostData, null, "/carddav", false, null).toString();
            }
            throw OnboardingExceptionCodes.MISSING_PROPERTY.create(propertyName);
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            if (generateIfAbsent) {
                return OnboardingUtility.constructURLWithParameters(hostData, null, "/carddav", false, null).toString();
            }
            throw OnboardingExceptionCodes.MISSING_PROPERTY.create(propertyName);
        }

        return value.trim();
    }

}
