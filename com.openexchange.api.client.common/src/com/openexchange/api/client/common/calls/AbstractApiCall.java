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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client.common.calls;

import static com.openexchange.api.client.common.ApiClientConstants.ACTION;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractApiCall.class);

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
    protected boolean putIfNotEmpty(Map<String, String> parameters, String key, String value) {
        if (Strings.isNotEmpty(value) && false == "null".equals(value)) {
            parameters.put(key, value);
            return true;
        }
        return false;
    }

}
