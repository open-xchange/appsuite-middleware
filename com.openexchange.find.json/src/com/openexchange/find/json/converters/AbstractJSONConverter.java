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

import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public abstract class AbstractJSONConverter implements ResultConverter {

    private final StringTranslator translator;

    protected AbstractJSONConverter(final StringTranslator translator) {
        super();
        this.translator = translator;
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    protected JSONArray convertFacets(Locale locale, List<Facet> facets) throws JSONException {
        JSONArray result = new JSONArray(facets.size());
        for (Facet facet : facets) {
            JSONObject jFacet = new JSONObject(4);

            // Type information
            FacetType type = facet.getType();
            jFacet.put("id", type.getId());
            if (type.isFieldFacet()) {
                jFacet.put("field_facet", true);
            } else {
                jFacet.put("display_name", AJAXUtility.sanitizeParam(translator.translate(locale, type.getDisplayName())));
            }

            // Facet values
            List<FacetValue> values = facet.getValues();
            JSONArray jValues = new JSONArray(values.size());
            for (FacetValue value : values) {
                JSONObject valueJSON = convertFacetValue(locale, value);
                jValues.put(valueJSON);
            }

            if (type.appliesOnce()) {
                jFacet.put("options", jValues);
            } else {
                jFacet.put("values", jValues);
            }

            // Flags
            JSONArray jFlags = new JSONArray();
            for (String flag : facet.getFlags()) {
                jFlags.put(flag);
            }
            jFacet.put("flags", jFlags);

            // Put to JSON array
            result.put(jFacet);
        }

        return result;
    }

    protected JSONObject convertFacetValue(Locale locale, FacetValue value) throws JSONException {
        JSONObject valueJSON = new JSONObject(4);
        valueJSON.put("id", value.getId());
        String displayName = convertDisplayItem(locale, value.getDisplayItem());
        valueJSON.put("display_name", displayName);
        int count = value.getCount();
        if (count >= 0) {
            valueJSON.put("count", value.getCount());
        }

        List<Filter> filters = value.getFilters();
        if (filters.size() > 1) {
            JSONArray filtersJSON = new JSONArray();
            for (Filter filter : filters) {
                JSONObject filterJSON = new JSONObject();
                filterJSON.put("id", filter.getId());
                // TODO: introduce boolean if "display_name" is localizable or state in JavaDoc
                // that it has to be always localizable
                filterJSON.put("display_name", AJAXUtility.sanitizeParam(translator.translate(locale, filter.getDisplayName())));
                filterJSON.put("filter", convertFilter(filter));
                filtersJSON.put(filterJSON);
            }
            valueJSON.put("options", filtersJSON);
        } else {
            valueJSON.put("filter", convertFilter(filters.get(0)));
        }

        return valueJSON;
    }

    protected String convertDisplayItem(Locale locale, DisplayItem displayItem) {
        JSONDisplayItemVisitor visitor = new JSONDisplayItemVisitor(translator, locale);
        displayItem.accept(visitor);
        return visitor.getDisplayName();
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

}
