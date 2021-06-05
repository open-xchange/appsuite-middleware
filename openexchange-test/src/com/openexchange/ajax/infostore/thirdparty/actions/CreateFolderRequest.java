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

package com.openexchange.ajax.infostore.thirdparty.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link CreateFolderRequest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class CreateFolderRequest extends AbstractFolderRequest<CreateFolderResponse> {

    private final String parentFolderId;
    private final int tree;
    private final boolean autorename;
    private final String folderName;

    /**
     * Initializes a new {@link CreateFolderRequest}.
     * Assumes <code>tree=1</code> and <code>autorename=false</code>
     *
     * @param parentFolderId
     * @param folderName the name of the folder which should be created
     */
    public CreateFolderRequest(String parentFolderId, String folderName) {
        this(parentFolderId, 1, false, folderName);
    }

    public CreateFolderRequest(String parentFolderId, int tree, boolean autorename, String folderName) {
        super(true);
        this.autorename = autorename;
        this.parentFolderId = parentFolderId;
        this.tree = tree;
        this.folderName = folderName;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> list = new ArrayList<Parameter>();
        list.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        list.add(new URLParameter("autorename", autorename));
        list.add(new URLParameter("folder_id", parentFolderId));
        list.add(new URLParameter("tree", tree));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends CreateFolderResponse> getParser() {
        return new CreateFolderParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("module", "infostore");
        jsonObject.put("subscribed", 1);
        jsonObject.put("title", folderName);
        return jsonObject;
    }
}
