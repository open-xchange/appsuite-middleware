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
