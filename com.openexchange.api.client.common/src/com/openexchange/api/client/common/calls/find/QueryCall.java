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

package com.openexchange.api.client.common.calls.find;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.http.HttpEntity;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractPutCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;

/**
 * {@link QueryCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @param <O> The type of the result objects
 * @param <E> The type of the result fields
 * @since v7.10.5
 */
public class QueryCall<O, E extends Enum<E>> extends AbstractPutCall<FindResponse<O>> {

    private final String module;
    private final int[] columns;
    private final String[] calendarFields;
    private final QueryBody query;
    private final DefaultJsonMapper<O, E> resultObjectMapper;

    /**
     * {@link QueryBody} - represents a search query
     */
    public static class QueryBody {

        List<Facet> facets;
        QueryOptions queryOptions;
        Integer start;
        Integer size;
    }

    /**
     * {@link QueryOptions} - Contains various options to control how the search query is performed
     */
    public static class QueryOptions {

        String timezone;
        Boolean includeSubfolders;
        Integer sortingField;
        SortOrder order;
    }

    /**
     * {@link Facet} - A search facet
     */
    public static class Facet {

        String facetId;
        String value;
        FacetFilter filter;
    }

    /**
     * {@link FacetFilter} - A search filter
     */
    public static class FacetFilter {

        List<String> fields;
        List<String> queries;

        /**
         * Sets the fields of the filter
         *
         * @param fields The fields
         * @return this
         */
        public FacetFilter setFields(String... fields) {
            if (this.fields == null) {
                this.fields = new ArrayList<String>(fields.length);
            }
            this.fields.addAll(Arrays.asList(fields));
            return this;
        }

        /**
         * Sets the queries of the filter
         *
         * @param queries The queries
         * @return this
         */
        public FacetFilter setQueries(String... queries) {
            if (this.queries == null) {
                this.queries = new ArrayList<String>(queries.length);
            }
            this.queries.addAll(Arrays.asList(queries));
            return this;
        }
    }

    /**
     * {@link QueryBuilder} A builder for {@link QueryBody} instances
     */
    public static class QueryBuilder {

        private final QueryBody queryBody = new QueryBody();

        /**
         * Sets the start offset for pagination
         *
         * @param start The start offset
         * @return this
         */
        public QueryBuilder withStart(int start) {
            queryBody.start = I(start);
            return this;
        }

        /**
         * Sets the size of the page
         *
         * @param size The size of the page
         * @return this
         */
        public QueryBuilder withSize(int size) {
            queryBody.size = I(size);
            return this;
        }

        /**
         * Sets the timezone
         *
         * @param timezone The timezone
         * @return this
         */
        public QueryBuilder withTimezone(String timezone) {
            if (queryBody.queryOptions == null) {
                queryBody.queryOptions = new QueryOptions();
            }
            queryBody.queryOptions.timezone = timezone;
            return this;
        }

        /**
         * Sets whether or not to include subfolders
         *
         * @param includeSubfolders <code>true</code> to search also in all subfolders, <code>false</code> otherwise
         * @return this
         */
        public QueryBuilder includeSubfolders(boolean includeSubfolders) {
            if (queryBody.queryOptions == null) {
                queryBody.queryOptions = new QueryOptions();
            }
            queryBody.queryOptions.includeSubfolders = B(includeSubfolders);
            return this;
        }

        /**
         * Sets the sorting field
         *
         * @param sortFieldId The ID of the sorting field
         * @return this
         */
        public QueryBuilder sortBy(int sortFieldId) {
            if (queryBody.queryOptions == null) {
                queryBody.queryOptions = new QueryOptions();
            }
            queryBody.queryOptions.sortingField = I(sortFieldId);
            return this;
        }

        /**
         * Sets the sort order
         *
         * @param order The sort order
         * @return this
         */
        public QueryBuilder withSortOrder(SortOrder order) {
            if (queryBody.queryOptions == null) {
                queryBody.queryOptions = new QueryOptions();
            }
            queryBody.queryOptions.order = order;
            return this;
        }

