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

package com.openexchange.xing.json.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.json.XingRequest;
import com.openexchange.xing.session.WebAuthSession;

/**
 * {@link FindByMailsRequestAction}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FindByMailsRequestAction extends AbstractXingAction {

	/**
	 * Initializes a new {@link FindByMailsRequestAction}.
	 */
	public FindByMailsRequestAction(ServiceLookup services) {
		super(services);
	}

    @Override
    protected AJAXRequestResult perform(XingRequest req) throws OXException, JSONException, XingException {
        Object objData = req.getRequest().getData();
        if (objData == null) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        if (!(objData instanceof JSONObject)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }

        JSONObject jsonData = (JSONObject) objData;
        JSONArray jsonArray = jsonData.optJSONArray("emails");
        if(jsonArray == null) {
        	throw XingExceptionCodes.MANDATORY_REQUEST_DATA_MISSING.create("emails");
        }

        int length = jsonArray.length();
        List<String> emails = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            String email = jsonArray.getString(i);
            email = validateMailAddress(email);
            emails.add(email);
        }

        String token = req.getParameter("testToken");
        String secret = req.getParameter("testSecret");

        final XingOAuthAccess xingOAuthAccess;
        {
            if (!Strings.isEmpty(token) && !Strings.isEmpty(secret)) {
                xingOAuthAccess = getXingOAuthAccess(token, secret, req.getSession());
            } else {
                xingOAuthAccess = getXingOAuthAccess(req);
            }
        }

		XingAPI<WebAuthSession> xingAPI = xingOAuthAccess.getXingAPI();
		Map<String, Object> xingUser = xingAPI.findByEmailsGetXingAttributes(emails);

		if (null == xingUser || xingUser.isEmpty()) {
		    return new AJAXRequestResult(new JSONObject(0));
        }

		final JSONObject result = (JSONObject) JSONCoercion.coerceToJSON(xingUser);
		return new AJAXRequestResult(result);
	}
}
