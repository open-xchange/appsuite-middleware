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

package com.openexchange.file.storage.infostore;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.ObjectPermission;


/**
 * {@link PermissionHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PermissionHelper {

    public static List<FileStorageObjectPermission> getFileStorageObjectPermissions(List<ObjectPermission> objectPermissions) {
        if (null == objectPermissions) {
            return null;
        }
        List<FileStorageObjectPermission> fileStorageObjectPermissions = new ArrayList<FileStorageObjectPermission>(objectPermissions.size());
        for (ObjectPermission objectPermission : objectPermissions) {
            fileStorageObjectPermissions.add(getFileStorageObjectPermission(objectPermission));
        }
        return fileStorageObjectPermissions;
    }

    public static DefaultFileStorageObjectPermission getFileStorageObjectPermission(ObjectPermission objectPermission) {
        return new DefaultFileStorageObjectPermission(
            objectPermission.getEntity(), objectPermission.isGroup(), objectPermission.getPermissions());
    }

    public static List<ObjectPermission> getObjectPermissions(List<FileStorageObjectPermission> fileStorageObjectPermissions) {
        if (null == fileStorageObjectPermissions) {
            return null;
        }
        List<ObjectPermission> objectPermissions = new ArrayList<ObjectPermission>(fileStorageObjectPermissions.size());
        for (FileStorageObjectPermission fileStorageObjectPermission : fileStorageObjectPermissions) {
            objectPermissions.add(getObjectPermission(fileStorageObjectPermission));
        }
        return objectPermissions;
    }

    public static ObjectPermission getObjectPermission(FileStorageObjectPermission fileStorageObjectPermission) {
        return new ObjectPermission(
            fileStorageObjectPermission.getEntity(), fileStorageObjectPermission.isGroup(), fileStorageObjectPermission.getPermissions());
    }

    private PermissionHelper() {
        super();
    }

}
