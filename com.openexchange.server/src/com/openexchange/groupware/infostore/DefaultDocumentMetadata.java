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

package com.openexchange.groupware.infostore;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.media.MediaMetadataExtractorService;
import com.openexchange.java.GeoLocation;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link DefaultDocumentMetadata}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultDocumentMetadata implements DocumentMetadata {

    private static final long serialVersionUID = -5864163840354454650L;

    protected Date lastModified;
    protected Date creationDate;
    protected int modifiedBy;
    protected long folderId;
    protected long originalFolderId = -1;
    protected String title;
    protected int version;
    protected String content;
    protected long fileSize;
    protected String fileMIMEType;
    protected String fileName;
    protected int id;
    protected int originalId = -1;
    protected int createdBy;
    protected String description;
    protected String url;
    protected Long sequenceNumber;
    protected String categories;
    protected Date lockedUntil;
    protected String fileMD5Sum;
    protected int colorLabel;
    protected boolean currentVersion;
    protected String versionComment;
    protected String filestoreLocation;
    protected int numberOfVersions;
    protected Map<String, Object> meta;
    protected List<ObjectPermission> objectPermissions;
    protected Map<String, String> properties;
    protected boolean shareable;
    protected InfostoreFolderPath originFolderPath;
    protected Date captureDate;
    protected GeoLocation geoLocation;
    protected Long width = null;
    protected Long height = null;
    private String cameraMake = null;
    private String cameraModel = null;
    private Long cameraIsoSpeed = null;
    private Double cameraAperture = null;
    private Double cameraExposureTime = null;
    private Double cameraFocalLength = null;
    protected Map<String, Object> mediaMeta;
    protected MediaStatus mediaStatus = null;
    protected EntityInfo createdFrom;
    protected EntityInfo modifiedFrom;

    /**
     * Initializes a new {@link DefaultDocumentMetadata}.
     */
    protected DefaultDocumentMetadata() {
        super();
    }

    @Override
    public String getProperty(String key) {
        return null != properties ? properties.get(key) : null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return null != properties ? properties.keySet() : Collections.<String>emptySet();
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public int getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public long getFolderId() {
        return folderId;
    }

    @Override
    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    @Override
    public long getOriginalFolderId() {
        return originalFolderId >= 0 ? originalFolderId : getFolderId();
    }

    @Override
    public void setOriginalFolderId(long id) {
        originalFolderId = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String getFileMIMEType() {
        return fileMIMEType;
    }

    @Override
    public void setFileMIMEType(String fileMIMEType) {
        this.fileMIMEType = fileMIMEType;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getOriginalId() {
        if (originalId < 0) {
            return getId();
        }
        return originalId;
    }

    @Override
    public void setOriginalId(int id) {
        originalId = id;
    }

    @Override
    public int getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public void setURL(String url) {
        this.url = url;
    }

    @Override
    public long getSequenceNumber() {
        if (null != sequenceNumber) {
            return sequenceNumber.longValue();
        }
        Date lastModDate = getLastModified();
        if (null != lastModDate) {
            return lastModDate.getTime();
        }
        return 0;
    }

    @Override
    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = Long.valueOf(sequenceNumber);
    }

    @Override
    public String getCategories() {
        return categories;
    }

    @Override
    public void setCategories(String categories) {
        this.categories = categories;
    }

    @Override
    public Date getLockedUntil() {
        return lockedUntil;
    }

    @Override
    public void setLockedUntil(Date lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    @Override
    public String getFileMD5Sum() {
        return fileMD5Sum;
    }

    @Override
    public void setFileMD5Sum(String fileMD5Sum) {
        this.fileMD5Sum = fileMD5Sum;
    }

    @Override
    public int getColorLabel() {
        return colorLabel;
    }

    @Override
    public void setColorLabel(int colorLabel) {
        this.colorLabel = colorLabel;
    }

    @Override
    public boolean isCurrentVersion() {
        return currentVersion;
    }

    @Override
    public void setIsCurrentVersion(boolean currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    public String getVersionComment() {
        return versionComment;
    }

    @Override
    public void setVersionComment(String versionComment) {
        this.versionComment = versionComment;
    }

    @Override
    public String getFilestoreLocation() {
        return filestoreLocation;
    }

    @Override
    public void setFilestoreLocation(String filestoreLocation) {
        this.filestoreLocation = filestoreLocation;
    }

    @Override
    public int getNumberOfVersions() {
        return numberOfVersions;
    }

    @Override
    public void setNumberOfVersions(int numberOfVersions) {
        this.numberOfVersions = numberOfVersions;
    }

    @Override
    public Map<String, Object> getMeta() {
        return meta;
    }

    @Override
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    @Override
    public List<ObjectPermission> getObjectPermissions() {
        return objectPermissions;
    }

    @Override
    public void setObjectPermissions(List<ObjectPermission> objectPermissions) {
        this.objectPermissions = objectPermissions;
    }

    @Override
    public boolean isShareable() {
        return shareable;
    }

    @Override
    public void setShareable(boolean shareable) {
        this.shareable = shareable;
    }

    @Override
    public InfostoreFolderPath getOriginFolderPath() {
        return originFolderPath;
    }

    @Override
    public void setOriginFolderPath(InfostoreFolderPath originFolderPath) {
        this.originFolderPath = originFolderPath;
    }

    @Override
    public Date getCaptureDate() {
        return captureDate;
    }

    @Override
    public void setCaptureDate(Date captureDate) {
        this.captureDate = captureDate;
    }

    @Override
    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    @Override
    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    public Long getWidth() {
        return width;
    }

    @Override
    public void setWidth(long width) {
        if (width < 0) {
            this.width = null;
        } else {
            this.width = Long.valueOf(width);
        }
    }

    @Override
    public Long getHeight() {
        return height;
    }

    @Override
    public void setHeight(long height) {
        if (height < 0) {
            this.height = null;
        } else {
            this.height = Long.valueOf(height);
        }
    }

    @Override
    public Long getCameraIsoSpeed() {
        return cameraIsoSpeed;
    }

    @Override
    public void setCameraIsoSpeed(long isoSpeed) {
        if (isoSpeed < 0) {
            this.cameraIsoSpeed = null;
        } else {
            this.cameraIsoSpeed = Long.valueOf(isoSpeed);
        }
    }

    @Override
    public Double getCameraAperture() {
        return cameraAperture;
    }

    @Override
    public void setCameraAperture(double aperture) {
        if (aperture < 0) {
            this.cameraAperture = null;
        } else {
            this.cameraAperture = Double.valueOf(aperture);
        }
    }

    @Override
    public Double getCameraExposureTime() {
        return cameraExposureTime;
    }

    @Override
    public void setCameraExposureTime(double exposureTime) {
        if (exposureTime < 0) {
            this.cameraExposureTime = null;
        } else {
            this.cameraExposureTime = Double.valueOf(exposureTime);
        }
    }

    @Override
    public Double getCameraFocalLength() {
        return cameraFocalLength;
    }

    @Override
    public void setCameraFocalLength(double focalLength) {
        if (focalLength < 0) {
            this.cameraFocalLength = null;
        } else {
            this.cameraFocalLength = Double.valueOf(focalLength);
        }
    }

    @Override
    public String getCameraMake() {
        return cameraMake;
    }

    @Override
    public void setCameraMake(String cameraMake) {
        this.cameraMake = cameraMake;
    }

    @Override
    public String getCameraModel() {
        return cameraModel;
    }

    @Override
    public void setCameraModel(String cameraModel) {
        this.cameraModel = cameraModel;
    }

    @Override
    public Map<String, Object> getMediaMeta() {
        return mediaMeta;
    }

    @Override
    public void setMediaMeta(Map<String, Object> mediaMeta) {
        this.mediaMeta = mediaMeta;
    }

    @Override
    public MediaStatus getMediaStatus() {
        return mediaStatus;
    }

    @Override
    public MediaStatus getMediaStatusForClient(Session session) {
        MediaStatus mediaStatus = getMediaStatus();
        if (null != mediaStatus) {
            return mediaStatus;
        }

        MediaMetadataExtractorService extractorService = ServerServiceRegistry.getInstance().getService(MediaMetadataExtractorService.class);
        if (null == extractorService) {
            return null;
        }

        try {
            return extractorService.isScheduledForMediaMetadataExtraction(this, session) ? MediaStatus.pending() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setMediaStatus(MediaStatus mediaStatus) {
        this.mediaStatus = mediaStatus;
    }

    @Override
    public EntityInfo getCreatedFrom() {
        return createdFrom;
    }

    @Override
    public void setCreatedFrom(EntityInfo createdFrom) {
        this.createdFrom = createdFrom;
    }

    @Override
    public EntityInfo getModifiedFrom() {
        return modifiedFrom;
    }

    @Override
    public void setModifiedFrom(EntityInfo modifiedFrom) {
        this.modifiedFrom = modifiedFrom;
    }

    @Override
    public String toString() {
        return "DefaultDocumentMetadata [id=" + id + ", folderId=" + folderId + ", fileName=" + fileName + "]";
    }

}
