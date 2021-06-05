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

package com.openexchange.api.client.common.calls;

import static com.openexchange.api.client.common.ApiClientConstants.ACTION;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.ApiCall;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 * {@link AbstractApiCall} - Common abstract class for {@link ApiCall}s
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @param <T> The class of the response
 * @since v7.10.5
 */
public abstract class AbstractApiCall<T> implements ApiCall<T> {

    /**
     * The sort order
     */
    public enum SortOrder {
        /**
         * Sort in ascending order
         */
        ASC,

        /**
         * Sort in descending order
         */
        DESC
    }

    /**
     * Initializes a new {@link AbstractApiCall}.
     */
    public AbstractApiCall() {
        super();
    }

    @Override
    @NonNull
    public Map<String, String> getPathParameters() {
        Map<String, String> parameters = new HashMap<>(10);
        if (Strings.isNotEmpty(getAction())) {
            parameters.put(ACTION, getAction());
        }
        fillParameters(parameters);
        return parameters;
    }

    @SuppressWarnings("null")
    @Override
    @NonNull
    public Map<String, String> getHeaders() {
        return Collections.emptyMap();
    }

    protected abstract void fillParameters(Map<String, String> parameters);

    /**
     * Get the action that should be appended to the path parameters
     *
     * @return The action
     */
    protected abstract String getAction();

    /*
     * ------------------------- HELPERS -------------------------
     */

    /**
     * Transforms a {@link JSONObject} into an {@link HttpEntity}
     *
     * @param json The JSON to transform
     * @return The {@link HttpEntity}
     */
    protected HttpEntity toHttpEntity(JSONValue json) {
        return new InputStreamEntity(new JSONInputStream(json, Charsets.UTF_8_NAME), -1L, ContentType.APPLICATION_JSON);
    }

    /**
     * Puts the given value into the map if it is not <code>null</code>.
     *
     * @param parameters The parameters to set the value in
     * @param key The key to set
     * @param object The value to set
     * @return <code>true</code> if the value has been set, false otherwise
     */
    protected boolean putIfPresent(Map<String, String> parameters, String key, Object object) {
        if (null != object) {
            parameters.put(key, String.valueOf(object));
            return true;
        }
        return false;
    }

    /**
     * Puts the given {@link String} value into the map if it is not the String <code>"null"</code>
     * and is not empty as per {@link Strings#isNotEmpty(String)}.
     *
     * @param parameters The parameters to set the value in
     * @param key The key to set
     * @param object The value to set
     * @return <code>true</code> if the value has been set, false otherwise
     */
    protected boolean putIfNotEmpty(Map<String, String> parameters, String key, Integer value) {
        if (null == value) {
            return false;
        }
        return putIfNotEmpty(parameters, key, value.toString());
    }

    /**
     * Puts the given {@link String} value into the map if it is not the String <code>"null"</code>
     * and is not empty as per {@link Strings#isNotEmpty(String)}.
     *
     * @param parameters The parameters to set the value in
     * @param key The key to set
     * @param object The value to set
     * @return <code>true</code> if the value has been set, false otherwise
     */
    protected boolean putIfNotEmpty(Map<String, String> parameters, String key, String value) {
        if (Strings.isNotEmpty(value) && false == "null".equals(value)) {
            parameters.put(key, value);
            return true;
        }
        return false;
    }

}
