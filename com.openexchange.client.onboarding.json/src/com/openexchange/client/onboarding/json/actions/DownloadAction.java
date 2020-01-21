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

package com.openexchange.client.onboarding.json.actions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.client.onboarding.BuiltInProvider;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.plist.OnboardingPlistProvider;
import com.openexchange.client.onboarding.plist.PlistScenario;
import com.openexchange.client.onboarding.plist.PlistScenarioType;
import com.openexchange.client.onboarding.plist.PlistUtility;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.Strings;
import com.openexchange.plist.PListDict;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DownloadAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class DownloadAction extends AbstractOnboardingAction {

    /**
     * Initializes a new {@link DownloadAction}.
     *
     * @param services The service look-up
     */
    public DownloadAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        // Obtain needed service
        OnboardingService service = getOnboardingService();

        // Obtain referenced PLIST scenario
        String[] types = Strings.splitByComma(requestData.requireParameter("type"));

        // Determine suitable providers
        Map<String, OnboardingPlistProvider> onboardingProviders = new LinkedHashMap<>(types.length);
        for (String type : types) {
            Optional<PlistScenarioType> optionalScenarioType = PlistScenarioType.plistScenarioTypeFor(type);
            if (optionalScenarioType.isPresent()) {
                switch (optionalScenarioType.get()) {
                    case CALDAV:
                        if (!OnboardingUtility.hasCapability(Permission.CALDAV.getCapabilityName(), session)) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALDAV.getCapabilityName());
                        }
                        if (!session.getUserPermissionBits().hasCalendar()) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALENDAR.getCapabilityName());
                        }
                        PlistUtility.putPlistProviderById(BuiltInProvider.CALDAV, onboardingProviders, service);
                        break;
                    case CARDDAV:
                        if (!OnboardingUtility.hasCapability(Permission.CARDDAV.getCapabilityName(), session)) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CARDDAV.getCapabilityName());
                        }
                        if (!session.getUserPermissionBits().hasContact()) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CONTACTS.getCapabilityName());
                        }
                        PlistUtility.putPlistProviderById(BuiltInProvider.CARDDAV, onboardingProviders, service);
                        break;
                    case DAV:
                        if (!OnboardingUtility.hasCapability(Permission.CALDAV.getCapabilityName(), session)) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALDAV.getCapabilityName());
                        }
                        if (!OnboardingUtility.hasCapability(Permission.CARDDAV.getCapabilityName(), session)) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CARDDAV.getCapabilityName());
                        }
                        if (!session.getUserPermissionBits().hasCalendar()) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALENDAR.getCapabilityName());
                        }
                        if (!session.getUserPermissionBits().hasContact()) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CONTACTS.getCapabilityName());
                        }
                        PlistUtility.putPlistProviderById(BuiltInProvider.CALDAV, onboardingProviders, service);
                        PlistUtility.putPlistProviderById(BuiltInProvider.CARDDAV, onboardingProviders, service);
                        break;
                    case MAIL:
                        if (!OnboardingUtility.hasCapability(Permission.WEBMAIL.getCapabilityName(), session)) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.WEBMAIL.getCapabilityName());
                        }
                        PlistUtility.putPlistProviderById(BuiltInProvider.MAIL, onboardingProviders, service);
                        break;
                    default:
                        throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", type);

                }
            } else {
                Optional<OnboardingPlistProvider> optionalProvider = PlistUtility.lookUpPlistProviderById(type, service);
                if (!optionalProvider.isPresent()) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", type);
                }

                OnboardingPlistProvider provider = optionalProvider.get();
                if (!provider.isAvailable(session).isAvailable()) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(provider.getId());
                }
                onboardingProviders.put(type, provider);
            }
        }

        // Generate synthetic scenario
        PListDict dict = null;
        for (Map.Entry<String, OnboardingPlistProvider> providerEntry : onboardingProviders.entrySet()) {
            OnboardingPlistProvider provider = providerEntry.getValue();
            PlistScenario scenario = PlistScenario.newInstance(providerEntry.getKey(), Collections.singletonList(provider));
            dict = provider.getPlist(dict, scenario, requestData.getHostData().getHost(), session.getUserId(), session.getContextId());
        }
        return new AJAXRequestResult(dict, "plist_download");
    }

}
