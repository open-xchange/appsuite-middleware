
package com.openexchange.file.storage.infostore;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.file.storage.AbstractFile;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;

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

/**
 * {@link InfostoreFile}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFile extends AbstractFile implements UserizedFile {

    private final DocumentMetadata document;

    /**
     * Initializes a new {@link InfostoreFile}.
     *
     * @param documentMetadata The underlying document metadata.
     */
    public InfostoreFile(final DocumentMetadata documentMetadata) {
        this.document = documentMetadata;
    }

    @Override
    public String getCategories() {
        return document.getCategories();
    }

    @Override
    public int getColorLabel() {
        return document.getColorLabel();
    }

    @Override
    public String getContent() {
        return document.getContent();
    }

    @Override
    public Date getCreated() {
        return document.getCreationDate();
    }

    @Override
    public int getCreatedBy() {
        return document.getCreatedBy();
    }

    @Override
    public String getDescription() {
        return document.getDescription();
    }

    @Override
    public String getFileMD5Sum() {
        return document.getFileMD5Sum();
    }

    @Override
    public String getFileMIMEType() {
        return document.getFileMIMEType();
    }

    @Override
    public String getFileName() {
        return document.getFileName();
    }

    @Override
    public long getFileSize() {
        return document.getFileSize();
    }

    @Override
    public String getFolderId() {
        return Long.toString(document.getFolderId());
    }

    @Override
    public String getId() {
        return Integer.toString(document.getId());
    }

    @Override
    public Date getLastModified() {
        return document.getLastModified();
    }

    @Override
    public Date getLockedUntil() {
        return document.getLockedUntil();
    }

    @Override
    public int getModifiedBy() {
        return document.getModifiedBy();
    }

    @Override
    public int getNumberOfVersions() {
        return document.getNumberOfVersions();
    }

    @Override
    public String getProperty(final String key) {
        return document.getProperty(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return document.getPropertyNames();
    }

    @Override
    public long getSequenceNumber() {
        return document.getSequenceNumber();
    }

    @Override
    public String getTitle() {
        return document.getTitle();
    }

    @Override
    public String getURL() {
        return document.getURL();
    }

    @Override
    public String getVersion() {
        return Integer.toString(document.getVersion());
    }

    @Override
    public String getVersionComment() {
        return document.getVersionComment();
    }

    @Override
    public boolean isCurrentVersion() {
        return document.isCurrentVersion();
    }

    @Override
    public void setCategories(final String categories) {
        document.setCategories(categories);
    }

    @Override
    public void setColorLabel(final int color) {
        document.setColorLabel(color);
    }

    @Override
    public void setCreatedBy(final int cretor) {
        document.setCreatedBy(cretor);
    }

    @Override
    public void setCreated(final Date creationDate) {
        document.setCreationDate(creationDate);
    }

    @Override
    public void setDescription(final String description) {
        document.setDescription(description);
    }

    @Override
    public void setFileMD5Sum(final String sum) {
        document.setFileMD5Sum(sum);
    }

    @Override
    public void setFileMIMEType(final String type) {
        document.setFileMIMEType(type);
    }

    @Override
    public void setFileName(final String fileName) {
        document.setFileName(fileName);
    }

    @Override
    public void setFileSize(final long length) {
        document.setFileSize(length);
    }

    @Override
    public void setFolderId(final String folderId) {
        if (folderId != null) {
            document.setFolderId(Long.parseLong(folderId));
        }
    }

    @Override
    public void setId(final String id) {
        if (id == FileStorageFileAccess.NEW) {
            document.setId(InfostoreFacade.NEW);
        } else {
            if (id.contains("/")) {
                document.setId(Integer.parseInt(id.substring(id.lastIndexOf("/") + 1, id.length())));
            } else {
                document.setId(Integer.parseInt(id));
            }
        }
    }

    @Override
    public void setIsCurrentVersion(final boolean bool) {
        document.setIsCurrentVersion(bool);
    }

    @Override
    public void setLastModified(final Date now) {
        document.setLastModified(now);
    }

    @Override
    public void setLockedUntil(final Date lockedUntil) {
        document.setLockedUntil(lockedUntil);
    }

    @Override
    public void setModifiedBy(final int lastEditor) {
        document.setModifiedBy(lastEditor);
    }

    @Override
    public void setNumberOfVersions(final int numberOfVersions) {
        document.setNumberOfVersions(numberOfVersions);
    }

    @Override
    public void setTitle(final String title) {
        document.setTitle(title);
    }

    @Override
    public void setURL(final String url) {
        document.setURL(url);
    }

    @Override
    public void setVersion(final String version) {
        document.setVersion(Integer.parseInt(version));
    }

    @Override
    public void setVersionComment(final String string) {
        document.setVersionComment(string);
    }

    @Override
    public Map<String, Object> getMeta() {
        return document.getMeta();
    }

    @Override
    public void setMeta(final Map<String, Object> properties) {
        document.setMeta(properties);
    }

    @Override
    public List<FileStorageObjectPermission> getObjectPermissions() {
        return PermissionHelper.getFileStorageObjectPermissions(document.getObjectPermissions());
    }

    @Override
    public void setObjectPermissions(List<FileStorageObjectPermission> objectPermissions) {
        document.setObjectPermissions(PermissionHelper.getObjectPermissions(objectPermissions));
    }

    @Override
    public boolean isShareable() {
        return document.isShareable();
    }

    @Override
    public void setShareable(boolean shareable) {
        document.setShareable(shareable);
    }

    @Override
    public String getOriginalId() {
        return Integer.toString(document.getOriginalId());
    }

    @Override
    public void setOriginalId(String id) {
        document.setOriginalId(Integer.parseInt(id));
    }

    @Override
    public String getOriginalFolderId() {
        return Long.toString(document.getOriginalFolderId());
    }

    @Override
    public void setOriginalFolderId(String id) {
        document.setOriginalFolderId(Long.parseLong(id));
    }

}
