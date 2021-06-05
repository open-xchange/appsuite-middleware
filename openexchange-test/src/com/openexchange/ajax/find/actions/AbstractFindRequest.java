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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Filter;

/**
 * {@link AbstractFindRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFindRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    /**
     * URL of the find AJAX interface.
     */
    public static final String FIND_URL = "/ajax/find";

    private final Map<String, String> options;

    /**
     * Initializes a new {@link AbstractFindRequest}.
     */
    protected AbstractFindRequest(final Map<String, String> options) {
        super();
        this.options = options;
    }

    @Override
    public String getServletPath() {
        return FIND_URL;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    protected void addOptions(JSONObject jBody) throws JSONException {
        if (options != null) {
            JSONObject jOptions = new JSONObject();
            for (Entry<String, String> entry : options.entrySet()) {
                jOptions.put(entry.getKey(), entry.getValue());
            }
            jBody.put("options", jOptions);
        }
    }

    protected void addFacets(JSONObject jBody, List<ActiveFacet> activeFacets) throws JSONException {
        if (activeFacets != null) {
            JSONArray jFacets = new JSONArray();
            for (ActiveFacet facet : activeFacets) {
                JSONObject jFacet = new JSONObject();
                jFacet.put("facet", facet.getType().getId());
                jFacet.put("value", facet.getValueId());

                // filter
                Filter filter = facet.getFilter();
                if (filter != null && filter != Filter.NO_FILTER) {
                    final JSONObject jFilter = new JSONObject(3);
                    final List<String> filterQueries = filter.getQueries();
                    final JSONArray jQueries = new JSONArray(filterQueries.size());
                    for (final String sQuery : filterQueries) {
                        jQueries.put(sQuery);
                    }
                    jFilter.put("queries", jQueries);

                    final List<String> fields = filter.getFields();
                    final JSONArray jFields = new JSONArray(fields.size());
                    for (final String sField : fields) {
                        jFields.put(sField);
                    }
                    jFilter.put("fields", jFields);
                    jFacet.put("filter", jFilter);
                }

                jFacets.put(jFacet);
            }
            jBody.put("facets", jFacets);
        }
    }

}