        /**
         * Adds a facet to the search
         *
         * @param id The id of the facet
         * @param value The value of the facet
         * @return this
         */
        public QueryBuilder withFacet(String id, String value) {
            return withFacet(id, value, null);
        }

        /**
         * Adds a facet to the search
         *
         * @param id The id of the facet
         * @param value The value of the facet
         * @param filter The filter
         * @return this
         */
        public QueryBuilder withFacet(String id, String value, FacetFilter filter) {
            if (queryBody.facets == null) {
                queryBody.facets = new ArrayList<Facet>();
            }
            Facet facet = new Facet();
            facet.facetId = id;
            facet.value = value;
            facet.filter = filter;
            queryBody.facets.add(facet);
            return this;
        }

        /**
         * Builds the query
         *
         * @return The query
         */
        public QueryBody build() {
            return queryBody;
        }
    }

    /**
     * Initializes a new {@link QueryCall}.
     *
     * @param module The module to search
     * @param columns The list of column IDs that should be returned in the response objects
     * @param query The query
     * @param resultMapper The mapper used for mapping the results
     */
    public QueryCall(String module, int[] columns, QueryBody query, DefaultJsonMapper<O, E> resultMapper) {
        this(module, columns, null, query, resultMapper);
    }

    /**
     * Initializes a new {@link QueryCall}.
     *
     * @param module The module to search
     * @param calendarFields A comma-separated list of field identifiers. This parameter must be used instead of columns in case the module is set to 'calendar
     * @param query The query
     * @param resultMapper The mapper used for mapping the results
     */
    public QueryCall(String module, String[] calendarFields, QueryBody query, DefaultJsonMapper<O, E> resultMapper) {
        this(module, null, calendarFields, query, resultMapper);
    }

    /**
     * Initializes a new {@link QueryCall}.
     *
     * @param module The module to search
     * @param columns The list of column IDs that should be returned in the response objects
     * @param calendarFields A comma-separated list of field identifiers. This parameter must be used instead of columns in case the module is set to 'calendar
     * @param query The query
     * @param resultMapper The mapper used for mapping the results
     */
    public QueryCall(String module, int[] columns, String[] calendarFields, QueryBody query, DefaultJsonMapper<O, E> resultMapper) {
        this.module = Objects.requireNonNull(module, "module must not be null");
        this.columns = columns;
        this.calendarFields = calendarFields;
        this.query = Objects.requireNonNull(query, "query must not be null");
        this.resultObjectMapper = Objects.requireNonNull(resultMapper, "resultMapper must not be null");
    }

    @Override
    @NonNull
    public String getModule() {
        return "/find";
    }

    /**
     * Creates a {@link JSONObject} for the given filter
     *
     * @param filter The filter to get the JSON for
     * @return The filter as JSON
     * @throws JSONException
     */
    private JSONObject toJSON(FacetFilter filter) throws JSONException {
        JSONObject jsonFilter = new JSONObject();
        if (filter.fields != null) {
            JSONArray jsonFields = new JSONArray(filter.fields.size());
            for (int i = 0; i < filter.fields.size(); i++) {
                jsonFields.add(i, filter.fields.get(i));
            }
            jsonFilter.put("fields", jsonFields);
        }

        if (filter.queries != null) {
            JSONArray jsonQueries = new JSONArray(filter.queries.size());
            for (int i = 0; i < filter.queries.size(); i++) {
                jsonQueries.add(i, filter.queries.get(i));
            }
            jsonFilter.put("queries", jsonQueries);
        }
        return jsonFilter;
    }

    /**
     * Creates a {@link JSONArray} for the given facets
     *
     * @param facets A list of facets to get the JSON for
     * @return The filter as JSONArray
     * @throws JSONException
     */
    private JSONArray toJSON(List<Facet> facets) throws JSONException {
        JSONArray jsonFacets = new JSONArray(facets.size());
        for (int i = 0; i < facets.size(); i++) {
            Facet f = facets.get(i);
            JSONObject jsonFacet = new JSONObject();
            if (f.facetId != null) {
                jsonFacet.put("facet", f.facetId);
            }
            if (f.value != null) {
                jsonFacet.put("value", f.value);
            }
            if (f.filter != null) {
                jsonFacet.put("filter", toJSON(f.filter));
            }
            jsonFacets.add(i, jsonFacet);

        }
        return jsonFacets;
    }

