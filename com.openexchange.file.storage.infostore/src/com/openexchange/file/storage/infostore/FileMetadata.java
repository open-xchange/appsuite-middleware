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

package com.openexchange.file.storage.infostore;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;


/**
 * {@link FileMetadata}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileMetadata implements DocumentMetadata {

    private static final long serialVersionUID = 2097235173990058583L;

    private final File file;
    private String fileSpool;

    public FileMetadata(final File document) throws OXException {
        validate(document);
        this.file = document;
    }

    @Override
    public String getCategories() {
        return file.getCategories();
    }

    @Override
    public int getColorLabel() {
        return file.getColorLabel();
    }

    @Override
    public String getContent() {
        return file.getContent();
    }

    @Override
    public Date getCreationDate() {
        return file.getCreated();
    }

    @Override
    public int getCreatedBy() {
        return file.getCreatedBy();
    }

    @Override
    public String getDescription() {
        return file.getDescription();
    }

    @Override
    public String getFileMD5Sum() {
        return file.getFileMD5Sum();
    }

    @Override
    public String getFileMIMEType() {
        return file.getFileMIMEType();
    }

    @Override
    public String getFileName() {
        return file.getFileName();
    }

    @Override
    public long getFileSize() {
        return file.getFileSize();
    }

    @Override
    public long getFolderId() {
        if(file.getFolderId() == null) {
            return -1;
        }
        return Long.valueOf(file.getFolderId());
    }

    @Override
    public int getId() {
        if(file.getId() == FileStorageFileAccess.NEW) {
            return InfostoreFacade.NEW;
        }
        return Integer.valueOf(file.getId());
    }

    @Override
    public Date getLastModified() {
        return file.getLastModified();
    }

    @Override
    public Date getLockedUntil() {
        return file.getLockedUntil();
    }

    @Override
    public int getModifiedBy() {
        return file.getModifiedBy();
    }

    @Override
    public int getNumberOfVersions() {
        return file.getNumberOfVersions();
    }

    @Override
    public String getProperty(final String key) {
        return file.getProperty(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return file.getPropertyNames();
    }

    @Override
    public long getSequenceNumber() {
        return file.getSequenceNumber();
    }

    @Override
    public String getTitle() {
        return file.getTitle();
    }

    @Override
    public String getURL() {
        return file.getURL();
    }

    @Override
    public int getVersion() {
        final String version = file.getVersion();
        return com.openexchange.java.Strings.isEmpty(version) ? -1 : Integer.parseInt(version);
    }

    @Override
    public String getVersionComment() {
        return file.getVersionComment();
    }

    @Override
    public boolean isCurrentVersion() {
        return file.isCurrentVersion();
    }

    @Override
    public void setCategories(final String categories) {
        file.setCategories(categories);
    }

    @Override
    public void setColorLabel(final int color) {
        file.setColorLabel(color);
    }

    @Override
    public void setCreationDate(final Date creationDate) {
        file.setCreated(creationDate);
    }

    @Override
    public void setCreatedBy(final int cretor) {
        file.setCreatedBy(cretor);
    }

    @Override
    public void setDescription(final String description) {
        file.setDescription(description);
    }

    @Override
    public void setFileMD5Sum(final String sum) {
        file.setFileMD5Sum(sum);
    }

    @Override
    public void setFileMIMEType(final String type) {
        file.setFileMIMEType(type);
    }

    @Override
    public void setFileName(final String fileName) {
        file.setFileName(fileName);
    }

    @Override
    public void setFileSize(final long length) {
        file.setFileSize(length);
    }

    @Override
    public void setFolderId(final long folderId) {
        file.setFolderId(Long.toString(folderId));
    }

    @Override
    public void setId(final int id) {
        file.setId(String.valueOf(id));
    }

    @Override
    public void setIsCurrentVersion(final boolean bool) {
        file.setIsCurrentVersion(bool);
    }

    @Override
    public void setLastModified(final Date now) {
        file.setLastModified(now);
    }

    @Override
    public void setLockedUntil(final Date lockedUntil) {
        file.setLockedUntil(lockedUntil);
    }

    @Override
    public void setModifiedBy(final int lastEditor) {
        file.setModifiedBy(lastEditor);
    }

    @Override
    public void setNumberOfVersions(final int numberOfVersions) {
        file.setNumberOfVersions(numberOfVersions);
    }

    @Override
    public void setTitle(final String title) {
        file.setTitle(title);
    }

    @Override
    public void setURL(final String url) {
        file.setURL(url);
    }

    @Override
    public void setVersion(final int version) {
        file.setVersion(version < 0 ? FileStorageFileAccess.CURRENT_VERSION : Integer.toString(version));
    }

    @Override
    public void setVersionComment(final String string) {
        file.setVersionComment(string);
    }

    @Override
    public String getFilestoreLocation() {
        return fileSpool;
    }

    @Override
    public void setFilestoreLocation(final String string) {
        this.fileSpool = string;
    }

    @Override
    public Map<String, Object> getMeta() {
        return file.getMeta();
    }

    @Override
    public void setMeta(final Map<String, Object> properties) {
        file.setMeta(properties);
    }

    @Override
    public List<ObjectPermission> getObjectPermissions() {
        return PermissionHelper.getObjectPermissions(file.getObjectPermissions());
    }

    @Override
    public void setObjectPermissions(List<ObjectPermission> objectPermissions) {
        file.setObjectPermissions(PermissionHelper.getFileStorageObjectPermissions(objectPermissions));
    }

    @Override
    public boolean isShareable() {
        return file.isShareable();
    }

    @Override
    public void setShareable(boolean shareable) {
        file.setShareable(shareable);
    }

    /**
     * Checks the supplied file metadata for infostore compatibility.
     *
     * @param file The file to check
     * @throws OXException If validation fails
     */
    private static void validate(final File file) throws OXException {
        if (null != file) {
            /*
             * check for numerical identifiers if set
             */
            String id = file.getId();
            if (FileStorageFileAccess.NEW != id) {
                if (id.contains("/")) {
                    id = id.substring(id.lastIndexOf("/") + 1, id.length());
                    file.setId(id);
                }
                try {
                    Integer.valueOf(id);
                } catch (final NumberFormatException e) {
                    throw FileStorageExceptionCodes.INVALID_FILE_IDENTIFIER.create(e, id);
                }
            }
            final String folderID = file.getFolderId();
            if (null != folderID) {
                try {
                    Integer.valueOf(folderID);
                } catch (final NumberFormatException e) {
                    throw FileStorageExceptionCodes.INVALID_FOLDER_IDENTIFIER.create(e, folderID);
                }
            }
        }
    }

    /**
     * Gets the InfoStore {@link DocumentMetadata} from given file.
     *
     * @param file The file
     * @return The appropriate {@link DocumentMetadata} instance
     */
    public static DocumentMetadata getMetadata(final File file) {
        final DocumentMetadata metaData = new DocumentMetadata() {

            private static final long serialVersionUID = -1476628439761201503L;

            @Override
            public void setVersionComment(final String string) {
                // nothing to do
            }

            @Override
            public void setVersion(final int version) {
                // nothing to do
            }

            @Override
            public void setURL(final String url) {
                // nothing to do
            }

            @Override
            public void setTitle(final String title) {
                // nothing to do
            }

            @Override
            public void setNumberOfVersions(final int numberOfVersions) {
                // nothing to do
            }

            @Override
            public void setModifiedBy(final int lastEditor) {
                // nothing to do
            }

            @Override
            public void setLockedUntil(final Date lockedUntil) {
                // nothing to do
            }

            @Override
            public void setLastModified(final Date now) {
                // nothing to do
            }

            @Override
            public void setIsCurrentVersion(final boolean bool) {
                // nothing to do
            }

            @Override
            public void setId(final int id) {
                // nothing to do
            }

            @Override
            public void setFolderId(final long folderId) {
                // nothing to do
            }

            @Override
            public void setFilestoreLocation(final String string) {
                // nothing to do
            }

            @Override
            public void setFileSize(final long length) {
                // nothing to do
            }

            @Override
            public void setFileName(final String fileName) {
                // nothing to do
            }

            @Override
            public void setFileMIMEType(final String type) {
                // nothing to do
            }

            @Override
            public void setFileMD5Sum(final String sum) {
                // nothing to do
            }

            @Override
            public void setDescription(final String description) {
                // nothing to do
            }

            @Override
            public void setCreationDate(final Date creationDate) {
                // nothing to do
            }

            @Override
            public void setCreatedBy(final int cretor) {
                // nothing to do
            }

            @Override
            public void setColorLabel(final int color) {
                // nothing to do
            }

            @Override
            public void setCategories(final String categories) {
                // nothing to do
            }

            @Override
            public void setOriginalId(int id) {
                // nothing to do
            }

            @Override
            public void setOriginalFolderId(long id) {
                // nothing to do
            }

            @Override
            public boolean isCurrentVersion() {
                return file.isCurrentVersion();
            }

            @Override
            public String getVersionComment() {
                return file.getVersionComment();
            }

            @Override
            public int getVersion() {
                return Integer.parseInt(file.getVersion());
            }

            @Override
            public String getURL() {
                return file.getURL();
            }

            @Override
            public String getTitle() {
                return file.getTitle();
            }

            @Override
            public long getSequenceNumber() {
                return file.getSequenceNumber();
            }

            @Override
            public Set<String> getPropertyNames() {
                return file.getPropertyNames();
            }

            @Override
            public String getProperty(final String key) {
                return file.getProperty(key);
            }

            @Override
            public int getNumberOfVersions() {
                return file.getNumberOfVersions();
            }

            @Override
            public int getModifiedBy() {
                return file.getModifiedBy();
            }

            @Override
            public Date getLockedUntil() {
                return file.getLockedUntil();
            }

            @Override
            public Date getLastModified() {
                return file.getLastModified();
            }

            @Override
            public int getId() {
                String id = file.getId();
                if (FileStorageFileAccess.NEW == id) {
                    return InfostoreFacade.NEW;
                }
                return Integer.parseInt(new FileID(id).getFileId());
            }

            @Override
            public long getFolderId() {
                String id = file.getFolderId();
                if (FileStorageFileAccess.NEW == id) {
                    return InfostoreFacade.NEW;
                }
                return Long.parseLong(new FolderID(id).getFolderId());
            }

            @Override
            public String getFilestoreLocation() {
                // TODO
                return null;
            }

            @Override
            public long getFileSize() {
                return file.getFileSize();
            }

            @Override
            public String getFileName() {
                return file.getFileName();
            }

            @Override
            public String getFileMIMEType() {
                return file.getFileMIMEType();
            }

            @Override
            public String getFileMD5Sum() {
                return file.getFileMD5Sum();
            }

            @Override
            public String getDescription() {
                return file.getDescription();
            }

            @Override
            public Date getCreationDate() {
                return file.getCreated();
            }

            @Override
            public int getCreatedBy() {
                return file.getCreatedBy();
            }

            @Override
            public String getContent() {
                return file.getContent();
            }

            @Override
            public int getColorLabel() {
                return file.getColorLabel();
            }

            @Override
            public String getCategories() {
                return file.getCategories();
            }

            @Override
            public Map<String, Object> getMeta() {
                return file.getMeta();
            }

            @Override
            public void setMeta(Map<String, Object> properties) {
                // Nothing to do
            }

            @Override
            public List<ObjectPermission> getObjectPermissions() {
                return PermissionHelper.getObjectPermissions(file.getObjectPermissions());
            }

            @Override
            public void setObjectPermissions(List<ObjectPermission> objectPermissions) {
                // Nothing to do
            }

            @Override
            public boolean isShareable() {
                return file.isShareable();
            }

            @Override
            public void setShareable(boolean shareable) {
                file.setShareable(shareable);
            }

            @Override
            public int getOriginalId() {
                if (file instanceof UserizedFile) {
                    return Integer.parseInt(((UserizedFile) file).getOriginalId());
                }

                return getId();
            }

            @Override
            public long getOriginalFolderId() {
                if (file instanceof UserizedFile) {
                    return Long.parseLong(((UserizedFile) file).getOriginalFolderId());
                }

                return getFolderId();
            }

        };
        return metaData;
    }

    @Override
    public int getOriginalId() {
        if (file instanceof UserizedFile) {
            return Integer.parseInt(((UserizedFile) file).getOriginalId());
        }

        return getId();
    }

    @Override
    public void setOriginalId(int id) {
        if (file instanceof UserizedFile) {
            ((UserizedFile) file).setOriginalId(Integer.toString(id));
        }
    }

    @Override
    public long getOriginalFolderId() {
        if (file instanceof UserizedFile) {
            return Long.parseLong(((UserizedFile) file).getOriginalFolderId());
        }

        return getFolderId();
    }

    @Override
    public void setOriginalFolderId(long id) {
        if (file instanceof UserizedFile) {
            ((UserizedFile) file).setOriginalFolderId(Long.toString(id));
        }
    }

}
