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

package com.openexchange.drive.impl.checksum;

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
    private int view;

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
     * @param view The view of the directory, or <code>0</code> for the default view
     */
    public DirectoryChecksum(int userID, FolderID folderID, long sequenceNumber, String checksum, int view) {
        this(userID, folderID, sequenceNumber, null, checksum, view);
    }

    /**
     * Initializes a new {@link DirectoryChecksum}.
     *
     * @param userID The user ID
     * @param folderID The folder ID
     * @param eTag The E-Tag
     * @param checksum The checksum
     * @param view The view of the directory, or <code>0</code> for the default view
     */
    public DirectoryChecksum(int userID, FolderID folderID, String eTag, String checksum, int view) {
        this(userID, folderID, 0, eTag, checksum, view);
    }

    /**
     * Initializes a new {@link DirectoryChecksum}.
     *
     * @param userID The user ID
     * @param folderID The folder ID
     * @param sequenceNumber The sequence number
     * @param eTag The E-Tag
     * @param checksum The checksum
     * @param view The view of the directory, or <code>0</code> for the default view
     */
    private DirectoryChecksum(int userID, FolderID folderID, long sequenceNumber, String eTag, String checksum, int view) {
        super();
        this.userID = userID;
        this.folderID = folderID;
        this.eTag = eTag;
        this.sequenceNumber = sequenceNumber;
        this.checksum = checksum;
        this.view = view;
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

    /**
     * Gets the view
     *
     * @return The view
     */
    public int getView() {
        return view;
    }

    /**
     * Sets the view
     *
     * @param view The view to set
     */
    public void setView(int view) {
        this.view = view;
    }

    @Override
    public String toString() {
        return folderID + " | " + checksum + " | " + (null != eTag ? eTag : String.valueOf(sequenceNumber));
    }

}
