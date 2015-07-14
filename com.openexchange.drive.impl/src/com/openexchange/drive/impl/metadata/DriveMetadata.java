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

package com.openexchange.drive.impl.metadata;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jonelo.jacksum.algorithm.MD;
import org.json.JSONObject;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link DriveMetadata}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveMetadata extends DefaultFile {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveMetadata.class);

    private final SyncSession session;
    private final FileStorageFolder folder;

    private byte[] document;
    private Long contentsSequenceNumber;

    /**
     * Initializes a new {@link DriveMetadata}.
     *
     * @param session The sync session
     * @param folder The folder to create the metadata for.
     * @throws OXException
     */
    public DriveMetadata(SyncSession session, FileStorageFolder folder) throws OXException {
        this(session, folder, null);
    }

    /**
     * Initializes a new {@link DriveMetadata}.
     *
     * @param session The sync session
     * @param folder The folder to create the metadata for
     * @param contentsSequenceNumber The sequence number for the folder's contents, or <code>null</code> if not available
     * @throws OXException
     */
    public DriveMetadata(SyncSession session, FileStorageFolder folder, Long contentsSequenceNumber) throws OXException {
        super();
        this.session = session;
        this.folder = folder;
        this.contentsSequenceNumber = contentsSequenceNumber;
    }

    /**
     * Gets the contents of the drive metadata file.
     *
     * @param offset The offset to get the data from
     * @param length The length of the data to get
     * @return The document data
     * @throws OXException
     */
    public InputStream getDocument(long offset, long length) throws OXException {
        if (0 < offset || 0 < length) {
            return Streams.newByteArrayInputStream(getDocumentData(), (int) offset, (int) length);
        }
        return Streams.newByteArrayInputStream(getDocumentData());
    }

    @Override
    public String getFileMD5Sum() {
        try {
            MD md = session.newMD5();
            md.update(getDocumentData());
            return md.getFormattedValue();
        } catch (OXException e) {
            LOG.error("Error determining md5 sum for metadata file of folder {}: {}", getFolderId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets the contents of the drive metadata file.
     *
     * @return The document data
     * @throws OXException
     */
    public InputStream getDocument() throws OXException {
        return getDocument(0, -1);
    }

    @Override
    public String getFileMIMEType() {
        return "application/json";
    }

    @Override
    public String getFileName() {
        return DriveConstants.METADATA_FILENAME;
    }

    @Override
    public String getFolderId() {
        return folder.getId();
    }

    @Override
    public String getId() {
        return getFolderId() + '/' + getFileName();
    }

    @Override
    public String getVersion() {
        return String.valueOf(session.getServerSession().getUserId());
    }

    @Override
    public Date getLastModified() {
        return folder.getLastModifiedDate();
    }

    @Override
    public long getFileSize() {
        try {
            return getDocumentData().length;
        } catch (OXException e) {
            LOG.error("", e);
        }
        return -1;
    }

    @Override
    public long getSequenceNumber() {
        long sequenceNumber = null != folder.getLastModifiedDate() ? folder.getLastModifiedDate().getTime() : 0;
        try {
            sequenceNumber = sequenceNumber + getContentsSequenceNumber();
        } catch (OXException e) {
            LOG.error("Error determining sequence number for metadata file of folder {}: {}", getFolderId(), e.getMessage(), e);
        }
        return sequenceNumber;
    }

    @Override
    public String toString() {
        try {
            return new String(getDocumentData(), Charsets.UTF_8);
        } catch (OXException e) {
            LOG.error("", e);
            return super.toString();
        }
    }

    private byte[] getDocumentData() throws OXException {
        if (null == document) {
            JSONObject jsonMetadata = new JsonDirectoryMetadata(session, folder).build();
            //new JSONInputStream(buildMetadata(), Charsets.UTF_8_NAME)
            document = jsonMetadata.toString().getBytes(Charsets.UTF_8);
        }
        return document;
    }

    private long getContentsSequenceNumber() throws OXException {
        if (null == contentsSequenceNumber) {
            /*
             * try and get sequence number for folder contents directly
             */
            if (session.getStorage().supports(new FolderID(getFolderId()), FileStorageCapability.SEQUENCE_NUMBERS)) {
                IDBasedFileAccess fileAccess = session.getStorage().getFileAccess();
                Map<String, Long> sequenceNumbers = fileAccess.getSequenceNumbers(Collections.singletonList(getFolderId()));
                Long sequenceNumber = sequenceNumbers.get(getFolderId());
                contentsSequenceNumber = null != sequenceNumber ? sequenceNumber : Long.valueOf(0);
            }
            if (null == contentsSequenceNumber) {
                /*
                 * fallback to manual sequence number calculation
                 */
                long sequenceNumber = 0;
                List<File> files = session.getStorage().getFilesInFolder(getFolderId(), true, null, Collections.singletonList(Field.SEQUENCE_NUMBER));
                for (File file : files) {
                    if (false == DriveMetadata.class.isInstance(file) && file.getSequenceNumber() > sequenceNumber) {
                        sequenceNumber = file.getSequenceNumber();
                    }
                }
                contentsSequenceNumber = Long.valueOf(sequenceNumber);
            }
        }
        return contentsSequenceNumber.longValue();
    }

}
