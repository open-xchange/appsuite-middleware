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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.java.GeoLocation;
import com.openexchange.session.Session;

public interface DocumentMetadata extends Serializable {

    // versioned
    String getProperty(String key);

    // versioned
    Set<String> getPropertyNames();

    // versioned persistent
    Date getLastModified();

    void setLastModified(Date now);

    // persistent
    Date getCreationDate();

    void setCreationDate(Date creationDate);

    // versioned persistent
    int getModifiedBy();

    void setModifiedBy(int lastEditor);

    // persistent
    long getFolderId();

    void setFolderId(long folderId);

    // persistent
    String getTitle();

    void setTitle(String title);

    // versioned persistent
    int getVersion();

    void setVersion(int version);

    // versioned transient
    String getContent();

    // versioned persistent
    long getFileSize();

    void setFileSize(long length);

    // versioned persistent
    String getFileMIMEType();

    void setFileMIMEType(String type);

    // versioned persistent
    String getFileName();

    void setFileName(String fileName);

    // persistent
    int getId();

    void setId(int id);

    // persistent
    int getCreatedBy();

    void setCreatedBy(int cretor);

    // persistent
    String getDescription();

    void setDescription(String description);

    // persistent
    String getURL();

    void setURL(String url);

    // versioned persistent
    long getSequenceNumber();

    void setSequenceNumber(long sequenceNumber);

    String getCategories();

    void setCategories(String categories);

    Date getLockedUntil();

    void setLockedUntil(Date lockedUntil);

    String getFileMD5Sum();

    void setFileMD5Sum(String sum);

    int getColorLabel();

    void setColorLabel(int color);

    boolean isCurrentVersion();

    void setIsCurrentVersion(boolean bool);

    String getVersionComment();

    void setVersionComment(String string);

    void setFilestoreLocation(String string);

    String getFilestoreLocation();

    // virtual
    void setNumberOfVersions(int numberOfVersions);

    int getNumberOfVersions();

    Map<String, Object> getMeta();

    void setMeta(Map<String, Object> properties);

    /**
     * Gets the object permissions in case they are defined.
     *
     * @return A list holding additional object permissions, or <code>null</code> if not defined
     */
    List<ObjectPermission> getObjectPermissions();

    /**
     * Sets the object permissions.
     *
     * @param objectPermissions The object permissions to set, or <code>null</code> to remove previously set permissions
     */
    void setObjectPermissions(List<ObjectPermission> objectPermissions);

    /**
     * Gets a value indicating whether the item can be shared to others based on underlying storage's capabilities and the permissions of
     * the requesting user.
     *
     * @return <code>true</code> if the file is shareable, <code>false</code>, otherwise
     */
    boolean isShareable();

    /**
     * Sets the flag indicating that the item can be shared to others based on underlying storage's capabilities and the permissions of
     * the requesting user.
     *
     * @param shareable <code>true</code> if the file is shareable, <code>false</code>, otherwise
     */
    void setShareable(boolean shareable);

    /**
     * Gets the original file ID, if the ID returned via {@link #getId()} is virtual.
     *
     * @return The original ID or delegates to {@link #getId()};
     */
    int getOriginalId();

    /**
     * Sets the original file ID, if the ID set via {@link #setId(int)} is virtual.
     */
    void setOriginalId(int id);

    /**
     * Gets the original folder ID, if the ID returned via {@link #getFolderId()} is virtual.
     *
     * @return The original ID or delegates to {@link #getFolderId()};
     */
    long getOriginalFolderId();

    /**
     * Sets the original folder ID, if the ID set via {@link #setFolderId(long)} is virtual.
     */
    void setOriginalFolderId(long id);

    /**
     * Gets the origin folder path
     *
     * @return The origin folder path
     */
    InfostoreFolderPath getOriginFolderPath();

    /**
     * Sets the origin folder path
     *
     * @param originFolderPath
     */
    void setOriginFolderPath(InfostoreFolderPath originFolderPath);

    /**
     * Gets the 'created_from' entity info
     * 
     * @return The 'created_from' entity info
     */
    EntityInfo getCreatedFrom();

    /**
     * Sets the 'created_from' entity info
     * 
     * @param createdFrom The 'created_from' entity info
     */
    void setCreatedFrom(EntityInfo createdFrom);

    /**
     * Gets the 'modified_from' entity info
     * 
     * @return The 'modified_from' entity info
     */
    EntityInfo getModifiedFrom();

