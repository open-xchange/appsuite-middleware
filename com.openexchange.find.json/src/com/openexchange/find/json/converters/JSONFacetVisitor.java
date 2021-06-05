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

package com.openexchange.find.json.converters;

import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.ExclusiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FacetVisitor;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.Option;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class JSONFacetVisitor implements FacetVisitor {

    private final StringTranslator translator;

    private final Locale locale;

    private final ServerSession session;

    private final JSONObject result;

    private JSONException jsonException;

    public JSONFacetVisitor(final StringTranslator translator, final ServerSession session) {
        super();
        this.translator = translator;
        this.session = session;
        this.locale = session.getUser().getLocale();
        result = new JSONObject();
    }

    @Override
    public void visit(SimpleFacet facet) {
        try {
            FacetType type = facet.getType();
            result.put("id", type.getId());
            result.put("style", facet.getStyle());
            addDisplayItem(result, facet.getDisplayItem());
            result.put("filter", convertFilter(facet.getFilter()));

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
            result.put("name", translator.translate(locale, type.getDisplayName()));

            List<FacetValue> values = facet.getValues();
            JSONArray jValues = new JSONArray(values.size());
            for (FacetValue value : values) {
                JSONObject jValue = convertFacetValue(value);
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
            result.put("name", translator.translate(locale, type.getDisplayName()));

            List<FacetValue> values = facet.getValues();
            JSONArray jValues = new JSONArray(values.size());
            for (FacetValue value : values) {
                JSONObject jValue = convertFacetValue(value);
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

    protected JSONObject convertFacetValue(FacetValue value) throws JSONException {
        JSONObject jValue = new JSONObject(4);
        jValue.put("id", value.getId());
        addDisplayItem(jValue, value.getDisplayItem());
        int count = value.getCount();
        if (count >= 0) {
            jValue.put("count", value.getCount());
        }

        if (value.hasOptions()) {
            JSONArray jOptions = new JSONArray();
            for (Option option : value.getOptions()) {
                JSONObject jOption = new JSONObject();
                jOption.put("id", option.getId());
                jOption.put("name", translator.translate(locale, option.getName()));
                jOption.put("filter", convertFilter(option.getFilter()));
                jOptions.put(jOption);
            }
            jValue.put("options", jOptions);
        } else {
            jValue.put("filter", convertFilter(value.getFilter()));
        }

        return jValue;
    }

    protected void addDisplayItem(JSONObject json, DisplayItem displayItem) throws JSONException {
        JSONDisplayItemVisitor visitor = new JSONDisplayItemVisitor(translator, session);
        displayItem.accept(visitor);
        visitor.appendResult(json);
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
