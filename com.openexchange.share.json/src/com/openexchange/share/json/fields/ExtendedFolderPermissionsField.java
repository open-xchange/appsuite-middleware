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

package com.openexchange.share.json.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.core.tools.PermissionResolver;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExtendedFolderPermissionsField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ExtendedFolderPermissionsField implements AdditionalFolderField {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ExtendedFolderPermissionsField}.
     *
     * @param services The service lookup reference
     */
    public ExtendedFolderPermissionsField(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public int getColumnID() {
        return 3060;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.share.extendedPermissions";
    }

    @Override
    public Object getValue(Folder folder, ServerSession session) {
        return getValues(Collections.singletonList(folder), session).iterator().next();
    }

    @Override
    public List<Object> getValues(List<Folder> folders, ServerSession session) {
        if (null == folders) {
            return null;
        }
        PermissionResolver resolver = new PermissionResolver(services, session);
        resolver.cacheFolderPermissionEntities(folders);
        List<Object> values = new ArrayList<Object>();
        for (Folder folder : folders) {
            Permission[] oclPermissions = folder.getPermissions();
            if (null == oclPermissions) {
                values.add(null);
            } else {
                List<ExtendedFolderPermission> extendedPermissions = new ArrayList<ExtendedFolderPermission>(oclPermissions.length);
                for (Permission oclPermission : oclPermissions) {
                    extendedPermissions.add(new ExtendedFolderPermission(resolver, folder, oclPermission));
                }
                values.add(extendedPermissions);
            }
        }
        return values;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        // The method returns a non null value if the value is non-null and can be cast to java.util.List
        if (null == requestData || false == List.class.isInstance(value)) {
            return JSONObject.NULL;
        }

        List<?> values = (List<?>) value;
        JSONArray jsonArray = new JSONArray(values.size());
        for (Object item : values) {
            if (ExtendedFolderPermission.class.isInstance(item)) {
                try {
                    jsonArray.put(((ExtendedFolderPermission) item).toJSON(requestData));
                } catch (JSONException | OXException e) {
                    org.slf4j.LoggerFactory.getLogger(ExtendedFolderPermissionsField.class).error("Error serializing extended permissions", e);
                }
            }
        }
        return jsonArray;
    }

}
