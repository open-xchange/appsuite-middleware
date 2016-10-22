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

package com.openexchange.file.storage.json.actions.files;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.file.storage.DelegatingFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;


/**
 * {@link MetaDataAddingFile} - Possibly adds meta-data to an existing file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MetaDataAddingFile implements DelegatingFile {

    private final File file;
    private Map<String, Object> metaData;

    /**
     * Initializes a new {@link MetaDataAddingFile}.
     */
    public MetaDataAddingFile(File file) {
        super();
        this.file = file;
    }

    @Override
    public File getDelegate() {
        return file;
    }

    @Override
    public String getProperty(String key) {
        return file.getProperty(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return file.getPropertyNames();
    }

    @Override
    public Date getLastModified() {
        return file.getLastModified();
    }

    @Override
    public void setLastModified(Date now) {
        file.setLastModified(now);
    }

    @Override
    public Date getCreated() {
        return file.getCreated();
    }

    @Override
    public void setCreated(Date creationDate) {
        file.setCreated(creationDate);
    }

    @Override
    public int getModifiedBy() {
        return file.getModifiedBy();
    }

    @Override
    public void setModifiedBy(int lastEditor) {
        file.setModifiedBy(lastEditor);
    }

    @Override
    public String getFolderId() {
        return file.getFolderId();
    }

    @Override
    public void setFolderId(String folderId) {
        file.setFolderId(folderId);
    }

    @Override
    public String getTitle() {
        return file.getTitle();
    }

    @Override
    public void setTitle(String title) {
        file.setTitle(title);
    }

    @Override
    public String getVersion() {
        return file.getVersion();
    }

    @Override
    public void setVersion(String version) {
        file.setVersion(version);
    }

    @Override
    public String getContent() {
        return file.getContent();
    }

    @Override
    public long getFileSize() {
        return file.getFileSize();
    }

    @Override
    public void setFileSize(long length) {
        file.setFileSize(length);
    }

    @Override
    public String getFileMIMEType() {
        return file.getFileMIMEType();
    }

    @Override
    public void setFileMIMEType(String type) {
        file.setFileMIMEType(type);
    }

    @Override
    public String getFileName() {
        return file.getFileName();
    }

    @Override
    public void setFileName(String fileName) {
        file.setFileName(fileName);
    }

    @Override
    public String getId() {
        return file.getId();
    }

    @Override
    public void setId(String id) {
        file.setId(id);
    }

    @Override
    public int getCreatedBy() {
        return file.getCreatedBy();
    }

    @Override
    public void setCreatedBy(int cretor) {
        file.setCreatedBy(cretor);
    }

    @Override
    public String getDescription() {
        return file.getDescription();
    }

    @Override
    public void setDescription(String description) {
        file.setDescription(description);
    }

    @Override
    public String getURL() {
        return file.getURL();
    }

    @Override
    public void setURL(String url) {
        file.setURL(url);
    }

    @Override
    public long getSequenceNumber() {
        return file.getSequenceNumber();
    }

    @Override
    public String getCategories() {
        return file.getCategories();
    }

    @Override
    public void setCategories(String categories) {
        file.setCategories(categories);
    }

    @Override
    public Date getLockedUntil() {
        return file.getLockedUntil();
    }

    @Override
    public void setLockedUntil(Date lockedUntil) {
        file.setLockedUntil(lockedUntil);
    }

    @Override
    public String getFileMD5Sum() {
        return file.getFileMD5Sum();
    }

    @Override
    public void setFileMD5Sum(String sum) {
        file.setFileMD5Sum(sum);
    }

    @Override
    public int getColorLabel() {
        return file.getColorLabel();
    }

    @Override
    public void setColorLabel(int color) {
        file.setColorLabel(color);
    }

    @Override
    public boolean isCurrentVersion() {
        return file.isCurrentVersion();
    }

    @Override
    public void setIsCurrentVersion(boolean bool) {
        file.setIsCurrentVersion(bool);
    }

    @Override
    public String getVersionComment() {
        return file.getVersionComment();
    }

    @Override
    public void setVersionComment(String string) {
        file.setVersionComment(string);
    }

    @Override
    public void setNumberOfVersions(int numberOfVersions) {
        file.setNumberOfVersions(numberOfVersions);
    }

    @Override
    public int getNumberOfVersions() {
        return file.getNumberOfVersions();
    }

    @Override
    public Map<String, Object> getMeta() {
        return null == metaData ? file.getMeta() : metaData;
    }

    @Override
    public void setMeta(Map<String, Object> properties) {
        this.metaData = properties;
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
    public List<FileStorageObjectPermission> getObjectPermissions() {
        return file.getObjectPermissions();
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
    public File dup() {
        return file.dup();
    }

    @Override
    public void copyInto(File other) {
        file.copyInto(other);
    }

    @Override
    public void copyFrom(File other) {
        file.copyFrom(other);
    }

    @Override
    public void copyInto(File other, Field... fields) {
        file.copyInto(other, fields);
    }

    @Override
    public void copyFrom(File other, Field... fields) {
        file.copyFrom(other, fields);
    }

    @Override
    public Set<Field> differences(File other) {
        return file.differences(other);
    }

    @Override
    public boolean equals(File other, Field criterium, Field... criteria) {
        return file.equals(other, criterium, criteria);
    }

    @Override
    public boolean matches(String pattern, Field... fields) {
        return file.matches(pattern, fields);
    }

}
