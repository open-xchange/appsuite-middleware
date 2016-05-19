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

package com.openexchange.file.storage.composition.internal.idmangling;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;

/**
 * {@link IDManglingFile}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IDManglingFile implements File {

    private final File file;
    private final String id;
    private final String folder;

    /**
     * Initializes a new {@link IDManglingFile} instance delegating all regular calls to the supplied file, but returning the unique ID
     * representations of the file's own object and the parent folder ID properties based on the underlying service- and account IDs.
     *
     * @param file The file delegate
     * @param service The service identifier
     * @param account The account identifier
     */
    IDManglingFile(final File file, final String service, final String account) {
        id = new FileID(service, account, file.getFolderId(), file.getId()).toUniqueID();
        folder = new FolderID(service, account, file.getFolderId()).toUniqueID();
        this.file = file;
    }

    @Override
    public boolean isAccurateSize() {
        return file.isAccurateSize();
    }

    @Override
    public void setAccurateSize(boolean accurateSize) {
        file.setAccurateSize(accurateSize);
    }

    @Override
    public boolean matches(final String pattern, final Field... fields) {
        return file.matches(pattern, fields);
    }

    @Override
    public void copyFrom(final File other) {
        file.copyFrom(other);
    }

    @Override
    public void copyInto(final File other) {
        file.copyInto(other);
    }

    @Override
    public void copyFrom(final File other, final Field... fields) {
        file.copyFrom(other, fields);
    }

    @Override
    public void copyInto(final File other, final Field... fields) {
        file.copyInto(other, fields);
    }

    @Override
    public Set<Field> differences(final File other) {
        return file.differences(other);
    }

    @Override
    public File dup() {
        return file.dup();
    }

    @Override
    public boolean equals(final File other, final Field criterium, final Field... criteria) {
        return file.equals(other, criterium, criteria);
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
    public Date getCreated() {
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
    public String getFolderId() {
        return folder;
    }

    @Override
    public String getId() {
        return id;
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
    public String getVersion() {
        return file.getVersion();
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
    public Map<String, Object> getMeta() {
        return file.getMeta();
    }

    @Override
    public List<FileStorageObjectPermission> getObjectPermissions() {
        return file.getObjectPermissions();
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
    public void setCreated(final Date creationDate) {
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
    public void setFolderId(final String folderId) {
        throw new IllegalStateException("IDs are only read only with this class");
    }

    @Override
    public void setId(final String id) {
        throw new IllegalStateException("IDs are only read only with this class");
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
    public void setVersion(final String version) {
        file.setVersion(version);
    }

    @Override
    public void setVersionComment(final String string) {
        file.setVersionComment(string);
    }

    @Override
    public void setMeta(Map<String, Object> properties) {
        file.setMeta(properties);
    }

    @Override
    public void setObjectPermissions(List<FileStorageObjectPermission> objectPermissions) {
        file.setObjectPermissions(objectPermissions);
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
    public String toString() {
        return "IDManglingFile [id=" + id + ", delegateId=" + file.getId() + ", name=" + file.getFileName() + "]";
    }

}
