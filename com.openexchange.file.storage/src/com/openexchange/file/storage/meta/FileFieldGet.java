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
import com.openexchange.file.storage.AbstractFileFieldSwitcher;
import com.openexchange.file.storage.File;


/**
 * {@link FileFieldGet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileFieldGet extends AbstractFileFieldSwitcher {

    @Override
    public Object categories(final Object... args) {
        return md( args ).getCategories();
    }

    @Override
    public Object colorLabel(final Object... args) {
        return Integer.valueOf(md( args ).getColorLabel());
    }

    @Override
    public Object content(final Object... args) {
        return md( args ).getContent();
    }

    @Override
    public Object created(final Object... args) {
        return md( args ).getCreated();
    }

    @Override
    public Object createdBy(final Object... args) {
        return Integer.valueOf(md( args ).getCreatedBy());
    }

    @Override
    public Object currentVersion(final Object... args) {
        return Boolean.valueOf(md( args ).isCurrentVersion());
    }

    @Override
    public Object description(final Object... args) {
        return md( args ).getDescription();
    }

    @Override
    public Object fileMd5sum(final Object... args) {
        return md( args ).getFileMD5Sum();
    }

    @Override
    public Object fileMimetype(final Object... args) {
        return md( args ).getFileMIMEType();
    }

    @Override
    public Object fileSize(final Object... args) {
        return Long.valueOf(md( args ).getFileSize());
    }

    @Override
    public Object filename(final Object... args) {
        return md( args ).getFileName();
    }

    @Override
    public Object folderId(final Object... args) {
        return md( args ).getFolderId();
    }

    @Override
    public Object id(final Object... args) {
        return md( args ).getId();
    }

    @Override
    public Object lastModified(final Object... args) {
        return md( args ).getLastModified();
    }

    @Override
    public Object lastModifiedUtc(final Object... args) {
        return md( args ).getLastModified();
    }

    @Override
    public Object lockedUntil(final Object... args) {
        return md( args ).getLockedUntil();
    }

    @Override
    public Object modifiedBy(final Object... args) {
        return Integer.valueOf(md( args ).getModifiedBy());
    }

    @Override
    public Object numberOfVersions(final Object... args) {
        return Integer.valueOf(md( args ).getNumberOfVersions());
    }

    @Override
    public Object sequenceNumber(final Object... args) {
        return Long.valueOf(md( args ).getSequenceNumber());
    }

    @Override
    public Object title(final Object... args) {
        return md( args ).getTitle();
    }

    @Override
    public Object url(final Object... args) {
        return md( args ).getURL();
    }

    @Override
    public Object version(final Object... args) {
        return md( args ).getVersion();
    }

    @Override
    public Object versionComment(final Object... args) {
        return md( args ).getVersionComment();
    }

    @Override
    public Object meta(Object... args) {
        return md( args ).getMeta();
    }

    @Override
    public Object objectPermissions(Object... args) {
        return md( args ).getObjectPermissions();
    }

    @Override
    public Object shareable(Object... args) {
        return Boolean.valueOf(md(args).isShareable());
    }

    @Override
    public Object origin(Object... args) {
        return md(args).getOrigin();
    }

    @Override
    public Object captureDate(Object... args) {
        return md(args).getCaptureDate();
    }

    @Override
    public Object geolocation(Object... args) {
        return md(args).getGeoLocation();
    }

    @Override
    public Object width(Object... args) {
        return md(args).getWidth();
    }

    @Override
    public Object height(Object... args) {
        return md(args).getHeight();
    }

    @Override
    public Object cameraMake(Object... args) {
        return md(args).getCameraMake();
    }

    @Override
    public Object cameraModel(Object... args) {
        return md(args).getCameraModel();
    }

    @Override
    public Object cameraIsoSpeed(Object... args) {
        return md(args).getCameraIsoSpeed();
    }

    @Override
    public Object cameraAperture(Object... args) {
        return md(args).getCameraAperture();
    }

    @Override
    public Object cameraExposureTime(Object... args) {
        return md(args).getCameraExposureTime();
    }

    @Override
    public Object cameraFocalLength(Object... args) {
        return md(args).getCameraFocalLength();
    }

    @Override
    public Object mediaMeta(Object... args) {
        return md(args).getMediaMeta();
    }

    @Override
    public Object mediaStatus(Object[] args) {
        return md(args).getMediaStatus();
    }

    @Override
    public Object mediaDate(Object[] args) {
        File file = md(args);
        Date captureDate = file.getCaptureDate();
        return null == captureDate ? file.getLastModified() : captureDate;
    }

    @Override
    public Object created_from(Object... args) {
        return md(args).getCreatedFrom();
    }

    @Override
    public Object modified_from(Object... args) {
        return md(args).getModifiedFrom();
    }

    @Override
    public Object unique_id(Object[] args) {
        return md(args).getUniqueId();
    }

}
