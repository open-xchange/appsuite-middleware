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

package com.openexchange.find.json.actions;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.EnqueuableAJAXActionService;
import com.openexchange.ajax.requesthandler.jobqueue.JobKey;
import com.openexchange.exception.OXException;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.SearchService;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.json.FindRequest;
import com.openexchange.find.json.Offset;
import com.openexchange.find.json.QueryResult;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link QueryAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class QueryAction extends AbstractFindAction implements EnqueuableAJAXActionService {

    /**
     * Initializes a new {@link QueryAction}.
     *
     * @param services The service look-up
     */
    public QueryAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    public EnqueuableAJAXActionService.Result isEnqueueable(AJAXRequestData request, ServerSession session) throws OXException {
        try {
            String module = request.requireParameter("module");

            JSONObject data = (JSONObject) request.requireData();
            long start = data.getLong("start");
            long size = data.getLong("size");
            JSONArray jFacets = data.getJSONArray("facets");

            JSONObject jKeyDesc = new JSONObject(4);
            jKeyDesc.put("module", "find");
            jKeyDesc.put("action", "query");
            jKeyDesc.put("findModule", module);
            jKeyDesc.put("start", start);
            jKeyDesc.put("size", size);
            jKeyDesc.put("facets", jFacets);

            return EnqueuableAJAXActionService.resultFor(true, new JobKey(session.getUserId(), session.getContextId(), jKeyDesc.toString()), this);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    protected AJAXRequestResult doPerform(final FindRequest request) throws OXException, JSONException {
        final SearchService searchService = getSearchService();
        final String[] columns = request.getColumns();
        final Module module = request.requireModule();
        final Offset offset = request.getOffset();
        if (offset.len <= 0) {
            return new AJAXRequestResult(SearchResult.EMPTY, SearchResult.class.getName());
        }

        final List<ActiveFacet> activeFacets = request.getActiveFacets();
        Map<String, String> options = request.getOptions();
        final SearchRequest searchRequest = new SearchRequest(offset.off, offset.len, activeFacets, options, columns);
        final SearchResult searchResult = searchService.search(searchRequest, module, request.getServerSession());
        return new AJAXRequestResult(new QueryResult(searchRequest, searchResult), QueryResult.class.getName());
    }

}
