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

package com.openexchange.jslob.json.rest.jslob;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.json.rest.AbstractMethodHandler;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link PostMethodHandler} - Serves the REST-like <code>POST</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PostMethodHandler extends AbstractMethodHandler {

    /**
     * Initializes a new {@link PostMethodHandler}.
     */
    public PostMethodHandler() {
        super();
    }

    @Override
    protected String getModule() {
        return "jslob";
    }

    // POST /jslob/id/<path> (set)

    @Override
    protected void parseByPathInfo(final AJAXRequestData requestData, final String pathInfo, final HttpServletRequest req) throws IOException, OXException {
        // E.g. pathInfo="11" (preceding "jslob" removed)
        if (com.openexchange.java.Strings.isEmpty(pathInfo)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        final String[] pathElements = SPLIT_PATH.split(pathInfo);
        final int length = pathElements.length;
        if (0 == length) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        if (1 == length) {
            /*-
             *  PUT /jslob/11
             */
            requestData.setAction("update");
            requestData.putParameter("id", pathElements[0]);
        } else if (2 == length) {
            /*-
             *  PUT /jslob/11/<path>
             */
            requestData.setAction("update");
            requestData.putParameter("id", pathElements[0]);
            try {
                final JSONObject jObject = new JSONObject();
                jObject.put("path", pathElements[1]);
                jObject.put("value", requestData.getData());
                requestData.setData(jObject, "json");
            } catch (final JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        } else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(pathInfo);
        }
    }

    @Override
    protected boolean shouldApplyBody() {
        return true;
    }

}
