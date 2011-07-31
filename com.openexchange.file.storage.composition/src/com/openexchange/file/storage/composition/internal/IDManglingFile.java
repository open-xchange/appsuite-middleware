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

package com.openexchange.file.storage.composition.internal;

import java.util.Date;
import java.util.Set;
import com.openexchange.file.storage.File;

/**
 * {@link IDManglingFile}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IDManglingFile implements File {

    private final File file;

    private final String id;

    private final String folder;

    public IDManglingFile(File file, String service, String account) {
        id = new FileID(service, account, file.getFolderId(), file.getId()).toUniqueID();
        folder = new FolderID(service, account, file.getFolderId()).toUniqueID();
        this.file = file;
    }

    public boolean matches(String pattern, Field... fields) {
        return file.matches(pattern, fields);
    }

    public void copyFrom(File other) {
        file.copyFrom(other);
    }

    public void copyInto(File other) {
        file.copyInto(other);
    }

    public void copyFrom(File other, Field... fields) {
        file.copyFrom(other, fields);
    }

    public void copyInto(File other, Field... fields) {
        file.copyInto(other, fields);
    }

    public Set<Field> differences(File other) {
        return file.differences(other);
    }

    public File dup() {
        return file.dup();
    }

    public boolean equals(File other, Field criterium, Field... criteria) {
        return file.equals(other, criterium, criteria);
    }

    public String getCategories() {
        return file.getCategories();
    }

    public int getColorLabel() {
        return file.getColorLabel();
    }

    public String getContent() {
        return file.getContent();
    }

    public Date getCreated() {
        return file.getCreated();
    }

    public int getCreatedBy() {
        return file.getCreatedBy();
    }

    public String getDescription() {
        return file.getDescription();
    }

    public String getFileMD5Sum() {
        return file.getFileMD5Sum();
    }

    public String getFileMIMEType() {
        return file.getFileMIMEType();
    }

    public String getFileName() {
        return file.getFileName();
    }

    public long getFileSize() {
        return file.getFileSize();
    }

    public String getFolderId() {
        return folder;
    }

    public String getId() {
        return id;
    }

    public Date getLastModified() {
        return file.getLastModified();
    }

    public Date getLockedUntil() {
        return file.getLockedUntil();
    }

    public int getModifiedBy() {
        return file.getModifiedBy();
    }

    public int getNumberOfVersions() {
        return file.getNumberOfVersions();
    }

    public String getProperty(String key) {
        return file.getProperty(key);
    }

    public Set<String> getPropertyNames() {
        return file.getPropertyNames();
    }

    public long getSequenceNumber() {
        return file.getSequenceNumber();
    }

    public String getTitle() {
        return file.getTitle();
    }

    public String getURL() {
        return file.getURL();
    }

    public int getVersion() {
        return file.getVersion();
    }

    public String getVersionComment() {
        return file.getVersionComment();
    }

    public boolean isCurrentVersion() {
        return file.isCurrentVersion();
    }

    public void setCategories(String categories) {
        file.setCategories(categories);
    }

    public void setColorLabel(int color) {
        file.setColorLabel(color);
    }

    public void setCreated(Date creationDate) {
        file.setCreated(creationDate);
    }

    public void setCreatedBy(int cretor) {
        file.setCreatedBy(cretor);
    }

    public void setDescription(String description) {
        file.setDescription(description);
    }

    public void setFileMD5Sum(String sum) {
        file.setFileMD5Sum(sum);
    }

    public void setFileMIMEType(String type) {
        file.setFileMIMEType(type);
    }

    public void setFileName(String fileName) {
        file.setFileName(fileName);
    }

    public void setFileSize(long length) {
        file.setFileSize(length);
    }

    public void setFolderId(String folderId) {
        throw new IllegalStateException("IDs are only read only with this class");
    }

    public void setId(String id) {
        throw new IllegalStateException("IDs are only read only with this class");
    }

    public void setIsCurrentVersion(boolean bool) {
        file.setIsCurrentVersion(bool);
    }

    public void setLastModified(Date now) {
        file.setLastModified(now);
    }

    public void setLockedUntil(Date lockedUntil) {
        file.setLockedUntil(lockedUntil);
    }

    public void setModifiedBy(int lastEditor) {
        file.setModifiedBy(lastEditor);
    }

    public void setNumberOfVersions(int numberOfVersions) {
        file.setNumberOfVersions(numberOfVersions);
    }

    public void setTitle(String title) {
        file.setTitle(title);
    }

    public void setURL(String url) {
        file.setURL(url);
    }

    public void setVersion(int version) {
        file.setVersion(version);
    }

    public void setVersionComment(String string) {
        file.setVersionComment(string);
    }
}
