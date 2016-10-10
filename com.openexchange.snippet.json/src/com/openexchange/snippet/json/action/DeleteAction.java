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

package com.openexchange.snippet.json.action;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteAction extends SnippetAction {

    private final List<Method> restMethods;

    /**
     * Initializes a new {@link DeleteAction}.
     *
     * @param services The service look-up
     */
    public DeleteAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super(services, snippetServices, actions);
        restMethods = Collections.singletonList(Method.DELETE);
    }

    @Override
    protected AJAXRequestResult perform(final SnippetRequest snippetRequest) throws OXException, JSONException {
        final JSONArray ids = (JSONArray) snippetRequest.getRequestData().getData();
        final SnippetService snippetService = getSnippetService(snippetRequest.getSession());
        if (null != ids) {
            final int length = ids.length();

            SnippetManagement management = snippetService.getManagement(snippetRequest.getSession());
            for (int i = 0; i < length; i++) {
                management.deleteSnippet(ids.getString(i));
            }
        } else {
            final String id = snippetRequest.checkParameter("id");
            snippetService.getManagement(snippetRequest.getSession()).deleteSnippet(id);
        }

        return new AJAXRequestResult(new JSONObject(0), "json");
    }

    @Override
    protected AJAXRequestResult performREST(final SnippetRequest snippetRequest, final Method method) throws OXException, JSONException, IOException {
        if (!Method.GET.equals(method)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        /*
         * REST style access
         */
        final AJAXRequestData requestData = snippetRequest.getRequestData();
        final String pathInfo = requestData.getPathInfo();
        if (isEmpty(pathInfo)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        final String[] pathElements = SPLIT_PATH.split(pathInfo);
        final int length = pathElements.length;
        if (0 == length) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        if (1 == length) {
            /*-
             * "Delete specific snippet"
             *  DELETE /snippet/11
             */
            final String element = pathElements[0];
            if (element.indexOf(',') < 0) {
                requestData.setAction("delete");
                requestData.putParameter("id", element);
            } else {
                requestData.setAction("delete");
                final JSONArray array = new JSONArray();
                for (final String id : SPLIT_CSV.split(element)) {
                    array.put(id);
                }
                requestData.setData(array);
            }
        } else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(pathInfo);
        }
        return actions.get(requestData.getAction()).perform(snippetRequest);
    }

    @Override
    public String getAction() {
        return "delete";
    }

    @Override
    public List<Method> getRESTMethods() {
        return restMethods;
    }

}
