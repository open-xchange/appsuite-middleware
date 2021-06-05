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

/**
 * {@link FolderStats}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class FolderStats {

	private long numFiles;
	private long numFolders;
	private long totalSize;

	/**
	 * Initializes a new {@link FolderStats}.
	 */
	public FolderStats() {
	    super();
	}

	/**
	 * Adds the statistics of the supplied folder statistics to this instance.
	 *
	 * @param stats The statistics
	 */
	public void add(FolderStats stats) {
	    if (null != stats) {
	        numFiles += stats.numFiles;
	        numFolders += stats.numFolders;
	        totalSize += stats.totalSize;
	    }
	}

	/**
	 * Gets the total number of files.
	 *
	 * @return The number of files
	 */
	public long getNumFiles() {
		return numFiles;
	}

    /**
     * Gets the total number of folders.
     *
     * @return The number of folders
     */
    public long getNumFolders() {
        return numFolders;
    }

    /**
     * Gets the total size of the contents in bytes.
     *
     * @return The total size
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * Increments the number of files by the supplied value.
     *
     * @param value The value to add
     */
    public void incrementNumFiles(long value) {
        numFiles += value;
    }

    /**
     * Increments the number of folders by the supplied value.
     *
     * @param value The value to add
     */
    public void incrementNumFolders(long value) {
        numFolders += value;
    }

    /**
     * Increments the total size by the supplied value.
     *
     * @param value The value to add
     */
    public void incrementTotalSize(long value) {
        totalSize += value;
    }

    @Override
    public String toString() {
        return "FolderStats [numFiles=" + numFiles + ", numFolders=" + numFolders + ", totalSize=" + totalSize + "]";
    }

}
