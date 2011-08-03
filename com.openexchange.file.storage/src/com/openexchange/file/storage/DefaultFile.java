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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.util.Map;
import java.util.Set;

/**
 * {@link DefaultFile}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultFile extends AbstractFile {

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

    private Map<String, String> properties;

    private String title;

    private String url;

    private int version;

    private String versionComment;

    private boolean isCurrentVersion;

    private static final String DEFAULT_TYPE = "application/octet-stream";

    /**
     * Initializes a new {@link DefaultFile}.
     */
    public DefaultFile() {
        super();
        fileMIMEType = DEFAULT_TYPE;
        properties = new HashMap<String, String>();
    }

    public DefaultFile(final File file) {
        copyFrom(file);
    }

    public String getCategories() {
        return categories;
    }

    public int getColorLabel() {
        return colorLabel;
    }

    public String getContent() {
        return content;
    }

    public Date getCreated() {
        return created;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public String getDescription() {
        return description;
    }

    public String getFileMD5Sum() {
        return fileMD5Sum;
    }

    public String getFileMIMEType() {
        return fileMIMEType;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFolderId() {
        return folderId;
    }

    public String getId() {
        return id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public Date getLockedUntil() {
        return lockedUntil;
    }

    public int getModifiedBy() {
        return modifiedBy;
    }

    public int getNumberOfVersions() {
        return numberOfVersions;
    }

    public String getProperty(final String key) {
        return properties.get(key);
    }

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

    public long getSequenceNumber() {
        if (lastModified == null) {
            return 0;
        }
        return lastModified.getTime();
    }

    public String getTitle() {
        return title;
    }

    public String getURL() {
        return url;
    }

    public int getVersion() {
        return version;
    }

    public String getVersionComment() {
        return versionComment;
    }

    public boolean isCurrentVersion() {
        return isCurrentVersion;
    }

    public void setCategories(final String categories) {
        this.categories = categories;
    }

    public void setColorLabel(final int color) {
        colorLabel = color;
    }

    public void setCreated(final Date creationDate) {
        created = creationDate;
    }

    public void setCreatedBy(final int creator) {
        createdBy = creator;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setFileMD5Sum(final String sum) {
        fileMD5Sum = sum;
    }

    public void setFileMIMEType(final String type) {
        fileMIMEType = type;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(final long length) {
        fileSize = length;
    }

    public void setFolderId(final String folderId) {
        this.folderId = folderId;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setIsCurrentVersion(final boolean bool) {
        isCurrentVersion = bool;
    }

    public void setLastModified(final Date now) {
        lastModified = now;
    }

    public void setLockedUntil(final Date lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public void setModifiedBy(final int lastEditor) {
        modifiedBy = lastEditor;
    }

    public void setNumberOfVersions(final int numberOfVersions) {
        this.numberOfVersions = numberOfVersions;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setURL(final String url) {
        this.url = url;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public void setVersionComment(final String string) {
        versionComment = string;
    }

}
