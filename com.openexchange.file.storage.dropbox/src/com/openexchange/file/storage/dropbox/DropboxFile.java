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

package com.openexchange.file.storage.dropbox;

import static com.openexchange.file.storage.dropbox.Utils.normalizeFolderId;
import java.util.Date;
import com.dropbox.client2.DropboxAPI.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.java.Strings;
import com.openexchange.mime.MimeTypeMap;

/**
 * {@link DropboxFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxFile extends DefaultFile {

    private final long sequenceNumber;

    /**
     * Initializes a new {@link DropboxFile}.
     *
     * @param entry The dropbox entry representing the file
     * @param userID The identifier of the user to use as created-/modified-by information
     * @throws OXException
     */
    public DropboxFile(Entry entry, int userID) throws OXException {
        super();
        if (entry.isDir) {
            throw FileStorageExceptionCodes.NOT_A_FILE.create(DropboxConstants.ID, entry.path);
        }
        String parentPath = entry.parentPath();
        setId(entry.fileName());
        setFolderId("/".equals(parentPath) ? FileStorageFolder.ROOT_FULLNAME : normalizeFolderId(parentPath));
        setCreatedBy(userID);
        setModifiedBy(userID);
        Date modified = Utils.parseDate(entry.modified);
        Date clientModified = Utils.parseDate(entry.clientMtime);
        setCreated(null == clientModified ? modified : clientModified);
        setLastModified(null == clientModified ? modified : clientModified);
        sequenceNumber = null != modified ? modified.getTime() : 0;
        setVersion(entry.rev);
        setIsCurrentVersion(true);
        setNumberOfVersions(1);
        setFileSize(entry.bytes);
        setFileMIMEType(Strings.isEmpty(entry.mimeType) ?
            DropboxServices.getService(MimeTypeMap.class).getContentType(entry.fileName()) : entry.mimeType);
        setFileName(entry.fileName());
        setTitle(entry.fileName());
    }

    /**
     * Gets the file's folder- and object-identifier inside an {@link IDTuple} structure.
     *
     * @return The ID tuple
     */
    public IDTuple getIDTuple() {
        return new IDTuple(getFolderId(), getId());
    }

    @Override
    public long getSequenceNumber() {
        return 0 != this.sequenceNumber ? sequenceNumber : super.getSequenceNumber();
    }

    @Override
    public String toString() {
        String folder = normalizeFolderId(getFolderId());
        return null == folder ? '/' + getId() : folder + '/' + getId();
    }

}
