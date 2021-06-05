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

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.CommonDeleteParser;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link ClearRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ClearRequest extends AbstractFolderRequest<CommonDeleteResponse> {

    private final String[] folderIds;

    public ClearRequest(final API api, final String[] folderIds) {
        super(api);
        this.folderIds = folderIds;
    }

    public ClearRequest(final API api, final String folderId) {
        this(api, new String[] { folderId });
    }

    public ClearRequest(final API api, final int[] folderIds) {
        this(api, i2s(folderIds));
    }

    public ClearRequest(final API api, final int folderId) {
        this(api, new int[] { folderId });
    }

    public ClearRequest(final API api, final FolderObject... folder) {
        super(api);
        folderIds = new String[folder.length];
        for (int i = 0; i < folder.length; i++) {
            if (folder[i].containsObjectID()) { // task, appointment or contact folder
                folderIds[i] = I(folder[i].getObjectID()).toString();
            } else { // mail folder
                folderIds[i] = folder[i].getFullName();
            }
        }
    }

    @Override
    public Object getBody() {
        final JSONArray array = new JSONArray();
        for (final String folderId : folderIds) {
            array.put(folderId);
        }
        return array;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    protected void addParameters(final List<Parameter> params) {
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_CLEAR));
    }

    @Override
    public CommonDeleteParser getParser() {
        return new CommonDeleteParser(true);
    }
}
