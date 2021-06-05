/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.snippet.json.action;

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetExceptionCodes;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetJsonParser;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.snippet.utils.SnippetProcessor;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = SnippetAction.MODULE, type = RestrictedAction.Type.WRITE)
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
        ServerSession session = snippetRequest.getSession();
        SnippetService snippetService = getSnippetService(session);
        SnippetManagement management = snippetService.getManagement(session);

        {
            Snippet snippetToChange = management.getSnippet(id);
            if (!snippetToChange.isShared() && snippetToChange.getCreatedBy() != session.getUserId()) {
                throw SnippetExceptionCodes.UPDATE_DENIED.create(id, I(session.getUserId()), I(session.getContextId()));
            }
        }

        JSONObject jsonSnippet = (JSONObject) snippetRequest.getRequestData().getData();
        if (null == jsonSnippet) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        // Parse from JSON to snippet
        DefaultSnippet snippet = new DefaultSnippet();
        Set<Property> properties = EnumSet.noneOf(Property.class);
        SnippetJsonParser.parse(jsonSnippet, snippet, properties);

        // Process image in an <img> tag and add it as an attachment
        String contentSubType = getContentSubType(snippet);
        List<Attachment> attachments = Collections.<Attachment> emptyList();
        List<String> managedFileIds = null;
        if (contentSubType.equals("html")) {
            attachments = new LinkedList<Attachment>();
            managedFileIds = new SnippetProcessor(session).processImages(snippet, attachments);
        }

        // Update
        String newId = management.updateSnippet(id, snippet, properties, attachments, Collections.<Attachment> emptyList());
        Snippet newSnippet = management.getSnippet(newId);
        AJAXRequestResult requestResult = new AJAXRequestResult(newSnippet, "snippet");

        if (null != managedFileIds && false == managedFileIds.isEmpty()) {
            ManagedFileManagement fileManagement = services.getOptionalService(ManagedFileManagement.class);
            if (null != fileManagement) {
                for (String managedFileId : managedFileIds) {
                    try {
                        fileManagement.removeByID(managedFileId);
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }

        return requestResult;
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
