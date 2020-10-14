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
        return new StringParser();
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
