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

package com.openexchange.onboarding.carddav;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.DisplayResult;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingProvider;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.OnboardingUtility;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.ResultReply;
import com.openexchange.onboarding.Scenario;
import com.openexchange.onboarding.plist.PlistResult;
import com.openexchange.plist.PListDict;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link CardDAVOnboardingProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class CardDAVOnboardingProvider implements OnboardingProvider {

    private final ServiceLookup services;
    private final String identifier;
    private final EnumSet<Device> supportedDevices;

    /**
     * Initializes a new {@link CardDAVOnboardingProvider}.
     */
    public CardDAVOnboardingProvider(ServiceLookup services) {
        super();
        this.services = services;
        identifier = "carddav";
        supportedDevices = EnumSet.of(Device.APPLE_IPAD, Device.APPLE_IPHONE, Device.APPLE_MAC);
    }

    @Override
    public String getId() {
        return identifier;
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
    private final static String CARDDAV_PASSWORD_FIELD = "carddav_password";
    private final static String CARDDAV_HOST_FIELD = "carddav_hostName";

    private Result displayResult(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        Map<String, Object> configuration = null == previousResult ? new HashMap<String, Object>(8) : ((DisplayResult) previousResult).getConfiguration();
        configuration.put(CARDDAV_LOGIN_FIELD, session.getLogin());
        configuration.put(CARDDAV_PASSWORD_FIELD, session.getPassword());
        configuration.put(CARDDAV_HOST_FIELD, getCardDAVUrl(request, session));
        return new DisplayResult(configuration);
    }

    // --------------------------------------------- PLIST utils --------------------------------------------------------------

    private Result plistResult(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        Scenario scenario = request.getScenario();

        // Get the PListDict to contribute to
        PListDict pListDict;
        if (null == previousResult) {
            pListDict = new PListDict();
            pListDict.setPayloadIdentifier("com.open-xchange." + scenario.getId());
            pListDict.setPayloadType("Configuration");
            pListDict.setPayloadUUID(OnboardingUtility.craftUUIDFrom(scenario.getId(), session).toString());
            pListDict.setPayloadVersion(1);
            pListDict.setPayloadDisplayName(scenario.getDisplayName(session));
        } else {
            pListDict = ((PlistResult) previousResult).getPListDict();
        }

        // Generate payload content dictionary
        PListDict payloadContent = new PListDict();
        payloadContent.setPayloadType("com.apple.carddav.account");
        payloadContent.setPayloadUUID(OnboardingUtility.craftUUIDFrom(identifier, session).toString());
        payloadContent.setPayloadIdentifier("com.open-xchange.carddav");
        payloadContent.setPayloadVersion(1);
        payloadContent.addStringValue("PayloadOrganization", "Open-Xchange");
        payloadContent.addStringValue("CardDAVUsername", session.getLogin());
        payloadContent.addStringValue("CardDAVPassword", session.getPassword());
        payloadContent.addStringValue("CardDAVHostName", getCardDAVUrl(request, session));
        payloadContent.addBooleanValue("CardDAVUseSSL", false);
        payloadContent.addStringValue("CardDAVAccountDescription", OnboardingUtility.getTranslationFor(CardDAVOnboardingStrings.CARDDAV_ACCOUNT_DESCRIPTION, session));

        // Add payload content dictionary to top-level dictionary
        pListDict.addPayloadContent(payloadContent);

        // Return result
        return new PlistResult(pListDict, ResultReply.NEUTRAL);
    }

    private String getCardDAVUrl(OnboardingRequest request, Session session) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        ComposedConfigProperty<String> property = view.property("com.openexchange.onboarding.carddav.url", String.class);
        if (null == property || !property.isDefined()) {
            return OnboardingUtility.constructURLWithParameters(request.getHostData(), null, "/carddav", false, null).toString();
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            return OnboardingUtility.constructURLWithParameters(request.getHostData(), null, "/carddav", false, null).toString();
        }

        return value;
    }

}
