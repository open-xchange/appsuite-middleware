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
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.client.onboarding.BuiltInProvider;
import com.openexchange.client.onboarding.Icon;
import com.openexchange.client.onboarding.Link;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingType;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.client.onboarding.plist.OnboardingPlistProvider;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.plist.PListDict;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DownloadAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class DownloadAction extends AbstractOnboardingAction {

    public DownloadAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        String[] onboardingProviderIds = null;
        String type = requestData.requireParameter("type");
        switch (type) {
            case "caldav":
                if (!OnboardingUtility.hasCapability(Permission.CALDAV.getCapabilityName(), session)) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALDAV);
                }
                if (!session.getUserPermissionBits().hasCalendar()) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALENDAR);
                }
                onboardingProviderIds = new String[] { BuiltInProvider.CALDAV.getId() };
                break;
            case "carddav":
                if (!OnboardingUtility.hasCapability(Permission.CARDDAV.getCapabilityName(), session)) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CARDDAV);
                }
                if (!session.getUserPermissionBits().hasContact()) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CONTACTS);
                }
                onboardingProviderIds = new String[] { BuiltInProvider.CARDDAV.getId() };
                break;
            case "dav":
                if (!OnboardingUtility.hasCapability(Permission.CALDAV.getCapabilityName(), session)) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALDAV);
                }
                if (!OnboardingUtility.hasCapability(Permission.CARDDAV.getCapabilityName(), session)) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CARDDAV);
                }
                if (!session.getUserPermissionBits().hasCalendar()) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALENDAR);
                }
                if (!session.getUserPermissionBits().hasContact()) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CONTACTS);
                }
                onboardingProviderIds = new String[] { BuiltInProvider.CALDAV.getId(), BuiltInProvider.CARDDAV.getId() };
                break;
            default:
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", type);
        }
        OnboardingService service = getOnboardingService();
        PListDict dict = null;
        for (String providerId : onboardingProviderIds) {
            OnboardingProvider provider = service.getProvider(providerId);
            if (OnboardingPlistProvider.class.isInstance(provider)) {
                dict = ((OnboardingPlistProvider) provider).getPlist(dict, new Scenario() {

                    @Override
                    public boolean isEnabled(int userId, int contextId) throws OXException {
                        return true;
                    }

                    @Override
                    public boolean isEnabled(Session session) throws OXException {
                        return true;
                    }

                    @Override
                    public String getId() {
                        return type;
                    }

                    @Override
                    public Icon getIcon(Session session) throws OXException {
                        return null;
                    }

                    @Override
                    public String getDisplayName(Session session) throws OXException {
                        return type;
                    }

                    @Override
                    public String getDisplayName(int userId, int contextId) throws OXException {
                        return type;
                    }

                    @Override
                    public String getDescription(Session session) throws OXException {
                        return null;
                    }

                    @Override
                    public OnboardingType getType() {
                        return OnboardingType.PLIST;
                    }

                    @Override
                    public List<OnboardingProvider> getProviders(int userId, int contextId) {
                        return Collections.singletonList(provider);
                    }

                    @Override
                    public List<OnboardingProvider> getProviders(Session session) {
                        return Collections.singletonList(provider);
                    }

                    @Override
                    public Link getLink() {
                        return null;
                    }

                    @Override
                    public List<String> getCapabilities(int userId, int contextId) {
                        return null;
                    }

                    @Override
                    public List<String> getCapabilities(Session session) {
                        return null;
                    }

                    @Override
                    public List<Scenario> getAlternatives(Session session) {
                        return null;
                    }
                }, requestData.getHostData().getHost(), session.getUserId(), session.getContextId());
            }
        }
        return new AJAXRequestResult(dict, "plist_download");
    }

}
