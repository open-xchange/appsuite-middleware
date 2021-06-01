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

package com.openexchange.file.storage.meta;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.file.storage.AbstractFileFieldSwitcher;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FolderPath;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.java.GeoLocation;

/**
 * {@link FileFieldSet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileFieldSet extends AbstractFileFieldSwitcher {

    /**
     * Initializes a new {@link FileFieldSet}.
     */
    public FileFieldSet() {
        super();
    }

    @Override
    public Object categories(final Object... args) {
        md(args).setCategories(string(1, args));
        return ret(args);
    }

    @Override
    public Object colorLabel(final Object... args) {
        md(args).setColorLabel(integer(1, args));
        return ret(args);
    }

    @Override
    public Object content(final Object... args) {
        return ret(args);
    }

    @Override
    public Object created(final Object... args) {
        Date date = date(1, args);
        if (date != null) {
            md(args).setCreated(date);
        }
        return ret(args);
    }

    @Override
    public Object createdBy(final Object... args) {
        md(args).setCreatedBy(integer(1, args));
        return ret(args);
    }

    @Override
    public Object currentVersion(final Object... args) {
        md(args).setIsCurrentVersion(bool(1, args));
        return ret(args);
    }

    @Override
    public Object description(final Object... args) {
        md(args).setDescription(string(1, args));
        return ret(args);
    }

    @Override
    public Object fileMd5sum(final Object... args) {
        md(args).setFileMD5Sum(string(1, args));
        return ret(args);
    }

    @Override
    public Object fileMimetype(final Object... args) {
        md(args).setFileMIMEType(string(1, args));
        return ret(args);
    }

    @Override
    public Object fileSize(final Object... args) {
        md(args).setFileSize(longValue(1, args));
        return ret(args);
    }

    @Override
    public Object filename(final Object... args) {
        md(args).setFileName(string(1, args));
        return ret(args);
    }

    @Override
    public Object folderId(final Object... args) {
        md(args).setFolderId(string(1, args));
        return ret(args);
    }

    @Override
    public Object id(final Object... args) {
        md(args).setId(string(1, args));
        return ret(args);
    }

    @Override
    public Object lastModified(final Object... args) {
        Date date = date(1, args);
        if (date != null) {
            md(args).setLastModified(date);
        }
        return ret(args);
    }

    @Override
    public Object lastModifiedUtc(final Object... args) {
        Date date = date(1, args);
        if (date != null) {
            md(args).setLastModified(date);
        }
        return ret(args);
    }

    @Override
    public Object lockedUntil(final Object... args) {
        if (1 > args.length || null == args[1]) {
            md(args).setLockedUntil(null);
        } else if (args[1] instanceof Number) {
            long value = ((Number) args[1]).longValue();
            md(args).setLockedUntil(0 < value ? new Date(value) : null);
        } else {
            md(args).setLockedUntil(date(1, args));
        }
        return ret(args);
    }

    @Override
    public Object modifiedBy(final Object... args) {
        md(args).setModifiedBy(integer(1, args));
        return ret(args);
    }

    @Override
    public Object numberOfVersions(final Object... args) {
        md(args).setNumberOfVersions(integer(1, args));
        return ret(args);
    }

    @Override
    public Object sequenceNumber(final Object... args) {
        md(args).setSequenceNumber(longValue(1, args));
        return ret(args);
    }

    @Override
    public Object title(final Object... args) {
        md(args).setTitle(string(1, args));
        return ret(args);
    }

    @Override
    public Object url(final Object... args) {
        md(args).setURL(string(1, args));
        return ret(args);
    }

    @Override
    public Object version(final Object... args) {
        md(args).setVersion(string(1, args));
        return ret(args);
    }


    @Override
    public Object versionComment(final Object... args) {
        md(args).setVersionComment(string(1, args));
        return ret(args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object meta(Object... args) {
        if (args[1] instanceof Map<?,?>) {
            md(args).setMeta((Map<String, Object>) args[1]);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object objectPermissions(Object... args) {
        if (args[1] instanceof List<?>) {
            md(args).setObjectPermissions((List<FileStorageObjectPermission>) args[1]);
        }
        return null;
    }

    @Override
    public Object shareable(Object... args) {
        md(args).setShareable(bool(1, args));
        return null;
    }

    @Override
    public Object origin(Object... args) {
        if (args[1] instanceof FolderPath) {
            md(args).setOrigin((FolderPath) args[1]);
        } else if (args[1] != null) {
            md(args).setOrigin(FolderPath.parseFrom(args[1].toString()));
        } else {
            md(args).setOrigin(null);
        }
        return null;
    }

    @Override
    public Object captureDate(Object... args) {
        Date date = date(1, args);
        if (date != null) {
            md(args).setCaptureDate(date);
        }
        return ret(args);
    }

    @Override
    public Object geolocation(Object... args) {
        if (args[1] instanceof GeoLocation) {
            md(args).setGeoLocation((GeoLocation) args[1]);
        } else {
            md(args).setGeoLocation(null);
        }
        return null;
    }

    @Override
    public Object width(Object... args) {
        md(args).setWidth(longValue(1, args));
        return ret(args);
    }

    @Override
    public Object height(Object... args) {
        md(args).setHeight(longValue(1, args));
        return ret(args);
    }

    @Override
    public Object cameraMake(Object... args) {
        md(args).setCameraMake(string(1, args));
        return ret(args);
    }

    @Override
    public Object cameraModel(Object... args) {
        md(args).setCameraModel(string(1, args));
        return ret(args);
    }

    @Override
    public Object cameraIsoSpeed(Object... args) {
        md(args).setCameraIsoSpeed(longValue(1, args));
        return ret(args);
    }

    @Override
    public Object cameraAperture(Object... args) {
        md(args).setCameraAperture(doubleValue(1, args));
        return ret(args);
    }

    @Override
    public Object cameraExposureTime(Object... args) {
        md(args).setCameraExposureTime(doubleValue(1, args));
        return ret(args);
    }

    @Override
    public Object cameraFocalLength(Object... args) {
        md(args).setCameraFocalLength(doubleValue(1, args));
        return ret(args);
    }

    @Override
    public Object mediaMeta(Object... args) {
        if (args[1] instanceof Map<?,?>) {
            md(args).setMediaMeta((Map<String, Object>) args[1]);
        }
        return null;
    }

    @Override
    public Object mediaStatus(Object[] args) {
        if (args[1] instanceof MediaStatus) {
            md(args).setMediaStatus((MediaStatus) args[1]);
        } else {
            md(args).setMediaStatus(null);
        }
        return null;
    }

    @Override
    public Object mediaDate(Object[] args) {
        // Nothing to do
        return null;
    }

    @Override
    public Object created_from(Object... args) {
        if (EntityInfo.class.isInstance(args[1])) {
            md(args).setCreatedFrom((EntityInfo) args[1]);
        }
        return ret(args);
    }

    @Override
    public Object modified_from(Object... args) {
        if (EntityInfo.class.isInstance(args[1])) {
            md(args).setModifiedFrom((EntityInfo) args[1]);
        }
        return ret(args);
    }

    private Object ret(final Object[] args) {
        if (args.length >= 3 && null != args[2]) {
            return args[2];
        }
        return args[0];
    }

    @Override
    public Object unique_id(Object[] args) {
        md(args).setUniqueId(string(1, args));
        return ret(args);
    }

}
