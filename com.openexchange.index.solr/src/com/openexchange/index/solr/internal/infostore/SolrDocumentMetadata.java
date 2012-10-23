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

package com.openexchange.index.solr.internal.infostore;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import com.openexchange.groupware.infostore.DocumentMetadata;


/**
 * {@link SolrDocumentMetadata}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrDocumentMetadata implements DocumentMetadata {

    private static final long serialVersionUID = -4095262954539802227L;
    

    private Date lastModified;
    
    private Date creationDate;
    
    private int modifiedBy;
    
    private long folderId;
    
    private int id;

    private String title;

    private int version;

    private int createdBy;

    private String description;

    private String url;

    private long sequenceNumber;

    private String categories;

    private int colorLabel;

    private String versionComment;

    private String filestoreLocation;

    private int numberOfVersions;
        

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return Collections.EMPTY_SET;
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
    public void setModifiedBy(int lastEditor) {
        this.modifiedBy = lastEditor;
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
        return null;
    }

    @Override
    public long getFileSize() {
        return 0;
    }

    @Override
    public void setFileSize(long length) {

    }

    @Override
    public String getFileMIMEType() {
        return null;
    }

    @Override
    public void setFileMIMEType(String type) {

    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public void setFileName(String fileName) {

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
        return null;
    }

    @Override
    public void setLockedUntil(Date lockedUntil) {

    }

    @Override
    public String getFileMD5Sum() {
        return null;
    }

    @Override
    public void setFileMD5Sum(String sum) {

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
        return true;
    }

    @Override
    public void setIsCurrentVersion(boolean bool) {

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
    public void setFilestoreLocation(String filestoreLocation) {
        this.filestoreLocation = filestoreLocation;
    }

    @Override
    public String getFilestoreLocation() {
        return filestoreLocation;
    }

    @Override
    public void setNumberOfVersions(int numberOfVersions) {
        this.numberOfVersions = numberOfVersions;
    }

    @Override
    public int getNumberOfVersions() {
        return numberOfVersions;
    }

}
