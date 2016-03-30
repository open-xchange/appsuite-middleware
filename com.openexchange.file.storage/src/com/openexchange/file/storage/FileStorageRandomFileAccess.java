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

package com.openexchange.file.storage;

import java.io.InputStream;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link FileStorageRandomFileAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface FileStorageRandomFileAccess extends FileStorageIgnorableVersionFileAccess {

    /**
     * Loads (part of) a document's content.
     *
     * @param folderId The folder identifier
     * @param id The ID of the document
     * @param version The version of the document. Pass {@link FileStorageFileAccess#CURRENT_VERSION} for the current version.
     * @param offset The start offset in bytes to read from the document, or <code>0</code> to start from the beginning
     * @param length The number of bytes to read from the document, or <code>-1</code> to read the stream until the end
     * @return An input stream for the content
     * @throws OXException If operation fails
     */
    InputStream getDocument(String folderId, String id, String version, long offset, long length) throws OXException;

    /**
     * Save file metadata and content. Since the actual version is modified, the version number is not increased.
     *
     * @param document The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @param modifiedColumns The fields to save. All other fields will be ignored
     * @param offset The start offset in bytes where to append the data to the document, must be equal to the actual document's length
     * @throws OXException If operation fails
     */
    IDTuple saveDocument(File document, InputStream data, long sequenceNumber, List<File.Field> modifiedColumns, long offset) throws OXException;

}
