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

package com.openexchange.file.storage.webdav;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.search.AndTerm;
import com.openexchange.file.storage.search.CategoriesTerm;
import com.openexchange.file.storage.search.ColorLabelTerm;
import com.openexchange.file.storage.search.ContentTerm;
import com.openexchange.file.storage.search.CreatedByTerm;
import com.openexchange.file.storage.search.CreatedTerm;
import com.openexchange.file.storage.search.CurrentVersionTerm;
import com.openexchange.file.storage.search.DescriptionTerm;
import com.openexchange.file.storage.search.FileMd5SumTerm;
import com.openexchange.file.storage.search.FileMimeTypeTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.FileSizeTerm;
import com.openexchange.file.storage.search.LastModifiedTerm;
import com.openexchange.file.storage.search.LastModifiedUtcTerm;
import com.openexchange.file.storage.search.LockedUntilTerm;
import com.openexchange.file.storage.search.MetaTerm;
import com.openexchange.file.storage.search.ModifiedByTerm;
import com.openexchange.file.storage.search.NotTerm;
import com.openexchange.file.storage.search.NumberOfVersionsTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.SearchTermVisitor;
import com.openexchange.file.storage.search.SequenceNumberTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.file.storage.search.UrlTerm;
import com.openexchange.file.storage.search.VersionCommentTerm;
import com.openexchange.file.storage.search.VersionTerm;

/**
 * {@link WebDAVSearchVisitor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class WebDAVSearchVisitor implements SearchTermVisitor {

    private static final class Key {

        final String folderId;
        final String id;

        Key(final String id, final String folderId) {
            super();
            this.id = id;
            this.folderId = folderId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (folderId == null) {
                if (other.folderId != null) {
                    return false;
                }
            } else if (!folderId.equals(other.folderId)) {
                return false;
            }
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }

    }

    // ------------------------------------------------------------------------------------------------------ //

    private final Set<String> folderIds;
    private final WebDAVFileStorageFileAccess fileAccess;
    private final List<Field> fields;
    private Map<Key, File> results;

    /**
     * Initializes a new {@link WebDAVSearchVisitor}.
     *
     * @param fields The fields to fill
     * @param fileAccess The file access to load files with
     */
    public WebDAVSearchVisitor(final List<Field> fields, final WebDAVFileStorageFileAccess fileAccess) {
        this(Collections.<String> emptyList(), fields, fileAccess);
    }

    /**
     * Initializes a new {@link WebDAVSearchVisitor}.
     */
    public WebDAVSearchVisitor(final List<String> folderIds, final List<Field> fields, final WebDAVFileStorageFileAccess fileAccess) {
        super();
        this.folderIds = folderIds == null ? Collections.<String> emptySet() : new LinkedHashSet<String>(folderIds);
        this.fileAccess = fileAccess;
        this.fields = fields;
    }

    /**
     * Gets the results in arbitrary order.
     *
     * @return The results
     */
    public List<File> getResults() {
        return new LinkedList<File>(results.values());
    }

    private void searchByTerm(final SearchTerm<?> term) throws OXException {
        if (folderIds.isEmpty()) {
            final List<File> results = new LinkedList<File>();
            fileAccess.recursiveSearchFile(term, fileAccess.getRootUri(), fields, results);
            final Map<Key, File> map = new HashMap<Key, File>(results.size());
            for (final File file : results) {
                map.put(new Key(file.getId(), file.getFolderId()), file);
            }
            this.results = map;
        } else {
            final List<File> results = new LinkedList<File>();
            for (final String folderId : folderIds) {
                final List<File> files = fileAccess.getFileList(folderId, fields);
                for (final File candidate : files) {
                    if (term.matches(candidate)) {
                        results.add(candidate);
                    }
                }
            }
            final Map<Key, File> map = new HashMap<Key, File>(results.size());
            for (final File file : results) {
                map.put(new Key(file.getId(), file.getFolderId()), file);
            }
            this.results = map;
        }
    }

    @Override
    public void visit(final AndTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final OrTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final NotTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final MetaTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final NumberOfVersionsTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final LastModifiedUtcTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final ColorLabelTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final CurrentVersionTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final VersionCommentTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final FileMd5SumTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final LockedUntilTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final CategoriesTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final SequenceNumberTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final FileMimeTypeTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final FileNameTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final LastModifiedTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final CreatedTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final ModifiedByTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final TitleTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final VersionTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final ContentTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final FileSizeTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final DescriptionTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final UrlTerm term) throws OXException {
        searchByTerm(term);
    }

    @Override
    public void visit(final CreatedByTerm term) throws OXException {
        searchByTerm(term);
    }

}
