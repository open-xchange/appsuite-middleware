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

import static com.openexchange.java.Autoboxing.B;
import java.util.Map;
import java.util.Objects;
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
 * {@link NewCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class NewCall extends AbstractPutCall<String> {

    private final String parentFolder;
    private final FolderBody folderBody;
    private final String tree;
    private final String[] allowedModules;
    private final String pushToken;
    private final Boolean autoRename;

    /**
     * Initializes a new {@link NewCall}.
     *
     * @param parentFolder The ID of the parent folder where the new folder should be created
     * @param folderBody The folder data to apply
     */
    public NewCall(String parentFolder, FolderBody folderBody) {
        this(parentFolder, folderBody, null, null, null, null);
    }

    /**
     * Initializes a new {@link NewCall}.
     *
     * @param parentFolder The ID of the parent folder where the new folder should be created
     * @param folderBody The folder data to apply
     * @param autoRename Whether to automatically rename the folder in case an folder with the same name already exists.
     */
    public NewCall(String parentFolder, FolderBody folderBody, boolean autoRename) {
        this(parentFolder, folderBody, null, null, null, B(autoRename));
    }

    /**
     * Initializes a new {@link NewCall}.
     *
     * @param parentFolder The ID of the parent folder where the new folder should be created
     * @param folderBody The folder data to apply
     * @param tree The tree Identifier of the folder tree, or null to asume "0" as default
     * @param allowedModules An array of modules (e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.
     * @param pushToken Drive push token
     * @param autoRename Whether to automatically rename the folder in case an folder with the same name already exists.
     */
    public NewCall(String parentFolder, FolderBody folderBody, String tree, String[] allowedModules, String pushToken, Boolean autoRename) {
        this.parentFolder = parentFolder;
        this.folderBody = Objects.requireNonNull(folderBody, "folderBody must not be null");
        this.tree = tree;
        this.allowedModules = allowedModules;
        this.pushToken = pushToken;
        this.autoRename = autoRename;

        Objects.requireNonNull(folderBody.getFolder(), "folder must not be null");
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
        parameters.put("folder_id", parentFolder);
        putIfNotEmpty(parameters, "tree", tree);
        putIfNotEmpty(parameters, "allowed_modules", ApiClientUtils.toCommaString(allowedModules));
        putIfNotEmpty(parameters, "pushToken", pushToken);
        putIfNotEmpty(parameters, "pushToken", String.valueOf(autoRename));
    }

    @Override
    protected String getAction() {
        return "new";
    }
}
