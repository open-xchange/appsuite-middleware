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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetExceptionCodes;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = SnippetAction.MODULE, type = RestrictedAction.Type.WRITE)
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
        ServerSession session = snippetRequest.getSession();
        SnippetService snippetService = getSnippetService(session);

        JSONArray ids = (JSONArray) snippetRequest.getRequestData().getData();
        if (null != ids) {
            int length = ids.length();

            SnippetManagement management = snippetService.getManagement(session);
            List<String> toDelete = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                String id = ids.getString(i);
                try {
                    Snippet snippetToChange = management.getSnippet(id);
                    if (!snippetToChange.isShared() && snippetToChange.getCreatedBy() != session.getUserId()) {
                        throw SnippetExceptionCodes.UPDATE_DENIED.create(id, I(session.getUserId()), I(session.getContextId()));
                    }

                    toDelete.add(id);
                } catch (OXException e) {
                    if (!SnippetExceptionCodes.SNIPPET_NOT_FOUND.equals(e)) {
                        throw e;
                    }
                }
            }

            for (String id : toDelete) {
                management.deleteSnippet(id);
            }
        } else {
            String id = snippetRequest.checkParameter("id");
            SnippetManagement management = snippetService.getManagement(session);

            {
                Snippet snippetToChange = management.getSnippet(id);
                if (!snippetToChange.isShared() && snippetToChange.getCreatedBy() != session.getUserId()) {
                    throw SnippetExceptionCodes.UPDATE_DENIED.create(id, I(session.getUserId()), I(session.getContextId()));
                }
            }

            management.deleteSnippet(id);
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
