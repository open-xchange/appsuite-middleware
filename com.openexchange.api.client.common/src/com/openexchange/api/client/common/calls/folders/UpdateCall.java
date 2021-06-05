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

import java.util.Map;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractPutCall;
import com.openexchange.api.client.common.parser.StringParser;
import com.openexchange.exception.OXException;

/**
 * {@link UpdateCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class UpdateCall extends AbstractPutCall<String> {

    private final String id;
    private final FolderBody folderBody;
    private final Boolean allowEnqueue;
    private final long timestamp;
    private final String tree;
    private final String[] allowedModules;
    private final Boolean cascadePermissions;
    private final String pushToken;
    private final Boolean autoRename;

    /**
     * Initializes a new {@link UpdateCall}.
     *
     * @param id The ID of the folder to update
     * @param folderBody The folder data to apply
     * @param timestamp Timestamp of the updated folder. If the folder was modified after the specified timestamp, then the update must fail.
     * @param autoRename Whether to automatically rename the folder in case an folder with the same name already exists.
     */
    public UpdateCall(String id, FolderBody folderBody, long timestamp, Boolean autoRename) {
        this(id, folderBody, null, timestamp, null, null, null, null, autoRename);
    }

    /**
     * Initializes a new {@link UpdateCall}.
     *
     * @param id The ID of the folder to update
     * @param folderBody The folder data to apply
     * @param allowEnqueue <code>true</code> if the request is allowed for being submitted to job queue, <code>false</code> otherwise
     * @param timestamp Timestamp of the updated folder. If the folder was modified after the specified timestamp, then the update must fail.
     * @param autoRename Whether to automatically rename the folder in case an folder with the same name already exists.
     */
    public UpdateCall(String id, FolderBody folderBody, Boolean allowEnqueue, long timestamp, Boolean autoRename) {
        this(id, folderBody, allowEnqueue, timestamp, null, null, null, null, autoRename);
    }

    /**
     * Initializes a new {@link UpdateCall}.
     *
     * @param id The ID of the folder to update
     * @param folderBody The folder data to apply
     * @param allowEnqueue <code>true</code> if the request is allowed for being submitted to job queue, <code>false</code> otherwise
     * @param timestamp Timestamp of the updated folder. If the folder was modified after the specified timestamp, then the update must fail.
     * @param autoRename Whether to automatically rename the folder in case an folder with the same name already exists.
     * @param cascadePermissions <code>true</code> to cascade permissions to all sub-folders. The user must have administrative permissions to all sub-folders subject to change. If one permission change fails, the entire operation fails.
     */
    public UpdateCall(String id, FolderBody folderBody, Boolean allowEnqueue, long timestamp, Boolean autoRename, Boolean cascadePermissions) {
        this(id, folderBody, allowEnqueue, timestamp, null, null, cascadePermissions, null, autoRename);
    }

    /**
     * Initializes a new {@link UpdateCall}.
     *
     * @param id The ID of the folder to update
     * @param folderBody The folder data to apply
     * @param allowEnqueue <code>true</code> if the request is allowed for being submitted to job queue, <code>false</code> otherwise
     * @param timestamp Timestamp of the updated folder. If the folder was modified after the specified timestamp, then the update must fail.
     * @param tree The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.
     * @param allowedModules An array of modules strings (e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.
     * @param cascadePermissions <code>true</code> to cascade permissions to all sub-folders. The user must have administrative permissions to all sub-folders subject to change. If one permission change fails, the entire operation fails.
     * @param pushToken The client's push token to restrict the generated drive event
     * @param autoRename Whether to automatically rename the folder in case an folder with the same name already exists.
     */
    public UpdateCall(String id, FolderBody folderBody, Boolean allowEnqueue, long timestamp, String tree, String[] allowedModules, Boolean cascadePermissions, String pushToken, Boolean autoRename) {
        this.id = id;
        this.folderBody = folderBody;
        this.allowEnqueue = allowEnqueue;
        this.timestamp = timestamp;
        this.tree = tree;
        this.allowedModules = allowedModules;
        this.cascadePermissions = cascadePermissions;
        this.pushToken = pushToken;
        this.autoRename = autoRename;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/folders";
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        JSONObject json = new JSONObject();
        RemoteFolderMapper mapper = new RemoteFolderMapper();
        RemoteFolder folder = folderBody.getFolder();
        json.put("folder", mapper.serialize(folder, mapper.getAssignedFields(folder)));
        return ApiClientUtils.createJsonBody(json);
    }

    @Override
    public HttpResponseParser<String> getParser() {
        return StringParser.getInstance();
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        parameters.put("timestamp", String.valueOf(timestamp));
        putIfPresent(parameters, "allow_enqueue", allowEnqueue);
        putIfPresent(parameters, "tree", tree);
        putIfPresent(parameters, "allowed_modules", ApiClientUtils.toCommaString(allowedModules));
        putIfPresent(parameters, "cascadePermissions", cascadePermissions);
        putIfPresent(parameters, "pushToken", pushToken);
        putIfPresent(parameters, "autoRename", autoRename);
    }

    @Override
    protected String getAction() {
        return "update";
    }
}
