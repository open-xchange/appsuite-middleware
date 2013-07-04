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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Set;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;


/**
 * {@link FileMetadata}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileMetadata implements DocumentMetadata {

    private final File file;
    private String fileSpool;

    public FileMetadata(final File document) {
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
        return isEmpty(version) ? -1 : Integer.parseInt(version);
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

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
