
package com.openexchange.file.storage.infostore;

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.file.storage.AbstractFile;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStoragePersistentIDs;
import com.openexchange.file.storage.FolderPath;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.file.storage.infostore.internal.Utils;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreFolderPath;
import com.openexchange.java.GeoLocation;
import com.openexchange.session.Session;

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

/**
 * {@link InfostoreFile}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFile extends AbstractFile implements UserizedFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfostoreFile.class);
    private final DocumentMetadata document;

    /**
     * Initializes a new {@link InfostoreFile}.
     *
     * @param documentMetadata The underlying document metadata.
     */
    public InfostoreFile(final DocumentMetadata documentMetadata) {
        this.document = documentMetadata;
    }

    @Override
    public String getCategories() {
        return document.getCategories();
    }

    @Override
    public int getColorLabel() {
        return document.getColorLabel();
    }

    @Override
    public String getContent() {
        return document.getContent();
    }

    @Override
    public Date getCreated() {
        return document.getCreationDate();
    }

    @Override
    public int getCreatedBy() {
        return document.getCreatedBy();
    }

    @Override
    public String getDescription() {
        return document.getDescription();
    }

    @Override
    public String getFileMD5Sum() {
        return document.getFileMD5Sum();
    }

    @Override
    public String getFileMIMEType() {
        return document.getFileMIMEType();
    }

    @Override
    public String getFileName() {
        return document.getFileName();
    }

    @Override
    public long getFileSize() {
        return document.getFileSize();
    }

    @Override
    public String getFolderId() {
        return Long.toString(document.getFolderId());
    }

    @Override
    public String getId() {
        return Integer.toString(document.getId());
    }

    @Override
    public Date getLastModified() {
        return document.getLastModified();
    }

    @Override
    public Date getLockedUntil() {
        return document.getLockedUntil();
    }

    @Override
    public int getModifiedBy() {
        return document.getModifiedBy();
    }

    @Override
    public int getNumberOfVersions() {
        return document.getNumberOfVersions();
    }

    @Override
    public String getProperty(final String key) {
        return document.getProperty(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return document.getPropertyNames();
    }

    @Override
    public long getSequenceNumber() {
        return document.getSequenceNumber();
    }

    @Override
    public String getTitle() {
        return document.getTitle();
    }

    @Override
    public String getURL() {
        return document.getURL();
    }

    @Override
    public String getVersion() {
        return Integer.toString(document.getVersion());
    }

    @Override
    public String getVersionComment() {
        return document.getVersionComment();
    }

    @Override
    public boolean isCurrentVersion() {
        return document.isCurrentVersion();
    }

    @Override
    public void setCategories(final String categories) {
        document.setCategories(categories);
    }

    @Override
    public void setColorLabel(final int color) {
        document.setColorLabel(color);
    }

    @Override
    public void setCreatedBy(final int cretor) {
        document.setCreatedBy(cretor);
    }

    @Override
    public void setCreated(final Date creationDate) {
        document.setCreationDate(creationDate);
    }

    @Override
    public void setDescription(final String description) {
        document.setDescription(description);
    }

    @Override
    public void setFileMD5Sum(final String sum) {
        document.setFileMD5Sum(sum);
    }

    @Override
    public void setFileMIMEType(final String type) {
        document.setFileMIMEType(type);
    }

    @Override
    public void setFileName(final String fileName) {
        document.setFileName(fileName);
    }

    @Override
    public void setFileSize(final long length) {
        document.setFileSize(length);
    }

    @Override
    public void setFolderId(final String folderId) {
        if (folderId != null) {
            document.setFolderId(Long.parseLong(folderId));
        }
    }

    @Override
    public void setId(final String id) {
        if (id == FileStorageFileAccess.NEW) {
            document.setId(InfostoreFacade.NEW);
        } else {
            document.setId(Utils.parseUnsignedInt(id));
        }
    }

    @Override
    public void setIsCurrentVersion(final boolean bool) {
        document.setIsCurrentVersion(bool);
    }

    @Override
    public void setLastModified(final Date now) {
        document.setLastModified(now);
    }

    @Override
    public void setLockedUntil(final Date lockedUntil) {
        document.setLockedUntil(lockedUntil);
    }

    @Override
    public void setModifiedBy(final int lastEditor) {
        document.setModifiedBy(lastEditor);
    }

    @Override
    public void setNumberOfVersions(final int numberOfVersions) {
        document.setNumberOfVersions(numberOfVersions);
    }

    @Override
    public void setTitle(final String title) {
        document.setTitle(title);
    }

    @Override
    public void setURL(final String url) {
        document.setURL(url);
    }

    @Override
    public void setVersion(final String version) {
        document.setVersion(Utils.parseUnsignedInt(version));
    }

    @Override
    public void setVersionComment(final String string) {
        document.setVersionComment(string);
    }

    @Override
    public Map<String, Object> getMeta() {
        return document.getMeta();
    }

    @Override
    public void setMeta(final Map<String, Object> properties) {
        document.setMeta(properties);
    }

    @Override
    public List<FileStorageObjectPermission> getObjectPermissions() {
        return PermissionHelper.getFileStorageObjectPermissions(document.getObjectPermissions());
    }

    @Override
    public void setObjectPermissions(List<FileStorageObjectPermission> objectPermissions) {
        document.setObjectPermissions(PermissionHelper.getObjectPermissions(objectPermissions));
    }

    @Override
    public boolean isShareable() {
        return document.isShareable();
    }

    @Override
    public void setShareable(boolean shareable) {
        document.setShareable(shareable);
    }

    @Override
    public String getOriginalId() {
        return Integer.toString(document.getOriginalId());
    }

    @Override
    public void setOriginalId(String id) {
        document.setOriginalId(Utils.parseUnsignedInt(id));
    }

    @Override
    public String getOriginalFolderId() {
        return Long.toString(document.getOriginalFolderId());
    }

    @Override
    public void setOriginalFolderId(String id) {
        document.setOriginalFolderId(Long.parseLong(id));
    }

    @Override
    public void setSequenceNumber(long sequenceNumber) {
        document.setSequenceNumber(sequenceNumber);
    }

    @Override
    public FolderPath getOrigin() {
        InfostoreFolderPath folderPath = document.getOriginFolderPath();
        return null == folderPath ? null : FolderPath.copyOf(folderPath);
    }

    @Override
    public void setOrigin(FolderPath origin) {
        document.setOriginFolderPath(null == origin ? null : InfostoreFolderPath.copyOf(origin));
    }

    @Override
    public Date getCaptureDate() {
        return document.getCaptureDate();
    }

    @Override
    public void setCaptureDate(Date captureDate) {
        document.setCaptureDate(captureDate);
    }

    @Override
    public GeoLocation getGeoLocation() {
        return document.getGeoLocation();
    }

    @Override
    public void setGeoLocation(GeoLocation geoLocation) {
        document.setGeoLocation(geoLocation);
    }

    @Override
    public Long getWidth() {
        return document.getWidth();
    }

    @Override
    public void setWidth(long width) {
        document.setWidth(width);
    }

    @Override
    public Long getHeight() {
        return document.getHeight();
    }

    @Override
    public void setHeight(long height) {
        document.setHeight(height);
    }

    @Override
    public Long getCameraIsoSpeed() {
        return document.getCameraIsoSpeed();
    }

    @Override
    public void setCameraIsoSpeed(long isoSpeed) {
        document.setCameraIsoSpeed(isoSpeed);
    }

    @Override
    public Double getCameraAperture() {
        return document.getCameraAperture();
    }

    @Override
    public void setCameraAperture(double aperture) {
        document.setCameraAperture(aperture);
    }

    @Override
    public Double getCameraExposureTime() {
        return document.getCameraExposureTime();
    }

    @Override
    public void setCameraExposureTime(double exposureTime) {
        document.setCameraExposureTime(exposureTime);
    }

    @Override
    public Double getCameraFocalLength() {
        return document.getCameraFocalLength();
    }

    @Override
    public void setCameraFocalLength(double focalLength) {
        document.setCameraFocalLength(focalLength);
    }

    @Override
    public String getCameraMake() {
        return document.getCameraMake();
    }

    @Override
    public void setCameraMake(String cameraMake) {
        document.setCameraMake(cameraMake);
    }

    @Override
    public String getCameraModel() {
        return document.getCameraModel();
    }

    @Override
    public void setCameraModel(String cameraModel) {
        document.setCameraModel(cameraModel);
    }

    @Override
    public Map<String, Object> getMediaMeta() {
        return document.getMediaMeta();
    }

    @Override
    public void setMediaMeta(Map<String, Object> mediaMeta) {
        document.setMediaMeta(mediaMeta);
    }

    @Override
    public MediaStatus getMediaStatus() {
        return document.getMediaStatus();
    }

    @Override
    public void setMediaStatus(MediaStatus mediaStatus) {
        document.setMediaStatus(mediaStatus);
    }

    @Override
    public MediaStatus getMediaStatusForClient(Session session) {
        return document.getMediaStatusForClient(session);
    }

    @Override
    public EntityInfo getCreatedFrom() {
        return document.getCreatedFrom();
    }

    @Override
    public void setCreatedFrom(EntityInfo createdFrom) {
        document.setCreatedFrom(createdFrom);
    }

    @Override
    public EntityInfo getModifiedFrom() {
        return document.getModifiedFrom();
    }

    @Override
    public void setModifiedFrom(EntityInfo modifiedFrom) {
        document.setModifiedFrom(modifiedFrom);
    }

    @Override
    public String getUniqueId() {
        return I(document.getId()).toString();
    }

    @Override
    public void setUniqueId(String uniqueId) {
        LOGGER.debug("The unique id for a infostore file cannot be modified.");
    }

}
