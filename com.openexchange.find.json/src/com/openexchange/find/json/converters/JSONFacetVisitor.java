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

package com.openexchange.find.json.converters;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.ExclusiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FacetVisitor;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.java.Strings;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class JSONFacetVisitor implements FacetVisitor {

    private final StringTranslator translator;

    private final Locale locale;

    private final JSONObject result;

    private JSONException jsonException;

    public JSONFacetVisitor(final StringTranslator translator, final Locale locale) {
        super();
        this.translator = translator;
        this.locale = locale;
        result = new JSONObject();
    }

    @Override
    public void visit(SimpleFacet facet) {
        /*
         * TODO: Discuss with UI team to simplify this type to
         * {
         *   "id":"facet_type_id",
         *   "display_name":"...",
         *   "filter":{
         *     "fields":[], "queries":[]
         *   },
         *   "flags":[]
         * }
         */
        try {
            FacetType type = facet.getType();
            result.put("id", type.getId());
            result.put("style", facet.getStyle());
            result.put("field_facet", true);

            JSONObject jValue = new JSONObject();
            jValue.put("id", type.getId());
            addDisplayItem(jValue, locale, facet.getDisplayItem());
            jValue.put("filter", convertFilter(facet.getFilter()));
            result.put("values", new JSONArray(Collections.singletonList(jValue)));

            addFlags(facet);
        } catch (JSONException e) {
            jsonException = e;
        }
    }

    @Override
    public void visit(DefaultFacet facet) {
        try {
            FacetType type = facet.getType();
            result.put("id", type.getId());
            result.put("style", facet.getStyle());
            result.put("display_name", sanitizeForDisplay(translator.translate(locale, type.getDisplayName())));

            List<FacetValue> values = facet.getValues();
            JSONArray jValues = new JSONArray(values.size());
            for (FacetValue value : values) {
                JSONObject jValue = convertFacetValue(locale, value);
                jValues.put(jValue);
            }

            result.put("values", jValues);

            addFlags(facet);
        } catch (JSONException e) {
            jsonException = e;
        }
    }

    @Override
    public void visit(ExclusiveFacet facet) {
        try {
            FacetType type = facet.getType();
            result.put("id", type.getId());
            result.put("style", facet.getStyle());
            result.put("display_name", sanitizeForDisplay(translator.translate(locale, type.getDisplayName())));

            List<FacetValue> values = facet.getValues();
            JSONArray jValues = new JSONArray(values.size());
            for (FacetValue value : values) {
                JSONObject jValue = convertFacetValue(locale, value);
                jValues.put(jValue);
            }

            result.put("options", jValues);

            addFlags(facet);
        } catch (JSONException e) {
            jsonException = e;
        }
    }

    private void addFlags(Facet facet) throws JSONException {
        JSONArray jFlags = new JSONArray();
        for (String flag : facet.getFlags()) {
            jFlags.put(flag);
        }
        result.put("flags", jFlags);
    }

    public JSONObject getResult() throws JSONException {
        if (jsonException != null) {
            throw jsonException;
        }

        return result;
    }

    protected JSONObject convertFacetValue(Locale locale, FacetValue value) throws JSONException {
        JSONObject jValue = new JSONObject(4);
        jValue.put("id", value.getId());
        addDisplayItem(jValue, locale, value.getDisplayItem());
        int count = value.getCount();
        if (count >= 0) {
            jValue.put("count", value.getCount());
        }

        List<Filter> filters = value.getFilters();
        if (filters.size() > 1) {
            JSONArray filtersJSON = new JSONArray();
            for (Filter filter : filters) {
                JSONObject filterJSON = new JSONObject();
                filterJSON.put("id", filter.getId());
                filterJSON.put("display_name", sanitizeForDisplay(translator.translate(locale, filter.getDisplayName())));
                filterJSON.put("filter", convertFilter(filter));
                filtersJSON.put(filterJSON);
            }
            jValue.put("options", filtersJSON);
        } else {
            jValue.put("filter", convertFilter(filters.get(0)));
        }

        return jValue;
    }

    protected void addDisplayItem(JSONObject json, Locale locale, DisplayItem displayItem) throws JSONException {
        JSONDisplayItemVisitor visitor = new JSONDisplayItemVisitor(translator, locale);
        displayItem.accept(visitor);
        Object result = visitor.getResult();
        if (result instanceof String) {
            json.put("display_name", result);
        } else if (result instanceof String[]) {
            JSONArray parts = new JSONArray();
            parts.put(((String[])result)[0]);
            parts.put(((String[])result)[1]);
            json.put("display_item", parts);
        }
    }

    protected JSONObject convertFilter(Filter filter) throws JSONException {
        // Put fields to JSON array
        List<String> fields = filter.getFields();
        JSONArray jFields = new JSONArray(fields.size());
        for (String field : fields) {
            jFields.put(field);
        }

        List<String> queries = filter.getQueries();
        JSONArray jQueries = new JSONArray(queries.size());
        for (String query : queries) {
            jQueries.put(query);
        }

        // Compose JSON object
        JSONObject filterJSON = new JSONObject(3);
        filterJSON.put("fields", jFields);
        filterJSON.put("queries", jQueries);
        return filterJSON;
    }

    private String sanitizeForDisplay(String forDisplay) {
        if (Strings.isEmpty(forDisplay)) {
            return forDisplay;
        }

        int pos = forDisplay.lastIndexOf(" <i>");
        if (pos < 0) {
            return AJAXUtility.sanitizeParam(forDisplay);
        }

        String toSanitize = forDisplay.substring(0, pos);
        if (Strings.isEmpty(toSanitize)) {
            return forDisplay;
        }

        return new StringBuilder(forDisplay.length()).append(AJAXUtility.sanitizeParam(toSanitize)).append(forDisplay.substring(pos)).toString();
    }

}
