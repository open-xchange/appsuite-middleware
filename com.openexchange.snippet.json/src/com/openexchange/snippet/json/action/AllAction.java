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

import java.util.List;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetRequest;

/**
 * {@link AllAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction()
public final class AllAction extends SnippetAction {

    /**
     * Initializes a new {@link AllAction}.
     */
    public AllAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super(services, snippetServices, actions);
    }

    @Override
    protected AJAXRequestResult perform(final SnippetRequest snippetRequest) throws OXException, JSONException {
        final String[] types;
        {
            final String tmp = snippetRequest.getParameter("type", String.class, true);
            types = isEmpty(tmp) ? new String[0] : SPLIT_CSV.split(tmp);
        }
        final SnippetService snippetService = getSnippetService(snippetRequest.getSession());

        final List<Snippet> snippets = snippetService.getManagement(snippetRequest.getSession()).getSnippets(types);
        return new AJAXRequestResult(snippets, "snippet");
    }

    @Override
    public String getAction() {
        return "all";
    }

}
