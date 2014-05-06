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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.checksum;

import com.openexchange.file.storage.composition.FolderID;


/**
 * {@link DirectoryChecksum}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryChecksum extends StoredChecksum {

    private FolderID folderID;
    private int userID;
    private String eTag;

    /**
     * Initializes a new {@link DirectoryChecksum}.
     */
    public DirectoryChecksum() {
        super();
    }

    /**
     * Initializes a new {@link DirectoryChecksum}.
     *
     * @param userID The user ID
     * @param folderID The folder ID
     * @param sequenceNumber The sequence number
     * @param checksum The checksum
     */
    public DirectoryChecksum(int userID, FolderID folderID, long sequenceNumber, String checksum) {
        this(userID, folderID, sequenceNumber, null, checksum);
    }

    /**
     * Initializes a new {@link DirectoryChecksum}.
     *
     * @param userID The user ID
     * @param folderID The folder ID
     * @param eTag The E-Tag
     * @param checksum The checksum
     */
    public DirectoryChecksum(int userID, FolderID folderID, String eTag, String checksum) {
        this(userID, folderID, 0, eTag, checksum);
    }

    /**
     * Initializes a new {@link DirectoryChecksum}.
     *
     * @param userID The user ID
     * @param folderID The folder ID
     * @param sequenceNumber The sequence number
     * @param eTag The E-Tag
     * @param checksum The checksum
     */
    private DirectoryChecksum(int userID, FolderID folderID, long sequenceNumber, String eTag, String checksum) {
        super();
        this.userID = userID;
        this.folderID = folderID;
        this.eTag = eTag;
        this.sequenceNumber = sequenceNumber;
        this.checksum = checksum;
    }

    /**
     * Gets the folderID
     *
     * @return The folderID
     */
    public FolderID getFolderID() {
        return folderID;
    }

    /**
     * Sets the folderID
     *
     * @param folderID The folderID to set
     */
    public void setFolderID(FolderID folderID) {
        this.folderID = folderID;
    }

    /**
     * Gets the userID
     *
     * @return The userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Sets the userID
     *
     * @param userID The userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * Gets the E-Tag
     *
     * @return The E-Tag
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets the E-Tag
     *
     * @param eTag The E-Tag to set
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public String toString() {
        return folderID + " | " + checksum + " | " + (null != eTag ? eTag : String.valueOf(sequenceNumber));
    }

}
