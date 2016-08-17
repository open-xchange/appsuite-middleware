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

package com.openexchange.pns.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractPushJsonAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public abstract class AbstractPushJsonAction implements AJAXActionService {

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractPushJsonAction}.
     *
     * @param services The service look-up
     */
    protected AbstractPushJsonAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the specified field's string value.
     *
     * @param field The field name
     * @param jObject The JSON object to read from
     * @return The string value
     * @throws OXException If no such field is available
     */
    protected String requireStringField(String field, JSONObject jObject) throws OXException {
        String value = jObject.optString(field, null);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_FIELD.create(field);
        }
        return value;
    }

    /**
     * Gets the specified field's array value.
     *
     * @param field The field name
     * @param jObject The JSON object to read from
     * @return The array value
     * @throws OXException If no such field is available
     */
    protected JSONArray requireArrayField(String field, JSONObject jObject) throws OXException {
        JSONArray value = jObject.optJSONArray(field);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_FIELD.create(field);
        }
        return value;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            return doPerform(requestData, session);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the identifier of the action.
     *
     * @return The action identifier
     */
    public abstract String getAction();

    /**
     * Performs given request.
     *
     * @param requestData The request to perform
     * @param session The session providing needed user data
     * @return The result yielded for given request
     * @throws OXException If an error occurs
     */
    protected abstract AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException;

}
