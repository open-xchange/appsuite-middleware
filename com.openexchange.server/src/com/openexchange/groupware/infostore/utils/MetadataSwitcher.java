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

package com.openexchange.groupware.infostore.utils;

public interface MetadataSwitcher {

	Object lastModified();

	Object creationDate();

	Object modifiedBy();

	Object folderId();

	Object title();

	Object version();

	Object content();

	Object id();

	Object fileSize();

	Object description();

	Object url();

	Object createdBy();

	Object fileName();

	Object fileMIMEType();

	Object sequenceNumber();

	Object categories();

	Object lockedUntil();

	Object fileMD5Sum();

	Object versionComment();

	Object currentVersion();

	Object colorLabel();

	Object filestoreLocation();

    Object lastModifiedUTC();

    Object numberOfVersions();

    Object meta();

    Object objectPermissions();

    Object shareable();

    Object origin();

    Object captureDate();

    Object geolocation();

    Object width();

    Object height();

    Object cameraMake();

    Object cameraModel();

    Object cameraIsoSpeed();

    Object cameraAperture();

    Object cameraExposureTime();

    Object cameraFocalLength();

    Object mediaMeta();

    Object mediaStatus();

    Object mediaDate();
}
