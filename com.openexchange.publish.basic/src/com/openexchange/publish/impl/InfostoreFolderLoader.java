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

package com.openexchange.publish.impl;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.publish.EscapeMode;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.Publications;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link InfostoreFolderLoader}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InfostoreFolderLoader implements PublicationDataLoaderService {

    private final InfostoreFacade infostore;

    public InfostoreFolderLoader(final InfostoreFacade infostore) {
        super();
        this.infostore = infostore;
    }

    @Override
    public Collection<? extends Object> load(Publication publication, EscapeMode escapeMode) throws OXException {
        int folderId = Integer.parseInt(publication.getEntityId());
        SearchIterator<DocumentMetadata> documentsInFolder = infostore.getDocuments(
            folderId, Metadata.HTTPAPI_VALUES_ARRAY, Metadata.TITLE_LITERAL, InfostoreFacade.ASC,
            ServerSessionAdapter.valueOf(publication.getUserId(), publication.getContext().getContextId())).results();
        try {
            LinkedList<Object> list = new LinkedList<Object>();
            while(documentsInFolder.hasNext()) {
                DocumentMetadata next = documentsInFolder.next();
                list.add(null != escapeMode && EscapeMode.NONE != escapeMode ? new EscapingDocumentMetadata(next, escapeMode) : next);
            }
            return list;
        } finally {
            SearchIterators.close(documentsInFolder);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    private static final class EscapingDocumentMetadata implements DocumentMetadata {

        private static final long serialVersionUID = 5986952103981970745L;

        private final DocumentMetadata documentMetadata;
        private final EscapeMode escapeMode;

        EscapingDocumentMetadata(DocumentMetadata documentMetadata, EscapeMode escapeMode) {
            super();
            this.documentMetadata = documentMetadata;
            this.escapeMode = escapeMode;
        }

        private String escape(String value) {
            return Publications.escape(value, escapeMode);
        }

        @Override
        public String getProperty(String key) {
            return escape(documentMetadata.getProperty(key));
        }

        @Override
        public Set<String> getPropertyNames() {
            return documentMetadata.getPropertyNames();
        }

        @Override
        public Date getLastModified() {
            return documentMetadata.getLastModified();
        }

        @Override
        public void setLastModified(Date now) {
            documentMetadata.setLastModified(now);
        }

        @Override
        public Date getCreationDate() {
            return documentMetadata.getCreationDate();
        }

        @Override
        public void setCreationDate(Date creationDate) {
            documentMetadata.setCreationDate(creationDate);
        }

        @Override
        public int getModifiedBy() {
            return documentMetadata.getModifiedBy();
        }

        @Override
        public void setModifiedBy(int lastEditor) {
            documentMetadata.setModifiedBy(lastEditor);
        }

        @Override
        public long getFolderId() {
            return documentMetadata.getFolderId();
        }

        @Override
        public void setFolderId(long folderId) {
            documentMetadata.setFolderId(folderId);
        }

        @Override
        public String getTitle() {
            return escape(documentMetadata.getTitle());
        }

        @Override
        public void setTitle(String title) {
            documentMetadata.setTitle(title);
        }

        @Override
        public int getVersion() {
            return documentMetadata.getVersion();
        }

        @Override
        public void setVersion(int version) {
            documentMetadata.setVersion(version);
        }

        @Override
        public String getContent() {
            return escape(documentMetadata.getContent());
        }

        @Override
        public long getFileSize() {
            return documentMetadata.getFileSize();
        }

        @Override
        public void setFileSize(long length) {
            documentMetadata.setFileSize(length);
        }

        @Override
        public String getFileMIMEType() {
            return escape(documentMetadata.getFileMIMEType());
        }

        @Override
        public void setFileMIMEType(String type) {
            documentMetadata.setFileMIMEType(type);
        }

        @Override
        public String getFileName() {
            return escape(documentMetadata.getFileName());
        }

        @Override
        public void setFileName(String fileName) {
            documentMetadata.setFileName(fileName);
        }

        @Override
        public int getId() {
            return documentMetadata.getId();
        }

        @Override
        public void setId(int id) {
            documentMetadata.setId(id);
        }

        @Override
        public int getCreatedBy() {
            return documentMetadata.getCreatedBy();
        }

        @Override
        public void setCreatedBy(int cretor) {
            documentMetadata.setCreatedBy(cretor);
        }

        @Override
        public String getDescription() {
            return escape(documentMetadata.getDescription());
        }

        @Override
        public void setDescription(String description) {
            documentMetadata.setDescription(description);
        }

        @Override
        public String getURL() {
            return escape(documentMetadata.getURL());
        }

        @Override
        public void setURL(String url) {
            documentMetadata.setURL(url);
        }

        @Override
        public long getSequenceNumber() {
            return documentMetadata.getSequenceNumber();
        }

        @Override
        public String getCategories() {
            return escape(documentMetadata.getCategories());
        }

        @Override
        public void setCategories(String categories) {
            documentMetadata.setCategories(categories);
        }

        @Override
        public Date getLockedUntil() {
            return documentMetadata.getLockedUntil();
        }

        @Override
        public void setLockedUntil(Date lockedUntil) {
            documentMetadata.setLockedUntil(lockedUntil);
        }

        @Override
        public String getFileMD5Sum() {
            return escape(documentMetadata.getFileMD5Sum());
        }

        @Override
        public void setFileMD5Sum(String sum) {
            documentMetadata.setFileMD5Sum(sum);
        }

        @Override
        public int getColorLabel() {
            return documentMetadata.getColorLabel();
        }

        @Override
        public void setColorLabel(int color) {
            documentMetadata.setColorLabel(color);
        }

        @Override
        public boolean isCurrentVersion() {
            return documentMetadata.isCurrentVersion();
        }

        @Override
        public void setIsCurrentVersion(boolean bool) {
            documentMetadata.setIsCurrentVersion(bool);
        }

        @Override
        public String getVersionComment() {
            return escape(documentMetadata.getVersionComment());
        }

        @Override
        public void setVersionComment(String string) {
            documentMetadata.setVersionComment(string);
        }

        @Override
        public void setFilestoreLocation(String string) {
            documentMetadata.setFilestoreLocation(string);
        }

        @Override
        public String getFilestoreLocation() {
            return escape(documentMetadata.getFilestoreLocation());
        }

        @Override
        public void setNumberOfVersions(int numberOfVersions) {
            documentMetadata.setNumberOfVersions(numberOfVersions);
        }

        @Override
        public int getNumberOfVersions() {
            return documentMetadata.getNumberOfVersions();
        }

        @Override
        public Map<String, Object> getMeta() {
            return documentMetadata.getMeta();
        }

        @Override
        public void setMeta(Map<String, Object> properties) {
            documentMetadata.setMeta(properties);
        }

        @Override
        public List<ObjectPermission> getObjectPermissions() {
            return documentMetadata.getObjectPermissions();
        }

        @Override
        public void setObjectPermissions(List<ObjectPermission> objectPermissions) {
            documentMetadata.setObjectPermissions(objectPermissions);
        }

        @Override
        public boolean isShareable() {
            return documentMetadata.isShareable();
        }

        @Override
        public void setShareable(boolean shareable) {
            documentMetadata.setShareable(shareable);
        }

        @Override
        public int getOriginalId() {
            return documentMetadata.getOriginalId();
        }

        @Override
        public void setOriginalId(int id) {
            documentMetadata.setOriginalId(id);
        }

        @Override
        public long getOriginalFolderId() {
            return documentMetadata.getOriginalFolderId();
        }

        @Override
        public void setOriginalFolderId(long id) {
            documentMetadata.setOriginalFolderId(id);
        }
    }

}
