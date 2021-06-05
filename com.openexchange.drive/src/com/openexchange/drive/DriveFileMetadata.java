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

package com.openexchange.drive;

import java.util.Date;

/**
 * {@link DriveFileMetadata}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveFileMetadata {

    private String directLink;
    private String directLinkFragments;
    private String previewLink;
    private String thumbnailLink;
    private Date modified;
    private Date created;
    private String checksum;
    private String fileName;
    private String mimeType;

    /**
     * Initializes a new {@link DriveFileMetadata}.
     */
    public DriveFileMetadata() {
        super();
    }

    /**
     * Gets the directLink
     *
     * @return The directLink
     */
    public String getDirectLink() {
        return directLink;
    }

    /**
     * Sets the directLink
     *
     * @param directLink The directLink to set
     */
    public void setDirectLink(String directLink) {
        this.directLink = directLink;
    }

    /**
     * Gets the directLinkFragments
     *
     * @return The directLinkFragments
     */
    public String getDirectLinkFragments() {
        return directLinkFragments;
    }

    /**
     * Sets the directLinkFragments
     *
     * @param directLinkFragments The directLinkFragments to set
     */
    public void setDirectLinkFragments(String directLinkFragments) {
        this.directLinkFragments = directLinkFragments;
    }

    /**
     * Gets the modified
     *
     * @return The modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * Sets the modified
     *
     * @param modified The modified to set
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Gets the fileName
     *
     * @return The fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the fileName
     *
     * @param fileName The fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the checksum
     *
     * @return The checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the checksum
     *
     * @param checksum The checksum to set
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Gets the mimeType
     *
     * @return The mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the mimeType
     *
     * @param mimeType The mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Gets the created
     *
     * @return The created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the created
     *
     * @param created The created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Gets the previewLink
     *
     * @return The previewLink
     */
    public String getPreviewLink() {
        return previewLink;
    }

    /**
     * Sets the previewLink
     *
     * @param previewLink The previewLink to set
     */
    public void setPreviewLink(String previewLink) {
        this.previewLink = previewLink;
    }

    /**
     * Gets the thumbnail link
     *
     * @return The thumbnailLink Link
     */
    public String getThumbnailLink() {
        return thumbnailLink;
    }

    /**
     * Sets the thumbnailLink
     *
     * @param thumbnailLink The thumbnailLink Link to set
     */
    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

}
