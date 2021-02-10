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
