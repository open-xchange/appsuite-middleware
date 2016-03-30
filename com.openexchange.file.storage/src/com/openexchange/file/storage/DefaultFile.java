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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link DefaultFile}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultFile extends AbstractFile {

    private static final String DEFAULT_TYPE = "application/octet-stream";

    private String categories;
    private int colorLabel;
    private String content;
    private Date created;
    private int createdBy;
    private String description;
    private String fileMD5Sum;
    private String fileMIMEType;
    private String fileName;
    private long fileSize;
    private String folderId;
    private String id;
    private Date lastModified;
    private Date lockedUntil;
    private int modifiedBy;
    private int numberOfVersions;
    private final Map<String, String> properties;
    private String title;
    private String url;
    private String version;
    private String versionComment;
    private boolean isCurrentVersion;
    private Map<String, Object> dynamicProperties;
    private List<FileStorageObjectPermission> objectPermissions;
    private boolean shareable;

    /**
     * Initializes a new {@link DefaultFile}.
     */
    public DefaultFile() {
        super();
        fileMIMEType = DEFAULT_TYPE;
        properties = new HashMap<String, String>();
        dynamicProperties = new LinkedHashMap<String, Object>();
    }

    /**
     * Initializes a new {@link DefaultFile} from given file.
     */
    public DefaultFile(final File file) {
        super();
        dynamicProperties = new LinkedHashMap<String, Object>();
        final Set<String> propertyNames = file.getPropertyNames();
        final Map<String, String> properties = new HashMap<String, String>(propertyNames.size());
        for (final String propertyName : propertyNames) {
            properties.put(propertyName, file.getProperty(propertyName));
        }
        this.properties = properties;
        copyFrom(file);
    }

    @Override
    public String getCategories() {
        return categories;
    }

    @Override
    public int getColorLabel() {
        return colorLabel;
    }

    @Override
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of this file.
     *
     * @param content The content
     */
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public int getCreatedBy() {
        return createdBy;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getFileMD5Sum() {
        return fileMD5Sum;
    }

    @Override
    public String getFileMIMEType() {
        return fileMIMEType;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public String getFolderId() {
        return folderId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public Date getLockedUntil() {
        return lockedUntil;
    }

    @Override
    public int getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public int getNumberOfVersions() {
        return numberOfVersions;
    }

    @Override
    public String getProperty(final String key) {
        return properties.get(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    /**
     * Sets specified property. A <code>null</code> value removes the property.
     *
     * @param name The name
     * @param value The value or <code>null</code> for removal
     */
    public void setProperty(final String name, final String value) {
        if (null == value) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

    @Override
    public long getSequenceNumber() {
        if (lastModified == null) {
            return 0;
        }
        return lastModified.getTime();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getVersionComment() {
        return versionComment;
    }

    @Override
    public boolean isCurrentVersion() {
        return isCurrentVersion;
    }
    
    @Override
    public boolean isShareable() {
        return shareable;
    }

    @Override
    public void setCategories(final String categories) {
        this.categories = categories;
    }

    @Override
    public void setColorLabel(final int color) {
        colorLabel = color;
    }

    @Override
    public void setCreated(final Date creationDate) {
        created = creationDate;
    }

    @Override
    public void setCreatedBy(final int creator) {
        createdBy = creator;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setFileMD5Sum(final String sum) {
        fileMD5Sum = sum;
    }

    @Override
    public void setFileMIMEType(final String type) {
        fileMIMEType = type;
    }

    @Override
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void setFileSize(final long length) {
        fileSize = length;
    }

    @Override
    public void setFolderId(final String folderId) {
        this.folderId = folderId;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public void setIsCurrentVersion(final boolean bool) {
        isCurrentVersion = bool;
    }

    @Override
    public void setLastModified(final Date now) {
        lastModified = now;
    }

    @Override
    public void setLockedUntil(final Date lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    @Override
    public void setModifiedBy(final int lastEditor) {
        modifiedBy = lastEditor;
    }

    @Override
    public void setNumberOfVersions(final int numberOfVersions) {
        this.numberOfVersions = numberOfVersions;
    }

    @Override
    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public void setURL(final String url) {
        this.url = url;
    }

    @Override
    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public void setVersionComment(final String string) {
        versionComment = string;
    }

    @Override
    public void setMeta(Map<String, Object> properties) {
        this.dynamicProperties = properties;
    }

    @Override
    public Map<String, Object> getMeta() {
        return dynamicProperties;
    }

    @Override
    public void setObjectPermissions(List<FileStorageObjectPermission> objectPermissions) {
        this.objectPermissions = objectPermissions;
    }

    @Override
    public List<FileStorageObjectPermission> getObjectPermissions() {
        return objectPermissions;
    }
    
    @Override
    public void setShareable(boolean shareable) {
        this.shareable = shareable;
    }

}
