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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.exception.OXException;
import com.openexchange.find.Document;
import com.openexchange.find.SearchResult;
import com.openexchange.find.json.QueryResult;
import com.openexchange.find.json.osgi.ResultConverterRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link QueryResultJSONConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class QueryResultJSONConverter extends AbstractJSONConverter {

    private final ResultConverterRegistry converterRegistry;

    public QueryResultJSONConverter(final StringTranslator translator, final ResultConverterRegistry converterRegistry) {
        super(translator);
        this.converterRegistry = converterRegistry;
    }

    @Override
    public String getInputFormat() {
        return QueryResult.class.getName();
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Object resultObject = result.getResultObject();
        if (resultObject instanceof QueryResult) {
            QueryResult queryResult = (QueryResult) resultObject;
            SearchResult searchResult = queryResult.getSearchResult();
            List<OXException> warnings = searchResult.getWarnings();
            if (null != warnings && 0 < warnings.size()) {
                result.addWarnings(warnings);
            }
            JSONObject json = new JSONObject();
            try {
                json.put("num_found", searchResult.getNumFound());
                json.put("start", searchResult.getStart());
                json.put("size", searchResult.getSize());

                JSONResponseVisitor visitor = new JSONResponseVisitor(session, requestData, converterRegistry, queryResult);
                for (Document document : searchResult.getDocuments()) {
                    document.accept(visitor);
                }

                JSONArray jsonDocuments = visitor.getJSONArray();
                json.put("results", jsonDocuments);
                result.addWarnings(visitor.getErrors());

                result.setResultObject(json, "json");
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
            }
        }
    }

}
