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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.find.Module;
import com.openexchange.find.ModuleConfig;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.DisplayItemVisitor;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.MandatoryFilter;

/**
 * {@link ConfigRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ConfigRequest extends AbstractFindRequest<ConfigResponse> {

    private final boolean failOnError;

    /**
     * Initializes a new {@link ConfigRequest}.
     */
    public ConfigRequest() {
        this(true);
    }

    /**
     * Initializes a new {@link ConfigRequest}.
     */
    public ConfigRequest(final boolean failOnError) {
        super();
        this.failOnError = failOnError;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "config"));
        return list.toArray(new Parameter[0]);
    }

    @Override
    public AbstractAJAXParser<? extends ConfigResponse> getParser() {
        return new ConfigParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    private static class ConfigParser extends AbstractAJAXParser<ConfigResponse> {

        /**
         * Initializes a new {@link ConfigParser}.
         */
        protected ConfigParser(final boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected ConfigResponse createResponse(final Response response) throws JSONException {
            final JSONArray jResponse = (JSONArray) response.getData();

            final int length = jResponse.length();
            final Map<Module, ModuleConfig> configuration = new LinkedHashMap<Module, ModuleConfig>(length);
            for (int i = 0; i < length; i++) {
                final JSONObject jConfig = jResponse.getJSONObject(i);

                final Module module = Module.moduleFor(jConfig.getString("module"));

                final List<Facet> staticFacets;
                {
                    final JSONArray jStaticFacets = jConfig.getJSONArray("staticFacets");
                    final int len = jStaticFacets.length();
                    staticFacets = new ArrayList<Facet>(len);
                    for (int j = 0; j < len; j++) {
                        staticFacets.add(parseJFacet(jStaticFacets.getJSONObject(j)));
                    }
                }

                final List<MandatoryFilter> mandatoryFilters;
                {
                    final JSONArray jMandatoryFilters = jConfig.getJSONArray("mandatoryFilters");
                    final int len = jMandatoryFilters.length();
                    mandatoryFilters = new ArrayList<MandatoryFilter>(len);
                    for (int j = 0; j < len; j++) {
                        mandatoryFilters.add(parseJMandatoryFilter(jMandatoryFilters.getJSONObject(j)));
                    }
                }

                ModuleConfig value = new ModuleConfig(module, staticFacets, mandatoryFilters);

                configuration.put(module, value);
            }

            return new ConfigResponse(response, configuration);
        }

        private MandatoryFilter parseJMandatoryFilter(final JSONObject jMandatoryFilter) throws JSONException {
            final String facetName = jMandatoryFilter.getString("facet");
            final FacetType facetType = new FacetType() {

                @Override
                public String getId() {
                    return facetName;
                }

                @Override
                public String getDisplayName() {
                    return "<unknwon>";
                }
            };

            final FacetValue facetValue = parseJFacetValue(jMandatoryFilter.getJSONObject("defaultValue"));

            return new MandatoryFilter(new Facet(facetType, Collections.<FacetValue> emptyList()), facetValue);
        }

        private Facet parseJFacet(final JSONObject jFacet) throws JSONException {
            // Type information
            final String id = jFacet.getString("id");
            final String displayName = jFacet.getString("displayName");
            final FacetType facetType = new FacetType() {

                @Override
                public String getId() {
                    return id;
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

            return new FacetValue(jFacetValue.getString("id"), parseJDisplayItem(jDisplayItem), count, parseJFilter(jFilter));
        }

        private Filter parseJFilter(final JSONObject jFilter) throws JSONException {
            final JSONArray jQueries = jFilter.getJSONArray("queries");
            int length = jQueries.length();
            final List<String> queries = new LinkedList<String>();
            for (int i = 0; i < length; i++) {
                queries.add(jQueries.getString(i));
            }

            final JSONArray jFields = jFilter.getJSONArray("fields");
            length= jFields.length();
            final List<String> fields = new LinkedList<String>();
            for (int i = 0; i < length; i++) {
                fields.add(jFields.getString(i));
            }

            return new Filter(fields, queries);
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
