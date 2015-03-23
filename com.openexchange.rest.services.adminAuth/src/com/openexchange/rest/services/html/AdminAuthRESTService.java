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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.rest.services.html;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.auth.Authenticator;
import com.openexchange.auth.Credentials;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.OXRESTService;
import com.openexchange.rest.services.annotations.PUT;
import com.openexchange.rest.services.annotations.ROOT;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link AdminAuthRESTService} allows clients to process HTML content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@ROOT("/adminproc/v1")
public class AdminAuthRESTService extends OXRESTService<Authenticator> {

    /**
     * Initializes a new {@link AdminAuthRESTService}.
     */
    public AdminAuthRESTService() {
        super();
    }

    /**
     * <pre>
     * PUT /rest/adminproc/v1/adminAuth
     * { ... }
     * </pre>
     *
     */
    @PUT("/adminAuth")
    public Object doAdminAuth() throws OXException {
        Object data = request.getData();

        JSONObject jRequest;
        if (data instanceof String) {
            try {
                jRequest = new JSONObject((String) data);
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        } else if (data instanceof JSONObject) {
            jRequest = (JSONObject) data;
        } else {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        if (false == jRequest.hasAndNotNull("login")) {
            throw AjaxExceptionCodes.MISSING_FIELD.create("login");
        }

        if (false == jRequest.hasAndNotNull("password")) {
            throw AjaxExceptionCodes.MISSING_FIELD.create("password");
        }

        try {
            int contextId = jRequest.optInt("context", 0);
            if (contextId <= 0) {
                context.doAuthentication(new Credentials(jRequest.getString("login"), jRequest.getString("password")));
            } else {
                context.doAuthentication(new Credentials(jRequest.getString("login"), jRequest.getString("password")), contextId);
            }
            return new JSONObject(2).put("result", true);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            return new JSONObject(2).putSafe("result", Boolean.FALSE);
        }
    }

}
