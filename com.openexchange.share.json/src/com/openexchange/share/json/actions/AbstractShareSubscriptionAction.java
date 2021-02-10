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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.share.json.actions;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractShareSubscriptionAction}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public abstract class AbstractShareSubscriptionAction implements AJAXActionService {

    /**
     * The {@value #LINK} parameter
     */
    protected static final String LINK = "link";

    /**
     * The {@value #SERVICE_ID} parameter
     */
    protected static final String SERVICE_ID = "service";

    /**
     * The {@value #DISPLAY_NAME} parameter
     */

    protected static final String DISPLAY_NAME = "name";
    /**
     * The {@value #PASSWORD} parameter
     */
    protected static final String PASSWORD = "password";

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AnalyzeAction}.
     * 
     * @param services The service lookup
     */
    public AbstractShareSubscriptionAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            JSONObject json = (JSONObject) requestData.requireData();
            String link = json.getString(LINK);
            if (Strings.isEmpty(link)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(LINK);
            }
            return perform(requestData, session, json, link);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Performs given request.
     *
     * @param requestData The request to perform
     * @param session The session providing needed user data
     * @param json The JSON object transmitted in the request
     * @param shareLink The share link, never <code>null</code>
     * @return The result yielded for given request
     * @throws OXException If an error occurs
     * @throws JSONException in case of missing item
     */
    abstract AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session, JSONObject json, String shareLink) throws OXException, JSONException;

    /**
     * Get the {@value #SERVICE_ID} parameter
     *
     * @param requestData The data to get the parameter from
     * @param The name of the parameter
     * @return The parameter
     * @throws OXException If parameter is unset
     */
    protected String requireParameter(AJAXRequestData requestData, String name) throws OXException {
        String param = requestData.getParameter(name);
        if (Strings.isEmpty(param)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return param;
    }

    /**
     * Creates an AJAX response for the infos
     *
     * @param infos The infos to send to a client
     * @return The response
     * @throws JSONException In case setting of values fails
     */
    protected AJAXRequestResult createResponse(ShareSubscriptionInformation infos) throws JSONException {
        return createResponse(infos, new JSONObject(3));
    }

    /**
     * Creates an AJAX response for the infos
     *
     * @param infos The infos to send to a client
     * @param response The JSON response to add the data to
     * @return The response
     * @throws JSONException In case setting of values fails
     */
    protected AJAXRequestResult createResponse(ShareSubscriptionInformation infos, JSONObject response) throws JSONException {
        if (null != infos) {
            response.put("account", infos.getAccountId());
            response.put("module", infos.getModule());
            response.put("folder", infos.getFolder());
        }
        return new AJAXRequestResult(response, new Date(System.currentTimeMillis()));
    }
}
