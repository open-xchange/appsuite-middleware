package com.openexchange.file.storage.infostore;

import java.util.Date;
import java.util.Set;
import com.openexchange.file.storage.AbstractFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
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

/**
 * {@link InfostoreFile}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFile extends AbstractFile implements File {

    private DocumentMetadata document;

    public InfostoreFile(DocumentMetadata documentMetadata) {
        this.document = documentMetadata;
    }

   
    public String getCategories() {
        return document.getCategories();
    }

   
    public int getColorLabel() {
        return document.getColorLabel();
    }

   
    public String getContent() {
        return document.getContent();
    }

   
    public Date getCreated() {
        return document.getCreationDate();
    }

   
    public int getCreatedBy() {
        return document.getCreatedBy();
    }

   
    public String getDescription() {
        return document.getDescription();
    }

   
    public String getFileMD5Sum() {
        return document.getFileMD5Sum();
    }

   
    public String getFileMIMEType() {
        return document.getFileMIMEType();
    }

   
    public String getFileName() {
        return document.getFileName();
    }

   
    public long getFileSize() {
        return document.getFileSize();
    }

   
    public String getFolderId() {
        return String.valueOf(document.getFolderId());
    }

   
    public String getId() {
        return String.valueOf(document.getId());
    }

   
    public Date getLastModified() {
        return document.getLastModified();
    }

   
    public Date getLockedUntil() {
        return document.getLockedUntil();
    }

   
    public int getModifiedBy() {
        return document.getModifiedBy();
    }

   
    public int getNumberOfVersions() {
        return document.getNumberOfVersions();
    }

   
    public String getProperty(String key) {
        return document.getProperty(key);
    }

   
    public Set<String> getPropertyNames() {
        return document.getPropertyNames();
    }

   
    public long getSequenceNumber() {
        return document.getSequenceNumber();
    }

   
    public String getTitle() {
        return document.getTitle();
    }

   
    public String getURL() {
        return document.getURL();
    }

   
    public int getVersion() {
        return document.getVersion();
    }

   
    public String getVersionComment() {
        return document.getVersionComment();
    }

   
    public boolean isCurrentVersion() {
        return document.isCurrentVersion();
    }

   
    public void setCategories(String categories) {
        document.setCategories(categories);
    }

   
    public void setColorLabel(int color) {
        document.setColorLabel(color);
    }

   
    public void setCreatedBy(int cretor) {
        document.setCreatedBy(cretor);
    }

   
    public void setCreated(Date creationDate) {
        document.setCreationDate(creationDate);
    }

   
    public void setDescription(String description) {
        document.setDescription(description);
    }

   
    public void setFileMD5Sum(String sum) {
        document.setFileMD5Sum(sum);
    }

   
    public void setFileMIMEType(String type) {
        document.setFileMIMEType(type);
    }

   
    public void setFileName(String fileName) {
        document.setFileName(fileName);
    }

   
    public void setFileSize(long length) {
        document.setFileSize(length);
    }

   
    public void setFolderId(String folderId) {
        if(folderId != null) {
            document.setFolderId(Long.parseLong(folderId));
        }
    }

   
    public void setId(String id) {
        if(id == FileStorageFileAccess.NEW) {
            document.setId(InfostoreFacade.NEW);
        } else {
            document.setId(Integer.parseInt(id));
        }
    }

   
    public void setIsCurrentVersion(boolean bool) {
        document.setIsCurrentVersion(bool);
    }

   
    public void setLastModified(Date now) {
        document.setLastModified(now);
    }

   
    public void setLockedUntil(Date lockedUntil) {
        document.setLockedUntil(lockedUntil);
    }

   
    public void setModifiedBy(int lastEditor) {
        document.setModifiedBy(lastEditor);
    }

   
    public void setNumberOfVersions(int numberOfVersions) {
        document.setNumberOfVersions(numberOfVersions);
    }

   
    public void setTitle(String title) {
        document.setTitle(title);
    }

   
    public void setURL(String url) {
        document.setURL(url);
    }

   
    public void setVersion(int version) {
        document.setVersion(version);
    }

   
    public void setVersionComment(String string) {
        document.setVersionComment(string);
    }

    
}
