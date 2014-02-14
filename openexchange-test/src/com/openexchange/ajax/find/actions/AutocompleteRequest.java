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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.find.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.DisplayItemVisitor;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;

/**
 * {@link AutocompleteRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AutocompleteRequest extends AbstractFindRequest<AutocompleteResponse> {

    private final boolean failOnError;
    private final String prefix;
    private final String module;

    /**
     * Initializes a new {@link AutocompleteRequest}.
     */
    public AutocompleteRequest(final String prefix, final String module) {
        this(prefix, module, true);
    }

    /**
     * Initializes a new {@link AutocompleteRequest}.
     */
    public AutocompleteRequest(final String prefix, final String module, final boolean failOnError) {
        super();
        this.failOnError = failOnError;
        this.prefix = prefix;
        this.module = module;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter("module", module));
        return list.toArray(new Parameter[0]);
    }

    @Override
    public AbstractAJAXParser<? extends AutocompleteResponse> getParser() {
        return new AutocompleteParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        final JSONObject jBody = new JSONObject(2);
        jBody.put("prefix", prefix);
        return jBody;
    }

    private static class AutocompleteParser extends AbstractAJAXParser<AutocompleteResponse> {

        /**
         * Initializes a new {@link AutocompleteParser}.
         */
        protected AutocompleteParser(final boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected AutocompleteResponse createResponse(final Response response) throws JSONException {
            final JSONObject jResponse = (JSONObject) response.getData();

            final JSONArray jFacets = jResponse.getJSONArray("facets");
            final int length = jFacets.length();
            final List<Facet> facets = new ArrayList<Facet>(length);
            for (int i = 0; i < length; i++) {
                facets.add(parseJFacet(jFacets.getJSONObject(i)));
            }
            return new AutocompleteResponse(response, facets);
        }

        private Facet parseJFacet(final JSONObject jFacet) throws JSONException {
            // Type information
            final String type = jFacet.getString("type");
            final String displayName = jFacet.getString("displayName");
            final FacetType facetType = new FacetType() {

                @Override
                public String getName() {
                    return type;
                }

                @Override
                public String getDisplayName() {
                    return displayName;
                }
            };

            // Facets
            final JSONArray jFacetValues = jFacet.getJSONArray("values");
            final int len = jFacetValues.length();
            final List<FacetValue> values = new ArrayList<FacetValue>(len);
            for (int i = 0; i < len; i++) {
                values.add(parseJFacetValue(jFacetValues.getJSONObject(i)));
            }

            return new Facet(facetType, values);
        }

        private FacetValue parseJFacetValue(final JSONObject jFacetValue) throws JSONException {
            final JSONObject jDisplayItem = jFacetValue.getJSONObject("displayItem");
            final int count = jFacetValue.optInt("count", -1);
            final JSONObject jFilter = jFacetValue.getJSONObject("filter");

            return new FacetValue(parseJDisplayItem(jDisplayItem), count, parseJFilter(jFilter));
        }

        private Filter parseJFilter(final JSONObject jFilter) throws JSONException {
            final String query = jFilter.getString("query");

            final JSONArray jFields = jFilter.getJSONArray("fields");
            final int length = jFields.length();
            final Set<String> fields = new LinkedHashSet<String>(length);
            for (int i = 0; i < length; i++) {
                fields.add(jFields.getString(i));
            }

            return new Filter(fields, query);
        }

        private DisplayItem parseJDisplayItem(final JSONObject jDisplayItem) throws JSONException {
            final String defaultValue = jDisplayItem.getString("defaultValue");
            final Map<String, Object> item = jDisplayItem.asMap();
            return new DisplayItem() {

                @Override
                public String getDefaultValue() {
                    return defaultValue;
                }

                @Override
                public void accept(final DisplayItemVisitor visitor) {
                    // Nothing
                }

                @Override
                public Map<String, Object> getItem() {
                    return item;
                }
            };
        }
    }

}
