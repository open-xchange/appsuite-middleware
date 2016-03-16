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

package com.openexchange.filestore;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link QuotaFileStorage} - A {@link FileStorage file storage} that is quota aware.
 */
public interface QuotaFileStorage extends FileStorage {

    /**
     * Gets the URI that fully qualifies this file storage.
     *
     * @return The URI
     */
    URI getUri();

    /**
     * Gets the total available quota
     *
     * @return The total quota
     */
    long getQuota();

    /**
     * Gets the currently used quota
     *
     * @return The currently used quota
     * @throws OXException
     */
    long getUsage() throws OXException;

    /**
     * Recalculates the used quota
     *
     * @throws OXException If calculation fails
     */
    void recalculateUsage() throws OXException;

    /**
     * Recalculates the used quota ignoring specified files.
     *
     * @param filesToIgnore The files to ignore
     * @throws OXException If calculation fails
     */
    void recalculateUsage(Set<String> filesToIgnore) throws OXException;

    /**
     * Saves a new file
     *
     * @param file The file to save
     * @param sizeHint The appr. file size
     * @return The identifier of the newly saved file
     * @throws OXException If save operation fails
     */
    String saveNewFile(InputStream file, long sizeHint) throws OXException;

    /**
     * Appends specified stream to the supplied file.
     *
     * @param file The stream to append to the file
     * @param name The existing file's path in associated file storage
     * @param offset The offset in bytes where to append the data, must be equal to the file's current length
     * @param sizeHint A size hint about the expected stream length in bytes, or <code>-1</code> if unknown
     * @return The updated length of the file
     * @throws OXException If appending file fails
     */
    long appendToFile(InputStream file, String name, long offset, long sizeHint) throws OXException;

}
