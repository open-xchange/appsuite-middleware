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

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.client.onboarding.CompositeId;
import com.openexchange.client.onboarding.DefaultOnboardingRequest;
import com.openexchange.client.onboarding.OnboardingAction;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.ResultObject;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
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
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        OnboardingService onboardingService = getOnboardingService();

        // Check for composite identifier
        String sCompositeId = requestData.getParameter("id");
        if (Strings.isEmpty(sCompositeId)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }

        // Parse composite identifier
        CompositeId compositeId = OnboardingUtility.parseCompositeId(sCompositeId);

        // Check for action
        String sAction = requestData.getParameter("action_id");
        if (Strings.isEmpty(sAction)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("action_id");
        }

        OnboardingAction action = OnboardingAction.actionFor(sAction);
        if (null == action) {
            throw AjaxExceptionCodes.IMVALID_PARAMETER.create("action_id");
        }

        // Parse optional form content
        Map<String, Object> input = null;
        {
            Object data = requestData.getData();
            if (data instanceof JSONObject) {
                JSONObject jFormContent = (JSONObject) data;
                input = (Map<String, Object>) JSONCoercion.coerceToNative(jFormContent);
            }
        }

        Scenario scenario = onboardingService.getScenario(compositeId.getScenarioId(), session);

        // Create on-boarding request & execute it
        DefaultOnboardingRequest request = new DefaultOnboardingRequest(scenario, action, compositeId.getDevice(), requestData.getHostData(), input);
        ResultObject resultObject = onboardingService.execute(request, session);

        // Return result
        String format = resultObject.getFormat();
        if (null == format) {
            if (resultObject.hasWarnings()) {
                AJAXRequestResult result = new AJAXRequestResult(resultObject.getObject());
                result.addWarnings(resultObject.getWarnings());
            } else {
                return new AJAXRequestResult(resultObject.getObject());
            }
        }
        if ("file".equals(format)) {
            requestData.setFormat(format);
        }
        AJAXRequestResult result = new AJAXRequestResult(resultObject.getObject(), format);
        if (resultObject.hasWarnings()) {
            result.addWarnings(resultObject.getWarnings());
        }
        return result;
    }

}
