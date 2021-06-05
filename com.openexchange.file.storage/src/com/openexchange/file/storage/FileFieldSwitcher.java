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

package com.openexchange.file.storage;


/**
 * A {@link FileFieldSwitcher} allows to generically do work for a certain field
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface FileFieldSwitcher {

    Object lastModified(Object...args);
    Object created(Object...args);
    Object modifiedBy(Object...args);
    Object folderId(Object...args);
    Object title(Object...args);
    Object version(Object...args);
    Object content(Object...args);
    Object id(Object...args);
    Object fileSize(Object...args);
    Object description(Object...args);
    Object url(Object...args);
    Object createdBy(Object...args);
    Object filename(Object...args);
    Object fileMimetype(Object...args);
    Object sequenceNumber(Object...args);
    Object categories(Object...args);
    Object lockedUntil(Object...args);
    Object fileMd5sum(Object...args);
    Object versionComment(Object...args);
    Object currentVersion(Object...args);
    Object colorLabel(Object...args);
    Object lastModifiedUtc(Object...args);
    Object numberOfVersions(Object...args);
    Object meta(Object...args);
    Object objectPermissions(Object...args);
    Object shareable(Object...args);
    Object origin(Object...args);
    Object captureDate(Object... args);
    Object geolocation(Object... args);
    Object width(Object... args);
    Object height(Object... args);
    Object cameraMake(Object... args);
    Object cameraModel(Object... args);
    Object cameraIsoSpeed(Object... args);
    Object cameraAperture(Object... args);
    Object cameraExposureTime(Object... args);
    Object cameraFocalLength(Object... args);
    Object mediaMeta(Object... args);
    Object mediaStatus(Object[] args);
    Object mediaDate(Object[] args);
    Object created_from(Object...args);
    Object modified_from(Object...args);
    Object unique_id(Object[] args);

}
