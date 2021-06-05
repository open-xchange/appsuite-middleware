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

package com.openexchange.ajax.folder.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;
import org.json.JSONValue;

/**
 * {@link GenJSONRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class GenJSONRequest extends AbstractFolderRequest<GenJSONResponse> {

    private final boolean failOnError;

    private JSONValue jsonValue;

    private Method method;

    private final Map<String, String> parameters;

    public GenJSONRequest(final API api) {
        this(api, true);
    }

    /**
     * Initializes a new {@link GenJSONRequest} with method set to <code>PUT</code>.
     *
     * @param failOnError Whether to fail on error
     */
    public GenJSONRequest(final API api, final boolean failOnError) {
        super(api);
        this.failOnError = failOnError;
        method = Method.PUT;
        parameters = new HashMap<String, String>();
    }

    public void setJSONValue(final JSONValue jsonValue) {
        this.jsonValue = jsonValue;
    }

    public void setMethod(final Method method) {
        this.method = method;
    }

    public void setParameter(final String name, final String value) {
        parameters.put(name, value);
    }

    @Override
    public Object getBody() {
        return null == jsonValue ? JSONObject.NULL : jsonValue;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    protected void addParameters(final List<Parameter> params) {
        for (final Entry<String, String> entry : parameters.entrySet()) {
            params.add(new Parameter(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public GenJSONParser getParser() {
        return new GenJSONParser(failOnError);
    }
}
