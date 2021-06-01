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
import org.slf4j.Logger;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.core.tools.PermissionResolver;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExtendedObjectPermissionsField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ExtendedObjectPermissionsField implements AdditionalFileField {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ExtendedObjectPermissionsField.class);
    }

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ExtendedObjectPermissionsField}.
     *
     * @param services The service lookup reference
     */
    public ExtendedObjectPermissionsField(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Field[] getRequiredFields() {
        return new Field[] { Field.OBJECT_PERMISSIONS, Field.FOLDER_ID, Field.ID, Field.CREATED_BY };
    }

    @Override
    public int getColumnID() {
        return 7010;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.share.extendedObjectPermissions";
    }

    @Override
    public Object getValue(File file, ServerSession session) {
        return getValues(Collections.singletonList(file), session).iterator().next();
    }

    @Override
    public List<Object> getValues(List<File> files, ServerSession session) {
        if (null == files) {
            return null;
        }
        PermissionResolver resolver = new PermissionResolver(services, session);
        resolver.cacheFilePermissionEntities(files);
        List<Object> values = new ArrayList<Object>(files.size());
        for (File file : files) {
            List<FileStorageObjectPermission> objectPermissions = file.getObjectPermissions();
            if (null != objectPermissions) {
                List<ExtendedObjectPermission> extendedPermissions = new ArrayList<ExtendedObjectPermission>(objectPermissions.size());
                for (FileStorageObjectPermission objectPermission : objectPermissions) {
                    extendedPermissions.add(new ExtendedObjectPermission(resolver, file, objectPermission));
                }
                values.add(extendedPermissions);
            } else {
                values.add(null);
            }
        }
        return values;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        if (null != value && List.class.isInstance(value)) {
            List<?> values = (List<?>) value;
            JSONArray jsonArray = new JSONArray(values.size());
            for (Object item : values) {
                if (ExtendedObjectPermission.class.isInstance(item)) {
                    try {
                        jsonArray.put(((ExtendedObjectPermission) item).toJSON(requestData));
                    } catch (Exception e) {
                        LoggerHolder.LOG.error("Error serializing extended permissions", e);
                    }
                }
            }
            return jsonArray;
        }
        return new JSONArray(0);
    }

}
