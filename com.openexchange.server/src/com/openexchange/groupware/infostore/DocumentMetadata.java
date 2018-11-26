/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware.infostore;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.java.GeoLocation;

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
     * Gets the name of the camera model associated with this file
     *
     * @return The camera model or <code>null</code> if unknown/not set
     */
    String getCameraModel();

    /**
     * Sets the name of the camera model associated with this file
     *
     * @param cameraModel The duration
     */
    void setCameraModel(String cameraModel);

    /**
     * Gets ISO speed value of a camera or input device associated with this file
     *
     * @return The ISO speed value or <code>null</code> if unknown/not set
     */
    Long getIsoSpeed();

    /**
     * Sets ISO speed value of a camera or input device associated with this file
     *
     * @param isoSpeed The ISO speed value
     */
    void setIsoSpeed(long isoSpeed);

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
     * Gets the status of parsing/analyzing media meta-data from the media resource associated with this file
     *
     * @return The media status
     */
    MediaStatus getMediaStatus();

    /**
     * Sets the status of parsing/analyzing media meta-data from the media resource associated with this file
     *
     * @param infostoreMediaStatus The media status
     */
    void setMediaStatus(MediaStatus infostoreMediaStatus);

    // --------------------------------------------------------- END OF MEDIA STUFF --------------------------------------------------------

}
