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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import com.openexchange.find.calendar.CalendarFacetType;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.facet.AbstractFacet;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FacetValue.FacetValueBuilder;
import com.openexchange.find.facet.Facets;
import com.openexchange.find.facet.Facets.DefaultFacetBuilder;
import com.openexchange.find.facet.Facets.ExclusiveFacetBuilder;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.Option;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.tasks.TasksFacetType;

/**
 * {@link AutocompleteRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AutocompleteRequest extends AbstractFindRequest<AutocompleteResponse> {

    private final boolean failOnError;
    private final String prefix;
    private final String module;
    private final List<ActiveFacet> activeFacets;

    /**
     * Initializes a new {@link AutocompleteRequest}.
     */
    public AutocompleteRequest(final String prefix, final String module) {
        this(prefix, module, null, null, true);
    }

    /**
     * Initializes a new {@link AutocompleteRequest}.
     */
    public AutocompleteRequest(final String prefix, final String module, final List<ActiveFacet> activeFacets) {
        this(prefix, module, activeFacets, null, true);
    }

    public AutocompleteRequest(final String prefix, final String module, final Map<String, String> options) {
        this(prefix, module, null, options, true);
    }

    /**
     * Initializes a new {@link AutocompleteRequest}.
     */
    public AutocompleteRequest(final String prefix, final String module, final List<ActiveFacet> activeFacets, final Map<String, String> options, final boolean failOnError) {
        super(options);
        this.failOnError = failOnError;
        this.prefix = prefix;
        this.module = module;
        this.activeFacets = activeFacets;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "autocomplete"));
        list.add(new Parameter("module", module));
        list.add(new Parameter("limit", Integer.MAX_VALUE));
        return list.toArray(new Parameter[0]);
    }

    @Override
    public AbstractAJAXParser<? extends AutocompleteResponse> getParser() {
        return new AutocompleteParser(module, failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        final JSONObject jBody = new JSONObject(2);
        jBody.put("prefix", prefix);
        addFacets(jBody, activeFacets);
        addOptions(jBody);
        return jBody;
    }

    private static class AutocompleteParser extends AbstractAJAXParser<AutocompleteResponse> {

        private final String module;

        /**
         * Initializes a new {@link AutocompleteParser}.
         */
        protected AutocompleteParser(final String module, final boolean failOnError) {
            super(failOnError);
            this.module = module;
        }

        @Override
        protected AutocompleteResponse createResponse(final Response response) throws JSONException {
            final JSONObject jResponse = (JSONObject) response.getData();
            List<Facet> facets = null;
            if (jResponse != null) {
                final JSONArray jFacets = jResponse.getJSONArray("facets");
                final int length = jFacets.length();
                facets = new ArrayList<Facet>(length);
                for (int i = 0; i < length; i++) {
                    facets.add(parseJFacet(jFacets.getJSONObject(i)));
                }
            }
            return new AutocompleteResponse(response, facets);
        }

        private Facet parseJFacet(final JSONObject jFacet) throws JSONException {
            // Type information
            final String id = jFacet.getString("id");
            final FacetType facetType = facetTypeFor(Module.moduleFor(module), id);

            AbstractFacet facet = null;
            if ("simple".equals(jFacet.getString("style"))) {
                final Filter filter = parseJFilter(jFacet.getJSONObject("filter"));
                facet = new SimpleFacet(facetType, extractDisplayItem(jFacet), filter);
            } else if ("default".equals(jFacet.getString("style"))) {
                final JSONArray jFacetValues = jFacet.getJSONArray("values");
                final int len = jFacetValues.length();
                final DefaultFacetBuilder builder = Facets.newDefaultBuilder(facetType);
                for (int i = 0; i < len; i++) {
                    builder.addValue(parseJFacetValue(jFacetValues.getJSONObject(i)));
                }
                facet = builder.build();
            } else if ("exclusive".equals(jFacet.getString("style"))) {
                final JSONArray jFacetValues = jFacet.getJSONArray("options");
                final int len = jFacetValues.length();
                final ExclusiveFacetBuilder builder = Facets.newExclusiveBuilder(facetType);
                for (int i = 0; i < len; i++) {
                    builder.addValue(parseJFacetValue(jFacetValues.getJSONObject(i)));
                }
                facet = builder.build();
            }

            JSONArray jFlags = jFacet.getJSONArray("flags");
            for (int i = 0; i < jFlags.length(); i++) {
                facet.addFlag(jFlags.getString(i));
            }
            return facet;
        }

        private FacetValue parseJFacetValue(final JSONObject jFacetValue) throws JSONException {
            final String id = jFacetValue.getString("id");
            final int count = jFacetValue.optInt("count", -1);
            FacetValueBuilder builder = FacetValue.newBuilder(id)
                .withDisplayItem(extractDisplayItem(jFacetValue))
                .withCount(count);
            if (jFacetValue.has("filter")) {
                final JSONObject jFilter = jFacetValue.getJSONObject("filter");
                builder.withFilter(parseJFilter(jFilter));
            } else {
                final JSONArray options = jFacetValue.getJSONArray("options");
                for (int i = 0; i < options.length(); i++) {
                    final JSONObject jOption = options.getJSONObject(i);
                    builder.addOption(parseJOption(jOption));
                }
            }

            return builder.build();
        }

        private Option parseJOption(final JSONObject jOption) throws JSONException {
            final String id = jOption.optString("id");
            final String displayName = jOption.getString("name");
            final Filter filter = parseJFilter(jOption.getJSONObject("filter"));
            return Option.newInstance(id, displayName, filter);
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

            return Filter.of(fields, queries);
        }

        private static FacetType facetTypeFor(Module module, String id) {
            FacetType type = null;
            switch(module) {
                case MAIL:
                    type = MailFacetType.getById(id);
                    break;

                case CALENDAR:
                    type = CalendarFacetType.getById(id);
                    break;

                case CONTACTS:
                    type = ContactsFacetType.getById(id);
                    break;

                case DRIVE:
                    type = DriveFacetType.getById(id);
                    break;

                case TASKS:
                    type = TasksFacetType.getById(id);
                    break;

                default:
                    return null;
            }

            if (type == null) {
                type = CommonFacetType.getById(id);
            }

            return type;
        }

        private static TestDisplayItem extractDisplayItem(JSONObject json) throws JSONException {
            if (json.has("name")) {
                return new TestDisplayItem(json.getString("name"), null, null);
            } else if (json.has("item")) {
                JSONObject jItem = json.getJSONObject("item");
                return new TestDisplayItem(jItem.getString("name"), jItem.optString("detail"), jItem.optString("image_url"));
            }

            return null;
        }
    }

}
