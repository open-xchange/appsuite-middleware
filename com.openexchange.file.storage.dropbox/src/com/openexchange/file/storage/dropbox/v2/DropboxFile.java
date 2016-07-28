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

package com.openexchange.file.storage.dropbox.v2;

import static com.openexchange.file.storage.dropbox.Utils.normalizeFolderId;
import java.util.Date;
import com.dropbox.core.v2.files.FileMetadata;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.mime.MimeTypeMap;

/**
 * {@link DropboxFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxFile extends DefaultFile {

    private final long sequenceNumber;

    /**
     * Initialises a new {@link DropboxFile}.
     * 
     * @param metadata The {@link FileMetadata} of the Dropbox file
     * @param userId The identifier of the user to use as created/modified-by information
     */
    public DropboxFile(FileMetadata metadata, int userid) {
        super();
        setId(metadata.getName());
        setFolderId(extractFolderId(metadata.getPathDisplay()));
        setCreatedBy(userid);
        setModifiedBy(userid);
        Date clientModified = metadata.getClientModified();
        Date serverModified = metadata.getServerModified();
        setCreated(null == clientModified ? serverModified : clientModified);
        setLastModified(null == clientModified ? serverModified : clientModified);
        sequenceNumber = null != serverModified ? serverModified.getTime() : 0;
        setVersion(metadata.getRev());
        setIsCurrentVersion(true);
        setNumberOfVersions(1);
        setFileSize(metadata.getSize());
        setFileMIMEType(DropboxServices.getService(MimeTypeMap.class).getContentType(metadata.getName()));
        setFileName(metadata.getName());
        setTitle(metadata.getName());
    }

    /**
     * Gets the file's folder- and object-identifier inside an {@link IDTuple} structure.
     *
     * @return The ID tuple
     */
    public IDTuple getIDTuple() {
        return new IDTuple(getFolderId(), getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.DefaultFile#getSequenceNumber()
     */
    @Override
    public long getSequenceNumber() {
        return 0 != this.sequenceNumber ? sequenceNumber : super.getSequenceNumber();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.AbstractFile#toString()
     */
    @Override
    public String toString() {
        String folder = normalizeFolderId(getFolderId());
        return null == folder ? '/' + getId() : folder + '/' + getId();
    }

    /**
     * Extracts the folder from the specified full path
     * 
     * @param path The full path to extract the parent folder
     * @return The extracted parent folder
     */
    private String extractFolderId(String path) {
        int lastIndex = path.lastIndexOf('/');
        return path.substring(0, lastIndex);
    }
}
