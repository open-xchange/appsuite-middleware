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

package com.openexchange.user.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link SetAttributeAction} - Maps the action to an <tt>update</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "setAttribute", description = "Set user attribute (available with v6.20).", parameters = {
		@Parameter(name = "session", description = "A session ID previously obtained from the login module."),
		@Parameter(name = "id", description = "ID of the user."),
		@Parameter(name = "setIfAbsent", description = "Set to \"true\" to put the value only if the specified name is not already associated with a value, otherwise \"false\" to put value in any case.")
}, requestBody = "A JSON object providing name and value of the attribute. If the \"value\" field id missing or NULL, the attribute is removed.",
responseDescription = "The boolean value \"true\" if PUT was successful; otherwise \"false\".")
public final class SetAttributeAction extends AbstractUserAction {

    /**
     * The <tt>setAttribute</tt> action string.
     */
    public static final String ACTION = "setAttribute";

    /**
     * Initializes a new {@link SetAttributeAction}.
     */
    public SetAttributeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            final int id = checkIntParameter(AJAXServlet.PARAMETER_ID, request);
            final boolean setIfAbsent = Boolean.parseBoolean(request.getParameter("setIfAbsent"));
            /*
             * Get user service
             */
            final UserService userService = services.getService(UserService.class);
            /*
             * Parse attribute JSON object
             */
            final JSONObject jData = (JSONObject) request.requireData();
            // Check if we are allowed to overwrite an existing attribute.
            final String name = jData.getString("name");
            final Context context = session.getContext();
            if (setIfAbsent) {
                final String prevValue = userService.getUserAttribute(name, id, context);
                if (null != prevValue) {
                    return new AJAXRequestResult(Boolean.FALSE);
                }
            }
            // Parse the value.
            Object tmp = jData.opt("value");
            final String value;
            if (null == tmp) {
                // HTTP/JSON API allows to omit the value attribute in the JSON object.
                value = null;
            } else if (JSONObject.NULL.equals(tmp)) {
                // HTTP/JSON API allows to sent JSON null for the value of the attribute.
                value = null;
            } else {
                // Normal new value for the attribute.
                value = tmp.toString();
            }
            // Apply the parsed value.
            userService.setUserAttribute(name, value, id, context);
            // Return
            return new AJAXRequestResult(Boolean.TRUE);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

}
