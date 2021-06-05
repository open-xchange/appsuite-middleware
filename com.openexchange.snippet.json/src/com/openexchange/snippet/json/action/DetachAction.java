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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetExceptionCodes;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetJsonParser;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DetachAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = SnippetAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class DetachAction extends SnippetAction {

    /**
     * Initializes a new {@link DetachAction}.
     */
    public DetachAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super(services, snippetServices, actions);
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

        final DefaultSnippet snippet = new DefaultSnippet().setId(id);
        final JSONArray jsonAttachments = (JSONArray) snippetRequest.getRequestData().requireData();
        int length = jsonAttachments.length();
        final List<Attachment> attachments = new ArrayList<Attachment>(length);
        for (int i = 0; i < length; i++) {
            final DefaultAttachment attachment = new DefaultAttachment();
            SnippetJsonParser.parse(jsonAttachments.getJSONObject(i), attachment);
            attachments.add(attachment);
        }
        /*
         * Update
         */
        String newId = management.updateSnippet(id, snippet, Collections.<Property> emptySet(), Collections.<Attachment> emptyList(), attachments);
        return new AJAXRequestResult(newId, "string");
    }

    @Override
    public String getAction() {
        return "detach";
    }

}
