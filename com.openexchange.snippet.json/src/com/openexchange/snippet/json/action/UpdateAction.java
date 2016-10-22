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
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.snippet.SnippetProcessor;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetJsonParser;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateAction extends SnippetAction {

    private final List<Method> restMethods;

    /**
     * Initializes a new {@link UpdateAction}.
     *
     * @param services The service look-up
     */
    public UpdateAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super(services, snippetServices, actions);
        restMethods = Collections.singletonList(Method.PUT);
    }

    @Override
    protected AJAXRequestResult perform(final SnippetRequest snippetRequest) throws OXException, JSONException {
        String id = snippetRequest.checkParameter("id");
        JSONObject jsonSnippet = (JSONObject) snippetRequest.getRequestData().getData();
        if (null == jsonSnippet) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        // Parse from JSON to snippet
        DefaultSnippet snippet = new DefaultSnippet();
        Set<Property> properties = EnumSet.noneOf(Property.class);
        SnippetJsonParser.parse(jsonSnippet, snippet, properties);

        // Process image in an <img> tag and add it as an attachment
        ServerSession session = snippetRequest.getSession();
        String contentSubType = getContentSubType(snippet);
        List<Attachment> attachments = Collections.<Attachment> emptyList();
        if (contentSubType.equals("html")) {
            attachments = new LinkedList<Attachment>();
            new SnippetProcessor(session).processImages(snippet, attachments);
        }

        // Update
        SnippetManagement management = getSnippetService(session).getManagement(session);
        String newId = management.updateSnippet(id, snippet, properties, attachments, Collections.<Attachment> emptyList());
        Snippet newSnippet = management.getSnippet(newId);
        return new AJAXRequestResult(newSnippet, "snippet");
    }

    @Override
    protected AJAXRequestResult performREST(final SnippetRequest snippetRequest, final Method method) throws OXException, JSONException, IOException {
        if (!Method.PUT.equals(method)) {
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
        if (1 < length) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(pathInfo);
        }
        /*-
         * "Update specific snippet"
         *  PUT /snippet/11
         */
        final String element = pathElements[0];
        if (element.indexOf(',') >= 0) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        requestData.setAction("update");
        requestData.putParameter("id", element);
        return actions.get(requestData.getAction()).perform(snippetRequest);
    }

    @Override
    public String getAction() {
        return "update";
    }

    @Override
    public List<Method> getRESTMethods() {
        return restMethods;
    }

}
