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

package com.openexchange.api.client.common.calls.folders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientConstants;
import com.openexchange.api.client.common.calls.AbstractPutCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;

/**
 * {@link DeleteFolderCall} - Deletes a folder. Always sends the <code>extendedResponse</code> with <code>false</code>
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class DeleteFolderCall extends AbstractPutCall<List<String>> {

    private final String tree;
    private final long timestamp;
    private final String allowedModules;
    private final boolean hardDelete;
    private final boolean failOnError;
    private final String pushToken;
    private List<String> deletees;

    /**
     * Initializes a new {@link DeleteFolderCall}.
     * 
     * <p>
     * If constructed with this constructor, the <code>hardDelete</code> will be set to <code>false</code> and the
     * <code>failOnError</code> flag will be set to <code>true</code>
     * 
     * @param deletees The folder IDs to delete
     * @param tree The identifier of the folder tree. If missing "0" (primary folder tree) is assumed
     * @param timestamp The optional timestamp of the last update of the deleted folders
     * @param allowedModules An array of modules supported by requesting client. If missing, all available modules are considered. E.g. <code>calendar,mail</code>
     * @param pushToken The client's push token to restrict the generated drive event
     */
    public DeleteFolderCall(List<String> deletees, String tree, long timestamp, String allowedModules, String pushToken) {
        this(deletees, tree, timestamp, allowedModules, pushToken, false, true);
    }

    /**
     * Initializes a new {@link DeleteFolderCall}.
     * 
     * @param deletees The folder IDs to delete
     * @param tree The identifier of the folder tree. If missing "0" (primary folder tree) is assumed
     * @param timestamp The optional timestamp of the last update of the deleted folders
     * @param allowedModules An array of modules supported by requesting client. If missing, all available modules are considered. E.g. <code>calendar,mail</code>
     * @param pushToken The client's push token to restrict the generated drive event
     * @param hardDelete If set to `true`, the folders are deleted permanently. Otherwise it is tried to move to the trash
     * @param failOnError If an error occurred for one folder and this parameter is set to <code>true</code> the process will abort and throw an error.
     */
    public DeleteFolderCall(List<String> deletees, String tree, long timestamp, String allowedModules, String pushToken, boolean hardDelete, boolean failOnError) {
        super();
        this.deletees = deletees;
        this.tree = tree;
        this.timestamp = timestamp;
        this.allowedModules = allowedModules;
        this.pushToken = pushToken;
        this.hardDelete = hardDelete;
        this.failOnError = failOnError;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/folders";
    }

    @Override
    protected String getAction() {
        return "delete";
    }

    @Override
    public HttpResponseParser<List<String>> getParser() {
        return new AbstractHttpResponseParser<List<String>>(failOnError, true) {

            @Override
            public List<String> parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException {
                if (false == commonResponse.isJSONArray()) {
                    throw ApiClientExceptions.JSON_ERROR.create(ApiClientConstants.NOT_JSON_ARRAY_MSG);
                }
                JSONArray jsonArray = commonResponse.getJSONArray();
                ArrayList<String> ids = new ArrayList<>(jsonArray.length());
                for (Object object : jsonArray) {
                    if (null != object) {
                        ids.add(String.valueOf(object));
                    }
                }
                /*
                 * Return IDs of folder that were NOT deleted
                 */
                return ids;
            }
        };
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("tree", tree);
        parameters.put("timestamp", String.valueOf(timestamp));
        parameters.put("hardDelete", Boolean.toString(hardDelete));
        parameters.put("failOnError", Boolean.toString(failOnError));
        parameters.put("extendedResponse", Boolean.toString(false));
        parameters.put("pushToken", pushToken);
        putIfPresent(parameters, "allowed_modules", allowedModules);
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException {
        if (null == deletees || deletees.isEmpty()) {
            throw ApiClientExceptions.MISSING_PARAMETER.create();
        }
        JSONArray array = new JSONArray(deletees.size());
        for (String deletee : deletees) {
            array.put(deletee);
        }
        return toHttpEntity(array);
    }

}
