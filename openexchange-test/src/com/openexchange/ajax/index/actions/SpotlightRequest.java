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

package com.openexchange.ajax.index.actions;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link SpotlightRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SpotlightRequest extends AbstractIndexRequest<GeneralIndexResponse> {

    private String searchTerm;
    private int maxPersons;
    private int maxTopics;

    public SpotlightRequest(String searchTerm, int maxPersons, int maxTopics) {
        super();
        this.searchTerm = searchTerm;
        this.maxPersons = maxPersons;
        this.maxTopics = maxTopics;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        Parameter[] params = new Parameter[4];
        params[0] = new Parameter("searchTerm", searchTerm);
        params[1] = new Parameter("maxPersons", maxPersons);
        params[2] = new Parameter("maxTopics", maxTopics);
        params[3] = new Parameter("action", "spotlight");
        return params;
    }

    @Override
    public AbstractAJAXParser<? extends GeneralIndexResponse> getParser() {
        return new GeneralIndexParser(true);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

}
