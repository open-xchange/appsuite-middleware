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

package com.openexchange.onboarding.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.datatypes.genericonf.json.FormContentWriter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.onboarding.DefaultClientInfo;
import com.openexchange.onboarding.DefaultOnboardingRequest;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.service.OnboardingConfigurationService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ExecuteAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class ExecuteAction extends AbstractOnboardingAction {

    /**
     * Initializes a new {@link ExecuteAction}.
     * @param services
     */
    public ExecuteAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        OnboardingConfigurationService onboardingService = getOnboardingService();

        String configurationId = requestData.getParameter("configurationId");
        if (Strings.isEmpty(configurationId)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("configurationId");
        }

        OnboardingConfiguration config = onboardingService.getConfiguration(configurationId);

        String selectionId = requestData.getParameter("selectionId");
        if (Strings.isEmpty(selectionId)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("selectionId");
        }

        Result onboardingResult = config.execute(new DefaultOnboardingRequest(configurationId, selectionId, new DefaultClientInfo(AJAXRequestDataTools.getUserAgent(requestData))), session);

        try {
            if (null != onboardingResult.getFormConfiguration() && null != onboardingResult.getFormDescription()) {
                return new AJAXRequestResult(FormContentWriter.write(onboardingResult.getFormDescription(), onboardingResult.getFormConfiguration(), null), "json");
            }

            JSONObject jResult = new JSONObject(2);
            jResult.put("result", onboardingResult.getResultText());
            return new AJAXRequestResult(jResult, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
