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
