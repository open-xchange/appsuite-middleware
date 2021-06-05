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

package com.openexchange.client.onboarding.carddav;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
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
import com.openexchange.client.onboarding.carddav.custom.CustomLoginSource;
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
import com.openexchange.osgi.ServiceListing;
import com.openexchange.plist.PListDict;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;


/**
 * {@link CardDAVOnboardingProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class CardDAVOnboardingProvider implements OnboardingPlistProvider {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CardDAVOnboardingProvider.class);

    private final ServiceLookup services;
    private final String identifier;
    private final Set<Device> supportedDevices;
    private final Set<OnboardingType> supportedTypes;
    private final ServiceListing<CustomLoginSource> loginSources;

    /**
     * Initializes a new {@link CardDAVOnboardingProvider}.
     */
    public CardDAVOnboardingProvider(ServiceListing<CustomLoginSource> loginSources, ServiceLookup services) {
        super();
        this.loginSources = loginSources;
        this.services = services;
        identifier = BuiltInProvider.CARDDAV.getId();
        supportedDevices = EnumSet.of(Device.APPLE_IPAD, Device.APPLE_IPHONE, Device.APPLE_MAC);
        supportedTypes = EnumSet.of(OnboardingType.PLIST, OnboardingType.MANUAL);
    }

    private CustomLoginSource getHighestRankedCustomLoginSource() {
        List<CustomLoginSource> sources = loginSources.getServiceList();
        return sources.isEmpty() ? null : sources.get(0);
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
        if (!Device.getActionsFor(request.getClientDevice(), device, scenario.getType(), session).contains(request.getAction())) {
            throw OnboardingExceptionCodes.UNSUPPORTED_ACTION.create(request.getAction().getId());
        }

        switch (scenario.getType()) {
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
        String login;
        {
            Boolean customSource = OnboardingUtility.getBoolFromProperty("com.openexchange.client.onboarding.carddav.login.customsource", Boolean.FALSE, session);
            if (customSource.booleanValue()) {
                CustomLoginSource customLoginSource = getHighestRankedCustomLoginSource();
                if (null == customLoginSource) {
                    LOG.warn("Unable to find any CustomLoginSource services! Falling back to login name.");
                    login = OnboardingUtility.getUserLogin(session.getUserId(), session.getContextId());
                } else {
                    login = customLoginSource.getCardDAVLogin(session.getUserId(), session.getContextId());
                }
            } else {
                login = OnboardingUtility.getUserLogin(session.getUserId(), session.getContextId());
            }
        }
        configuration.put(CARDDAV_LOGIN_FIELD, login);
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

        ServerConfigService serverConfigService = services.getOptionalService(ServerConfigService.class);
        String scenarioProductName = null;
        if (null != serverConfigService) {
            ServerConfig config = serverConfigService.getServerConfig(hostName, userId, contextId);
            scenarioProductName = config.getProductName();
        } else {
            scenarioProductName = scenario.getDisplayName(userId, contextId);
        }

        // Get the PListDict to contribute to
        PListDict pListDict;
        if (null == optPrevPListDict) {
            pListDict = new PListDict();
            pListDict.setPayloadIdentifier("com.open-xchange." + scenario.getId());
            pListDict.setPayloadType("Configuration");
            pListDict.setPayloadUUID(OnboardingUtility.craftScenarioUUIDFrom(scenario.getId(), userId, contextId).toString());
            pListDict.setPayloadVersion(1);
            pListDict.setPayloadDisplayName(scenarioProductName);
        } else {
            pListDict = optPrevPListDict;
        }

        // Generate payload content dictionary
        PListDict payloadContent = new PListDict();
        payloadContent.setPayloadType("com.apple.carddav.account");
        payloadContent.setPayloadUUID(OnboardingUtility.craftProviderUUIDFrom(identifier, userId, contextId).toString());
        payloadContent.setPayloadIdentifier("com.open-xchange.carddav");
        payloadContent.setPayloadVersion(1);
        payloadContent.addStringValue("PayloadOrganization", "Open-Xchange");
        payloadContent.setPayloadDisplayName(scenarioProductName + " CardDAV");
        String login;
        {
            Boolean customSource = OnboardingUtility.getBoolFromProperty("com.openexchange.client.onboarding.carddav.login.customsource", Boolean.FALSE, userId, contextId);
            if (customSource.booleanValue()) {
                CustomLoginSource customLoginSource = getHighestRankedCustomLoginSource();
                if (null == customLoginSource) {
                    LOG.warn("Unable to find any CustomLoginSource services! Falling back to login name.");
                    login = OnboardingUtility.getUserLogin(userId, contextId);
                } else {
                    login = customLoginSource.getCardDAVLogin(userId, contextId);
                }
            } else {
                login = OnboardingUtility.getUserLogin(userId, contextId);
            }
        }
        payloadContent.addStringValue("CardDAVUsername", login);

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
                return OnboardingUtility.constructURLWithParameters(hostData, null, "/carddav", null).toString();
            }
            throw OnboardingExceptionCodes.MISSING_PROPERTY.create(propertyName);
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            if (generateIfAbsent) {
                return OnboardingUtility.constructURLWithParameters(hostData, null, "/carddav", null).toString();
            }
            throw OnboardingExceptionCodes.MISSING_PROPERTY.create(propertyName);
        }

        return value.trim();
    }

}