    /**
     * Creates a {@link JSONObject} for the given options
     *
     * @param options The options to get the JSON for
     * @return The options as JSON
     * @throws JSONException
     */
    private JSONObject toJSON(QueryOptions options) throws JSONException {
        final JSONObject json = new JSONObject();
        if (options.timezone != null) {
            json.put("timezone", options.timezone);
        }

        if (options.includeSubfolders != null) {
            json.put("includeSubfolders", options.includeSubfolders);
        }

        if (options.sortingField != null) {
            json.put("sort", String.valueOf(options.sortingField));
        }

        if (options.order != null) {
            json.put("order", options.order.toString().toLowerCase());
        }

        return json;
    }

    /**
     * Creates a {@link JSONObject} for the given query
     *
     * @param query The query to get the JSON for
     * @return The query as JSON
     * @throws JSONException
     */
    private JSONObject toJSON(QueryBody query) throws JSONException {
        final JSONObject jsonQueryBody = new JSONObject();
        if (query.facets != null) {
            jsonQueryBody.put("facets", toJSON(query.facets));
        }
        if (query.queryOptions != null) {
            jsonQueryBody.put("options", toJSON(query.queryOptions));
        }
        if (query.start != null) {
            jsonQueryBody.put("start", query.start);
        }
        if (query.size != null) {
            jsonQueryBody.put("size", query.size);
        }
        return jsonQueryBody;
    }

    /**
     * Gets the columns
     *
     * @return The columns
     */
    int[] getColumns() {
        return this.columns;
    }

    /**
     * Gets the calendar fields
     *
     * @return The calendar fields
     */
    String[] getCalendarFields() {
        return this.calendarFields;
    }

    /**
     * Gets the resultObjectMapper
     *
     * @return The resultObjectMapper
     */
    DefaultJsonMapper<O, E> getResultObjectMapper() {
        return resultObjectMapper;
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        return ApiClientUtils.createJsonBody(toJSON(query));
    }

    @Override
    public HttpResponseParser<FindResponse<O>> getParser() {
        return new AbstractHttpResponseParser<FindResponse<O>>() {

            @Override
            public FindResponse<O> parse(CommonApiResponse response, HttpContext httpContext) throws OXException, JSONException {
                JSONObject jsonObject = response.getJSONObject();

                int found = 0, start = 0, size = 0;
                List<O> results = Collections.emptyList();

                if (jsonObject.has("results")) {
                    final int[] columns = getColumns();
                    final String[] calendarFields = getCalendarFields();
                    final JSONArray resultArray = jsonObject.getJSONArray("results");
                    final DefaultJsonMapper<O, E> resultObjectMapper = getResultObjectMapper();
                    //@formatter:off
                        final E[] mappedFields = columns != null ?
                            resultObjectMapper.getMappedFields(columns) :
                            resultObjectMapper.getMappedFields(calendarFields);
                    //@formatter:on
                    if (null == mappedFields) {
                        throw new IllegalArgumentException("mappedFields");
                    }
                    results = new ArrayList<>(resultArray.length());
                    for (int i = 0; i < resultArray.length(); i++) {
                        results.add(resultObjectMapper.deserialize(resultArray.getJSONObject(i), mappedFields));
                    }
                }

                if (jsonObject.has("num_found")) {
                    found = jsonObject.getInt("num_found");
                }

                if (jsonObject.has("start")) {
                    start = jsonObject.getInt("start");
                }

                if (jsonObject.has("size")) {
                    size = jsonObject.getInt("size");
                }

                return new FindResponse<O>(found, start, size, results);
            }
        };
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("module", module);
        putIfPresent(parameters, "columns", ApiClientUtils.toCommaString(columns));
        putIfPresent(parameters, "fields", ApiClientUtils.toCommaString(calendarFields));
    }

    @Override
    protected String getAction() {
        return "query";
    }
}
