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

import java.util.Optional;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.client.onboarding.plist.OnboardingPlistProvider;
import com.openexchange.client.onboarding.plist.PlistScenarioType;
import com.openexchange.client.onboarding.plist.PlistUtility;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DownloadLinkAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class DownloadLinkAction extends AbstractOnboardingAction {

    /**
     * Initializes a new {@link DownloadLinkAction}.
     *
     * @param services The service look-up
     */
    public DownloadLinkAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        // Obtain needed service
        OnboardingService service = getOnboardingService();

        // Obtain referenced PLIST scenario
        String sTypes = requestData.requireParameter("type");
        String[] types = Strings.splitByComma(sTypes);

        // Determine suitable providers
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
                        break;
                    case CARDDAV:
                        if (!OnboardingUtility.hasCapability(Permission.CARDDAV.getCapabilityName(), session)) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CARDDAV.getCapabilityName());
                        }
                        if (!session.getUserPermissionBits().hasContact()) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CONTACTS.getCapabilityName());
                        }
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
                        break;
                    case MAIL:
                        if (!OnboardingUtility.hasCapability(Permission.WEBMAIL.getCapabilityName(), session)) {
                            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.WEBMAIL.getCapabilityName());
                        }
                        break;
                    default:
                        throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", type);

                }
            } else {
                Optional<OnboardingPlistProvider> optionalProvider = PlistUtility.lookUpPlistProviderById(type, service);
                if (!optionalProvider.isPresent()) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", type);
                }
            }
        }

        DownloadLinkProvider linkProvider = services.getService(DownloadLinkProvider.class);
        String link = linkProvider.getLink(requestData.getHostData(), session.getUserId(), session.getContextId(), sTypes, Device.APPLE_IPHONE.getId());
        return new AJAXRequestResult(link, "json");

    }

}
