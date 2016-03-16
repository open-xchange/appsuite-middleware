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

package com.openexchange.groupware.infostore;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.container.ObjectPermission;

/**
 * {@link DefaultDocumentMetadata}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultDocumentMetadata implements DocumentMetadata {

    private static final long serialVersionUID = -5864163840354454650L;

    protected Date lastModified;
    protected Date creationDate;
    protected int modifiedBy;
    protected long folderId;
    protected long originalFolderId = -1;
    protected String title;
    protected int version;
    protected String content;
    protected long fileSize;
    protected String fileMIMEType;
    protected String fileName;
    protected int id;
    protected int originalId = -1;
    protected int createdBy;
    protected String description;
    protected String url;
    protected long sequenceNumber;
    protected String categories;
    protected Date lockedUntil;
    protected String fileMD5Sum;
    protected int colorLabel;
    protected boolean currentVersion;
    protected String versionComment;
    protected String filestoreLocation;
    protected int numberOfVersions;
    protected Map<String, Object> meta;
    protected List<ObjectPermission> objectPermissions;
    protected Map<String, String> properties;
    protected boolean shareable;


    @Override
    public String getProperty(String key) {
        return null != properties ? properties.get(key) : null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return null != properties ? properties.keySet() : Collections.<String>emptySet();
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public int getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public long getFolderId() {
        return folderId;
    }

    @Override
    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    @Override
    public long getOriginalFolderId() {
        if (originalFolderId < 0) {
            return getFolderId();
        }

        return originalFolderId;
    }

    @Override
    public void setOriginalFolderId(long id) {
        originalFolderId = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String getFileMIMEType() {
        return fileMIMEType;
    }

    @Override
    public void setFileMIMEType(String fileMIMEType) {
        this.fileMIMEType = fileMIMEType;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getOriginalId() {
        if (originalId < 0) {
            return getId();
        }
        return originalId;
    }

    @Override
    public void setOriginalId(int id) {
        originalId = id;
    }

    @Override
    public int getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public void setURL(String url) {
        this.url = url;
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public String getCategories() {
        return categories;
    }

    @Override
    public void setCategories(String categories) {
        this.categories = categories;
    }

    @Override
    public Date getLockedUntil() {
        return lockedUntil;
    }

    @Override
    public void setLockedUntil(Date lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    @Override
    public String getFileMD5Sum() {
        return fileMD5Sum;
    }

    @Override
    public void setFileMD5Sum(String fileMD5Sum) {
        this.fileMD5Sum = fileMD5Sum;
    }

    @Override
    public int getColorLabel() {
        return colorLabel;
    }

    @Override
    public void setColorLabel(int colorLabel) {
        this.colorLabel = colorLabel;
    }

    @Override
    public boolean isCurrentVersion() {
        return currentVersion;
    }

    @Override
    public void setIsCurrentVersion(boolean currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    public String getVersionComment() {
        return versionComment;
    }

    @Override
    public void setVersionComment(String versionComment) {
        this.versionComment = versionComment;
    }

    @Override
    public String getFilestoreLocation() {
        return filestoreLocation;
    }

    @Override
    public void setFilestoreLocation(String filestoreLocation) {
        this.filestoreLocation = filestoreLocation;
    }

    @Override
    public int getNumberOfVersions() {
        return numberOfVersions;
    }

    @Override
    public void setNumberOfVersions(int numberOfVersions) {
        this.numberOfVersions = numberOfVersions;
    }

    @Override
    public Map<String, Object> getMeta() {
        return meta;
    }

    @Override
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    @Override
    public List<ObjectPermission> getObjectPermissions() {
        return objectPermissions;
    }

    @Override
    public void setObjectPermissions(List<ObjectPermission> objectPermissions) {
        this.objectPermissions = objectPermissions;
    }

    @Override
    public boolean isShareable() {
        return shareable;
    }

    @Override
    public void setShareable(boolean shareable) {
        this.shareable = shareable;
    }

    @Override
    public String toString() {
        return "DefaultDocumentMetadata [id=" + id + ", folderId=" + folderId + ", fileName=" + fileName + "]";
    }

}
