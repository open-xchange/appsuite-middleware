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

package com.openexchange.api.client.common.calls.jobs;

import java.util.Map;
import java.util.Objects;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;

/**
 * {@link GetCall} - Gets the result of a certain job
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @param <T> The type of the response
 * @since v7.10.5
 */
public class GetCall<T> extends AbstractGetCall<T> {

    private final String id;
    private final HttpResponseParser<T> responseParser;

    /**
     * Initializes a new {@link GetCall}.
     *
     * @param id The ID of the job to get
     * @param responseParser The parser to use for the actual expected result
     */
    public GetCall(String id, HttpResponseParser<T> responseParser) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.responseParser = Objects.requireNonNull(responseParser, "responseParser must not be null");
    }

    /**
     * Gets the parser to use
     *
     * @return The actual parser to use for the result
     */
    public HttpResponseParser<T> getResponseParser() {
        return responseParser;
    }

    @Override
    @NonNull
    public String getModule() {
        return "jobs";
    }

    @Override
    public HttpResponseParser<T> getParser() {
        return responseParser;
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
    }

    @Override
    protected String getAction() {
        return "get";
    }
}
