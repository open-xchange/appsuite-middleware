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

package com.openexchange.client.onboarding.json.actions;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.client.onboarding.BuiltInProvider;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.plist.OnboardingPlistProvider;
import com.openexchange.client.onboarding.plist.PListSigner;
import com.openexchange.client.onboarding.plist.PlistScenario;
import com.openexchange.client.onboarding.plist.PlistScenarioType;
import com.openexchange.client.onboarding.plist.PlistUtility;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.plist.PListDict;
import com.openexchange.plist.PListWriter;
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

        // Generate synthetic scenario and sign result
        PListDict dict = generatePListDict(requestData, session, onboardingProviders);
        if (dict == null) {
            return new AJAXRequestResult();
        }
        
        PListSigner signer = services.getOptionalService(PListSigner.class);
        if (null != signer) {
            boolean error = true;
            ThresholdFileHolder fileHolder = null;
            try {
                fileHolder = new ThresholdFileHolder();
                fileHolder.setDisposition("attachment");
                fileHolder.setName("profile.mobileconfig");
                fileHolder.setContentType("application/x-apple-aspen-config");
                fileHolder.setDelivery("download");
                new PListWriter().write(dict, fileHolder.asOutputStream());
                IFileHolder signed = signer.signPList(fileHolder, session);
                fileHolder = new ThresholdFileHolder(signed);
                signed.close();
                error = false;
                return new AJAXRequestResult(fileHolder, "signed_plist_download");
            } catch (IOException e) {
                throw OnboardingExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } finally {
                if (error) {
                    Streams.close(fileHolder);
                }
            }
        }
        return new AJAXRequestResult(dict, "plist_download");
    }

    /**
     * Generate a possible synthetic scenario
     *
     * @param requestData The {@link AJAXRequestData}
     * @param session The session
     * @param onboardingProviders the onboarding providers
     * @return The possible {@link PListDict} with the synthetic scenario
     * @throws OXException
     */
    private PListDict generatePListDict(AJAXRequestData requestData, ServerSession session, Map<String, OnboardingPlistProvider> onboardingProviders) throws OXException {
        HostData hostData = requestData.getHostData();
        if (hostData == null) {
            return null;
        }
        PListDict dict = null;
        for (Map.Entry<String, OnboardingPlistProvider> providerEntry : onboardingProviders.entrySet()) {
            OnboardingPlistProvider provider = providerEntry.getValue();
            PlistScenario scenario = PlistScenario.newInstance(providerEntry.getKey(), Collections.singletonList(provider));
            dict = provider.getPlist(dict, scenario, hostData.getHost(), session.getUserId(), session.getContextId());
        }
        return dict;
    }
}
