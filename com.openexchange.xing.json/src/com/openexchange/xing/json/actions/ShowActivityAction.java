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

import java.util.Collection;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.xing.UserField;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.json.XingRequest;
import com.openexchange.xing.session.WebAuthSession;


/**
 * {@link ShowActivityAction}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public final class ShowActivityAction extends AbstractXingAction {

    /**
     * Initializes a new {@link ShowActivityAction}.
     */
    public ShowActivityAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final XingRequest req) throws OXException, JSONException, XingException {
        final String activityId = getMandatoryStringParameter(req, "activity_id");
        // User Fields
        Collection<UserField> optUserFields = getUserFields(req.getParameter("user_fields"));
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
        Map<String, Object> activity = xingAPI.showActivity(activityId, optUserFields);
        JSONObject result = (JSONObject) JSONCoercion.coerceToJSON(activity);

        return new AJAXRequestResult(result);
    }

}
