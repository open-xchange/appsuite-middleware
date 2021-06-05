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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetJsonParser;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.snippet.utils.SnippetProcessor;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = SnippetAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class NewAction extends SnippetAction {

    private final List<Method> restMethods;

    /**
     * Initializes a new {@link NewAction}.
     *
     * @param services The service look-up
     */
    public NewAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super(services, snippetServices, actions);
        restMethods = Collections.singletonList(Method.POST);
    }

    @Override
    protected AJAXRequestResult perform(final SnippetRequest snippetRequest) throws OXException, JSONException {
        JSONObject jsonSnippet = (JSONObject) snippetRequest.getRequestData().getData();
        if (null == jsonSnippet) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        // TESTING: UI should set the key "content-type" in order to specify the content type of the snippet, if none provided then
        // text/plain would be set as default
        // jsonSnippet.getJSONObject("misc").put("content-type", "text/html");
        // Parse from JSON to snippet
        DefaultSnippet snippet = new DefaultSnippet();
        SnippetJsonParser.parse(jsonSnippet, snippet);

        // Check for needed fields
        if (isEmpty(snippet.getDisplayName())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(Property.DISPLAY_NAME.getPropName());
        }
        if (isEmpty(snippet.getType())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(Property.TYPE.getPropName());
        }
        if (isEmpty(snippet.getModule())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(Property.MODULE.getPropName());
        }
        if (snippet.getContent() == null) {
            // Set to empty string
            snippet.setContent("");
        }
        List<Attachment> attachments = snippet.getAttachments();
        if (null != attachments) {
            for (final Attachment attachment : attachments) {
                if (null == attachment.getId()) {
                    ((DefaultAttachment) attachment).setId(UUID.randomUUID().toString());
                }
            }
        }

        // Process image in an img HTML tag and add it as an attachment
        ServerSession session = snippetRequest.getSession();
        String contentSubType = getContentSubType(snippet);
        List<String> managedFileIds = null;
        if (contentSubType.equals("html")) {
            managedFileIds = new SnippetProcessor(session).processImages(snippet);
        }

        // Create via management
        String id = getSnippetService(session).getManagement(session).createSnippet(snippet);
        AJAXRequestResult requestResult = new AJAXRequestResult(id, "string");

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
        if (!Method.POST.equals(method)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        /*
         * REST style access
         */
        final AJAXRequestData requestData = snippetRequest.getRequestData();
        final String pathInfo = requestData.getPathInfo();
        if (isEmpty(pathInfo)) {
            requestData.setAction("new");
        } else {
            final String[] pathElements = SPLIT_PATH.split(pathInfo);
            final int length = pathElements.length;
            if (0 < length) {
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(pathInfo);
            }
            requestData.setAction("new");
        }
        return actions.get(requestData.getAction()).perform(snippetRequest);
    }

    @Override
    public String getAction() {
        return "new";
    }

    @Override
    public List<Method> getRESTMethods() {
        return restMethods;
    }

}