    /**
     * Sets the 'modified_from' entity info
     * 
     * @param modifiedFrom The 'modified_from' entity info
     */
    void setModifiedFrom(EntityInfo modifiedFrom);

    // ------------------------------------------------------------ MEDIA STUFF ------------------------------------------------------------

    /**
     * Gets the capture date of the image associated with this file
     *
     * @return The capture date
     */
    Date getCaptureDate();

    /**
     * Sets the capture date of the image associated with this file
     *
     * @param captureDate The capture date
     */
    void setCaptureDate(Date captureDate);

    /**
     * Gets the geo location of the media resource associated with this file
     *
     * @return The geo location
     */
    GeoLocation getGeoLocation();

    /**
     * Sets the geo location of the media resource associated with this file
     *
     * @param geoLocation The geo location
     */
    void setGeoLocation(GeoLocation geoLocation);

    /**
     * Gets the width of the media resource associated with this file
     *
     * @return The width or <code>null</code> if unknown/not set
     */
    Long getWidth();

    /**
     * Sets the width of the media resource associated with this file
     *
     * @param width The width
     */
    void setWidth(long width);

    /**
     * Gets the height of the media resource associated with this file
     *
     * @return The height or <code>null</code> if unknown/not set
     */
    Long getHeight();

    /**
     * Sets the height of the media resource associated with this file
     *
     * @param heigth The height
     */
    void setHeight(long height);

    /**
     * Gets the name for the manufacturer of the recording equipment used to create the photo.
     *
     * @return The camera make or <code>null</code> if unknown/not set
     */
    String getCameraMake();

    /**
     * Sets the name for the manufacturer of the recording equipment used to create the photo.
     *
     * @param cameraMake The model make
     */
    void setCameraMake(String cameraMake);

    /**
     * Gets the model name or model number of the equipment used to create the photo.
     *
     * @return The camera model or <code>null</code> if unknown/not set
     */
    String getCameraModel();

    /**
     * Sets the model name or model number of the equipment used to create the photo
     *
     * @param cameraModel The model name
     */
    void setCameraModel(String cameraModel);

    /**
     * Gets ISO speed value of a camera or input device associated with this file
     *
     * @return The ISO speed value or <code>null</code> if unknown/not set
     */
    Long getCameraIsoSpeed();

    /**
     * Sets ISO speed value of a camera or input device associated with this file
     *
     * @param isoSpeed The ISO speed value
     */
    void setCameraIsoSpeed(long isoSpeed);

    /**
     * Gets the aperture used to create the photo (f-number).
     *
     * @return The value or <code>null</code> for none
     */
    java.lang.Double getCameraAperture();

    /**
     * Set the aperture used to create the photo (f-number).
     *
     * @param aperture The aperture
     */
    void setCameraAperture(double aperture);

    /**
     * Gets the focal length used to create the photo, in millimeters.
     *
     * @return The value or <code>null</code> for none
     */
    java.lang.Double getCameraFocalLength();

    /**
     * Sets the focal length used to create the photo, in millimeters.
     *
     * @param focalLength The focal length
     */
    void setCameraFocalLength(double focalLength);

    /**
     * Gets the length of the exposure, in seconds.
     *
     * @return The value or <code>null</code> for none
     */
    java.lang.Double getCameraExposureTime();

    /**
     * Sets the length of the exposure, in seconds.
     *
     * @param exposureTime The exposure time
     */
    void setCameraExposureTime(double exposureTime);

    /**
     * Gets the meta information for the media resource associated with this file
     *
     * @return The meta information
     */
    Map<String, Object> getMediaMeta();

    /**
     * Sets the meta information for the media resource associated with this file
     *
     * @param mediaMeta The meta information
     */
    void setMediaMeta(Map<String, Object> mediaMeta);

    /**
     * Gets the status of parsing/analyzing media meta-data from the media resource
     *
     * @return The media status
     */
    MediaStatus getMediaStatus();

    /**
     * Sets the status of parsing/analyzing media meta-data from the media resource
     *
     * @param infostoreMediaStatus The media status
     */
    void setMediaStatus(MediaStatus infostoreMediaStatus);

    /**
     * Gets the status of parsing/analyzing media meta-data from the media resource for the client
     *
     * @param session The client-associated session
     * @return The media status
     */
    MediaStatus getMediaStatusForClient(Session session);

    // --------------------------------------------------------- END OF MEDIA STUFF --------------------------------------------------------

}
