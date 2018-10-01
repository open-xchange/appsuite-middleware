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

import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DownloadLinkAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class DownloadLinkAction extends AbstractOnboardingAction {

    public DownloadLinkAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        String type = requestData.requireParameter("type");
        switch (type) {
            case "caldav":
                if (!OnboardingUtility.hasCapability(Permission.CALDAV.getCapabilityName(), session)) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALDAV);
                }
                if (!session.getUserPermissionBits().hasCalendar()) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CALENDAR);
                }
                break;
            case "carddav":
                if (!OnboardingUtility.hasCapability(Permission.CARDDAV.getCapabilityName(), session)) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CARDDAV);
                }
                if (!session.getUserPermissionBits().hasContact()) {
                    throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create(Permission.CONTACTS);
                }
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
                break;
            default:
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", type);
        }
        DownloadLinkProvider linkProvider = services.getService(DownloadLinkProvider.class);
        String link = linkProvider.getLink(requestData.getHostData(), session.getUserId(), session.getContextId(), type, Device.APPLE_IPHONE.getId());
        return new AJAXRequestResult(link, "json");

    }

}
