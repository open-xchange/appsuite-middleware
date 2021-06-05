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

package com.openexchange.file.storage.dropbox.access;

import static com.openexchange.file.storage.dropbox.Utils.normalizeFolderId;
import java.util.Date;
import com.dropbox.core.v2.files.Dimensions;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GpsCoordinates;
import com.dropbox.core.v2.files.MediaMetadata;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.java.GeoLocation;
import com.openexchange.mime.MimeTypeMap;

/**
 * {@link DropboxFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxFile extends DefaultFile {

    private final long sequenceNumber;

    /**
     * Initialises a new {@link DropboxFile}.
     *
     * @param metadata The {@link FileMetadata} of the Dropbox file
     * @param userid The identifier of the user to use as created/modified-by information
     */
    public DropboxFile(FileMetadata metadata, int userid) {
        super();
        setId(metadata.getName());
        setFolderId(extractFolderId(metadata.getPathDisplay()));
        setCreatedBy(userid);
        setModifiedBy(userid);
        Date clientModified = metadata.getClientModified();
        Date serverModified = metadata.getServerModified();
        setCreated(null == clientModified ? serverModified : clientModified);
        setLastModified(null == clientModified ? serverModified : clientModified);
        sequenceNumber = null != serverModified ? serverModified.getTime() : 0;
        setVersion(metadata.getRev());
        setIsCurrentVersion(true);
        setNumberOfVersions(1);
        setFileSize(metadata.getSize());
        setFileMIMEType(DropboxServices.getService(MimeTypeMap.class).getContentType(metadata.getName()));
        setFileName(metadata.getName());
        setTitle(metadata.getName());
        setUniqueId(metadata.getId());

        applyMediaInfo(metadata);
    }

    /**
     * Gets the file's folder- and object-identifier inside an {@link IDTuple} structure.
     *
     * @return The ID tuple
     */
    public IDTuple getIDTuple() {
        return new IDTuple(getFolderId(), getId());
    }

    @Override
    public long getSequenceNumber() {
        return 0 != this.sequenceNumber ? sequenceNumber : super.getSequenceNumber();
    }

    @Override
    public String toString() {
        String folder = normalizeFolderId(getFolderId());
        return null == folder ? '/' + getId() : folder + '/' + getId();
    }

    /**
     * Extracts the folder from the specified full path
     *
     * @param path The full path to extract the parent folder
     * @return The extracted parent folder
     */
    private String extractFolderId(String path) {
        int lastIndex = path.lastIndexOf('/');
        return path.substring(0, lastIndex);
    }

    /**
     * Set photo/video related properties if available
     *
     * @param metadata The {@link FileMetadata}
     */
    private void applyMediaInfo(FileMetadata metadata) {
        if (null == metadata.getMediaInfo()) {
            setMediaStatus(MediaStatus.none());
            return;
        }

        if (metadata.getMediaInfo().isMetadata()) {
            setMediaStatus(MediaStatus.success());
            MediaMetadata values = metadata.getMediaInfo().getMetadataValue();
            setCaptureDate(values.getTimeTaken());
            GpsCoordinates coordinates = values.getLocation();
            if (null != coordinates) {
                setGeoLocation(new GeoLocation(coordinates.getLatitude(), coordinates.getLongitude()));
            }
            Dimensions dimensions = values.getDimensions();
            if (null != dimensions) {
                setHeight(values.getDimensions().getHeight());
                setWidth(values.getDimensions().getWidth());
            }
        } else if (metadata.getMediaInfo().isPending()) {
            setMediaStatus(MediaStatus.pending());
        } else {
            setMediaStatus(MediaStatus.failure());
        }
    }

}
