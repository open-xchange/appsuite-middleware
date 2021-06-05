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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.folderstorage.FolderStorage;

/**
 * {@link SubscribeRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SubscribeRequest extends AbstractFolderRequest<SubscribeResponse> {

    private final String parent;

    private final boolean failOnError;

    private final List<String> folderIds;

    private final List<Boolean> flags;

    public SubscribeRequest(final API api, final boolean failOnError) {
        this(api, FolderStorage.ROOT_ID, failOnError);
    }

    public SubscribeRequest(final API api, final String parent, final boolean failOnError) {
        super(api);
        this.parent = parent;
        this.failOnError = failOnError;
        folderIds = new ArrayList<String>(4);
        flags = new ArrayList<Boolean>(4);
    }

    /**
     * Adds specified folder identifier.
     *
     * @param folderId The folder identifier
     * @param subscribe <code>true</code> to subscribe denoted folder to tree; otherwise <code>false</code>
     * @return This request with folder identifier added
     */
    public SubscribeRequest addFolderId(final String folderId, final boolean subscribe) {
        folderIds.add(folderId);
        flags.add(Boolean.valueOf(subscribe));
        return this;
    }

    @Override
    public Object getBody() {
        try {
            final JSONArray jArray = new JSONArray();
            final int size = folderIds.size();
            for (int i = 0; i < size; i++) {
                final JSONObject jObject = new JSONObject();
                jObject.put("id", folderIds.get(i));
                jObject.put("subscribe", flags.get(i).booleanValue());
                jArray.put(jObject);
            }
            return jArray;
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    protected void addParameters(final List<Parameter> params) {
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "subscribe"));
        params.add(new Parameter(Folder.PARAMETER_PARENT, parent));
    }

    @Override
    public AbstractAJAXParser<? extends SubscribeResponse> getParser() {
        return new AbstractAJAXParser<SubscribeResponse>(failOnError) {

            @Override
            protected SubscribeResponse createResponse(final Response response) {
                return new SubscribeResponse(response);
            }
        };
    }
}
