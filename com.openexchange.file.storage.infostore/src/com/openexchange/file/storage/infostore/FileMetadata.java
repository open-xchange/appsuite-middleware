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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FolderPath;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.infostore.internal.Utils;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreFolderPath;
import com.openexchange.java.GeoLocation;
import com.openexchange.session.Session;


/**
 * {@link FileMetadata}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileMetadata implements DocumentMetadata {

    private static final long serialVersionUID = 2097235173990058583L;

    private final File file;
    private String fileSpool;

    public FileMetadata(final File document) throws OXException {
        validate(document);
        this.file = document;
    }

    @Override
    public String getCategories() {
        return file.getCategories();
    }

    @Override
    public int getColorLabel() {
        return file.getColorLabel();
    }

    @Override
    public String getContent() {
        return file.getContent();
    }

    @Override
    public Date getCreationDate() {
        return file.getCreated();
    }

    @Override
    public int getCreatedBy() {
        return file.getCreatedBy();
    }

    @Override
    public String getDescription() {
        return file.getDescription();
    }

    @Override
    public String getFileMD5Sum() {
        return file.getFileMD5Sum();
    }

    @Override
    public String getFileMIMEType() {
        return file.getFileMIMEType();
    }

    @Override
    public String getFileName() {
        return file.getFileName();
    }

    @Override
    public long getFileSize() {
        return file.getFileSize();
    }

    @Override
    public long getFolderId() {
        String folderId = file.getFolderId();
        if (folderId == null) {
            String fileId = file.getId();
            if (fileId == FileStorageFileAccess.NEW) {
                return -1;
            }

            String unmangledFolderId = new com.openexchange.file.storage.composition.FileID(fileId).getFolderId();
            if (null != unmangledFolderId) {
                return Long.parseLong(unmangledFolderId);
            }
        }

        return Long.parseLong(new com.openexchange.file.storage.composition.FolderID(folderId).getFolderId());
    }

    @Override
    public int getId() {
        String fileId = file.getId();
        if (fileId == FileStorageFileAccess.NEW) {
            return InfostoreFacade.NEW;
        }

        return Integer.parseInt(new com.openexchange.file.storage.composition.FileID(fileId).getFileId());
    }

    @Override
    public Date getLastModified() {
        return file.getLastModified();
    }

    @Override
    public Date getLockedUntil() {
        return file.getLockedUntil();
    }

    @Override
    public int getModifiedBy() {
        return file.getModifiedBy();
    }

    @Override
    public int getNumberOfVersions() {
        return file.getNumberOfVersions();
    }

    @Override
    public String getProperty(final String key) {
        return file.getProperty(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return file.getPropertyNames();
    }

    @Override
    public long getSequenceNumber() {
        return file.getSequenceNumber();
    }

    @Override
    public String getTitle() {
        return file.getTitle();
    }

    @Override
    public String getURL() {
        return file.getURL();
    }

    @Override
    public int getVersion() {
        final String version = file.getVersion();
        return com.openexchange.java.Strings.isEmpty(version) ? -1 : Utils.parseUnsignedInt(version);
    }

    @Override
    public String getVersionComment() {
        return file.getVersionComment();
    }

    @Override
    public boolean isCurrentVersion() {
        return file.isCurrentVersion();
    }

    @Override
    public void setCategories(final String categories) {
        file.setCategories(categories);
    }

    @Override
    public void setColorLabel(final int color) {
        file.setColorLabel(color);
    }

    @Override
    public void setCreationDate(final Date creationDate) {
        file.setCreated(creationDate);
    }

    @Override
    public void setCreatedBy(final int cretor) {
        file.setCreatedBy(cretor);
    }

    @Override
    public void setDescription(final String description) {
        file.setDescription(description);
    }

    @Override
    public void setFileMD5Sum(final String sum) {
        file.setFileMD5Sum(sum);
    }

    @Override
    public void setFileMIMEType(final String type) {
        file.setFileMIMEType(type);
    }

    @Override
    public void setFileName(final String fileName) {
        file.setFileName(fileName);
    }

    @Override
    public void setFileSize(final long length) {
        file.setFileSize(length);
    }

    @Override
    public void setFolderId(final long folderId) {
        file.setFolderId(Long.toString(folderId));
    }

    @Override
    public void setId(final int id) {
        file.setId(String.valueOf(id));
    }

    @Override
    public void setIsCurrentVersion(final boolean bool) {
        file.setIsCurrentVersion(bool);
    }

    @Override
    public void setLastModified(final Date now) {
        file.setLastModified(now);
    }

    @Override
    public void setLockedUntil(final Date lockedUntil) {
        file.setLockedUntil(lockedUntil);
    }

    @Override
    public void setModifiedBy(final int lastEditor) {
        file.setModifiedBy(lastEditor);
    }

    @Override
    public void setNumberOfVersions(final int numberOfVersions) {
        file.setNumberOfVersions(numberOfVersions);
    }

    @Override
    public void setTitle(final String title) {
        file.setTitle(title);
    }

    @Override
    public void setURL(final String url) {
        file.setURL(url);
    }

    @Override
    public void setVersion(final int version) {
        file.setVersion(version < 0 ? FileStorageFileAccess.CURRENT_VERSION : Integer.toString(version));
    }

    @Override
    public void setVersionComment(final String string) {
        file.setVersionComment(string);
    }

    @Override
    public String getFilestoreLocation() {
        return fileSpool;
    }

    @Override
    public void setFilestoreLocation(final String string) {
        this.fileSpool = string;
    }

    @Override
    public Map<String, Object> getMeta() {
        return file.getMeta();
    }

    @Override
    public void setMeta(final Map<String, Object> properties) {
        file.setMeta(properties);
    }

    @Override
    public List<ObjectPermission> getObjectPermissions() {
        return PermissionHelper.getObjectPermissions(file.getObjectPermissions());
    }

    @Override
    public void setObjectPermissions(List<ObjectPermission> objectPermissions) {
        file.setObjectPermissions(PermissionHelper.getFileStorageObjectPermissions(objectPermissions));
    }

    @Override
    public boolean isShareable() {
        return file.isShareable();
    }

    @Override
    public void setShareable(boolean shareable) {
        file.setShareable(shareable);
    }

    /**
     * Checks the supplied file metadata for infostore compatibility.
     *
     * @param file The file to check
     * @throws OXException If validation fails
     */
    private static void validate(final File file) throws OXException {
        if (null != file) {
            /*
             * check for numerical identifiers if set
             */
            String id = file.getId();
            if (FileStorageFileAccess.NEW != id) {
                try {
                    // -1 is a valid id
                    Integer.valueOf(id);
                } catch (NumberFormatException e) {
                    throw FileStorageExceptionCodes.INVALID_FILE_IDENTIFIER.create(e, id);
                }
            }

            String folderID = file.getFolderId();
            if (null != folderID && Utils.parseUnsignedLong(folderID) < 0) {
                throw FileStorageExceptionCodes.INVALID_FOLDER_IDENTIFIER.create(folderID);
            }
        }
    }

    /**
     * Gets the InfoStore {@link DocumentMetadata} from given file.
     *
     * @param file The file
     * @return The appropriate {@link DocumentMetadata} instance
     */
    public static DocumentMetadata getMetadata(final File file) {
        final DocumentMetadata metaData = new DocumentMetadata() {

            private static final long serialVersionUID = -1476628439761201503L;

            @Override
            public void setVersionComment(final String string) {
                // nothing to do
            }

            @Override
            public void setVersion(final int version) {
                // nothing to do
            }

            @Override
            public void setURL(final String url) {
                // nothing to do
            }

            @Override
            public void setTitle(final String title) {
                // nothing to do
            }

            @Override
            public void setNumberOfVersions(final int numberOfVersions) {
                // nothing to do
            }

            @Override
            public void setModifiedBy(final int lastEditor) {
                // nothing to do
            }

            @Override
            public void setLockedUntil(final Date lockedUntil) {
                // nothing to do
            }

            @Override
            public void setLastModified(final Date now) {
                // nothing to do
            }

            @Override
            public void setIsCurrentVersion(final boolean bool) {
                // nothing to do
            }

            @Override
            public void setId(final int id) {
                // nothing to do
            }

            @Override
            public void setFolderId(final long folderId) {
                // nothing to do
            }

            @Override
            public void setFilestoreLocation(final String string) {
                // nothing to do
            }

            @Override
            public void setFileSize(final long length) {
                // nothing to do
            }

            @Override
            public void setFileName(final String fileName) {
                // nothing to do
            }

            @Override
            public void setFileMIMEType(final String type) {
                // nothing to do
            }

            @Override
            public void setFileMD5Sum(final String sum) {
                // nothing to do
            }

            @Override
            public void setDescription(final String description) {
                // nothing to do
            }

            @Override
            public void setCreationDate(final Date creationDate) {
                // nothing to do
            }

            @Override
            public void setCreatedBy(final int cretor) {
                // nothing to do
            }

            @Override
            public void setColorLabel(final int color) {
                // nothing to do
            }

            @Override
            public void setCategories(final String categories) {
                // nothing to do
            }

            @Override
            public void setOriginalId(int id) {
                // nothing to do
            }

            @Override
            public void setOriginalFolderId(long id) {
                // nothing to do
            }

            @Override
            public boolean isCurrentVersion() {
                return file.isCurrentVersion();
            }

            @Override
            public String getVersionComment() {
                return file.getVersionComment();
            }

            @Override
            public int getVersion() {
                return Utils.parseUnsignedInt(file.getVersion());
            }

            @Override
            public String getURL() {
                return file.getURL();
            }

            @Override
            public String getTitle() {
                return file.getTitle();
            }

            @Override
            public long getSequenceNumber() {
                return file.getSequenceNumber();
            }

            @Override
            public Set<String> getPropertyNames() {
                return file.getPropertyNames();
            }

            @Override
            public String getProperty(final String key) {
                return file.getProperty(key);
            }

            @Override
            public int getNumberOfVersions() {
                return file.getNumberOfVersions();
            }

            @Override
            public int getModifiedBy() {
                return file.getModifiedBy();
            }

            @Override
            public Date getLockedUntil() {
                return file.getLockedUntil();
            }

            @Override
            public Date getLastModified() {
                return file.getLastModified();
            }

            @Override
            public int getId() {
                String id = file.getId();
                if (FileStorageFileAccess.NEW == id) {
                    return InfostoreFacade.NEW;
                }
                return Utils.parseUnsignedInt(new FileID(id).getFileId());
            }

            @Override
            public long getFolderId() {
                String id = file.getFolderId();
                if (FileStorageFileAccess.NEW == id) {
                    return InfostoreFacade.NEW;
                }
                return Long.parseLong(new FolderID(id).getFolderId());
            }

            @Override
            public String getFilestoreLocation() {
                // TODO
                return null;
            }

            @Override
            public long getFileSize() {
                return file.getFileSize();
            }

            @Override
            public String getFileName() {
                return file.getFileName();
            }

            @Override
            public String getFileMIMEType() {
                return file.getFileMIMEType();
            }

            @Override
            public String getFileMD5Sum() {
                return file.getFileMD5Sum();
            }

            @Override
            public String getDescription() {
                return file.getDescription();
            }

            @Override
            public Date getCreationDate() {
                return file.getCreated();
            }

            @Override
            public int getCreatedBy() {
                return file.getCreatedBy();
            }

            @Override
            public String getContent() {
                return file.getContent();
            }

            @Override
            public int getColorLabel() {
                return file.getColorLabel();
            }

            @Override
            public String getCategories() {
                return file.getCategories();
            }

            @Override
            public Map<String, Object> getMeta() {
                return file.getMeta();
            }

            @Override
            public void setMeta(Map<String, Object> properties) {
                // Nothing to do
            }

            @Override
            public List<ObjectPermission> getObjectPermissions() {
                return PermissionHelper.getObjectPermissions(file.getObjectPermissions());
            }

            @Override
            public void setObjectPermissions(List<ObjectPermission> objectPermissions) {
                // Nothing to do
            }

            @Override
            public boolean isShareable() {
                return file.isShareable();
            }

            @Override
            public void setShareable(boolean shareable) {
                file.setShareable(shareable);
            }

            @Override
            public int getOriginalId() {
                if (file instanceof UserizedFile) {
                    return Utils.parseUnsignedInt(((UserizedFile) file).getOriginalId());
                }

                return getId();
            }

            @Override
            public long getOriginalFolderId() {
                if (file instanceof UserizedFile) {
                    return Utils.parseUnsignedLong(((UserizedFile) file).getOriginalFolderId());
                }

                return getFolderId();
            }

            @Override
            public void setSequenceNumber(long sequenceNumber) {
                file.setSequenceNumber(sequenceNumber);
            }

            @Override
            public InfostoreFolderPath getOriginFolderPath() {
                return null;
            }

            @Override
            public void setOriginFolderPath(InfostoreFolderPath originFolderPath) {
                // nothing to do
            }

            @Override
            public Date getCaptureDate() {
                return file.getCaptureDate();
            }

            @Override
            public void setCaptureDate(Date captureDate) {
                // nothing to do
            }

            @Override
            public GeoLocation getGeoLocation() {
                return file.getGeoLocation();
            }

            @Override
            public void setGeoLocation(GeoLocation geoLocation) {
                // nothing to do
            }

            @Override
            public Long getWidth() {
                return file.getWidth();
            }

            @Override
            public void setWidth(long width) {
                // nothing to do
            }

            @Override
            public Long getHeight() {
                return file.getHeight();
            }

            @Override
            public void setHeight(long height) {
                // nothing to do
            }

            @Override
            public Long getCameraIsoSpeed() {
                return file.getCameraIsoSpeed();
            }

            @Override
            public void setCameraIsoSpeed(long isoSpeed) {
                // nothing to do
            }

            @Override
            public Double getCameraAperture() {
                return file.getCameraAperture();
            }

            @Override
            public void setCameraAperture(double aperture) {
                // nothing to do
            }

            @Override
            public Double getCameraExposureTime() {
                return file.getCameraExposureTime();
            }

            @Override
            public void setCameraExposureTime(double exposureTime) {
                // nothing to do
            }

            @Override
            public Double getCameraFocalLength() {
                return file.getCameraFocalLength();
            }

            @Override
            public void setCameraFocalLength(double focalLength) {
                // nothing to do
            }

            @Override
            public String getCameraMake() {
                return file.getCameraMake();
            }

            @Override
            public void setCameraMake(String cameraMake) {
                // nothing to do
            }

            @Override
            public String getCameraModel() {
                return file.getCameraModel();
            }

            @Override
            public void setCameraModel(String cameraModel) {
                // nothing to do
            }

            @Override
            public Map<String, Object> getMediaMeta() {
                return file.getMediaMeta();
            }

            @Override
            public void setMediaMeta(Map<String, Object> mediaMeta) {
                // nothing to do
            }

            @Override
            public MediaStatus getMediaStatus() {
                return file.getMediaStatus();
            }

            @Override
            public void setMediaStatus(MediaStatus infostoreMediaStatus) {
                // nothing to do
            }

            @Override
            public MediaStatus getMediaStatusForClient(Session session) {
                return file.getMediaStatus();
            }

            @Override
            public EntityInfo getCreatedFrom() {
                return file.getCreatedFrom();
            }

            @Override
            public void setCreatedFrom(EntityInfo createdFrom) {
                // nothing to do
            }

            @Override
            public EntityInfo getModifiedFrom() {
                return file.getModifiedFrom();
            }

            @Override
            public void setModifiedFrom(EntityInfo modifiedFrom) {
                // nothing to do
            }

        };
        return metaData;
    }

    @Override
    public int getOriginalId() {
        if (file instanceof UserizedFile) {
            return Utils.parseUnsignedInt(((UserizedFile) file).getOriginalId());
        }

        return getId();
    }

    @Override
    public void setOriginalId(int id) {
        if (file instanceof UserizedFile) {
            ((UserizedFile) file).setOriginalId(Integer.toString(id));
        }
    }

    @Override
    public long getOriginalFolderId() {
        if (file instanceof UserizedFile) {
            return Long.parseLong(((UserizedFile) file).getOriginalFolderId());
        }

        return getFolderId();
    }

    @Override
    public void setOriginalFolderId(long id) {
        if (file instanceof UserizedFile) {
            ((UserizedFile) file).setOriginalFolderId(Long.toString(id));
        }
    }

    @Override
    public void setSequenceNumber(long sequenceNumber) {
        file.setSequenceNumber(sequenceNumber);
    }

    @Override
    public InfostoreFolderPath getOriginFolderPath() {
        FolderPath folderPath = file.getOrigin();
        return null == folderPath ? null : InfostoreFolderPath.copyOf(folderPath);
    }

    @Override
    public void setOriginFolderPath(InfostoreFolderPath originFolderPath) {
        file.setOrigin(null == originFolderPath ? null : FolderPath.copyOf(originFolderPath));
    }

    @Override
    public Date getCaptureDate() {
        return file.getCaptureDate();
    }

    @Override
    public void setCaptureDate(Date captureDate) {
        file.setCaptureDate(captureDate);
    }

    @Override
    public GeoLocation getGeoLocation() {
        return file.getGeoLocation();
    }

    @Override
    public void setGeoLocation(GeoLocation geoLocation) {
        file.setGeoLocation(geoLocation);
    }

    @Override
    public Long getWidth() {
        return file.getWidth();
    }

    @Override
    public void setWidth(long width) {
        file.setWidth(width);
    }

    @Override
    public Long getHeight() {
        return file.getHeight();
    }

    @Override
    public void setHeight(long height) {
        file.setHeight(height);
    }

    @Override
    public Long getCameraIsoSpeed() {
        return file.getCameraIsoSpeed();
    }

    @Override
    public void setCameraIsoSpeed(long isoSpeed) {
        file.setCameraIsoSpeed(isoSpeed);
    }

    @Override
    public Double getCameraAperture() {
        return file.getCameraAperture();
    }

    @Override
    public void setCameraAperture(double aperture) {
        file.setCameraAperture(aperture);
    }

    @Override
    public Double getCameraExposureTime() {
        return file.getCameraExposureTime();
    }

    @Override
    public void setCameraExposureTime(double exposureTime) {
        file.setCameraExposureTime(exposureTime);
    }

    @Override
    public Double getCameraFocalLength() {
        return file.getCameraFocalLength();
    }

    @Override
    public void setCameraFocalLength(double focalLength) {
        file.setCameraFocalLength(focalLength);
    }

    @Override
    public String getCameraMake() {
        return file.getCameraMake();
    }

    @Override
    public void setCameraMake(String cameraMake) {
        file.setCameraMake(cameraMake);
    }

    @Override
    public String getCameraModel() {
        return file.getCameraModel();
    }

    @Override
    public void setCameraModel(String cameraModel) {
        file.setCameraModel(cameraModel);
    }

    @Override
    public Map<String, Object> getMediaMeta() {
        return file.getMediaMeta();
    }

    @Override
    public void setMediaMeta(Map<String, Object> mediaMeta) {
        file.setMediaMeta(mediaMeta);
    }

    @Override
    public MediaStatus getMediaStatus() {
        return file.getMediaStatus();
    }

    @Override
    public void setMediaStatus(MediaStatus infostoreMediaStatus) {
        file.setMediaStatus(infostoreMediaStatus);
    }

    @Override
    public MediaStatus getMediaStatusForClient(Session session) {
        return file.getMediaStatus();
    }

    @Override
    public EntityInfo getCreatedFrom() {
        return file.getCreatedFrom();
    }

    @Override
    public void setCreatedFrom(EntityInfo createdFrom) {
        file.setCreatedFrom(createdFrom);
    }

    @Override
    public EntityInfo getModifiedFrom() {
        return file.getModifiedFrom();
    }

    @Override
    public void setModifiedFrom(EntityInfo modifiedFrom) {
        file.setModifiedFrom(modifiedFrom);
    }

}
