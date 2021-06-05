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

package com.openexchange.ajax.find.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.find.Document;
import com.openexchange.find.SearchResult;
import com.openexchange.find.facet.ActiveFacet;

/**
 * {@link QueryRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class QueryRequest extends AbstractFindRequest<QueryResponse> {

    private final boolean failOnError;
    private final int start;
    private final int size;
    private final List<ActiveFacet> activeFacets;
    private final String module;
    private final String[] columns;

    /**
     * Initializes a new {@link QueryRequest}.
     */
    public QueryRequest(int start, int size, List<ActiveFacet> activeFacets, String module) {
        this(true, start, size, activeFacets, null, module, null);
    }

    public QueryRequest(int start, int size, List<ActiveFacet> activeFacets, String module, String[] columns) {
        this(true, start, size, activeFacets, null, module, columns);
    }

    /**
     * Initializes a new {@link QueryRequest}.
     */
    public QueryRequest(boolean failOnError, int start, int size, List<ActiveFacet> activeFacets, Map<String, String> options, String module, String[] columns) {
        super(options);
        this.failOnError = failOnError;
        this.start = start;
        this.size = size;
        this.activeFacets = activeFacets;
        this.module = module;
        this.columns = columns;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "query"));
        list.add(new Parameter("module", module));
        if (columns != null) {
            list.add(new Parameter("columns", columns));
        }
        return list.toArray(new Parameter[0]);
    }

    @Override
    public AbstractAJAXParser<? extends QueryResponse> getParser() {
        return new QueryParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        final JSONObject jBody = new JSONObject(3);
        addFacets(jBody, activeFacets);
        addOptions(jBody);

        // Add size / start if present
        if (0 < start) {
            jBody.put("start", start);
        }
        if (0 < size) {
            jBody.put("size", size);
        }

        return jBody;
    }

    private static class QueryParser extends AbstractAJAXParser<QueryResponse> {

        /**
         * Initializes a new {@link AutocompleteParser}.
         */
        protected QueryParser(final boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected QueryResponse createResponse(final Response response) throws JSONException {
            final JSONObject jResponse = (JSONObject) response.getData();

            final int numFound = jResponse.optInt("numFound", -1);
            final int from = jResponse.optInt("from", -1);

            final JSONArray jDocuments = jResponse.getJSONArray("results");
            final int len = jDocuments.length();
            final List<Document> documents = new ArrayList<Document>(len);
            for (int i = 0; i < len; i++) {
                documents.add(new PropDocument(jDocuments.getJSONObject(i).asMap()));
            }

            return new QueryResponse(response, new SearchResult(numFound, from, documents, Collections.<ActiveFacet> emptyList()));
        }
    }

}
